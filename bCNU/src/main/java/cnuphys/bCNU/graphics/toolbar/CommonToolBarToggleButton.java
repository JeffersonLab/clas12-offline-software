package cnuphys.bCNU.graphics.toolbar;

import java.awt.Cursor;
import java.awt.event.MouseEvent;

import javax.swing.JToggleButton;

@SuppressWarnings("serial")
public class CommonToolBarToggleButton extends JToggleButton {

	/**
	 * The active toggle button has changed
	 */
	protected void activeToggleButtonChanged() {
	}

	/**
	 * Handle a single click
	 * 
	 * @param mouseEvent
	 *            the causal event
	 */
	public void handleSingleClick(MouseEvent mouseEvent) {
	}

	/**
	 * Handle a mouse dragged (on the map canvas) event (if this tool is active)
	 * 
	 * @param mouseEvent
	 *            the causal event
	 */
	public void mouseDragged(MouseEvent mouseEvent) {
	}

	/**
	 * Handle a mouse move event.
	 * 
	 * @param mouseEvent
	 *            the causal event
	 */
	public void mouseMoved(MouseEvent mouseEvent) {
	}

	/**
	 * Handle a mouse clicked event.
	 * 
	 * @param mouseEvent
	 *            the causal event
	 */
	public void mouseClicked(MouseEvent mouseEvent) {
	}

	/**
	 * Handle a mouse double event.
	 * 
	 * @param mouseEvent
	 *            the causal event
	 */
	public void mouseDoubleClicked(MouseEvent mouseEvent) {
	}

	/**
	 * Handle a mouse button 3 event.
	 * 
	 * @param mouseEvent
	 *            the causal event
	 */
	public void mouseButton3Click(MouseEvent mouseEvent) {
	}
	
	/**
	 * Handle popup trigger
	 * @param mouseEvent the causal event
	 */
	public void popupTrigger(MouseEvent mouseEvent) {
	}

	/**
	 * Handle a mouse press.
	 * 
	 * @param mouseEvent
	 *            the causal event
	 */
	public void mousePressed(MouseEvent mouseEvent) {
	}

	/**
	 * Handle a mouse exited event (if this tool is active)
	 * 
	 * @param mouseEvent
	 *            the causal event
	 */
	public void mouseExited(MouseEvent mouseEvent) {
	}

	/**
	 * Handle a mouse entered event (if this tool is active)
	 * 
	 * @param mouseEvent
	 *            the causal event
	 */
	public void mouseEntered(MouseEvent mouseEvent) {
	}

	/**
	 * Handle a mouse released event (if this tool is active)
	 * 
	 * @param mouseEvent
	 *            the causal event
	 */
	public void mouseReleased(MouseEvent mouseEvent) {
	}

	/**
	 * Get the appropriate cursor for this tool.
	 * 
	 * @return the cursor appropriate when the mouse is in the map canvas.
	 */

	public Cursor canvasCursor() {
		return Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
	}

}
