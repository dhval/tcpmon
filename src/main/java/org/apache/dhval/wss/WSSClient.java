package org.apache.dhval.wss;

import org.apache.dhval.utils.Utils;
import org.apache.ws.security.WSSecurityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.soap.SoapVersion;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.xml.transform.StringResult;

import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.util.Map;

public class WSSClient {
    private static final Logger LOG = LoggerFactory.getLogger(WSSClient.class);

    public String post(String url, String srcXml, Map<String, String> wss4jProfile) throws Exception {
        try {
            WebServiceTemplate template = new WebServiceTemplate();
            SaajSoapMessageFactory messageFactory = new SaajSoapMessageFactory();
            messageFactory.setSoapVersion(SoapVersion.SOAP_12);
            messageFactory.afterPropertiesSet();
            // template.setMessageFactory(messageFactory);
            addInterceptor(wss4jProfile, template);
            // The afterPropertiesSet or @PostConstruct annotated method is called after an instance of class is created.
            template.afterPropertiesSet();
            StreamSource requestMessage = new StreamSource(new StringReader(srcXml));
            StringResult responseResult = new StringResult();
            //InputStream stream = new ByteArrayInputStream(srcXml.getBytes(StandardCharsets.UTF_8));
            //InputStream inputStream = new ByteArrayInputStream(srcXml.getBytes());
            //Source source =  messageFactory.createWebServiceMessage(inputStream).getPayloadSource();
            template.sendSourceAndReceiveToResult(url, requestMessage, responseResult);
            return responseResult.toString();
        } catch (WSSecurityException wse) {
            return Utils.printStackTrace(wse);
        }
    }

    private void addInterceptor(Map<String, String> profile, WebServiceTemplate template) throws Exception {
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

}
