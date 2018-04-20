package apache.tcpmon;

import org.apache.tcpmon.TCPMon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileWriter;

public class SaveFileAction extends AbstractAction {
    private static final Logger LOG = LoggerFactory.getLogger(SaveFileAction.class);

    private JFileChooser fc = new JFileChooser();
    private JPanel component;
    private JTextArea textArea;

    public SaveFileAction(String action, JPanel component, JTextArea textArea) {
        super(action);
        fc.setCurrentDirectory(new File(TCPMon.CWD));
        this.component = component;
        this.textArea = textArea;
    }

    public void actionPerformed(ActionEvent e) {
        LOG.info(e.getActionCommand());
        LOG.info(e.paramString());
        switch (fc.showSaveDialog(component))
        {
            case JFileChooser.APPROVE_OPTION:
                JOptionPane.showMessageDialog(component, "Selected: "+
                                fc.getSelectedFile(),
                        "FCDemo",
                        JOptionPane.OK_OPTION);
                try {
                    String filePath = fc.getSelectedFile().getAbsolutePath();
                    filePath += (filePath.endsWith(".xml")) ? "" : ".xml";
                    String text = Utils.prettyXML(textArea.getText());
                    try(FileWriter fw = new FileWriter(filePath)) {
                        fw.write(text);
                    }
                } catch (Exception ex) {
                    LOG.warn(ex.getMessage(), ex);
                }
                break;

            case JFileChooser.CANCEL_OPTION:
                JOptionPane.showMessageDialog(component, "Cancelled",
                        "FCDemo",
                        JOptionPane.OK_OPTION);
                break;

            case JFileChooser.ERROR_OPTION:
                JOptionPane.showMessageDialog(component, "Error",
                        "FCDemo",
                        JOptionPane.OK_OPTION);
        }
    }
}
