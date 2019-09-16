package hvl.projectparmorel.knowledge;

public class TagValuePair implements Comparable<TagValuePair> {
	
	int tagId;
	int value;
	
	public TagValuePair(int tagId, int value) {
		this.tagId = tagId;
		this.value = value;
	}

	@Override
	public int compareTo(TagValuePair o) {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getValue() {
		return value;
	}

}
