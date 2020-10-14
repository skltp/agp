package se.skltp.aggregatingservices.utils;

import static se.skltp.aggregatingservices.data.TestDataDefines.SAMPLE_SENDER_ID;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import se.skltp.agp.riv.interoperability.headers.v1.StatusCodeEnum;

public class ExpectedResponse {

  private Map<String, Object[]> map = new HashMap<>();

  int numResponses = 0;
  int numProducerCallsOk = 0;
  int numProducerCallsFailed = 0;
  int responseCode;

  String logSenderIdEI = "ei-sender-id";
  String logSenderId = SAMPLE_SENDER_ID;
  String logOriginalSenderId = SAMPLE_SENDER_ID;
  String logCorrelationId = "test-corr-id";


  public ExpectedResponse() {
    this(200);
  }

  public ExpectedResponse(int responseCode) {
    this.responseCode = responseCode;
  }

  public void add(String producer, int responseSize, StatusCodeEnum statusCode, String errTxtPart) {
    add(producer, responseSize, statusCode, errTxtPart, 200);
  }

  public void add(String producer, int responseSize, StatusCodeEnum statusCode, String errTxtPart, int responseCode) {
    map.put(producer, new Object[]{statusCode, responseSize, errTxtPart, responseCode});
    numResponses += responseSize;
    if (statusCode == StatusCodeEnum.DATA_FROM_SOURCE || statusCode == StatusCodeEnum.DATA_FROM_CACHE) {
      numProducerCallsOk++;
    } else {
      numProducerCallsFailed++;
    }
  }

  public Collection<String> getProducers() {
    return map.keySet();
  }

  public StatusCodeEnum getStatusCode(String producer) {
    return (StatusCodeEnum) map.get(producer)[0];
  }

  public String getErrTxtPart(String producer) {
    return (String) map.get(producer)[2];
  }

  public int getResponseCode(String producer) {
    return (int) map.get(producer)[3];
  }

  public int numProducers() {
    return map.size();
  }

  public int numResponses() {
    return numResponses;
  }

  public int numResponses(String producer) {
    return (int) map.get(producer)[1];
  }

  public int getNumProducerCallsOk() {
    return numProducerCallsOk;
  }

  public int getNumProducerCallsFailed() {
    return numProducerCallsFailed;
  }

  public int getResponseCode() {
    return responseCode;
  }

  public boolean contains(String producer) {
    return map.containsKey(producer);
  }

  public String getLogSenderIdEI() {
    return logSenderIdEI;
  }

  public void setLogSenderIdEI(String logSenderIdEI) {
    this.logSenderIdEI = logSenderIdEI;
  }

  public String getLogSenderId() {
    return logSenderId;
  }

  public void setLogSenderId(String logSenderId) {
    this.logSenderId = logSenderId;
  }

  public String getLogOriginalSenderId() {
    return logOriginalSenderId;
  }

  public void setLogOriginalSenderId(String logOriginalSenderId) {
    this.logOriginalSenderId = logOriginalSenderId;
  }

  public String getLogCorrelationId() {
    return logCorrelationId;
  }

  public void setLogCorrelationId(String logCorrelationId) {
    this.logCorrelationId = logCorrelationId;
  }
}
