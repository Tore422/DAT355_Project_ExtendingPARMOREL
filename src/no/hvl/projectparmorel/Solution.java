package no.hvl.projectparmorel;

import java.io.File;

import no.hvl.projectparmorel.exceptions.DistanceUnavailableException;

/**
 * @author Magnus Marthinsen
 * @author Angela Barriga Rodriguez, abar@hvl.no 
 * 2019 
 * Western Norway University of Applied Sciences Bergen - Norway
 */
public abstract class Solution {
	private int id;
	private File model;
	private File original;

	public Solution() {
		super();
	}

	public Solution(int id, File model) {
		super();
		this.id = id;
		this.model = model;
	}
	
	/**
	 * Calculates the difference between the solution and the original model.
	 * 
	 * @return the calculated distance. Returns -1 if the distance could not be calculated.
	 * @throws DistanceUnavailableException if something goes wrong with the calculation or if the method is not implemented.
	 */
	public abstract double calculateDistanceFromOriginal()throws DistanceUnavailableException;
	
	/**
	 * Calculates the following metrics: maintainability, understandability, complexity,
	 * reuse and relaxation index.
	 * 
	 * @return the calculated metric. Returns -1 if the metric could not be calculated.
	 */
	
	public abstract double calculateMaintainability();
	public abstract double calculateUnderstandability();
	public abstract double calculateComplexity();
	public abstract double calculateReuse();
	public abstract double calculateRelaxation();
	
	/**
	 * Deletes the associated file;
	 */
	public void discard() {
		model.delete();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setModel(File model) {
		this.model = model;
	}

	public File getModel() {
		return model;
	}

	/**
	 * Set the original model
	 * 
	 * @param originalModel
	 */
	public void setOriginal(File originalModel) {
		original = originalModel;
	}
	
	/**
	 * Gets the original file
	 * @return the original model
	 */
	public File getOriginal() {
		return original;
	}
}
