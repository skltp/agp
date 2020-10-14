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
