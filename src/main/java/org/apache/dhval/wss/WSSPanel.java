package org.apache.dhval.wss;

import org.apache.dhval.action.JFieldDocumentListener;
import org.apache.dhval.utils.JUtils;
import org.apache.tcpmon.TCPMon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map;

@Component
public class WSSPanel extends JPanel {
   private static final Logger LOG = LoggerFactory.getLogger(WSSPanel.class);

   private List<Map<String, Object>> keystores = null;

   public JComboBox<String> ksLocations;
   public JTextField ksLocationField;

   public JComboBox<String> ksAliases;
   public JTextField ksAliasField;

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
            String[] value = keystores.stream().filter(m -> m.get("location").equals(ksLocations.getItemAt(0))).map(m -> (List<String>) m.get("aliases")).flatMap(l -> l.stream()).toArray(String[]::new);
            if (value == null || value.length ==0)
                ksAliases= new JComboBox<> (new String[] {"client"});
            else
                ksAliases= new JComboBox<> (value);
        } else {
            ksLocations = new JComboBox<String> (new String[] {"keystore.jks"});
            ksAliases= new JComboBox<String> (new String[] {"client"});
        }

        jPanel.add(new JLabel("KeyStore: "), JUtils.createGridElement());
        jPanel.add(ksLocations, JUtils.createGridElement());
        jPanel.add(ksLocationField = JUtils.jTextField(ksLocations.getItemAt(0), 25, 50), JUtils.createGridEndElement());

        jPanel.add(new JLabel("StoreAlias: "), JUtils.createGridElement());
        jPanel.add(ksAliases, JUtils.createGridElement());
        jPanel.add(ksAliasField = JUtils.jTextField(ksAliases.getItemAt(0), 25, 50), JUtils.createGridEndElement());

        this.add(jPanel);
    }

    @PostConstruct
    public void init() {
        ksLocations.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String ksLocation = (String) ksLocations.getSelectedItem();
                ksLocationField.setText(ksLocation);
                keystores.stream().filter(m -> m.get("location").equals(ksLocation)).forEach(m ->  {
                    List<String> list = (List<String>) m.get("aliases");
                    String ksAlias = list.get(0);
                    ksAliases.removeAllItems();
                    list.forEach(item -> ksAliases.addItem(item));
                    ksAliasField.setText(ksAlias);
                });
            }
        });
        ksAliases.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String item = (String) ksAliases.getSelectedItem();
                ksAliasField.setText(item);
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
}
