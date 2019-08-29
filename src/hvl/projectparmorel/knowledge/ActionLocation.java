package hvl.projectparmorel.knowledge;

class ActionLocation {
	private Integer contextId;
	private Integer actionId;
	private Integer errorCode;
	
	public ActionLocation(Integer contextId, Integer actionId) {
		this.contextId = contextId;
		this.actionId = actionId;
	}

	public ActionLocation(Integer contextId, Integer actionId, Integer errorCode) {
		this.contextId = contextId;
		this.actionId = actionId;
		this.errorCode = errorCode;
	}
	
	protected Integer getContextId() {
		return contextId;
	}

	protected void setContextId(Integer contextId) {
		this.contextId = contextId;
	}

	protected Integer getActionId() {
		return actionId;
	}

	protected void setActionId(Integer actionId) {
		this.actionId = actionId;
	}

	protected Integer getErrorCode() {
		return errorCode;
	}

	protected void setErrorCode(Integer errorCode) {
		this.errorCode = errorCode;
	}
}
