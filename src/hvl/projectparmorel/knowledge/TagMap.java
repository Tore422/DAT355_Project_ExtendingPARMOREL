package hvl.projectparmorel.knowledge;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import hvl.projectparmorel.ml.ErrorAction;

public class TagMap {
	private ErrorContextActionDirectory<TagValueMap> tagMap;
	
	public TagMap() {
		tagMap = new HashErrorContextActionDirectory<>();
	}
	
	/**
	 * Checks that the specified error code and context ID contains the specified
	 * action ID
	 * 
	 * @param errorCode
	 * @param contextId
	 * @param actionId
	 * @return true if the action ID is found for the specified error and context,
	 *         false otherwise
	 */
	public boolean containsTagIdForErrorCodeAndContextIdAndAction(int errorCode, int contextId, int actionId, int tagId) {
		if(tagMap.getErrorMap().containsValueForErrorCodeAndContextId(errorCode, contextId, actionId)) {
			TagValueMap tagValueMap = tagMap.getErrorMap().getValue(errorCode, contextId, actionId);
			return tagValueMap.containsTagId(tagId);
		}
		return false;
	}
	
	/**
	 * Sets the tag for the specified action in the specified context for the
	 * specified error. If the error, context or action is not in the hierarchy,
	 * they will be added.
	 * 
	 * @param errorCode
	 * @param contextId
	 * @param actionId
	 * @param weight
	 */
	public void setTag(Integer errorCode, Integer contextId, Integer actionId, Integer tagId, Integer value) {
		if(tagMap.getErrorMap().containsValueForErrorCodeAndContextId(errorCode, contextId, actionId)) {
			tagMap.getErrorMap().getValue(errorCode, contextId, actionId).set(tagId, value);
		} else {
			tagMap.getErrorMap().setValue(errorCode, contextId, actionId, new TagValueMap(tagId, value));
		}
	}
	
	/**
	 * Gets the tag for the specified action for the specified context for the
	 * specified error.
	 * 
	 * @param errorCode
	 * @param contextId
	 * @param actionId
	 * @return the tag
	 */
//	public int getTag(Integer errorCode, Integer contextId, Integer actionId, tagId) {
//		if(errorContextActionDirectory.getErrorMap().containsValueForErrorCodeAndContextId(errorCode, contextId, actionId)) {
//			errorContextActionDirectory.getErrorMap().getValue(errorCode, contextId, actionId).get();
//		}
//	}
	
	public void updateRewardInActionDirectory(QTable actionDirectory, ErrorAction errorAction, int contextId) {
		int errorCode = errorAction.getError().getCode();
		int actionId = errorAction.getAction().getCode();

		if(tagMap.containsValueForErrorAndContext(errorCode, contextId, actionId)) {
			for(Integer key : tagMap.getValue(errorCode, contextId, actionId).keySet()) {
				Action action = actionDirectory.getAction(errorCode, contextId, actionId);
				if(action.getTagDictionary().contains(key)) {
					int newTagValue = actionDirectory.getTagDictionaryForAction(errorCode, contextId, actionId)
							.getTagDictionary().get(key)
							+ tagMap.getValue(errorCode, contextId, actionId).get(key);
					actionDirectory.setTagValueInTagDictionary(errorCode, contextId, actionId, key, newTagValue);
				} else {
					int newTagValue = tagMap.getValue(errorCode, contextId, actionId).get(key);
					actionDirectory.setTagValueInTagDictionary(errorCode, contextId, actionId, key, newTagValue);
				}
			}
		}
	}

	private class TagValueMap implements Comparable<TagValueMap> {
		
		private Map<Integer, Integer> tagValueMap;
		
		private TagValueMap() {
			tagValueMap = new HashMap<>();
		}

		/**
		 * Gets the value for the specified tag id
		 * 
		 * @param tagId
		 * @return the corresponding value
		 */
		public Integer get(Integer tagId) {
			return tagValueMap.get(tagId);
		}

		private TagValueMap(int tagId, int value) {
			tagValueMap = new HashMap<>();
			tagValueMap.put(tagId, value);
		}
		
		/**
		 * Adds the tag id and value to the map
		 * 
		 * @param tagId
		 * @param value
		 */
		private void set(Integer tagId, Integer value) {
			if(tagValueMap.containsKey(tagId)) {
				tagValueMap.put(tagId, tagValueMap.get(tagId) + value);
			} else {
				tagValueMap.put(tagId, value);
			}
		}

		/**
		 * Checks that the tag exists in the map
		 * 
		 * @param tagId
		 * @return true if the tag id exists, false otherwise
		 */
		private boolean containsTagId(int tagId) {
			return tagValueMap.containsKey(tagId);
		}
		
		private Set<Integer> keySet(){
			return tagValueMap.keySet();
		}

		@Override
		public int compareTo(TagValueMap o) {
			// TODO Auto-generated method stub
			return 0;
		}
	}	
}