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
		return "Error " + code + ", msg=" + msg + ", sons=" + sons+", where= "+where +System.getProperty("line.separator");
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
