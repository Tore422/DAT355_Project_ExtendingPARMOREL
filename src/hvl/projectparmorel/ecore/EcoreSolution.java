package hvl.projectparmorel.ecore;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.compare.Comparison;
import org.eclipse.emf.compare.Diff;
import org.eclipse.emf.compare.EMFCompare;
import org.eclipse.emf.compare.scope.DefaultComparisonScope;
import org.eclipse.emf.compare.scope.IComparisonScope;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;

import hvl.projectparmorel.modelrepair.Solution;

public class EcoreSolution extends Solution {
	
	public EcoreSolution() {
		super();
	}

	@Override
	public double calculateDistanceFromOriginal() {
		URI originalUri = URI.createFileURI(getOriginal().getAbsolutePath());
		URI modelUri = URI.createFileURI(getModel().getAbsolutePath());
		
		ResourceSet originalResourceSet = new ResourceSetImpl();
		ResourceSet modelResourceSet = new ResourceSetImpl();
		
		originalResourceSet.getResource(originalUri, true);
		modelResourceSet.getResource(modelUri, true);
		
		IComparisonScope scope = new DefaultComparisonScope(originalResourceSet, modelResourceSet, null);
		Comparison comparison = EMFCompare.builder().build().compare(scope);
		EList<Diff> diff = comparison.getDifferences();
		return diff.size();
	}
}