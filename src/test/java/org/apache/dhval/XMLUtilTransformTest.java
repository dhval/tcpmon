package org.apache.dhval;

import org.apache.dhval.utils.XMLUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import org.w3c.dom.Document;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RunWith(SpringRunner.class)
public class XMLUtilTransformTest {

    public static final String NIEM_PERSON_SUR_NAME = "//*[local-name()='PersonSurName' and namespace-uri()='http://release.niem.gov/niem/niem-core/3.0/']";
    public static final String NIEM_PERSON_GIVEN_NAME = "//*[local-name()='PersonGivenName' and namespace-uri()='http://release.niem.gov/niem/niem-core/3.0/']";
    public static final String NIEM_PERSON_MIDDLE_NAME = "//*[local-name()='PersonMiddleName' and namespace-uri()='http://release.niem.gov/niem/niem-core/3.0/']";

    public static final String JNET_METADATA_TRACKING_ID = "//*[local-name()='UserDefinedTrackingID' and namespace-uri()='http://www.jnet.state.pa.us/niem/jnet/metadata/1']";
    public static final String JNET_METADATA_ATTN_NAME = "//*[local-name()='RequestAttentionName' and namespace-uri()='http://www.jnet.state.pa.us/niem/jnet/metadata/1']";
    public static final String JNET_METADATA_AUTH_USER_ID = "//*[local-name()='RequestAuthenticatedUserID' and namespace-uri()='http://www.jnet.state.pa.us/niem/jnet/metadata/1']";

    @Test
    public void transform() throws Exception {
        Path resourceDirectory = Paths.get("soap");
        File file1 = new File(resourceDirectory.toFile(), "offender-inquiry-03.xml");
        File file2 = new File(resourceDirectory.toFile(), "offender-inquiry-04.xml");
        Document srcDocument = XMLUtil.createDOMNode(file1.getAbsolutePath());
        Map<String, String> xpaths = Stream.of(
                new AbstractMap.SimpleEntry<>(NIEM_PERSON_SUR_NAME, "Mudawal"),
                new AbstractMap.SimpleEntry<>(NIEM_PERSON_GIVEN_NAME, "Sx")
        ).collect(Collectors.toMap(e -> e.getKey(), e-> e.getValue()));
        Document result = XMLUtil.transform(srcDocument, xpaths);
        XMLUtil.saveDOMNode(result, file2.getAbsolutePath());
    }
}
