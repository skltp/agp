/*
 * Copyright © 2014-2026 Inera.
 * Copyright owner URL: https://www.inera.se/
 * SKLTP overview page: https://inera.atlassian.net/wiki/spaces/SKLTP/overview
 * This library is free software under the GNU Lesser General Public License v3.0.
 * Please refer to the full license files at the project root.
 */
package se.skltp.aggregatingservices.actuator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import se.skltp.aggregatingservices.service.TakCacheService;
import se.skltp.takcache.TakCacheLog;

import static org.junit.jupiter.api.Assertions.*;

public class TakCacheHealthIndicatorTest {

  @Mock
  TakCacheService takCacheServiceMock;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void testNotInitialized() {
    TakCacheHealthIndicator indicator = new TakCacheHealthIndicator(takCacheServiceMock);
    Health health = indicator.health();
    assertEquals(Status.DOWN, health.getStatus());
  }

  @Test
  void testInitializedOk() {
    TakCacheLog log = new TakCacheLog();
    log.setRefreshStatus(TakCacheLog.RefreshStatus.REFRESH_OK);
    Mockito.when(takCacheServiceMock.isInitalized()).thenReturn(true);
    Mockito.when(takCacheServiceMock.getLastRefreshLog()).thenReturn(log);

    TakCacheHealthIndicator indicator = new TakCacheHealthIndicator(takCacheServiceMock);
    Health health = indicator.health();
    assertEquals(Status.UP, health.getStatus());
  }

  @Test
  void testRefreshFailed() {
    TakCacheLog log = new TakCacheLog();
    log.setRefreshStatus(TakCacheLog.RefreshStatus.REFRESH_FAILED);
    Mockito.when(takCacheServiceMock.isInitalized()).thenReturn(false);
    Mockito.when(takCacheServiceMock.getLastRefreshLog()).thenReturn(log);

    TakCacheHealthIndicator indicator = new TakCacheHealthIndicator(takCacheServiceMock);
    Health health = indicator.health();
    assertEquals(Status.DOWN, health.getStatus());
  }
}