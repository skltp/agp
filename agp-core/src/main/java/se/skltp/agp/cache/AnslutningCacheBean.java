package se.skltp.agp.cache;

import se.skl.tp.DefaultRoutingConfiguration;
import se.skl.tp.behorighet.BehorighetHandler;
import se.skl.tp.behorighet.BehorighetHandlerImpl;
import se.skl.tp.hsa.cache.HsaCache;
import se.skltp.tak.vagvalsinfo.wsdl.v2.AnropsBehorighetsInfoType;
import se.skltp.takcache.TakCache;
import se.skltp.takcache.util.XmlGregorianCalendarUtil;

import java.util.List;
import java.util.stream.Collectors;

public class AnslutningCacheBean {
    private String agpHsaId;
    private String servicecontractNamespace;

    private BehorighetHandler behorighetHandler;
    private TakCache takCache;

    public AnslutningCacheBean(HsaCache hsaCache, TakCache takCache, DefaultRoutingConfiguration defaultRoutingConfiguration) {
        this.behorighetHandler = new BehorighetHandlerImpl(hsaCache, takCache, defaultRoutingConfiguration);
        this.takCache = takCache;
    }

    public String getAgpHsaId() {
        return agpHsaId;
    }

    public void setAgpHsaId(String agpHsaId) {
        this.agpHsaId = agpHsaId;
    }

    public String getServicecontractNamespace() {
        return servicecontractNamespace;
    }

    public void setServicecontractNamespace(String servicecontractNamespace) {
        this.servicecontractNamespace = servicecontractNamespace;
    }

    public List<String> getReceivers(String senderId, String originalServiceConsumerId) {
        List<AnropsBehorighetsInfoType> anropsBehorigheter = takCache.getAnropsBehorighetsInfos();
        return anropsBehorigheter.stream().
                filter(anropsBehorighet -> anropsBehorighet.getTjansteKontrakt().equals(servicecontractNamespace)).
                filter(anropsBehorighet -> senderId.equals(anropsBehorighet.getSenderId()) || originalServiceConsumerId.equals(anropsBehorighet.getSenderId())).
                filter(anropsBehorighet -> !anropsBehorighet.getReceiverId().equals(HsaCache.DEFAUL_ROOTNODE)).
                filter(anropsBehorighet -> !anropsBehorighet.getReceiverId().equals(agpHsaId)).
                filter(anropsBehorighet -> !anropsBehorighet.getReceiverId().equals(BehorighetHandlerImpl.DEFAULT_RECEIVER_ADDRESS)).
                filter(anropsBehorighet -> !XmlGregorianCalendarUtil.isTimeWithinInterval(XmlGregorianCalendarUtil.getNowAsXMLGregorianCalendar(),
                        anropsBehorighet.getFromTidpunkt(), anropsBehorighet.getTomTidpunkt())).
                map(AnropsBehorighetsInfoType::getReceiverId).collect(Collectors.toList());
    }

    public boolean isAuthorizedConsumer(String senderId, String originalServiceConsumerId, String consumerId) {
        return behorighetHandler.isAuthorized(senderId, servicecontractNamespace, consumerId) ||
                behorighetHandler.isAuthorized(originalServiceConsumerId, servicecontractNamespace, consumerId);
    }
}