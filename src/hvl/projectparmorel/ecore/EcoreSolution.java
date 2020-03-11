package hvl.projectparmorel.ecore;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.common.util.URI;
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
		
		Resource orignalResource = originalResourceSet.getResource(originalUri, false);
		Resource modelResource = modelResourceSet.getResource(modelUri, false);
		
		EObject thisModel = orignalResource.getContents().get(0);
		EObject compareModel = modelResource.getContents().get(0);
		
		double distance = 0;
		try {
			distance = EcoreDistanceCalculator.calculateElementDistance(thisModel, compareModel);
		} catch (Exception e) {
			// something went wrong
		}
		System.out.println("Distance = " + distance);
		return distance;
		
//		URI originalUri = URI.createFileURI(getOriginal().getAbsolutePath());
//		URI modelUri = URI.createFileURI(getModel().getAbsolutePath());
//		
//		ResourceSet originalResourceSet = new ResourceSetImpl();
//		ResourceSet modelResourceSet = new ResourceSetImpl();
//		
//		originalResourceSet.getResource(originalUri, true);
//		modelResourceSet.getResource(modelUri, true);
//		
//		IComparisonScope scope = new DefaultComparisonScope(originalResourceSet, modelResourceSet, null);
////		Comparison comparison = EMFCompare.builder().build().compare(scope);
////		EList<Diff> diff = comparison.getDifferences();
//		
//		Comparison comparison = CompareFactory.eINSTANCE.createComparison();
//		DistanceFunction meter = new EditionDistance();
//		
//		Resource orignalResource = originalResourceSet.getResource(originalUri, false);
//		Resource modelResource = modelResourceSet.getResource(modelUri, false);
//		
//		WeightProvider weightProvider = new EcoreWeightProvider();
//		EditionDistance distanceCalculator = EditionDistance.builder().weightProvider(weightProvider).build();
//		
//		
//		double totalDistance = meter.distance(comparison, orignalResource.getContents().get(0), modelResource.getContents().get(0));
//		CachingDistance cachingDistanceCalculator = new CachingDistance(meter);
//		Comparison comparison2 = CompareFactory.eINSTANCE.createComparison();
//
//		Comparison comparison3 = CompareFactory.eINSTANCE.createComparison();
//		EditionDistance ed = new EditionDistance(WeightProviderDescriptorRegistryImpl.createStandaloneInstance(), EqualityHelperExtensionProviderDescriptorRegistryImpl.createStandaloneInstance() );
//		double altDist3 = ed.distance(comparison3, orignalResource.getContents().get(0), modelResource.getContents().get(0));
//		
//		double dist = cachingDistanceCalculator.distance(comparison2,  orignalResource.getContents().get(0), modelResource.getContents().get(0));
//		//		System.out.println("Calculating distance. Number of differences: " + diff.size());
//		System.out.println("Number of contents:" + orignalResource.getContents().size());
//		System.out.println("Number of new contents:" + modelResource.getContents().size());
//		System.out.println("Distance: " + totalDistance);
//		System.out.println("Alt dsitance: " + dist);
//		System.out.println("Alt dist 2: " + altDist3);
//		
//		Comparison comparison4 = CompareFactory.eINSTANCE.createComparison();
//		EditionDistance editionDistance = new EditionDistance(WeightProviderDescriptorRegistryImpl.createStandaloneInstance(), EqualityHelperExtensionProviderDescriptorRegistryImpl.createStandaloneInstance());
//		CachingDistance cachingDistance = new CachingDistance(editionDistance);
//		double cachdist = cachingDistance.distance(comparison4, orignalResource.getContents().get(0), modelResource.getContents().get(0));
//		System.out.println("Distance: "+ cachdist);
//		
////		WeightProviderDescriptorRegistryImpl.createStandaloneInstance()
////		IEObjectMatcher matcher = DefaultMatchEngine.createDefaultEObjectMatcher(UseIdentifiers.NEVER, new WeightProviderDescriptorRegistryImpl(), EqualityHelperExtensionProviderDescriptorRegistryImpl.createStandaloneInstance());
////		matcher.createMatches(comparison2, leftEObjects, rightEObjects, originEObjects, monitor);
////		for(int i = 0; i < orignalResource.getContents().size(); i++) {
////			EObject originalObject = orignalResource.getContents().get(i);
////			EObject newObject = modelResource.getContents().get(i);
////			
////			System.out.print("Are identic: ");
////			System.out.println(Boolean.toString(distanceCalculator.areIdentic(comparison, originalObject, newObject)));
////			
////			totalDistance += distanceCalculator.distance(comparison, originalObject, newObject);
////			System.out.println("New total distance: " + totalDistance);
////		}
//		System.out.println();
//		return totalDistance; // distanceCalculator.distance(comparison, originalModel, newModel);
//		
////		return diff.size();
	}
}