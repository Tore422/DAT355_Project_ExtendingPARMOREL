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
	 * Checks that the specified error code and context ID contains the specified action ID
	 * 
	 * @param errorCode
	 * @param contextId
	 * @param actionId
	 * @return true if the action ID is found, false otherwise
	 */
	public boolean containsActionIdForErrorCodeAndContextId(int errorCode, int contextId, int actionId) {
		return qTable.getErrorMap().containsActionIdForErrorCodeAndContextId(errorCode, contextId, actionId);
	}

	/**
	 * Updates the weight for the specified action ID for the specified context ID for the specified error code.
	 * 
	 * @param errorCode
	 * @param contextId
	 * @param actionId
	 * @param weight
	 */
	public void updateWeight(Integer errorCode, Integer contextId, Integer actionId, Double weight) {
		qTable.updateValue(errorCode, contextId, actionId, weight);	
	}

	/**
	 * Sets the weight for the specified action ID for the specified context ID for the specified error code.
	 * 
	 * @param errorCode
	 * @param contextId
	 * @param actionId
	 * @param weight
	 */
	public void setWeight(Integer errorCode, Integer contextId, Integer actionId, Double weight) {
		qTable.addValue(errorCode, contextId, actionId, weight);
	}

	/**
	 * Gets the optimal context and action ID to handle the specified error.
	 * 
	 * @param errorCode
	 * @return the location of highest value in the context map. If two are equal, one of them is returned. If the set is empty, null is returned.
	 */
	protected ActionLocation getOptimalActionIndexForErrorCode(Integer errorCode) {
		return qTable.getOptimalActionIndexForErrorCode(errorCode);
		
	}
}
