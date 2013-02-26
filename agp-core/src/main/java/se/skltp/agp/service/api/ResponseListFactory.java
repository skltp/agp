package se.skltp.agp.service.api;

import java.util.List;

public interface ResponseListFactory {
	public String getXmlFromAggregatedResponse(QueryObject queryObject, List<Object> aggregatedResponseList);
}