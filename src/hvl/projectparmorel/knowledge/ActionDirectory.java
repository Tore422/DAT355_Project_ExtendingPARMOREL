package hvl.projectparmorel.knowledge;

public class ActionDirectory {
	ErrorContextActionDirectory<Action> preferenceScores;
	
	protected ActionDirectory() {
		preferenceScores = new HashErrorContextActionDirectory<>();
	}
	
	/**
	 * Inserts a new entry in the qTable.
	 * 
	 * @param errorCode
	 * @param contextId
	 * @param actionId
	 * @param weight
	 */
	public void insertNewErrorCode(Integer errorCode, Integer contextId, hvl.projectparmorel.ml.Action action) {
		preferenceScores.insertNewErrorCode(errorCode, contextId, action.getCode(), new Action(action));
	}
	
	/**
	 * Inserts a new context for the specified error code.
	 * 
	 * @param errorCode
	 * @param contextId
	 * @param action
	 */
	public void addContextToError(Integer errorCode, Integer contextId, hvl.projectparmorel.ml.Action action) {
		preferenceScores.addContextToError(errorCode, contextId, action.getCode(), new Action(action));
	}
	
	/**
	 * Checks that the provided error code is stored in the ErrorMap.
	 * 
	 * @param errorCode to check
	 * @return true if the errorCode is in the ErrorMap, false otherwise.
	 */
	public boolean containsErrorCode(Integer errorCode) {
		return preferenceScores.getErrorMap().containsErrorCode(errorCode);
	}

	/**
	 * Checks that the provided context id is stored in the Context Map for the given error.
	 * 
	 * @param errorCode
	 * @param contextId
	 * @return true if the context ID exists in the map, false otherwise.
	 */
	public boolean errorContainsContext(Integer errorCode, Integer contextId) {
		if(!containsErrorCode(errorCode)) {
			throw new IllegalArgumentException("No data exists on this error code.");
		}
		return preferenceScores.getErrorMap().containsContextIdForErrorCode(errorCode, contextId);
	}
	
	/**
	 * Checks that the provided action Id exists for the specified error code and contect id
	 * 
	 * @param errorCode
	 * @param contextId
	 * @param actionId
	 * @return true if the action ID exists for the specified error code and context ID, false otherwise.
	 */
	public boolean containsActionForErrorAndContext(int errorCode, int contextId, int actionId) {
		if(!errorContainsContext(errorCode, contextId)) {
			throw new IllegalArgumentException("No data exists on this context ID.");
		}
		return preferenceScores.getErrorMap().containsActionIdForErrorCodeAndContextId(errorCode, contextId, actionId);
	}
	
	/**
	 * Adds action to the specified error code and context ID.
	 * 
	 * @param errorCode
	 * @param contextId
	 * @param action
	 */
	public void addAction(int errorCode, int contextId, hvl.projectparmorel.ml.Action action) {
		preferenceScores.addValue(errorCode, contextId, action.getCode(), new Action(action));
	}
	
	/**
	 * Gets the qTable directory.
	 * 
	 * @return the qTalbe
	 */
	protected ErrorContextActionDirectory<Action> getActionDirectory(){
		return preferenceScores;
	}

	/**
	 * Gets the value for the specified error code, context id and action id.
	 * 
	 * @param errorCode
	 * @param contextId
	 * @param actionId
	 * @return the corresponding value
	 */
	protected Action getAction(Integer errorCode, Integer contextId, Integer actionId) {
		return preferenceScores.getValue(errorCode, contextId, actionId);
	}
}
