package cnuphys.bCNU.simanneal.example.ising2D;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import cnuphys.bCNU.attributes.Attributes;
import cnuphys.bCNU.simanneal.Simulation;
import cnuphys.bCNU.simanneal.Solution;

public class Ising2DSimulation extends Simulation {
	
	//custom attributes
	public static final String NUMROWS = "num rows";
	public static final String NUMCOLUMNS = "num columns";
	
	private Ising2DSolution _i2dSolution;


	@Override
	protected Solution setInitialSolution() {
		_i2dSolution = new Ising2DSolution(this);
		return _i2dSolution;
	}
	
	/**
	 * Get the number of rows in the current simulation
	 * @return the number of rows
	 */
	public int getNumRows() {
		return _i2dSolution.getNumRows();
	}

	/**
	 * Get the number of columns in the current simulation
	 * @return the number of columns
	 */
	public int getNumColumns() {
		return _i2dSolution.getNumColumns();
	}


	@Override
	protected void setInitialAttributes(Attributes attributes) {
		
		//change some defaults
		attributes.setValue(Simulation.PLOTTITLE, "2D Ising Model");
		attributes.setValue(Simulation.YAXISLABEL, "|Magnetization|");
		attributes.setValue(Simulation.XAXISLABEL, "Temp");
		attributes.setValue(Simulation.USELOGTEMP, false);
		attributes.setValue(Simulation.COOLRATE, 0.002);
		attributes.setValue(Simulation.THERMALCOUNT, 2000);
		attributes.setValue(Simulation.MAXSTEPS, 10000);
		
		//custom
		attributes.add(NUMROWS, 100);
		attributes.add(NUMCOLUMNS, 100);
		
	}
	

	//main program for testing
	public static void main(String arg[]) {

		final JFrame frame = new JFrame();

		// set up what to do if the window is closed
		WindowAdapter windowAdapter = new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent event) {
				System.exit(1);
			}
		};

		frame.addWindowListener(windowAdapter);

		frame.setLayout(new BorderLayout());
		
		Ising2DSimulation simulation = new Ising2DSimulation();

		Ising2DPanel tsPanel = new Ising2DPanel(simulation);
		
		frame.add(tsPanel, BorderLayout.CENTER);

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				frame.pack();
				frame.setVisible(true);
				frame.setLocationRelativeTo(null);
			}
		});
	}

}
