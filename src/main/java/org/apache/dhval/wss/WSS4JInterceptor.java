package org.apache.dhval.wss;

import org.apache.tcpmon.TCPMon;
import org.apache.wss4j.dom.WSConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
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

    private static final Logger LOG = LoggerFactory.getLogger(WSS4JInterceptor.class);

    public static ClientInterceptor userNameTokenInterceptor(Map<String, String> map) throws Exception {
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

    public static ClientInterceptor signedX509(Map<String, String> map) throws Exception {

        Wss4jSecurityInterceptor securityInterceptor = new Wss4jSecurityInterceptor();

        CryptoFactoryBean cryptoFactory = new CryptoFactoryBean();
        cryptoFactory.setKeyStoreLocation(new FileSystemResource(map.get("keystore-location")));
        cryptoFactory.setKeyStorePassword(map.get("keystore-password"));
        cryptoFactory.afterPropertiesSet();
        Crypto crypto = cryptoFactory.getObject();
        // set security actions: Timestamp Signature SAMLTokenSigned SAMLTokenUnsigned
        securityInterceptor.setSecurementActions(map.get("action"));
        // sign the request
        securityInterceptor.setSecurementUsername(map.get("keystore-alias"));
        securityInterceptor.setSecurementPassword(map.get("keystore-password"));
        securityInterceptor.setSecurementSignatureCrypto(crypto);
        securityInterceptor.setSecurementSignatureParts(
                "{Element}{http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd}Timestamp;" +
                        "{Element}{http://schemas.xmlsoap.org/soap/envelope/}Body"
        );
        // X509KeyIdentifier, DirectReference
        securityInterceptor.setSecurementSignatureKeyIdentifier("DirectReference");
        // initialize
        securityInterceptor.afterPropertiesSet();
        return securityInterceptor;
    }

    /**
     * @return
     * @throws Exception
     */
    public static ClientInterceptor signedSAMLAssertion(Map<String, String> map) throws Exception {

        Wss4jSecurityInterceptor securityInterceptor = new Wss4jSecurityInterceptor();
        FileSystemResource resource = new FileSystemResource(map.get("keystore-location"));
        LOG.info(resource.toString() + resource.exists());
        CryptoFactoryBean cryptoFactory = new CryptoFactoryBean();
        cryptoFactory.setKeyStoreLocation(resource);
        cryptoFactory.setKeyStorePassword(map.get("keystore-password"));
        cryptoFactory.afterPropertiesSet();
        Crypto crypto = cryptoFactory.getObject();

        //SAML2CallbackHandler samlCallbackHandler = new SAML2CallbackHandler(crypto, map.get("keystore-alias"));

        /**
        SAMLIssuerImpl issuer = new SAMLIssuerImpl();
        issuer.setIssuerCrypto(crypto);
        issuer.setIssuerKeyName(map.get("keystore-alias"));
        issuer.setIssuerKeyPassword(map.get("password"));
        issuer.setIssuerName(map.get("user"));
        issuer.setSendKeyValue(false);
        issuer.setSignAssertion(true);
        issuer.setCallbackHandler(samlCallbackHandler);
**/
        securityInterceptor.setSecurementActions(map.get("action"));
        securityInterceptor.setSecurementSignatureCrypto(crypto);
        securityInterceptor.setSecurementUsername(map.get("keystore-alias"));
        securityInterceptor.setSecurementPassword(map.get("keystore-password"));
     //   securityInterceptor.setSamlIssuer(issuer);
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
