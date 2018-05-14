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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RunWith(SpringRunner.class)
public class RegexXMLExtractor {

    private static final Logger LOG = LoggerFactory.getLogger(XMLUtilTest.class);

    Path resourceDirectory = Paths.get("src", "test", "resources");
    Path tmpDir = Paths.get("soap");

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