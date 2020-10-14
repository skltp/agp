package se.skltp.aggregatingservices.riv.clinicalprocess.healthcond.actoutcome.v4;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import riv.clinicalprocess.healthcond.actoutcome.getlaboratoryorderoutcomeresponder.v4.GetLaboratoryOrderOutcomeResponseType;
import se.skltp.aggregatingservices.GLOOTestDataGenerator;
import se.skltp.aggregatingservices.api.AgpServiceFactory;
import se.skltp.aggregatingservices.riv.clinicalprocess.healthcond.actoutcome.getaggregatedlaboratoryorderoutcome.GLOOAgpServiceConfiguration;
import se.skltp.aggregatingservices.riv.clinicalprocess.healthcond.actoutcome.getaggregatedlaboratoryorderoutcome.GLOOAgpServiceFactoryImpl;
import se.skltp.aggregatingservices.tests.CreateFindContentTest;


@ExtendWith({SpringExtension.class})
public class GLOOCreateFindContentTest extends CreateFindContentTest {

  private static GLOOAgpServiceConfiguration configuration = new GLOOAgpServiceConfiguration();
  private static AgpServiceFactory<GetLaboratoryOrderOutcomeResponseType> agpServiceFactory = new GLOOAgpServiceFactoryImpl();
  private static GLOOTestDataGenerator testDataGenerator = new GLOOTestDataGenerator();

  public GLOOCreateFindContentTest() {
    super(testDataGenerator, agpServiceFactory, configuration);
  }

}
