package org.apache.dhval.action;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.TextAction;
import java.awt.event.ActionEvent;
import java.io.File;

/**
 * An action that gets the filename at the current caret position and tries
 * to open that file. If there is a selection, it uses the selected text as
 * the filename.
 */
public class SelectTextAction extends TextAction {
    private static final Logger LOG = LoggerFactory.getLogger(SelectTextAction.class);
    public SelectTextAction() {
        super("SelectText");
    }

    public void actionPerformed(ActionEvent e) {

        JTextComponent tc = getTextComponent(e);
        String filename = null;

        // Get the name of the file to load. If there is a selection, use
        // that as the file name, otherwise, scan for a filename around
        // the caret.
        try {
            int selStart = tc.getSelectionStart();
            int selEnd = tc.getSelectionEnd();
            if (selStart != selEnd) {
                filename = tc.getText(selStart, selEnd - selStart);
            } else {
                filename = getFilenameAtCaret(tc);
            }
        } catch (BadLocationException ble) {
            ble.printStackTrace();
            UIManager.getLookAndFeel().provideErrorFeedback(tc);
            return;
        }

        //loadFile(new File(filename));
        LOG.info("G" + filename);

    }

    /**
     * Gets the filename that the caret is sitting on. Note that this is a
     * somewhat naive implementation and assumes filenames do not contain
     * whitespace or other "funny" characters, but it will catch most common
     * filenames.
     *
     * @param tc The text component to look at.
     * @return The filename at the caret position.
     * @throws BadLocationException Shouldn't actually happen.
     */
    public String getFilenameAtCaret(JTextComponent tc) throws BadLocationException {
        int caret = tc.getCaretPosition();
        int start = caret;
        Document doc = tc.getDocument();
        while (start > 0) {
            char ch = doc.getText(start - 1, 1).charAt(0);
            if (isFilenameChar(ch)) {
                start--;
            } else {
                break;
            }
        }
        int end = caret;
        while (end < doc.getLength()) {
            char ch = doc.getText(end, 1).charAt(0);
            if (isFilenameChar(ch)) {
                end++;
            } else {
                break;
            }
        }
        return doc.getText(start, end - start);
    }

    public boolean isFilenameChar(char ch) {
        return Character.isLetterOrDigit(ch) || ch == ':' || ch == '.'
                || ch == File.separatorChar;
    }

}