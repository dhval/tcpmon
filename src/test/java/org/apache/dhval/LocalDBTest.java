package org.apache.dhval;

import org.apache.dhval.storage.LocalDB;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
//@SpringBootTest(classes = {TCPMon.class})
//@ComponentScan(basePackages = {"org.apache.dhval"})
@SpringBootTest(classes = {LocalDB.class})
@EnableConfigurationProperties
public class LocalDBTest {
    private static final Logger LOG = LoggerFactory.getLogger(LocalDBTest.class);

    @Autowired
    LocalDB localDB;

    @Test
    public void getFileHistory() throws Exception {
        localDB.getFileHistory().stream().forEach(s -> LOG.info(s));
        localDB.getAvailableRequestResponse().forEach(integer -> {
            LOG.info(localDB.getRequestHistory(integer));
        });
    }
}
