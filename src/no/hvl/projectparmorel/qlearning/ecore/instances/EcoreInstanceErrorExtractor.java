package no.hvl.projectparmorel.qlearning.ecore.instances;

import no.hvl.projectparmorel.qlearning.ModelType;
import no.hvl.projectparmorel.qlearning.ecore.EcoreErrorExtractor;

public class EcoreInstanceErrorExtractor extends EcoreErrorExtractor {

	public EcoreInstanceErrorExtractor() {
		super();
		this.unsuportedErrorCodes = ModelType.ECORE_INSTANCE.getUnsupportedErrorCodes();
	}
}
