package se.skltp.agp.service.api;

import java.util.List;

import se.riv.itintegration.engagementindex.findcontentresponder.v1.FindContentResponseType;

public interface RequestListFactory {
	public List<Object[]> createRequestList(QueryObject qo, FindContentResponseType src);
}
