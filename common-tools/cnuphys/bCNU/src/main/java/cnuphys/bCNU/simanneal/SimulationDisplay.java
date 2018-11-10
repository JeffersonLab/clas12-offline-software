package cnuphys.bCNU.simanneal;

import java.awt.Graphics;
import javax.swing.JComponent;

public abstract class SimulationDisplay extends JComponent implements IUpdateListener {
	
	protected Simulation _simulation;

	public SimulationDisplay(Simulation simulation) {
		_simulation = simulation;
		_simulation.addUpdateListener(this);
		setOpaque(false);
	}
	
	@Override
	public abstract void paintComponent(Graphics g);

	
	@Override
	public void updateSolution(Simulation simulation, Solution newSolution, Solution oldSolution) {
		repaint();
	}
	
	@Override
	public void reset(Simulation simulation) {
	}

	@Override
	public void stateChange(Simulation simulation, SimulationState oldState, SimulationState newState) {
	}

}
