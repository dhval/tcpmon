package org.apache.dhval.admin;

import org.apache.dhval.server.MockPanel;
import org.apache.dhval.utils.JUtils;
import org.apache.tcpmon.Listener;
import org.apache.tcpmon.SlowLinkSimulator;
import org.apache.tcpmon.TCPMon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;;
import static org.apache.dhval.utils.JUtils.*;

/**
 * this is the admin page
 */

@Component
public class AdminPane extends JPanel {
    private static final Logger LOG = LoggerFactory.getLogger(AdminPane.class);

    /**
     * Field listenerButton, proxyButton
     */
    public JRadioButton listenerButton, proxyButton;

    /**
     * Field hostLabel, tportLabel
     */
    public JLabel hostLabel, tportLabel;

    /**
     * Field port
     */
    public NumberField port;

    /**
     * Field host
     */
    public HostnameField host;

    /**
     * Field tport
     */
    public NumberField tport;

    /**
     * Field noteb
     */
    public JTabbedPane noteb;

    /**
     * Field HTTPProxyBox
     */
    public JCheckBox HTTPProxyBox;

    /**
     * Field HTTPProxyHost
     */
    public HostnameField HTTPProxyHost;

    /**
     * Field HTTPProxyPort
     */
    public NumberField HTTPProxyPort;

    /**
     * Field HTTPProxyHostLabel, HTTPProxyPortLabel
     */
    public JLabel HTTPProxyHostLabel, HTTPProxyPortLabel;

    /**
     * Field delayTimeLabel, delayBytesLabel
     */
    public JLabel delayTimeLabel, delayBytesLabel;

    /**
     * Field delayTime, delayBytes
     */
    public NumberField delayTime, delayBytes;

    /**
     * Field delayBox
     */
    public JCheckBox delayBox;

     /**
     * Constructor AdminPage
     *
     * @param notebook
     */
    public AdminPane(@Autowired JTabbedPane notebook, @Autowired MockPanel mockPanel) {
        JPanel mainPane = null;
        JButton addButton = null;

        this.setLayout(new BorderLayout());
        noteb = notebook;
        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        mainPane = new JPanel(layout);
        c.anchor = GridBagConstraints.WEST;
        c.gridwidth = GridBagConstraints.REMAINDER;
        mainPane.add(new JLabel(TCPMon.getMessage("newTCP00",
                "Create a new TCPMon...")
                + " "), c);

        // Add some blank space
        mainPane.add(Box.createRigidArea(new Dimension(1, 5)), c);

        // The listener info
        // /////////////////////////////////////////////////////////////////
        JPanel tmpPanel = new JPanel(new GridBagLayout());
        c.anchor = GridBagConstraints.WEST;
        c.gridwidth = 1;
        tmpPanel.add(new JLabel(TCPMon.getMessage("listenPort00",
                "Listen Port #")
                + " "), c);
        c.anchor = GridBagConstraints.WEST;
        c.gridwidth = GridBagConstraints.REMAINDER;
        tmpPanel.add(port = new NumberField(4), c);
        mainPane.add(tmpPanel, c);
        mainPane.add(Box.createRigidArea(new Dimension(1, 5)), c);

        // Group for the radio buttons
        ButtonGroup btns = new ButtonGroup();
        c.anchor = GridBagConstraints.WEST;
        c.gridwidth = GridBagConstraints.REMAINDER;
        mainPane.add(new JLabel(TCPMon.getMessage("actAs00", "Act as a...")), c);

        // Target Host/Port section
        // /////////////////////////////////////////////////////////////////
        c.anchor = GridBagConstraints.WEST;
        c.gridwidth = GridBagConstraints.REMAINDER;
        final String listener = TCPMon.getMessage("listener00", "Listener");
        mainPane.add(listenerButton = new JRadioButton(listener), c);
        btns.add(listenerButton);
        listenerButton.setSelected(true);
        listenerButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if (listener.equals(event.getActionCommand())) {
                    boolean state = listenerButton.isSelected();
                    tport.setEnabled(state);
                    host.setEnabled(state);
                    hostLabel.setForeground(state
                            ? Color.black
                            : Color.gray);
                    tportLabel.setForeground(state
                            ? Color.black
                            : Color.gray);
                }
            }
        });
        c.anchor = GridBagConstraints.WEST;
        c.gridwidth = 1;
        mainPane.add(Box.createRigidArea(new Dimension(25, 0)));
        mainPane.add(hostLabel =
                new JLabel(TCPMon.getMessage("targetHostname00",
                        "Target Hostname")
                + " "), c);
        c.anchor = GridBagConstraints.WEST;
        c.gridwidth = GridBagConstraints.REMAINDER;
        host = new HostnameField(30);
        mainPane.add(host, c);
        host.setText(TCPMon.DEFAULT_HOST);
        c.anchor = GridBagConstraints.WEST;
        c.gridwidth = 1;
        mainPane.add(Box.createRigidArea(new Dimension(25, 0)));
        mainPane.add(tportLabel =
                new JLabel(TCPMon.getMessage("targetPort00", "Target Port #")
                + " "), c);
        c.anchor = GridBagConstraints.WEST;
        c.gridwidth = GridBagConstraints.REMAINDER;
        tport = new NumberField(4);
        mainPane.add(tport, c);
        tport.setValue(TCPMon.DEFAULT_PORT);

        // Act as proxy section
        // /////////////////////////////////////////////////////////////////
        c.anchor = GridBagConstraints.WEST;
        c.gridwidth = GridBagConstraints.REMAINDER;
        final String proxy = TCPMon.getMessage("proxy00", "HTTP Proxy");
        mainPane.add(proxyButton = new JRadioButton(proxy), c);
        btns.add(proxyButton);
        proxyButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if (proxy.equals(event.getActionCommand())) {
                    boolean state = proxyButton.isSelected();
                    tport.setEnabled(!state);
                    host.setEnabled(!state);
                    hostLabel.setForeground(state
                            ? Color.gray
                            : Color.black);
                    tportLabel.setForeground(state
                            ? Color.gray
                            : Color.black);
                }
            }
        });

        // Spacer
        // ///////////////////////////////////////////////////////////////
        c.anchor = GridBagConstraints.WEST;
        c.gridwidth = GridBagConstraints.REMAINDER;
        mainPane.add(Box.createRigidArea(new Dimension(1, 10)), c);

        // Options section
        // /////////////////////////////////////////////////////////////////
        JPanel opts = new JPanel(new GridBagLayout());
        opts.setBorder(new TitledBorder(TCPMon.getMessage("options00",
                "Options")));
        c.anchor = GridBagConstraints.WEST;
        c.gridwidth = GridBagConstraints.REMAINDER;
        mainPane.add(opts, c);

        // HTTP Proxy Support section
        // /////////////////////////////////////////////////////////////////
        c.anchor = GridBagConstraints.WEST;
        c.gridwidth = GridBagConstraints.REMAINDER;
        final String proxySupport = TCPMon.getMessage("proxySupport00",
                "HTTP Proxy Support");
        opts.add(HTTPProxyBox = new JCheckBox(proxySupport), c);
        c.anchor = GridBagConstraints.WEST;
        c.gridwidth = 1;
        opts.add(HTTPProxyHostLabel =
                new JLabel(TCPMon.getMessage("hostname00", "Hostname") + " "),
                c);
        HTTPProxyHostLabel.setForeground(Color.gray);
        c.anchor = GridBagConstraints.WEST;
        c.gridwidth = GridBagConstraints.REMAINDER;
        opts.add(HTTPProxyHost = new HostnameField(30), c);
        HTTPProxyHost.setEnabled(false);
        c.anchor = GridBagConstraints.WEST;
        c.gridwidth = 1;
        opts.add(HTTPProxyPortLabel =
                new JLabel(TCPMon.getMessage("port00", "Port #") + " "), c);
        HTTPProxyPortLabel.setForeground(Color.gray);
        c.anchor = GridBagConstraints.WEST;
        c.gridwidth = GridBagConstraints.REMAINDER;
        opts.add(HTTPProxyPort = new NumberField(4), c);
        HTTPProxyPort.setEnabled(false);
        HTTPProxyBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if (proxySupport.equals(event.getActionCommand())) {
                    boolean b = HTTPProxyBox.isSelected();
                    Color color = b
                            ? Color.black
                            : Color.gray;
                    HTTPProxyHost.setEnabled(b);
                    HTTPProxyPort.setEnabled(b);
                    HTTPProxyHostLabel.setForeground(color);
                    HTTPProxyPortLabel.setForeground(color);
                }
            }
        });

        // Set default proxy values...
        String tmp = System.getProperty("http.proxyHost");
        if ((tmp != null) && tmp.equals("")) {
            tmp = null;
        }
        HTTPProxyBox.setSelected(tmp != null);
        HTTPProxyHost.setEnabled(tmp != null);
        HTTPProxyPort.setEnabled(tmp != null);
        HTTPProxyHostLabel.setForeground((tmp != null)
                ? Color.black
                : Color.gray);
        HTTPProxyPortLabel.setForeground((tmp != null)
                ? Color.black
                : Color.gray);
        if (tmp != null) {
            HTTPProxyBox.setSelected(true);
            HTTPProxyHost.setText(tmp);
            tmp = System.getProperty("http.proxyPort");
            if ((tmp != null) && tmp.equals("")) {
                tmp = null;
            }
            if (tmp == null) {
                tmp = "80";
            }
            HTTPProxyPort.setText(tmp);
        }

        // add byte delay fields
        opts.add(Box.createRigidArea(new Dimension(1, 10)), c);
        c.anchor = GridBagConstraints.WEST;
        c.gridwidth = GridBagConstraints.REMAINDER;
        final String delaySupport = TCPMon.getMessage("delay00",
                "Simulate Slow Connection");
        opts.add(delayBox = new JCheckBox(delaySupport), c);

        // bytes per pause
        c.anchor = GridBagConstraints.WEST;
        c.gridwidth = 1;
        delayBytesLabel = new JLabel(TCPMon.getMessage("delay01",
                "Bytes per Pause"));
        opts.add(delayBytesLabel, c);
        delayBytesLabel.setForeground(Color.gray);
        c.anchor = GridBagConstraints.WEST;
        c.gridwidth = GridBagConstraints.REMAINDER;
        opts.add(delayBytes = new NumberField(6), c);
        delayBytes.setEnabled(false);

        // delay interval
        c.anchor = GridBagConstraints.WEST;
        c.gridwidth = 1;
        delayTimeLabel = new JLabel(TCPMon.getMessage("delay02",
                "Delay in Milliseconds"));
        opts.add(delayTimeLabel, c);
        delayTimeLabel.setForeground(Color.gray);
        c.anchor = GridBagConstraints.WEST;
        c.gridwidth = GridBagConstraints.REMAINDER;
        opts.add(delayTime = new NumberField(6), c);
        delayTime.setEnabled(false);

        // enabler callback
        delayBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if (delaySupport.equals(event.getActionCommand())) {
                    boolean b = delayBox.isSelected();
                    Color color = b
                            ? Color.black
                            : Color.gray;
                    delayBytes.setEnabled(b);
                    delayTime.setEnabled(b);
                    delayBytesLabel.setForeground(color);
                    delayTimeLabel.setForeground(color);
                }
            }
        });

        mainPane.add(Box.createRigidArea(new Dimension(1, 10)), c);

        // Act as Server section
        // /////////////////////////////////////////////////////////////////
        mainPane.add(mockPanel, JUtils.createGridEndElement());

        // Spacer
        // ////////////////////////////////////////////////////////////////
        mainPane.add(Box.createRigidArea(new Dimension(1, 10)), c);

        // ADD Button
        // /////////////////////////////////////////////////////////////////

        JPanel bottomButtons = new JPanel();
        bottomButtons.setLayout(new BoxLayout(bottomButtons, BoxLayout.LINE_AXIS));
        bottomButtons.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));


        final String add = TCPMon.getMessage("add00", "Add Listener");
        bottomButtons.add(addButton = new JButton(add), c);

        mainPane.add(bottomButtons, JUtils.createGridElement());

        this.add(new JScrollPane(mainPane), BorderLayout.CENTER);

        // addButton.setEnabled( false );
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                LOG.info(event.toString());
                LOG.info(event.getActionCommand().toString());
                if (add.equals(event.getActionCommand())) {
                    String text;
                    Listener l = null;
                    int lPort;
                    lPort = port.getValue(0);
                    if (lPort == 0) {

                        // no port, button does nothing
                        return;
                    }
                    String tHost = host.getText();
                    int tPort = 0;
                    tPort = tport.getValue(0);
                    SlowLinkSimulator slowLink = null;
                    if (delayBox.isSelected()) {
                        int bytes = delayBytes.getValue(0);
                        int time = delayTime.getValue(0);
                        slowLink = new SlowLinkSimulator(bytes, time);
                    }
                    try {
                        l = new Listener(noteb, null, lPort, tHost, tPort,
                                proxyButton.isSelected(),
                                slowLink);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    // Pick-up the HTTP Proxy settings
                    // /////////////////////////////////////////////////
                    text = HTTPProxyHost.getText();
                    if ("".equals(text)) {
                        text = null;
                    }
                    l.HTTPProxyHost = text;
                    text = HTTPProxyPort.getText();
                    int proxyPort = HTTPProxyPort.getValue(-1);
                    if (proxyPort != -1) {
                        l.HTTPProxyPort = Integer.parseInt(text);
                    }

                    // reset the port
                    port.setText(null);
                }
            }
        });

        notebook.addTab("Admin", this);
        notebook.setSelectedComponent(this);
        notebook.repaint();
        notebook.setSelectedIndex(notebook.getTabCount() - 1);
    }
  }

