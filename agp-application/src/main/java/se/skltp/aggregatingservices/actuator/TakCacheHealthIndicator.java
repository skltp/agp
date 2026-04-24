/*
 * Copyright © 2014-2026 Inera.
 * Copyright owner URL: https://www.inera.se/
 * SKLTP overview page: https://inera.atlassian.net/wiki/spaces/SKLTP/overview
 * This library is free software under the GNU Lesser General Public License v3.0.
 * Please refer to the full license files at the project root.
 */
package se.skltp.aggregatingservices.actuator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Component;
import se.skltp.aggregatingservices.service.TakCacheService;
import se.skltp.takcache.TakCacheLog;

@Component
public class TakCacheHealthIndicator implements HealthIndicator {

  TakCacheService takCacheService;

  @Autowired
  public TakCacheHealthIndicator(TakCacheService takCacheService) {
    this.takCacheService = takCacheService;
  }

  @Override
  public Health health() {
    if (!takCacheService.isInitalized()) {
      return Health.down().withDetail("refreshStatus", getTakRefreshStatus()).build();
    }
    return Health.up().build();
  }

  private String getTakRefreshStatus() {
    TakCacheLog takCacheLog = takCacheService.getLastRefreshLog();
    return takCacheLog == null ? "Not initialized" : takCacheLog.getRefreshStatus().toString();
  }
}
