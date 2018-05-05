package org.apache.tcpmon;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.dhval.dto.LocalServer;
import org.apache.dhval.dto.TcpProxy;
import org.apache.dhval.storage.LocalDB;
import org.apache.dhval.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.ws.soap.server.endpoint.SoapFaultMappingExceptionResolver;

import javax.annotation.PostConstruct;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Proxy that sniffs and shows HTTP messages and responses, both SOAP and plain HTTP.
 */

@SpringBootApplication
@ComponentScan(basePackages = {"org.apache.dhval"})
@EnableScheduling
//@EnableAutoConfiguration(exclude={DataSourceAutoConfiguration.class})
public class TCPMon extends JFrame {

    private static final Logger LOG = LoggerFactory.getLogger(TCPMon.class);

    public static Map jsonMap;

    static {
        System.setProperty("javax.xml.soap.MessageFactory", "com.sun.xml.internal.messaging.saaj.soap.ver1_2.SOAPMessageFactory1_2Impl");
        System.setProperty("javax.xml.bind.JAXBContext", "com.sun.xml.internal.bind.v2.ContextFactory");


        try {
            jsonMap = new ObjectMapper().readValue(new File("config.json"), Map.class);
        } catch (IOException io) {
            LOG.warn("config.json file not found");
            jsonMap = new HashMap();
        }
        Utils.disableSSLValidation();
    }

    /**
     * Field notebook
     */
    private JTabbedPane notebook = new JTabbedPane();;

    @Bean
    JTabbedPane createNotebook() {
        return notebook;
    }

    @Bean
    LocalServer createLocalServer() {
        return LocalServer.buildFromMap(jsonMap);
    }

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
    public static final String DEFAULT_HOST = "127.0.0.1";

    /**
     * Field DEFAULT_PORT
     */
    public static final int DEFAULT_PORT = 8888;

    public static final String CWD = System.getProperty("user.dir");

    public static ConfigurableApplicationContext context;

    public TCPMon() {
        super("TCPMon2");
    }

    @PostConstruct
    public void start() {
        boolean embedded = false;
        this.getContentPane().add(notebook);
        if (!embedded) {
            this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        }
        this.pack();
        this.setSize(1000, 800);
        this.setVisible(true);
        initializeTcpProxies(TcpProxy.buildProxies(jsonMap));
    }

    private void initializeTcpProxies(List<TcpProxy> proxies) {
        for(TcpProxy proxy : proxies) {
            if (proxy.getListenPort() != 0) {
                Listener l = null;
                if (proxy.getTargetHost() == null) {
                    l = new Listener(notebook, null, proxy.getListenPort(), proxy.getTargetHost(), proxy.getTargetPort(), true, null);
                } else {
                    l = new Listener(notebook, null, proxy.getListenPort(), proxy.getTargetHost(), proxy.getTargetPort(), false, null);
                }
                // componentToDisplay = l;
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
        }
    }

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
    public static void main(String[] args) throws Exception {
        context = new SpringApplicationBuilder(TCPMon.class).headless(false).run(args);
        context.registerShutdownHook();
        try {
            TCPMon.setupLookAndFeel(true);
        } catch (Throwable exp) {
            exp.printStackTrace();
        }
        EventQueue.invokeLater(() -> {
            final LocalDB localDB = context.getBean(LocalDB.class);
            TCPMon tcpMon = context.getBean(TCPMon.class);
            tcpMon.setVisible(true);
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    if (localDB != null) localDB.close();
                }
            });
        });
        LOG.info("Current Working Directory: " + System.getProperty("user.dir"));
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
        messages = ResourceBundle.getBundle("org.apache.tcpmon.tcpmon");
    }

}
