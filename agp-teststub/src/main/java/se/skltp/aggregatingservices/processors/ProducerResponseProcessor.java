/*
 * Copyright © 2014-2026 Inera.
 * Copyright owner URL: https://www.inera.se/
 * SKLTP overview page: https://inera.atlassian.net/wiki/spaces/SKLTP/overview
 * This library is free software under the GNU Lesser General Public License v3.0.
 * Please refer to the full license files at the project root.
 */
package se.skltp.aggregatingservices.processors;

import lombok.extern.log4j.Log4j2;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.cxf.message.MessageContentsList;
import se.skltp.aggregatingservices.data.TestDataGenerator;

@Log4j2
public class ProducerResponseProcessor implements Processor {

  TestDataGenerator producerData;

  public ProducerResponseProcessor(TestDataGenerator producerData){
    this.producerData = producerData;
  }

  @Override
  public void process(Exchange exchange) throws Exception {
    MessageContentsList messageContentsList = exchange.getIn().getBody(MessageContentsList.class);
    Object response = producerData.processRequest(messageContentsList);
    exchange.getIn().setBody(response);
  }
}
