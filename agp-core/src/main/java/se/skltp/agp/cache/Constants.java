package se.skltp.agp.cache;

import org.soitoolkit.commons.mule.util.RecursiveResourceBundle;

public class Constants {

	// FIXME: Must be injected to avoid hardcoded property filenames
	private static final RecursiveResourceBundle rb = new RecursiveResourceBundle("GetAggregatedSubjectOfCareSchedule-config", "GetAggregatedSubjectOfCareSchedule-config-override");

	public static final String ENGAGEMANGSINDEX_HSA_ID = rb.getString("ENGAGEMANGSINDEX_HSA_ID");

}