package cnuphys.bCNU.simanneal.example.ising2D;

import java.awt.Dimension;
import java.awt.Insets;

import javax.swing.JPanel;

import cnuphys.bCNU.simanneal.SimulationPanel;

public class Ising2DPanel extends JPanel {
	
	//the simulation panel
	private SimulationPanel _simPanel;

	//the simulation
	private Ising2DSimulation _simulation;
	
	private Ising2DDisplay _i2dDisplay;

	
	public Ising2DPanel(Ising2DSimulation simulation) {
		
		_simulation = simulation;
		
		_i2dDisplay = new Ising2DDisplay(_simulation);
		_i2dDisplay.setPreferredSize(new Dimension(600, 600));
		_simPanel = new SimulationPanel(_simulation, _i2dDisplay);
		
		_simPanel.getSimulationPlot().getParameters().mustIncludeXZero(true);
		
		add(_simPanel);
		
	}
		
	@Override
	public Insets getInsets() {
		Insets def = super.getInsets();
		return new Insets(def.top + 2, def.left + 2, def.bottom + 2,
				def.right + 2);
	}

}
