package se.skltp.agp.cache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
    public void testWriteAndLoadLocalCacheSuccess() throws IOException {
        final TakCacheBean testObject = new TakCacheBean();
        testObject.setTakLocalCacheFile(TEST_LOCAL_TEST_FILE);

        final Set<String> testRecivers = new HashSet<String>();
        testRecivers.add("TEST1");
        testRecivers.add("TEST2");
        testRecivers.add("TEST3");

        testObject.writeTakLocalCache(testRecivers);
        assertTrue(Files.exists(testPath));
        List<String> cacheFileContent = Files.readAllLines(testPath, Charset.forName("UTF-8"));
        assertEquals(testRecivers.size(), cacheFileContent.size());
        for (String s : cacheFileContent) {
            assertTrue(testRecivers.contains(s));
        }

        testObject.loadTakLocalCache();
        assertEquals(cacheFileContent.size(), testObject.receiverIds().size());
        for (String s : cacheFileContent) {
            assertTrue(testObject.contains(s));
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

        testObject.populateCache(list);

        assertEquals(2, testObject.receiverIds().size());
        assertTrue(testObject.receiverIds().contains(firstCorrect.getReceiverId()));
        assertTrue(testObject.receiverIds().contains(secondCorrect.getReceiverId()));
        assertFalse(testObject.receiverIds().contains(firstWrong.getReceiverId()));

        list.remove(0);
        testObject.populateCache(list);
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
        Set<String> fallback = new HashSet<String>();
        fallback.add("one");
        fallback.add("two");
        fallback.add("three");
        testObject.writeTakLocalCache(fallback);

        assertTrue(testObject.receiverIds().isEmpty());

        testObject.updateCache();
        assertEquals(3, testObject.receiverIds().size());
        for (String s : fallback) {
            assertTrue(testObject.contains(s));
        }
    }

    @After
    public void cleanUpAfterTest() throws IOException {
        Files.deleteIfExists(testPath);
    }

}
