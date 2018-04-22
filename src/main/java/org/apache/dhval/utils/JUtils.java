package org.apache.dhval.utils;

import javax.swing.*;
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
}
