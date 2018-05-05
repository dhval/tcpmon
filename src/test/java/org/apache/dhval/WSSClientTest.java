package org.apache.dhval;

import org.apache.dhval.wss.WSSClient;
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
import java.util.AbstractMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RunWith(SpringRunner.class)
public class WSSClientTest {
    private static final Logger LOG = LoggerFactory.getLogger(XMLUtilTest.class);
    Path resourceDirectory = Paths.get("soap");

    @Test
    public void wss4jX509() throws Exception {
        String url = "https://ws.jnet.beta.pa.gov/JNETInquiry/OffenderInquiry/1";
        File file = new File(resourceDirectory.toFile(), "offender-inquiry-03.xml");
        String srcXml = new String (Files.readAllBytes(file.toPath()), Charset.forName("UTF-8"));
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
        LOG.info(result);
    }
}
