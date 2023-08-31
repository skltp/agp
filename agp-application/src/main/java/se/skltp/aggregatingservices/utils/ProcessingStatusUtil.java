package se.skltp.aggregatingservices.utils;

import static se.skltp.agp.riv.interoperability.headers.v1.StatusCodeEnum.DATA_FROM_CACHE;
import static se.skltp.agp.riv.interoperability.headers.v1.StatusCodeEnum.DATA_FROM_CACHE_SYNCH_FAILED;
import static se.skltp.agp.riv.interoperability.headers.v1.StatusCodeEnum.DATA_FROM_SOURCE;

import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import org.apache.cxf.binding.soap.SoapFault;
import se.skltp.agp.riv.interoperability.headers.v1.CausingAgentEnum;
import se.skltp.agp.riv.interoperability.headers.v1.LastUnsuccessfulSynchErrorType;
import se.skltp.agp.riv.interoperability.headers.v1.ProcessingStatusRecordType;
import se.skltp.agp.riv.interoperability.headers.v1.StatusCodeEnum;

public class ProcessingStatusUtil {

	private static ThreadSafeSimpleDateFormat df = new ThreadSafeSimpleDateFormat("yyyyMMddHHmmss");

	// Utility class
	private ProcessingStatusUtil() {
	}

	public static ProcessingStatusRecordType createStatusRecord(String logicalAddress, StatusCodeEnum statusCode) {
		return createStatusRecord(logicalAddress, statusCode, null);
	}

	/**
	 * Implement the logic as described in the table:
	 * 
	 * +--------------------------+---------------------+-------------------+--------------------------+-----------------------+----------------------------+
	 * | Status Code              | isResponseFromCache | isResponseInSynch | lastSuccessfulSynch      | lastUnsuccessfulSynch | lastUnsuccessfulSynchError |
	 * +--------------------------+---------------------+-------------------+--------------------------+-----------------------+----------------------------+
	 * | DataFromSource           | False               | True              | Current timestamp        | Emtpy                 | Empty                      |
	 * | DataFromCache            | True                | True              | Last time for succ. call | Empty                 | Empty                      |
	 * | DataFromCacheSynchFailed | True                | False             | Last time for succ. call | Current timestamp     | Relevant error info        |
	 * | NoDataSynchFailed        | False               | False             | Empty                    | Current timestamp     | Relevant error info        |
	 * +--------------------------+---------------------+-------------------+--------------------------+-----------------------+----------------------------+
	 * */
	public static ProcessingStatusRecordType createStatusRecord(String logicalAddress, StatusCodeEnum statusCode,
			Exception exception) {
		ProcessingStatusRecordType status = new ProcessingStatusRecordType();

		status.setLogicalAddress(logicalAddress);
		status.setStatusCode(statusCode);
		status.setIsResponseFromCache(statusCode == DATA_FROM_CACHE || statusCode == DATA_FROM_CACHE_SYNCH_FAILED);
		status.setIsResponseInSynch(statusCode == DATA_FROM_SOURCE || statusCode == DATA_FROM_CACHE);

		if (statusCode == DATA_FROM_SOURCE) {
			status.setLastSuccessfulSynch(df.format(new Date()));
		}

		if (status.isIsResponseInSynch()) {
			if (exception != null) {
				throw new IllegalArgumentException("Error argument not allowed for state DATA_FROM_SOURCE and DATA_FROM_CACHE, must be null");
			}

		} else {

			// Ok, so the call failed. Fill in error info...
			status.setLastUnsuccessfulSynch(df.format(new Date()));
			status.setLastUnsuccessfulSynchError(createError(exception));
		}
		return status;
	}

	public static LastUnsuccessfulSynchErrorType createError(Exception exception){

		String errorText = exception.getMessage();
		if(exception.getCause()!=null){
			errorText += ", " + exception.getCause().getMessage();
		}

		LastUnsuccessfulSynchErrorType error = new LastUnsuccessfulSynchErrorType();
		error.setCausingAgent(CausingAgentEnum.VIRTUALIZATION_PLATFORM);
		if(exception instanceof SoapFault){
			error.setCode(Integer.toString(((SoapFault)exception).getStatusCode()));
			// Fix error in encoding from SOAP Fault details (probably a CXF bug)
			errorText = new String(errorText.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
		} else if( exception instanceof SocketTimeoutException ) {
			// Gateway timeout
			error.setCode("504");
		}	else {
			// This is the default for old mule AGP ( We couldn't find a definition for the code)
			error.setCode("43000");
		}
		error.setText(errorText);
		return error;
	}


}