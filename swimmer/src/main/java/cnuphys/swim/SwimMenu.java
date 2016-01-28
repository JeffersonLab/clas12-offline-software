package cnuphys.swim;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTextField;
import javax.swing.MenuSelectionManager;

import cnuphys.lund.DoubleFormat;
import cnuphys.lund.LundTrackDialog;
import cnuphys.rk4.RungeKutta4;

/**
 * Create the menu that control swimming (trajectory integration)
 * 
 * @author DHeddle
 * 
 */
@SuppressWarnings("serial")
public class SwimMenu extends JMenu implements ActionListener {

	// swim all particles in the generated bank
	private JMenuItem _swimAllMCItem;

	// always swim
	private JCheckBoxMenuItem _alwaysSwimMCItem;

	// show or hide
	private JRadioButtonMenuItem _showMonteCarloTracks;
	private JRadioButtonMenuItem _hideMonteCarloTracks;

	// swim all particles in the recon bank
	private JMenuItem _swimAllReconItem;

	// always swim
	private JCheckBoxMenuItem _alwaysSwimReconItem;

	// show or hide
	private JRadioButtonMenuItem _showReconTracks;
	private JRadioButtonMenuItem _hideReconTracks;

	// interested parties can listen for property changes
	public static final String TRAJ_CLEARED_MC_PROP = "MC Trajectories Cleared";
	public static final String TRAJ_CLEARED_RECON_PROP = "Recon Trajectories Cleared";
	public static final String SWIM_ALL_MC_PROP = "Swim all MC";
	public static final String SWIM_ALL_RECON_PROP = "Swim all Recon";

	private boolean _showMonteCarlo = true;
	private boolean _showRecon = true;
	
	//singleton
	private static SwimMenu _instance;

	/**
	 * Create a menu for controlling swimming
	 * 
	 * @param field
	 *            object that implements the magnetic field interface.
	 */
	private SwimMenu() {
		super("Swim");

		add(getLundDialogMenuItem());
		addSeparator();

		// MC related
		_alwaysSwimMCItem = new JCheckBoxMenuItem(
				"Always Swim all Monte Carlo Tracks", true);
		add(_alwaysSwimMCItem);
		add(getSwimAllMenuMCItem());

		// hide or show
		ButtonGroup bgmc = new ButtonGroup();
		_showMonteCarloTracks = createRadioMenuItem("Show Monte Carlo Tracks",
				bgmc, _showMonteCarlo);
		_hideMonteCarloTracks = createRadioMenuItem("Hide Monte Carlo Tracks",
				bgmc, !_showMonteCarlo);
		add(getClearAllMCMenuItem());
		addSeparator();

		// Rcecon related
		_alwaysSwimReconItem = new JCheckBoxMenuItem(
				"Always Swim all Reconstructed Tracks", true);
		add(_alwaysSwimReconItem);
		add(getSwimAllMenuReconItem());

		// hide or show
		ButtonGroup bgrecon = new ButtonGroup();
		_showReconTracks = createRadioMenuItem("Show Reconstructed Tracks",
				bgrecon, _showRecon);
		_hideReconTracks = createRadioMenuItem("Hide Reconstructed Tracks",
				bgrecon, !_showRecon);
		add(getClearAllReconMenuItem());
		addSeparator();
		add(createEpsPanel());
		add(createMaxSSPanel());
	}
	
	/**
	 * Accessor for the SwimMenu singleton
	 * @return the SwimMenu singleton
	 */
	public static SwimMenu getInstance() {
		if (_instance == null) {
			_instance = new SwimMenu();
		}
		return _instance;
	}

	// convenience method for adding a radio button
	private JRadioButtonMenuItem createRadioMenuItem(String label,
			ButtonGroup bg, boolean on) {
		JRadioButtonMenuItem mi = new JRadioButtonMenuItem(label, on);
		mi.addActionListener(this);
		bg.add(mi);
		add(mi);
		return mi;
	}

	private JPanel createEpsPanel() {
		JPanel sp = new JPanel();
		sp.setBackground(Color.white);

		sp.setLayout(new FlowLayout(FlowLayout.LEFT, 4, 0));

		JLabel label = new JLabel("Swimming Tolerance: ");

		String s = DoubleFormat.doubleFormat(Swimmer.getEps(), 1, true);
		final JTextField epsTF = new JTextField(s, 10);

		KeyAdapter ka = new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent kev) {
				if (kev.getKeyCode() == KeyEvent.VK_ENTER) {
					double enumber;
					MenuSelectionManager.defaultManager().clearSelectedPath();
					try {
						enumber = Double.parseDouble(epsTF.getText());
						enumber = Math.min(1.0e-4, Math.max(1.0e-10, enumber));
						Swimmer.setCLASTolerance(enumber);
					} catch (Exception e) {
						// e.printStackTrace();
						enumber = Swimmer.getEps();
					}
					String s = DoubleFormat.doubleFormat(Swimmer.getEps(), 1,
							true);
					epsTF.setText(s);
				}
			}
		};
		epsTF.addKeyListener(ka);

		sp.add(label);
		sp.add(epsTF);
		epsTF.setEnabled(true);
		return sp;
	}

	private JPanel createMaxSSPanel() {
		JPanel sp = new JPanel();
		sp.setBackground(Color.white);
		sp.setLayout(new FlowLayout(FlowLayout.LEFT, 4, 0));

		JLabel label = new JLabel("Max Stepsize (cm): ");

		// convert to cm

		System.err.println("Current Max Stepsize  (cm): " + 100
				* RungeKutta4.getMaxStepSize());
		String s = DoubleFormat.doubleFormat(
				100 * RungeKutta4.getMaxStepSize(), 2, false);
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
						System.err.println("Changing max step size to "
								+ enumber + " cm");
						RungeKutta4.setMaxStepSize(enumber / 100); // to meters
					} catch (Exception e) {
						// e.printStackTrace();
						enumber = Swimmer.getEps();
					}
					String s = DoubleFormat.doubleFormat(
							100 * RungeKutta4.getMaxStepSize(), 2, false);
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
	 * Always swim montecarlo particles
	 * 
	 * @return <code>true</code> if we should swim all montecarlo particles
	 */
	public boolean isAlwaysSwimMC() {
		return _alwaysSwimMCItem.isSelected();
	}

	/**
	 * Always swim reconstructed particles
	 * 
	 * @return <code>true</code> if we should swim all reconstructed particles
	 */
	public boolean isAlwaysSwimRecon() {
		return _alwaysSwimReconItem.isSelected();
	}

	/**
	 * Control with the swim all MC item is enabled. The mc dialog item goes
	 * along with it: they should be enabled or disabled together.
	 * 
	 * @param enabled
	 *            if <code>true</code> then it is enabled.
	 */
	public void setSwimAllMCEnabled(boolean enabled) {
		_swimAllMCItem.setEnabled(enabled);
	}

	/**
	 * Control with the swim all recon item is enabled.
	 * 
	 * @param enabled
	 *            if <code>true</code> then it is enabled.
	 */
	public void setSwimAllReconEnabled(boolean enabled) {
		_swimAllReconItem.setEnabled(enabled);
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

	/**
	 * Get the menu item to swim all MonteCarlo particles
	 * 
	 * @return the menu item for swimming all MonteCarlo particles
	 */
	private JMenuItem getSwimAllMenuMCItem() {
		_swimAllMCItem = new JMenuItem("Swim all Monte Carlo Tracks");

		final SwimMenu menu = this;
		ActionListener al = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Swimming.clearMCTrajectories();
				menu.firePropertyChange(TRAJ_CLEARED_MC_PROP, 0, 1);
				menu.firePropertyChange(SWIM_ALL_MC_PROP, 0, 1);
			}

		};

		_swimAllMCItem.addActionListener(al);
		// _swimAllGeneratedItem.setEnabled(false);
		return _swimAllMCItem;
	}

	/**
	 * Get the menu item to swim all recon particles
	 * 
	 * @return the menu item for swimming all MonteCarlo particles
	 */
	private JMenuItem getSwimAllMenuReconItem() {
		_swimAllReconItem = new JMenuItem("Swim all Reconstructed Tracks");

		final SwimMenu menu = this;
		ActionListener al = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Swimming.clearReconTrajectories();
				menu.firePropertyChange(TRAJ_CLEARED_RECON_PROP, 0, 1);
				menu.firePropertyChange(SWIM_ALL_RECON_PROP, 0, 1);
			}

		};

		_swimAllReconItem.addActionListener(al);
		return _swimAllReconItem;
	}

	/**
	 * Get the menu item to clear mc trajectories
	 * 
	 * @return the menu item for swimming all MonteCarlo particles
	 */
	private JMenuItem getClearAllMCMenuItem() {
		JMenuItem mi = new JMenuItem("Clear All Monte Carlo Trajectories");

		final SwimMenu menu = this;
		ActionListener al = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Swimming.clearMCTrajectories();
				menu.firePropertyChange(TRAJ_CLEARED_MC_PROP, 0, 1);
			}

		};

		mi.addActionListener(al);
		return mi;
	}

	/**
	 * Get the menu item to clear reconstructed trajectories
	 * 
	 * @return the menu item for swimming all MonteCarlo particles
	 */
	private JMenuItem getClearAllReconMenuItem() {
		JMenuItem mi = new JMenuItem("Clear All Reconstructed Trajectories");

		final SwimMenu menu = this;
		ActionListener al = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Swimming.clearReconTrajectories();
				menu.firePropertyChange(TRAJ_CLEARED_RECON_PROP, 0, 1);
			}

		};

		mi.addActionListener(al);
		return mi;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		_showMonteCarlo = _showMonteCarloTracks.isSelected();
		_showRecon = _showReconTracks.isSelected();

		Swimming.notifyListeners();
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