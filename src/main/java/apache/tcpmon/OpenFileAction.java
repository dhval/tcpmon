package apache.tcpmon;

import com.dhval.utils.Utils;
import org.apache.commons.io.FileUtils;
import org.apache.tcpmon.TCPMon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;

public class OpenFileAction extends AbstractAction {
    private static final Logger LOG = LoggerFactory.getLogger(OpenFileAction.class);
    private  JFileChooser fc = new JFileChooser();
    private JPanel component;
    private JLabel label;
    private JTextArea textArea;

    public OpenFileAction(String action, JPanel component, JLabel label, JTextArea textArea) {
        super(action);
        fc.setCurrentDirectory(new File(TCPMon.CWD));
        this.component = component;
        this.label = label;
        this.textArea = textArea;
    }

    public OpenFileAction(String action, JPanel component, JLabel label) {
        this(action, component, label, null);
    }

    public void actionPerformed(ActionEvent e) {
        LOG.info(e.paramString() + e.getActionCommand());
        switch (fc.showOpenDialog(component))
        {
            case JFileChooser.APPROVE_OPTION:
                JOptionPane.showMessageDialog(component, "Selected: "+
                                fc.getSelectedFile(),
                        "FCDemo",
                        JOptionPane.OK_OPTION);
                try {
                    LOG.info("Read File: " + fc.getSelectedFile().getAbsolutePath());
                    if (textArea != null) {
                        String text = FileUtils.readFileToString(fc.getSelectedFile());
                        if (!StringUtils.isEmpty(text)) textArea.setText(Utils.prettyXML(text));
                    }
                    label.setText(fc.getSelectedFile().getCanonicalPath());
                } catch (Exception ex) {

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
