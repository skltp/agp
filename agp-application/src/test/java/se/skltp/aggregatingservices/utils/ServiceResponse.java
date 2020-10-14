package se.skltp.aggregatingservices.utils;

import java.util.Map;
import lombok.Data;
import org.apache.cxf.binding.soap.SoapFault;
import se.skltp.agp.riv.interoperability.headers.v1.ProcessingStatusType;

@Data
public class ServiceResponse <T> {
  T object;
  SoapFault soapFault;
  ProcessingStatusType processingStatus;
  Map<String,Object> headers;
  int responseCode;
}
