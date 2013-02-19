package se.skltp.agp.cache;

import static se.riv.interoperability.headers.v1.StatusCodeEnum.DATA_FROM_CACHE;
import static se.riv.interoperability.headers.v1.StatusCodeEnum.DATA_FROM_CACHE_SYNCH_FAILED;
import static se.riv.interoperability.headers.v1.StatusCodeEnum.DATA_FROM_SOURCE;

import java.util.Date;
import java.util.List;

import org.soitoolkit.commons.mule.util.ThreadSafeSimpleDateFormat;

import se.riv.interoperability.headers.v1.LastUnsuccessfulSynchErrorType;
import se.riv.interoperability.headers.v1.ProcessingStatusRecordType;
import se.riv.interoperability.headers.v1.ProcessingStatusType;
import se.riv.interoperability.headers.v1.StatusCodeEnum;

public class ProcessingStatusUtil {

	private final ProcessingStatusType ps;

	private ThreadSafeSimpleDateFormat df = new ThreadSafeSimpleDateFormat("yyyyMMddHHmmss");

	public ProcessingStatusUtil() {
		ps = new ProcessingStatusType();
	}

	public ProcessingStatusUtil(ProcessingStatusType ps) {
		this.ps = ps;
	}

	public ProcessingStatusType getStatus() {
		return ps;
	}

	public void addStatusRecord(String logicalAddress, StatusCodeEnum statusCode) {
		addStatusRecord(logicalAddress, statusCode, null);
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
	 * 
	 * @param logicalAddress
	 * @param statusCode
	 * @param error
	 */
	public void addStatusRecord(String logicalAddress, StatusCodeEnum statusCode, LastUnsuccessfulSynchErrorType error) {

		ProcessingStatusRecordType status = new ProcessingStatusRecordType();
		
		// LogicalAddress
		status.setLogicalAddress(logicalAddress);

		// StatusCode
		status.setStatusCode(statusCode);

		// isResponseFromCache
		status.setIsResponseFromCache(statusCode == DATA_FROM_CACHE || statusCode == DATA_FROM_CACHE_SYNCH_FAILED);
		
		// isResponseInSynch
		status.setIsResponseInSynch(statusCode == DATA_FROM_SOURCE || statusCode == DATA_FROM_CACHE);

		// lastSuccessfulSynch
		if (statusCode == DATA_FROM_SOURCE) {
			status.setLastSuccessfulSynch(df.format(new Date()));
		}
		// TODO: DATA_FROM_CACHE/DATA_FROM_CACHE_SYNCH_FAILED: How to pickup time for last succ call from cache??? 

		// lastUnsuccessfulSynch and lastUnsuccessfulSynchError
		if (status.isIsResponseInSynch()) {

			// Check that no error info was supplied if the call was successful
			if (error != null) {
				throw new IllegalArgumentException("Error argument not allowed for state DATA_FROM_SOURCE and DATA_FROM_CACHE, must be null");
			}

		} else {
		
			// Ok, so the call failed. Fill in error info...
			status.setLastUnsuccessfulSynch(df.format(new Date()));
			status.setLastUnsuccessfulSynchError(error);
		}

		// Finally add the new record to the list
		ps.getProcessingStatusList().add(status);
	}

	/**
	 * Updates processing status when a new entry is stored in the cache as:
	 * 1. Status Code: DataFromSource --> DataFromCache
	 * 2. isResponseFromCache: false --> true 
	 */	
	public void updateProcessingStatusAsCached() {
		List<ProcessingStatusRecordType> psList = ps.getProcessingStatusList();
		for (ProcessingStatusRecordType psr : psList) {
			if (psr.getStatusCode() == StatusCodeEnum.DATA_FROM_SOURCE) {
				psr.setStatusCode(StatusCodeEnum.DATA_FROM_CACHE);
				psr.setIsResponseFromCache(true);
			}
		}
	}

	/**
	 * Updates the processing status for the specified logicalAddress after an successful update of its state (i.e. as a response to a notification from EI)
	 * 
	 * Set the following state:
	 * +--------------------------+---------------------+-------------------+--------------------------+-----------------------+----------------------------+
	 * | Status Code              | isResponseFromCache | isResponseInSynch | lastSuccessfulSynch      | lastUnsuccessfulSynch | lastUnsuccessfulSynchError |
	 * +--------------------------+---------------------+-------------------+--------------------------+-----------------------+----------------------------+
	 * | DataFromCache            | True                | True              | Last time for succ. call | Empty                 | Empty                      |
	 * +--------------------------+---------------------+-------------------+--------------------------+-----------------------+----------------------------+
	 */	
	public void updateProcessingStatusAsCacheUpdated(String logicalAddress) {
		List<ProcessingStatusRecordType> psList = ps.getProcessingStatusList();
		for (ProcessingStatusRecordType psr : psList) {
			if (psr.getLogicalAddress().equals(logicalAddress)) {
				psr.setStatusCode(StatusCodeEnum.DATA_FROM_CACHE);
				psr.setIsResponseFromCache(true);
				psr.setIsResponseInSynch(true);
				psr.setLastSuccessfulSynch(df.format(new Date()));
				psr.setLastUnsuccessfulSynch(null);
				psr.setLastUnsuccessfulSynchError(null);
			}
		}
	}

}