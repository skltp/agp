package se.skltp.agp.cache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import se.skltp.agp.riv.vagvalsinfo.v2.VirtualiseringsInfoType;

/**
 *
 */
public class TakCacheBeanTest {

    private final static String TEST_LOCAL_TEST_FILE = "testFile.testCache";
    private final static String TEST_NAMESPACE = "urn:riv:test:namespace:2:rivtabp21";

    private Path testPath;

    @Before
    public void init() {
        testPath = FileSystems.getDefault().getPath(TEST_LOCAL_TEST_FILE);
    }

    @Test
    public void testWriteAndLoadLocalCacheSuccess() throws IOException, ClassNotFoundException {
        final TakCacheBean testObject = new TakCacheBean();
        testObject.setTakLocalCacheFile(TEST_LOCAL_TEST_FILE);
        
        Properties prop = new Properties();
        prop.put("TEST1", "TEST1,TEST2");
        prop.put("TEST2", "TEST2,TEST3");
        prop.put("TEST3", "TEST3,TEST5");
        
        testObject.writeTakLocalCache(prop);
        
        assertTrue(Files.exists(testPath));
        testObject.loadTakLocalCache();
        assertTrue(testObject.getAuthorizedConsumers("TEST3").contains("TEST5"));
        
        Properties cacheFileContent = new Properties();
        try (FileInputStream fin = new FileInputStream(testPath.toString())) {
		    try (InputStreamReader ois = new InputStreamReader(fin, Charset.forName("UTF-8"))) {
		    	cacheFileContent.load(ois);
		    }
		}   
        assertFalse(cacheFileContent.isEmpty());
        Enumeration enumeration = cacheFileContent.keys();
        
        while (enumeration.hasMoreElements()) {
            assertTrue(testObject.contains(enumeration.nextElement().toString()));
        }
    }

    @Test
    public void testPopulateCacheSuccess() throws Exception {
        final TakCacheBean testObject = new TakCacheBean();
        testObject.setTakLocalCacheFile(TEST_LOCAL_TEST_FILE);
        testObject.setTargetNamespace(TEST_NAMESPACE);

        final VirtualiseringsInfoType firstCorrect = new VirtualiseringsInfoType();
        final VirtualiseringsInfoType firstWrong = new VirtualiseringsInfoType();
        final VirtualiseringsInfoType secondWrong = new VirtualiseringsInfoType();
        final VirtualiseringsInfoType secondCorrect = new VirtualiseringsInfoType();

        firstCorrect.setReceiverId("correct1");
        firstCorrect.setTjansteKontrakt(TEST_NAMESPACE);

        secondCorrect.setReceiverId("correct2");
        secondCorrect.setTjansteKontrakt(TEST_NAMESPACE);

        firstWrong.setReceiverId("wrong");
        firstWrong.setTjansteKontrakt(UUID.randomUUID().toString());

        secondWrong.setReceiverId("wrong");
        secondWrong.setTjansteKontrakt(UUID.randomUUID().toString());

        List<VirtualiseringsInfoType> list = new ArrayList<VirtualiseringsInfoType>();
        list.add(firstCorrect);
        list.add(secondWrong);
        list.add(firstWrong);
        list.add(secondCorrect);
        
        Properties prop = new Properties();
        testObject.populateVirtualiseringsInfoCache(prop, list);

        assertEquals(2, testObject.receiverIds().size());
        assertTrue(testObject.receiverIds().contains(firstCorrect.getReceiverId()));
        assertTrue(testObject.receiverIds().contains(secondCorrect.getReceiverId()));
        assertFalse(testObject.receiverIds().contains(firstWrong.getReceiverId()));

        list.remove(0);
        prop.clear();
        testObject.populateVirtualiseringsInfoCache(prop, list);
        assertEquals(1, testObject.receiverIds().size());
        assertTrue(testObject.receiverIds().contains(secondCorrect.getReceiverId()));
        assertFalse(testObject.receiverIds().contains(firstWrong.getReceiverId()));
    }

    @Test
    public void testPopulateFromLocalCacheFileOnException() throws Exception {
        final TakCacheBean testObject = new TakCacheBean();
        testObject.setTakEndpoint("failHard");
        testObject.setTakLocalCacheFile(TEST_LOCAL_TEST_FILE);
        testObject.setTargetNamespace(TEST_NAMESPACE);

        // First populate localcache so that we have something to fallback on.
        Properties prop = new Properties();
        prop.put("one", "TEST1,TEST2");
        prop.put("two", "TEST2,TEST3");
        prop.put("three", "TEST3,TEST5");
        
        testObject.writeTakLocalCache(prop);

        assertTrue(testObject.receiverIds().isEmpty());

        testObject.updateCache();
        assertEquals(3, testObject.receiverIds().size());
        
        Enumeration items = prop.keys(); 
        while (items.hasMoreElements()) {
        	assertTrue(testObject.contains(items.nextElement().toString()));
        }
    }

    @After
    public void cleanUpAfterTest() throws IOException {
        Files.deleteIfExists(testPath);
    }

}
