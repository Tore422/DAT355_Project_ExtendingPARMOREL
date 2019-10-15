package hvl.projectparmorel.moderrepair;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class Preferences {
	private static final String xmlFilePath = "./preferences.xml";

	private List<Integer> preferences;

	private int[] weights;

	public Preferences() {
		preferences = new ArrayList<Integer>();
		weights = new int[7];
		weights[0] = 2000;
		weights[1] = 2000;
		weights[2] = 150;
		weights[3] = 150;
		weights[4] = 1000;
		weights[5] = 150;
		weights[6] = 150;

		readSettingsFromFile();
	}

	public void setRewardPreference(String name, int weight) {
		if (name.equals("shorterSequencesOfActions")) {
			weights[0] = weight;
			preferences.add(0);
		} else if (name.equals("longerSequencesOfActions")) {
			weights[1] = weight;
			preferences.add(1);
		} else if (name.equals("repairingHigherInTheContextHierarchies")) {
			weights[2] = weight;
			preferences.add(2);
		} else if (name.equals("repairingLowerInTheContextHierarchies")) {
			weights[3] = weight;
			preferences.add(3);
		} else if (name.contentEquals("modificationOfTheOriginalModel")) {
			weights[6] = weight;
			preferences.add(6);
		}
	}

	public void setPunishPreference(String name, int weight) {
		if (name.equals("deletion")) {
			weights[4] = weight;
			preferences.add(4);
		} else if (name.contentEquals("modificationOfTheOriginalModel")) {
			weights[5] = weight;
			preferences.add(5);
		}
	}

	private void readSettingsFromFile() {
		try {
			DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
			Document document = documentBuilder.parse(xmlFilePath);
			document.getDocumentElement().normalize();

			NodeList nodeList = document.getElementsByTagName("preference");

			for (int i = 0; i < nodeList.getLength(); i++) {
				Node settingNode = nodeList.item(i);
				if (settingNode.getNodeType() == Node.ELEMENT_NODE) {
					Element setting = (Element) settingNode;
					int settingNumber = nameToSettningNumber(setting.getAttribute("name"));
					weights[settingNumber] = Integer.parseInt(setting.getAttribute("weight"));
					boolean preferenceSelected = Boolean.getBoolean(setting.getAttribute("preferenceSelected"));
					if (preferenceSelected)
						preferences.add(settingNumber);
				}
			}

		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (IOException io) {
			io.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}
	}

	public void saveToFile() {
		try {
			DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
			Document document = documentBuilder.newDocument();

			// root element
			Element root = document.createElement("settings");
			document.appendChild(root);

			for (int pref = 0; pref < weights.length; pref++) {
				// preference element
				Element preference = document.createElement("preference");
				root.appendChild(preference);
				Attr name = document.createAttribute("name");
				name.setValue(numberToSettningName(pref));
				preference.setAttributeNode(name);

				Attr weight = document.createAttribute("weight");
				weight.setValue(weights[pref] + "");
				preference.setAttributeNode(weight);

				Attr selectedPref = document.createAttribute("preferenceSelected");
				if (preferences.contains(pref)) {
					selectedPref.setValue("true");
				} else {
					selectedPref.setValue("false");
				}

				preference.setAttributeNode(selectedPref);
			}

			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource domSource = new DOMSource(document);
			StreamResult streamResult = new StreamResult(new File(xmlFilePath));

			transformer.transform(domSource, streamResult);
		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (TransformerException tfe) {
			tfe.printStackTrace();
		}
	}

	private String numberToSettningName(int number) {
		switch (number) {
		case 0:
			return "shorterSequencesOfActions";
		case 1:
			return "longerSequencesOfActions";
		case 2:
			return "repairingHigherInTheContextHierarchies";
		case 3:
			return "repairingLowerInTheContextHierarchies";
		case 4:
			return "punishDeletion";
		case 5:
			return "punishModificationOfTheOriginalModel";
		case 6:
			return "rewardModificationOfTheOriginalModel";
		default:
			throw new IllegalArgumentException("Number not within the specified rewards.");
		}
	}

	private int nameToSettningNumber(String number) {
		switch (number) {
		case "shorterSequencesOfActions":
			return 0;
		case "longerSequencesOfActions":
			return 1;
		case "repairingHigherInTheContextHierarchies":
			return 2;
		case "repairingLowerInTheContextHierarchies":
			return 3;
		case "punishDeletion":
			return 4;
		case "punishModificationOfTheOriginalModel":
			return 5;
		case "rewardModificationOfTheOriginalModel":
			return 6;
		default:
			throw new IllegalArgumentException("Number not within the specified rewards.");
		}
	}

	public void deselectPreference(int pref) {
		for (int i = 0; i < preferences.size(); i++) {
			if (preferences.get(i) == pref) {
				preferences.remove(i);
			}
		}
	}

	public int getWeightRewardShorterSequencesOfActions() {
		return weights[0];
	}

	public void setWeightRewardShorterSequencesOfActions(int weightRewardShorterSequencesOfActions) {
		this.weights[0] = weightRewardShorterSequencesOfActions;
	}

	public int getWeightRewardLongerSequencesOfActions() {
		return weights[1];
	}

	public void setWeightRewardLongerSequencesOfActions(int weightRewardLongerSequencesOfActions) {
		this.weights[1] = weightRewardLongerSequencesOfActions;
	}

	public int getWeightRewardRepairingHighInErrorHierarchies() {
		return weights[2];
	}

	public void setWeightRewardRepairingHighInErrorHierarchies(int weightRewardRepairingHighInErrorHierarchies) {
		this.weights[2] = weightRewardRepairingHighInErrorHierarchies;
	}

	public int getWeightRewardRepairingLowInErrorHierarchies() {
		return weights[3];
	}

	public void setWeightRewardRepairingLowInErrorHierarchies(int weightRewardRepairingLowInErrorHierarchies) {
		this.weights[3] = weightRewardRepairingLowInErrorHierarchies;
	}

	public int getWeightPunishDeletion() {
		return weights[4];
	}

	public void setWeightPunishDeletion(int weightPunishDeletion) {
		this.weights[4] = weightPunishDeletion;
	}

	public int getWeightPunishModificationOfTheOriginalModel() {
		return weights[5];
	}

	public void setWeightPunishModificationOfTheOriginalModel(int weightPunishModificationOfTheOriginalModel) {
		this.weights[5] = weightPunishModificationOfTheOriginalModel;
	}

	public int getWeightRewardModificationOfTheOriginalModel() {
		return weights[6];
	}

	public void setWeightRewardModificationOfTheOriginalModel(int weightRewardModificationOfTheOriginalModel) {
		this.weights[6] = weightRewardModificationOfTheOriginalModel;
	}
}
