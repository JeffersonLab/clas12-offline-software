package cnuphys.fastMCed.eventgen;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JRadioButtonMenuItem;

import cnuphys.fastMCed.eventgen.filegen.LundFileEventGenerator;
import cnuphys.fastMCed.eventgen.random.RandomEventGenerator;
import cnuphys.fastMCed.eventgen.sweep.SweepEventGenerator;
import cnuphys.fastMCed.eventio.PhysicsEventManager;

public class GeneratorManager implements ActionListener {
	
	//suggestions
	private static double _pMin = 0.5; // GeV/c
	private static double _pMax = 8.5; // GeV/c
	private static double _pPerpMax = 2.5; // GeV/c
	private static double _thetaMin = 5.; // degrees
	private static double _thetaMax = 40.; // degrees
	private static double _phiMin = -20; // degrees
	private static double _phiMax = 20.; // degrees
	
	//menu stuff
	private JMenu _menu;
	private static JRadioButtonMenuItem _fileGenerator;
	private static JRadioButtonMenuItem _sweepGenerator;
	private static JRadioButtonMenuItem _randomGenerator;
		
	//singleton
	private static GeneratorManager instance;
	
	//private constructor for 
	private GeneratorManager() {
	}
	
	/**
	 * Access to the singleton GeneratorManager
	 * @return the GeneratorManager
	 */
	public static GeneratorManager getInstance() {
		if (instance == null) {
			instance = new GeneratorManager();
		}
		return instance;
	}
	
	/**
	 * Get the Generator menu
	 * @return
	 */
	public JMenu getMenu() {
		if (_menu == null) {
			createMenu();
		}
		return _menu;
	}

	//create the menu
	private void createMenu() {
		ButtonGroup bgroup = new ButtonGroup();
		_menu = new JMenu("Generators");
		_fileGenerator = menuItem("Lund File Generator...", bgroup);
		_menu.addSeparator();
		_sweepGenerator = menuItem("Sweep Generator...", bgroup);
		_randomGenerator = menuItem("Random Generator...", bgroup);
	}
	
	private JRadioButtonMenuItem menuItem(String label, ButtonGroup bg) {
				
		JRadioButtonMenuItem item = new JRadioButtonMenuItem(label);
		bg.add(item);
		item.addActionListener(this);
		_menu.add(item);

		return item;
	}
	
	/**
	 * Set the file generator as the selected generator
	 */
	public void setFileGeneratorSelected() {
		System.err.println("Selecting file generator radio item");
		_fileGenerator.setSelected(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();

		if (source == _fileGenerator) {
			LundFileEventGenerator lfgenerator = PhysicsEventManager.getInstance().getFileEventGenerator();

			if (lfgenerator != null) {
				PhysicsEventManager.getInstance().setEventGenerator(lfgenerator);
				PhysicsEventManager.getInstance().reloadCurrentEvent();
			}
		} else if (source == _randomGenerator) {
			RandomEventGenerator generator = RandomEventGenerator.createRandomGenerator();
			if (generator != null) {
				PhysicsEventManager.getInstance().setEventGenerator(generator);
			}
		}
		else if (source == _sweepGenerator) {
			SweepEventGenerator generator = SweepEventGenerator.createSweepGenerator();
			if (generator != null) {
				PhysicsEventManager.getInstance().setEventGenerator(generator);
			}
		}
	}
	
	/**
	 * Get the default value for p-perp (Gev/c)
	 * @return the default value for p-perp
	 */
	public static double getPPerpMax() {
		return _pPerpMax;
	}
	
	/**
	 * Set the default value for p-perp (Gev/c)
	 * @param pPerpMax the default value for p-perp
	 */
	public static void setPPerpMax(double pPerpMax) {
		_pPerpMax = pPerpMax;
	}

	public static double getPMin() {
		return _pMin;
	}

	public static void setpMin(double pMin) {
		GeneratorManager._pMin = pMin;
	}

	public static double getPMax() {
		return _pMax;
	}

	public static void setpMax(double pMax) {
		GeneratorManager._pMax = pMax;
	}

	public static double getThetaMin() {
		return _thetaMin;
	}

	public static void setThetaMin(double thetaMin) {
		GeneratorManager._thetaMin = thetaMin;
	}

	public static double getThetaMax() {
		return _thetaMax;
	}

	public static void setThetaMax(double thetaMax) {
		GeneratorManager._thetaMax = thetaMax;
	}

	public static double getPhiMin() {
		return _phiMin;
	}

	public static void setPhiMin(double phiMin) {
		GeneratorManager._phiMin = phiMin;
	}

	public static double getPhiMax() {
		return _phiMax;
	}

	public static void setPhiMax(double phiMax) {
		GeneratorManager._phiMax = phiMax;
	}

}
