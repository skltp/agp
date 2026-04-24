/*
 * Copyright © 2014-2026 Inera.
 * Copyright owner URL: https://www.inera.se/
 * SKLTP overview page: https://inera.atlassian.net/wiki/spaces/SKLTP/overview
 * This library is free software under the GNU Lesser General Public License v3.0.
 * Please refer to the full license files at the project root.
 */
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
