package hvl.projectparmorel.runnable;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.impl.EClassImpl;
import org.eclipse.emf.ecore.impl.EDataTypeImpl;
import org.eclipse.emf.ecore.impl.EEnumImpl;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;

import hvl.projectparmorel.ml.Error;
import hvl.projectparmorel.ml.QLearning;

public class Main {

	public static void main(String[] args) throws IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, IOException, NoSuchMethodException, SecurityException {

		QLearning ql = new QLearning();
		QLearning.user = 0;
		QLearning.createTags(QLearning.user);

		long startTimeT = System.currentTimeMillis();
		long endTimeT = 0;
		String root = "././mutants/";
		String root2 = "././fixed/";
		File folder = new File(root);
		File[] listOfFiles = folder.listFiles();
		for (int i = 0; i < listOfFiles.length; i++) {
			// invert mutant order
			// for (int i = listOfFiles.length-1; i >=0; i--) {

			File dest = new File(root2 + listOfFiles[i].getName());
			long startTime = System.currentTimeMillis();
			long endTime = 0;
			System.out.println("----------------------------------------------------------------------");
			System.out.println("----------------------------------------------------------------------");
			System.out.println("STARTING WITH MODEL - " + i + ": " + listOfFiles[i].getName());

			// Copy original file
			QLearning.copyFile(listOfFiles[i], dest);

			ql.uri = URI.createFileURI(dest.getAbsolutePath());
			ql.resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("ecore",
					new EcoreResourceFactoryImpl());
			ql.myMetaModel = ql.resourceSet.getResource(ql.uri, true);

			Resource auxModel = ql.resourceSet.createResource(ql.uri);
			auxModel.getContents().addAll(EcoreUtil.copyAll(ql.myMetaModel.getContents()));

			EPackage epa = (EPackage) auxModel.getContents().get(0);
			System.out.println("Num. Classes: " + epa.getEClassifiers().size());

			int enums = 0;
			int refs = 0;
			int attribs = 0;
			int ops = 0;
			int datatypes = 0;

			for (int j = 0; j < epa.getEClassifiers().size(); j++) {
				if (epa.getEClassifiers().get(j).getClass() == EEnumImpl.class) {
					enums++;
				} else {
					if (epa.getEClassifiers().get(j).getClass() == EDataTypeImpl.class) {
						datatypes++;
					} else {
						refs = refs + ((EClassImpl) epa.getEClassifiers().get(j)).getEAllReferences().size();
						attribs = attribs + ((EClassImpl) epa.getEClassifiers().get(j)).getEAllAttributes().size();
						ops = ops + ((EClassImpl) epa.getEClassifiers().get(j)).getEAllOperations().size();
					}
				}
			}

			System.out.println("Num. References: " + refs);
			System.out.println("Num. Attributes: " + attribs);
			System.out.println("Num. Operations: " + ops);
			System.out.println("Num. Enums: " + enums);
			System.out.println("Num. DataTypes: " + datatypes);
			System.out.println("----------------------------------------------------------------------");
			System.out.println("----------------------------------------------------------------------");

			List<Error> errors = ql.errorsExtractor(auxModel);
			ql.nuQueue = errors;

			for (int y = 0; y < ql.nuQueue.size(); y++) {
				ql.originalCodes.add(ql.nuQueue.get(y).getCode());
			}
			System.out.println("INITIAL ERRORS:");
			System.out.println(errors.toString());
			System.out.println("Size= " + errors.size());
			System.out.println();

			ql.actionsExtractor(errors);

			ql.modelFixer(auxModel);

			endTime = System.currentTimeMillis();
			long timeneeded = (endTime - startTime);
			System.out.println("TOTAL TIME: " + timeneeded);
		}

		System.out.println("COMPLETELY FINISHED!!!!!!");
		endTimeT = System.currentTimeMillis();
		long timeneededT = (endTimeT - startTimeT);
		System.out.println("TOTAL EXECUTION TIME: " + timeneededT);
		QLearning.save(ql.getKnowledge().getExperience(), "././knowledge.properties");
		System.exit(0);
	}
}
