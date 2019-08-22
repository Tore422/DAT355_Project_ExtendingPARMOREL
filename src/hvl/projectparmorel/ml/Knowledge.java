package hvl.projectparmorel.ml;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents the algorithms knowledge.
 * 
 * @author Angela Barriga Rodriguez, Magnus Marthinsen
 */
public class Knowledge {
	private String knowledgeFilePath = "././knowledge.properties";
	private Experience experience;
	
	private static List<Integer> preferences;
	
	public Knowledge(List<Integer> preferences) {
		Knowledge.preferences = preferences;
		experience = loadKnowledge();
	}
	
	/**
	 * Loads the knowledge
	 */
	private Experience loadKnowledge() {
		Experience newExperience = new Experience();
		Experience oldXp = loadKnowledgeFromFile();
		if (oldXp.getActionsDictionary().size() > 0) {
			// copy structure of qtable with values to 0
			newExperience.setqTable(normalizeQTable(oldXp.getqTable()));
			// copy actions dictionary (actions + old rewards)
			newExperience.setActionsDictionary(oldXp.getActionsDictionary());
			// if tags coincide, introduce in qtable rewards*coef 0,2
			insertTags(newExperience);
		}
		return newExperience;
	}

	/**
	 * Loads the knowledge from file if it exists.
	 * 
	 * @return Experience from file if it exists. Otherwise, it returns a new Experience.
	 */
	private Experience loadKnowledgeFromFile() {
		Experience oldExperience = new Experience();

		FileInputStream knowledgeFile = null;
		BufferedInputStream inputStream = null;
		ObjectInput objectInputStream = null;
		try {
			knowledgeFile = new FileInputStream(knowledgeFilePath);
			inputStream = new BufferedInputStream(knowledgeFile);
			objectInputStream = new ObjectInputStream(inputStream);

			oldExperience = ((Experience) objectInputStream.readObject());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (objectInputStream != null)
					objectInputStream.close();
				if (inputStream != null)
					inputStream.close();
				if (knowledgeFile != null)
					knowledgeFile.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		return oldExperience;
	}

	/**
	 * Normalizes the Q-table, putting zero for all the values.
	 * 
	 * @param qTable
	 * @return QTable with all zero values.
	 */
	private Map<Integer, HashMap<Integer, HashMap<Integer, Double>>> normalizeQTable(
			Map<Integer, HashMap<Integer, HashMap<Integer, Double>>> qTable) {
		for (Integer key : qTable.keySet()) {
			for (Integer key2 : qTable.get(key).keySet()) {
				for (Integer key3 : qTable.get(key).get(key2).keySet()) {
					qTable.get(key).get(key2).put(key3, 0.0);
				}
			}
		}
		return qTable;
	}

	// TODO: Refractor! Move inside experience?
	private void insertTags(Experience experience) {
		for (Integer key : experience.getActionsDictionary().keySet()) { // error
			for (Integer key2 : experience.getActionsDictionary().get(key).keySet()) { // where
				for (Integer keyact : experience.getActionsDictionary().get(key).get(key2).keySet()) { // action
					for (Integer key3 : experience.getActionsDictionary().get(key).get(key2).get(keyact).getTagsDictionary()
							.keySet()) { // tag
						if (preferences.contains(key3)) {
							double value = experience.getActionsDictionary().get(key).get(key2).get(keyact)
									.getTagsDictionary().get(key3);
							value *= 0.2;
							value = experience.getqTable().get(key).get(key2).get(keyact) + value;
							experience.getqTable().get(key).get(key2).put(keyact, value);
							
							// Due to the transfer we wont need as many episodes of randomness
							QLearning.N_EPISODES = 12;
							QLearning.randomfactor = 0.15;
						}
					}
				}
			}
		}
	}
	
	/**
	 * Saves the experience
	 */
	public void save() {
		try {
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(knowledgeFilePath));
			oos.writeObject(experience);
			oos.flush();
			oos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Gets the experience
	 * 
	 * @return the experience
	 */
	public Experience getExperience() {
		return experience;
	}
}
