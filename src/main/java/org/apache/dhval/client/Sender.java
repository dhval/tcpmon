package org.apache.dhval.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.apache.dhval.action.FormatXMLAction;
import org.apache.dhval.action.JFieldDocumentListener;
import org.apache.dhval.action.SaveFileAction;
import org.apache.dhval.action.SelectTextAction;
import org.apache.dhval.storage.LocalDB;
import org.apache.dhval.utils.JUtils;
import org.apache.dhval.utils.Utils;
import org.apache.dhval.wss.WSSClient;
import org.apache.tcpmon.TCPMon;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.AbstractMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Allows one to send an arbitrary soap message to a specific url with a specified soap action
 */
@org.springframework.stereotype.Component
public class Sender extends JPanel {
    public static final String SWITCH_LAYOUT = "Switch Layout";
    private static final Logger LOG = LoggerFactory.getLogger(Sender.class);
    public JTextField endpointField = null;
    public JTextField actionField = JUtils.jTextField("", 4, 4);
    public JCheckBox xmlFormatBox = null;
    public JCheckBox retryBox = null;
    public JButton sendButton = null;
    public JButton switchButton = null;
    public JSplitPane outPane = null;
    public JPanel leftPanel = null;
    public JPanel rightPanel = null;
    public JTabbedPane notebook = null;
    //   public JTextField hostNameField = null;
    public JFileChooser fc = new JFileChooser();
    RSyntaxTextArea inputText = new RSyntaxTextArea(20, 60);
    JPopupMenu popupIn = inputText.getPopupMenu();
    JMenu submenu = new JMenu("Files");
    RSyntaxTextArea outputText = new RSyntaxTextArea(20, 60);
    JPopupMenu popupOut = outputText.getPopupMenu();

    JLabel requestFileLabel = new JLabel("");
    JLabel statusLabel = new JLabel("Ready");
    JComboBox<String> selectEnvironment = null;
    JComboBox<String> selectHost = null;
    JComboBox<String> selectWSS4J = null;
    private boolean enableScheduler = false;
    private JMenuItem saveMenuItem = new JMenuItem("Save");
    private SwingWorker clientWorker;
    private Map<String, String> environmentMap;
    private Map<String, String> hostMap;

    private LocalDB db;
    private AbstractAction saveInputFileListener = new AbstractAction("Save") {
        @Override
        public void actionPerformed(ActionEvent e) {
            String fileName = requestFileLabel.getText();
            try {
                if (Utils.isFilePresent(fileName)) {
                    try (FileWriter fw = new FileWriter(fileName)) {
                        fw.write(inputText.getText());
                    }
                    LOG.info("Save: " + fileName);
                } else {
                    JOptionPane.showMessageDialog(Sender.this, "Cannot write to disk.", "Use Save as:",
                            JOptionPane.OK_OPTION);
                }
            } catch (Exception ex) {
                LOG.warn(ex.getMessage(), ex);
            }
        }
    };
    private ActionListener itemListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            LOG.info(e.paramString());
            readFile(e.getActionCommand());
            LOG.info(e.getSource().toString());
        }
    };

    public Sender(@Autowired JTabbedPane _notebook, @Autowired LocalDB db) {
        notebook = _notebook;
        this.db = db;
        fc.setCurrentDirectory(new File(TCPMon.CWD));

        inputText.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_XML);
        inputText.setCodeFoldingEnabled(true);
        popupIn.addSeparator();
        popupIn.add(new JMenuItem(new SelectTextAction()));
        popupIn.addSeparator();
        popupIn.add(new JMenuItem(new OpenFileAction("Open", this)));
        popupIn.add(new JMenuItem(saveInputFileListener));
        popupIn.add(new JMenuItem(new SaveFileAction("Save As", this, inputText)));
        popupIn.add(new JMenuItem(new FormatXMLAction(this, inputText)));
        popupIn.add(submenu);

        popupOut.addSeparator();
        popupOut.add(new JMenuItem(new SelectTextAction()));
        popupOut.addSeparator();
        popupOut.add(new JMenuItem(new FormatXMLAction(this, outputText)));
        popupOut.add(new JMenuItem(new SaveFileAction("Save", this, outputText)));
        outputText.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_XML);
        outputText.setCodeFoldingEnabled(true);


        environmentMap = new LinkedHashMap<>();
        hostMap = new LinkedHashMap<>();
        final Map<String, String> requestWSS4J = new LinkedHashMap<>();
        try {
            environmentMap.put("Select", "http://localhost:8080/echo/services/23");
            hostMap.put("None", "");
            requestWSS4J.put("None", "");
            Map readValue = new ObjectMapper().readValue(new File("config.json"), Map.class);
            environmentMap.putAll((Map<String, String>) readValue.get("environments"));
            hostMap.putAll((Map<String, String>) readValue.get("hosts"));
            requestWSS4J.putAll((Map<String, String>) readValue.get("WSS4J"));
        } catch (IOException io) {
            LOG.warn("config.json file not found");
        }

        selectEnvironment = new JComboBox<>(environmentMap.keySet().toArray(new String[0]));
        selectHost = new JComboBox<>(hostMap.keySet().toArray(new String[0]));
        selectWSS4J = new JComboBox<>(requestWSS4J.keySet().toArray(new String[0]));

        String lastWSSProfile = db.getHistory(LocalDB.LAST_WSS_PROFILE);
        for (int i=1; i< selectWSS4J.getItemCount(); i++)
            if(selectWSS4J.getItemAt(i).equals(lastWSSProfile)) selectWSS4J.setSelectedIndex(i);

        this.setLayout(new BorderLayout());

        // 1st component is just a row of labels and 1-line entry fields
        // ///////////////////////////////////////////////////////////////////
        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.X_AXIS));
        top.add(selectHost);
        top.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        top.add(selectEnvironment);
        top.add(Box.createRigidArea(new Dimension(5, 0)));
        top.add(new JLabel("Endpoint", SwingConstants.RIGHT));
        top.add(Box.createRigidArea(new Dimension(5, 0)));
        top.add(endpointField = new JTextField(db.getHistory(LocalDB.LAST_WS_ENDPOINT, "http://localhost:8080/echo/services/23"), 50));
        top.add(Box.createRigidArea(new Dimension(5, 0)));
        top.add(new JLabel("WS-Security"));
        top.add(Box.createRigidArea(new Dimension(5, 0)));
        top.add(selectWSS4J);
        top.add(Box.createRigidArea(new Dimension(5, 0)));

        JPanel bottom = new JPanel();
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.X_AXIS));
        bottom.add(new JLabel("File:"));
        bottom.add(Box.createRigidArea(new Dimension(5, 0)));
        bottom.add(requestFileLabel);
        bottom.add(Box.createHorizontalGlue());
        bottom.add(statusLabel);

        endpointField.setMaximumSize(new Dimension(300, Short.MAX_VALUE));
        actionField.setMaximumSize(new Dimension(100, Short.MAX_VALUE));
        this.add(top, BorderLayout.NORTH);
        this.add(bottom, BorderLayout.SOUTH);

        // Add Request/Response Section
        // ///////////////////////////////////////////////////////////////////
        JPanel center = new JPanel();
        center.setLayout(new BorderLayout());
        leftPanel = new JPanel();
        leftPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));

        leftPanel.add(new RTextScrollPane(inputText));


        rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.add(new RTextScrollPane(outputText));
        outPane = new JSplitPane(0, leftPanel, rightPanel);
        outPane.setDividerSize(4);
        center.add(outPane, BorderLayout.CENTER);
        JPanel bottomButtons = new JPanel();
        bottomButtons.setLayout(new BoxLayout(bottomButtons, BoxLayout.LINE_AXIS));
        bottomButtons.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        bottomButtons.add(retryBox = new JCheckBox("Retry"));
        bottomButtons.add(Box.createRigidArea(new Dimension(5, 0)));
        bottomButtons.add(
                xmlFormatBox =
                        new JCheckBox(TCPMon.getMessage("xmlFormat00", "XML Format")));
        bottomButtons.add(Box.createRigidArea(new Dimension(5, 0)));
        bottomButtons.add(sendButton = new JButton("Send"));

        bottomButtons.add(Box.createRigidArea(new Dimension(5, 0)));
        bottomButtons.add(switchButton = new JButton(SWITCH_LAYOUT));
        bottomButtons.add(Box.createHorizontalGlue());
        final String close = TCPMon.getMessage("close00", "Close");
        center.add(bottomButtons, BorderLayout.SOUTH);

        this.add(center, BorderLayout.CENTER);
        outPane.setDividerLocation(250);
        notebook.addTab("Sender", this);
    }

    @PostConstruct
    void init() {
        // Load previously opened files
        db.getFileHistory().forEach(item -> addFileMenuItem(item));
        // Register event listeners
        sendButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if ("Send".equals(event.getActionCommand())) {
                    send();
                }
            }
        });
        switchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if (SWITCH_LAYOUT.equals(event.getActionCommand())) {
                    int v = outPane.getOrientation();
                    if (v == 0) {
                        // top/bottom
                        outPane.setOrientation(1);
                    } else {
                        // left/right
                        outPane.setOrientation(0);
                    }
                    outPane.setDividerLocation(0.5);
                }
            }
        });
        selectEnvironment.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String item = (String) selectEnvironment.getSelectedItem();
                String itemVal = environmentMap.get(item);
                endpointField.setText(itemVal);
            }
        });
        selectHost.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selHostKey = (String) selectHost.getSelectedItem();
                String selHost = hostMap.get(selHostKey);
                if (StringUtils.isEmpty(selHost)) return;
                String selectEndPoint = endpointField.getText();
                endpointField.setText(Utils.replaceHost(selectEndPoint, selHost));
            }
        });
        endpointField.getDocument().addDocumentListener(
                new JFieldDocumentListener(endpointField, LocalDB.LAST_WS_ENDPOINT, db)
        );
        selectWSS4J.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String item = (String) selectWSS4J.getSelectedItem();
                db.publish(LocalDB.LAST_WSS_PROFILE, item);
            }
        });
        xmlFormatBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (!StringUtils.isEmpty(outputText.getText()))
                        outputText.setText(Utils.prettyXML(outputText.getText()));
                    if (!StringUtils.isEmpty(inputText.getText()))
                        inputText.setText(Utils.prettyXML(inputText.getText()));
                } catch (Exception e1) {
                    LOG.warn(e1.getMessage(), e1);
                }
            }
        });
        retryBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                enableScheduler = e.getStateChange() == ItemEvent.SELECTED;
            }
        });
    }

    private void addFileMenuItem(String file) {
        JMenuItem menuItem;
        submenu.add(menuItem = new JMenuItem(file));
        menuItem.addActionListener(itemListener);
    }

    /**
     * Method setLeft
     *
     * @param left
     */
    public void setLeft(Component left) {
        leftPanel.removeAll();
        leftPanel.add(left);
    }

    /**
     * Method setRight
     *
     * @param right
     */
    public void setRight(Component right) {
        rightPanel.removeAll();
        rightPanel.add(right);
    }

    /**
     * Method close
     */
    public void close() {
        notebook.remove(this);
    }

    @Scheduled(initialDelay = 30000, fixedDelay = 20000L)
    public void scheduler() {
        if (!enableScheduler) return;
        send();
    }

    void readFile(String file) {
        try {
            if (Utils.isFilePresent(file)) {
                String text = FileUtils.readFileToString(new File(file));
                if (Utils.isXML(text)) {
                    inputText.setText(Utils.prettyXML(text));
                    inputText.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_XML);
                } else {
                    inputText.setText(text);
                }
                requestFileLabel.setText(file);
            }
        } catch (Exception e) {
            LOG.info(e.getMessage(), e);
        }
    }

    public void send() {
        if (clientWorker != null && clientWorker.getState().equals(SwingWorker.StateValue.PENDING)) {
            LOG.warn("Query already in progress.");
            return;
        }
        // update file history cache
        if (!db.getFileHistory().contains(requestFileLabel.getText())) {
            db.saveFileHistory(requestFileLabel.getText());
            addFileMenuItem(requestFileLabel.getText());
        }
        LOG.info("Hi" + db.getFileHistory().size());
        statusLabel.setText("Pending...");
        outputText.setText("");
        Instant start = Instant.now();
        clientWorker = new SwingWorker<String, Integer>() {
            @Override
            protected String doInBackground() {
                // Background work
                try {
                    String url = endpointField.getText();
                    String action = actionField.getText();
                    String data = inputText.getText();
                    String selWSS4JProfile = selectWSS4J.getSelectedItem().toString();

                    Map<String, String> headers = Stream.of(
                            new AbstractMap.SimpleEntry<>("SOAPAction", action),
                            new AbstractMap.SimpleEntry<>("Content-Type", "text/xml; charset=utf-8"),
                            new AbstractMap.SimpleEntry<>("User-Agent", "TCPMon/2.0")
                    ).collect(Collectors.toMap((e) -> e.getKey(), (e) -> e.getValue()));

                    return WSSClient.post(url, data, headers, selWSS4JProfile);
                } catch (Exception e) {
                    LOG.warn(e.getMessage(), e);
                    return Utils.printStackTrace(e);
                }
            }

            @Override
            protected void process(List<Integer> chunks) {
                // Process results
            }

            @Override
            protected void done() {
                try {
                    String result = get();
                    LOG.debug(result);
                    if (Utils.isXML(result)) {
                        outputText.setText(Utils.prettyXML(result));
                    } else {
                        outputText.setText(result);
                    }
                    db.saveRequestResponse(inputText.getText(), outputText.getText());
                } catch (Exception e) {
                    outputText.setText(Utils.printStackTrace(e));
                } finally {
                    statusLabel.setText("Ready: " + Duration.between(start, Instant.now()));
                }
            }
        };
        //worker.isDon
        clientWorker.execute();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {

            }
        });

    }

}
