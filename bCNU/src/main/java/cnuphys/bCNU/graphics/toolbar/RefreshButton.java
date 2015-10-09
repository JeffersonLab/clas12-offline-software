package cnuphys.bCNU.graphics.toolbar;

import java.awt.event.ActionEvent;

import cnuphys.bCNU.graphics.container.IContainer;

/**
 * Refresh button just causes the container to be repainted.
 * 
 * @author heddle
 * 
 */
@SuppressWarnings("serial")
public class RefreshButton extends ToolBarButton {

	/**
	 * Create the Refresh button.
	 * 
	 * @param container
	 *            the container this button applies to.
	 */
	public RefreshButton(IContainer container) {
		super(container, "images/refresh.gif", "Refresh");
	}

	/**
	 * This is what I do if I am pressed
	 * 
	 * @param e
	 *            The causal event.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		container.refresh();
		container.getToolBar().resetDefaultSelection();
	}

}
