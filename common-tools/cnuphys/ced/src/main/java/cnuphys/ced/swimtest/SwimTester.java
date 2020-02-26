package cnuphys.ced.swimtest;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import cnuphys.bCNU.dialog.VerticalFlowLayout;
import cnuphys.bCNU.graphics.component.TextFieldPanel;
import cnuphys.magfield.MagneticFields;
import cnuphys.magfield.MagneticFields.FieldType;

/**
 * Used for testing the swimmer
 * @author heddle
 *
 */
public class SwimTester extends JFrame implements ListSelectionListener, ActionListener {

	private GenericList<ASwimTest> _jList;
	
	private static SwimTester _instance;
	
	//buttons
	private JButton quitButton;
	private JButton launchButton;
	
	//entry text fields
	private TextFieldPanel iterationTF;
	private TextFieldPanel randomTF;
	
	//description text area
	private JTextArea descriptionTextArea;
	
	//results text area
	private JTextArea resultsTextArea;
	
	private SwimTester() {
		super("Swim Tester");
		
		WindowAdapter wa = new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent we) {
				System.out.println("Exiting.");
				System.exit(0);
			}
		};
		addWindowListener(wa);
		
		addContent();
		pack();
	}
	
	/**
	 * Access to the singleton
	 * @return the SwimTester
	 */
	public static SwimTester getInstance() {
		if (_instance == null) {
			_instance = new SwimTester();
		}
		return _instance;
	}
	
	//add the content to the frame
	private void addContent() {
		setLayout(new BorderLayout(4, 4));
		addWest();
		addSouth();
		addEast();
		addNorth();
		addCenter();
	}
	
	//add a south component
	private void addSouth() {
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.CENTER, 40, 0));
		
		launchButton = addButton(" Launch ", panel, this);
		launchButton.setEnabled(false);
		quitButton = addButton(" Quit ", panel, this);
		
		add(panel, BorderLayout.SOUTH);
	}
	
	//convenience method for adding a button
	private JButton addButton(String label, JPanel panel, ActionListener al) {
		JButton button = new JButton(label);
		button.addActionListener(al);
		panel.add(button);
		return button;
	}
	
	//add component on the center
	private void addCenter() {
		resultsTextArea = new JTextArea(20, 40);
		resultsTextArea.setWrapStyleWord(true);
		resultsTextArea.setLineWrap(true);
		resultsTextArea.setEditable(false);
		
		JScrollPane spane = new JScrollPane(resultsTextArea);
		
		add(spane, BorderLayout.CENTER);
	}
	
	//add component on the north
	private void addNorth() {
		descriptionTextArea = new JTextArea(5, 40);
		descriptionTextArea.setWrapStyleWord(true);
		descriptionTextArea.setLineWrap(true);
		descriptionTextArea.setEditable(false);
		
		JScrollPane spane = new JScrollPane(descriptionTextArea);
		
		add(spane, BorderLayout.NORTH);
	}

	//add component on the east
	private void addEast() {
		JPanel panel = new JPanel();
		panel.setLayout(new VerticalFlowLayout());
		
		String sizeText = "  Random Seed  "; 
		iterationTF = makeTextFieldPanel("Iterations", sizeText, 10, "1", panel);
		randomTF    = makeTextFieldPanel("Random Seed", sizeText, 10, "-1", panel);
		
		add(panel, BorderLayout.EAST);
	}

	private TextFieldPanel makeTextFieldPanel(String prompt, String sizeStr, int numCol, String defText, JPanel panel) {
		TextFieldPanel tfp = new TextFieldPanel(prompt, sizeStr, numCol);
		panel.add(tfp);
		if (defText != null) {
			tfp.setText(defText);
		}
		return tfp;
	}
	
	//add the west component
	private void addWest() {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(4, 4));
		
		_jList = new GenericList();
		_jList.addListSelectionListener(this);
		
		JScrollPane spane = _jList.getScrollPane();

		JLabel label = new JLabel(" Discovered Tests ");
		label.setForeground(Color.red);
		
		panel.add(label, BorderLayout.NORTH);
		
		panel.add(spane, BorderLayout.CENTER);
		add(panel, BorderLayout.WEST);
	}
	
	public void addTest(ASwimTest test) {
		_jList.getModel().addElement(test);
	}
	
	//use reflection to find the tests
	private  ArrayList<ASwimTest> findTests() {
		
		ArrayList<ASwimTest> tests = new ArrayList<>();
		
		Class<ASwimTest> baseClaz = null;
		try {
			
			Class<?> claz = Class
					.forName("cnuphys.ced.swimtest.ASwimTest");
			
            if (claz.equals(ASwimTest.class)) {
            	baseClaz = (Class<ASwimTest>) claz;
            }
			
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		if (baseClaz != null) {
			String cwd = System.getProperty("user.dir");
			System.err.println("CWD: [" + cwd + "]");
			
			File testDir = new File(cwd + "/cnuphys/ced/swimtest/tests");
			if (testDir.exists() && testDir.isDirectory()) {
				System.err.println("TEST DIR: [" + testDir.getPath() + "]");
				
				Vector<String> classBareNames = new Vector<String>();
				searchDir(testDir, classBareNames);
				
				if (!classBareNames.isEmpty()) {
					for (String bareName : classBareNames) {
						try {
							Class claz = Class.forName(bareName);
							if (baseClaz.isAssignableFrom(claz)) {
								System.err.println("Found one!");
								try {
									tests.add((ASwimTest) claz.newInstance());
								}
								catch (InstantiationException e) {
									e.printStackTrace();
								}
								catch (IllegalAccessException e) {
									e.printStackTrace();
								}
				            }
						}
						catch (ClassNotFoundException e) {
							e.printStackTrace();
						}
					}
				}


			}
			
		}

		return tests;

	}
	

	/**
	 * Search a directory for classes that are plugins.
	 * 
	 * @param dir
	 *            the file the is a directory in the classpath
	 * @param v
	 *            the vector to which we add any matching classes.
	 */
	private  void searchDir(File dir, Vector<String> v) {
		// System.out.println("Searching directory: " + dir.getAbsolutePath());
		
		// filter on .class files and subdirectories
		FilenameFilter filter = new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				File theFile = new File(dir, name);
				return (theFile.isDirectory() || name.endsWith(".java"));
			}

		};


		String[] files = dir.list(filter);
		int testDirLen = dir.getPath().length();

		
		if ((files == null) || (files.length < 1)) {
			return;
		}
		// used to remove the leading class path part

		for (String fileName : files) {
			File file = new File(dir.getAbsolutePath(), fileName);

			// System.err.println("FILE: " + file.getAbsolutePath());
			if (file.isDirectory()) {
//				searchDir(file, v);
			}
			else { // is a regular file

				String klass = file.getAbsolutePath();

				klass = klass.substring(testDirLen + 1).replace(File.separatorChar, '.');
				// remove .class
				klass = klass.substring(0, klass.lastIndexOf('.'));
				
				klass = "cnuphys.ced.swimtest.tests." + klass;

				System.err.println("KLAS: " + klass);
				v.add(klass);
			}
		}
	}
	
	
	private void initMagFields() {
		MagneticFields.getInstance().initializeMagneticFields();
		MagneticFields.getInstance().setActiveField(FieldType.TORUS);
		System.out.println("Active Field Description: " + MagneticFields.getInstance().getActiveFieldDescription());
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		
		if (source == quitButton) {
			System.exit(1);
		}
		else if (source == launchButton) {
			handleLaunch();
		}
			
	}
	
	//launch button pressed
	private void handleLaunch() {
		System.err.println("Hit launch button");
		
		ASwimTest test = (ASwimTest) _jList.getSelectedValue();
		if (test != null) {
			test.launch();
			System.err.println("Ready for results");
			resultsTextArea.setText(test.getResults());
		}
	}

	
	@Override
	public void valueChanged(ListSelectionEvent e) {
		if (!e.getValueIsAdjusting()) {
			System.err.println("Selected [" + _jList.getSelectedValue() + "]");
			
			ASwimTest test = (ASwimTest) _jList.getSelectedValue();
			if (test == null) {
				launchButton.setEnabled(false);
				descriptionTextArea.setText("");
			}
			else {
				launchButton.setEnabled(true);
				descriptionTextArea.setText(test.getDescription());
			}
						
		}
	}

	/**
	 * @return the iterationTF
	 */
	public TextFieldPanel getIterationTF() {
		return iterationTF;
	}

	/**
	 * @return the randomTF
	 */
	public TextFieldPanel getRandomTF() {
		return randomTF;
	}
		
	/**
	 * Main program
	 * 
	 * @param arg
	 *            command line arguments
	 */
	public static void main(String arg[]) {

		final SwimTester tester = getInstance();
		
		//initialize mag fields
		tester.initMagFields();
		
		// look for tests
		ArrayList<ASwimTest> tests = tester.findTests();

		// add the tests
		for (ASwimTest test : tests) {
			tester.addTest(test);
		}

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				tester.setLocationRelativeTo(null);
				tester.setVisible(true);
			}
		});

	}


	


}
