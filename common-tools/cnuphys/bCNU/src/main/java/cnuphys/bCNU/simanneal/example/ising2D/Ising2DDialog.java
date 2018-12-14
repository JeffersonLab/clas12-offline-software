package cnuphys.bCNU.simanneal.example.ising2D;

import java.awt.Component;

import cnuphys.bCNU.dialog.DialogUtilities;
import cnuphys.bCNU.dialog.SimpleDialog;

public class Ising2DDialog extends SimpleDialog {

	public Ising2DDialog() {
		super("2D Ising Model (Simulated Annealing)", false, "Close");
		DialogUtilities.centerDialog(this);
	}

	/**
	 * Override to create the component that goes in the center. Usually this is
	 * the "main" component.
	 * 
	 * @return the component that is placed in the center
	 */
	@Override
	protected Component createCenterComponent() {
		Ising2DSimulation simulation = new Ising2DSimulation();

		Ising2DPanel i2dPanel = new Ising2DPanel(simulation);

		return i2dPanel;
	}
	
	
	public static void main(String arg[]) {
		Ising2DDialog dialog = new Ising2DDialog();
		dialog.setVisible(true);
	}

}
