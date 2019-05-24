package cnuphys.splot.plot;

import java.awt.Point;

public interface Draggable {

	public boolean contains(Point p);

	public boolean isDraggingPrimed();

	public boolean isDragging();

	public void setDraggingPrimed(boolean primed);

	public void setDragging(boolean dragging);

	public void setCurrentPoint(Point p);

	public Point getCurrentPoint();
}
