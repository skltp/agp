package se.skltp.aggregatingservices.processors;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static se.skltp.aggregatingservices.constants.AgpProperties.AGP_ORIGINAL_QUERY;
import static se.skltp.aggregatingservices.constants.AgpProperties.AGP_SERVICE_HANDLER;
import static se.skltp.aggregatingservices.constants.AgpProperties.AGP_TAK_CONTRACT_NAME;
import static se.skltp.aggregatingservices.data.TestDataDefines.CATEGORY_1;
import static se.skltp.aggregatingservices.data.TestDataDefines.CATEGORY_2;
import static se.skltp.aggregatingservices.data.TestDataDefines.CATEGORY_3;
import static se.skltp.aggregatingservices.data.TestDataDefines.CATEGORY_DEFAULT;
import static se.skltp.aggregatingservices.data.TestDataDefines.TEST_RR_ID_MANY_HITS_NO_ERRORS;
import static se.skltp.aggregatingservices.data.TestDataDefines.TEST_RR_ID_THREE_CATEGORIES;

import java.util.Arrays;
import java.util.List;
import javax.annotation.PostConstruct;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.DefaultExchange;
import org.apache.camel.test.spring.junit5.MockEndpoints;
import org.apache.cxf.message.MessageContentsList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import se.skltp.aggregatingservices.constants.AgpHeaders;
import se.skltp.aggregatingservices.riv.itintegration.engagementindex.findcontentresponder.v1.FindContentResponseType;
import se.skltp.aggregatingservices.service.Authority;
import se.skltp.aggregatingservices.service.TakCacheService;
import se.skltp.aggregatingservices.service.TakCacheServiceImpl;
import se.skltp.aggregatingservices.utils.AgpServiceFactoryImpl;
import se.skltp.aggregatingservices.utils.FindContentUtil;
import se.skltp.aggregatingservices.utils.RequestUtil;
import se.skltp.takcache.BehorigheterCache;
import se.skltp.takcache.RoutingInfo;
import se.skltp.takcache.TakCache;
import se.skltp.takcache.VagvalCache;

@ExtendWith({SpringExtension.class})
@ContextConfiguration(classes = {FilterFindContentResponseProcessor.class, TakCacheServiceImpl.class})
@TestPropertySource("classpath:application.properties")
@MockEndpoints("direct:end")
public class FilterFindContentResponseProcessorTest {

  @MockBean(name = "takCache")
  private TakCache takCache;

  @Mock
  VagvalCache vagvalCache;

  @Mock
  private BehorigheterCache behorigheterCache;

  @Autowired
  TakCacheService takCacheService;

  @Autowired
  FilterFindContentResponseProcessor filterFindContentResponseProcessor;

  @PostConstruct
  public void postConstruct() {
    Mockito.when(takCache.getBehorigeterCache()).thenReturn(behorigheterCache);
    Mockito.when(takCache.getVagvalCache()).thenReturn(vagvalCache);
    Mockito.when(vagvalCache.getRoutingInfo(any(), any())).thenReturn(Arrays.asList(new RoutingInfo()));
    ((TakCacheServiceImpl)takCacheService).initHandlers();
  }

  @Test
  public void testProcessNoEngagementFilteredWhenOnlyOneCategoryDefined() {
    final Exchange exchange = createExchange(TEST_RR_ID_THREE_CATEGORIES, CATEGORY_DEFAULT);
    Mockito.when(behorigheterCache.isAuthorized(any(), any(),any())).thenReturn(true);

    filterFindContentResponseProcessor.process(exchange);
    final FindContentResponseType findContentResponse = (FindContentResponseType) exchange.getIn().getBody(MessageContentsList.class).get(0);
    assertEquals(3, findContentResponse.getEngagement().size());
  }

  @Test
  public void testProcessNoEngagementFilteredWhenAllCategoriesDefined() {
    final Exchange exchange = createExchange(TEST_RR_ID_THREE_CATEGORIES, "cat1,cat2,cat3");
    Mockito.when(behorigheterCache.isAuthorized(any(), any(),any())).thenReturn(true);

    filterFindContentResponseProcessor.process(exchange);
    final FindContentResponseType findContentResponse = (FindContentResponseType) exchange.getIn().getBody(MessageContentsList.class).get(0);
    assertEquals(3, findContentResponse.getEngagement().size());
  }

  @Test
  public void testProcessOneEngagementFilteredWhenTwoCategoriesDefined() {
    final Exchange exchange = createExchange(TEST_RR_ID_THREE_CATEGORIES, "cat1,cat2");
    Mockito.when(behorigheterCache.isAuthorized(any(), any(),any())).thenReturn(true);

    filterFindContentResponseProcessor.process(exchange);
    final FindContentResponseType findContentResponse = (FindContentResponseType) exchange.getIn().getBody(MessageContentsList.class).get(0);
    assertEquals(2, findContentResponse.getEngagement().size());
  }

  @Test
  public void testProcessAllEngagementFilteredWhenOtherCategoriesDefined() {
    final Exchange exchange = createExchange(TEST_RR_ID_THREE_CATEGORIES, "cat5,cat6");
    filterFindContentResponseProcessor.process(exchange);
    final FindContentResponseType findContentResponse = (FindContentResponseType) exchange.getIn().getBody(MessageContentsList.class).get(0);
    assertEquals(0, findContentResponse.getEngagement().size());
  }

  @Test
  public void testFilterFunctionNoEngagementFilteredByCategorizations() {
    // Creates three engagements with HSA-ID-4, HSA-ID-5, HSA-ID-6
    final FindContentResponseType findContentResponse = FindContentUtil.createFindContentResponse(TEST_RR_ID_THREE_CATEGORIES);
    assertEquals(3, findContentResponse.getEngagement().size());

    final List<String> cats = Arrays.asList(CATEGORY_1, CATEGORY_2, CATEGORY_3);

    filterFindContentResponseProcessor.filterFindContentResponseBasedOnCategorizations(findContentResponse, cats);
    assertEquals(3, findContentResponse.getEngagement().size());

  }

  @Test
  public void testFilterFunctionOneEngagementFilteredByCategorizations() {
    // Creates three engagements with HSA-ID-4, HSA-ID-5, HSA-ID-6
    final FindContentResponseType findContentResponse = FindContentUtil.createFindContentResponse(TEST_RR_ID_THREE_CATEGORIES);
    assertEquals(3, findContentResponse.getEngagement().size());

    final List<String> cats = Arrays.asList(CATEGORY_1, CATEGORY_3);

    filterFindContentResponseProcessor.filterFindContentResponseBasedOnCategorizations(findContentResponse, cats);
    assertEquals(2, findContentResponse.getEngagement().size());

  }

  @Test
  public void testFilterFunctionAllEngagementFilteredByCategorizations() {
    // Creates three engagements with HSA-ID-4, HSA-ID-5, HSA-ID-6
    final FindContentResponseType findContentResponse = FindContentUtil.createFindContentResponse(TEST_RR_ID_THREE_CATEGORIES);
    assertEquals(3, findContentResponse.getEngagement().size());

    final List<String> cats = Arrays.asList(CATEGORY_DEFAULT);

    filterFindContentResponseProcessor.filterFindContentResponseBasedOnCategorizations(findContentResponse, cats);
    assertEquals(0, findContentResponse.getEngagement().size());

  }

  @Test
  public void NoEngagementFilteredByAuthority() {
    // Creates three engagements with HSA-ID-4, HSA-ID-5, HSA-ID-6
    final FindContentResponseType findContentResponse = FindContentUtil.createFindContentResponse(TEST_RR_ID_MANY_HITS_NO_ERRORS);
    assertEquals(3, findContentResponse.getEngagement().size());

    Mockito.when(behorigheterCache.isAuthorized("sender1", "ns:1", "HSA-ID-4")).thenReturn(true);
    Mockito.when(behorigheterCache.isAuthorized("org_sender1", "ns:1", "HSA-ID-5")).thenReturn(true);
    Mockito.when(behorigheterCache.isAuthorized("sender1", "ns:1", "HSA-ID-6")).thenReturn(true);

    filterFindContentResponseProcessor.filterFindContentResponseBasedOnAuthority(findContentResponse, getAuthority());
    assertEquals(3, findContentResponse.getEngagement().size());

  }

  @Test
  public void TwoEngagementFilteredByAuthority() {
    // Creates three engagements with HSA-ID-4, HSA-ID-5, HSA-ID-6
    final FindContentResponseType findContentResponse = FindContentUtil.createFindContentResponse(TEST_RR_ID_MANY_HITS_NO_ERRORS);
    assertEquals(3, findContentResponse.getEngagement().size());

    Mockito.when(behorigheterCache.isAuthorized("sender1", "ns:1", "HSA-ID-4")).thenReturn(true);

    filterFindContentResponseProcessor.filterFindContentResponseBasedOnAuthority(findContentResponse, getAuthority());
    assertEquals(1, findContentResponse.getEngagement().size());
  }

  @Test
  public void AllEngagementFilteredByAuthority() {
    // Creates three engagements with HSA-ID-4, HSA-ID-5, HSA-ID-6
    final FindContentResponseType findContentResponse = FindContentUtil.createFindContentResponse(TEST_RR_ID_MANY_HITS_NO_ERRORS);
    assertEquals(3, findContentResponse.getEngagement().size());

    Mockito.when(behorigheterCache.isAuthorized("sender1", "ns:1", "HSA-ID-4")).thenReturn(false);

    filterFindContentResponseProcessor.filterFindContentResponseBasedOnAuthority(findContentResponse, getAuthority());
    assertEquals(0, findContentResponse.getEngagement().size());
  }

  private Authority getAuthority() {
    Authority authority = new Authority();
    authority.setSenderId("sender1");
    authority.setOriginalSenderId("org_sender1");
    authority.setServicecontractNamespace("ns:1");
    return authority;
  }

  private Exchange createExchange(String patient, String category) {
    final Exchange ex = new DefaultExchange(new DefaultCamelContext());

    ex.setProperty(AGP_ORIGINAL_QUERY, RequestUtil.createTestMessageContentsList());
    ex.setProperty(AGP_SERVICE_HANDLER, AgpServiceFactoryImpl.createInstance("domain1", category));
    ex.getIn().setHeader(AgpHeaders.X_VP_SENDER_ID, "sender1");
    ex.getIn().setHeader(AgpHeaders.X_RIVTA_ORIGINAL_SERVICE_CONSUMER_HSA_ID, "org_sender1");
    ex.setProperty(AGP_TAK_CONTRACT_NAME, "ns:1");
    ex.getIn().setBody(FindContentUtil.createMessageContentsList(patient));

    return ex;
  }

}
