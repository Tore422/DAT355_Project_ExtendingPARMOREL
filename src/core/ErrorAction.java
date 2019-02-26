/*
 * Angela Barriga Rodriguez - 2019
 * abar@hvl.no
 * Western Norway University of Applied Sciences
 * Bergen - Norway
 */


package core;

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
		return "ErrorAction [error=" + error.getCode() + " " + error.getMsg() + ", action=" + action.getCode() + " "
				+ action.getMsg() + " " + action.getHierarchy() + " " + action.getSubHierarchy() + "]"
				+ System.getProperty("line.separator");
	}

	public boolean equals(ErrorAction ea) {
		boolean yes = false;
		if (this.getAction().getCode() == ea.getAction().getCode()
				&& this.getError().getCode() == ea.getError().getCode()
				&& this.getAction().getHierarchy() == ea.getAction().getHierarchy()
				&& this.getAction().getSubHierarchy() == ea.getAction().getSubHierarchy()) {
			yes = true;
		}
		return yes;
	}

}
