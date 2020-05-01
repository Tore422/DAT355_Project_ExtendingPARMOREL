/*******************************************************************************
 * Copyright (c) 2008 The University of York.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * Contributors:
 *     Dimitrios Kolovos - initial API and implementation
 ******************************************************************************/
package it.gssi.cs.quality;


import java.util.ArrayList;
import java.util.List;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.epsilon.eol.EolModule;
import org.eclipse.epsilon.eol.IEolModule;
import org.eclipse.epsilon.eol.models.IModel;
import it.gssi.cs.quality.EpsilonStandaloneExample;

public class QualityEvalEngine extends EpsilonStandaloneExample {
	
	public static void main(String[] args) throws Exception {
				
		for (int i = 1; i <= Integer.parseInt(args[0]); i++) {
			final String MM = String.format("model/fixed/Company/%s/Company.ecore", i);
			final String qualityModel = String.format("model/quality.model", i);
			
			new QualityEvalEngine().execute( MM, qualityModel);
		}
		

	}

	@Override
	public IEolModule createModule() {
		return new EolModule();
	}

	@Override
	public List<IModel> getModels( String MM, String qualityModel) throws Exception {
		List<IModel> models = new ArrayList<IModel>();

		models.add(createEmfModelByURI("MM", MM,
				EcorePackage.eNS_URI, true, false));

		models.add(createEmfModel("qualityModel", qualityModel,
				"model/QualityMM.ecore", true, true));

		return models;
	}

	@Override
	public String getSource() throws Exception {
		return "eol/eval.eol";

	}

	@Override
	public void postProcess() {
		System.err.println("Evaluation finished");
	}

}
