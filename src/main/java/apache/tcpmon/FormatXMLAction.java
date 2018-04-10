package apache.tcpmon;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class FormatXMLAction extends AbstractAction {
    private static final Logger LOG = LoggerFactory.getLogger(FormatXMLAction.class);
    private JPanel component;
    private RSyntaxTextArea textArea;

    public FormatXMLAction(JPanel component, RSyntaxTextArea textArea) {
        super("Format XML");
        this.component = component;
        this.textArea = textArea;
    }

    public void actionPerformed(ActionEvent e) {
        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_XML);
        try {
            if (!StringUtils.isEmpty(textArea.getText()))
                textArea.setText(Utils.prettyXML(textArea.getText()));
        } catch (Exception ex) {
            LOG.warn(ex.getMessage(), ex);
        }
    }
}
