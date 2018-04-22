package org.apache.dhval.logger;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import org.apache.tcpmon.TCPMon;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;

public class Appender extends AppenderBase<ILoggingEvent> {
    private PatternLayout patternLayout;
    private static SimpleAttributeSet ERROR_ATT, WARN_ATT, INFO_ATT, DEBUG_ATT, TRACE_ATT, RESTO_ATT;
    private LogPanel logPanel;

    static {
        ERROR_ATT = new SimpleAttributeSet();
        ERROR_ATT.addAttribute(StyleConstants.CharacterConstants.Bold, Boolean.TRUE);
        ERROR_ATT.addAttribute(StyleConstants.CharacterConstants.Italic, Boolean.FALSE);
        ERROR_ATT.addAttribute(StyleConstants.CharacterConstants.Foreground, new Color(153, 0, 0));
        WARN_ATT = new SimpleAttributeSet();
        WARN_ATT.addAttribute(StyleConstants.CharacterConstants.Bold, Boolean.FALSE);
        WARN_ATT.addAttribute(StyleConstants.CharacterConstants.Italic, Boolean.FALSE);
        WARN_ATT.addAttribute(StyleConstants.CharacterConstants.Foreground, new Color(153, 76, 0));
        INFO_ATT = new SimpleAttributeSet();
        INFO_ATT.addAttribute(StyleConstants.CharacterConstants.Bold, Boolean.FALSE);
        INFO_ATT.addAttribute(StyleConstants.CharacterConstants.Italic, Boolean.FALSE);
        INFO_ATT.addAttribute(StyleConstants.CharacterConstants.Foreground, new Color(0, 0, 153));
        DEBUG_ATT = new SimpleAttributeSet();
        DEBUG_ATT.addAttribute(StyleConstants.CharacterConstants.Bold, Boolean.FALSE);
        DEBUG_ATT.addAttribute(StyleConstants.CharacterConstants.Italic, Boolean.TRUE);
        DEBUG_ATT.addAttribute(StyleConstants.CharacterConstants.Foreground, new Color(64, 64, 64));
        TRACE_ATT = new SimpleAttributeSet();
        TRACE_ATT.addAttribute(StyleConstants.CharacterConstants.Bold, Boolean.FALSE);
        TRACE_ATT.addAttribute(StyleConstants.CharacterConstants.Italic, Boolean.TRUE);
        TRACE_ATT.addAttribute(StyleConstants.CharacterConstants.Foreground, new Color(153, 0, 76));
        RESTO_ATT = new SimpleAttributeSet();
        RESTO_ATT.addAttribute(StyleConstants.CharacterConstants.Bold, Boolean.FALSE);
        RESTO_ATT.addAttribute(StyleConstants.CharacterConstants.Italic, Boolean.TRUE);
        RESTO_ATT.addAttribute(StyleConstants.CharacterConstants.Foreground, new Color(0, 0, 0));
    }

    @Override
    public void start() {
        patternLayout = new PatternLayout();
        patternLayout.setContext(getContext());
        patternLayout.setPattern("%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n");
        patternLayout.start();
        super.start();
    }

    @Override
    protected void append(ILoggingEvent event) {
        String formattedMsg = patternLayout.doLayout(event);
        if (TCPMon.context == null) {
            return;
        }
        logPanel = TCPMon.context.getBean(LogPanel.class);
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    JTextPane textPane = logPanel.jTextPane;
                    try {
                        if (textPane.getDocument().getDefaultRootElement().getElementCount() > 1000) {
                            int end = getLineEndOffset(textPane, 200);
                            replaceRange(textPane, null, 0, end);
                        }
                       if (event.getLevel() == Level.ERROR)
                            textPane.getDocument().insertString(textPane.getDocument().getLength(), formattedMsg, ERROR_ATT);
                        else if (event.getLevel() == Level.WARN)
                            textPane.getDocument().insertString(textPane.getDocument().getLength(), formattedMsg, WARN_ATT);
                        else if (event.getLevel() == Level.INFO)
                            textPane.getDocument().insertString(textPane.getDocument().getLength(), formattedMsg, INFO_ATT);
                        else if (event.getLevel() == Level.DEBUG)
                            textPane.getDocument().insertString(textPane.getDocument().getLength(), formattedMsg, DEBUG_ATT);
                        else if (event.getLevel() == Level.TRACE)
                            textPane.getDocument().insertString(textPane.getDocument().getLength(), formattedMsg, TRACE_ATT);
                        else
                            textPane.getDocument().insertString(textPane.getDocument().getLength(), formattedMsg, RESTO_ATT);

                    } catch (BadLocationException e) {
                        // Ignore
                    }
                    textPane.setCaretPosition(textPane.getDocument().getLength());
                }
            });
    }

    private int getLineCount(JTextPane textPane) {
        return textPane.getDocument().getDefaultRootElement().getElementCount();
    }

    private int getLineEndOffset(JTextPane textPane, int line) throws BadLocationException {
        int lineCount = getLineCount(textPane);
        if (line < 0) {
            throw new BadLocationException("Negative line", -1);
        } else if (line >= lineCount) {
            throw new BadLocationException("No such line", textPane.getDocument().getLength()+1);
        } else {
            Element map = textPane.getDocument().getDefaultRootElement();
            Element lineElem = map.getElement(line);
            int endOffset = lineElem.getEndOffset();
            return ((line == lineCount - 1) ? (endOffset - 1) : endOffset);
        }
    }

    private void replaceRange(JTextPane textPane, String str, int start, int end) throws IllegalArgumentException {
        if (end < start) {
            throw new IllegalArgumentException("end before start");
        }
        Document doc = textPane.getDocument();
        if (doc != null) {
            try {
                if (doc instanceof AbstractDocument) {
                    ((AbstractDocument)doc).replace(start, end - start, str, null);
                }
                else {
                    doc.remove(start, end - start);
                    doc.insertString(start, str, null);
                }
            } catch (BadLocationException e) {
                throw new IllegalArgumentException(e.getMessage());
            }
        }
    }
}