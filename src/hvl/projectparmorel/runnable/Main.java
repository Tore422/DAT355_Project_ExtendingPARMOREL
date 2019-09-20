package hvl.projectparmorel.runnable;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.impl.EClassImpl;
import org.eclipse.emf.ecore.impl.EDataTypeImpl;
import org.eclipse.emf.ecore.impl.EEnumImpl;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import hvl.projectparmorel.ml.Error;
import hvl.projectparmorel.ml.ErrorExtractor;
import hvl.projectparmorel.ml.QLearning;

public class Main {

	public static void main(String[] args) throws IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, IOException, NoSuchMethodException, SecurityException {

		QLearning ql = new QLearning(createTags(0));

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
			copyFile(listOfFiles[i], dest);

			URI uri = URI.createFileURI(dest.getAbsolutePath());
//			ql.resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("ecore",
//					new EcoreResourceFactoryImpl());
			Resource myMetaModel = ql.getResourceSet().getResource(uri, true);

			Resource auxModel = ql.getResourceSet().createResource(uri);
			auxModel.getContents().addAll(EcoreUtil.copyAll(myMetaModel.getContents()));

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

			List<Error> errors = ErrorExtractor.extractErrorsFrom(auxModel);

			System.out.println("INITIAL ERRORS:");
			System.out.println(errors.toString());
			System.out.println("Size= " + errors.size());
			System.out.println();

			System.out.println("PREFERENCES: " + ql.getPreferences().toString());
			ql.fixModel(myMetaModel, uri);

			endTime = System.currentTimeMillis();
			long timeneeded = (endTime - startTime);
			System.out.println("TOTAL TIME: " + timeneeded);
		}

		System.out.println("COMPLETELY FINISHED!!!!!!");
		endTimeT = System.currentTimeMillis();
		long timeneededT = (endTimeT - startTimeT);
		System.out.println("TOTAL EXECUTION TIME: " + timeneededT);
		ql.saveKnowledge();
		System.exit(0);
	}
	
	private static void copyFile(File from, File to) throws IOException {
		Files.copy(from.toPath(), to.toPath());
	}
	
	private static List<Integer> createTags(int user) {
		switch (user) {
		// ECMFA paper preferences
		case 0:
			return new ArrayList<Integer>(Arrays.asList(new Integer[] { 2, 4 }));
		// error hierarchy high, sequence short
		case 1:
			return new ArrayList<Integer>(Arrays.asList(new Integer[] { 0, 2 }));
		// error hierarchy low, sequence long, high modification
		case 2:
			return new ArrayList<Integer>(Arrays.asList(new Integer[] { 1, 3, 6 }));
		// avoid deletion
		case 3:
			return new ArrayList<Integer>(Arrays.asList(new Integer[] { 4 }));
		// short sequence, low modification
		case 4:
			return new ArrayList<Integer>(Arrays.asList(new Integer[] { 0, 4, 5 }));
		// long sequence, low modification, avoid deletion
		case 5:
			return new ArrayList<Integer>(Arrays.asList(new Integer[] { 1, 5 }));
		// error hierarchy high
		case 6:
			return new ArrayList<Integer>(Arrays.asList(new Integer[] { 2 }));
		default:
			return null;
		}
	}
}
