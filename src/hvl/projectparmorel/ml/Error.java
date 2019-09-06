package hvl.projectparmorel.ml;
/*
 * Angela Barriga Rodriguez - 2019
 * abar@hvl.no
 * Western Norway University of Applied Sciences
 * Bergen - Norway
 */

import java.util.List;

public class Error {

	int code;
	String msg;
	List<?> where;
	int sons;

	public Error() {
		super();
	}

	public Error(int code, String msg, List<?> object, int sons) {
		super();
		this.code = code;
		this.msg = msg;
		this.where = object;
		this.sons = sons;
	}

	@Override
	public String toString() {
		// return "Error " + code + ", msg=" + msg + ", sons=" + sons+", where= "+where
		// +System.getProperty("line.separator");
		return "Error " + code + ", msg=" + msg + System.getProperty("line.separator");

	}

	/**
	 * Gets the error code
	 * 
	 * Error code explanation:
	 * <ul>
	 * <li>2: The generic type associated with the X classifier must not have X
	 * argument(s) when the classifier has 0 type parameter(s)</li>
	 * <li>4: The feature X of Y contains an unresolved proxy Z</li>
	 * <li>6: The lower bound X must be less or equal to the upper bound Y</li>
	 * <li>13: The opposite must be a feature of the reference’s type</li>
	 * <li>14: The opposite of the opposite may not be a reference different from
	 * this one</li>
	 * <li>17: The attribute X is not transient so it must have a data type that is
	 * serializable</li>
	 * <li>20: The generic reference type must not refer to a data type</li>
	 * <li>25: A class that is an interface must also be abstract</li>
	 * <li>27: An operation with void return type must have an upper bound of 1 not
	 * X</li>
	 * <li>28: A container reference must have upper bound of 1 not -1</li>
	 * <li>34: There may not be two operations X and Y with the same signature</li>
	 * <li>38: The default value literal X must be a valid literal of the
	 * attribute’s type</li>
	 * <li>40: The typed element must have a type</li>
	 * <li>44: The name X is not well formed</li>
	 * <li>48: There may not be an operation X with the same signature as an
	 * accessor method for feature Y</li>
	 * <li>50: A containment or bidirectional reference must be unique if its upper
	 * bound is different from 1
	 * </ul>
	 * 
	 * @return the error code
	 */
	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public List<?> getWhere() {
		return where;
	}

	public void setWhere(List<?> where) {
		this.where = where;
	}

	public int getSons() {
		return sons;
	}

	public void setSons(int sons) {
		this.sons = sons;
	}
}
