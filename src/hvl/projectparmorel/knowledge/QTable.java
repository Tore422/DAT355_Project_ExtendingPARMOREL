package hvl.projectparmorel.knowledge;

public class QTable {
	ErrorContextActionDirectory<Double> qTable;
	
	protected QTable() {
		qTable = new HashErrorContextActionDirectory<>();
	}
	
	/**
	 * Inserts a new entry in the qTable.
	 * 
	 * @param errorCode
	 * @param contextId
	 * @param actionId
	 * @param weight
	 */
	public void insertNewErrorCode(Integer errorCode, Integer contextId, Integer actionId, Double weight) {
		qTable.insertNewErrorCode(errorCode, contextId, actionId, weight);
	}
	
	/**
	 * Checks that the provided error code is stored in the ErrorMap.
	 * 
	 * @param errorCode to check
	 * @return true if the errorCode is in the ErrorMap, false otherwise.
	 */
	public boolean containsErrorCode(Integer errorCode) {
		return qTable.getErrorMap().containsErrorCode(errorCode);
	}
	
	/**
	 * Checks that the provided context ID exists for the given error code.
	 * 
	 * @param errorCode
	 * @param contextId
	 * @return true if the context ID exists for the given error code, false otherwise.
	 */
	public boolean containsContextIdForError(int errorCode, int contextId) {
		return qTable.getErrorMap().containsContextIdForErrorCode(errorCode, contextId);
	}
	
	/**
	 * Gets the qTable directory.
	 * 
	 * @return the qTalbe
	 */
	protected ErrorContextActionDirectory<Double> getQTableDirectory(){
		return qTable;
	}

	/**
	 * Checks that 
	 * @param errorCode
	 * @param contextId
	 * @param actionId
	 * @return
	 */
	public boolean containsActionIdForErrorCodeAndContextId(int errorCode, int contextId, int actionId) {
		return qTable.getErrorMap().containsActionIdForErrorCodeAndContextId(errorCode, contextId, actionId);
	}
}
