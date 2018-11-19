package cnuphys.bCNU.simanneal.example.ts;

import java.awt.Component;

import cnuphys.bCNU.dialog.DialogUtilities;
import cnuphys.bCNU.dialog.SimpleDialog;

public class TSDialog extends SimpleDialog {

	public TSDialog() {
		super("Traveling Salesperson (Simulated Annealing)", false, "Close");
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
		TSSimulation simulation = new TSSimulation();

		TSPanel tsPanel = new TSPanel(simulation);

		return tsPanel;
	}
	
	
	public static void main(String arg[]) {
		TSDialog dialog = new TSDialog();
		dialog.setVisible(true);
	}

}
