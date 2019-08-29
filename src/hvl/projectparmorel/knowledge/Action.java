package hvl.projectparmorel.knowledge;

import hvl.projectparmorel.ml.SerializableMethod;

class Action implements Comparable<Action> {
	private TagDictionary tagDictionary;
	int code;
	String msg;
	SerializableMethod method;
	int hierarchy;
	int subHierarchy;
	
	public Action() {
		tagDictionary = new TagDictionary();
	}
	
	public Action(hvl.projectparmorel.ml.Action action) {
		tagDictionary = new TagDictionary();
		code = action.getCode();
		msg = action.getMsg();
		method = action.getSerializableMethod();
		hierarchy = action.getHierarchy();
		subHierarchy = action.getSubHierarchy();
	}

	protected TagDictionary getTagDictionary() {
		return tagDictionary;
	}
	
	//TODO: make sure the action is correct, and not just a string
	@Override
	public String toString() {
		return "ActionExp [action=" + "action" + ", tagsDictionary=" + tagDictionary + "]";
	}

	//TODO: implement comparable or fix the getOptimalActionIndexForErrorCode for the QTable
	@Override
	public int compareTo(Action otherAction) {
		// TODO Auto-generated method stub
		return 0;
	}
}
