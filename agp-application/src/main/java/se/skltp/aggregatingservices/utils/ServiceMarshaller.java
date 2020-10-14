package se.skltp.aggregatingservices.utils;

public interface ServiceMarshaller {

  String marshal(Object jaxbObject);

  @SuppressWarnings("rawtypes")
  Object unmarshal(Object payload);
}
