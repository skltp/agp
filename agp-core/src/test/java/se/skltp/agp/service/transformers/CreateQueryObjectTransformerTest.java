package se.skltp.agp.service.transformers;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class CreateQueryObjectTransformerTest {

    @Test
    public void testMessageContainsSourceSystemHSAId() {
        assertFalse(testThisSoapMessage(
                                          " <soapenv:Envelope                                                                             " +
                                          " xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"                                   " +
                                          " xmlns:urn=\"urn:riv:itintegration:registry:1\"                                                " +
                                          " xmlns:urn1=\"urn:riv:clinicalprocess:healthcond:actoutcome:GetReferralOutcomeResponder:3\"    " +
                                          " xmlns:urn2=\"urn:riv:clinicalprocess:healthcond:actoutcome:3\">                               " +
                                          "   <soapenv:Header>                                                                            " + 
                                          "      <urn:LogicalAddress>5565594230</urn:LogicalAddress>                                      " + 
                                          "   </soapenv:Header>                                                                           " + 
                                          "   <soapenv:Body>                                                                              " + 
                                          "      <urn1:GetReferralOutcome>                                                                " + 
                                          "         <urn1:patientId>                                                                      " + 
                                          "            <urn2:id>121212121212</urn2:id>                                                    " + 
                                          "            <urn2:type>1.2.752.129.2.1.3.1</urn2:type>                                         " + 
                                          "         </urn1:patientId>                                                                     " + 
                                          "         <urn1:sourceSystemHSAId>HSA-ID-5</urn1:sourceSystemHSAId>                             " + 
                                          "      </urn1:GetReferralOutcome>                                                               " + 
                                          "   </soapenv:Body>                                                                             " + 
                                          " </soapenv:Envelope>"
                                          ) );                    
    }
    

    
    @Test
    public void testMessageContainsSourceSystemHSAid() {
        assertFalse(testThisSoapMessage(
                                          " <soapenv:Envelope                                                                             " +
                                          " xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"                                   " +
                                          " xmlns:urn=\"urn:riv:itintegration:registry:1\"                                                " +
                                          " xmlns:urn1=\"urn:riv:clinicalprocess:healthcond:actoutcome:GetReferralOutcomeResponder:3\"    " +
                                          " xmlns:urn2=\"urn:riv:clinicalprocess:healthcond:actoutcome:3\">                               " +
                                          "   <soapenv:Header>                                                                            " + 
                                          "      <urn:LogicalAddress>5565594230</urn:LogicalAddress>                                      " + 
                                          "   </soapenv:Header>                                                                           " + 
                                          "   <soapenv:Body>                                                                              " + 
                                          "      <urn1:GetReferralOutcome>                                                                " + 
                                          "         <urn1:patientId>                                                                      " + 
                                          "            <urn2:id>121212121212</urn2:id>                                                    " + 
                                          "            <urn2:type>1.2.752.129.2.1.3.1</urn2:type>                                         " + 
                                          "         </urn1:patientId>                                                                     " + 
                                          "         <urn1:sourceSystemHSAid>HSA-ID-5</urn1:sourceSystemHSAid>                             " + 
                                          "      </urn1:GetReferralOutcome>                                                               " + 
                                          "   </soapenv:Body>                                                                             " + 
                                          " </soapenv:Envelope>"
                                          ) );                    
    }
    
    @Test
    public void testMessageContainsEmptySourceSystemHSAId() {
        assertTrue(testThisSoapMessage(
                                          " <soapenv:Envelope                                                                             " +
                                          " xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"                                   " +
                                          " xmlns:urn=\"urn:riv:itintegration:registry:1\"                                                " +
                                          " xmlns:urn1=\"urn:riv:clinicalprocess:healthcond:actoutcome:GetReferralOutcomeResponder:3\"    " +
                                          " xmlns:urn2=\"urn:riv:clinicalprocess:healthcond:actoutcome:3\">                               " +
                                          "   <soapenv:Header>                                                                            " + 
                                          "      <urn:LogicalAddress>5565594230</urn:LogicalAddress>                                      " + 
                                          "   </soapenv:Header>                                                                           " + 
                                          "   <soapenv:Body>                                                                              " + 
                                          "      <urn1:GetReferralOutcome>                                                                " + 
                                          "         <urn1:patientId>                                                                      " + 
                                          "            <urn2:id>121212121212</urn2:id>                                                    " + 
                                          "            <urn2:type>1.2.752.129.2.1.3.1</urn2:type>                                         " + 
                                          "         </urn1:patientId>                                                                     " + 
                                          "         <urn1:sourceSystemHSAid/>                                                             " + 
                                          "      </urn1:GetReferralOutcome>                                                               " + 
                                          "   </soapenv:Body>                                                                             " + 
                                          " </soapenv:Envelope>"
                                          ) );                    
    }
    
    @Test
    public void testMessageContainsNoSourceSystemHSAId() {
        assertTrue(testThisSoapMessage(
                                          " <soapenv:Envelope                                                                             " +
                                          " xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"                                   " +
                                          " xmlns:urn=\"urn:riv:itintegration:registry:1\"                                                " +
                                          " xmlns:urn1=\"urn:riv:clinicalprocess:healthcond:actoutcome:GetReferralOutcomeResponder:3\"    " +
                                          " xmlns:urn2=\"urn:riv:clinicalprocess:healthcond:actoutcome:3\">                               " +
                                          "   <soapenv:Header>                                                                            " + 
                                          "      <urn:LogicalAddress>5565594230</urn:LogicalAddress>                                      " + 
                                          "   </soapenv:Header>                                                                           " + 
                                          "   <soapenv:Body>                                                                              " + 
                                          "      <urn1:GetReferralOutcome>                                                                " + 
                                          "         <urn1:patientId>                                                                      " + 
                                          "            <urn2:id>121212121212</urn2:id>                                                    " + 
                                          "            <urn2:type>1.2.752.129.2.1.3.1</urn2:type>                                         " + 
                                          "         </urn1:patientId>                                                                     " + 
                                          "      </urn1:GetReferralOutcome>                                                               " + 
                                          "   </soapenv:Body>                                                                             " + 
                                          " </soapenv:Envelope>"
                                          ) );                    
    }
    
    private boolean testThisSoapMessage(String xml) {
        
        CreateQueryObjectTransformer objectUnderTest = new CreateQueryObjectTransformer();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder;
        Document doc = null;
        try {
            builder = factory.newDocumentBuilder();
            try {
                InputSource inputSource = new InputSource(new StringReader(xml));                    
                doc = builder.parse(inputSource);
                return (objectUnderTest.validSourceSystemHSAIdUsingXPath(doc));
            } catch (SAXException e) {
                fail(e.getLocalizedMessage());
            } catch (IOException e) {
                fail(e.getLocalizedMessage());
            }
        } catch (ParserConfigurationException e) {
            fail(e.getLocalizedMessage());
        }
        return false;
    }
    

}
