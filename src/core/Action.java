/*
 * Angela Barriga Rodriguez - 2019
 * abar@hvl.no
 * Western Norway University of Applied Sciences
 * Bergen - Norway
 */

package core;

import java.lang.reflect.Method;

public class Action {

	int code;
	String msg;
	Class where;
	Method method;
	int hierarchy;
	int subHierarchy;

	public Action() {
		super();
	}

	public Action(int code, String msg, Class where, Method method, int hierarchy, int subHierarchy) {
		super();
		this.code = code;
		this.msg = msg;
		this.where = where;
		this.method = method;
		this.hierarchy = hierarchy;
		this.subHierarchy = subHierarchy;
	}

	@Override
	public String toString() {
		return "Action"+ code + ", msg=" + msg +"." +System.getProperty("line.separator");
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
	public Class getWhere() {
		return where;
	}
	public void setWhere(Class where) {
		this.where = where;
	}
	public Method getMethod() {
		return method;
	}

	public void setMethod(Method method) {
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
