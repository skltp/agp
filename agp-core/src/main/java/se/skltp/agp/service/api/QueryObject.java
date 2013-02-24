package se.skltp.agp.service.api;

import se.riv.itintegration.engagementindex.findcontentresponder.v1.FindContentType;

public class QueryObject {

	private FindContentType findContent = null;
	private Object extraArg = null;

	public QueryObject(FindContentType findContent, Object extraArg) {
		this.findContent = findContent;
		this.extraArg = extraArg;
	}

	public FindContentType getFindContent() {
		return findContent;
	}

	public Object getExtraArg() {
		return extraArg;
	}	
}
