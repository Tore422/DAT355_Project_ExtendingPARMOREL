package hvl.projectparmorel.knowledge;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public interface Savable {
	
	/**
	 * Saves the data as a element to the document under the parent element
	 * 
	 * @param document
	 * @param parent
	 */
	void saveTo(Document document, Element parent);
}
