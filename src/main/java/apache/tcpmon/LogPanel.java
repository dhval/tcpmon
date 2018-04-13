package apache.tcpmon;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.nio.charset.StandardCharsets;

public class LogPanel extends JPanel {
    private static final Logger LOG = LoggerFactory.getLogger(LogPanel.class);
    private static LogPanel instance = null;
    public JTabbedPane notebook = null;
    private TextAreaOutputStream textArea = null;

    public LogPanel(JTabbedPane _notebook) {
        notebook = _notebook;
        textArea = new TextAreaOutputStream(new JTextArea(), 100);

        notebook.addTab("Log Viewer", this);
    }

    public void append(String string) {
        SwingUtilities.invokeLater(new Runnable() {
                                       @Override
                                       public void run() {
                                           textArea.write(string.getBytes(StandardCharsets.UTF_8));
                                       }
                                   });

    }

}
