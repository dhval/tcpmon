package org.apache.dhval.wss;
import org.apache.dhval.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ws.soap.SoapFault;
import org.springframework.ws.soap.SoapFaultDetail;
import org.springframework.ws.soap.server.endpoint.SoapFaultMappingExceptionResolver;
import javax.xml.namespace.QName;

public class FaultResolver extends SoapFaultMappingExceptionResolver {

    private static final Logger LOG = LoggerFactory.getLogger(FaultResolver.class);

    private static final QName CODE = new QName("code");
    private static final QName DESCRIPTION = new QName("description");

    @Override
    protected void customizeFault(Object endpoint, Exception ex, SoapFault fault) {
        LOG.warn("Exception processed ", ex);
        try {
            LOG.info(Utils.prettyXML(fault.toString()));
        } catch (Exception e) {
            LOG.warn(e.getMessage(), e);
        }
    }
}
