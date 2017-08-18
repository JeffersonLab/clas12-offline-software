package cnuphys.ced.fastmc;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.jlab.clas.physics.PhysicsEvent;
import org.jlab.io.base.DataEvent;

import bCNU3D.DoubleFormat;
import cnuphys.bCNU.dialog.SimpleDialog;
import cnuphys.bCNU.graphics.ImageManager;
import cnuphys.bCNU.graphics.component.CommonBorder;
import cnuphys.bCNU.util.Fonts;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.clasio.ClasIoEventManager.EventSourceType;
import cnuphys.ced.clasio.IClasIoEventListener;
import cnuphys.ced.event.AccumulationManager;
import cnuphys.ced.event.IAccumulationListener;

public class NNDataDialog extends SimpleDialog implements IClasIoEventListener, IAccumulationListener {

	// button names for closeout
	private static String[] closeoutButtons = { "Close" };

	// nn data file
	private File _outputFile;

	// file label
	private JLabel _fileLabel;
	
	// status label
	private JLabel _statusLabel;
	
	//the min max panels
	private MinMaxPanel _momentumRange;
	private MinMaxPanel _thetaRange;
	private MinMaxPanel _phiRange;
	
	private JTextField _countTF;
	private JButton _generateButton;

	/** the last accessed directory */
	private static String dataFilePath;

	public NNDataDialog() {
		super("Create Neural Net Input Data", true, closeoutButtons);
		ClasIoEventManager.getInstance().addClasIoEventListener(this, 2);
		AccumulationManager.getInstance().addAccumulationListener(this);

		checkButtons();
	}

	/**
	 * Override to create the component that goes in the center. Usually this is
	 * the "main" component.
	 * 
	 * @return the component that is placed in the center
	 */
	@Override
	protected Component createCenterComponent() {
		JPanel fsp = new JPanel();

		fsp.setLayout(new BoxLayout(fsp, BoxLayout.Y_AXIS));
		_momentumRange = new MinMaxPanel("momentum", "GeV", 5.0, 10.0);
		_thetaRange = new MinMaxPanel("theta", "deg", 8, 42);
		_phiRange = new MinMaxPanel("phi", "deg", -23, 23);
		
		
		fsp.add(_momentumRange);
		fsp.add(_thetaRange);
		fsp.add(_phiRange);
		fsp.add(generatePanel());
		
		_statusLabel = new JLabel("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
		_statusLabel.setFont(Fonts.defaultFont);
		_statusLabel.setForeground(Color.red);
		_statusLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		
		fsp.add(_statusLabel);
		return fsp;
	}
	
	private void setStatus(String s) {
		_statusLabel.setText(s);
	}
	
	private JPanel generatePanel() {
		JPanel p = new JPanel();
//		p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
		
		p.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		
		JLabel label = new JLabel("Count:");
		_countTF = new JTextField(10);
		_countTF.setText("10000");
		_generateButton = new JButton("Generate");
		
		p.add(label);
		p.add(Box.createHorizontalStrut(8));
		p.add(_countTF);
		p.add(Box.createHorizontalStrut(20));
		p.add(_generateButton);
		
		ActionListener al = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() == _generateButton) {
					generateData();
				}
			}
			
		};
		
		_generateButton.addActionListener(al);
		
		return p;
	}
	
	private int getCount() {
		int count = 0;
		try {
			count = Integer.parseInt(_countTF.getText());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		count = (Math.max(count, 0));
		return count;
	}
	
	private boolean generateData() {
		//get the count
		int count = getCount();
		if (count < 1) {
			setStatus("The \"Count\" value is not valid.");
			return false;
		}
		
		// first create the lund file
		
	    File tempFile;
		try {
			setStatus("Creating (temp) Lund File");

			tempFile = File.createTempFile("_cedNN", ".dat");
	//	    tempFile.deleteOnExit();
		    System.err.format("Canonical filename: %s\n", tempFile.getCanonicalFile());
			PrintWriter printWriter = new PrintWriter(tempFile);

			//write the header, most is not relevant for fast MC
			
			Random rand = new Random();
			for (int i = 0; i < count; i++) {
				header(printWriter);
				randomLine(printWriter, rand);
			}
			
			
			printWriter.flush();
			printWriter.close();
		}
		catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		
		return true;
	}
	
	private void header(PrintWriter writer) {
		writer.println("1  1.  1.  0 0 0.0   0.0   0.0   0.0  0.0");
	}
	
	private void randomLine(PrintWriter writer, Random rand) {
		
		double p  = _momentumRange.random(rand);
		double theta = _thetaRange.random(rand);
		double phi = _phiRange.random(rand);
		
//		System.err.println("p = " + DoubleFormat.doubleFormat(p, 3) +
//				"  theta = "+ DoubleFormat.doubleFormat(theta, 3) +
//				"  phi = "+ DoubleFormat.doubleFormat(phi, 3));
		
		theta = Math.toRadians(theta);
		phi = Math.toRadians(phi);
		
		int index = 1;
		int charge = -1; //electron
		int type = 1;
		int pid = 11; //electron
		int parentIndex = 0;
		int daughterIndex = 0;
		double px = p*Math.sin(theta)*Math.cos(phi);
		double py = p*Math.sin(theta)*Math.sin(phi);
		double pz = p*Math.cos(theta);
		double m = 0.000511; //electron
		double e = Math.sqrt(p*p + m*m);
		double vx = 0; //vertex x
		double vy = 0; //vertex y
		double vz = 0; //vertex z
		
		String s = String.format("%d %d %d %d %d %d %-9.4f %-9.4f %-9.4f %-9.4f %-11.6f %-9.4f %-9.4f %-9.4f", 
				index, charge, type, pid, parentIndex, daughterIndex,
				px, py, pz, e, m, vx, vy, vz);
		System.err.println(s);
		writer.println(s);
	}

	/**
	 * Override to create the component that goes in the north.
	 * 
	 * @return the component that is placed in the north
	 */
	protected Component createNorthComponent() {
		JPanel fsp = fileSelectionPanel();

		return fsp;
	}

	// create the file selection panel
	private JPanel fileSelectionPanel() {
		JPanel fsp = new JPanel();

		fsp.setLayout(new BoxLayout(fsp, BoxLayout.X_AXIS));

		final JButton button = new JButton("Output File");

		ActionListener al = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() == button) {
					getOutputFile();
					checkButtons();
				}
			}

		};

		button.addActionListener(al);

		_fileLabel = new JLabel("/XXXXXXX/XXXXXXX/XXXXXXX/XXXXXXX/XXXXXXX/XXXXXXXXXXXXXXX.XXX");
		_fileLabel.setFont(Fonts.defaultFont);

		fsp.add(button);
		fsp.add(Box.createHorizontalStrut(8));
		fsp.add(_fileLabel);
		fsp.add(Box.createHorizontalStrut(8));
		fsp.add(Box.createHorizontalGlue());

		fsp.setBorder(new CommonBorder("Select data file location"));

		return fsp;
	}

	/**
	 * can do preparation--for example a component might be added on
	 * "createCenterComponent" but a reference needed in "addNorthComponent"
	 */
	@Override
	protected void prepare() {
	}

	/**
	 * Override to create the component that goes in the east.
	 * 
	 * @return the component that is placed in the east
	 */
	@Override
	protected Component createEastComponent() {
		return null;
	}

	/**
	 * Override to create the component that goes in the west.
	 * 
	 * @return the component that is placed in the west.
	 */
	@Override
	protected Component createWestComponent() {
		return null;
	}

	/**
	 * Check the enabled state of all the buttons. Default implementation does
	 * nothing.
	 */
	@Override
	protected void checkButtons() {
		
		boolean goodFile = haveGoodFile();
		
		if (_fileLabel != null) {
			if (!goodFile) {
				_fileLabel.setText("No Output File Selected");
				setStatus("Start by selecting an output file for the training data.");
			}
			else {
				_fileLabel.setText(_outputFile.getPath());
			}
		}
		
		_generateButton.setEnabled(goodFile);

		revalidate();
	}

	// do we have a good output file?
	private boolean haveGoodFile() {
		return (_outputFile != null);
	}

	private boolean overWrite() {
		if (_outputFile.exists()) {
			int answer = JOptionPane.showConfirmDialog(null,
					_outputFile.getAbsolutePath() + "  already exists. Do you want to overwrite it?",
					"Overwite Existing Data File?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
					ImageManager.cnuIcon);

			return (answer == JFileChooser.APPROVE_OPTION);

		} // end file exists check
		else {
			return true;
		}
	}

	// bring up file chooser to get output file
	private void getOutputFile() {
		JFileChooser chooser = new JFileChooser(dataFilePath);
		chooser.setSelectedFile(_outputFile);
		int returnVal = chooser.showSaveDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			_outputFile = chooser.getSelectedFile();
			// System.err.println("SELECTED FILE: [" + _outputFile.getPath() +
			// "]");
			dataFilePath = _outputFile.getParent();
		} // approved file selection

	}

	@Override
	public void accumulationEvent(int reason) {
	}

	@Override
	public void newClasIoEvent(DataEvent event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void openedNewEventFile(String path) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void changedEventSource(EventSourceType source) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void newFastMCGenEvent(PhysicsEvent event) {
		System.err.println("HEY MAN");
	}

}
