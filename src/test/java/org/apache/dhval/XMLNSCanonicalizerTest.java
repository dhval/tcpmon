package org.apache.dhval;

import org.apache.dhval.utils.NamespaceCache;
import org.apache.dhval.utils.XMLUtil;
import org.junit.Ignore;
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

    @Test
    @Ignore
    public void cannonicalNS() throws Exception {
        File file = new File(tmpDir.toFile(), "file2.xml");
        Document document = XMLUtil.createDOMNode(file.getAbsolutePath());
        document = XMLUtil.canonicalNS(document);

        File file2 = new File(tmpDir.toFile(), "file2.xml");
        XMLUtil.saveDOMNode(document, file2.getAbsolutePath());
    }

    /**
     * Get name-space prefixes from first XML file and apply to second XML, generating third XML as output.
     * @throws Exception
     */
    @Test
    public void cannonicalNSWithCache() throws Exception {
        File fileNS = new File(tmpDir.toFile(), "file1.xml");
        NamespaceCache cache = new NamespaceCache(XMLUtil.createDOMNode(fileNS.getAbsolutePath()), false);

        File file = new File(tmpDir.toFile(), "file2.xml");
        Document document = XMLUtil.createDOMNode(file.getAbsolutePath());
        document = XMLUtil.canonicalNS(document, cache);

        File file2 = new File(tmpDir.toFile(), "file3.xml");
        XMLUtil.saveDOMNode(document, file2.getAbsolutePath());
    }
}
