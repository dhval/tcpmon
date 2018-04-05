package org.apache.ws.commons.tcpmon;

import apache.tcpmon.JUtils;
import apache.tcpmon.Utils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import javax.swing.*;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

/**
 * Allows one to send an arbitrary soap message to a specific url with a specified soap action
 */
class Sender extends JPanel {
    private static final Logger LOG = LoggerFactory.getLogger(Sender.class);
    public JTextField endpointField = null;
    public JTextField actionField = null;
    public JCheckBox xmlFormatBox = null;
    public JButton sendButton = null;
    public JButton switchButton = null;
    public JButton fileButton = null;
    public JSplitPane outPane = null;
    public JPanel leftPanel = null;
    public JPanel rightPanel = null;
    public JTabbedPane notebook = null;
    private JTextArea inputText = null;
    private JTextArea outputText = null;

    private Sender instance = null;
    public JTextField hostNameField = null;
    public  JFileChooser fc = new JFileChooser();
    public JButton saveRequestdBtn =  new JButton(BTN_SAVE_REQUEST);
    public JButton saveResponsedBtn = new JButton(BTN_SAVE_RESPONSE);

    JComboBox<String> selectEnvironment = null;
    JComboBox<String> selectHost = null;
    JComboBox<String> selectRequest = null;

    private static final String BTN_SAVE_REQUEST = "Save Request";
    private static final String BTN_SAVE_RESPONSE = "Save Response";

    public Sender(JTabbedPane _notebook) {
        notebook = _notebook;
        instance = this;
        fc.setCurrentDirectory(new File(TCPMon.CWD));

        final Map<String, String> environmentMap = new HashMap<>();
        final Map<String, String> hostMap = new HashMap<>();
        final Map<String, String> requestMap = new HashMap<>();
        try {
            Map readValue = new ObjectMapper().readValue(new File("config.json"), Map.class);
            environmentMap.putAll((Map<String, String>) readValue.get("environments"));
            hostMap.putAll((Map<String, String>) readValue.get("hosts"));
            requestMap.putAll((Map<String, String>) readValue.get("requests"));
        } catch (IOException io) {
            LOG.warn("config.json file not found");
        }

        selectEnvironment = new JComboBox<>(environmentMap.keySet().toArray(new String[0]));
        selectHost = new JComboBox<>(hostMap.keySet().toArray(new String[0]));
        selectRequest = new JComboBox<>(requestMap.keySet().toArray(new String[0]));

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
        top.add(endpointField = new JTextField("http://localhost:8080/axis2/services/XYZ", 50));
        top.add(Box.createRigidArea(new Dimension(5, 0)));
        top.add(new JLabel("SOAP Action  ", SwingConstants.RIGHT));
        top.add(actionField = new JTextField("", 4));
        top.add(Box.createRigidArea(new Dimension(5, 0)));

        JPanel top2 = new JPanel();
        top2.setLayout(new BoxLayout(top2, BoxLayout.LINE_AXIS));
        top2.add(new JLabel("hjgj"));
        top2.add(new JLabel("hjgj"));

        endpointField.setMaximumSize(new Dimension(300, Short.MAX_VALUE));
        actionField.setMaximumSize(new Dimension(100, Short.MAX_VALUE));
        this.add(top, BorderLayout.NORTH);
        top.add(top2, BorderLayout.SOUTH);
        inputText = new JTextArea(null, null, 20, 80);
        JScrollPane inputScroll = new JScrollPane(inputText);
        outputText = new JTextArea(null, null, 20, 80);
        JScrollPane outputScroll = new JScrollPane(outputText);

        // Add Request/Response Section
        // ///////////////////////////////////////////////////////////////////
        JPanel pane2 = new JPanel();
        pane2.setLayout(new BorderLayout());
        leftPanel = new JPanel();
        leftPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.add(inputScroll);
        rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.add(outputScroll);
        outPane = new JSplitPane(0, leftPanel, rightPanel);
        outPane.setDividerSize(4);
        pane2.add(outPane, BorderLayout.CENTER);
        JPanel bottomButtons = new JPanel();
        bottomButtons.setLayout(new BoxLayout(bottomButtons, BoxLayout.LINE_AXIS));
        bottomButtons.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        bottomButtons.add(
                xmlFormatBox =
                new JCheckBox(TCPMon.getMessage("xmlFormat00", "XML Format")));
        bottomButtons.add(Box.createRigidArea(new Dimension(5, 0)));
        bottomButtons.add(sendButton = new JButton("Send"));

        bottomButtons.add(Box.createRigidArea(new Dimension(5, 0)));
        bottomButtons.add(fileButton = new JButton("Open Request"));

        bottomButtons.add(Box.createRigidArea(new Dimension(5, 0)));
        bottomButtons.add(saveRequestdBtn);

        bottomButtons.add(Box.createRigidArea(new Dimension(5, 0)));
        bottomButtons.add(saveResponsedBtn);

        bottomButtons.add(Box.createRigidArea(new Dimension(5, 0)));
        final String switchStr = TCPMon.getMessage("switch00", "Switch Layout");
        bottomButtons.add(switchButton = new JButton(switchStr));
        bottomButtons.add(Box.createHorizontalGlue());
        final String close = TCPMon.getMessage("close00", "Close");
        pane2.add(bottomButtons, BorderLayout.SOUTH);
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
                String selHost = (String) selectHost.getSelectedItem();
                hostNameField.setText(hostMap.get(selHost));
                if (StringUtils.isEmpty(selHost)) return;
                String selectEndPoint = endpointField.getText();
                endpointField.setText(selectEndPoint.replaceAll("\\d{1,3}.\\d{1,3}.\\d{1,3}.\\d{1,3}", hostNameField.getText()));
            }
        });
        Sender sender = this;
        fileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                switch (fc.showOpenDialog(sender))
                {
                    case JFileChooser.APPROVE_OPTION:
                        JOptionPane.showMessageDialog(sender, "Selected: "+
                                        fc.getSelectedFile(),
                                "FCDemo",
                                JOptionPane.OK_OPTION);
                        try {
                            LOG.info("Read File: " + fc.getSelectedFile().getAbsolutePath());
                            String text = FileUtils.readFileToString(fc.getSelectedFile());
                            inputText.setText(prettyXML(text));
                        } catch (Exception ex) {

                        }
                        break;

                    case JFileChooser.CANCEL_OPTION:
                        JOptionPane.showMessageDialog(sender, "Cancelled",
                                "FCDemo",
                                JOptionPane.OK_OPTION);
                        break;

                    case JFileChooser.ERROR_OPTION:
                        JOptionPane.showMessageDialog(sender, "Error",
                                "FCDemo",
                                JOptionPane.OK_OPTION);
                }
            }
        });
        xmlFormatBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (!StringUtils.isEmpty(outputText.getText())) outputText.setText(prettyXML(outputText.getText()));
                    if (!StringUtils.isEmpty(inputText.getText())) inputText.setText(prettyXML(inputText.getText()));
                } catch (Exception e1) {
                    LOG.warn(e1.getMessage(), e1);
                }
            }
        });
        saveRequestdBtn.addActionListener(fileWritterAction);
        saveResponsedBtn.addActionListener(fileWritterAction);
        this.add(pane2, BorderLayout.CENTER);
        outPane.setDividerLocation(250);
        notebook.addTab("Sender", this);
    }

    public ActionListener fileWritterAction = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            LOG.info(e.getActionCommand());
            LOG.info(e.paramString());
            switch (fc.showOpenDialog(instance))
            {
                case JFileChooser.APPROVE_OPTION:
                    JOptionPane.showMessageDialog(instance, "Selected: "+
                                    fc.getSelectedFile(),
                            "FCDemo",
                            JOptionPane.OK_OPTION);
                    try {
                        JTextArea textArea = (e.getActionCommand().equals(BTN_SAVE_REQUEST)) ? inputText : outputText;
                        String filePath = fc.getSelectedFile().getAbsolutePath();
                        filePath += (filePath.endsWith(".xml")) ? "" : ".xml";
                        LOG.info("Write request to file: " + filePath);
                        String text = prettyXML(textArea.getText());
                        try(FileWriter fw = new FileWriter(filePath)) {
                            fw.write(text);
                        }
                    } catch (Exception ex) {
                        LOG.warn(ex.getMessage(), ex);
                    }
                    break;

                case JFileChooser.CANCEL_OPTION:
                    JOptionPane.showMessageDialog(instance, "Cancelled",
                            "FCDemo",
                            JOptionPane.OK_OPTION);
                    break;

                case JFileChooser.ERROR_OPTION:
                    JOptionPane.showMessageDialog(instance, "Error",
                            "FCDemo",
                            JOptionPane.OK_OPTION);
            }
        }
    };

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

    public void send() {
        try {
            URL u = new URL(endpointField.getText());
            URLConnection uc = u.openConnection();
            HttpURLConnection connection = (HttpURLConnection) uc;
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            String action = "\"" + (actionField.getText() == null ? "" : actionField.getText()) + "\"";
            connection.setRequestProperty("SOAPAction", action);
            connection.setRequestProperty("Content-Type", "text/xml");
            connection.setRequestProperty("User-Agent", "Axis/2.0");
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
            if(xmlFormatBox.isSelected()){
                outputText.setText(prettyXML(outputText.getText()));        
            }
        } catch (Exception e) {
            StringWriter w = new StringWriter();
            e.printStackTrace(new PrintWriter(w));
            outputText.setText(w.toString());
        }
    }
    
    public String prettyXML(String input) throws Exception {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        try {transformerFactory.setAttribute("indent-number", new Integer(2)); } catch (Exception e){}
        Transformer transformer = transformerFactory.newTransformer();
        try {transformer.setOutputProperty("{http://xml.apache.org/xalan}indent-amount", "2"); } catch (Exception e){}
        transformer.setOutputProperty(OutputKeys.INDENT , "yes");
        StringWriter writer = new StringWriter();
        transformer.transform(new StreamSource(new StringReader(input)), new StreamResult(writer));
        return writer.toString();
    }
}
