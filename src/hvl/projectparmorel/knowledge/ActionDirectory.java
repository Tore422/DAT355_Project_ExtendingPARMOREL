package hvl.projectparmorel.knowledge;

public class ActionDirectory {
	ErrorContextActionDirectory<Action> preferenceScores;
	
	protected ActionDirectory() {
		preferenceScores = new HashErrorContextActionDirectory<>();
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
	 * Checks that the provided action Id exists for the specified error code and contect id
	 * 
	 * @param errorCode
	 * @param contextId
	 * @param actionId
	 * @return true if the action ID exists for the specified error code and context ID, false otherwise.
	 */
	public boolean containsActionForErrorAndContext(int errorCode, int contextId, int actionId) {
		return preferenceScores.containsValueForErrorAndContext(errorCode, contextId, actionId);
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
	 * Gets a random action for the specified error
	 * 
	 * @param errorCode
	 * @return a random action
	 */
	public Action getRandomActionForError(int errorCode) {
		return preferenceScores.getRandomValueForError(errorCode);
	}

	/**
	 * Gets the tag dictionary for the specified action
	 * 
	 * @param errorCode
	 * @param contextId
	 * @param actionId
	 * @return the tag dictionary for the action
	 */
	public hvl.projectparmorel.ml.TagDictionary getTagDictionaryForAction(Integer errorCode, Integer contextId, Integer actionId) {
		Action action = getAction(errorCode, contextId, actionId);
		return new hvl.projectparmorel.ml.TagDictionary(action.getTagDictionary().getTagDictionary());
	}

	/**
	 * Sets the value for the specified tag for the specified action in the specified context for the specified error.
	 * 
	 * @param errorCode
	 * @param contextId
	 * @param actionId
	 * @param tag
	 * @param value
	 */
	public void setTagValueInTagDictionary(Integer errorCode, Integer contextId, Integer actionId, int tag, int value) {
		Action action = getAction(errorCode, contextId, actionId);
		action.getTagDictionary().set(tag, value);
	}
	
	/**
	 * Adds action to the specified error code and context ID.
	 * 
	 * @param errorCode
	 * @param contextId
	 * @param action
	 */
	public void setAction(int errorCode, int contextId, Action action) {
		preferenceScores.setValue(errorCode, contextId, action.getCode(), action);
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
