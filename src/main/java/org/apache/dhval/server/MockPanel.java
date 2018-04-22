package org.apache.dhval.server;

import org.apache.dhval.action.OpenFileAction;
import org.apache.dhval.dto.LocalServer;
import org.apache.dhval.utils.JUtils;
import org.apache.dhval.utils.Utils;
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

@Component
public class MockPanel extends JPanel {
    private static final Logger LOG = LoggerFactory.getLogger(MockPanel.class);
    public static final String NOT_STARTED = "Not started !";
    private final EchoHandler httpHandler = new EchoHandler();

    public JButton runButton = new JButton("Start");
    public JButton clearButton = new JButton("Stop");

    private JLabel fileLabel;
    private JLabel statusLabel = new JLabel(NOT_STARTED);

    MockServer server;

    public JTextField httpPath;
    public JComboBox<String> httpPaths = new JComboBox<String> (new String[] {"/echo/*", "/api/*"});

    JTextField portField;
    public JComboBox<String> portFields = new JComboBox<String> (new String[] {"7832", "8080"});

    public MockPanel(@Autowired JTabbedPane notebook, @Autowired LocalServer cfg) {
        super(new GridBagLayout());
        notebook.addTab("Mock Server", this);
        this.setLayout(new BorderLayout());
        fileLabel = new JLabel(cfg.getFileToServer());
        portField = JUtils.jTextField(Integer.toString(cfg.getListenPort()), 25, 50);
        httpPath = JUtils.jTextField(cfg.getPathURI(), 25, 50);

        JPanel jPanel = new JPanel(new GridBagLayout());
        jPanel.setBorder(new TitledBorder("Mock Server"));

        jPanel.add(new JLabel("Status: "), JUtils.createGridElement());
        jPanel.add(statusLabel, JUtils.createGridElement());
        jPanel.add(fileLabel, JUtils.createGridEndElement());

        jPanel.add(new JLabel("Path: "), JUtils.createGridElement());
        jPanel.add(httpPaths, JUtils.createGridElement());
        jPanel.add(httpPath, JUtils.createGridEndElement());

        jPanel.add(new JLabel("Port: "), JUtils.createGridElement());
        jPanel.add(portFields, JUtils.createGridElement());
        jPanel.add(portField, JUtils.createGridEndElement());

        jPanel.add(new JLabel("File: "), JUtils.createGridElement());
        jPanel.add(new JButton(new OpenFileAction("Select File", this, fileLabel)), JUtils.createGridElement());
        OpenFileAction fileAction = new OpenFileAction("Select Dir", this, fileLabel);
        fileAction.setSelectionModeDir();
        jPanel.add(new JButton(fileAction), JUtils.createGridEndElement());

        jPanel.add(new JLabel("Server: "), JUtils.createGridElement());
        jPanel.add(runButton, JUtils.createGridElement());
        jPanel.add(clearButton, JUtils.createGridEndElement());

        this.add(jPanel);

        if (cfg.getEnabled())
            start();
    }

    @PostConstruct
    public void init() {
        MockPanel sender = this;
        runButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
               start();
            }
        });
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
               stop();
            }
        });
        httpPaths.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String item = (String) httpPaths.getSelectedItem();
                httpPath.setText(item);
            }
        });
        portFields.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String item = (String) portFields.getSelectedItem();
                portField.setText(item);
            }
        });
    }

    private void start() {
        try {
            if (server != null)
                server.stop();
            int lPort = Integer.parseInt(portField.getText());
            String httpPathTxt = httpPath.getText();
            String echoFile = fileLabel.getText();
            if (Utils.isFilePresent(echoFile)) {
                httpHandler.addFile(echoFile);
            } else if (Utils.isDirPresent(echoFile)) {
                httpHandler.addFiles(Utils.allFilesByType(echoFile, "xml"));
            }
            server = new MockServer(lPort);
            server.register(httpPathTxt, httpHandler);
            server.start();
            statusLabel.setText("Listening - " + lPort);
            LOG.info("Listening on localhost:" + lPort);
        } catch (Exception ex) {
            LOG.warn(ex.getMessage(), ex);
        }
    }

    private void stop() {
        if (server == null) return;
        LOG.info("Stopping Server");
        try {
            server.stop();
            statusLabel.setText(NOT_STARTED);
        } catch (Exception ex) {
            LOG.warn(ex.getMessage(), ex);
        }
    }
}
