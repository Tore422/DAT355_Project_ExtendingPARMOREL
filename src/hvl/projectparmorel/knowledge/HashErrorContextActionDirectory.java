package hvl.projectparmorel.knowledge;

import java.util.List;
import java.util.Set;

public class HashErrorContextActionDirectory<T> extends ErrorContextActionDirectory<T> {
	private ErrorMap<T> errors;

	public HashErrorContextActionDirectory() {
		super();
		errors = new ErrorMap<>();
	}

	@Override
	public void setAllValuesTo(T value) {
		errors.setAllValuesTo(value);
	}

	@Override
	public Set<Integer> getAllErrorCodes() {
		return errors.getAllErrorCodes();
	}

	@Override
	public void influenceWeightsByPreferedScores(ErrorContextActionDirectory<Action> preferenceScores,
			List<Integer> preferences) {
		errors.influenceWeightsByPreferedScores(preferenceScores.getErrorMap(), preferences);
	}

	@Override
	protected ErrorMap<T> getErrorMap() {
		return errors;
	}


}
