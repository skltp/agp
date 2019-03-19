package se.skltp.agp.test.consumer;

import java.util.Objects;

public class TestData implements Comparable<TestData> {

    private String expectedBusinessObjectId;
    private String expectedLogicalAddress;

    public TestData(String expectedBusinessObjectId, String expectedLogicalAddress) {
        this.expectedBusinessObjectId = expectedBusinessObjectId;
        this.expectedLogicalAddress = expectedLogicalAddress;
    }

    public String getExpectedBusinessObjectId() {
        return expectedBusinessObjectId;
    }

    public String getExpectedLogicalAddress() {
        return expectedLogicalAddress;
    }

    @Override
    public int compareTo(TestData o) {
        if (expectedBusinessObjectId.compareTo(o.expectedBusinessObjectId) > 0) {
            return 1;
        } else {
            return expectedLogicalAddress.compareTo(o.expectedLogicalAddress);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        TestData td = (TestData) obj;
        return Objects.equals(expectedBusinessObjectId, td.expectedBusinessObjectId) &&
                Objects.equals(expectedLogicalAddress, td.expectedLogicalAddress);
    }
}
