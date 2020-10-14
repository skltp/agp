package se.skltp.aggregatingservices.service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.skl.tp.behorighet.BehorighetHandler;
import se.skl.tp.behorighet.BehorighetHandlerImpl;
import se.skl.tp.vagval.VagvalHandler;
import se.skl.tp.vagval.VagvalHandlerImpl;
import se.skltp.aggregatingservices.configuration.AgpServiceConfiguration;
import se.skltp.takcache.TakCache;
import se.skltp.takcache.TakCacheLog;
import se.skltp.takcache.TakCacheLog.RefreshStatus;

@Service
public class TakCacheServiceImpl implements TakCacheService {

  TakCache takCache;

  BehorighetHandler behorighetHandler;

  VagvalHandler vagvalHandler;

  TakCacheLog takCacheLog = null;

  Date lastResetDate;

  List<String> takContracts;

  List<AgpServiceConfiguration> serviceConfigurationList;


  @Autowired
  public TakCacheServiceImpl(TakCache takCache, List<AgpServiceConfiguration> serviceConfigurationList) {
    this.serviceConfigurationList = serviceConfigurationList;
    this.takCache = takCache;
    initHandlers();
    resetTakContracts();
  }

  @Override
  public void setTakContracts(List<String> takContracts) {
    this.takContracts = takContracts;
  }

  @Override
  public void resetTakContracts() {
    this.takContracts = serviceConfigurationList.stream().map(AgpServiceConfiguration::getTakContract).collect(Collectors.toList());
  }

  @Override
  public TakCacheLog refresh() {

    takCacheLog = takCache.refresh(takContracts);

    lastResetDate = new Date();
    return takCacheLog;
  }

  @Override
  public boolean isInitalized() {
    return takCacheLog != null && takCacheLog.getRefreshStatus() != RefreshStatus.REFRESH_FAILED;
  }

  @Override
  public boolean isAuthorized(String senderId, String servicecontractNamespace, String receiverId) {
    // it has to be authority and a guilty vagval
    return behorighetHandler.isAuthorized(senderId, servicecontractNamespace, receiverId) && !vagvalHandler.getRoutingInfo(servicecontractNamespace, receiverId).isEmpty();
  }

  @Override
  public Date getLastResetDate() {
    return lastResetDate;
  }

  @Override
  public TakCacheLog getLastRefreshLog() {
    return takCacheLog;
  }

  @Override
  public boolean isAuthorizedConsumer(Authority authority) {
    return isAuthorized(authority.getSenderId(),authority.getServicecontractNamespace(), authority.receiverId ) ||
        isAuthorized(authority.getOriginalSenderId(), authority.getServicecontractNamespace(), authority.receiverId);
  }

  public void initHandlers(){
    this.behorighetHandler = new BehorighetHandlerImpl(takCache.getBehorigeterCache());
    this.vagvalHandler = new VagvalHandlerImpl(takCache.getVagvalCache());
  }


}
