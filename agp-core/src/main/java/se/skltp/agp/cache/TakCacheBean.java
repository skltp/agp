package se.skltp.agp.cache;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.xml.soap.SOAPException;

import org.apache.commons.lang.StringUtils;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.mule.api.MuleContext;
import org.mule.api.context.MuleContextAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.skltp.agp.riv.vagvalsinfo.v2.HamtaAllaVirtualiseringarResponseType;
import se.skltp.agp.riv.vagvalsinfo.v2.SokVagvalsInfoInterface;
import se.skltp.agp.riv.vagvalsinfo.v2.VirtualiseringsInfoType;


/**
 * Scope: Singelton
 * 
 * Cache for TAK service hamtaAllaVirtualliseringar for current namespace.
 * 
 * @author torbjorncla
 */
public class TakCacheBean implements MuleContextAware { 
	private final static Logger log = LoggerFactory.getLogger(TakCacheBean.class);
	private static final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();
	
	private final ConcurrentHashMap<String, Boolean> cache;
	
	private String takEndpoint;
	private String targetNamespace;
	private long serviceTimeout;
	private String takLocalCacheFile;

	public TakCacheBean() {
		cache = new ConcurrentHashMap<String, Boolean>();
	}
	
	public String getTakLocalCacheFile() {
		return takLocalCacheFile;
	}

	public void setTakLocalCacheFile(String takLocalCacheFile) {
		this.takLocalCacheFile = takLocalCacheFile;
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
	 * Update cache.
	 * 
	 */
	public synchronized void updateCache() {
		try {
			final SokVagvalsInfoInterface client = createClient();
			final HamtaAllaVirtualiseringarResponseType vr = client.hamtaAllaVirtualiseringar(null);
			populateCache(vr.getVirtualiseringsInfo());			
		} catch (Exception err) {
			if(err instanceof IOException) {
				log.error("Could not write to local tak cache: " + takLocalCacheFile, err);
			} else {
				try {
					log.error("Could not get data from TAK at endpoint: " + takEndpoint, err);
					loadTakLocalCache();
				} catch (IOException io) {
					log.error("Could not load local tak cache file: " + takLocalCacheFile, io);
				}
			}
		}
	}
	
	protected synchronized void populateCache(final List<VirtualiseringsInfoType> virtualiseringar) throws IOException {
		final Set<String> current = new HashSet<String>();
		for(final VirtualiseringsInfoType vi : virtualiseringar) {
			if(StringUtils.equalsIgnoreCase(vi.getTjansteKontrakt(), targetNamespace)) {
				if(vi.getReceiverId() != null) {
					current.add(vi.getReceiverId());
					cache.putIfAbsent(vi.getReceiverId(), false);
				}
			}
		}
		final Iterator<String> it = cache.keySet().iterator();
		while(it.hasNext()) {
			final String key = it.next();
			if(!current.contains(key)) {
				cache.remove(key);
			}
		}
		log.info("TAK Cache updated");
		if(log.isDebugEnabled()) {
			prettyPrintCache(cache.keySet());
		}
		writeTakLocalCache(current);
		log.info("Updated local cache file: " + takLocalCacheFile);
	}
	
	protected synchronized void writeTakLocalCache(final Set<String> reciverIds) throws IOException {
		final Path path = FileSystems.getDefault().getPath(takLocalCacheFile);
		Files.write(path, reciverIds, Charset.forName("UTF-8"));
	}
	
	protected synchronized void loadTakLocalCache() throws IOException {
		final List<String> localCache = 
				Files.readAllLines(FileSystems.getDefault().getPath(takLocalCacheFile), Charset.forName("UTF-8"));
		for(String cached : localCache) {
			cache.putIfAbsent(cached, false);
		}
		final Iterator<String> it = cache.keySet().iterator();
		while(it.hasNext()) {
			final String key = it.next();
			if(!localCache.contains(key)) {
				cache.remove(key);
			}
		}
		log.info("Local cache loaded");
	}
	
	private void prettyPrintCache(final Set<String> cache) {
		for(final String v : cache) {
			log.debug("## Cached value: " + v);
		}
	}
	
	/**
	 * Returns set of matching reciverIds (LogicalAddresses) for current service domain i TAK
	 * @return Set<String> reciverIds.
	 */
	public Set<String> receiverIds() {
		return Collections.unmodifiableSet(cache.keySet());
	}
	
	/**
	 * Helper method to verify if reciverIds exists in cache.
	 * @param reciverId to lookup.
	 * @return true if reciverId exists in cache, else false.
	 */
	public boolean contains(final String reciverId) {
		if(reciverId == null) {
			return false;
		}
		return cache.containsKey(reciverId);			
	}
	
	/**
	 * Create client, with bean properties value.
	 * @return SokVagvalsInfo client.
	 */
	private SokVagvalsInfoInterface createClient() {
		final JaxWsProxyFactoryBean jaxWs = new JaxWsProxyFactoryBean();
		jaxWs.setServiceClass(SokVagvalsInfoInterface.class);
		jaxWs.setAddress(takEndpoint);
		
		final HTTPClientPolicy policy = new HTTPClientPolicy();
        policy.setConnectionTimeout(0);
        policy.setReceiveTimeout(serviceTimeout);
        policy.setAllowChunking(true);

        final Object service = jaxWs.create();
        final Client client = ClientProxy.getClient(service);
        ((HTTPConduit) client.getConduit()).setClient(policy);

        return (SokVagvalsInfoInterface) service;
	}

	@Override
	public void setMuleContext(MuleContext context) {
		log.info("MuleContext is ready, populate TAK-cache");
		worker.schedule(new Runnable() {
			public void run() {
				updateCache();
			}
		}, 10, TimeUnit.SECONDS);
	}
}
