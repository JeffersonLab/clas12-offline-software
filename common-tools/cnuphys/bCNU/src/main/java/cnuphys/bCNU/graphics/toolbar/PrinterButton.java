package cnuphys.bCNU.graphics.toolbar;

import java.awt.event.ActionEvent;
import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.util.PrintUtilities;

@SuppressWarnings("serial")
public class PrinterButton extends ToolBarButton {

	/**
	 * Create a button to print the container.
	 * 
	 * @param container
	 *            the owner container.
	 */
	public PrinterButton(IContainer container) {
		super(container, "images/printer.gif", "Send view to printer");
	}

	/**
	 * This is what I do if I am pressed
	 * 
	 * @param actionEvent
	 *            the causal event.
	 */
	@Override
	public void actionPerformed(ActionEvent actionEvent) {
		
		if (container.handledPrint()) {
			return;
		}

		PrintUtilities.printComponent(container.getComponent());
	}
}
