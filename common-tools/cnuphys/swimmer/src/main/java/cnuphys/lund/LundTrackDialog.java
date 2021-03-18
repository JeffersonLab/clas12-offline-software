package cnuphys.lund;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import cnuphys.adaptiveSwim.AdaptiveSwimException;
import cnuphys.adaptiveSwim.AdaptiveSwimResult;
import cnuphys.adaptiveSwim.AdaptiveSwimmer;
import cnuphys.magfield.FastMath;
import cnuphys.swim.SwimTrajectory;
import cnuphys.swim.Swimming;
import cnuphys.swimZ.SwimZResult;

@SuppressWarnings("serial")
public class LundTrackDialog extends JDialog {

	private static final int CANCEL_RESPONSE = 1;

	// combo box for selecting the particle
	private LundComboBox _lundComboBox;

	// for selecting energy
	// private JTextField _energyTextField;

	// text field for gamma
	private JTextField _relativisticGamma;

	// text field for beta
	private JTextField _relativisticBeta;

	// text field for total energy
	private JTextField _totalEnergyTextField;

	// text field for momentum
	private JTextField _momentumTextField;

	// text field for mass
	private JTextField _massTextField;

	// starts the swimming
	private JButton _swimButton;

	// text field for x vertex
	private JTextField _vertexX;

	// text field for y vertex
	private JTextField _vertexY;

	// text field for z vertex
	private JTextField _vertexZ;

	// text field for initial theta
	private JTextField _theta;

	// text field for initial phi
	private JTextField _phi;

	// use standard cutoff
	private JRadioButton _standardCutoff;

	// fixed z cutoff
	private JRadioButton _fixedZCutoff;


	// fixed Z value
	private JTextField _fixedZ;

	// accuracy in fixed z
	private JTextField _accuracy;

	// old (and default_ momentum in GeV/c
	private double _oldMomentum = 2.0;

	// only need one swimmer

	// unicode strings
	public static final String SMALL_BETA = "\u03B2";
	public static final String SMALL_GAMMA = "\u03B3";
	public static final String SMALL_THETA = "\u03B8";
	public static final String SMALL_PHI = "\u03C6";
	public static final String SUPER2 = "\u00B2";

	// usual relativistic quantities
	private double _gamma;
	private double _beta;
	private double _energy; // total energy

	// for labels
	private static final String RELGAMMA = "Relativistic " + SMALL_GAMMA;
	private static final String RELBETA = "Relativistic " + SMALL_BETA;
	private static final String TOTENERGY = "Total Energy";
	private static final String MOMENTUMMAG = "Momentum";
	private static final String MASS = "Mass";

	// singleton
	private static LundTrackDialog instance;

	/**
	 * Create a dialog used to swim a particle
	 * 
	 * @param swimmer Object that will swim (integrate) the particle through B
	 *                field.
	 */
	private LundTrackDialog() {
		setTitle("Swim a Lund Particle");
		setModal(false);

		// close is like a cancel
		WindowAdapter wa = new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent we) {
				doClose(CANCEL_RESPONSE);
			}
		};
		addWindowListener(wa);

		addComponents();
		pack();
		centerComponent(this);
	}

	/**
	 * Access to the dialog singleton
	 * 
	 * @return the dialog (set visible)
	 */
	public static LundTrackDialog getInstance() {
		if (instance == null) {
			instance = new LundTrackDialog();
		}

		instance.setVisible(true);
		return instance;
	}

	// add all the widgets
	private void addComponents() {
		setLayout(new BorderLayout(6, 6));

		Box box = Box.createVerticalBox();
		box.add(Box.createVerticalStrut(6));

		ActionListener al = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				selectedParticle();
			}
		};

		_lundComboBox = new LundComboBox(true, 950.0, 11);
		_lundComboBox.addActionListener(al);
		box.add(paddedPanel(20, 6, _lundComboBox));

		// add the energy selection panel
		box.add(Box.createVerticalStrut(6));
		box.add(energyPanel());

		// add the direction selection panel
		box.add(Box.createVerticalStrut(6));
		box.add(initConditionsPanel());

		// add the vertex selection panel
		box.add(Box.createVerticalStrut(6));
		box.add(vertexPanel());

		// integration cutoff panel
		box.add(Box.createVerticalStrut(6));
		box.add(cutoffPanel());

		// the swim button
		_swimButton = new JButton("Swim");
		_swimButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				setMomentum();
				doCommonSwim();
			}
		});

		add(box, BorderLayout.CENTER);
		add(paddedPanel(50, 6, _swimButton), BorderLayout.SOUTH);

		// padding
		add(Box.createHorizontalStrut(4), BorderLayout.EAST);
		add(Box.createHorizontalStrut(4), BorderLayout.WEST);

		selectedParticle(); // selects the default
	}

	/**
	 * Swim the particle
	 */
	private void doCommonSwim() {
		
		AdaptiveSwimmer swimmer = new AdaptiveSwimmer();
		
		try {
			LundId lid = _lundComboBox.getSelectedId();

			// note xo, yo, zo converted to meters
			double xo = Double.parseDouble(_vertexX.getText()) / 100.;
			double yo = Double.parseDouble(_vertexY.getText()) / 100.;
			double zo = Double.parseDouble(_vertexZ.getText()) / 100.;
			double momentum = Double.parseDouble(_momentumTextField.getText());
			double theta = Double.parseDouble(_theta.getText());
			double phi = Double.parseDouble(_phi.getText());
			
			double stepSize = 5e-3; // m
			double maxPathLen = 8.0; // m
			
			double eps = 1.0e-6;
			
			AdaptiveSwimResult result = new AdaptiveSwimResult(true);
			
			if (_standardCutoff.isSelected()) {
				swimmer.swim(lid.getCharge(), xo, yo, zo, momentum, theta, phi, maxPathLen, stepSize, eps, result);
				SwimTrajectory traj = result.getTrajectory();
				traj.setLundId(lid);
				result.printOut(System.out, "RESULT from base swim");

				Swimming.addMCTrajectory(traj);
			}
			else {
				// convert accuracy from microns to meters
				double accuracy = Double.parseDouble(_accuracy.getText()) / 1.0e6;
				double ztarget = Double.parseDouble(_fixedZ.getText())/100; // meters
				swimmer.swimZ(lid.getCharge(), xo, yo, zo, momentum, theta, phi, ztarget, accuracy,  maxPathLen, stepSize, eps, result);
				SwimTrajectory traj = result.getTrajectory();
				traj.setLundId(lid);
				result.printOut(System.out, "RESULT from fixed Z swim");

				Swimming.addMCTrajectory(traj);
			}

		}
		catch (AdaptiveSwimException e) {
			e.printStackTrace();
		}
		

	}

	private static void partialReport(SwimZResult result, String name) {
		double p3v[] = result.getFinalThreeMomentum();
		// check
		double pf = Math.sqrt(p3v[0] * p3v[0] + p3v[1] * p3v[1] + p3v[2] * p3v[2]);

		printVect(result.getInitialThreeMomentum(), "po");
		printVect(result.getFinalThreeMomentum(), "pf");
		System.out.println(name + " [" + result.size() + "]");
		System.out.println("Initial state vector: " + result.first());
		System.out.println("Final state vector: " + result.last());

		double thetaPhi[] = result.getInitialThetaAndPhi();
		System.out.println(String.format("Initial theta = %-8.2f phi = %-8.2f deg", thetaPhi[0], thetaPhi[1]));
		thetaPhi = result.getFinalThetaAndPhi();
		System.out.println(String.format("Final theta = %-8.2f phi = %-8.2f deg", thetaPhi[0], thetaPhi[1]));

		System.out.println("p: " + pf + " GeV/c");
	}

	private static void printVect(double v[], String s) {
		String out = String.format("%s [%-12.5f, %-12.5f, %-12.5f]", s, v[0], v[1], v[2]);
		System.out.println(out);
	}

	// print a summary
	private static void printSummary(String message, int nstep, double momentum, double Q[], double hdata[]) {
		System.out.println(message);
		double R = FastMath.sqrt(Q[0] * Q[0] + Q[1] * Q[1] + Q[2] * Q[2]);
		double norm = FastMath.sqrt(Q[3] * Q[3] + Q[4] * Q[4] + Q[5] * Q[5]);
		double P = momentum * norm;
		double px = P * Q[3];
		double py = P * Q[4];
		double pz = P * Q[5];
		double theta = FastMath.acos2Deg(pz / P);
		double phi = FastMath.atan2Deg(py, px);

		System.out.println("Number of steps: " + nstep);

		if (hdata != null) {
			System.out.println("min stepsize: " + hdata[0]);
			System.out.println("avg stepsize: " + hdata[1]);
			System.out.println("max stepsize: " + hdata[2]);
		}
		System.out.println(
				String.format("R = [%9.6f, %9.6f, %9.6f] |R| = %9.6f m\nP = [%9.6e, %9.6e, %9.6e] |P| =  %9.6e GeV/c",
						Q[0], Q[1], Q[2], R, px, py, pz, P));
		System.out.println("norm (should be 1): " + norm);

		System.out.println(String.format("Final angles theta: %9.6f  phi: %9.6f", theta, phi));

		// for swimming backwards
		px = -px;
		py = -py;
		pz = -pz;
		theta = FastMath.acos2Deg(pz / P);
		phi = FastMath.atan2Deg(py, px);
		System.out.println(String.format("Swim backwards use theta: %9.6f  phi: %9.6f", theta, phi));

		System.out.println("--------------------------------------\n");
	}

	/**
	 * Create a Box that has a prompt, text field, and unit string
	 * 
	 * @param prompt
	 * @param tf
	 * @param units
	 * @param promptWidth
	 * @return a Box holding a labeled text field
	 */
	private Box labeledTextField(String prompt, JTextField tf, String units, final int promptWidth) {
		Box box = Box.createHorizontalBox();

		JLabel plabel = new JLabel(prompt) {
			@Override
			public Dimension getPreferredSize() {
				if (promptWidth > 0) {
					return new Dimension(promptWidth, 18);
				} else {
					return super.getPreferredSize();
				}
			}
		};
		box.add(plabel);
		box.add(Box.createHorizontalStrut(6));
		box.add(tf);
		if (units != null) {
			box.add(Box.createHorizontalStrut(6));
			box.add(new JLabel(units));
		}

		return box;
	}

	/**
	 * A new particle was selected
	 */
	private void selectedParticle() {
		// System.err.println("Selected particle: " +
		// _lundComboBox.getSelectedId());
		setMomentum();
	}

	/**
	 * Create the panel for setting the vertex
	 * 
	 * @return the panel holding the vertex pane
	 */
	private JPanel vertexPanel() {
		JPanel panel = new JPanel();
		Box box = Box.createVerticalBox();

		_vertexX = new JTextField(8);
		_vertexY = new JTextField(8);
		_vertexZ = new JTextField(8);

		_vertexX.setText("0.0");
		_vertexY.setText("0.0");
		_vertexZ.setText("0.0");

		box.add(labeledTextField("X:", _vertexX, "cm", 20));
		box.add(Box.createVerticalStrut(5));
		box.add(labeledTextField("Y:", _vertexY, "cm", 20));
		box.add(Box.createVerticalStrut(5));
		box.add(labeledTextField("Z:", _vertexZ, "cm", 20));
		box.add(Box.createVerticalStrut(5));

		panel.add(box);
		panel.setBorder(new CommonBorder("Track Vertex"));
		return panel;
	}

	// initial conditions
	private JPanel initConditionsPanel() {
		JPanel panel = new JPanel();
		Box box = Box.createVerticalBox();
		_momentumTextField = new JTextField(8);
		_momentumTextField.setEditable(true);

		_momentumTextField.setText("" + String.format("%-9.5f", _oldMomentum));
		_momentumTextField.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				setMomentum();
			}
		});

		box.add(labeledTextField(MOMENTUMMAG, _momentumTextField, "GeV/c", -1));

		_theta = new JTextField(8);
		_phi = new JTextField(8);

		_theta.setText("15.0");
		_phi.setText("0.0");

		box.add(labeledTextField(SMALL_THETA, _theta, "deg", 20));
		box.add(Box.createVerticalStrut(5));
		box.add(labeledTextField(SMALL_PHI, _phi, "deg", 20));
		box.add(Box.createVerticalStrut(5));

		panel.add(box);
		panel.setBorder(new CommonBorder("Initial Momentum and Direction"));
		return panel;
	}

	// create the cutoff panel
	private JPanel cutoffPanel() {
		JPanel panel = new JPanel();
		Box box = Box.createVerticalBox();
		box.add(cutoffType());

		_fixedZ = new JTextField(8);
		_accuracy = new JTextField(8);

		_fixedZ.setText("575.0");
		_accuracy.setText("10");

		box.add(Box.createVerticalStrut(5));
		box.add(labeledTextField("      Stopping Z", _fixedZ, "cm", -1));
		box.add(Box.createVerticalStrut(5));
		box.add(labeledTextField("        Accuracy", _accuracy, "microns", -1));
		box.add(Box.createVerticalStrut(5));

		panel.add(box);
		panel.setBorder(new CommonBorder("Integration Controls"));
		return panel;
	}

	public void setFixedZSelected(boolean selected) {
		_standardCutoff.setSelected(!selected);
		_fixedZCutoff.setSelected(selected);
	}

	private JPanel cutoffType() {

		_standardCutoff = new JRadioButton("Standard");
		_fixedZCutoff = new JRadioButton("Fixed Z");
		_standardCutoff.setSelected(true);

		ButtonGroup bga = new ButtonGroup();

		ButtonGroup bg = new ButtonGroup();
		bg.add(_standardCutoff);
		bg.add(_fixedZCutoff);

		ActionListener al = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
			}

		};

		_standardCutoff.addActionListener(al);
		_fixedZCutoff.addActionListener(al);
		JPanel spanel = new JPanel();
		spanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 0));
		spanel.add(_standardCutoff);
		spanel.add(_fixedZCutoff);


		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.add(spanel);
		return panel;
	}

	/**
	 * Create the panel that allows the user to select the energy
	 * 
	 * @return the panel for selecting the energy.
	 */
	private JPanel energyPanel() {
		JPanel panel = new JPanel();

		Box box = Box.createVerticalBox();

		_massTextField = new JTextField(8);
		_relativisticGamma = new JTextField(8);
		_relativisticBeta = new JTextField(8);
		_totalEnergyTextField = new JTextField(8);

		disable(_massTextField);
		disable(_relativisticGamma);
		disable(_relativisticBeta);
		disable(_totalEnergyTextField);

		box.add(Box.createVerticalStrut(5));
		box.add(labeledTextField(MASS, _massTextField, " GeV/c" + SUPER2, -1));
		box.add(Box.createVerticalStrut(5));
		box.add(labeledTextField(RELGAMMA, _relativisticGamma, null, -1));
		box.add(Box.createVerticalStrut(5));
		box.add(labeledTextField(RELBETA, _relativisticBeta, null, -1));
		box.add(Box.createVerticalStrut(5));
		box.add(labeledTextField(TOTENERGY, _totalEnergyTextField, " GeV", -1));
		box.add(Box.createVerticalStrut(5));
		box.add(Box.createVerticalStrut(5));

		panel.add(box);
		panel.setBorder(new CommonBorder("Particle Energy"));
		return panel;
	}

	private void disable(JTextField tf) {
		tf.setEditable(false);
		tf.setBackground(Color.black);
		tf.setForeground(Color.cyan);
	}

	// set the momentum
	private void setMomentum() {

		double momentum = 0.0;
		try {
			momentum = Double.parseDouble(_momentumTextField.getText());
		} catch (Exception e) {
			momentum = _oldMomentum;
			_momentumTextField.setText("" + String.format("%-9.5f", _oldMomentum));
			return;
		}

		_oldMomentum = momentum;
		LundId lid = _lundComboBox.getSelectedId();
		// mass GeV
		double mass = lid.getMass();

		_energy = Math.sqrt(momentum * momentum + mass * mass);

		_gamma = _energy / mass;
		_beta = Math.sqrt(1.0 - 1.0 / (_gamma * _gamma));

		_relativisticGamma.setText(String.format("%-9.5f", _gamma));
		_relativisticBeta.setText(String.format("%-13.9f", _beta));
		_massTextField.setText(String.format("%-10.6f", mass));
		_totalEnergyTextField.setText(String.format("%-9.5f", _energy));
	}

	// user has hit ok or cancel
	private void doClose(int reason) {
		setVisible(false);
	}

	/**
	 * Create a nice padded panel.
	 * 
	 * @param hpad      the pixel pad on the left and right
	 * @param vpad      the pixel pad on the top and bottom
	 * @param component the main component placed in the center.
	 * @return the padded panel
	 */
	public static JPanel paddedPanel(int hpad, int vpad, Component component) {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());

		if (hpad > 0) {
			panel.add(Box.createHorizontalStrut(hpad), BorderLayout.WEST);
			panel.add(Box.createHorizontalStrut(hpad), BorderLayout.EAST);
		}
		if (hpad > 0) {
			panel.add(Box.createVerticalStrut(vpad), BorderLayout.NORTH);
			panel.add(Box.createVerticalStrut(vpad), BorderLayout.SOUTH);
		}

		panel.add(component, BorderLayout.CENTER);
		return panel;
	}

	/**
	 * Center a component.
	 * 
	 * @param component The Component to center.
	 * @param dh        offset from horizontal center.
	 * @param dv        offset from vertical center.
	 */
	public static void centerComponent(Component component) {

		if (component == null)
			return;

		try {
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			Dimension componentSize = component.getSize();
			if (componentSize.height > screenSize.height) {
				componentSize.height = screenSize.height;
			}
			if (componentSize.width > screenSize.width) {
				componentSize.width = screenSize.width;
			}

			int x = ((screenSize.width - componentSize.width) / 2);
			int y = ((screenSize.height - componentSize.height) / 2);

			component.setLocation(x, y);

		} catch (Exception e) {
			component.setLocation(200, 200);
			e.printStackTrace();
		}
	}

	// for a nice border
	public class CommonBorder extends TitledBorder {

		public Border etched = BorderFactory.createEtchedBorder();
		public Font font = new Font("SandSerif", Font.PLAIN, 9);

		public CommonBorder() {
			super(BorderFactory.createEtchedBorder());
			setTitleColor(Color.blue);
			setTitleFont(font);
		}

		public CommonBorder(String title) {
			this();
			setTitle(title);
		}
	}

}