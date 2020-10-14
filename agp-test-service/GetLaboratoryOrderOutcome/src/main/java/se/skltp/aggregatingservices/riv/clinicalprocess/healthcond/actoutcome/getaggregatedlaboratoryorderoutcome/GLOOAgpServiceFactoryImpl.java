package se.skltp.aggregatingservices.riv.clinicalprocess.healthcond.actoutcome.getaggregatedlaboratoryorderoutcome;

import java.util.List;
import lombok.extern.log4j.Log4j2;
import riv.clinicalprocess.healthcond.actoutcome.getlaboratoryorderoutcomeresponder.v4.GetLaboratoryOrderOutcomeResponseType;
import riv.clinicalprocess.healthcond.actoutcome.getlaboratoryorderoutcomeresponder.v4.GetLaboratoryOrderOutcomeType;
import se.skltp.aggregatingservices.AgServiceFactoryBase;

@Log4j2
public class GLOOAgpServiceFactoryImpl extends
    AgServiceFactoryBase<GetLaboratoryOrderOutcomeType, GetLaboratoryOrderOutcomeResponseType> {

  @Override
  public String getPatientId(GetLaboratoryOrderOutcomeType queryObject) {
    return queryObject.getPatientId().getId();
  }

  @Override
  public String getSourceSystemHsaId(GetLaboratoryOrderOutcomeType queryObject) {
    return queryObject.getSourceSystemHSAId();
  }

  @Override
  public GetLaboratoryOrderOutcomeResponseType aggregateResponse(
      List<GetLaboratoryOrderOutcomeResponseType> aggregatedResponseList) {
    GetLaboratoryOrderOutcomeResponseType aggregatedResponse = new GetLaboratoryOrderOutcomeResponseType();

    for (GetLaboratoryOrderOutcomeResponseType response : aggregatedResponseList) {
      aggregatedResponse.getLaboratoryOrderOutcome().addAll(response.getLaboratoryOrderOutcome());
    }

    return aggregatedResponse;
  }
}
