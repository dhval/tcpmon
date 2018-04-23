package org.apache.dhval.wss;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.soap.SoapVersion;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.xml.transform.StringResult;

import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;

public class WSSClient {
    private static final Logger LOG = LoggerFactory.getLogger(WSSClient.class);

    public String post(String url, String srcXml, ClientInterceptor interceptor) throws Exception {
        WebServiceTemplate template = new WebServiceTemplate();
        SaajSoapMessageFactory messageFactory = new SaajSoapMessageFactory();
        messageFactory.setSoapVersion(SoapVersion.SOAP_12);
        messageFactory.afterPropertiesSet();
       // template.setMessageFactory(messageFactory);

        template.setInterceptors(new ClientInterceptor[] {interceptor});


        // The afterPropertiesSet or @PostConstruct annotated method is called after an instance of class is created.
        template.afterPropertiesSet();


//        Element source = SaxonUtils.createDOMElement("", "");

        StreamSource requestMessage = new StreamSource(new StringReader(srcXml));
        StringResult responseResult = new StringResult();

        //InputStream stream = new ByteArrayInputStream(srcXml.getBytes(StandardCharsets.UTF_8));
        //InputStream inputStream = new ByteArrayInputStream(srcXml.getBytes());
        //Source source =  messageFactory.createWebServiceMessage(inputStream).getPayloadSource();


        template.sendSourceAndReceiveToResult(url, requestMessage, responseResult);

        return responseResult.toString();
    }

}
