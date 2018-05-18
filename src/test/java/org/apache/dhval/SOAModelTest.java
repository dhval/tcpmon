package org.apache.dhval;

import com.predic8.wsdl.Definitions;
import com.predic8.wsdl.WSDLParser;
import com.predic8.wstool.creator.RequestTemplateCreator;
import com.predic8.wstool.creator.SOARequestCreator;
import groovy.xml.MarkupBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.StringWriter;

@RunWith(SpringRunner.class)
public class SOAModelTest {
    private static final Logger LOG = LoggerFactory.getLogger(SOAModelTest.class);

    @Test
    public void extractXpath() throws Exception {
        WSDLParser parser = new WSDLParser();
        Definitions wsdl = parser.parse("/Users/dhval/projects/dp/test/default/local/ELectronicReporting/ER_Probation-Full.wsdl");
        StringWriter writer = new StringWriter();
        SOARequestCreator creator = new SOARequestCreator(wsdl, new RequestTemplateCreator(), new MarkupBuilder(writer));
//creator.createRequest(PortType name, Operation name, Binding name);
        creator.createRequest("ElectronicReportingInterface", "PublishCountyProbationEvent", "ElectronicReportingSOAPBinding");
        System.out.println(writer);

    }
}
