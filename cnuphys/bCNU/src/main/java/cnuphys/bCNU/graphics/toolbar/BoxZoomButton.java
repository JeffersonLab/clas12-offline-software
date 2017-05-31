package cnuphys.bCNU.graphics.toolbar;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.graphics.rubberband.IRubberbanded;
import cnuphys.bCNU.graphics.rubberband.Rubberband;
import cnuphys.bCNU.util.Environment;

/**
 * Used to rubber band a zoom.
 * 
 * @author heddle
 * 
 */
@SuppressWarnings("serial")
public class BoxZoomButton extends ToolBarToggleButton implements IRubberbanded {

	// rubberbanding policy
	private Rubberband.Policy _policy = Rubberband.Policy.RECTANGLE_PRESERVE_ASPECT;
	
	private boolean _xorMode = false;
	

	/**
	 * Create the button for a rubber-band zoom.
	 * 
	 * @param container
	 *            the owner container.
	 */
	public BoxZoomButton(IContainer container) {
		super(container, "images/box_zoom.gif", "Rubberband zoom");
		customCursorImageFile = "images/box_zoomcursor.gif";
	}

	/**
	 * Handle a mouse press (into the container) event (if this tool is active).
	 * 
	 * @param mouseEvent
	 *            the causal event.
	 */
	@Override
	public void mousePressed(MouseEvent mouseEvent) {
		if (rubberband == null) {
			Environment.getInstance().setDragging(true);
			rubberband = new Rubberband(container, this, _policy, _xorMode);
			// make it look different from the pointer rubberband
			rubberband.setHighlightColor1(Color.gray);
			rubberband.setHighlightColor2(Color.green);
			rubberband.setActive(true);
			rubberband.startRubberbanding(mouseEvent.getPoint());
		}
	}

	/**
	 * Set the xor mode
	 * @param mode the xor mode
	 */
	public void setXorMode(boolean mode) {
	    _xorMode = mode;
	    rubberband = null;
	}

	/**
	 * Notification that rubber banding is finished.
	 */
	@Override
	public void doneRubberbanding() {
		Rectangle b = rubberband.getRubberbandBounds();
		rubberband = null;
		container.rubberBanded(b);
		Environment.getInstance().setDragging(false);
	}

	/**
	 * @param policy
	 *            the policy to set
	 */
	public void setPolicy(Rubberband.Policy policy) {
		_policy = policy;
	}

}
