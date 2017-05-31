package cnuphys.bCNU.graphics.toolbar.lasso;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;

import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.graphics.rubberband.IRubberbanded;
import cnuphys.bCNU.graphics.rubberband.Rubberband;
import cnuphys.bCNU.graphics.toolbar.ToolBarToggleButton;
import cnuphys.bCNU.util.Environment;

/**
 * The is the default "pointer" button.
 * 
 * @author heddle
 */
@SuppressWarnings("serial")
public class LassoRectButton extends ToolBarToggleButton implements
		IRubberbanded {

	/**
	 * Current mouse position.
	 */
	private Point _currentPoint = new Point();

	// listener for lassos
	private ILassoListener _lassoListener;

	// xor rubberbanding works better for 3D canvases
	private boolean _xorMode;

	/**
	 * Creates the default pointer button used for selecting objects,
	 * 
	 * @param container
	 *            the owner container.
	 */
	public LassoRectButton(final IContainer container,
			ILassoListener lassoListener, boolean xormode) {
		super(container, "images/lassorect.gif", "Rubberband a query rectangle");

		_lassoListener = lassoListener;
		_xorMode = xormode;

		// use a custom cursor
		xhot = 3;
		yhot = 1;
		customCursorImageFile = "images/pointercursor.gif";
	}

	/**
	 * The mouse was clicked. Note that the order the events will come is
	 * PRESSED, RELEASED, CLICKED. And a CLICKED will happen only if the mouse
	 * was not moved between press and release.
	 * 
	 * @param mouseEvent
	 *            the causal event.
	 */
	@Override
	public void mousePressed(MouseEvent mouseEvent) {

		if (rubberband == null) {

			if (_lassoListener != null) {
				_lassoListener.lassoStarting();
			}

			Environment.getInstance().setDragging(true);
			rubberband = new Rubberband(container, this,
					Rubberband.Policy.RECTANGLE, _xorMode);
			rubberband.setFillColor(new Color(0, 255, 128, 96));
			rubberband.setHighlightColor1(Color.cyan);
			rubberband.setHighlightColor2(Color.blue);

			rubberband.setActive(true);
			rubberband.startRubberbanding(mouseEvent.getPoint());
		}
	}

	/**
	 * Mouse has been dragged with pointer button active.
	 * 
	 * @param mouseEvent
	 *            the causal event.
	 */
	@Override
	public void mouseDragged(MouseEvent mouseEvent) {
		_currentPoint.setLocation(mouseEvent.getPoint());
	}

	/**
	 * The mouse was clicked. Note that the order the events will come is
	 * PRESSED, RELEASED, CLICKED. And a CLICKED will happen only if the mouse
	 * was not moved between press and release.
	 * 
	 * @param mouseEvent
	 *            the causal event.
	 */
	@Override
	public void mouseReleased(MouseEvent mouseEvent) {
	}

	/**
	 * Handle a mouse double event.
	 * 
	 * @param mouseEvent
	 *            the causal event
	 */
	@Override
	public void mouseDoubleClicked(MouseEvent mouseEvent) {
	}

	/**
	 * Handle a mouse button 3 event.
	 * 
	 * @param mouseEvent
	 *            the causal event
	 */
	@Override
	public void mouseButton3Click(MouseEvent mouseEvent) {
	}

	/**
	 * The rubberbanding is complete.
	 */
	@Override
	public void doneRubberbanding() {

		System.err.println("Done rubberbanding with lasso");

		Rectangle b = rubberband.getRubberbandBounds();
		rubberband = null;

		container.getToolBar().resetDefaultSelection();
		container.refresh();
		Environment.getInstance().setDragging(false);

		if (_lassoListener != null) {
			_lassoListener.lassoEnding();
			Rectangle2D.Double wr = new Rectangle2D.Double();
			container.localToWorld(b, wr);
			_lassoListener.rectangleLasso(wr, false);
		}
	}
}
