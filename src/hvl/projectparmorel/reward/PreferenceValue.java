package hvl.projectparmorel.reward;

public enum PreferenceValue {
	SHORT_SEQUENCES_OF_ACTIONS(0),
	LONG_SEQUENCES_OF_ACTIONS(1),
	REPAIR_HIGH_IN_CONTEXT_HIERARCHY(2),
	REPAIR_LOW_IN_CONTEXT_HIERARCHY(3),
	PUNISH_DELETION(4),
	PUNISH_MODIFICATION_OF_MODEL(5),
	REWARD_MODIFICATION_OF_MODEL(6);
	
	public final int id;
	
	private PreferenceValue(int id) {
		this.id = id;
	}

	/**
	 * Get the preference value from the ID
	 */
	public static PreferenceValue valueOfID(int preferenceID) {
		for(PreferenceValue value : values()) {
			if(value.id == preferenceID) {
				return value;
			}
		}
		return null;
	}
}
