package se.skltp.agp.service.api;

import org.w3c.dom.Node;

public interface QueryObjectFactory {
	public QueryObject createQueryObject(Node node);
}