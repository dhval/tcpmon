package org.apache.dhval;

import org.apache.dhval.utils.NamespaceCache;
import org.apache.dhval.utils.XMLUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.junit4.SpringRunner;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RunWith(SpringRunner.class)
public class XMLUtilTest {
    private static final Logger LOG = LoggerFactory.getLogger(XMLUtilTest.class);

    @Test
    public void extractXpathNoNameSpace() throws Exception {
        Path resourceDirectory = Paths.get("src","test","resources");
        File file = new File(resourceDirectory.toFile(), "items.xml");
        Node root = XMLUtil.createDOMNode(file.getAbsolutePath());

        XPathFactory xpathfactory = XPathFactory.newInstance();
        XPath xpath = xpathfactory.newXPath();

        List<String> list = XMLUtil.getListValue(root, xpath.compile("//item/title"));

        list.stream().forEach(content -> LOG.info(content));
    }

    @Test
    public void getNameSpaces() throws Exception {
        Path resourceDirectory = Paths.get("soap");
        File file = new File(resourceDirectory.toFile(), "offender-inquiry-03.xml");
        Document document = XMLUtil.createDOMNode(file.getAbsolutePath());

        NamespaceCache namespaceCache = new NamespaceCache(document, false);
        Map<String, String> prefix2Uri = namespaceCache.getPrefix2Uri();
        LOG.info("The list of the cached namespaces:");
        for (String key : prefix2Uri.keySet()) {
            LOG.info("prefix " + key + ": uri " + prefix2Uri.get(key));
        }
    }

    @Test
    public void extractXpath() throws Exception {
        Path resourceDirectory = Paths.get("soap");
        File file = new File(resourceDirectory.toFile(), "Notify-PA_Arrest-1afa-bc21-a85e-e9ef-162e-7a1408c-data.xml");
        Document document = XMLUtil.createDOMNode(file.getAbsolutePath());

        NamespaceCache namespaceCache = new NamespaceCache(document, false);

        XPathFactory xpathfactory = XPathFactory.newInstance();
        XPath xpath = xpathfactory.newXPath();
        xpath.setNamespaceContext(namespaceCache);

        List<String> list = XMLUtil.getListValue(document, xpath.compile("//nc:IdentificationID"));

        list.stream().forEach(content -> LOG.info(content));
    }


}
