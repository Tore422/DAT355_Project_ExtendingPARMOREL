package hvl.projectparmorel.knowledge;

abstract class ErrorContextActionDirectory<T> {
	public ErrorContextActionDirectory() {
		
	}
	
	/**
	 * Sets all the values to the provided default value.
	 */
	public abstract void setAllValuesTo(T defaultValue);
}

