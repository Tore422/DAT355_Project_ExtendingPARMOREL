package hvl.projectparmorel.runnable;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;

import hvl.projectparmorel.ml.Error;
import hvl.projectparmorel.ml.ErrorExtractor;
import hvl.projectparmorel.ml.QLearning;

public class GUI extends JPanel {

	private static final long serialVersionUID = 1L;
	private JButton importButton;
	private JButton repairButton;
	private JTextArea errorsDisplay;
	private JRadioButton tag0;
	private JLabel jcomp5;
	private JRadioButton tag1;
	private JRadioButton tag2;
	private JRadioButton tag3;
	private JRadioButton tag5;
	private JRadioButton tag6;
	private JCheckBox tag4;
	private ButtonGroup groupSeq;
	private ButtonGroup groupHierar;
	private ButtonGroup groupMod;
	private File[] files;
	private File dest;
	static JFrame frame;
	JPanel newPanel = new JPanel();
	private URI uri;
	private QLearning ql;
	List<Error> errors = null;
	Resource auxModel;
	Resource myMetaModel;

	private JButton exportButton;
	private JTextArea sequenceDisplay;

	public File[] getFiles() {
		return files;
	}

	public void setFiles(File[] files) {
		this.files = files;
	}

	public JTextArea getErrorsDisplay() {
		return errorsDisplay;
	}

	public void setErrorsDisplay(JTextArea errorsDisplay) {
		this.errorsDisplay = errorsDisplay;
	}

	public JTextArea getSequenceDisplay() {
		return sequenceDisplay;
	}

	public void setSequenceDisplay(JTextArea sequenceDisplay) {
		this.sequenceDisplay = sequenceDisplay;
	}

	public GUI() {
		ql = new QLearning();
		// construct components
		importButton = new JButton("Import model");
		repairButton = new JButton("Repair");
		errorsDisplay = new JTextArea(5, 5);
		tag0 = new JRadioButton("Short sequence");
		jcomp5 = new JLabel("Repair preferences");
		tag1 = new JRadioButton("Long sequence");
		tag2 = new JRadioButton("High error hierarchy");
		tag3 = new JRadioButton("Low error hierarchy");
		tag5 = new JRadioButton("Punish modification");
		tag6 = new JRadioButton("Reward modification");
		tag4 = new JCheckBox("Punish deletion");

		// construct components
		exportButton = new JButton("Export repaired model");
		sequenceDisplay = new JTextArea(5, 5);

		groupSeq = new ButtonGroup();
		groupHierar = new ButtonGroup();
		groupMod = new ButtonGroup();

		groupSeq.add(tag0);
		groupSeq.add(tag1);

		groupHierar.add(tag2);
		groupHierar.add(tag3);

		groupMod.add(tag5);
		groupMod.add(tag6);

		// set components properties
		repairButton.setEnabled(false);
		errorsDisplay.setDisabledTextColor(Color.BLACK);
		errorsDisplay.setEnabled(false);

		// adjust size and set layout
		setPreferredSize(new Dimension(800, 412));
		setLayout(null);

		// add components
		add(importButton);
		add(repairButton);
		add(errorsDisplay);
		add(tag0);
		add(jcomp5);
		add(tag1);
		add(tag2);
		add(tag3);
		add(tag5);
		add(tag6);
		add(tag4);

		// set component bounds (only needed by Absolute Positioning)
		importButton.setBounds(120, 35, 250, 40);
		repairButton.setBounds(120, 345, 250, 40);
		errorsDisplay.setBounds(30, 105, 450, 210);

		jcomp5.setBounds(575, 65, 240, 35);
		jcomp5.setFont(new Font("Arial", Font.BOLD, 14));
		tag0.setBounds(570, 115,230, 25);
		tag0.setActionCommand("0");

		tag1.setBounds(570, 135, 260, 30);
		tag1.setActionCommand("1");
		
		tag3.setBounds(570, 175, 235, 25);
		tag3.setActionCommand("3");

		tag2.setBounds(570, 200, 170, 20);
		tag2.setActionCommand("2");

		tag4.setBounds(570, 305, 175, 25);
		tag4.setActionCommand("4");

		tag5.setBounds(570, 240, 185, 25);
		tag5.setActionCommand("5");

		tag6.setBounds(570, 260, 170, 30);
		tag6.setActionCommand("6");

		importButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				try {
					importButtonPressed();
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
						| NoSuchMethodException | SecurityException | IOException e) {
					e.printStackTrace();
				}
			}
		});

		repairButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				try {
					repairButtonPressed();
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
						| NoSuchMethodException | SecurityException | IOException e) {
					e.printStackTrace();
				}
			}
		});

		exportButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				try {
					exportButtonPressed();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});

	}

	void importButtonPressed() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, IOException {
		JFileChooser chooser = new JFileChooser();
		chooser.setMultiSelectionEnabled(true);
		chooser.showOpenDialog((Frame) null);
		File[] files = chooser.getSelectedFiles();
		setFiles(files);

		dest = new File(files[0].getParent()+"\\"+"repaired-"+files[0].getName());

		Files.copy(files[0].toPath(), dest.toPath());
	
		uri = URI.createFileURI(dest.getAbsolutePath());
		ql.resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("ecore",
				new EcoreResourceFactoryImpl());
		myMetaModel = ql.resourceSet.getResource(uri, true);

		Resource auxModel = ql.resourceSet.createResource(uri);
		auxModel.getContents().addAll(EcoreUtil.copyAll(myMetaModel.getContents()));

		
		errors = ErrorExtractor.extractErrorsFrom(auxModel);
//		ql.nuQueue = errors;
		

		String errorsFound = "Errors found in model " + files[0].getName() + ":" + System.getProperty("line.separator")
				+ System.getProperty("line.separator");
		getErrorsDisplay().insert(errorsFound + errors.toString(), 0);
		repairButton.setEnabled(true);
	}

	void repairButtonPressed() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException, IOException {
		List<Integer> preferences = new ArrayList<>();
		if (groupSeq.getSelection() != null) {
			preferences.add(Integer.parseInt(groupSeq.getSelection().getActionCommand()));
		}
		if (groupHierar.getSelection() != null) {
			preferences.add(Integer.parseInt(groupHierar.getSelection().getActionCommand()));
		}
		if (groupMod.getSelection() != null) {
			preferences.add(Integer.parseInt(groupMod.getSelection().getActionCommand()));
		}
		if (tag4.isSelected()) {
			preferences.add(Integer.parseInt(tag4.getActionCommand()));
		}
		long startTime = System.currentTimeMillis();
		long endTime = 0;
		ql.setPreferences(preferences);
		System.out.println("PREFERENCES: " + preferences.toString());
		ql.fixModel(myMetaModel, uri);
//		ql.saveKnowledge();	

		frame.getContentPane().removeAll();
		frame.getContentPane().add(secondGUI());
		frame.getContentPane().revalidate();
		frame.getContentPane().repaint();

		String seqFound = "Best sequence found to repair model " + files[0].getName() + ":"
				+ System.getProperty("line.separator") + System.getProperty("line.separator");
		getSequenceDisplay().insert(seqFound + ql.getBestSeq().toString(), 0);
		
		endTime = System.currentTimeMillis();
		long timeneeded = (endTime - startTime);
		System.out.println("TOTAL TIME: " + timeneeded);
	}

	void exportButtonPressed() throws IOException {
		JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.showOpenDialog((Frame) null);
		File file = chooser.getSelectedFile();

		 dest.renameTo(new File(file.getAbsolutePath() + "\\" + dest.getName()));
		 System.exit(0);
		}

	public JPanel secondGUI() {
	
		// set components properties
		sequenceDisplay.setEnabled(false);
		sequenceDisplay.setDisabledTextColor(Color.BLACK);

		// adjust size and set layout
		newPanel.setPreferredSize(new Dimension(1200, 400));
		newPanel.setLayout(null);

		// set component bounds (only needed by Absolute Positioning)
		exportButton.setBounds(275, 330, 250, 40);
		sequenceDisplay.setBounds(30, 35, 740, 270);

		// add components
		newPanel.add(exportButton);
		newPanel.add(sequenceDisplay);

		return newPanel;
	}

	public static void main(String[] args) {
		frame = new JFrame("PARMOREL");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(new GUI());
		frame.pack();
		frame.setVisible(true);
	}
}