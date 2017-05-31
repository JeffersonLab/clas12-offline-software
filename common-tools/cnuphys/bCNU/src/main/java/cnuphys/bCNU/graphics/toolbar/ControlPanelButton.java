/**
 * 
 */
package cnuphys.bCNU.graphics.toolbar;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import cnuphys.bCNU.graphics.container.IContainer;

/**
 * @author heddle
 * 
 *         TODO To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Style - Code Templates
 */
@SuppressWarnings("serial")
public class ControlPanelButton extends ToolBarToggleButton implements
		ActionListener {

	/**
	 * Create the button used to delete items on the container.
	 * 
	 * @param container
	 *            the owner container.
	 */
	public ControlPanelButton(IContainer container) {
		super(container, "images/cp.gif", "Toggle Control Panel Visibility");
		addActionListener(this);
	}

	/**
	 * This is what I do if I am pressed. I simply tell the view that the
	 * control panel button was selected
	 * 
	 * @param e
	 *            The causal event.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		container.getView().controlPanelButtonHit();
	}

}
