package cnuphys.bCNU.simanneal.example.ts;

import java.awt.Dimension;
import java.awt.Insets;
import javax.swing.JPanel;

import cnuphys.bCNU.attributes.Attributes;
import cnuphys.bCNU.simanneal.Simulation;
import cnuphys.bCNU.simanneal.SimulationPanel;

public class TSPanel extends JPanel {
	
	//Simulation panel for display
	private TSDisplay _tsDisplay;
	
	//the solution
	private TravelingSalesperson _travPerson;
	
	//the simulation panel
	private SimulationPanel _simPanel;

	//the simulation
	private Simulation _simulation;
	
	public TSPanel() {
		//initial solution
		
		int numCity = 400;
		River river = River.NORIVER;
		
		_travPerson = TravelingSalesperson.getInstance();
		_travPerson.reset(numCity, river);
		
		System.out.println("City count: " + _travPerson.count());
		System.out.println("Initial distance: " + _travPerson.getDistance());
		System.out.println("Initial energy: " + _travPerson.getEnergy());
		TravelingSalesperson neighbor = (TravelingSalesperson) _travPerson.getNeighbor();
		System.out.println("Initial distance: " + _travPerson.getDistance());
		System.out.println("Initial energy: " + _travPerson.getEnergy());
		System.out.println("Neighbor distance: " + neighbor.getDistance());
		System.out.println("Neighbor energy: " + neighbor.getEnergy());
		
		
		Attributes attributes = new Attributes();
		attributes.add(Simulation.COOLRATE, 0.03);
		attributes.add(Simulation.RANDSEED, -1L);
		attributes.add(Simulation.THERMALCOUNT, 200);
		attributes.add(Simulation.MAXSTEPS, 1000);
		
		_simulation = new Simulation(_travPerson, attributes);
		_tsDisplay = new TSDisplay(_travPerson);

		_simulation.addUpdateListener(_tsDisplay);

		
		_travPerson.setSimulation(_simulation);
		
		//add initial values
		_travPerson.temps.add(_simulation.getTemperature());
		_travPerson.dists.add(_travPerson.getDistance());
		
		_tsDisplay.setPreferredSize(new Dimension(600, 600));
		
		_simPanel = new SimulationPanel(_simulation, _tsDisplay);
		
		add(_simPanel);
	}
	
	/**
	 * Get the underlying simulation
	 * @return the underlying simulation
	 */
	public Simulation getSimulation() {
		return _simulation;
	}

	
	@Override
	public Insets getInsets() {
		Insets def = super.getInsets();
		return new Insets(def.top + 2, def.left + 2, def.bottom + 2,
				def.right + 2);
	}

}
