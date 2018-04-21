package com.dhval.echo;

import apache.tcpmon.OpenFileAction;
import com.dhval.utils.JUtils;
import com.dhval.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.List;

@Component
public class EchoPanel extends JPanel {
    private static final Logger LOG = LoggerFactory.getLogger(EchoPanel.class);
    public static final String NOT_STARTED = "Not started !";
    private final EchoHandler httpHandler = new EchoHandler();

    public JButton runButton = null;
    public JButton clearButton = null;

    private JLabel fileLabel = new JLabel("sample.xml");
    private JLabel statusLabel = new JLabel(NOT_STARTED);

    JTextField portField;
    JTextField contextField;

    LocalTestServer server;

    public EchoPanel(@Autowired JTabbedPane notebook) {
        notebook.addTab("Mock Server", this);
        this.setLayout(new BorderLayout());

        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.X_AXIS));
        top.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Query Type
        top.add(new JLabel("Port: "));
        top.add(Box.createRigidArea(new Dimension(5, 0)));
        top.add(portField = JUtils.jTextField("7832", 5, 10));
        // Tracking Id
        top.add(new JLabel("Path: "));
        top.add(Box.createRigidArea(new Dimension(5, 0)));
        top.add(contextField = JUtils.jTextField("/echo/*", 25, 150));
        top.add(Box.createHorizontalGlue());
        top.add(new JButton(new OpenFileAction("Select File", this, fileLabel)));
        top.add(Box.createRigidArea(new Dimension(5, 0)));
        OpenFileAction fileAction = new OpenFileAction("Select Dir", this, fileLabel);
        fileAction.setSelectionModeDir();
        top.add(Box.createRigidArea(new Dimension(5, 0)));
        top.add(new JButton(fileAction));
        top.add(runButton = new JButton("Run"));
        top.add(Box.createHorizontalGlue());
        top.add(clearButton = new JButton("Stop"));

        JPanel bottom = new JPanel();
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.X_AXIS));
        bottom.add(fileLabel);
        bottom.add(Box.createRigidArea(new Dimension(5, 0)));
        bottom.add(Box.createHorizontalGlue());
        bottom.add(statusLabel);

        JSplitPane pane1 = new JSplitPane(0);
        pane1.setDividerSize(4);
        //pane1.setTopComponent(tablePane);
        //pane1.setBottomComponent(pane2);
        pane1.setDividerLocation(150);

        this.add(top, BorderLayout.NORTH);
        this.add(pane1, BorderLayout.CENTER);
        this.add(bottom, BorderLayout.SOUTH);

    }

    @PostConstruct
    public void init() {
        runButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (server != null) server.stop();
                    int lPort = Integer.parseInt(portField.getText());
                    String httpPath = contextField.getText();
                    String echoFile = fileLabel.getText();
                    if (StringUtils.isEmpty(echoFile)) {
                        statusLabel.setText("File Not Found:" + echoFile);
                        return;
                    } else if (Utils.isFilePresent(echoFile)) {
                        httpHandler.setFile(echoFile);
                    } else if (Utils.isDirPresent(echoFile)) {
                        httpHandler.addFiles(Utils.allFilesByType(echoFile, "xml"));
                    } else {
                        statusLabel.setText("Error:" + echoFile);
                        return;
                    }
                    server = new LocalTestServer(lPort);
                    server.register(httpPath, httpHandler);
                    server.start();
                    statusLabel.setText("Listening on localhost:" + lPort);
                    LOG.info("Listening on localhost:" + lPort);
                } catch (Exception ex) {
                    LOG.warn(ex.getMessage(), ex);
                }
            }
        });
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (server == null) return;
                try {
                    server.stop();
                    statusLabel.setText(NOT_STARTED);
                    LOG.info("Stopping Server");
                } catch (Exception ex) {
                    LOG.warn(ex.getMessage(), ex);
                }
            }
        });
    }
}
