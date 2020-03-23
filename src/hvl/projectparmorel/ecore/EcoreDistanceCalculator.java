package hvl.projectparmorel.ecore;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;

public class EcoreDistanceCalculator {
	
	
	public static double calculateElementDistance(EObject model, EObject targetModel) throws Exception {
		double distance_value = 0.0;

		Set<Object> src_Objects = getAllObjectIds(model);
		Set<Object> tar_Objects = getAllObjectIds(targetModel);
		double total = src_Objects.size() + tar_Objects.size();

		// Clone target set
		Set<Object> clone = new HashSet<Object>(tar_Objects); // shallow copy
		tar_Objects.removeAll(src_Objects); // added objects
		src_Objects.removeAll(clone); // removed objects

		distance_value = (tar_Objects.size() + src_Objects.size()) / total;
		return distance_value;
	}

	/**
	 * Returns the id of all objects in the model.
	 * @param root is the root object of the model from which we can access all the objects in the model
	 * @return list of all ids
	 */
	private static Set<Object> getAllObjectIds(EObject root) {
		return getAllObjects(root).stream().map(
				e -> getId(e)).collect(Collectors.toSet());
	}
	
	/**
	 * Returns the unique identifier that characterizes a position or movable object.
	 * By default, it returns the object's ID as a String.
	 * @param object a position or movable object
	 * @return the identifier value, null if not found
	 * @see org.eclipse.emf.ecore.util.EcoreUtil#getID()
	 */
	private static Object getId(EObject object) {
		return EcoreUtil.getID(object);
	}
	
	/**
	 * Returns all objects in the model.
	 * @param root is the root object of the model from which we can access all the objects in the model
	 * @return set of all objects
	 */
	private static Set<EObject> getAllObjects(EObject root) {
		HashSet<EObject> collection = new HashSet<EObject>();
		List<EObject> list = getObjects(root);
		collection.addAll(list);
		return collection;
	}
	
	public static List<EObject> getObjects(EObject root) {
		if (root.eContainer() != null) {
			root = EcoreUtil.getRootContainer(root);
		}
		return root.eContents();
	}
}
