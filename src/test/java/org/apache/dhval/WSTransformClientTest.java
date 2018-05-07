package org.apache.dhval;

import junitparams.FileParameters;
import junitparams.JUnitParamsRunner;
import org.apache.dhval.utils.Utils;
import org.apache.dhval.utils.XMLUtil;
import org.apache.dhval.wss.WSSClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.junit4.SpringRunner;
import org.w3c.dom.Document;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RunWith(JUnitParamsRunner.class)
public class WSTransformClientTest {
    private static final Logger LOG = LoggerFactory.getLogger(WSTransformClientTest.class);

    public static final String NIEM_PERSON_SUR_NAME = "//*[local-name()='PersonSurName' and namespace-uri()='http://release.niem.gov/niem/niem-core/3.0/']";
    public static final String NIEM_PERSON_GIVEN_NAME = "//*[local-name()='PersonGivenName' and namespace-uri()='http://release.niem.gov/niem/niem-core/3.0/']";

    Path resourceDirectory = Paths.get("soap");
    Path tmpDirectory = Paths.get("tmp");

    @Test
    @FileParameters("src/test/resources/param.csv")
    public void wss4jX509(String last, String first) throws Exception {
        String url = "https://ws.jnet.beta.pa.gov/JNETInquiry/OffenderInquiry/1";
        File file = new File(resourceDirectory.toFile(), "offender-inquiry-05.xml");
        Map<String, String> xpaths = Stream.of(
                new AbstractMap.SimpleEntry<>(NIEM_PERSON_GIVEN_NAME, first),
                new AbstractMap.SimpleEntry<>(NIEM_PERSON_SUR_NAME, last)
        ).collect(Collectors.toMap(e -> e.getKey(), e-> e.getValue()));

        Document document = XMLUtil.transform(XMLUtil.createDOMNode(file.getAbsolutePath()), xpaths);
        String srcXml = XMLUtil.toString(document);

        Map<String, String> headers = Stream.of(
                new AbstractMap.SimpleEntry<>("SOAPAction", ""),
                new AbstractMap.SimpleEntry<>("Content-Type", "text/xml; charset=utf-8"),
                new AbstractMap.SimpleEntry<>("User-Agent", "TCPMon/2.0")
        ).collect(Collectors.toMap((e) -> e.getKey(), (e) -> e.getValue()));
        Map<String, String> wss4jProfile = Stream.of(
                new AbstractMap.SimpleEntry<>("name", "DigitalSignature"),
                new AbstractMap.SimpleEntry<>("action", "Timestamp Signature"),
                new AbstractMap.SimpleEntry<>("keystore-alias", "client"),
                new AbstractMap.SimpleEntry<>("keystore-password", "changeit"),
                new AbstractMap.SimpleEntry<>("keystore-location", "keystore.jks")).collect(Collectors.toMap(e -> e.getKey(), e-> e.getValue()));
        String result = WSSClient.wssPost(url, srcXml, headers, wss4jProfile);
        File file2 = new File(tmpDirectory.toFile(), "offender-inquiry-" + first + "-" + last);
        Utils.overWriteToDisk(file2.getAbsolutePath(), result);
    }
}
