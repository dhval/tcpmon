package com.dhval.utils;

import javax.swing.*;
import java.awt.*;

public class JUtils {

    public static JTextField jTextField(String text, int col, int width) {
        JTextField textField = new JTextField(text, col);
        textField.setMaximumSize(new Dimension(width, Short.MAX_VALUE));
        return textField;
    }
}
