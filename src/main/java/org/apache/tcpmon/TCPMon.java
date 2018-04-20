/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.tcpmon;

import com.dhval.utils.DateUtils;
import com.dhval.logger.LogPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;


import javax.annotation.PostConstruct;
import javax.swing.*;
import java.awt.*;
import java.util.ResourceBundle;

/**
 * Proxy that sniffs and shows HTTP messages and responses, both SOAP and plain HTTP.
 */

@SpringBootApplication
@ComponentScan(basePackages = {"com.dhval"})
public class TCPMon extends JFrame {

    private static final Logger LOG = LoggerFactory.getLogger(TCPMon.class);

    @Bean
    JTabbedPane createNotebook() {
        return notebook;
    }
    /**
     * Field notebook
     */
    private JTabbedPane notebook = new JTabbedPane();;

    /**
     * Field STATE_COLUMN
     */
    public static final int STATE_COLUMN = 0;

    /**
     * Field OUTHOST_COLUMN
     */
    static final int OUTHOST_COLUMN = 3;

    /**
     * Field REQ_COLUMN
     */
    public static final int REQ_COLUMN = 4;

    /**
     * Field ELAPSED_COLUMN
     */
    static final int ELAPSED_COLUMN = 5;
    
    /**
     * Field DEFAULT_HOST
     */
    static final String DEFAULT_HOST = "127.0.0.1";

    /**
     * Field DEFAULT_PORT
     */
    static final int DEFAULT_PORT = 8888;

    public static final String CWD = System.getProperty("user.dir");

    public static ConfigurableApplicationContext context;

    public TCPMon() {
        super("TCPMon2");
    }

    /**
     * Constructor
     *
     * @param listenPort
     * @param targetHost
     * @param targetPort
     * @param embedded
     */
    public void start(int listenPort, String targetHost, int targetPort, boolean embedded) {
        JComponent componentToDisplay;

        this.getContentPane().add(notebook);
        componentToDisplay = new AdminPane(notebook, getMessage("admin00", "Admin"));
         //TransactionPanel transactionPanel = new TransactionPanel(notebook, this);
        if (listenPort != 0) {
            Listener l = null;
            if (targetHost == null) {
                l = new Listener(notebook, null, listenPort, targetHost, targetPort, true, null);
            } else {
                l = new Listener(notebook, null, listenPort, targetHost, targetPort, false, null);
            }
            componentToDisplay = l;
            l.HTTPProxyHost = System.getProperty("http.proxyHost");
            if ((l.HTTPProxyHost != null) && l.HTTPProxyHost.equals("")) {
                l.HTTPProxyHost = null;
            }
            if (l.HTTPProxyHost != null) {
                String tmp = System.getProperty("http.proxyPort");
                if ((tmp != null) && tmp.equals("")) {
                    tmp = null;
                }
                if (tmp == null) {
                    l.HTTPProxyPort = 80;
                } else {
                    l.HTTPProxyPort = Integer.parseInt(tmp);
                }
            }
        }
        if (!embedded) {
            this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        }
        this.pack();
        this.setSize(1000, 700);
        notebook.setSelectedComponent(componentToDisplay);
        this.setVisible(true);
    }

    /**
     * Constructor
     *
     * @param listenPort
     * @param targetHost
     * @param targetPort

    public TCPMon(int listenPort, String targetHost, int targetPort) {
        this(listenPort, targetHost, targetPort, false);
    }
     */
    /**
     * set up the L&F
     *
     * @param nativeLookAndFeel
     * @throws Exception
     */
    private static void setupLookAndFeel(boolean nativeLookAndFeel) throws Exception {
        String classname = UIManager.getCrossPlatformLookAndFeelClassName();
        if (nativeLookAndFeel) {
            classname = UIManager.getSystemLookAndFeelClassName();
        }
        String lafProperty = System.getProperty("httptracer.laf", "");
        if (lafProperty.length() > 0) {
            classname = lafProperty;
        }
        try {
            UIManager.setLookAndFeel(classname);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
    }

    /**
     * this is our main method
     *
     * @param args
     */
    public static void main(String[] args) {
        context = new SpringApplicationBuilder(TCPMon.class)
                .headless(false).run(args);

        try {
            TCPMon.setupLookAndFeel(true);
        } catch (Throwable exp) {
            exp.printStackTrace();
        }

        EventQueue.invokeLater(() -> {
            TCPMon ex = context.getBean(TCPMon.class);
            ex.setVisible(true);
            ex.start(0, null, 0, false);
        });

        LOG.info("Current Working Directory: " + System.getProperty("user.dir"));
        LOG.info("GMT: " + DateUtils.gmt());

 /*       try {

            // switch between swing L&F here
            setupLookAndFeel(true);
            if (args.length == 3) {
                int p1 = Integer.parseInt(args[0]);
                int p2 = Integer.parseInt(args[2]);
                new TCPMon(p1, args[1], p2);
            } else if (args.length == 1) {
                int p1 = Integer.parseInt(args[0]);
                new TCPMon(p1, null, 0);
            } else if (args.length != 0) {
                System.err.println(
                        getMessage("usage00", "Usage:")
                        + " TCPMon [listenPort targetHost targetPort]\n");
            } else {
                new TCPMon(0, null, 0);
            }
        } catch (Throwable exp) {
            exp.printStackTrace();
        }*/
    }

    /**
     * Field messages
     */
    private static ResourceBundle messages = null;

    /**
     * Get the message with the given key.  There are no arguments for this message.
     *
     * @param key
     * @param defaultMsg
     * @return string
     */
    public static String getMessage(String key, String defaultMsg) {
        try {
            if (messages == null) {
                initializeMessages();
            }
            return messages.getString(key);
        } catch (Throwable t) {

            // If there is any problem whatsoever getting the internationalized
            // message, return the default.
            return defaultMsg;
        }
    }

    /**
     * Load the resource bundle messages from the properties file.  This is ONLY done when it is
     * needed.  If no messages are printed (for example, only Wsdl2java is being run in non-
     * verbose mode) then there is no need to read the properties file.
     */
    private static void initializeMessages() {
        messages = ResourceBundle.getBundle("org.apache.ws.commons.tcpmon.tcpmon");
    }

}
