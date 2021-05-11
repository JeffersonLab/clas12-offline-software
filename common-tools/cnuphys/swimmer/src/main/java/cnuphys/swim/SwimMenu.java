package cnuphys.swim;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTextField;
import javax.swing.MenuSelectionManager;

import cnuphys.lund.DoubleFormat;
import cnuphys.lund.LundTrackDialog;
import cnuphys.rk4.RungeKutta;

/**
 * Create the menu that control swimming (trajectory integration)
 * 
 * @author DHeddle
 * 
 */
@SuppressWarnings("serial")
public class SwimMenu extends JMenu implements ActionListener {

	// show or hide
	private JRadioButtonMenuItem _showMonteCarloTracks;
	private JRadioButtonMenuItem _hideMonteCarloTracks;

	// show or hide
	private JRadioButtonMenuItem _showReconTracks;
	private JRadioButtonMenuItem _hideReconTracks;

	// clear
	private JMenuItem _clearTracks;

	private boolean _showMonteCarlo = true;
	private boolean _showRecon = true;

	// singleton
	private static SwimMenu _instance;

	/**
	 * Create a menu for controlling swimming
	 * 
	 * @param field object that implements the magnetic field interface.
	 */
	private SwimMenu() {
		super("Swim");

		add(getLundDialogMenuItem());
		addSeparator();

		// hide or show MC
		ButtonGroup bgmc = new ButtonGroup();
		_showMonteCarloTracks = createRadioMenuItem("Show Monte Carlo Tracks", bgmc, _showMonteCarlo);
		_hideMonteCarloTracks = createRadioMenuItem("Hide Monte Carlo Tracks", bgmc, !_showMonteCarlo);
		addSeparator();
		_clearTracks = new JMenuItem("Clear all Tracks");
		_clearTracks.addActionListener(this);
		add(_clearTracks);
		addSeparator();

		// hide or show recon
		ButtonGroup bgrecon = new ButtonGroup();
		_showReconTracks = createRadioMenuItem("Show Reconstructed Tracks", bgrecon, _showRecon);
		_hideReconTracks = createRadioMenuItem("Hide Reconstructed Tracks", bgrecon, !_showRecon);
		addSeparator();
		add(createEpsPanel());
		add(createMaxSSPanel());
	}

	/**
	 * Accessor for the SwimMenu singleton
	 * 
	 * @return the SwimMenu singleton
	 */
	public static SwimMenu getInstance() {
		if (_instance == null) {
			_instance = new SwimMenu();
		}
		return _instance;
	}

	// convenience method for adding a radio button
	private JRadioButtonMenuItem createRadioMenuItem(String label, ButtonGroup bg, boolean on) {
		JRadioButtonMenuItem mi = new JRadioButtonMenuItem(label, on);
		mi.addActionListener(this);
		bg.add(mi);
		add(mi);
		return mi;
	}

	// swimming tolerance
	private JPanel createEpsPanel() {
		JPanel sp = new JPanel();
		sp.setBackground(Color.white);

		sp.setLayout(new FlowLayout(FlowLayout.LEFT, 4, 0));

		JLabel label = new JLabel("Swimming Tolerance: ");

		String s = DoubleFormat.doubleFormat(Swimmer.getEps(), 1, true);
		final JTextField epsTF = new JTextField(s, 10);

		ActionListener al = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent ae) {
				double enumber;
				MenuSelectionManager.defaultManager().clearSelectedPath();
				try {
					enumber = Double.parseDouble(epsTF.getText());
					enumber = Math.min(1.0e-4, Math.max(1.0e-10, enumber));
					System.err.println("new tolerance: " + enumber);
					Swimmer.setCLASTolerance(enumber);
				} catch (Exception e) {
					// e.printStackTrace();
					enumber = Swimmer.getEps();
				}
				String s = DoubleFormat.doubleFormat(Swimmer.getEps(), 1, true);
				epsTF.setText(s);
			}

		};

//		KeyAdapter ka = new KeyAdapter() {
//			@Override
//			public void keyReleased(KeyEvent kev) {
//				if (kev.getKeyCode() == KeyEvent.VK_ENTER) {
//					double enumber;
//					MenuSelectionManager.defaultManager().clearSelectedPath();
//					try {
//						enumber = Double.parseDouble(epsTF.getText());
//						enumber = Math.min(1.0e-4, Math.max(1.0e-10, enumber));
//						System.err.println("new tolerance: " + enumber);
//						Swimmer.setCLASTolerance(enumber);
//					} catch (Exception e) {
//						// e.printStackTrace();
//						enumber = Swimmer.getEps();
//					}
//					String s = DoubleFormat.doubleFormat(Swimmer.getEps(), 1,
//							true);
//					epsTF.setText(s);
//				}
//			}
//		};
//		epsTF.addKeyListener(ka);

		epsTF.addActionListener(al);

		sp.add(label);
		sp.add(epsTF);
		epsTF.setEnabled(true);
		return sp;
	}

	// step size
	private JPanel createMaxSSPanel() {
		JPanel sp = new JPanel();
		sp.setBackground(Color.white);
		sp.setLayout(new FlowLayout(FlowLayout.LEFT, 4, 0));

		JLabel label = new JLabel("Max Stepsize (cm): ");

		// convert to cm

//		System.err.println("Current Max Stepsize  (cm): " + 100
//				* RungeKutta4.getMaxStepSize());
		String s = DoubleFormat.doubleFormat(100 * RungeKutta.DEFMAXSTEPSIZE, 2, false);
		final JTextField maxSStf = new JTextField(s, 10);

		KeyAdapter ka = new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent kev) {
				if (kev.getKeyCode() == KeyEvent.VK_ENTER) {
					double enumber;
					MenuSelectionManager.defaultManager().clearSelectedPath();
					try {
						enumber = Double.parseDouble(maxSStf.getText());
						enumber = Math.min(100, Math.max(0.1, enumber));
						System.err.println("Changing max step size to " + enumber + " cm");
						RungeKutta.DEFMAXSTEPSIZE = (enumber / 100); // to meters
					} catch (Exception e) {
						// e.printStackTrace();
						enumber = Swimmer.getEps();
					}
					String s = DoubleFormat.doubleFormat(100 * RungeKutta.DEFMAXSTEPSIZE, 2, false);
					maxSStf.setText(s);
				}
			}
		};
		maxSStf.addKeyListener(ka);

		sp.add(label);
		sp.add(maxSStf);
		maxSStf.setEnabled(true);
		return sp;
	}

	/**
	 * Get the menu item for the lund track dialog used to swim a particle
	 * 
	 * @return the menu item for the lund track dialog used to swim a particle
	 */
	private JMenuItem getLundDialogMenuItem() {
		JMenuItem mi = new JMenuItem("Swim a Particle...");

		ActionListener al = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				LundTrackDialog.getInstance();
			}

		};

		mi.addActionListener(al);
		return mi;
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if (e.getSource() == _clearTracks) {
			clearTracks();
			return;
		}
		_showMonteCarlo = _showMonteCarloTracks.isSelected();
		_showRecon = _showReconTracks.isSelected();

		Swimming.notifyListeners();
	}

	// clear all the tracks
	private void clearTracks() {
		Swimming.clearAllTrajectories();
	}

	/**
	 * Check whether we should show Monte Carlo tracks
	 * 
	 * @return <code>true</code> if we should show MC tracks
	 */
	public boolean showMonteCarloTracks() {
		return _showMonteCarlo;
	}

	/**
	 * Check whether we should showreconstructed tracks
	 * 
	 * @return <code>true</code> if we should show MC tracks
	 */
	public boolean showReconstructedTracks() {
		return _showRecon;
	}

}