package cnuphys.bCNU.graphics.toolbar;

import java.awt.event.ActionEvent;

import cnuphys.bCNU.graphics.container.IContainer;

/**
 * @author heddle
 * 
 *         TODO To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Style - Code Templates
 */
@SuppressWarnings("serial")
public class DeleteButton extends ToolBarButton {

	/**
	 * Create the button used to delete items on the container.
	 * 
	 * @param container
	 *            the owner container.
	 */
	public DeleteButton(IContainer container) {
		super(container, "images/delete.gif", "Delete selected items");
	}

	/**
	 * This is what I do if I am pressed, i.e. delete all selected items.
	 * 
	 * @param e
	 *            The causal event.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		container.deleteSelectedItems(container);
		container.getToolBar().resetDefaultSelection();
		container.getToolBar().checkButtonState();
		container.refresh();
	}

}
