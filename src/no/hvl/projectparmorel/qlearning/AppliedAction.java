package no.hvl.projectparmorel.qlearning;

/**
 * @author Angela Barriga Rodriguez - 2019 abar@hvl.no Western Norway University
 *         of Applied Sciences Bergen - Norway
 */
public class AppliedAction {

	private Error error;
	private Action action;

	public AppliedAction(Error error, Action action) {
		super();
		this.error = error;
		this.action = action;
	}

	/**
	 * Gets the error the action was applied to fix
	 * 
	 * @return the error
	 */
	public Error getError() {
		return error;
	}

	/**
	 * Sets the error the action was applied to fix
	 * 
	 * @param error
	 */
	public void setError(Error error) {
		this.error = error;
	}

	/**
	 * Gets the action that was applied
	 * 
	 * @return the applied action
	 */
	public Action getAction() {
		return action;
	}

	/**
	 * Sets the action that was applied
	 * 
	 * @param action
	 */
	public void setAction(Action action) {
		this.action = action;
	}

	@Override
	public String toString() {
		return "ErrorAction [Error: " + error.getCode() + " " + error.getMessage() + ", Action: " + action.getName()
				+ ", Error hierarchy: " + action.getContextId() + "]" + System.getProperty("line.separator");
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof AppliedAction) {
			AppliedAction otherAplpiedAction = (AppliedAction) other;
			if (this.getAction().getId() == otherAplpiedAction.getAction().getId()
					&& this.getError().getCode() == otherAplpiedAction.getError().getCode()
					&& this.getAction().getContextId() == otherAplpiedAction.getAction().getContextId()) {
				return true;
			}
		}
		return false;
	}

//	public boolean equals(AppliedAction ea) {
//		boolean yes = false;
//		if (this.getAction().getCode() == ea.getAction().getCode()
//				&& this.getError().getCode() == ea.getError().getCode()
//				&& this.getAction().getHierarchy() == ea.getAction().getHierarchy()) {
//			yes = true;
//		}
//		return yes;
//	}

}
