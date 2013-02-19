package se.skltp.agp.service.api;

public class QueryObject {
	private String registeredResidentIdentification = "";
	private String serviceDomain = "";

	public QueryObject() {
	}

	public QueryObject(String registeredResidentIdentification, String serviceDomain) {
		this.registeredResidentIdentification = registeredResidentIdentification;
		this.serviceDomain = serviceDomain;
	}

	public String getRegisteredResidentIdentification() {
		return registeredResidentIdentification;
	}

	public void setRegisteredResidentIdentification(String registeredResidentIdentification) {
		this.registeredResidentIdentification = registeredResidentIdentification;
	}

	public String getServiceDomain() {
		return serviceDomain;
	}

	public void setServiceDomain(String serviceDomain) {
		this.serviceDomain = serviceDomain;
	}

}
