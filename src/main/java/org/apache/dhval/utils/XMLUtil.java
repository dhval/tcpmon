package org.apache.dhval.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utility class for working with XML documents and XPath expressions.
 */
public class XMLUtil {

    private static final Logger LOG = LoggerFactory.getLogger(XMLUtil.class);

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
