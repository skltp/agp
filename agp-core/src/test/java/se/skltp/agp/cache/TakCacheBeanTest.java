package se.skltp.agp.cache;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;

import com.arjuna.ats.internal.jdbc.drivers.modifiers.list;

import se.skltp.agp.riv.vagvalsinfo.v2.SokVagvalsInfoInterface;
import se.skltp.agp.riv.vagvalsinfo.v2.VirtualiseringsInfoType;
/**
 * Need to add mock for TAK
 * @author torbjorncla
 *
 */
public class TakCacheBeanTest {
	
	private static final long TEST_TIMEOUT = 4000;
	private static final String TEST_ENDPOINT = "http://33.33.33.33:8080/tp-vagval-admin-services/SokVagvalsInfo/v2";
	private static final String TEST_TNS = "urn:riv:clinicalprocess:healthcond:actoutcome:GetLaboratoryOrderOutcome:3:rivtabp21";
	
	private Set<String> list1 = new HashSet<String>();
	private Set<String> list2 = new HashSet<String>();
	private Set<String> list3 = new HashSet<String>();
	
	private static final ConcurrentHashMap<String, Boolean> cache = new ConcurrentHashMap<String, Boolean>();
	
	@Before
	public void init() {
		list1.add("Kalle");
		list1.add("Nisse");
		list1.add("Lasse");
		list1.add("Test");
		list1.add("Hans");
		list1.add("Olle");
		
		list2.add("Kalle");
		list2.add("Nisse");
		list2.add("Lasse");
		list2.add("Hans");
		list2.add("Olle");
		
		list3.add("Woot1");
		list3.add("Woot2");
		list3.add("Woot3");
		list3.add("Woot4");
	}
	
	
	public void testCreateClientSuccess() {
		updateCache(list1);
		print(cache.keySet());
		updateCache(list2);
		print(cache.keySet());
	}
	
	public void updateCache(final Set<String> webMock) {
		final Set<String> current = new HashSet<String>();
		for(String id : webMock) {			
			current.add(id);
			cache.putIfAbsent(id, false);
		}
		/**
		for(final String cachedReciver : cache.keySet()) {
			if(!current.contains(cachedReciver)) {
				cache.remove(cachedReciver);
			}
		}
		**/
		final Iterator<String> it = cache.keySet().iterator();
		while(it.hasNext()) {
			final String value = it.next();
			if(!current.contains(value)) {
				cache.remove(value);
			}
		}
	}
	
	private void print(Set<String> list) {
		for(String s : list) {
			System.out.println("Value: " + s);
		}
		System.out.println("\n");
	}
	
	@Test
	public void testWriteTakCacheFile() throws IOException {
		final TakCacheBean testObject = new TakCacheBean();
		testObject.setTakLocalCacheFile("testFile.testCache");
		testObject.writeTakLocalCache(list3);
	}
	
	@Test
	public void testUpdateCache() {
	}
	
	@Test
	public void testGetReciverIds() {
	}
}
