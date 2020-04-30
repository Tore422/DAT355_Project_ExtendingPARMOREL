package hvl.projectparmorel.reward;

public enum PreferenceOption {
	SHORT_SEQUENCES_OF_ACTIONS(0),
	LONG_SEQUENCES_OF_ACTIONS(1),
	REPAIR_HIGH_IN_CONTEXT_HIERARCHY(2),
	REPAIR_LOW_IN_CONTEXT_HIERARCHY(3),
	PUNISH_DELETION(4),
	PUNISH_MODIFICATION_OF_MODEL(5),
	REWARD_MODIFICATION_OF_MODEL(6),
	PREFER_CLOSE_DISTANCE_TO_ORIGINAL(7),
	PREFER_MAINTAINABILITY(8),
	PREFER_UNDERSTANDABILITY(9),
	PREFER_COMPLEXITY(10),
	PREFER_REUSE(11),
	PREFER_RELAXATION(12);
	
	public final int id;
	
	private PreferenceOption(int id) {
		this.id = id;
	}

	/**
	 * Get the preference value from the ID
	 */
	public static PreferenceOption valueOfID(int preferenceID) {
		for(PreferenceOption option : values()) {
			if(option.id == preferenceID) {
				return option;
			}
		}
		return null;
	}
}
