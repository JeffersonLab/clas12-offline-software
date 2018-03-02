package cnuphys.bCNU.simanneal;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * A panel that holds a simulation
 * @author heddle
 *
 */
public class SimulationPanel extends JPanel implements IUpdateListener {
	
	private JComponent _plotComponent;
	private JComponent _displayComponent;
	private JComponent _controlPanel;
	private Simulation _simulation;
	
	public SimulationPanel(Simulation simulation, JComponent plotComponent, JComponent displayComponent, JComponent controlPanel) {
		_plotComponent = plotComponent;
		_displayComponent = displayComponent;
		_controlPanel = controlPanel;
		
		setLayout(new BorderLayout(4, 4));
		
		
		if (plotComponent != null) {
			plotComponent.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
			add(plotComponent, BorderLayout.NORTH);
		}
		
		if (displayComponent != null) {
			displayComponent.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
			add(displayComponent, BorderLayout.CENTER);
		}
		
		if (controlPanel != null) {
			controlPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
			add(controlPanel, BorderLayout.EAST);
		}
		
		setSimulation(simulation);
		
	}
	
	public void setSimulation(Simulation simulation) {
		
		if (_simulation == simulation) {
			return;
		}
		
		
		if (_simulation != null) {
			_simulation.removeUpdateListener(this);
		}
		
		_simulation = simulation;
		_simulation.addUpdateListener(this);
	}
	

	@Override
	public void updateSolution(Simulation simulation, Solution newSolution, Solution oldSolution) {
	}

}
