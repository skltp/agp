package se.skltp.agp.cache;

import org.mule.api.context.notification.MuleContextNotificationListener;
import org.mule.context.notification.MuleContextNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.skl.tp.hsa.cache.HsaCache;
import se.skltp.takcache.TakCache;

import java.util.Arrays;

public class MuleStartupNotificationHandler implements
        MuleContextNotificationListener<MuleContextNotification> {
    private static final Logger logger = LoggerFactory.getLogger(ResetHsaCache.class);

    private TakCache takCache;
    private String tjanstekontrakt;

    private HsaCache hsaCache;
    private String[] hsaFiles;

    @Override
    public void onNotification(MuleContextNotification notification) {
        if (notification.getType().equalsIgnoreCase(MuleContextNotification.TYPE_INFO)
                && notification.getAction() == MuleContextNotification.CONTEXT_STARTED) {

            logger.info("Initiates hsaCache with files: {}", Arrays.toString(hsaFiles));
            hsaCache.init(hsaFiles);

            logger.info("Initiates takCache");
            takCache.refresh(tjanstekontrakt);

            logger.info("Mule started, vagvalAgent and hsaCache successfully initiated");
        }
    }
}
