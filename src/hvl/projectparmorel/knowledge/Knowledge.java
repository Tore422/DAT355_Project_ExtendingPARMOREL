package hvl.projectparmorel.knowledge;

import java.io.File;
import java.io.IOException;
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

/**
 * Represents the algorithms knowledge.
 * 
 * @author Angela Barriga Rodriguez
 * @author Magnus Marthinsen
 */
public class Knowledge {
	private Logger logger = Logger.getGlobal();
	private final String knowledgeFileName = "knowledge.xml";
	private QTable actionDirectory;

	public Knowledge() {
		actionDirectory = new QTable();
	}
//
//	/**
//	 * Adds 20% of the scores set in the preferences to the QTable. We only add 20
//	 * %, so we don't influence the scores to much. This allows for new learnings to
//	 * be acquired.
//	 * 
//	 * @param the preferences to influence. Only these preferences will be affected.
//	 */
//	public void influenceQTableFromPreferenceScores(List<Integer> preferences) {
//		ErrorContextActionDirectory preferenceScores = actionDirectory.getActionDirectory();
//		preferenceScores.influenceWeightsByPreferedScores(preferenceScores, preferences);
//	}

	/**
	 * Gets the action directory
	 * 
	 * @return the action directory
	 */
	public QTable getActionDirectory() {
		return actionDirectory;
	}

	/**
	 * Gets the optimal action for the specified error code.
	 * 
	 * @param errorCode
	 * @return the action for the specified error code with the highest weight.
	 */
	public Action getOptimalActionForErrorCode(Integer errorCode) {
		return actionDirectory.getOptimalActionForErrorCode(errorCode);
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
			logger.info("document created");

			Element root = document.createElement("knowledge");
			document.appendChild(root);

			actionDirectory.saveTo(document, root);

			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource domSource = new DOMSource(document);
			StreamResult streamResult = new StreamResult(new File(knowledgeFileName));
			transformer.transform(domSource, streamResult);
			logger.info("Saving completed");
		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (TransformerException tfe) {
			tfe.printStackTrace();
		}
	}

	public void load() {
		logger.info("Loading initialized");
		try {
			File fXmlFile = new File(knowledgeFileName);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
			doc.getDocumentElement().normalize();

			logger.info("Root found: " + doc.getDocumentElement().getNodeName());
			actionDirectory.loadFrom(doc);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}
	}
}
