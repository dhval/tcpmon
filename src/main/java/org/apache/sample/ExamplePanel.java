package org.apache.sample;

import org.apache.dhval.action.OpenFileAction;
import org.apache.dhval.utils.JUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

@Component
public class ExamplePanel extends JPanel {
    private static final Logger LOG = LoggerFactory.getLogger(ExamplePanel.class);

    public JButton startBtn = null;
    public JButton stopBtn = null;

    private JLabel fileLabel = new JLabel("sample.xml");
    private JLabel statusLabel = new JLabel("Start");

    JTextField portField;
    JTextField contextField;

    public ExamplePanel(@Autowired JTabbedPane notebook) {
        notebook.addTab("Hello World", this);
        this.setLayout(new BorderLayout());

        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.X_AXIS));
        top.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Port
        top.add(new JLabel("Port: "));
        top.add(Box.createRigidArea(new Dimension(5, 0)));
        top.add(portField = JUtils.jTextField("7832", 5, 10));
        // Path
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
        top.add(startBtn = new JButton("Run"));
        top.add(Box.createHorizontalGlue());
        top.add(stopBtn = new JButton("Stop"));

        JPanel bottom = new JPanel();
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.X_AXIS));
        bottom.add(fileLabel);
        bottom.add(Box.createRigidArea(new Dimension(5, 0)));
        bottom.add(Box.createHorizontalGlue());
        bottom.add(statusLabel);

        JSplitPane pane1 = new JSplitPane(0);
        pane1.setDividerSize(4);
        pane1.setDividerLocation(150);

        this.add(top, BorderLayout.NORTH);
        this.add(pane1, BorderLayout.CENTER);
        this.add(bottom, BorderLayout.SOUTH);
    }

    @PostConstruct
    public void init() {
        startBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                statusLabel.setText("Started ...");
            }
        });
        stopBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                statusLabel.setText("Stopped ...");
            }
        });
    }
}
