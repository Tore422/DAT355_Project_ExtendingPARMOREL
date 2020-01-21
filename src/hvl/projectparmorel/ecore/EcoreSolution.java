package hvl.projectparmorel.ecore;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.compare.Comparison;
import org.eclipse.emf.compare.Diff;
import org.eclipse.emf.compare.EMFCompare;
import org.eclipse.emf.compare.match.eobject.CachingDistance;
import org.eclipse.emf.compare.match.eobject.EcoreWeightProvider;
import org.eclipse.emf.compare.match.eobject.EditionDistance;
import org.eclipse.emf.compare.match.eobject.WeightProvider;
import org.eclipse.emf.compare.scope.DefaultComparisonScope;
import org.eclipse.emf.compare.scope.IComparisonScope;
import org.eclipse.emf.ecore.EObject;
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
		
		Resource orignalResource = originalResourceSet.getResource(originalUri, false);
		Resource modelResource = modelResourceSet.getResource(modelUri, false);
		
		WeightProvider weightProvider = new EcoreWeightProvider();
		EditionDistance distanceCalculator = EditionDistance.builder().weightProvider(weightProvider).build();
//		CachingDistance cachingDistanceCalculator = new CachingDistance();
		
		double totalDistance = 0;
		System.out.println("Calculating distance. Number of differences: " + diff.size());
		System.out.println("Number of contents:" + orignalResource.getContents().size());
		System.out.println("Number of new contents:" + modelResource.getContents().size());
		for(int i = 0; i < orignalResource.getContents().size(); i++) {
			EObject originalObject = orignalResource.getContents().get(i);
			EObject newObject = modelResource.getContents().get(i);
			
			System.out.print("Are identic: ");
			System.out.println(Boolean.toString(distanceCalculator.areIdentic(comparison, originalObject, newObject)));
			
			totalDistance += distanceCalculator.distance(comparison, originalObject, newObject);
			System.out.println("New total distance: " + totalDistance);
		}
		System.out.println();
		return totalDistance; // distanceCalculator.distance(comparison, originalModel, newModel);
		
//		return diff.size();
	}
}