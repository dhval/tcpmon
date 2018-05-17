package org.apache.dhval.action;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class JFieldDocumentListener implements DocumentListener {
    private static final Logger LOG = LoggerFactory.getLogger(JFieldDocumentListener.class);
    public JTextField textField;
    public String fieldKey;

    public JFieldDocumentListener(JTextField textField, String fieldKey) {
        this.textField = textField;
        this.fieldKey = fieldKey;
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        publish();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        publish();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        publish();
    }

    public void publish() {
        LOG.info(textField.getText());
    }
}
