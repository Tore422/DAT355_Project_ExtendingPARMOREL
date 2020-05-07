package no.hvl.projectparmorel.qlearning.knowledge;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import no.hvl.projectparmorel.qlearning.Action;
import no.hvl.projectparmorel.qlearning.reward.PreferenceOption;

public class HashErrorContextActionDirectory implements ErrorContextActionDirectory {
	private ErrorMap errors;

	public HashErrorContextActionDirectory() {
		errors = new ErrorMap();
	}
	
	@Override
	public void addAction(Integer errorCode, Integer contextId, Action action) {
		errors.addAction(errorCode, contextId, action);
	}

	@Override
	public Set<Integer> getAllErrorCodes() {
		return errors.getAllErrorCodes();
	}
	
	@Override
	public ErrorMap getErrorMap() {
		return errors;
	}

	@Override
	public Action getOptimalActionForErrorCode(Integer errorCode) {
		return errors.getOptimalActionForErrorCode(errorCode);
	}

	@Override
	public Action getAction(Integer errorCode, Integer contextId, Integer actionId) {
		return errors.getAction(errorCode, contextId, actionId);
	}

	@Override
	public Action getRandomActionForError(int errorCode) {
		return errors.getRandomActionInRandomContextForError(errorCode);
	}

	@Override
	public boolean containsValueForErrorAndContext(int errorCode, int contextId, int actionId) {
		return errors.containsActionForErrorCodeAndContextId(errorCode, contextId, actionId);
	}

	@Override
	public void saveTo(Document document, Element root) {
		errors.saveTo(document, root);		
	}

	@Override
	public void loadFrom(Document document) {
		try {
			errors.loadFrom(document);
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}

	@Override
	public void clearWeights() {
		errors.clearActionWeights();
	}

	@Override
	public void influenceWeightsFromPreferencesBy(double factor, List<PreferenceOption> preferences) {
		errors.influenceActionWeightsFromPreferencesBy(factor, preferences);
	}
}