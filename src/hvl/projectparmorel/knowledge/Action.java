package hvl.projectparmorel.knowledge;

class Action {
	private TagDictionary tagDictionary;
	
	public Action() {
		tagDictionary = new TagDictionary();
	}
	
	protected TagDictionary getTagDictionary() {
		return tagDictionary;
	}
	
	//TODO: make sure the action is correct, and not just a string
	@Override
	public String toString() {
		return "ActionExp [action=" + "action" + ", tagsDictionary=" + tagDictionary + "]";
	}
}
