package org.apache.dhval;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.dhval.utils.NamespaceCache;
import org.apache.dhval.utils.Utils;
import org.apache.dhval.utils.XMLUtil;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.junit4.SpringRunner;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RunWith(SpringRunner.class)
public class XPathBuilderTest {
    private static final Logger LOG = LoggerFactory.getLogger(XMLUtilTest.class);

    @Test
    @Ignore
    public void extractXpathNoNameSpace() throws Exception {
        Path tmpDir = Paths.get("tmp");
        String inputFile = "offender-inquiry-EUGENE-BRYANT";

        File file = new File(tmpDir.toFile(), inputFile);
        Node root = XMLUtil.createDOMNode(file.getAbsolutePath()).getFirstChild();

        Map<String, String> values = new LinkedHashMap<>();
        XMLUtil.buildXpathMap("/", root, values);

        values.entrySet().stream().forEach(entry -> LOG.info(entry.getKey() + " :: " + entry.getValue()));

        Map<String, Object> data = Stream.of(
                new AbstractMap.SimpleEntry<>("namespaces", new NamespaceCache(root.getOwnerDocument(), false).getPrefix2Uri()),
                new AbstractMap.SimpleEntry<>("data", values)
        ).collect(Collectors.toMap((e) -> e.getKey(), (e) -> e.getValue()));

        new ObjectMapper().writeValue(new File("tmp/" + inputFile + ".json"), data);
    }

    @Test
    @Ignore
    public void extractXpathFromDir() throws Exception {
        String srcDirectory = "/Users/dhval/projects/tcpmon/tmp/extract";
        List<String> files = Utils.allFilesByType(srcDirectory, "xml");

        Map<String, String> data = new HashMap<>();
        for(String file : files) {
            Map<String, String> values = new HashMap<>();
            Node root = XMLUtil.createDOMNode(file).getFirstChild();
            XMLUtil.buildXpathMap("/", root, values);
            data.putAll(values);
        }

        new ObjectMapper().writeValue(new File("tmp/out" + ".json"), data);

    }

    @Test
    public void evaluateBoolean() throws Exception {
       File file = new File("soap/offender-inquiry-er-doc.xml");
        //File file = new File("soap/offender-inquiry-01.xml");
        String content = new String (Files.readAllBytes(file.toPath()), Charset.forName("UTF-8"));

        Boolean bool = XMLUtil.evaluate(content, "//*[local-name()='Body']");

        LOG.info("v-" + bool);

        String result = XMLUtil.evaluateNode(content, "//*[local-name()='Body']/*[1]");

        LOG.info(result);

    }



    }

