package hvl.projectparmorel.general;

public interface Model {
	/**
	 * Gets the model representation
	 * 
	 * @return the model representation
	 */
	public Object getRepresentation();

	/**
	 * Gets a copy of the model representation
	 * 
	 * @return a copy of the model representation
	 */
	public Object getRepresentationCopy();
}
