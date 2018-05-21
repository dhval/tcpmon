package org.apache.dhval;

import org.apache.ws.security.components.crypto.Crypto;
import org.apache.ws.security.components.crypto.Merlin;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.test.context.junit4.SpringRunner;

import java.security.KeyStore;
import java.security.cert.Certificate;
import java.util.Enumeration;

@RunWith(SpringRunner.class)
public class KeyToolTest {
    private static final Logger LOG = LoggerFactory.getLogger(KeyToolTest.class);

    @Test
    public void extractXpathNoNameSpace() throws Exception {
        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(new FileSystemResource("keystore.jks").getInputStream(),
                "changeit".toCharArray());
        Crypto crypto = new Merlin();
        ((Merlin) crypto).setKeyStore(keyStore);

        Enumeration enumeration = keyStore.aliases();
        while(enumeration.hasMoreElements()) {
            String alias = (String) enumeration.nextElement();
            LOG.info("alias name: " + alias);
            Certificate certificate = keyStore.getCertificate(alias);
            LOG.info(certificate.getType());

        }

    }
}
