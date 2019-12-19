package hvl.projectparmorel.ecore;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;

import hvl.projectparmorel.general.Model;

public class EcoreModel implements Model {
	private Resource model;
	private Resource modelCopy;
	
	public EcoreModel(ResourceSet resourceSet, Resource model, URI destinationURI) {
		this.model = model;
		modelCopy = resourceSet.createResource(destinationURI);
	}
	
	public Object getRepresentation() {
		return model;
	}
	
	public Object getRepresentationCopy() {
		modelCopy.getContents().clear();
		modelCopy.getContents().addAll(EcoreUtil.copyAll(model.getContents()));
		return modelCopy;
	}
}