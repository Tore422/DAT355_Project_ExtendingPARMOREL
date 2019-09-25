package hvl.projectparmorel.ml;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;

/**
 * @author Angela Barriga Rodriguez - 2019 abar@hvl.no Western Norway University
 *         of Applied Sciences Bergen - Norway
 */
public class Sequence {
	private int id;
	private List<AppliedAction> sequence;
	private double weight;
	private Resource model;
	private ResourceSet resourceSet;
	private URI uri;

	public Sequence() {
		super();
		sequence = new ArrayList<AppliedAction>();
		weight = 0.0;
		resourceSet = new ResourceSetImpl();
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("ecore", new EcoreResourceFactoryImpl());
	}

	public Sequence(int id, List<AppliedAction> seq, double weight, URI u) {
		super();
		this.id = id;
		this.sequence = seq;
		this.weight = weight;
		this.uri = u;
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("ecore", new EcoreResourceFactoryImpl());
		this.model = resourceSet.createResource(this.uri);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public List<AppliedAction> getSequence() {
		return sequence;
	}

	public void setSequence(List<AppliedAction> sequence) {
		this.sequence = sequence;
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

	@Override
	public String toString() {
		return "Sequence [id=" + id + ", seq=" + sequence + ", weight=" + weight + "]" + System.getProperty("line.separator")
				+ System.getProperty("line.separator");
	}

	public void setModel(Resource model) {
		this.model.getContents().addAll(EcoreUtil.copyAll(model.getContents()));
	}

	public void setURI(URI uri) {
		this.uri = uri;
		model = resourceSet.createResource(this.uri);
	}

	public Resource getModel() {
		return model;
	}

}
