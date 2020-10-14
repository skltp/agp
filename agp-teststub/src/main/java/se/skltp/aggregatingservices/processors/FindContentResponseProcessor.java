package se.skltp.aggregatingservices.processors;

import static se.skltp.aggregatingservices.data.TestDataDefines.TEST_ID_FAULT_INVALID_ID_IN_EI;
import static se.skltp.aggregatingservices.data.TestDataDefines.TEST_ID_FAULT_TIMEOUT_IN_EI;
import static se.skltp.aggregatingservices.data.TestDataDefines.TEST_RR_ID_THREE_CATEGORIES;

import java.util.concurrent.TimeUnit;
import lombok.extern.log4j.Log4j2;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.cxf.message.MessageContentsList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import se.skltp.aggregatingservices.data.FindContentTestData;
import se.skltp.aggregatingservices.data.TestDataGenerator.TestProducerException;
import se.skltp.aggregatingservices.riv.itintegration.engagementindex.findcontentresponder.v1.FindContentResponseType;
import se.skltp.aggregatingservices.riv.itintegration.engagementindex.findcontentresponder.v1.FindContentType;
import se.skltp.aggregatingservices.riv.itintegration.engagementindex.v1.EngagementType;

@Component
@Log4j2
public class FindContentResponseProcessor implements Processor {

  @Autowired
  FindContentTestData findContentTestData;

  @Value("${teststub.findContentTimeout:30000}")
  int serviceTimeoutMS;

  @Value("${teststub.findContentCategorization:#{null}}")
  String categorization;

  @Override
  public void process(Exchange exchange) throws Exception {

    MessageContentsList messageContentsList = exchange.getIn().getBody(MessageContentsList.class);
    FindContentType request = (FindContentType) messageContentsList.get(1);

    exchange.getIn().setBody(createResponse(request));
  }

  private  FindContentResponseType createResponse(FindContentType request){
    log.info("### Engagemengsindex.findContent() received a request for Registered Resident id: {}",
        request.getRegisteredResidentIdentification());

    String id = request.getRegisteredResidentIdentification();

    // Return an error-message if invalid id
    if (TEST_ID_FAULT_INVALID_ID_IN_EI.equals(id)) {
      throw new TestProducerException("Invalid Id: " + id);
    }

    // Force a timeout if zero Id
    if (TEST_ID_FAULT_TIMEOUT_IN_EI.equals(id)) {
      try {
        TimeUnit.MILLISECONDS.sleep(serviceTimeoutMS+1000L);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }

    // Lookup the response
    FindContentResponseType response = findContentTestData.getResponseForPatient(request.getRegisteredResidentIdentification());
    updateResponseWithDomain(request, response);

    if(request.getCategorization()== null && categorization!=null){
      updateResponseWithCategory(categorization, response);
    } else if (!TEST_RR_ID_THREE_CATEGORIES.equals(id)) {
      updateResponseWithCategory(request.getCategorization(), response);
    }


    log.info("### Engagemengsindex return {} items", response.getEngagement().size());
    return response;
  }

  private void updateResponseWithCategory(String category,  FindContentResponseType response) {
    for(EngagementType engagementType : response.getEngagement()){
      engagementType.setCategorization(category);
    }
  }

  private void updateResponseWithDomain(FindContentType request, FindContentResponseType response) {
    for(EngagementType engagementType : response.getEngagement()){
      engagementType.setServiceDomain(request.getServiceDomain());
    }
  }

}
