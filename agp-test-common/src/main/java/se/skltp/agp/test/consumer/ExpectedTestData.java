package se.skltp.agp.test.consumer;

public class ExpectedTestData {

	private String expectedBusinessObjectId;
	private String expectedLogicalAddress;
	
	public ExpectedTestData(String expectedBusinessObjectId, String expectedLogicalAddress) {
		this.expectedBusinessObjectId = expectedBusinessObjectId;
		this.expectedLogicalAddress = expectedLogicalAddress;
	}

	public String getExpectedBusinessObjectId() {
		return expectedBusinessObjectId;
	}

	public String getExpectedLogicalAddress() {
		return expectedLogicalAddress;
	}
}
