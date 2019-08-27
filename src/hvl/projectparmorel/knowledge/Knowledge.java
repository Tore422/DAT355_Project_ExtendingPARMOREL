package hvl.projectparmorel.knowledge;

public class Knowledge {
	ErrorContextActionDirectory<Double> qTable;
	ErrorContextActionDirectory<Action> preferenceScores;
	
	public Knowledge() {
		qTable = new HashErrorContextActionDirectory<>();
		preferenceScores = new HashErrorContextActionDirectory<>();
	}
}
