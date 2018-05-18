package org.apache.dhval.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utility class for working with XML documents and XPath expressions.
 */
public class XMLUtil {

    private static final Logger LOG = LoggerFactory.getLogger(XMLUtil.class);
    private static final String XMLNS_XSI = "xmlns:xsi";
    private static final String XSI_SCHEMA_LOCATION = "xsi:schemaLocation";

    /**
     * Moves all namepace declarations to root element.
     *
     * @param document
     * @return
     */
    public static Document canonicalNS(Document document, NamespaceCache cache) {
        Element root = document.getDocumentElement();
        cache.getPrefix2Uri().entrySet().stream().filter(entry -> !entry.getKey().equals(NamespaceCache.DEFAULT_NS)).forEach(e -> {
            root.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:" + e.getKey(), e.getValue());
        });
        List<Node> list = new ArrayList<>();
        list.add(root);
        while (!list.isEmpty()) {
            Node node = list.remove(0);
            node.setPrefix(cache.getPrefix(node.getNamespaceURI()));
            ((Element) node).removeAttribute("xmlns");
            NodeList nodeList = node.getChildNodes();
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node item = nodeList.item(i);
                if (item.getNodeType() == Node.ELEMENT_NODE)
                    list.add(item);
            }
        }
        return root.getOwnerDocument();
    }

    public static Document canonicalNS(Document document) {
        NamespaceCache cache = new NamespaceCache(document, false);
        return canonicalNS(document, cache);
    }


    public static Document createDOMNode(String file)
            throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setNamespaceAware(true);
        javax.xml.parsers.DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

        Document doc = dBuilder.parse(new InputSource(new FileInputStream(new File(file))));
        doc.getDocumentElement().normalize();

        return doc;
    }

    public static void saveDOMNode(Document doc, String file)
            throws ParserConfigurationException, SAXException, IOException, TransformerException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new File(file));
        transformer.transform(source, result);
    }

    public static NamespaceContext createNamespaceContext() {
        return new NamespaceContext() {
            public String getNamespaceURI(String prefix) {
                if (prefix == null)
                    throw new IllegalArgumentException("No prefix provided!");
                else if ("soap".equals(prefix))
                    return "http://schemas.xmlsoap.org/wsdl/soap/";
                else if ("xml".equals(prefix))
                    return XMLConstants.XML_NS_URI;
                return XMLConstants.NULL_NS_URI;
            }

            // This method isn't necessary for XPath processing.
            public String getPrefix(String uri) {
                throw new UnsupportedOperationException();
            }

            // This method isn't necessary for XPath processing either.
            public Iterator getPrefixes(String uri) {
                throw new UnsupportedOperationException();
            }
        };
    }

    public static Document transform(Document document, Map<String, String> map) throws Exception {
       XPathFactory xpathfactory = XPathFactory.newInstance();
        XPath xpath = xpathfactory.newXPath();

        NamespaceContext namespaceContext = new NamespaceCache(document, false);
        xpath.setNamespaceContext(namespaceContext);

        map.entrySet().stream().forEach(
                entry -> {
                    try {
                        XPathExpression expression = xpath.compile(entry.getKey());
                        Node node = (Node) expression.evaluate(document, XPathConstants.NODE);
                        if (node != null) node.setTextContent(entry.getValue());
                    } catch (XPathExpressionException exp) {
                        LOG.warn(exp.getMessage());
                    }
                }
        );
        return document;
    }

    public static String toString(Document document) throws Exception {
        StringWriter writer = new StringWriter();
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.transform(new DOMSource(document), new StreamResult(writer));
        return writer.toString();
    }

    public static Map<String, String> extractXpath(Node root, Map<String, String> xpaths)
            throws ParserConfigurationException, IOException, SAXException, XPathExpressionException {

        NamespaceCache namespaceCache = new NamespaceCache(root.getOwnerDocument(), false);
        XPathFactory xpathfactory = XPathFactory.newInstance();
        XPath xpath = xpathfactory.newXPath();
        xpath.setNamespaceContext(namespaceCache);

        XPathExpression expression;
        Map<String, String> result = new HashMap<>();
        for(String key: xpaths.keySet()) {
            expression = xpath.compile(xpaths.get(key));
            String value = (String) expression.evaluate(root, XPathConstants.STRING);
            result.put(key, value);
        }

        return result;
    }

    public static void extractXMLFromText(String content, String tag, String dstDir) throws IOException {
        // allow any attributes in root element.
        String startTag = "<" + tag;
        String endTag = "</" + tag + ">";

        Pattern p = Pattern.compile(startTag + "[\\s\\S]*?" + endTag); // [\s\S] captures multi line
        Matcher m = p.matcher(content);
        int counter  = 1;
        while (m.find()) {
            String text = m.group(0);
            Files.write(Paths.get(dstDir + "/file-" + (counter++) + ".xml"), text.getBytes());
        }
    }

    /**
     * Compiles an XPath expression for later evaluation.
     *
     * @param expression       the expression to compile as a String
     * @param namespaceContext the namespace context to use
     * @return the compiled XPathExpression object
     * @throws XPathExpressionException if the expression does not compile
     */
    public static XPathExpression compile(String expression,
                                          NamespaceContext namespaceContext) throws XPathExpressionException {
        return createXPath(namespaceContext, null).compile(expression);
    }

    /**
     * Compiles an XPath expression for later evaluation.
     *
     * @param expression       the expression to compile as a String
     * @param namespaceContext the namespace context to use
     * @param functionResolver the function resolver to use
     * @return the compiled XPathExpression object
     * @throws XPathExpressionException if the expression does not compile
     */
    public static XPathExpression compile(String expression,
                                          NamespaceContext namespaceContext,
                                          XPathFunctionResolver functionResolver) throws XPathExpressionException {
        return createXPath(namespaceContext, functionResolver).compile(expression);
    }

    /**
     * Evaluates the XPath expression in the specified context and returns whether such element was found.
     *
     * @param node       the XML document to evaluate
     * @param expression the compiled XPath expression
     * @return <code>true</code> if the given expression evaluates to an existing element in the given node,
     * <code>false</code> otherwise
     */
    public static boolean evaluate(Node node, XPathExpression expression) {
        try {
            Boolean result = (Boolean) expression.evaluate(node, XPathConstants.BOOLEAN);
            return result != null && result;
        } catch (XPathExpressionException e) {
            return false;
        }
    }

    /**
     * Evaluates the XPath expression in the specified context and returns the found element as a String.
     *
     * @param node       the XML document to evaluate
     * @param expression the compiled XPath expression
     * @return the element if it was found, or null
     */
    public static String getStringValue(Node node, XPathExpression expression) {
        try {
            return (String) expression.evaluate(node, XPathConstants.STRING);
        } catch (XPathExpressionException e) {
            return null;
        }
    }

    public static void buildXpathMap(String path, Node node, Map<String, String> map) {
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            String tPath = path + "/" + node.getNodeName() + "[0]";
            int childCounter = 1;
            while (map.containsKey(tPath)) {
                tPath = path + "/" + node.getNodeName() + "[" + (childCounter++) + "]";
            }
            map.put(tPath, "");
            NodeList nodeList = node.getChildNodes();
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node item = nodeList.item(i);
                if (item.getNodeType() == Node.ELEMENT_NODE || item.getNodeType() == Node.TEXT_NODE)
                    buildXpathMap(tPath, item, map);
            }
        } else  if (node.getNodeType() == Node.TEXT_NODE) {
            map.put(path, node.getTextContent().trim());
        }
    }

    /**
     * Sets the namespace to specific element.
     *
     * @param element the element to set
     * @param namespace the namespace to set
     * @param schemaLocation the XML schema file location URI
     */
    public static void setNamespace(Element element, String namespace,
                                    String schemaLocation) {
        element.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI,
                XMLConstants.XMLNS_ATTRIBUTE, namespace);
        element.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, XMLNS_XSI,
                XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);
        element.setAttributeNS(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI,
                XSI_SCHEMA_LOCATION, schemaLocation);
    }

    /**
     * Evaluates the XPath expression in the specified context and returns the found items as a List.
     *
     * @param node       the XML document to evaluate
     * @param expression the compiled XPath expression
     * @return the list of elements found
     */
    public static List<String> getListValue(Node node, XPathExpression expression) {
        try {
            NodeList nodeList = (NodeList) expression.evaluate(node, XPathConstants.NODESET);
            List<String> list = new ArrayList<String>(nodeList.getLength());
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node item = nodeList.item(i);
                list.add(item.getFirstChild().getNodeValue());
            }
            return list;
        } catch (XPathExpressionException e) {
            // Try to evaluate in string context:
            String value = getStringValue(node, expression);
            if (value != null) {
                List<String> list = new ArrayList<String>(1);
                list.add(value);
                return list;
            }
            return Collections.emptyList();
        }
    }

    private static XPath createXPath(NamespaceContext namespaceContext, XPathFunctionResolver functionResolver) {
        XPath xPath = XPathFactory.newInstance().newXPath();
        if (namespaceContext != null) {
            xPath.setNamespaceContext(namespaceContext);
        }
        if (functionResolver != null) {
            xPath.setXPathFunctionResolver(functionResolver);
        }
        return xPath;
    }
}
