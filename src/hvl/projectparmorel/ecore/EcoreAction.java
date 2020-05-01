package hvl.projectparmorel.ecore;

import java.io.IOException;

import org.w3c.dom.Element;

import hvl.projectparmorel.general.Action;
import hvl.projectparmorel.modelrepair.SerializableMethod;

public class EcoreAction extends Action {
	
	public static final String TYPE = "ECORE";
	
	public EcoreAction(){
		super();
	}
	
	public EcoreAction(int id, String name, SerializableMethod method, int contextId) {
		super(id, name, method, contextId);
	}
	
	public EcoreAction(Element action) throws IOException {
		super(action);
	}

	@Override
	public boolean isDelete() {
		return String.valueOf(id).startsWith("9999");
	}

	@Override
	protected String getActionType() {
		return TYPE;
	}

}
