package org.apache.dhval;

import org.apache.dhval.utils.XMLUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import org.w3c.dom.Document;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

@RunWith(SpringRunner.class)
public class XMLNSCanonicalizerTest {
    Path soapResourceDir = Paths.get("soap");
    Path tmpDir = Paths.get("tmp");

    String testFile = "Correction-Inquiry-1.xml";

    @Test
    public void cannonicalNS() throws Exception {
        File file = new File(soapResourceDir.toFile(), testFile);
        Document document = XMLUtil.createDOMNode(file.getAbsolutePath());
        document = XMLUtil.canonicalNS(document);

        File file2 = new File(tmpDir.toFile(), testFile);
        XMLUtil.saveDOMNode(document, file2.getAbsolutePath());
    }
}
