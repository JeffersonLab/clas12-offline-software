package cnuphys.splot.plot;

import java.awt.Point;
import java.awt.Rectangle;

public class DraggableRectangle extends Rectangle implements Draggable {

	// are we being dragged
	protected boolean _dragging;

	// is dragging primed
	protected boolean _draggingPrimed;

	// current point
	protected Point _currentPoint;

	protected boolean _beenMoved;

	@Override
	public boolean isDraggingPrimed() {
		return _draggingPrimed;
	}

	@Override
	public boolean isDragging() {
		return _dragging;
	}

	@Override
	public void setDraggingPrimed(boolean primed) {
		_draggingPrimed = primed;
	}

	@Override
	public void setDragging(boolean dragging) {
		_dragging = dragging;
		_beenMoved = true;
	}

	@Override
	public void setCurrentPoint(Point p) {
		if (p == null) {
			_currentPoint = null;
		}
		else {
			_currentPoint = new Point(p);
		}
	}

	@Override
	public Point getCurrentPoint() {
		return _currentPoint;
	}

}
