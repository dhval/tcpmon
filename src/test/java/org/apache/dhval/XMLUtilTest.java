package org.apache.dhval;

import org.apache.dhval.utils.XMLUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.junit4.SpringRunner;
import org.w3c.dom.Node;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RunWith(SpringRunner.class)
public class XMLUtilTest {
    private static final Logger LOG = LoggerFactory.getLogger(XMLUtilTest.class);
    Path resourceDirectory = Paths.get("src","test","resources");

    @Test
    public void getFileHistory() throws Exception {
        File file = new File(resourceDirectory.toFile(), "items.xml");
        Node root = XMLUtil.createDOMNode(file.getAbsolutePath());

        XPathFactory xpathfactory = XPathFactory.newInstance();
        XPath xpath = xpathfactory.newXPath();

        List<String> list = XMLUtil.getListValue(root, xpath.compile("//item/title"));
        //String content = new String (Files.readAllBytes(file.toPath()), Charset.forName("UTF-8"));

        list.stream().forEach(content -> LOG.info(content));
    }
}
