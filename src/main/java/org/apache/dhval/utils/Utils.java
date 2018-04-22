package org.apache.dhval.utils;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.XPathCompiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Utils {
    private static final Logger LOG = LoggerFactory.getLogger(Utils.class);
    private static Processor proc = new Processor(false);
    private static XPathCompiler xpath = proc.newXPathCompiler();
    private static Pattern pattern = Pattern.compile("^https?://[^/]+/([^/]+)/.*$");
    static {
        xpath.declareNamespace("jnet", "http://www.jnet.state.pa.us/niem/JNET/jnet-core/1");
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

    public static String getContextPath(String url) {
        LOG.info(url);
        Matcher matcher = pattern.matcher(url);
      //  LOG.info("" + matcher.find() + matcher.groupCount());
        if (matcher.find() && matcher.groupCount() ==1) {
            LOG.info(matcher.group(0));
            LOG.info(matcher.group(1));
            return matcher.group(1);
        }
        return null;
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

}
