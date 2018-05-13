package org.apache.dhval;

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

    String inputFile = "SubmitCourtFiling_01.xml";

    String startTag = "<ID>";
    String endTag = "</ID>";

    @Test
    public void extractXpathNoNameSpace() throws Exception {
        File file = new File(tmpDir.toFile(), inputFile);

        String content = new String (Files.readAllBytes(file.toPath()), Charset.forName("UTF-8"));

        Pattern p = Pattern.compile(startTag + "[\\s\\S]*?" + endTag); // [\s\S]
        Matcher m = p.matcher(content);
        while (m.find()) {
            LOG.info(m.group(0));
        }
    }
}