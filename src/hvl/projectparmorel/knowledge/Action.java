package hvl.projectparmorel.knowledge;

class Action {
	private TagDictionary tagDictionary;
	
	public Action() {
		tagDictionary = new TagDictionary();
	}
	
	protected TagDictionary getTagDictionary() {
		return tagDictionary;
	}
}
