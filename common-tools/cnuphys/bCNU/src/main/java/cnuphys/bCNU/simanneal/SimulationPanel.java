package cnuphys.bCNU.simanneal;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import cnuphys.bCNU.attributes.AttributePanel;
import cnuphys.bCNU.util.Fonts;

/**
 * This panel will display the attributes for the simulation, the
 * run and reset buttons, and a plot
 * @author heddle
 *
 */
public class SimulationPanel extends JPanel implements ActionListener, IUpdateListener {
	
	//the underlying simulation
	private Simulation _simulation;
	
	//the content (display_ component
	private JComponent _content;
	
	//the attribute panel
	private AttributePanel _attributePanel;
	
	private JLabel _stateLabel;
	
	private SimulationPlot _simPlot;

	
	//the buttons
	private JButton runButton;
	private JButton stopButton;
	private JButton pauseButton;
	private JButton resumeButton;
	private JButton resetButton;

	/**
	 * Create a panel to hold all the optics for the simulation
	 * @param simulation the simulation
	 * @param content the custom content, e.g. a map for the traveling salesperson problem
	 */
	public SimulationPanel(Simulation simulation, JComponent content) {
		setLayout(new BorderLayout(4, 4));
		_simulation = simulation;
		_simulation.addUpdateListener(this);
		_content = content;
		add(_content, BorderLayout.WEST);
		
		addEast();
		addCenter();
		
	}
	
	/**
	 * Get the simulation plot
	 * @return the simulation plot
	 */
	public SimulationPlot getSimulationPlot() {
		return _simPlot;
	}
	
	private JPanel insetPanel() {
		JPanel panel = new JPanel() {
			@Override
			public Insets getInsets() {
				Insets def = super.getInsets();
				return new Insets(def.top + 2, def.left + 2, def.bottom + 2,
						def.right + 2);
			}
			
		};
		panel.setLayout(new BorderLayout(4, 4));
		return panel;
	}
	
	//put the sim plot in the center
	private void addCenter() {
		JPanel panel = insetPanel();
		
		_simPlot = new SimulationPlot(_simulation);

		
		panel.setLayout(new BorderLayout(4, 4));
		panel.add(_simPlot, BorderLayout.CENTER);

	    add(panel, BorderLayout.CENTER);
	}

	//add the east panel
	private void addEast() {
		JPanel panel = insetPanel();
		
		//state label in north
		_stateLabel = new JLabel("State:            ");
	    panel.add(_stateLabel, BorderLayout.NORTH);
	    
		//attributes in center of east panel
		_attributePanel = new AttributePanel(_simulation.getAttributes());
			
	    panel.add(_attributePanel, BorderLayout.CENTER);
		
	    //buttons in south of east panel
	    JPanel bPanel = new JPanel();
	    bPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 6, 2));
	    runButton = makeButton("Run");
	    stopButton = makeButton("Stop");
	    pauseButton = makeButton("Pause");
	    resumeButton = makeButton("Resume");
	    resetButton = makeButton("Reset");

	    
	    bPanel.add(runButton);
	    bPanel.add(pauseButton);
	    bPanel.add(resumeButton);
	    bPanel.add(resetButton);
	    bPanel.add(stopButton);
	    panel.add(bPanel, BorderLayout.SOUTH);
	    
	    add(panel, BorderLayout.EAST);
	    fixPanelState();
	}
	
	//create a buttom
	private JButton makeButton(String label) {
		JButton button = new JButton(label);
		button.addActionListener(this);
		button.setFont(Fonts.smallFont);
		return button;
	}
	
	//fix the states of the buttons
	private void fixPanelState() {
		SimulationState state = _simulation.getSimulationState();
		_stateLabel.setText("State: " + state);
		
		switch (state) {
		case RUNNING:
			runButton.setEnabled(false);
			pauseButton.setEnabled(true);
			resumeButton.setEnabled(false);
			resetButton.setEnabled(false);
			stopButton.setEnabled(true);
			break;
			
		case PAUSED:
			runButton.setEnabled(false);
			pauseButton.setEnabled(false);
			resumeButton.setEnabled(true);
			resetButton.setEnabled(false);
			stopButton.setEnabled(true);
			break;
			
		case STOPPED:
			runButton.setEnabled(true);
			pauseButton.setEnabled(false);
			resumeButton.setEnabled(false);
			resetButton.setEnabled(true);
			stopButton.setEnabled(false);
			break;
		}
	}
	
	@Override
	public Insets getInsets() {
		Insets def = super.getInsets();
		return new Insets(def.top + 2, def.left + 2, def.bottom + 2,
				def.right + 2);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		Object source = e.getSource();
		
		if (source == runButton) {
			_simulation.setSimulationState(SimulationState.RUNNING);
			_simulation.startSimulation();
		}
		else if (source == pauseButton) {
			_simulation.setSimulationState(SimulationState.PAUSED);
		}
		else if (source == resumeButton) {
			_simulation.setSimulationState(SimulationState.RUNNING);
		}
		else if (source == resetButton) {
			_simulation.reset();
		}
		else if (source == stopButton) {
			_simulation.setSimulationState(SimulationState.STOPPED);
		}
	}

	@Override
	public void updateSolution(Simulation simulation, Solution newSolution, Solution oldSolution) {
	}

	@Override
	public void reset(Simulation simulation) {
	}

	@Override
	public void stateChange(Simulation simulation, SimulationState oldState, SimulationState newState) {
		fixPanelState();
	}

}
