package se.skltp.agp.cache;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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

import se.skltp.agp.riv.vagvalsinfo.v2.HamtaAllaVirtualiseringarResponseType;
import se.skltp.agp.riv.vagvalsinfo.v2.SokVagvalsInfoInterface;
import se.skltp.agp.riv.vagvalsinfo.v2.VirtualiseringsInfoType;

/**
 * Scope: Singleton
 * 
 * Cache for TAK service hamtaAllaVirtualliseringar for current namespace.
 */
public class TakCacheBean implements MuleContextAware {
    private final static Logger log = LoggerFactory.getLogger(TakCacheBean.class);
    private static final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();

    private final ConcurrentHashMap<String, Boolean> cache;

    private String takEndpoint;
    private String targetNamespace;
    private long serviceTimeout;
    private String takLocalCacheFileName;

    public TakCacheBean() {
        cache = new ConcurrentHashMap<String, Boolean>();
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
            populateCache(vr.getVirtualiseringsInfo());
        } catch (Exception err) {
            if (err instanceof IOException) {
                log.error("Could not write to local tak cache: " + takLocalCacheFileName, err);
            } else {
                try {
                    log.error("Could not get data from TAK at endpoint: " + takEndpoint, err);
                    loadTakLocalCache();
                } catch (IOException io) {
                    log.error("Could not load local tak cache file: " + takLocalCacheFileName, io);
                }
            }
        }
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
    protected synchronized void populateCache(final List<VirtualiseringsInfoType> virtualiseringar) throws IOException {
        final Set<String> current = new HashSet<String>();
        for (final VirtualiseringsInfoType vi : virtualiseringar) {
            if (StringUtils.equalsIgnoreCase(vi.getTjansteKontrakt(), targetNamespace)) {
                if (vi.getReceiverId() != null) {
                    current.add(vi.getReceiverId());
                    cache.putIfAbsent(vi.getReceiverId(), false);
                }
            }
        }
        final Iterator<String> it = cache.keySet().iterator();
        while (it.hasNext()) {
            final String key = it.next();
            if (!current.contains(key)) {
                cache.remove(key);
            }
        }
        log.info("TAK cache updated");
        if (log.isDebugEnabled()) {
            prettyPrintCache(cache.keySet());
        }
        writeTakLocalCache(current);
        log.info("Updated local cache file: " + takLocalCacheFileName);
    }

    /**
     * Writes received elements to locale cache file.
     * 
     * @param receiverIds
     *            , set of values to write to cache file.
     * @throws IOException
     */
    protected synchronized void writeTakLocalCache(final Set<String> receiverIds) throws IOException {
        final Path path = FileSystems.getDefault().getPath(takLocalCacheFileName);
        // create, truncate_existing, write
        Files.write(path, receiverIds, Charset.forName("UTF-8"));
    }

    /**
     * Populates current cache with elements from local cache file.
     * 
     * @throws IOException
     */
    protected synchronized void loadTakLocalCache() throws IOException {

        Path cacheFile = FileSystems.getDefault().getPath(takLocalCacheFileName);
        if (Files.notExists(cacheFile)) {
            Files.createFile(cacheFile);
        }

        final List<String> localCache = Files.readAllLines(FileSystems.getDefault().getPath(takLocalCacheFileName), Charset.forName("UTF-8"));
        for (String cached : localCache) {
            cache.putIfAbsent(cached, false);
        }
        final Iterator<String> it = cache.keySet().iterator();
        while (it.hasNext()) {
            final String key = it.next();
            if (!localCache.contains(key)) {
                cache.remove(key);
            }
        }
        log.info("Local cache loaded");
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
     * Returns boolean representation for the existences of receiverId in cache.
     * 
     * @param reciverId to lookup.
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

    /**
     * updateCache can't be executed before mule context has been loaded.
     */
    @Override
    public void setMuleContext(MuleContext context) {
        log.info("MuleContext is ready - will populate cache from tak after delay");
        worker.schedule(new Runnable() {
            public void run() {
                log.info("Delay expired - updating cache now from tak");
                updateCache();
            }
        }, 20, TimeUnit.SECONDS);
    }
}
