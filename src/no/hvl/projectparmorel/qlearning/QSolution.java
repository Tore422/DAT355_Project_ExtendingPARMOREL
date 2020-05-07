package no.hvl.projectparmorel.qlearning;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import no.hvl.projectparmorel.Solution;
import no.hvl.projectparmorel.qlearning.reward.RewardCalculator;

public abstract class QSolution extends Solution  implements Comparable<QSolution> {
	private List<AppliedAction> sequence;
	private double weight;
	private RewardCalculator rewardCalculator;

	public QSolution() {
		super();
		weight = 0.0;
		sequence = new ArrayList<>();
	}
	
	public QSolution(int id, List<AppliedAction> seq, double weight, File model) {
		super(id, model);
		this.sequence = seq;
		this.weight = weight;
	}
	
	/**
	 * Awards the solution by boosting all the actions taken
	 * 
	 * @param shouldSave indicates that the knowledge should be saved to file immediately
	 */
	public void reward(boolean shouldSave) {
		rewardCalculator.rewardSolution(this, shouldSave);
	}
	
	@Override
	public int compareTo(QSolution solution) {
		return Double.compare(weight, solution.getWeight());
	}
	
	@Override
	public String toString() {
		return "Sequence [id=" + getId() + ", seq=" + sequence + ", weight=" + weight + "]" + System.getProperty("line.separator")
				+ System.getProperty("line.separator");
	}
	
	/**
	 * Set the reward calculator used to generate the solution
	 * 
	 * @param rewardCalculator
	 */
	public void setRewardCalculator(RewardCalculator rewardCalculator) {
		this.rewardCalculator = rewardCalculator;
	}
	
	public List<AppliedAction> getSequence() {
		return sequence;
	}

	public void setSequence(List<AppliedAction> sequence) {
		this.sequence = sequence;
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}
}
