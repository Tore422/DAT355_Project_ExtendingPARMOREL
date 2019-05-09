package hvl.projectparmorel.ml;
/*
 * Angela Barriga Rodriguez - 2019
 * abar@hvl.no
 * Western Norway University of Applied Sciences
 * Bergen - Norway
 */




import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;

public class Sequence {

	int id;
	List <ErrorAction> seq;
	double weight;
	Resource model;
	ResourceSet rs;
	URI u;
	
	public Sequence() {
		super();
		seq = new ArrayList<ErrorAction>();
		weight = 0.0;
		rs =  new ResourceSetImpl();
		rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put("ecore",
				new EcoreResourceFactoryImpl());
	}

	public Sequence(int id, List<ErrorAction> seq, double weight, URI u) {
		super();
		this.id = id;
		this.seq = seq;
		this.weight = weight;
		this.u = u;
		rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put("ecore",
				new EcoreResourceFactoryImpl());
		this.model = rs.createResource(this.u);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public List<ErrorAction> getSeq() {
		return seq;
	}

	public void setSeq(List<ErrorAction> seq) {
		this.seq = seq;
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

	@Override
	public String toString() {
		return "Sequence [id=" + id + ", seq=" + seq + ", weight=" + weight + "]" + System.getProperty("line.separator") + System.getProperty("line.separator");
	}

	public void setModel(Resource model) {
		this.model.getContents().addAll(EcoreUtil.copyAll(model.getContents()));
	}

	public void setU(URI u) {
		this.u = u;
		model = rs.createResource(this.u);
	}

	public Resource getModel() {
		return model;
	}
	
	
	
}
