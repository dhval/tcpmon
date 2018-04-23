package org.apache.dhval.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.apache.dhval.action.FormatXMLAction;
import org.apache.dhval.action.SaveFileAction;
import org.apache.dhval.action.SelectTextAction;
import org.apache.dhval.utils.JUtils;
import org.apache.dhval.utils.Utils;
import org.apache.tcpmon.TCPMon;
import org.apache.dhval.wss.WSSClient;
import org.apache.dhval.wss.WSS4JInterceptor;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Allows one to send an arbitrary soap message to a specific url with a specified soap action
 */
@org.springframework.stereotype.Component
public class Sender extends JPanel {
    private static final Logger LOG = LoggerFactory.getLogger(Sender.class);
    private boolean enableScheduler = false;
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

    RSyntaxTextArea inputText = new RSyntaxTextArea(20, 60);
    JPopupMenu popupIn = inputText.getPopupMenu();
    JMenu submenu = new JMenu("Files");
    RSyntaxTextArea outputText = new RSyntaxTextArea(20, 60);
    JPopupMenu popupOut = outputText.getPopupMenu();

    JLabel requestFileLabel = new JLabel("");
    private Sender instance = null;
    public JTextField hostNameField = null;
    public JFileChooser fc = new JFileChooser();

    JComboBox<String> selectEnvironment = null;
    JComboBox<String> selectHost = null;
    JComboBox<String> selectWSS4J = null;

    public Sender(@Autowired JTabbedPane _notebook) {
        notebook = _notebook;
        instance = this;
        fc.setCurrentDirectory(new File(TCPMon.CWD));

        inputText.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_XML);
        inputText.setCodeFoldingEnabled(true);
        popupIn.addSeparator();
        popupIn.add(new JMenuItem(new SelectTextAction()));
        popupIn.addSeparator();
        popupIn.add(new JMenuItem(new OpenFileAction("Open", this)));
        popupIn.add(new JMenuItem(new FormatXMLAction(this, inputText)));
        popupIn.add(submenu);

        popupOut.addSeparator();
        popupOut.add(new JMenuItem(new SelectTextAction()));
        popupOut.addSeparator();
        popupIn.add(new JMenuItem(new FormatXMLAction(this, outputText)));
        popupOut.add(new JMenuItem(new SaveFileAction("Save", this, outputText)));
        outputText.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_XML);
        outputText.setCodeFoldingEnabled(true);


        final Map<String, String> environmentMap = new LinkedHashMap<>();
        final Map<String, String> hostMap = new LinkedHashMap<>();
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

        this.setLayout(new BorderLayout());

        // 1st component is just a row of labels and 1-line entry fields
        // ///////////////////////////////////////////////////////////////////
        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.X_AXIS));
        top.add(selectHost);
        top.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        top.add(hostNameField = JUtils.jTextField("", 10, 15));
        top.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        top.add(selectEnvironment);
        top.add(Box.createRigidArea(new Dimension(5, 0)));
        top.add(new JLabel("Connection Endpoint", SwingConstants.RIGHT));
        top.add(Box.createRigidArea(new Dimension(5, 0)));
        top.add(endpointField = new JTextField("http://localhost:8080/echo/services/23", 50));
        top.add(new JLabel("Profile"));
        top.add(Box.createRigidArea(new Dimension(5, 0)));
        top.add(selectWSS4J);
        top.add(Box.createRigidArea(new Dimension(5, 0)));

        JPanel bottom = new JPanel();
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.X_AXIS));
        bottom.add(new JLabel("File:"));
        bottom.add(Box.createRigidArea(new Dimension(5, 0)));
        bottom.add(requestFileLabel);
        bottom.add(Box.createHorizontalGlue());
        bottom.add(new JLabel("Ready:"));

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
        final String switchStr = TCPMon.getMessage("switch00", "Switch Layout");
        bottomButtons.add(switchButton = new JButton(switchStr));
        bottomButtons.add(Box.createHorizontalGlue());
        final String close = TCPMon.getMessage("close00", "Close");
        center.add(bottomButtons, BorderLayout.SOUTH);
        sendButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if ("Send".equals(event.getActionCommand())) {
                    send();
                }
            }
        });
        switchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if (switchStr.equals(event.getActionCommand())) {
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
                hostNameField.setText(selHost);
                if (StringUtils.isEmpty(selHost)) return;
                String selectEndPoint = endpointField.getText();
                endpointField.setText(selectEndPoint.replaceAll("\\d{1,3}.\\d{1,3}.\\d{1,3}.\\d{1,3}", hostNameField.getText()));
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

        this.add(center, BorderLayout.CENTER);
        outPane.setDividerLocation(250);
        notebook.addTab("Sender", this);

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

    @Scheduled(initialDelay = 30000, fixedDelay = 15000L)
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
        try {
            String selWSS4JProfile = selectWSS4J.getSelectedItem().toString();
            Map jsonMap = TCPMon.jsonMap;
            if (jsonMap != null && jsonMap.containsKey("wss4j-profiles")) {
                Map<String, Object> map = (Map<String, Object>) jsonMap.get("wss4j-profiles");
                if (map != null && map.containsKey(selWSS4JProfile)) {
                    String result = new WSSClient().post(endpointField.getText(), inputText.getText(), (Map<String, String>) map.get(selWSS4JProfile));
                    if (Utils.isXML(result)) {
                        outputText.setText(Utils.prettyXML(result));
                    } else {
                        outputText.setText(result);
                    }
                    return;
                }
            }
            // Use vanilla HTTP Post
            URL u = new URL(endpointField.getText());
            URLConnection uc = u.openConnection();
            HttpURLConnection connection = (HttpURLConnection) uc;
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            String action = "\"" + (actionField.getText() == null ? "" : actionField.getText()) + "\"";
            connection.setRequestProperty("SOAPAction", action);
            connection.setRequestProperty("Content-Type", "text/xml");
            connection.setRequestProperty("User-Agent", "TCPMon/2.0");
            OutputStream out = connection.getOutputStream();
            Writer writer = new OutputStreamWriter(out);
            writer.write(inputText.getText());
            writer.flush();
            writer.close();
            String line;
            InputStream inputStream = null;
            try {
                inputStream = connection.getInputStream();
            } catch (IOException e) {
                inputStream = connection.getErrorStream();
            }
            outputText.setText("");
            BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream));
            while ((line = rd.readLine()) != null) {
                outputText.append(line);
            }
            if (xmlFormatBox.isSelected()) {
                outputText.setText(Utils.prettyXML(outputText.getText()));
            }
        } catch (Exception e) {
            LOG.warn(e.getMessage(), e);
            outputText.setText(Utils.printStackTrace(e));
        }
    }

}
