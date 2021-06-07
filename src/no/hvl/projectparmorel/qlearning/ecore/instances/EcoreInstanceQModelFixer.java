package no.hvl.projectparmorel.qlearning.ecore.instances;

import no.hvl.projectparmorel.qlearning.*;
import no.hvl.projectparmorel.qlearning.ecore.EcoreActionExtractor;
import no.hvl.projectparmorel.qlearning.ecore.EcoreQModelFixer;
import no.hvl.projectparmorel.qlearning.ecore.EcoreSolution;
import no.hvl.projectparmorel.qlearning.reward.PreferenceOption;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

import java.io.File;
import java.util.List;

public class EcoreInstanceQModelFixer extends EcoreQModelFixer {

	public EcoreInstanceQModelFixer() {
		super();
		resourceSet = new ResourceSetImpl();
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi",
				new XMIResourceFactoryImpl());
		EcorePackage.eINSTANCE.eClass();
	}

	public EcoreInstanceQModelFixer(List<PreferenceOption> preferences) {
		super(preferences);
		resourceSet = new ResourceSetImpl();
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi",
				new XMIResourceFactoryImpl());
	}

	@Override
	protected Model initializeModelFromFile() {
		this.uri = URI.createFileURI(originalModel.getAbsolutePath());
		Resource modelResource = getModel(uri);
		
		return new EcoreInstanceModel(resourceSet, modelResource, uri);
	}

	private Resource getModel(URI uri) {
		return resourceSet.getResource(uri, true);
	}

	@Override
	protected Model getModel(File model) {
		URI episodeModelUri = URI.createFileURI(model.getAbsolutePath());
		Resource episodeModelResource = getModel(episodeModelUri);
		
		return new EcoreInstanceModel(resourceSet, episodeModelResource, uri);
	}

	@Override
	protected void updateRewardCalculator() {
		modelProcessor = new EcoreInstanceModelProcessor(knowledge);
	}

	@Override
	protected QSolution initializeSolution() {
		return new EcoreSolution();
	}

	@Override
	protected ActionExtractor initializeActionExtractor() {
		return new EcoreActionExtractor();
	}

	@Override
	protected ErrorExtractor initializeErrorExtractor() {
		return new EcoreInstanceErrorExtractor();
	}

	@Override
	protected ModelProcessor initializeModelProcessor() {
		return new EcoreInstanceModelProcessor(knowledge);
	}
}
