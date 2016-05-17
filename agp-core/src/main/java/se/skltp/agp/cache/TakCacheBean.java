package se.skltp.agp.cache;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.transport.Conduit;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.mule.api.MuleContext;
import org.mule.api.context.MuleContextAware;
import org.mule.module.cxf.transport.MuleUniversalConduit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.skltp.agp.riv.vagvalsinfo.v2.AnropsBehorighetsInfoType;
import se.skltp.agp.riv.vagvalsinfo.v2.HamtaAllaAnropsBehorigheterResponseType;
import se.skltp.agp.riv.vagvalsinfo.v2.HamtaAllaVirtualiseringarResponseType;
import se.skltp.agp.riv.vagvalsinfo.v2.SokVagvalsInfoInterface;
import se.skltp.agp.riv.vagvalsinfo.v2.VirtualiseringsInfoType;

/**
 * Scope: Singleton
 * 
 * Cache for tak service hamtaAllaVirtualliseringar for current namespace.
 */
public class TakCacheBean implements MuleContextAware {
    private final static Logger log = LoggerFactory.getLogger(TakCacheBean.class);

    private final ConcurrentHashMap<String, AuthorizedConsumers> cache;

    private String takEndpoint;
    private String targetNamespace;
    private long serviceTimeout;
    private String takLocalCacheFileName;

    public TakCacheBean() {
        cache = new ConcurrentHashMap<String, AuthorizedConsumers>();
    }

    public String getTakLocalCacheFile() {
        return takLocalCacheFileName;
    }

    public void setTakLocalCacheFile(String takLocalCacheFile) {
        this.takLocalCacheFileName = takLocalCacheFile;
    }

    public long getServiceTimeout() {
        return serviceTimeout;
    }

    public void setServiceTimeout(long serviceTimeout) {
        this.serviceTimeout = serviceTimeout;
    }

    public String getTargetNamespace() {
        return targetNamespace;
    }

    public void setTargetNamespace(String targetNamespace) {
        this.targetNamespace = targetNamespace;
    }

    public String getTakEndpoint() {
        return takEndpoint;
    }

    public void setTakEndpoint(String takEnpoint) {
        this.takEndpoint = takEnpoint;
    }

    /**
     * Update cache. Main entry point.
     * Synchronises cache with tak (master).
     * Called from Mule-flow (TakCache-service.xml) and setMuleContext().
     */
    public synchronized void updateCache() {
        log.info("updating tak cache");
        try {
            final SokVagvalsInfoInterface client = createClient();
            log.info("about to call hamtaAllaVirtualiseringar");
            final HamtaAllaVirtualiseringarResponseType vr = client.hamtaAllaVirtualiseringar(null);
            log.info("about to call hamtaAllaVirtualiseringar");
            final HamtaAllaAnropsBehorigheterResponseType ab = client.hamtaAllaAnropsBehorigheter(null);
            
            Properties prop = new Properties();
            populateVirtualiseringsInfoCache(prop, vr.getVirtualiseringsInfo());
            populateAnropsbehorighetsInfoCache(prop, ab.getAnropsBehorighetsInfo());
            
            writeTakLocalCache(prop);
            log.info("Updated local cache file: " + takLocalCacheFileName);
            
        } catch (Exception err) {
            if (err instanceof IOException || err instanceof ClassNotFoundException) {
                log.error("Could not write to local tak cache: " + takLocalCacheFileName, err);
            } else {
                try {
                    log.error("Could not get data from TAK at endpoint: " + takEndpoint, err);
                    loadTakLocalCache();
                } catch (IOException | ClassNotFoundException io) {
                    log.error("Could not load local tak cache file: " + takLocalCacheFileName, io);
                }
            }
        }
    }

    /**
     * updateCache can't be executed before mule context has been loaded.
     */
    @Override
    public void setMuleContext(MuleContext context) {
        log.info ("MuleContext is ready - populating cache from tak.");
        log.debug("Note - if using the tak teststub (skltp-box, dev), then mule needs to have started the teststub before starting the aggregating service.");
        log.debug("This is managed in skltp-box by listing the stub before the service in the script file");
        log.debug("In dev environment, it is managed by script file hostenv");
        updateCache();
    }
    
    /**
     * Populates cache with values after applying filter on targetNamespace.
     * Adds new elements to the cache. 
     * Removes elements from cache that have been removed from TAK. 
     * Persists to file.
     * 
     * @param virtualiseringar
     * @throws IOException
     */
    protected synchronized void populateVirtualiseringsInfoCache(final Properties prop, final List<VirtualiseringsInfoType> virtualiseringar) throws IOException {
        for (final VirtualiseringsInfoType vi : virtualiseringar) {
            if (StringUtils.equalsIgnoreCase(vi.getTjansteKontrakt(), targetNamespace)) {
                if (vi.getReceiverId() != null) {
                    prop.put(vi.getReceiverId(), "");
                    cache.putIfAbsent(vi.getReceiverId(), new AuthorizedConsumers());
                }
            }
        }
        final Iterator<String> it = cache.keySet().iterator();
        while (it.hasNext()) {
            final String key = it.next();
            if (!prop.containsKey(key)) {
                cache.remove(key);
            }
        }
        
        if (log.isDebugEnabled()) {
            prettyPrintCache(cache.keySet());
        }
        log.info("tak cache updated with {} values", prop.size());
    }
    
    /**
     * Populates cache with values after applying filter on targetNamespace.
     * Adds new elements to the cache. 
     * Removes elements from cache that have been removed from TAK. 
     * Persists to file.
     * 
     * @param virtualiseringar
     * @throws IOException
     */
    protected synchronized void populateAnropsbehorighetsInfoCache(final Properties prop, final List<AnropsBehorighetsInfoType> anropsBehorighetsInfoTypeList) throws IOException {
        for (final AnropsBehorighetsInfoType ab : anropsBehorighetsInfoTypeList) {
            if (StringUtils.equalsIgnoreCase(ab.getTjansteKontrakt(), targetNamespace)) {
                if (ab.getReceiverId() != null && ab.getSenderId() != null) {
                    AuthorizedConsumers authConsumers = cache.get(ab.getReceiverId());
                    authConsumers.addIfAbsent(ab.getSenderId());
                    prop.put(ab.getReceiverId(), StringUtils.join(authConsumers, ','));
                }
            }
        }
        log.info("tak cache updated with {} anropbehoriget", prop.size());
    }
    
    /**
     * Writes received elements to locale cache file.
     * 
     * @param receiverIds set of values to write to cache file.
     * @throws IOException
     */
    protected synchronized void writeTakLocalCache(final Properties prop) throws IOException {
        final Path path = FileSystems.getDefault().getPath(takLocalCacheFileName);
        try (OutputStreamWriter writer = new OutputStreamWriter(
        	    new FileOutputStream(path.toString()), Charset.forName("UTF-8"))) {
        	prop.store(writer, String.valueOf(System.currentTimeMillis()));
        }
    }

    /**
     * Populates current cache with elements from local cache file.
     * 
     * @throws IOException
     * @throws ClassNotFoundException 
     */
    protected synchronized void loadTakLocalCache() throws IOException, ClassNotFoundException {
    	Properties localCache = new Properties();
        Path cacheFile = FileSystems.getDefault().getPath(takLocalCacheFileName);
        if (Files.notExists(cacheFile)) {
            Files.createFile(cacheFile);
        }
        
        try (FileInputStream fin = new FileInputStream(cacheFile.toString())) {
		    try (InputStreamReader ois = new InputStreamReader(fin, Charset.forName("UTF-8"))) {
		    	localCache.load(ois);
			    
		    	Enumeration<Object> enumeration = localCache.keys();
			    while (enumeration.hasMoreElements()) {
			    	
			    	String receiverId = (String) enumeration.nextElement();
			    	AuthorizedConsumers consumers = cache.get(receiverId);
		            if (consumers != null) {
		            	consumers.update(localCache.get(receiverId));
		            } else {
		            	consumers = new AuthorizedConsumers(localCache.get(receiverId));
		            	cache.put(receiverId, consumers);
		            }
		        }
			    
			    final Iterator<String> it = cache.keySet().iterator();
		        while (it.hasNext()) {
		            final String key = it.next();
		            if (!localCache.containsKey(key)) {
		                cache.remove(key);
		            }
		        }
				ois.close();
		    }
        }
        
        log.info("Local cache loaded");
    }
    
    public AuthorizedConsumers getAuthorizedConsumers(String receiverId) {
    	return cache.get(receiverId);
    }

    /**
     * Convenience method to log elements in cache.
     * 
     * @param cache
     */
    private void prettyPrintCache(final Set<String> cache) {
        for (final String v : cache) {
            log.debug("## Cached value: " + v);
        }
    }

    /**
     * Returns set of matching receiverIds (LogicalAddresses) for current service domain i TAK
     * 
     * @return Set<String> receiverIds.
     */
    public Set<String> receiverIds() {
        return Collections.unmodifiableSet(cache.keySet());
    }

    /**
     * @param receiverId to look up.
     * @return true if receiverId exists in cache, else false.
     */
    public boolean contains(final String receiverId) {
        if (receiverId == null) {
            return false;
        }
        return cache.containsKey(receiverId);
    }

    /**
     * @return soap client for calling tak SokVagvalsInfo
     */
    private SokVagvalsInfoInterface createClient() {
        
        final JaxWsProxyFactoryBean jaxWs = new JaxWsProxyFactoryBean();
        jaxWs.setServiceClass(SokVagvalsInfoInterface.class);
        jaxWs.setAddress(takEndpoint);

        final Object service = jaxWs.create();
        final Client client = ClientProxy.getClient(service);
        
        Conduit conduit = client.getConduit();
        if (conduit instanceof HTTPConduit) {
            final HTTPClientPolicy policy = new HTTPClientPolicy();
            policy.setConnectionTimeout(0);
            policy.setReceiveTimeout(serviceTimeout);
            policy.setAllowChunking(true);
            ((HTTPConduit) client.getConduit()).setClient(policy);
        } else if (conduit instanceof MuleUniversalConduit) {
            // TODO - is this an error?
            // Occurs when running TakCacheBeanIntegrationTest.
            // Does not occur when running in skltp-box.
            // @see https://www.mulesoft.org/jira/browse/MULE-4464
            // Processing still works .. ..
            MuleUniversalConduit muc = (MuleUniversalConduit)conduit;
            log.error("Expected HTTPConduit, but received MuleUniversalConduit, target:{}", muc.getTarget().getAddress().getValue());
        } else {
            log.error("Expected HTTPConduit, but received {}", conduit.getClass().getSimpleName());
        }
        SokVagvalsInfoInterface takClient = (SokVagvalsInfoInterface) service;
        return takClient;
    }
}
