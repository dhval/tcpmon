package org.apache.dhval;

import org.apache.dhval.utils.Utils;
import org.apache.dhval.utils.XMLUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.junit4.SpringRunner;
import org.w3c.dom.Node;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RunWith(SpringRunner.class)
public class XPathFileRenameTest {
    private static final Logger LOG = LoggerFactory.getLogger(XPathFileRenameTest.class);

    @Test
    public void extractXpath() throws Exception {
        String srcDirectory = "/Users/dhval/projects/tcpmon/tmp/extract-cie";
        List<String> files = Utils.allFilesByType(srcDirectory, "xml");
        files.stream().forEach( file -> {
            // list of xpath expressions to look
            Map<String, String> xpaths = Stream.of(
                    new AbstractMap.SimpleEntry<>("PersonGivenName", "//erx:SupervisionPerson/nc:PersonName/nc:PersonGivenName"),
                    new AbstractMap.SimpleEntry<>("PersonMiddleName", "//erx:SupervisionPerson/nc:PersonName/nc:PersonMiddleName"),
                    new AbstractMap.SimpleEntry<>("PersonSurName", "//erx:SupervisionPerson/nc:PersonName/nc:PersonSurName"),
                    new AbstractMap.SimpleEntry<>("PersonBirthDate", "//erx:SupervisionPerson/nc:PersonBirthDate/nc:Date")
            ).collect(Collectors.toMap(e -> e.getKey(), e-> e.getValue()));
            try {
                Node root = XMLUtil.createDOMNode(file).getFirstChild();
                Map<String, String> result = XMLUtil.extractXpath(root, xpaths);
                String outFile = result.get("PersonGivenName") + "_" + result.get("PersonMiddleName")
                        + "_" + result.get("PersonSurName") + "_" + result.get("PersonBirthDate");
                Path path = Paths.get(file);
                File dstFile = new File(path.getParent() + "/" + outFile + ".xml");
                // if file already exists then add #suffix
                int dupCount = 1;
                while(Utils.isFilePresent(dstFile.getAbsolutePath())) {
                    dstFile = new File(path.getParent() + "/" + outFile + "-" + (dupCount++) + ".xml");
                }
                Files.move(new File(file).toPath(), dstFile.toPath());
            } catch (Exception e) {
                LOG.info(e.getMessage());
            }
        });
    }

}
