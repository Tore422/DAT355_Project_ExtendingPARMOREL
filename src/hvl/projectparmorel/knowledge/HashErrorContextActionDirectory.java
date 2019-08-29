package hvl.projectparmorel.knowledge;

import java.util.List;
import java.util.Random;
import java.util.Set;

public class HashErrorContextActionDirectory<T extends Comparable<T>> extends ErrorContextActionDirectory<T> {
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
	public void insertNewErrorCode(Integer errorCode, Integer contextId, Integer actionId, T value) {
		errors.insertNewErrorCode(errorCode, contextId, actionId, value);
	}
	
	@Override
	protected void addContextToError(Integer errorCode, Integer contextId, Integer actionId, T value) {
		errors.insertNewContext(errorCode, contextId, actionId, value);
	}
	
	@Override
	protected void addValue(int errorCode, int contextId, int actionId, T value) {
		errors.insertNewAction(errorCode, contextId, actionId, value);
		
	}
	
	@Override
	protected ErrorMap<T> getErrorMap() {
		return errors;
	}

	@Override
	protected void updateValue(Integer errorCode, Integer contextId, Integer actionId, T value) {
		errors.updateValue(errorCode, contextId, actionId, value);
	}

	@Override
	protected ActionLocation getOptimalActionIndexForErrorCode(Integer errorCode) {
		return errors.getOptimalActionIndexForErrorCode(errorCode);
	}

	@Override
	protected T getValue(Integer errorCode, Integer contextId, Integer actionId) {
		return errors.getValue(errorCode, contextId, actionId);
	}

	@Override
	protected T getRandomValueForError(int errorCode) {
		Random randomGenerator = new Random();
		int randomContext = randomGenerator.nextInt(errors.getNumberOfContextsForError(errorCode));
		return errors.getRandomValueForErrorInContext(randomContext);
	}
}