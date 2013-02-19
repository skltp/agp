package se.skltp.agp.service.api;

import java.util.List;

public interface ResponseListFactory {
	public String getXmlFromAggregatedResponse(List<Object> aggregatedResponseList);
}