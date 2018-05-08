package org.apache.dhval.wss;

import org.apache.dhval.utils.SaxonUtils;
import org.apache.dhval.utils.Utils;
import org.apache.tcpmon.TCPMon;
import org.apache.ws.security.WSSecurityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.client.core.WebServiceMessageCallback;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.soap.SoapVersion;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.xml.transform.StringResult;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPConstants;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

public class WSSClient {
    private static final Logger LOG = LoggerFactory.getLogger(WSSClient.class);

    public static String post(String url, String data, Map<String, String> headers, String selWSS4JProfile) throws Exception {
        Map jsonMap = TCPMon.jsonMap;
        // Check any WS-Security profile is selected
        if (jsonMap != null && jsonMap.containsKey("wss4j-profiles")) {
            Map<String, Object> map = (Map<String, Object>) jsonMap.get("wss4j-profiles");
            if (map != null && map.containsKey(selWSS4JProfile)) {
                return WSSClient.wssPost(url, data, headers, (Map<String, String>) map.get(selWSS4JProfile));
            }
        }
        // Use vanilla HTTP Post
        return httpPost(url, data, headers);
    }

    public static String wssPost(String url, String srcXml, Map<String, String> headers, Map<String, String> wss4jProfile) throws Exception {
        try {
            WebServiceTemplate template = new WebServiceTemplate();

            /** **/
            SaajSoapMessageFactory messageFactory = new SaajSoapMessageFactory();
            messageFactory.setSoapVersion(SoapVersion.SOAP_12);
            messageFactory.afterPropertiesSet();

       //     MessageFactory messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
        //    WebServiceMessageFactory webMessageFactory = new SaajSoapMessageFactory(messageFactory);
         //   template.setMessageFactory(messageFactory);


            addInterceptor(wss4jProfile, template);

            WebServiceMessageCallback callback = (WebServiceMessage message) -> {
                //((SoapMessage)message).setSoapAction("");
                SaajSoapMessage soapMessage = (SaajSoapMessage) message;
                MimeHeaders mimeHeader = soapMessage.getSaajMessage().getMimeHeaders();
                headers.entrySet().stream().forEach(e -> mimeHeader.setHeader(e.getKey(), e.getValue()));
            };

            StringBuilder sb = new StringBuilder();
            template.setFaultMessageResolver((WebServiceMessage webServiceMessage) -> {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                webServiceMessage.writeTo(stream);
                sb.append(new String(stream.toByteArray()));
            });

            // The afterPropertiesSet or @PostConstruct annotated method is called after an instance of class is created.
            template.afterPropertiesSet();
            StreamSource requestMessage = new StreamSource(new StringReader(SaxonUtils.extractSoapBody(srcXml)));
            StringResult responseResult = new StringResult();

            template.sendSourceAndReceiveToResult(url, requestMessage, callback, responseResult);
            sb.append(responseResult.toString());
            return sb.toString();
        } catch (WSSecurityException wse) {
            return Utils.printStackTrace(wse);
        }
    }

    private static void addInterceptor(Map<String, String> profile, WebServiceTemplate template) throws Exception {
        ClientInterceptor interceptor = null;
        switch (profile.get("name")) {
            case "UserNameToken":
                interceptor = WSS4JInterceptor.userNameTokenInterceptor(profile);
                break;
            case "SAMLTokenSigned":
                interceptor = WSS4JInterceptor.signedSAMLAssertion(profile);
                break;
            case "DigitalSignature":
                interceptor = WSS4JInterceptor.signedX509(profile);
                break;
        }
        if (interceptor != null)
            template.setInterceptors(new ClientInterceptor[]{interceptor});
    }

    public static String httpPost(String url, String data, Map<String, String> headers) throws Exception {
        URL u = new URL(url);
        URLConnection uc = u.openConnection();
        HttpURLConnection connection = (HttpURLConnection) uc;
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setRequestMethod("POST");
        headers.entrySet().stream().forEach(e -> {
            connection.setRequestProperty(e.getKey(), e.getValue());
        });
        OutputStream out = connection.getOutputStream();
        Writer writer = new OutputStreamWriter(out);
        writer.write(data);
        writer.flush();
        writer.close();
        StringBuffer sb = new StringBuffer();
        String line;
        InputStream inputStream = null;
        try {
            inputStream = connection.getInputStream();
        } catch (IOException e) {
            inputStream = connection.getErrorStream();
        }
        BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream));
        while ((line = rd.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }

}
