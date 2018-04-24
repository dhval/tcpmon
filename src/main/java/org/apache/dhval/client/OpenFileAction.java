package org.apache.dhval.client;

import org.apache.dhval.utils.Utils;
import org.apache.tcpmon.TCPMon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;

class OpenFileAction extends AbstractAction {
    private static final Logger LOG = LoggerFactory.getLogger(OpenFileAction.class);
    private JFileChooser fc = new JFileChooser();
    private Sender component;

    public OpenFileAction(String action, Sender component) {
        super(action);
        fc.setCurrentDirectory(new File(TCPMon.CWD));
        this.component = component;
    }

    public void actionPerformed(ActionEvent e) {
        LOG.info(e.paramString() + e.getActionCommand());
        switch (fc.showOpenDialog(component)) {
            case JFileChooser.APPROVE_OPTION:
                JOptionPane.showMessageDialog(component, "Selected: " + fc.getSelectedFile(), "FCDemo", JOptionPane.OK_OPTION);
                try {
                    LOG.info("Read File: " + fc.getSelectedFile().getAbsolutePath());
                    component.readFile(fc.getSelectedFile().getAbsolutePath());
                } catch (Exception ex) {
                    LOG.info(ex.getMessage(), ex);
                }
                break;
            case JFileChooser.CANCEL_OPTION:
                JOptionPane.showMessageDialog(component, "Cancelled", "FCDemo", JOptionPane.OK_OPTION);
                break;
            case JFileChooser.ERROR_OPTION:
                JOptionPane.showMessageDialog(component, "Error", "FCDemo", JOptionPane.OK_OPTION);
        }
    }
}
