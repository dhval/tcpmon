package org.apache.dhval.wss;

import org.apache.dhval.action.JFieldDocumentListener;
import org.apache.dhval.utils.JUtils;
import org.apache.tcpmon.TCPMon;
import org.apache.ws.security.components.crypto.Crypto;
import org.apache.ws.security.components.crypto.Merlin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.util.*;
import java.util.List;

@Component
public class WSSPanel extends JPanel {
   private static final Logger LOG = LoggerFactory.getLogger(WSSPanel.class);

    private String keyStoreLocation;
    private String keyStorePWD;
    private String keyStoreAlias;

   private List<Map<String, Object>> keystores = null;

   public JComboBox<String> ksLocations;
   public JTextField ksLocationField;

   public JComboBox<String> ksAliases;
   public JTextField ksAliasField;


    public String getKeyStoreLocation() {
        return keyStoreLocation;
    }

    public void setKeyStoreLocation(String keyStoreLocation) {
        this.keyStoreLocation = keyStoreLocation;
    }

    public String getKeyStorePWD() {
        return keyStorePWD;
    }

    public void setKeyStorePWD(String keyStorePWD) {
        this.keyStorePWD = keyStorePWD;
    }

    public String getKeyStoreAlias() {
        return keyStoreAlias;
    }

    public void setKeyStoreAlias(String keyStoreAlias) {
        this.keyStoreAlias = keyStoreAlias;
    }

    public WSSPanel(@Autowired JTabbedPane notebook) {
        super(new GridBagLayout());
        this.setLayout(new BorderLayout());

        JPanel jPanel = new JPanel(new GridBagLayout());
        jPanel.setBorder(new TitledBorder("Keystore Configuration"));


        Map jsonMap = TCPMon.jsonMap;
        if (jsonMap != null || jsonMap.containsKey("KeyStores")) {
            keystores = (List<Map<String, Object>>) jsonMap.get("KeyStores");
        }

        if (keystores != null) {
            ksLocations = new JComboBox<String> (keystores.stream().map(m -> m.get("location")).toArray(String[]::new));
            ksAliases= new JComboBox<> (getKeysStoreAlias(ksLocations.getItemAt(0)));
        } else {
            ksLocations = new JComboBox<String> (new String[] {"keystore.jks"});
            ksAliases= new JComboBox<String> (new String[] {"client"});
        }

        jPanel.add(new JLabel("KeyStore: "), JUtils.createGridElement());
        jPanel.add(ksLocations, JUtils.createGridElement());
        jPanel.add(ksLocationField = JUtils.jTextField(ksLocations.getItemAt(0), 25, 50), JUtils.createGridEndElement());
        ksLocationField.setEnabled(false);

        jPanel.add(new JLabel("StoreAlias: "), JUtils.createGridElement());
        jPanel.add(ksAliases, JUtils.createGridElement());
        jPanel.add(ksAliasField = JUtils.jTextField(ksAliases.getItemAt(0), 25, 50), JUtils.createGridEndElement());
        ksAliasField.setEnabled(false);


        this.add(jPanel);
    }

    @PostConstruct
    public void init() {
        ksLocations.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ksAliases.removeAllItems();
                String ksLocation = (String) ksLocations.getSelectedItem();
                ksLocationField.setText(ksLocation);
                String[] aliases = getKeysStoreAlias(ksLocation);
                if (aliases.length <= 0) {
                    return;
                }
                Arrays.stream(aliases).forEach(item -> ksAliases.addItem(item));
                ksAliasField.setText(aliases[0]);
                keyStoreAlias = aliases[0];
            }
        });
        ksAliases.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String item = (String) ksAliases.getSelectedItem();
                ksAliasField.setText(item);
                keyStoreAlias = item;
            }
        });
        /**
        ksAliasField.getDocument().addDocumentListener(
                new JFieldDocumentListener(ksAliasField, LocalDB.KEY_STORE_ALIAS, localDB)
        );
        ksLocationField.getDocument().addDocumentListener(
                new JFieldDocumentListener(ksLocationField, LocalDB.KEY_STORE_LOCATION, localDB)
        );
        **/
    }

    private String[] getKeysStoreAlias(String ksLocation) {
        List<String> aliases = new ArrayList<>();
        String pwd = keystores.stream().filter(m -> m.get("location").equals(ksLocation)).map(m -> (String) m.get("keystore-password")).findFirst().get();
        keyStoreLocation = ksLocation;
        keyStorePWD = pwd;
        try {
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(new FileSystemResource(ksLocation).getInputStream(), pwd.toCharArray());
            Crypto crypto = new Merlin();
            ((Merlin) crypto).setKeyStore(keyStore);
            Enumeration enumeration = keyStore.aliases();
            while (enumeration.hasMoreElements()) {
                String alias = (String) enumeration.nextElement();
                aliases.add(alias);
            }
        } catch (Exception e) {
            LOG.warn(e.getMessage(), e);
        }
        return aliases.toArray(new String[0]);
    }
}
