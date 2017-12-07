package se.skltp.agp.service.api;

import java.util.List;

public interface RequestListFactoryExtended extends RequestListFactory {
	public List<Object[]> createRequestList(QueryObject qo, List<String> src);
}