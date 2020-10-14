package se.skltp.aggregatingservices.data;

public class TestDataDefines {
  // Test cases are documented at
  // https://skl-tp.atlassian.net/wiki/pages/viewpage.action?pageId=30015599
  // "Aggregerande tjänst - Funktionella tester"

  // Class containing defines
  private TestDataDefines() {
  }

  public static final String SAMPLE_SENDER_ID               = "sample-sender-id";
  public static final String SAMPLE_ORIGINAL_CONSUMER_HSAID = "sample-original-consumer-hsaid";

  public static final String TEST_LOGICAL_ADDRESS_1 = "HSA-ID-1";
  public static final String TEST_LOGICAL_ADDRESS_2 = "HSA-ID-2";
  public static final String TEST_LOGICAL_ADDRESS_3 = "HSA-ID-3";
  public static final String TEST_LOGICAL_ADDRESS_4 = "HSA-ID-4";
  public static final String TEST_LOGICAL_ADDRESS_5 = "HSA-ID-5";
  public static final String TEST_LOGICAL_ADDRESS_6 = "HSA-ID-6";
  public static final String TEST_LOGICAL_ADDRESS_7 = "HSA-ID-7";
  public static final String TEST_LOGICAL_ADDRESS_11 = "HSA-ID-11";
  public static final String TEST_LOGICAL_ADDRESS_12 = "HSA-ID-12";
  public static final String TEST_LOGICAL_ADDRESS_31 = "HSA-ID-31";
  public static final String TEST_LOGICAL_ADDRESS_32 = "HSA-ID-32";
  public static final String TEST_LOGICAL_ADDRESS_77 = "HSA-ID-77";
  public static final String TEST_LOGICAL_ADDRESS_CHILD = "SE0000000005-1234";
  public static final String TEST_LOGICAL_ADDRESS_PARENT = "SE0000000003-1234";
  public static final String HSA_ID_FEL = "HSA-ID-FEL";

  public static final String TEST_RR_ID_MANY_HITS_NO_ERRORS = "121212121212"; // TC1 - Tolvan Tolvansson
  public static final String TEST_RR_ID_ZERO_HITS = "188803099368"; // TC2 - Agda Andersson
  public static final String TEST_RR_ID_ONE_HIT = "194911172296"; // TC3 - Sven Sturesson
  public static final String TEST_RR_ID_MANY_HITS = "198611062384"; // TC4 - Ulla Alm
  public static final String TEST_RR_ID_FAULT_INVALID_ID = "192011189228"; // TC5 - Gunbritt Boden
  public static final String TEST_RR_ID_EJ_SAMVERKAN_I_TAK = "194804032094"; // TC6 - Laban Meijer
  public static final String TEST_RR_ID_TRADKLATTRING = "194808069887"; // TC7
  public static final String TEST_RR_ID_ONE_FORMAT_ERROR = "194800000001"; // TC8
  public static final String TEST_RR_ID_THREE_CATEGORIES = "194800000002"; // TC9
  public static final String TEST_ID_FAULT_INVALID_ID_IN_EI = "EI:INV_ID";
  public static final String TEST_ID_FAULT_TIMEOUT_IN_EI = "EI:TIMEOUT";

  public static final String TEST_BO_ID_ONE_HIT = "1001";
  public static final String TEST_BO_ID_MANY_HITS_1 = "1002";
  public static final String TEST_BO_ID_MANY_HITS_2 = "1003";
  public static final String TEST_BO_ID_MANY_HITS_3 = "1004";
  public static final String TEST_BO_ID_MANY_HITS_4 = "1004";
  public static final String TEST_BO_ID_MANY_HITS_NEW_1 = "2001";
  public static final String TEST_BO_ID_FAULT_INVALID_ID = "5001";
  public static final String TEST_BO_ID_EJ_SAMVERKAN_I_TAK = "6001";
  public static final String TEST_BO_ID_TRADKLATTRING = "7001";

  public static final String TEST_DATE_ONE_HIT = "20130101000000";
  public static final String TEST_DATE_MANY_HITS_1 = "20130301000000";
  public static final String TEST_DATE_MANY_HITS_2 = "20130401000000";
  public static final String TEST_DATE_MANY_HITS_3 = "20130401000000";
  public static final String TEST_DATE_MANY_HITS_4 = "20130415000000";
  public static final String TEST_DATE_FAULT_INVALID_ID = "20130101000000";
  public static final String TEST_DATE_EJ_SAMVERKAN_I_TAK = "20130106000000";
  public static final String TEST_DATE_TRADKLATTRING = "20130406000000";

  public static final String CATEGORY_DEFAULT = "test_category";
  public static final String CATEGORY_1 = "cat1";
  public static final String CATEGORY_2 = "cat2";
  public static final String CATEGORY_3 = "cat3";


}