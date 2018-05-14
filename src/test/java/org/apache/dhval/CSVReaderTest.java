package org.apache.dhval;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StringUtils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

@RunWith(SpringRunner.class)
public class CSVReaderTest {
    private static final Logger LOG = LoggerFactory.getLogger(CSVReaderTest.class);

    String fileName = "/Users/dhval/Desktop/OffenderMapping.csv";
    String outfileName = "tmp/OffenderMapping.csv";

    @Test
    public void extractXpathNoNameSpace() throws Exception {

        try (Stream<String> stream = Files.lines(Paths.get(fileName));
             BufferedWriter writer = Files.newBufferedWriter(Paths.get(outfileName))) {
            stream.forEach(line -> {
                String[] data = line.split(",");
                if (data.length >= 3 && !StringUtils.isEmpty(data[0])
                        && !StringUtils.isEmpty(data[1]) && !StringUtils.isEmpty(data[2])) {
                    String out = data[0] + ", " + data[1] + ", " + data[2] + "\n";
                    try {
                        writer.write(out);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
