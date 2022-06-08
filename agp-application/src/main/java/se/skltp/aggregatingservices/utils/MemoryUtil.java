package se.skltp.aggregatingservices.utils;

import com.sun.management.HotSpotDiagnosticMXBean;
import java.lang.management.*;

public class MemoryUtil {
  private static final BufferPoolMXBean directBufferPool = getDirectBufferPool();
  private static final HotSpotDiagnosticMXBean hotSpotDiagnostic = getHotSpotDiagnostic();
  private static final MemoryMXBean mbean = ManagementFactory.getMemoryMXBean();

  // Utility class
  private MemoryUtil() {
  }

  public static String getMemoryUsed() {
    return bytesReadable(directBufferPool.getMemoryUsed());
  }

  public static String getTotalCapacity() {
    return bytesReadable(directBufferPool.getTotalCapacity());
  }

  public static String getVMMaxMemory() {
    return hotSpotDiagnostic.getVMOption("MaxDirectMemorySize").toString();
  }

  public static long getCount() {
    return directBufferPool.getCount();
  }

  public static MemoryUsage getNonHeapMemoryUsage() {
    return mbean.getNonHeapMemoryUsage();
  }

  public static String bytesReadable(long v) {
    if (v < 1024) {
      return v + " B";
    }
    int z = (63 - Long.numberOfLeadingZeros(v)) / 10;
    return String.format("%.1f %sB", (double) v / (1L << (z * 10)), " KMGTPE".charAt(z));
  }

  private static BufferPoolMXBean getDirectBufferPool() {
    for (BufferPoolMXBean pool : ManagementFactory.getPlatformMXBeans(BufferPoolMXBean.class)) {
      if (pool.getName().equals("direct")) {
        return pool;
      }
    }
    throw new RuntimeException("Could not find direct BufferPoolMXBean");
  }

  private static HotSpotDiagnosticMXBean getHotSpotDiagnostic() {
    HotSpotDiagnosticMXBean hsdiag = ManagementFactory.getPlatformMXBean(HotSpotDiagnosticMXBean.class);
    if (hsdiag == null) {
      throw new RuntimeException("Could not find HotSpotDiagnosticMXBean");
    }
    return hsdiag;
  }
}

