package org.apache.dhval.wss;

import org.apache.tcpmon.TCPMon;
import org.apache.wss4j.dom.WSConstants;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.soap.security.wss4j.Wss4jSecurityInterceptor;
import org.springframework.ws.soap.security.wss4j.support.CryptoFactoryBean;
import org.apache.ws.security.components.crypto.Crypto;
import org.apache.ws.security.saml.SAMLIssuerImpl;

import java.util.Map;
import org.springframework.core.io.ClassPathResource;

/**
 * https://www.one-tab.com/page/S2c297WjQJmzJbFyE-_6RQ
 *
 */
public class WSS4JInterceptor {

    public static ClientInterceptor userNameTokenInterceptor() throws Exception {
        Map<String, String> map = getWSS4JProfile("UserNameToken");
        Wss4jSecurityInterceptor wss4jInterceptor = new Wss4jSecurityInterceptor();
        wss4jInterceptor.setSecurementActions(map.get("action"));
        wss4jInterceptor.setSecurementUsername(map.get("user"));
        wss4jInterceptor.setSecurementPassword(map.get("password"));
        if(map.get("encrypt").equals("yes")){
            wss4jInterceptor.setSecurementPasswordType(WSConstants.PW_DIGEST);
        }else{
            wss4jInterceptor.setSecurementPasswordType(WSConstants.PW_TEXT);
        }
        wss4jInterceptor.afterPropertiesSet();
        return wss4jInterceptor;
    }

    /**
     * @return
     * @throws Exception
     */
    public static ClientInterceptor signedSAMLAssertion() throws Exception {
        Map<String, String> map = getWSS4JProfile("SAMLTokenSigned");

        Wss4jSecurityInterceptor securityInterceptor = new Wss4jSecurityInterceptor();

        CryptoFactoryBean cryptoFactory = new CryptoFactoryBean();
        cryptoFactory.setKeyStoreLocation(new ClassPathResource("keystore.jks"));
        cryptoFactory.setKeyStorePassword("changeit");
        cryptoFactory.afterPropertiesSet();
        Crypto crypto = cryptoFactory.getObject();

        SAML2CallbackHandler samlCallbackHandler = new SAML2CallbackHandler(crypto, "selfsigned");

        SAMLIssuerImpl issuer = new SAMLIssuerImpl();
        issuer.setIssuerCrypto(crypto);
        issuer.setIssuerKeyName("selfsigned");
        issuer.setIssuerKeyPassword("password");
        issuer.setIssuerName("selfsigned");
        issuer.setSendKeyValue(false);
        issuer.setSignAssertion(true);
        issuer.setCallbackHandler(samlCallbackHandler);

        securityInterceptor.setSecurementActions("Timestamp SAMLTokenSigned");
        securityInterceptor.setSecurementSignatureCrypto(crypto);
        securityInterceptor.setSecurementUsername("selfsigned");
        securityInterceptor.setSecurementPassword("password");
        securityInterceptor.setSamlIssuer(issuer);
        securityInterceptor.afterPropertiesSet();

        securityInterceptor.afterPropertiesSet();
        return securityInterceptor;
    }

    private static Map<String, String> getWSS4JProfile(String name) {
        Map jsonMap = TCPMon.jsonMap;
        if (jsonMap == null || !jsonMap.containsKey("wss4j-profiles"))
            return null;
        Map<String, Object> map = (Map<String, Object>) jsonMap.get("wss4j-profiles");
        return (Map<String, String>) map.get(name);
    }
}
