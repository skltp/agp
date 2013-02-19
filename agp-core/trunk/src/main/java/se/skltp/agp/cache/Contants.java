package se.skltp.agp.cache;

import org.soitoolkit.commons.mule.util.RecursiveResourceBundle;

public class Contants {

	private static final RecursiveResourceBundle rb = new RecursiveResourceBundle("GetAggregatedSubjectOfCareSchedule-config", "GetAggregatedSubjectOfCareSchedule-config-override");

	public static final String SERVICE_DOMAIN_SCHEDULING = "riv:crm:scheduling";
	public static final String CATEGORIZATION_BOOKING = "Booking";

	public static final String ENGAGEMANGSINDEX_HSA_ID = rb.getString("ENGAGEMANGSINDEX_HSA_ID");

}