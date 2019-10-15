package hvl.projectparmorel.moderrepair;

import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;

public interface ModelFixer {

	/**
	 * Fixes the model provided as attribute, and stores the repaired model in the uri-location.
	 * @param model
	 * @param ur
	 * @return the optimal sequence of actions
	 */
	public Sequence fixModel(Resource model, URI uri);

	/**
	 * Gets the model specified by the uri
	 * 
	 * @param uri
	 * @return the coresponding model
	 */
	public Resource getModel(URI uri);
  
	/**
	 * Checks that there exists error in the model
	 * 
	 * @param model
	 * @return true if errors exist in the model, false otherwise
	 */
	public boolean modelIsBroken(Resource model);

	/**
	 * Copies the model passed as a parameter
	 * 
	 * @param model
	 * @param uri, the Uniform Resource Identifier for the copy
	 * @return a copy
	 */
	public Resource copy(Resource myMetaModel, URI uri);

	/**
	 * Sets the user preferences used in the algorithm.
	 * 
	 * @param preferences
	 */
	public void setPreferences(List<Integer> preferences);
}
