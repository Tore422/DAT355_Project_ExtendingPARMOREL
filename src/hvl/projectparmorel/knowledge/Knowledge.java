package hvl.projectparmorel.knowledge;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import hvl.projectparmorel.exceptions.UnsupportedErrorException;
import hvl.projectparmorel.general.Action;

/**
 * Represents the algorithms knowledge.
 * 
 * @author Angela Barriga Rodriguez
 * @author Magnus Marthinsen
 */
public class Knowledge {
	private Logger logger;
	public static final String KNOWLEDGE_FILE_NAME = "knowledge.xml";
	private QTable qTable;

	public Knowledge() {
		logger = Logger.getLogger("MyLog");
		qTable = new QTable();
	}

	/**
	 * Gets the action directory
	 * 
	 * @return the action directory
	 */
	public QTable getQTable() {
		return qTable;
	}

	/**
	 * Gets the optimal action for the specified error code.
	 * 
	 * @param errorCode
	 * @return the action for the specified error code with the highest weight.
	 * @throws UnsupportedErrorException if the error code is not in the Q-table
	 */
	public Action getOptimalActionForErrorCode(Integer errorCode) throws UnsupportedErrorException {
		if(qTable.containsErrorCode(errorCode)) {
			return qTable.getOptimalActionForErrorCode(errorCode);
		} else {
			logger.warning("Error code " + errorCode + " is not in the Q-table.");
			throw new UnsupportedErrorException("The error code " + errorCode + " was not in the QTable.", errorCode);
		}
	}

	/**
	 * Saves the knowledge to file.
	 */
	public void save() {
		logger.info("Saving initialized");
		try {
			DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
			Document document = documentBuilder.newDocument();
			logger.info("document prepared");

			Element root = document.createElement("knowledge");
			document.appendChild(root);

			qTable.saveTo(document, root);

			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource domSource = new DOMSource(document);
			File file = new File(KNOWLEDGE_FILE_NAME);
			StreamResult streamResult = new StreamResult(file);
			transformer.transform(domSource, streamResult);
			logger.info("Saving completed to " + file.getAbsolutePath());
		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (TransformerException tfe) {
			tfe.printStackTrace();
		}
	}

	/**
	 * Loads the knowledge
	 * @return true if the file is successfully loaded, false otherwise 
	 */
	public boolean load() {
		try {
			logger.info("Loading initialized");
			File fXmlFile = new File(KNOWLEDGE_FILE_NAME);
			logger.info("File created: " + fXmlFile.getAbsolutePath());
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
			doc.getDocumentElement().normalize();

			logger.info("Root found: " + doc.getDocumentElement().getNodeName());
			qTable.loadFrom(doc);
			return true;
		} catch (FileNotFoundException e) {
			logger.info("File not found");
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Sets all the weights in the q-table to zero.
	 */
	public void clearWeights() {
		qTable.clearWeights();
	}

	/**
	 * Influences the weights in the q-table from the preferences and previous learning by the specified factor.
	 * 
	 * @param factor
	 * @param preferences 
	 */
	public void influenceWeightsFromPreferencesBy(double factor, List<Integer> preferences) {
		qTable.influenceWeightsFromPreferencesBy(factor, preferences);		
	}
}
