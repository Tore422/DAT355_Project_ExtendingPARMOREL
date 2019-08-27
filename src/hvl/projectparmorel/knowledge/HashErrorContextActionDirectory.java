package hvl.projectparmorel.knowledge;

public class HashErrorContextActionDirectory<T> extends ErrorContextActionDirectory<T> {
	private ErrorMap<T> errors;
	
	public HashErrorContextActionDirectory() {
		super();
		errors = new ErrorMap<>();
	}

	/**
	 * Clears all the values, setting them to the provided default value.
	 */
	@Override
	public void setAllValuesTo(T defaultValue) {
		errors.setAllValuesTo(defaultValue);
	}
}
