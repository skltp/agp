package se.skltp.aggregatingservices.tests;

import java.util.List;
import org.apache.cxf.message.MessageContentsList;

public class TestDataUtil {

  private TestDataUtil() {
    // Static utility class
  }

  public static MessageContentsList createResponse(Object object) {
    MessageContentsList requestList = new MessageContentsList();
    requestList.add(object);
    return requestList;
  }

  public static MessageContentsList createRequest(String sourceSystem, Object objects) {
    MessageContentsList requestList = new MessageContentsList();
    requestList.add(sourceSystem);
    if(objects instanceof List){
        for(Object object : (List)objects){
          requestList.add(object);
        }
    } else {
      requestList.add(objects);
    }
    return requestList;
  }
}
