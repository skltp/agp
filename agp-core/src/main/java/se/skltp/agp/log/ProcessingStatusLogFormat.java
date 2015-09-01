package se.skltp.agp.log;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.module.SimpleModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.skltp.agp.riv.interoperability.headers.v1.LastUnsuccessfulSynchErrorType;
import se.skltp.agp.riv.interoperability.headers.v1.ProcessingStatusRecordType;
import se.skltp.agp.riv.interoperability.headers.v1.ProcessingStatusType;
import se.skltp.agp.riv.interoperability.headers.v1.StatusCodeEnum;

/**
 * Format the RIV SOAP header ProcessingStatus
 * (urn:riv:interoperability:headers:1) to make it suitable for writing as a
 * logevent that can easily be both manually read and machine-parsed.
 * <p>
 * Example of fully populated header, see schema for details:
 * </p>
 * 
 * <pre>
 * &lt;ProcessingStatus xmlns="urn:riv:interoperability:headers:1" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="urn:riv:interoperability:headers:1 interoperability_headers_1.0.xsd "&gt;
 *   &lt;ProcessingStatusList&gt;
 *     &lt;logicalAddress&gt;logicalAddress&lt;/logicalAddress&gt;
 *     &lt;statusCode&gt;DataFromSource&lt;/statusCode&gt;
 *     &lt;isResponseFromCache&gt;true&lt;/isResponseFromCache&gt;
 *     &lt;isResponseInSynch&gt;true&lt;/isResponseInSynch&gt;
 *     &lt;lastSuccessfulSynch&gt;lastSuccessfulSynch&lt;/lastSuccessfulSynch&gt;
 *     &lt;lastUnsuccessfulSynch&gt;lastUnsuccessfulSynch&lt;/lastUnsuccessfulSynch&gt;
 *     &lt;lastUnsuccessfulSynchError&gt;
 *       &lt;causingAgent&gt;service_catalog&lt;/causingAgent&gt;
 *       &lt;code&gt;code&lt;/code&gt;
 *       &lt;text&gt;text&lt;/text&gt;
 *     &lt;/lastUnsuccessfulSynchError&gt;
 *   &lt;/ProcessingStatusList&gt;
 * &lt;/ProcessingStatus&gt;
 * </pre>
 * 
 * @author hakan
 */
public class ProcessingStatusLogFormat {
	private static final Logger log = LoggerFactory
			.getLogger(ProcessingStatusLogFormat.class);

	private static final String LOG_PROPERTY_NAME_LOGICAL_ADDRESS = "logicalAddress";
	private static final String LOG_PROPERTY_NAME_STATUS_CODE = "statusCode";
	private static final String LOG_PROPERTY_NAME_ERROR_CAUSING_AGENT = "errorCausingAgent";
	private static final String LOG_PROPERTY_NAME_ERROR_CODE = "errorCode";
	private static final String LOG_PROPERTY_NAME_ERROR_TEXT = "errorText";

	private int procStatusCountTot;
	private int procStatusCountFail;
	private String procStatus;

	public int getProcStatusCountTot() {
		return procStatusCountTot;
	}

	public int getProcStatusCountFail() {
		return procStatusCountFail;
	}

	/**
	 * The formatted ProcessingStatusType needs to be a single line if we are to
	 * map it using soi-toolkit and logstash (without doing too much heavy
	 * work).
	 */
	public String getProcStatus() {
		return procStatus;
	}

	public ProcessingStatusLogFormat(ProcessingStatusType pst) {
		try {
			procStatusCountTot = pst.getProcessingStatusList().size();
			// count errors
			// Note: caching not used, so StatusCodeEnum.DATA_FROM_CACHE and all
			// cache related attributes are not interesting
			for (ProcessingStatusRecordType ps : pst.getProcessingStatusList()) {
				if (!ps.getStatusCode().equals(StatusCodeEnum.DATA_FROM_SOURCE)) {
					procStatusCountFail++;
				}
			}
			formatProcessingStatusAsJson(pst);
		} catch (Exception e) {
			procStatus = "could not parse ProcessingStatus into JSON";
			log.error(procStatus, e);
		}
	}

	private void formatProcessingStatusAsJson(ProcessingStatusType pst)
			throws JsonGenerationException, JsonMappingException, IOException {

		// serialize to JSON using Jackson (part of Mule ESB dependencies, Mule
		// 3.3 bundles Jackson 1.8)
		ObjectMapper om = new ObjectMapper();
		SimpleModule customSerializerMod = new SimpleModule(
				"CustomSerializerModule", new Version(1, 0, 0, null));
		customSerializerMod.addSerializer(new JsonCustomSerializer());
		om.registerModule(customSerializerMod);
		procStatus = om.writeValueAsString(pst.getProcessingStatusList());
	}

	/**
	 * Custom Jackson serializer to remove unnecessary fields and to flatten the
	 * structure.
	 * 
	 * @author hakan
	 */
	private static class JsonCustomSerializer extends
			JsonSerializer<ProcessingStatusRecordType> {

		@Override
		public Class<ProcessingStatusRecordType> handledType() {
			return ProcessingStatusRecordType.class;
		}

		@Override
		public void serialize(ProcessingStatusRecordType value,
				JsonGenerator jgen, SerializerProvider provider)
				throws IOException, JsonProcessingException {

			jgen.writeStartObject();

			jgen.writeStringField(LOG_PROPERTY_NAME_LOGICAL_ADDRESS,
					value.getLogicalAddress());
			jgen.writeStringField(LOG_PROPERTY_NAME_STATUS_CODE, value
					.getStatusCode().value());

			LastUnsuccessfulSynchErrorType errType = value
					.getLastUnsuccessfulSynchError();
			if (errType != null) {
				jgen.writeStringField(LOG_PROPERTY_NAME_ERROR_CAUSING_AGENT,
						errType.getCausingAgent().value());
				jgen.writeStringField(LOG_PROPERTY_NAME_ERROR_CODE,
						errType.getCode());
				jgen.writeStringField(LOG_PROPERTY_NAME_ERROR_TEXT,
						errType.getText());
			}

			jgen.writeEndObject();
		}

	}
}
