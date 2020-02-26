package cnuphys.bCNU.simanneal.example.ts;

import java.awt.Dimension;
import java.awt.Insets;
import javax.swing.JPanel;

import cnuphys.bCNU.simanneal.SimulationPanel;

public class TSPanel extends JPanel {
	
	//Simulation panel for display
	private TSDisplay _tsDisplay;
	
	//the simulation panel
	private SimulationPanel _simPanel;

	//the simulation
	private TSSimulation _simulation;
	
	public TSPanel(TSSimulation simulation) {
		
		_simulation = simulation;
		
		_tsDisplay = new TSDisplay(_simulation);
		_tsDisplay.setPreferredSize(new Dimension(550, 550));
		_simPanel = new SimulationPanel(_simulation, _tsDisplay);
		add(_simPanel);
	}
		
	@Override
	public Insets getInsets() {
		Insets def = super.getInsets();
		return new Insets(def.top + 2, def.left + 2, def.bottom + 2,
				def.right + 2);
	}

}
