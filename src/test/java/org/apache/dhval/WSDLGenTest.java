package org.apache.dhval;

import org.apache.dhval.utils.Utils;
import org.apache.dhval.utils.XMLUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.junit4.SpringRunner;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RunWith(SpringRunner.class)
public class WSDLGenTest {
    private static final Logger LOG = LoggerFactory.getLogger(WSDLGenTest.class);

    private Document document;

    private static final String SCHEMA_PATH = "/Users/dhval/projects/xsl-tool/ssp/ERInmate";

    public static final String TARGET_NS = "http://jnet.state.pa.us/service/jnet/ElectronicReporting/1.0";
    public static final String SOAP_ACTION = "http://jnet.state.pa.us/service/jnet/ElectronicReporting/1.0/PublishCountyInmateEvent";
    public static final String ENDPOINT_URL = "https://jnet.state.pa.us:51003/soap/ElectronicReporting";
    public static final String SOAP_REQUEST = "PublishCountyInmateEventRequest";
    public static final String SOAP_RESPONSE = "PublishCountyInmateEventResponse";
    public static final String SOAP_FAULT = "ElectronicReportingApplicationFault";
    public static final String SOAP_BINDING = "ElectronicReportingSOAPBinding";
    public static final String SERVICE_NAME = "ElectronicReportingService";
    public static final String PORT_NAME = "ElectronicReportingPort";
    public static final String OPERATION_NAME = "PublishCountyInmateEvent";
    public static final String PORT_TYPE = "ElectronicReportingInterface";

    @Test
    public void createWSDL() throws Exception {
        Path resourceDirectory = Paths.get("tmp");
        File file2 = new File(resourceDirectory.toFile(), "example.xml");



        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();

        document = db.newDocument();

        Element definitions = create("http://schemas.xmlsoap.org/wsdl/", "definitions");
        getNSMap().entrySet().forEach(entry ->
                definitions.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:" + entry.getValue(), entry.getKey())
        );
        definitions.setAttribute("targetNamespace", TARGET_NS);
        document.appendChild(definitions);

        Element types = create("http://schemas.xmlsoap.org/wsdl/", "types");
        importSchema(types);
        definitions.appendChild(types);

        definitions.appendChild(createMessage(SOAP_RESPONSE, "PublishCountyInmateEventResponse"));
        definitions.appendChild(createMessage(SOAP_REQUEST, "PublishCountyInmateEventRequest"));
        definitions.appendChild(createMessage(SOAP_FAULT, "ElectronicReportingApplicationFault"));

        definitions.appendChild(createPortType(PORT_TYPE, OPERATION_NAME, SOAP_REQUEST, SOAP_RESPONSE, SOAP_FAULT));

        Element binding = createBinding(SOAP_BINDING, PORT_TYPE, OPERATION_NAME, SOAP_ACTION, SOAP_FAULT);
        definitions.appendChild(binding);

        definitions.appendChild(createService(SERVICE_NAME, PORT_NAME, SOAP_BINDING, ENDPOINT_URL));

        XMLUtil.saveDOMNode(document, file2.getAbsolutePath());
    }

    private Element createMessage(String name, String element) {
        Element message = create("http://schemas.xmlsoap.org/wsdl/", "message","name", name);
        Element part = create("http://schemas.xmlsoap.org/wsdl/", "part","name", name, "element", element);
        message.appendChild(part);
        return message;
    }

    private Element createPortType(String name, String operationName, String inputName, String outputName, String faultName) {
        Element portType = create("http://schemas.xmlsoap.org/wsdl/", "portType","name", name);
        Element operation = create("http://schemas.xmlsoap.org/wsdl/", "operation","name", operationName);
        Element input = create("http://schemas.xmlsoap.org/wsdl/", "input","message", "tns:" + inputName);
        Element output = create("http://schemas.xmlsoap.org/wsdl/", "output","message", "tns:" + outputName);
        Element fault = create("http://schemas.xmlsoap.org/wsdl/", "fault","name", faultName, "message", "tns:" + faultName);

        operation.appendChild(input);
        operation.appendChild(output);
        operation.appendChild(fault);

        portType.appendChild(operation);
        return portType;
    }

    private Element createService(String serviceName, String portName, String bindingName, String location) {
        Element service = create("http://schemas.xmlsoap.org/wsdl/", "service", "name", serviceName);
        Element port = create("http://schemas.xmlsoap.org/wsdl/", "port","name", portName, "binding", "tns:" + bindingName);
        Element address = create("http://schemas.xmlsoap.org/wsdl/soap/", "address","location", location);
        port.appendChild(address);
        service.appendChild(port);
        return service;
    }

    private Element createBinding(String name, String type, String operationName, String soapAction, String faultName) {
        Element binding = create("http://schemas.xmlsoap.org/wsdl/", "binding","name", name, "type", "tns:" + type);
        Element soapBinding = create("http://schemas.xmlsoap.org/wsdl/soap/", "binding","style", "document", "transport", "http://schemas.xmlsoap.org/soap/http");
        Element operation = create("http://schemas.xmlsoap.org/wsdl/", "operation","name", operationName);
        Element soapOperation = create("http://schemas.xmlsoap.org/wsdl/soap/", "operation","soapAction", soapAction);
        Element input = create("http://schemas.xmlsoap.org/wsdl/", "input");
        Element soapInput = create("http://schemas.xmlsoap.org/wsdl/soap/", "body","use", "literal");
        input.appendChild(soapInput);
        Element output = create("http://schemas.xmlsoap.org/wsdl/", "output");
        Element soapOutput = create("http://schemas.xmlsoap.org/wsdl/soap/", "body","use", "literal");
        output.appendChild(soapOutput);
        Element fault = create("http://schemas.xmlsoap.org/wsdl/", "fault","name", faultName);
        Element soapFault = create("http://schemas.xmlsoap.org/wsdl/soap/", "fault", "name", faultName, "use", "literal");
        fault.appendChild(soapFault);

        operation.appendChild(soapOperation);
        operation.appendChild(input);
        operation.appendChild(output);
        operation.appendChild(fault);

        binding.appendChild(soapBinding);
        binding.appendChild(operation);
        return binding;
    }

    private Element create(String ns, String name, String ... attrs) {
        Element element = document.createElementNS(ns, name);
        element.setPrefix(getNSMap().get(ns));
        for(int i=0; i<attrs.length; i=i+2)
            element.setAttribute(attrs[i], attrs[i+1]);
        return element;
    }

    private void importSchema(Element types) throws Exception {
        List<String> files = Utils.allFilesByType(Paths.get(SCHEMA_PATH).toString(), "xsd");
        for(String file : files) {
            String content = new String (Files.readAllBytes(new File(file).toPath()), Charset.forName("UTF-8"));
            Node node = XMLUtil.evaluateNodeForNode(content, "//*[local-name()='schema']");
            if (node == null) continue;
            Node tmp = document.importNode(node, true);
            //remove schema location attribute
            List<Node> nodeList = new ArrayList<>();
            nodeList.add(tmp);
            while(!nodeList.isEmpty()) {
               Node current = nodeList.remove(0);
               if (current.getNodeType()  == Node.ELEMENT_NODE) {
                   Element element = (Element) current;
                   if (element.hasAttribute("schemaLocation"))
                       element.removeAttribute("schemaLocation");
                   NodeList nodeList1 = current.getChildNodes();
                   for(int i=0; i<nodeList1.getLength(); i++)
                       nodeList.add(nodeList1.item(i));
               }
            }
            types.appendChild(tmp);
        }
    }

    private  Map<String, String> getNSMap() {
        return Stream.of(
                new AbstractMap.SimpleEntry<>("http://schemas.xmlsoap.org/wsdl/soap/", "soap"),
                new AbstractMap.SimpleEntry<>("http://schemas.xmlsoap.org/wsdl/", "wsdl"),
                new AbstractMap.SimpleEntry<>(TARGET_NS, "tns"),
                new AbstractMap.SimpleEntry<>("http://www.w3.org/2001/XMLSchema", "xs")
        ).collect(Collectors.toMap((e) -> e.getKey(), (e) -> e.getValue()));
    }

}