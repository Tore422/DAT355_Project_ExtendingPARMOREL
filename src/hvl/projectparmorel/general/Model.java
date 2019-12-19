package hvl.projectparmorel.general;

public interface Model {
	/**
	 * Gets the model representation.
	 * 
	 * Warning: altering this representation might cause changes to the original
	 * model.
	 * 
	 * @return the model representation
	 */
	public Object getRepresentation();

	/**
	 * Gets a copy of the model representation. This can safely be altered without
	 * causing changes to the original model.
	 * 
	 * @return a copy of the model representation
	 */
	public Object getRepresentationCopy();
	
	/**
	 * Saves what is stored in the representation.
	 */
	public void save();
}
