
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
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
public class CompareXMLFilesTest {
    private static final Logger LOG = LoggerFactory.getLogger(CompareXMLFilesTest.class);

    String srcDirectory = "/Users/dhval/projects/tcpmon/tmp/extract-cie";

    @Test
    public void findAndDeleteDuplicateFiles() throws Exception {

        List<String> files = Utils.allFilesByType(srcDirectory, "xml");
        for(int i = 0; i < files.size()-1; i++) {

            String f1 = files.get(i);
            if (!Utils.isFilePresent(f1)) continue;
            Node root1 = XMLUtil.createDOMNode(f1).getFirstChild();
            Map<String, String> values1 = new LinkedHashMap<>();
            XMLUtil.buildXpathMap("/", root1, values1);

            for(int j = i+1; j < files.size(); j++) {
               String f2 = files.get(j);
                if (f1.equals(f2) || !Utils.isFilePresent(f2)) continue;

                Node root2 = XMLUtil.createDOMNode(f2).getFirstChild();
                Map<String, String> values2 = new LinkedHashMap<>();
                XMLUtil.buildXpathMap("/", root2, values2);

                if (values1.equals(values2))  {
                    LOG.info("Delete - " + f2);
                    Files.delete(Paths.get(f2));
                }
            }
        }

    }

}