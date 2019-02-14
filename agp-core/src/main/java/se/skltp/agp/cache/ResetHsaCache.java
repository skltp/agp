package se.skltp.agp.cache;

import org.mule.api.MuleEventContext;
import org.mule.api.lifecycle.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.skl.tp.hsa.cache.HsaCache;
import se.skl.tp.hsa.cache.HsaCacheInitializationException;

import java.util.Arrays;

public class ResetHsaCache implements Callable {
    private static final Logger logger = LoggerFactory.getLogger(ResetHsaCache.class);

    private HsaCache hsaCache;
    private String[] hsaFiles;

    public void setHsaCache(HsaCache hsaCache) {
        this.hsaCache = hsaCache;
    }

    public void setHsaFiles(String... hsaFiles) {
        this.hsaFiles = hsaFiles;
    }

    public Object onCall(final MuleEventContext eventContext){
        final String content = resetCache();
        eventContext.setStopFurtherProcessing(true);
        return content;
    }

    private String resetCache() {
        try {
            logger.info("Start a reset of HSA cache using files: {} ...", Arrays.toString(hsaFiles));
            int oldCacheSize = hsaCache.getHSACacheSize();
            HsaCache cache = hsaCache.init(hsaFiles);
            int cacheSize = cache.getHSACacheSize();
            // Guarding against having a default HSA-ID.
            if (cacheSize > 1) {
                String message = "Successfully reset HSA cache using files: " + Arrays.toString(hsaFiles) +
                        "\nHSA cache size was: " + oldCacheSize +
                        "\nHSA cache now is: " + cacheSize;
                logger.info(message);
                return message;
            } else {
                String message = "Warning: HSA cache reset to" + cacheSize +
                        ". Was " + oldCacheSize + "entries!.\nUsing files: " +
                        Arrays.toString(hsaFiles);
                logger.warn(message);
                return message;
            }
        } catch (HsaCacheInitializationException e) {
            logger.error("Reset HSA cache failed", e);
            return "Reset HSA cache failed using files: " + Arrays.toString(hsaFiles);
        }
    }
}
