package hvl.projectparmorel.general;

import java.io.File;
import java.util.List;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;

import hvl.projectparmorel.modelrepair.Solution;

public interface ModelFixer {

	/**
	 * Fixes the model provided as attribute, and stores the repaired model in the
	 * uri-location.
	 * 
	 * @param model
	 * @return the optimal sequence of actions
	 */
	public Solution fixModel(File model);

	/**
	 * Gets the model specified by the uri
	 * 
	 * @param uri
	 * @return the coresponding model
	 */
//	public Resource getModel(URI uri);

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
	 * @param       uri, the Uniform Resource Identifier for the copy
	 * @return a copy
	 */
//	public Resource copy(Resource myMetaModel, URI uri);

	/**
	 * Sets the user preferences used in the algorithm.
	 * 
	 * @param preferences
	 */
	public void setPreferences(List<Integer> preferences);

	/**
	 * Gets the list of possible solutions. Requires that
	 * {@link ModelFixer#fixModel}-method has been called.
	 * 
	 * @return a list of possible solutions.
	 */
	public List<Solution> getPossibleSolutions();
}
