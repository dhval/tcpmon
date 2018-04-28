package org.apache.dhval.utils;

import net.sf.saxon.s9api.*;
import net.sf.saxon.trans.XPathException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;

public class SaxonUtils {
    private static final Logger LOG = LoggerFactory.getLogger(SaxonUtils.class);
    private static Processor proc = new Processor(false);
    private static XPathCompiler xpath = proc.newXPathCompiler();

    static {
        xpath.declareNamespace("jnet", "http://www.jnet.state.pa.us/niem/JNET/jnet-core/1");
        xpath.declareNamespace("aopc", "http://jnet.state.pa.us/jxdm/aopc");
    }

    public static XPathSelector getXPathSelector(String pathToInputFile, String expression)
            throws SaxonApiException, FileNotFoundException {

        DocumentBuilder builder = proc.newDocumentBuilder();
        builder.setLineNumbering(true);
        builder.setWhitespaceStrippingPolicy(WhitespaceStrippingPolicy.ALL);
        XdmNode xmlDoc = builder.build(new File(pathToInputFile));
        XPathSelector selector = xpath.compile(expression).load();
        selector.setContextItem(xmlDoc);
        return selector;
    }

    public static XPathSelector getXPathSelectorFromString(String input, String expression)
            throws SaxonApiException {
        DocumentBuilder builder = proc.newDocumentBuilder();
        builder.setLineNumbering(true);
        builder.setWhitespaceStrippingPolicy(WhitespaceStrippingPolicy.ALL);

        StringReader reader = new StringReader(input);
        XdmNode xmlDoc = builder.build(new StreamSource(reader));

        XPathSelector selector = xpath.compile(expression).load();
        selector.setContextItem(xmlDoc);
        return selector;
    }

    public static String extractSoapBody(String xml) throws SaxonApiException {
        XPathSelector selector = getXPathSelectorFromString(xml, "//*[local-name()='Body']/*[1]");
        XdmValue xdmValue = selector.evaluate();
        if (xdmValue != null && xdmValue.iterator().hasNext())
            return  xdmValue.toString();
        return xml;
    }

    public static Element createDOMElement(String file, String xPath)
            throws ParserConfigurationException, SaxonApiException, FileNotFoundException, SAXException, IOException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setNamespaceAware(true);
        javax.xml.parsers.DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        XPathSelector selector = SaxonUtils.getXPathSelector(file, xPath);

        Document doc = dBuilder.parse( new InputSource(new StringReader(selector.evaluate().toString())));
        doc.getDocumentElement().normalize();

        Element element = doc.getDocumentElement();
        //Add more namespaces
        element.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:aopc","http://jnet.state.pa.us/jxdm/aopc");
        element.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:aopc-doc","http://us.pacourts.us/niem/aopc/CourtFiling/1");
        element.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:aopc-ext","http://us.pacourts.us/niem/aopc/Extension/2");
        element.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:nc","http://niem.gov/niem/niem-core/2.0");

        return element;
    }

}
