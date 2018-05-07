package org.apache.dhval;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.dhval.utils.NamespaceCache;
import org.apache.dhval.utils.XMLUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.junit4.SpringRunner;
import org.w3c.dom.Node;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RunWith(SpringRunner.class)
public class XPathBuilderTest {
    private static final Logger LOG = LoggerFactory.getLogger(XMLUtilTest.class);

    Path resourceDirectory = Paths.get("src","test","resources");
    Path soapResourceDir = Paths.get("soap");

    @Test
    public void extractXpathNoNameSpace() throws Exception {
        File file = new File(soapResourceDir.toFile(), "Notify-PA_Arrest-1afa-bc21-a85e-e9ef-162e-7a1408c-data.xml");
        Node root = XMLUtil.createDOMNode(file.getAbsolutePath()).getFirstChild();

        Map<String, String> values = new LinkedHashMap<>();
        XMLUtil.buildXpathMap("/", root, values);

        values.entrySet().stream().forEach(entry -> LOG.info(entry.getKey() + " :: " + entry.getValue()));

        Map<String, Object> data = Stream.of(
                new AbstractMap.SimpleEntry<>("namespaces", new NamespaceCache(root.getOwnerDocument(), false).getPrefix2Uri()),
                new AbstractMap.SimpleEntry<>("data", values)
        ).collect(Collectors.toMap((e) -> e.getKey(), (e) -> e.getValue()));

        new ObjectMapper().writeValue(new File("tmp/xpath.json"), data);
    }
}
