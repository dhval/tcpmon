package org.apache.dhval.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import javax.net.ssl.*;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Utils {
    private static final Logger LOG = LoggerFactory.getLogger(Utils.class);
    private static Pattern patternURL = Pattern.compile("^(http[s]?):\\/\\/([^:\\/]*)([^\\/]*)\\/(.*)$");

    public static String printStackTrace(Exception e) {
        StringWriter w = new StringWriter();
        e.printStackTrace(new PrintWriter(w));
        return w.toString();
    }

    public static List<String> allFilesByType(String path, String type) throws IOException{
        String[] files = new File(path).list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if (name.endsWith("." + type))
                    return true;
                return false;
            }
        });
        return Arrays.stream(files).map(str -> path + "/" + str).collect(Collectors.toList());
    }

    public static void writeToDisk(String path, String content) throws IOException {
        try(BufferedWriter w = new BufferedWriter(new FileWriter(path,true)))
        {
            w.write(content);
        } catch(IOException e) {
            throw e;
        }
    }

    public static void overWriteToDisk(String path, String content) throws IOException {
        try(BufferedWriter w = new BufferedWriter(new FileWriter(path,false)))
        {
            w.write(content);
        } catch(IOException e) {
            throw e;
        }
    }

    public static String prettyXml(JAXBElement element) {
        try {
            JAXBContext jc = JAXBContext.newInstance(element.getValue().getClass());
            Marshaller marshaller = jc.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            marshaller.marshal(element, baos);
            return baos.toString();
        } catch (Exception e) {
            LOG.info(e.getMessage(), e);
        }
        return "";
    }

    public static String replaceHost(String url, String host) {
        LOG.info(url + host);
        Matcher matcher = patternURL.matcher(url);
        if (!matcher.find())
            return url;
        LOG.info("" + matcher.groupCount());
        if (matcher.groupCount() ==3) {
            LOG.info(matcher.group(0));
            LOG.info(matcher.group(2));
            return matcher.group(1) + "://" + host + "/" + matcher.group(3);
        } else if (matcher.groupCount() ==4) {
            LOG.info(matcher.group(0));
            LOG.info(matcher.group(1));
            LOG.info(matcher.group(2));
            LOG.info(matcher.group(3));
            LOG.info(matcher.group(4));
            if (host.matches("^\\d{1,3}.\\d{1,3}.\\d{1,3}.\\d{1,3}$"))
                return matcher.group(1) + "://" + host + matcher.group(3) + "/" + matcher.group(4);
            else
                return matcher.group(1) + "://" + host + "/" + matcher.group(4);
        }
        return url;
    }

    public static String prettyXML(String input) throws Exception {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        try {transformerFactory.setAttribute("indent-number", new Integer(1)); } catch (Exception e){}
        Transformer transformer = transformerFactory.newTransformer();
        try {transformer.setOutputProperty("{http://xml.apache.org/xalan}indent-amount", "1"); } catch (Exception e){}
        transformer.setOutputProperty(OutputKeys.INDENT , "yes");
        StringWriter writer = new StringWriter();
        transformer.transform(new StreamSource(new StringReader(input)), new StreamResult(writer));
        return writer.toString();
    }

    public static boolean isFilePresent(String fileName) throws IOException {
        return new File(fileName).isFile();
    }

    public static boolean isDirPresent(String fileName) throws IOException {
        return new File(fileName).isDirectory();
    }

    public static boolean isXML(String text) throws IOException {
        return !StringUtils.isEmpty(text) && text.charAt(0) == '<';
    }

    public static Boolean disableSSLValidation() {
        try {
            final SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            }}, null);

            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });
            return true;
        } catch (Exception e) {
          LOG.warn(e.getMessage(), e);
        }
        return false;
    }
}
