package org.apache.dhval.wss;

import org.apache.ws.security.WSConstants;
import org.apache.ws.security.message.WSSecEncryptedKey;
import org.apache.ws.security.saml.ext.SAMLCallback;
import org.apache.ws.security.saml.ext.bean.ActionBean;
import org.apache.ws.security.saml.ext.bean.AttributeBean;
import org.apache.ws.security.saml.ext.bean.AttributeStatementBean;
import org.apache.ws.security.saml.ext.bean.AuthenticationStatementBean;
import org.apache.ws.security.saml.ext.bean.AuthDecisionStatementBean;
import org.apache.ws.security.saml.ext.bean.KeyInfoBean;
import org.apache.ws.security.saml.ext.bean.SubjectBean;
import org.apache.ws.security.saml.ext.bean.KeyInfoBean.CERT_IDENTIFIER;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.security.auth.callback.CallbackHandler;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import java.security.cert.X509Certificate;
import java.util.Collections;

/**
 * A base implementation of a Callback Handler for a SAML assertion. By default it creates an
 * authentication assertion.
 */
public abstract class AbstractSAMLCallbackHandler implements CallbackHandler {

    public enum Statement {
        AUTHN, ATTR, AUTHZ
    };

    protected String subjectName = null;
    protected String subjectQualifier = null;
    protected String confirmationMethod = null;
    protected X509Certificate[] certs;
    protected Statement statement = Statement.AUTHN;
    protected CERT_IDENTIFIER certIdentifier = CERT_IDENTIFIER.X509_CERT;
    protected byte[] ephemeralKey = null;
    protected String issuer = null;

    public void setConfirmationMethod(String confMethod) {
        confirmationMethod = confMethod;
    }

    public void setStatement(Statement statement) {
        this.statement = statement;
    }

    public void setCertIdentifier(CERT_IDENTIFIER certIdentifier) {
        this.certIdentifier = certIdentifier;
    }

    public void setCerts(X509Certificate[] certs) {
        this.certs = certs;
    }

    public byte[] getEphemeralKey() {
        return ephemeralKey;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    /**
     * Note that the SubjectBean parameter should be null for SAML2.0
     */
    protected void createAndSetStatement(SubjectBean subjectBean, SAMLCallback callback) {
        if (statement == Statement.AUTHN) {
            AuthenticationStatementBean authBean = new AuthenticationStatementBean();
            if (subjectBean != null) {
                authBean.setSubject(subjectBean);
            }
            authBean.setAuthenticationMethod("Password");
            callback.setAuthenticationStatementData(Collections.singletonList(authBean));
        } else if (statement == Statement.ATTR) {
            AttributeStatementBean attrBean = new AttributeStatementBean();
            if (subjectBean != null) {
                attrBean.setSubject(subjectBean);
            }
            AttributeBean attributeBean = new AttributeBean();
            attributeBean.setSimpleName("role");
            attributeBean.setAttributeValues(Collections.singletonList("user"));
            attrBean.setSamlAttributes(Collections.singletonList(attributeBean));
            callback.setAttributeStatementData(Collections.singletonList(attrBean));
        } else {
            AuthDecisionStatementBean authzBean = new AuthDecisionStatementBean();
            if (subjectBean != null) {
                authzBean.setSubject(subjectBean);
            }
            ActionBean actionBean = new ActionBean();
            actionBean.setContents("Read");
            authzBean.setActions(Collections.singletonList(actionBean));
            authzBean.setResource("endpoint");
            authzBean.setDecision(AuthDecisionStatementBean.Decision.PERMIT);
            callback.setAuthDecisionStatementData(Collections.singletonList(authzBean));
        }
    }

    protected KeyInfoBean createKeyInfo() throws Exception {
        KeyInfoBean keyInfo = new KeyInfoBean();
        if (statement == Statement.AUTHN) {
            keyInfo.setCertificate(certs[0]);
            keyInfo.setCertIdentifer(certIdentifier);
        } else if (statement == Statement.ATTR) {
            // Build a new Document
            DocumentBuilderFactory docBuilderFactory =
                    DocumentBuilderFactory.newInstance();
            docBuilderFactory.setNamespaceAware(true);
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();

            // Create an Encrypted Key
            WSSecEncryptedKey encrKey = new WSSecEncryptedKey();
            encrKey.setKeyIdentifierType(WSConstants.X509_KEY_IDENTIFIER);
            encrKey.setUseThisCert(certs[0]);
            encrKey.prepare(doc, null);
            ephemeralKey = encrKey.getEphemeralKey();
            Element encryptedKeyElement = encrKey.getEncryptedKeyElement();

            // Append the EncryptedKey to a KeyInfo element
            Element keyInfoElement =
                    doc.createElementNS(
                            WSConstants.SIG_NS, WSConstants.SIG_PREFIX + ":" + WSConstants.KEYINFO_LN
                    );
            keyInfoElement.setAttributeNS(
                    WSConstants.XMLNS_NS, "xmlns:" + WSConstants.SIG_PREFIX, WSConstants.SIG_NS
            );
            keyInfoElement.appendChild(encryptedKeyElement);

            keyInfo.setElement(keyInfoElement);
        }
        return keyInfo;
    }
}
