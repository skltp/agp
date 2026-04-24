/*
 * Copyright © 2014-2026 Inera.
 * Copyright owner URL: https://www.inera.se/
 * SKLTP overview page: https://inera.atlassian.net/wiki/spaces/SKLTP/overview
 * This library is free software under the GNU Lesser General Public License v3.0.
 * Please refer to the full license files at the project root.
 */
package se.skltp.aggregatingservices.processors;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import se.skltp.aggregatingservices.service.TakCacheService;
import se.skltp.takcache.TakCacheLog;

@Component
public class ResetTakCacheProcessor implements Processor {

    private final TakCacheService takService;

    @Autowired
    public ResetTakCacheProcessor(TakCacheService takService) {
        this.takService = takService;
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        TakCacheLog result = takService.refresh();
        exchange.getMessage().setBody(getResultAsString(result));
        exchange.getMessage().setHeader("Content-Type", "text/html;");
        exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, 200);
    }

    private String getResultAsString(TakCacheLog result) {
        StringBuilder resultAsString = new StringBuilder();
        for (String processingLog : result.getLog()) {
            resultAsString.append("<br>").append(processingLog);
        }
        return resultAsString.toString();
    }
}
