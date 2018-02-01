package cnuphys.bCNU.graphics.toolbar;

import java.awt.Dimension;
import java.awt.event.ActionEvent;

import cnuphys.bCNU.graphics.container.IContainer;

public class CloneButton extends ToolBarButton {
	
	private static Dimension preferredSize = new Dimension(38, 24);


	/**
	 * Create the zoom-in button, which zooms in by a fixed amount.
	 * 
	 * @param container
	 *            the container this button acts upon.
	 */
	public CloneButton(IContainer container) {
		super(container, "images/dolly.png", "Clone the view");
	}
	

	/**
	 * This is what I do if I am pressed
	 * 
	 * @param e
	 *            The causal event.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		container.getView().cloneView();
	}
	

	/**
	 * Get the preferred size.
	 * 
	 * @return the preferred size for layout.
	 */
	@Override
	public Dimension getPreferredSize() {
		return preferredSize;
	}
}
