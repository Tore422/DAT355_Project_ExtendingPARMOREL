package hvl.projectparmorel.ml;
/*
 * Angela Barriga Rodriguez - 2019
 * abar@hvl.no
 * Western Norway University of Applied Sciences
 * Bergen - Norway
 */

import java.io.Serializable;
import java.lang.reflect.Method;

public class Action implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	int code;
	String msg;
	SerializableMethod method;
	int hierarchy;
	int subHierarchy;

	public Action() {
		super();
	}

	public Action(int code, String msg, SerializableMethod method, int hierarchy, int subHierarchy) {
		super();
		this.code = code;
		this.msg = msg;
		this.method = method;
		this.hierarchy = hierarchy;
		this.subHierarchy = subHierarchy;
	}

	@Override
	public String toString() {
		return "Action" + code + ", msg=" + msg + "." + System.getProperty("line.separator");
	}

	/**
	 * Gets the context id
	 * 
	 * @return the context ID
	 */
	public int getContextId() {
		if (subHierarchy > -1) {
			return Integer.valueOf(String.valueOf(hierarchy) + String.valueOf(subHierarchy));
		} else {
			return hierarchy;
		}
	}

	/**
	 * Checks if the action is a delete action
	 * 
	 * @return true if the action is a delete action, false otherwise
	 */
	public boolean isDelete() {
		return String.valueOf(code).startsWith("9999");
	}

	/**
	 * Checks if the action handles an error of type "The feature X of Y contains an
	 * unresolved proxy Z", by adding an argument to a generic type.
	 * 
	 * @param error
	 * @return true if the action handles it, false otherwise
	 */
	public boolean handlesMissingArgumentForGenericType(Error error) {
		return String.valueOf(code).startsWith("888") && error.getCode() == 4;
	}

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

	public SerializableMethod getSerializableMethod() {
		return method;
	}

	public void setSerializableMethod(SerializableMethod method) {
		this.method = method;
	}

	public int getHierarchy() {
		return hierarchy;
	}

	public void setHierarchy(int hierarchy) {
		this.hierarchy = hierarchy;
	}

	public int getSubHierarchy() {
		return subHierarchy;
	}

	public void setSubHierarchy(int subHierarchy) {
		this.subHierarchy = subHierarchy;
	}
}
