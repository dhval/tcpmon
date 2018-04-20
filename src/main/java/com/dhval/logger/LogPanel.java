package com.dhval.logger;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.nio.charset.StandardCharsets;

public class LogPanel extends JPanel {
    private static final Logger LOG = LoggerFactory.getLogger(LogPanel.class);
    private static LogPanel instance = null;
    public JTabbedPane notebook = null;
    private TextAreaOutputStream textArea = null;

    public LogPanel(JTabbedPane _notebook) {
        notebook = _notebook;
        JTextArea jTextArea = new JTextArea();
        textArea = new TextAreaOutputStream(jTextArea, 200);
        notebook.addTab("Log Viewer", this);
        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.add(jTextArea);
        this.add(top, BorderLayout.CENTER);
    }

    public void append(String string) {
        textArea.write(string.getBytes(StandardCharsets.UTF_8));
    }
}
