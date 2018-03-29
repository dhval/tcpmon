package org.apache.ws.commons.tcpmon;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
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



    JComboBox<String> selectEnvironment = null;
    JComboBox<String> selectApplication = null;
    JComboBox<String> selectRequest = null;

    public Sender(JTabbedPane _notebook) {
        notebook = _notebook;

        final Map<String, String> environmentMap = new HashMap<>();
        final Map<String, String> applicationMap = new HashMap<>();
        final Map<String, String> requestMap = new HashMap<>();
        try {
            Map readValue = new ObjectMapper().readValue(new File("config.json"), Map.class);
            environmentMap.putAll((Map<String, String>) readValue.get("environments"));
            applicationMap.putAll((Map<String, String>) readValue.get("applications"));
            requestMap.putAll((Map<String, String>) readValue.get("requests"));
        } catch (IOException io) {
            LOG.warn("config.json file not found");
        }

        selectEnvironment = new JComboBox<>(environmentMap.keySet().toArray(new String[0]));
        selectApplication = new JComboBox<>(applicationMap.keySet().toArray(new String[0]));
        selectRequest = new JComboBox<>(applicationMap.keySet().toArray(new String[0]));

        this.setLayout(new BorderLayout());

        // 1st component is just a row of labels and 1-line entry fields
        // ///////////////////////////////////////////////////////////////////
        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.X_AXIS));
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

        JFileChooser fc = new JFileChooser();

        endpointField.setMaximumSize(new Dimension(300, Short.MAX_VALUE));
        actionField.setMaximumSize(new Dimension(100, Short.MAX_VALUE));
        this.add(top, BorderLayout.NORTH);
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
        bottomButtons.add(fileButton = new JButton("Open"));

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

                    String item = (String) selectEnvironment.getSelectedItem();//get the selected item

                    endpointField.setText(environmentMap.get(item));

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
        this.add(pane2, BorderLayout.CENTER);
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
