/*
 * Copyright © 2014-2026 Inera.
 * Copyright owner URL: https://www.inera.se/
 * SKLTP overview page: https://inera.atlassian.net/wiki/spaces/SKLTP/overview
 * This library is free software under the GNU Lesser General Public License v3.0.
 * Please refer to the full license files at the project root.
 */
package se.skltp.aggregatingservices.utils;

public interface ServiceMarshaller {

  String marshal(Object jaxbObject);

  @SuppressWarnings("rawtypes")
  Object unmarshal(Object payload);
}
