package hvl.projectparmorel.ml;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

//TODO: Refactor to errors or similar?
public class ExperienceMap implements Serializable {

	/**
	 * A map containing the context for the given errors.
	 */
	private Map<Integer, ContextMap> errors;
	
	private static final long serialVersionUID = 1L;
	Map<Integer, HashMap<Integer, HashMap<Integer, Double>>> qTable = new HashMap<Integer, HashMap<Integer, HashMap<Integer, Double>>>();
	Map<Integer, HashMap<Integer, HashMap<Integer, ActionExp>>> actionsDictionary = new HashMap<Integer, HashMap<Integer, HashMap<Integer, ActionExp>>>();

	public ExperienceMap() {
		super();
		errors = new HashMap<>();
	}
	
	public ContextMap getContextForError(int errorCode) {
		return errors.get(errorCode);
	}
	
	public void influenceQTableFromActionTable() {
//		Collection<Context> allContexts = getContextsForAllErrorCodes();
//		for(Context context : allContexts) {
//			Collection<ActionExp> actions = context.getAllActions();
//			for(ActionExp action : actions) {
//				System.out.println();
//			}
//		}
		
		
		Set<Integer> errorCodes = getAllErrorCodes();
		for(Integer errorCode : errorCodes) { //for (Integer key : getNewXp().getActionsDictionary().keySet()) { // error
					
			ContextMap contextMap = errors.get(errorCode);
			Set<Integer> allContextIds = contextMap.getAlLContextIds(); 
			
			for (Integer contextId : allContextIds) { // for (Integer key2 : getNewXp().getActionsDictionary().get(key).keySet()) { // where
				
				ActionMap actionMap = contextMap.getActionForContext(contextId);
				Set<Integer> allActionIds = actionMap.getAllActionIds();
				
				for(Integer actionId : allActionIds) {
					ActionExp action = actionMap.getAction(actionId);
					Set<Integer> allTagIds = actionMap.getAllActionIds();
					
					Map<Integer, Integer> tagsDictionary = action.getTagsDictionary();
					for(Integer tagId : allTagIds) {
						double value = tagsDictionary.get(tagId) * 0.2;
						value += qTable.get(errorCode).get(contextId).get(actionId);
						qTable.get(errorCode).get(contextId).put(actionId, value);
					}
					     
				}
			}
		}
	}
	
	private Set<Integer> getAllErrorCodes(){
		return errors.keySet();
	}

	public ExperienceMap(Map<Integer, HashMap<Integer, HashMap<Integer, Double>>> qTable,
			Map<Integer, HashMap<Integer, HashMap<Integer, ActionExp>>> actionsDictionary) {
		super();
		this.qTable = qTable;
		this.actionsDictionary = actionsDictionary;
	}

	public Map<Integer, HashMap<Integer, HashMap<Integer, Double>>> getqTable() {
		return qTable;
	}

	public void setqTable(Map<Integer, HashMap<Integer, HashMap<Integer, Double>>> qTable) {
		this.qTable = qTable;
	}

	public Map<Integer, HashMap<Integer, HashMap<Integer, ActionExp>>> getActionsDictionary() {
		return actionsDictionary;
	}

	public void setActionsDictionary(Map<Integer, HashMap<Integer, HashMap<Integer, ActionExp>>> actionsDictionary) {
		this.actionsDictionary = actionsDictionary;
	}




}
