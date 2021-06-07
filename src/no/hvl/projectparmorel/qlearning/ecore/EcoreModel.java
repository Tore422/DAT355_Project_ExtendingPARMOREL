package no.hvl.projectparmorel.qlearning.ecore;

import java.io.IOException;
import java.util.Collection;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil.Copier;

import no.hvl.projectparmorel.qlearning.Model;
import no.hvl.projectparmorel.qlearning.ModelType;

public class EcoreModel implements Model {
	protected Resource model;
	protected Resource modelCopy;
	
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
	
		Copier copier = new Copier();
		Collection<EObject> contents = copier.copyAll(model.getContents());
		modelCopy.getContents().addAll(contents);
		
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

	@Override
	public ModelType getModelType() {
		return ModelType.ECORE;
	}
}