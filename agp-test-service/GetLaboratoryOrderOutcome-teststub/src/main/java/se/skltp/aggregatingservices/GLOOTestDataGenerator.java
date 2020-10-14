/**
 * Copyright (c) 2014 Inera AB, <http://inera.se/>
 *
 * This file is part of SKLTP.
 *
 * This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later
 * version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package se.skltp.aggregatingservices;

import lombok.extern.log4j.Log4j2;
import org.apache.cxf.message.MessageContentsList;
import org.springframework.stereotype.Service;
import riv.clinicalprocess.healthcond.actoutcome._4.AccessControlHeaderType;
import riv.clinicalprocess.healthcond.actoutcome._4.AnsvarigtLabbType;
import riv.clinicalprocess.healthcond.actoutcome._4.CVType;
import riv.clinicalprocess.healthcond.actoutcome._4.HeaderType;
import riv.clinicalprocess.healthcond.actoutcome._4.IIType;
import riv.clinicalprocess.healthcond.actoutcome._4.LaboratoryOrderOutcomeBodyType;
import riv.clinicalprocess.healthcond.actoutcome._4.LaboratoryOrderOutcomeType;
import riv.clinicalprocess.healthcond.actoutcome._4.OrgUnitType;
import riv.clinicalprocess.healthcond.actoutcome._4.PatientType;
import riv.clinicalprocess.healthcond.actoutcome._4.PatientinformationType;
import riv.clinicalprocess.healthcond.actoutcome._4.PersonIdType;
import riv.clinicalprocess.healthcond.actoutcome._4.RemissType;
import riv.clinicalprocess.healthcond.actoutcome._4.SourceType;
import riv.clinicalprocess.healthcond.actoutcome._4.SvarsmottagareType;
import riv.clinicalprocess.healthcond.actoutcome.getlaboratoryorderoutcomeresponder.v4.GetLaboratoryOrderOutcomeResponseType;
import riv.clinicalprocess.healthcond.actoutcome.getlaboratoryorderoutcomeresponder.v4.GetLaboratoryOrderOutcomeType;
import se.skltp.aggregatingservices.data.TestDataGenerator;


@Log4j2
@Service
public class GLOOTestDataGenerator extends TestDataGenerator {

  @Override
  public String getPatientId(MessageContentsList messageContentsList) {
    GetLaboratoryOrderOutcomeType request = (GetLaboratoryOrderOutcomeType) messageContentsList.get(1);
    return request.getPatientId().getId();
  }

  @Override
  public Object createResponse(Object... responseItems) {
    log.info("Creating a response with {} items", responseItems.length);
    GetLaboratoryOrderOutcomeResponseType response = new GetLaboratoryOrderOutcomeResponseType();
    for (int i = 0; i < responseItems.length; i++) {
      response.getLaboratoryOrderOutcome().add((LaboratoryOrderOutcomeType) responseItems[i]);
    }

    log.info("response.toString:" + response.toString());

    return response;
  }

  @Override
  public Object createResponseItem(String logicalAddress, String registeredResidentId, String businessObjectId, String time) {

    log.debug("Created LaboratoryOrderOutcomeType for logical-address {}, registeredResidentId {} and businessObjectId {}",
        new Object[]{logicalAddress, registeredResidentId, businessObjectId});

    LaboratoryOrderOutcomeType labOrderOutcome = new LaboratoryOrderOutcomeType();

    // Heaader
    HeaderType header = createHeaderType(logicalAddress, registeredResidentId);
    labOrderOutcome.setLaboratoryOrderOutcomeHeader(header);

    // Body
    LaboratoryOrderOutcomeBodyType body = createLaboratoryOrderOutcomeBodyType();
    labOrderOutcome.setLaboratoryOrderOutcomeBody(body);

    return labOrderOutcome;
  }

  @Override
  public Object createFormatError(Object responseItem){
    LaboratoryOrderOutcomeType labOrderOutcome = (LaboratoryOrderOutcomeType) responseItem;
    PatientinformationType pinfo = labOrderOutcome.getLaboratoryOrderOutcomeBody().getPatientinformation();

    // This will create validation error if schema validation is enabled
    pinfo.setFodelsetidpunkt("1895");

    return labOrderOutcome;
  }

	@Override
	public Object createRequest(String patientId, String sourceSystemHSAId) {
    GetLaboratoryOrderOutcomeType getLaboratoryOrderOutcomeType = new GetLaboratoryOrderOutcomeType();
    PersonIdType personIdType = new PersonIdType();
    personIdType.setType("1.2.752.129.2.1.3.1");
    personIdType.setId(patientId);
    getLaboratoryOrderOutcomeType.setPatientId(personIdType);
    getLaboratoryOrderOutcomeType.setSourceSystemHSAId(sourceSystemHSAId);
    return getLaboratoryOrderOutcomeType;
  }


	private LaboratoryOrderOutcomeBodyType createLaboratoryOrderOutcomeBodyType() {
    LaboratoryOrderOutcomeBodyType body = new LaboratoryOrderOutcomeBodyType();

    // Remiss
    RemissType remissType = new RemissType();
    remissType.setRemisstid("20200609104530");
    remissType.setRemissid(createIIType("remiss id", "1:2"));
    body.setRemiss(remissType);

    // Patientinformation (YYYYMMDDhhmmss)
    PatientinformationType pinfo = new PatientinformationType();
    pinfo.setFodelsetidpunkt("19010101010101");
    body.setPatientinformation(pinfo);

    // resultattyp
    CVType cvType = new CVType();
    cvType.setCode("code");
    body.setResultattyp(cvType);

    // laboratorieid
    body.setLaboratorieid(createIIType("lab id", "1.2.3"));

    //svarsrapportid
    body.setSvarsrapportid(createIIType("svars id", "2.3.4"));

    // svarstidpunkt YYYYMMDDhhmmss
    body.setSvarstidpunkt("20200609104530");

    //ansvarigtLabb
    final AnsvarigtLabbType ansvarigtLabbType = new AnsvarigtLabbType();
    final OrgUnitType orgUnitType = new OrgUnitType();
    orgUnitType.setName("org unit");
    orgUnitType.setId(createIIType("org unit", "0.2.3"));
    ansvarigtLabbType.setOrgUnit(orgUnitType);
    body.setAnsvarigtLabb(ansvarigtLabbType);

    //svarsmottagare
    body.setSvarsmottagare(new SvarsmottagareType());

    //Body start
    body.setResultatkommentar("kommentar");
    body.setResultatrapport("OK");
    return body;
  }

  private HeaderType createHeaderType(String logicalAddress, String registeredResidentId) {
    HeaderType header = new HeaderType();

    // AccessControlHeader
    AccessControlHeaderType accessControlHeaderType = new AccessControlHeaderType();
    accessControlHeaderType.setAccountableCareGiver(createIIType("HSA-CareGiver", "0.1.2"));
    accessControlHeaderType.setAccountableCareUnit(createIIType("HSA-CareUnit", "0.1.2"));
    PatientType person = new PatientType();
    person.getId().add(createIIType(registeredResidentId, "1.2.752.129.2.1.3.1"));
    accessControlHeaderType.setPatient(person);

    header.setAccessControlHeader(accessControlHeaderType);

    // Source
    SourceType source = new SourceType();
    IIType systemId = createIIType(logicalAddress, "4.5.6");
    source.setSystemId(systemId);
    header.setSource(source);

    return header;
  }

  private IIType createIIType(String logicalAddress, String s) {
    IIType systemId = new IIType();
    systemId.setRoot(logicalAddress);
    systemId.setExtension(s);
    return systemId;
  }


}
