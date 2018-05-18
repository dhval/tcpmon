package org.apache.dhval;

import org.apache.dhval.utils.XMLUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;

@RunWith(SpringRunner.class)
public class RegexXMLExtractor {

    private static final Logger LOG = LoggerFactory.getLogger(XMLUtilTest.class);

    String inputFile = "/Users/dhval/Downloads/PublishCountyInmateBulk.xml";
    String dstDirectory = "tmp/extract-cpe";
    String TAG = "cpe:CountyProbationEvent";

    @Test
    public void extractXpathNoNameSpace() throws Exception {
        File file = new File(inputFile);
        String content = new String (Files.readAllBytes(file.toPath()), Charset.forName("UTF-8"));
        XMLUtil.extractXMLFromText(content, TAG, dstDirectory);
    }
}