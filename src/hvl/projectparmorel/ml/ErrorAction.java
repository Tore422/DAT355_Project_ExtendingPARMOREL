package hvl.projectparmorel.ml;

import hvl.projectparmorel.knowledge.Action;

/**
 * @author Angela Barriga Rodriguez - 2019
 * abar@hvl.no
 * Western Norway University of Applied Sciences
 * Bergen - Norway
 */
public class ErrorAction {

	Error error;
	Action action;

	public ErrorAction(Error error, Action action) {
		super();
		this.error = error;
		this.action = action;
	}

	public Error getError() {
		return error;
	}

	public void setError(Error error) {
		this.error = error;
	}

	public Action getAction() {
		return action;
	}

	public void setAction(Action action) {
		this.action = action;
	}

	@Override
	public String toString() {
		return "ErrorAction [Error: " + error.getCode() + " " + error.getMessage() +", Action: "
				+ action.getMessage() + ", Error hierarchy: " + action.getHierarchy() + ", " + "]"
				+ System.getProperty("line.separator");
	}

	public boolean equals(ErrorAction ea) {
		boolean yes = false;
		if (this.getAction().getCode() == ea.getAction().getCode()
				&& this.getError().getCode() == ea.getError().getCode()
				&& this.getAction().getHierarchy() == ea.getAction().getHierarchy()) {
			yes = true;
		}
		return yes;
	}

}
