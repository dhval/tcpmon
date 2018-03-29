package apache.tcpmon;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.XPathCompiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import java.io.ByteArrayOutputStream;

public class Utils {
    private static final Logger LOG = LoggerFactory.getLogger(Utils.class);
    private static Processor proc = new Processor(false);
    private static XPathCompiler xpath = proc.newXPathCompiler();

    static {
        xpath.declareNamespace("jnet", "http://www.jnet.state.pa.us/niem/JNET/jnet-core/1");
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
}
