package com.dhval.logger;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.nio.charset.StandardCharsets;

@Component
public class LogPanel extends JPanel {
    private TextAreaOutputStream textArea = null;
    private JScrollPane scrollPane;
    public JTextPane jTextPane = new JTextPane();

    public LogPanel(@Autowired JTabbedPane notebook) {
        this.setLayout(new BorderLayout());
        JTextArea jTextArea = new JTextArea();
        textArea = new TextAreaOutputStream(jTextArea, 50);
        notebook.addTab("Log Viewer", this);

        JPanel top = new JPanel(new GridLayout());
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.add(scrollPane = new JScrollPane(jTextPane,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
        //scrollPane.setMinimumSize(new Dimension(800, 600));
        //scrollPane.setPreferredSize(new Dimension(1000, 600));
        this.add(top);
    }

    public void append(String string) {
        textArea.write(string.getBytes(StandardCharsets.UTF_8));
    }
}
