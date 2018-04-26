package org.apache.dhval.wss;

import org.apache.dhval.server.MockPanel;
import org.apache.dhval.storage.LocalDB;
import org.apache.dhval.utils.JUtils;
import org.apache.tcpmon.TCPMon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import rx.Subscriber;
import rx.schedulers.Schedulers;
import rx.Observable;

import javax.annotation.PostConstruct;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.util.ArrayList;
import java.util.Map;
import java.util.List;
import java.util.Optional;

@Component
public class WSSPanel extends JPanel {
   private static final Logger LOG = LoggerFactory.getLogger(MockPanel.class);

   private List<Map<String, Object>> keystores = null;
   public JComboBox<String> ksLocations;
   public JTextField ksLocationField;

    public JComboBox<String> ksAliases;
    public JTextField ksAliasField;

    public WSSPanel(@Autowired JTabbedPane notebook, @Autowired LocalDB localDB) {
        super(new GridBagLayout());
        this.setLayout(new BorderLayout());

        JPanel jPanel = new JPanel(new GridBagLayout());
        jPanel.setBorder(new TitledBorder("Mock Server"));


        Map jsonMap = TCPMon.jsonMap;
        if (jsonMap != null || jsonMap.containsKey("KeyStores")) {
            keystores = (List<Map<String, Object>>) jsonMap.get("KeyStores");
        }

        if (keystores != null) {
            ksLocations = new JComboBox<String> (keystores.stream().map(m -> m.get("location")).toArray(String[]::new));
            Optional<String> value = keystores.stream().filter(m -> m.get("location").equals(ksLocations.getItemAt(0))).map(m -> (List<String>) m.get("aliases")).flatMap(l -> l.stream()).findAny();
            String ksAlias = value.isPresent() ? value.get() : "client";
            ksAliases= new JComboBox<> (new String[] {ksAlias});
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
                String item = (String) ksLocations.getSelectedItem();
                ksLocationField.setText(item);
                keystores.stream().filter(m -> m.get("location").equals(item)).forEach(m ->  {
                    List<String> list = (List<String>) m.get("aliases");
                    ksAliases= new JComboBox<String> (list.toArray(new String[0]));
                });
            }
        });
        ksLocationField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                doSomething();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                doSomething();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                doSomething();
            }

            public void doSomething() {
                LOG.info(ksAliasField.getText());
            }
        });
        ksAliases.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String item = (String) ksAliases.getSelectedItem();
                ksAliasField.setText(item);
            }
        });

        Observable<String> todoObservable = Observable.create(new Observable.OnSubscribe<T>() {
            @Override
            public void subscribe(ObservableEmitter<Todo> emitter) throws Exception {
                try {
                    List<String> todos = RxJavaUnitTest.this.getTodos();
                    for (String todo : todos) {
                        emitter.onNext(todo);
                    }
                    emitter.onComplete();
                } catch (Exception e) {
                    emitter.onError(e);
                }
            }
        });

        private static rx.Observable<String> createSubscriber() {
            return rx.Observable.create((Subscriber<? super String> s) -> {
                while (!s.isUnsubscribed()) {

                    WatchKey key;
                    try {
                        while ((key = watchService.take()) != null) {
                            for (WatchEvent<?> event : key.pollEvents()) {
                                LOG.info("Detected File System Event: " + event.kind() + " ( " + event.context() + " )");
                                WatchEvent<Path> pathEvent = (WatchEvent) event;
                                Path file = pathEvent.context();
                                s.onNext(file.toString());
                            }
                            key.reset();
                        }
                    } catch (Exception e) {
                        LOG.info(e.getMessage(), e);
                    }
                }
            }).subscribeOn(Schedulers.io());
        };\
    }
}
