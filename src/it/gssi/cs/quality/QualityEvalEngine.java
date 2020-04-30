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


import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.epsilon.common.util.StringProperties;
import org.eclipse.epsilon.emc.emf.EmfMetaModel;
import org.eclipse.epsilon.emc.emf.EmfUtil;
import org.eclipse.epsilon.eol.EolModule;
import org.eclipse.epsilon.eol.IEolModule;
import org.eclipse.epsilon.eol.dom.Operation;
import org.eclipse.epsilon.eol.exceptions.EolRuntimeException;
import org.eclipse.epsilon.eol.exceptions.models.EolModelLoadingException;
import org.eclipse.epsilon.eol.models.IModel;
import org.eclipse.epsilon.eol.models.IRelativePathResolver;
import it.gssi.cs.quality.EpsilonStandaloneExample;
 
import org.eclipse.m2m.atl.emftvm.EmftvmPackage;
import org.eclipse.m2m.atl.emftvm.trace.TracePackage;

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

		// EPackage.Registry.INSTANCE.put(EmftvmPackage.eNS_URI,
		// EmftvmPackage.eINSTANCE);
		

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
