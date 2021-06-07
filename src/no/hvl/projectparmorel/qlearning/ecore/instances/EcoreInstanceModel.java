package no.hvl.projectparmorel.qlearning.ecore.instances;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import no.hvl.projectparmorel.qlearning.ModelType;
import no.hvl.projectparmorel.qlearning.ecore.EcoreModel;

public class EcoreInstanceModel extends EcoreModel {
	public EcoreInstanceModel(ResourceSet resourceSet, Resource model, URI destinationURI) {
		super(resourceSet, model, destinationURI);
	}
	
	@Override
	public ModelType getModelType() {
		return ModelType.ECORE_INSTANCE;
	}
}