package se.skltp.agp.log;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;
import org.soitoolkit.commons.mule.jaxb.JaxbUtil;

import se.skltp.agp.riv.interoperability.headers.v1.ProcessingStatusType;

public class ProcessingStatusLogFormatTest {

	@Test
	public void testProcessingStatus_no_failures() {
		JaxbUtil jaxbUtil = new JaxbUtil(ProcessingStatusType.class);
		ProcessingStatusType pst = (ProcessingStatusType) jaxbUtil
				.unmarshal(new File(
						"src/test/resources/testfiles/processingstatus_no_failures.xml"));
		ProcessingStatusLogFormat pslf = new ProcessingStatusLogFormat(pst);
		assertEquals(2, pslf.getProcStatusCountTot());
		assertEquals(0, pslf.getProcStatusCountFail());
		// System.out.println(pslf.getProcStatus());
		assertNoNewLinesInString(pslf);
		String ps = pslf.getProcStatus();
		assertTrue(ps.contains("SE2321000164-101B"));
		assertTrue(ps.contains("SE2321000164-1004"));
		assertTrue(ps.contains("DataFromSource"));
	}

	@Test
	public void testProcessingStatus_with_failures() {
		JaxbUtil jaxbUtil = new JaxbUtil(ProcessingStatusType.class);
		ProcessingStatusType pst = (ProcessingStatusType) jaxbUtil
				.unmarshal(new File(
						"src/test/resources/testfiles/processingstatus_with_failures.xml"));
		ProcessingStatusLogFormat pslf = new ProcessingStatusLogFormat(pst);
		assertEquals(3, pslf.getProcStatusCountTot());
		assertEquals(3, pslf.getProcStatusCountFail());
		// System.out.println(pslf.getProcStatus());
		String ps = pslf.getProcStatus();

		assertTrue(ps.contains("HSATEST2-CRS"));
		assertTrue(ps.contains("NoDataSynchFailed"));
		assertTrue(ps.contains("virtualization_platform"));
		assertTrue(ps.contains("43000"));
		assertTrue(ps.contains("VP007 Authorization missing"));

		assertTrue(ps.contains("TEST-HSAID-99"));
		assertTrue(ps.contains("VP004 No Logical Adress found for"));

		assertTrue(ps.contains("SE2321000016-A2BF"));
		assertTrue(ps.contains("VP006 More than one Logical Adress"));
	}

	private void assertNoNewLinesInString(ProcessingStatusLogFormat pslf) {
		assertFalse(
				"don't let the string contain newlines, would cause logstash parsing problems",
				pslf.getProcStatus().contains("\\n")
						|| pslf.getProcStatus().contains("\r"));
	}

}
