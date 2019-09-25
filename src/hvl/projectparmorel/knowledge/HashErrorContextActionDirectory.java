package hvl.projectparmorel.knowledge;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class HashErrorContextActionDirectory extends ErrorContextActionDirectory {
	private ErrorMap errors;

	public HashErrorContextActionDirectory() {
		super();
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
	protected ErrorMap getErrorMap() {
		return errors;
	}

	@Override
	protected Action getOptimalActionForErrorCode(Integer errorCode) {
		return errors.getOptimalActionForErrorCode(errorCode);
	}

	@Override
	protected Action getAction(Integer errorCode, Integer contextId, Integer actionId) {
		return errors.getAction(errorCode, contextId, actionId);
	}

	@Override
	protected Action getRandomActionForError(int errorCode) {
		return errors.getRandomActionInRandomContextForError(errorCode);
	}

	@Override
	protected boolean containsValueForErrorAndContext(int errorCode, int contextId, int actionId) {
		return errors.containsActionForErrorCodeAndContextId(errorCode, contextId, actionId);
	}

	@Override
	protected void saveTo(Document document, Element root) {
		errors.saveTo(document, root);		
	}

	@Override
	protected void loadFrom(Document document) {
		try {
			errors.loadFrom(document);
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}

	@Override
	protected void clearWeights() {
		errors.clearActionWeights();
	}

	@Override
	protected void influenceWeightsFromPreferencesBy(double factor, List<Integer> preferences) {
		errors.influenceActionWeightsFromPreferencesBy(factor, preferences);
	}
}