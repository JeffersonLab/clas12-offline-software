package cnuphys.bCNU.simanneal.example.ts;

import java.awt.Component;

import cnuphys.bCNU.dialog.SimpleDialog;

public class TSDialog extends SimpleDialog {
	
	public TSDialog() {
		super("Traveling Salesperson", false, "Close");
	}
	
	/**
	 * Override to create the component that goes in the east.
	 * 
	 * @return the component that is placed in the east
	 */
	@Override
	protected Component createNorthComponent() {
		return null;
	}


	/**
	 * Override to create the component that goes in the east.
	 * 
	 * @return the component that is placed in the east
	 */
	@Override
	protected Component createEastComponent() {
		return null;
	}

	/**
	 * Override to create the component that goes in the west.
	 * 
	 * @return the component that is placed in the west.
	 */
	@Override
	protected Component createWestComponent() {
		return null;
	}

	/**
	 * Override to create the component that goes in the center. Usually this is
	 * the "main" component.
	 * 
	 * @return the component that is placed in the center
	 */
	@Override
	protected Component createCenterComponent() {
		return null;
	}


}
