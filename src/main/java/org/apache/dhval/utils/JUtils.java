package org.apache.dhval.utils;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import java.awt.*;

public class JUtils {

    public static JTextField jTextField(String text, int col, int width) {
        JTextField textField = new JTextField(text, col);
        textField.setMaximumSize(new Dimension(width, Short.MAX_VALUE));
        return textField;
    }

    public static GridBagConstraints createGridEndElement() {
        GridBagConstraints DEF_GRID_BAG = new GridBagConstraints();
        DEF_GRID_BAG.anchor = GridBagConstraints.WEST;
        DEF_GRID_BAG.gridwidth = GridBagConstraints.REMAINDER;
        return DEF_GRID_BAG;
    }

    public static GridBagConstraints createGridElement() {
        GridBagConstraints DEF_GRID_1 = new GridBagConstraints();
        DEF_GRID_1.anchor = GridBagConstraints.WEST;
        DEF_GRID_1.gridwidth = 1;
        return  DEF_GRID_1;
    }

    /**
     * a text field with a restricted set of characters
     */
    public static class RestrictedTextField extends JTextField {
        /**
         * Field validText
         */
        protected String validText;

        /**
         * Constructor RestrictedTextField
         *
         * @param validText
         */
        public RestrictedTextField(String validText) {
            setValidText(validText);
        }

        /**
         * Constructor RestrictedTextField
         *
         * @param columns
         * @param validText
         */
        public RestrictedTextField(int columns, String validText) {
            super(columns);
            setValidText(validText);
        }

        /**
         * Constructor RestrictedTextField
         *
         * @param text
         * @param validText
         */
        public RestrictedTextField(String text, String validText) {
            super(text);
            setValidText(validText);
        }

        /**
         * Constructor RestrictedTextField
         *
         * @param text
         * @param columns
         * @param validText
         */
        public RestrictedTextField(String text, int columns, String validText) {
            super(text, columns);
            setValidText(validText);
        }

        /**
         * Method setValidText
         *
         * @param validText
         */
        private void setValidText(String validText) {
            this.validText = validText;
        }

        /**
         * fascinatingly, this method is called in the super() constructor,
         * meaning before we are fully initialized. C++ doesnt actually permit
         * such a situation, but java clearly does...
         *
         * @return a new document
         */
        public Document createDefaultModel() {
            return new RestrictedDocument();
        }

        /**
         * this class strips out invaid chars
         */
        class RestrictedDocument extends PlainDocument {
            /**
             * Constructs a plain text document.  A default model using
             * <code>GapContent</code> is constructed and set.
             */
            public RestrictedDocument() {
            }

            /**
             * add a string; only those chars in the valid text list are allowed
             *
             * @param offset
             * @param string
             * @param attributes
             * @throws BadLocationException
             */
            public void insertString(int offset,
                                     String string,
                                     AttributeSet attributes)
                    throws BadLocationException {
                if (string == null) {
                    return;
                }
                int len = string.length();
                StringBuffer buffer = new StringBuffer(string.length());
                for (int i = 0; i < len; i++) {
                    char ch = string.charAt(i);
                    if (validText.indexOf(ch) >= 0) {
                        buffer.append(ch);
                    }
                }
                super.insertString(offset, new String(buffer), attributes);
            }
        }    // end class NumericDocument
    }

    /**
     * because we cant use Java1.4's JFormattedTextField, here is
     * a class that accepts numbers only
     */
    public static class NumberField extends RestrictedTextField {
        /**
         * Field VALID_TEXT
         */
        private static final String VALID_TEXT = "0123456789";

        /**
         * Constructs a new <code>TextField</code>.  A default model is created,
         * the initial string is <code>null</code>,
         * and the number of columns is set to 0.
         */
        public NumberField() {
            super(VALID_TEXT);
        }

        /**
         * Constructs a new empty <code>TextField</code> with the specified
         * number of columns.
         * A default model is created and the initial string is set to
         * <code>null</code>.
         *
         * @param columns the number of columns to use to calculate
         *                the preferred width; if columns is set to zero, the
         *                preferred width will be whatever naturally results from
         *                the component implementation
         */
        public NumberField(int columns) {
            super(columns, VALID_TEXT);
        }

        /**
         * get the int value of a field, any invalid (non int) field returns
         * the default
         *
         * @param def default value
         * @return the field contents
         */
        public int getValue(int def) {
            int result = def;
            String text = getText();
            if ((text != null) && (text.length() != 0)) {
                try {
                    result = Integer.parseInt(text);
                } catch (NumberFormatException e) {
                }
            }
            return result;
        }

        /**
         * set the text to a numeric value
         *
         * @param value number to assign
         */
        public void setValue(int value) {
            setText(Integer.toString(value));
        }
    }    // end class NumericTextField

    /**
     * hostname fields
     */
    public static class HostnameField extends RestrictedTextField {

        // list of valid chars in a hostname

        /**
         * Field VALID_TEXT
         */
        private static final String VALID_TEXT =
                "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWZYZ-.";

        /**
         * Constructor HostnameField
         *
         * @param columns
         */
        public HostnameField(int columns) {
            super(columns, VALID_TEXT);
        }

        /**
         * Constructor HostnameField
         */
        public HostnameField() {
            super(VALID_TEXT);
        }
    }

}
