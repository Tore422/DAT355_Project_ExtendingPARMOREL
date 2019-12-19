package hvl.projectparmorel.ecore;

import java.io.IOException;

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
	
	@Override
	public Object getRepresentation() {
		return model;
	}
	
	@Override
	public Object getRepresentationCopy() {
		modelCopy.getContents().clear();
		modelCopy.getContents().addAll(EcoreUtil.copyAll(model.getContents()));
		return modelCopy;
	}

	@Override
	public void save() {
		try {
			model.save(null);
		} catch (NullPointerException exception) {
			exception.printStackTrace();
		} catch (IOException exception) {
			exception.printStackTrace();
		}
	}
}