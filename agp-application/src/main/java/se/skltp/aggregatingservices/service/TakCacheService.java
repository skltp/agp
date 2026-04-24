/*
 * Copyright © 2014-2026 Inera.
 * Copyright owner URL: https://www.inera.se/
 * SKLTP overview page: https://inera.atlassian.net/wiki/spaces/SKLTP/overview
 * This library is free software under the GNU Lesser General Public License v3.0.
 * Please refer to the full license files at the project root.
 */
package se.skltp.aggregatingservices.service;

import java.util.Date;
import java.util.List;
import se.skltp.takcache.TakCacheLog;

public interface TakCacheService {

  void setTakContracts(List<String> takContracts);

  void resetTakContracts();

  TakCacheLog refresh();

  boolean isInitalized();

  boolean isAuthorized(String senderId, String servicecontractNamespace, String receiverId);

  Date getLastResetDate();

  TakCacheLog getLastRefreshLog();

  boolean isAuthorizedConsumer(Authority authority);
}
