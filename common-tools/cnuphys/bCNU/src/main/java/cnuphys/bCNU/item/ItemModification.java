package cnuphys.bCNU.item;

import java.awt.Point;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;

import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.graphics.world.WorldGraphicsUtilities;

public class ItemModification {

	public enum ModificationType {
		DRAG, RESIZE, ROTATE
	};

	// the type of modification. This must be set by the item.
	// Default is drag.
	private ModificationType type = ModificationType.DRAG;

	// the item being modified
	private AItem item;

	// the container that holds the item
	private IContainer container;

	// the location of the mouse when the modification began
	private Point startMousePoint;

	// the current location of the mouse
	private Point currentMousePoint;

	// the start world point of the mouse
	private Point2D.Double startWorldPoint;

	// the current world point of the mouse
	private Point2D.Double currentWorldPoint;

	// the start focus point of the item--i.e. its location or centroid.
	private Point2D.Double startFocus;

	// the pixel point corrsponding to the focus
	private Point startFocusPoint;

	// the index of the selectrect that was grabbed, if any
	private int selectIndex = -1;

	// used by some objects
	private Path2D.Double startPath;

	// used by some objects
	private Line2D.Double startLine;

	// used by some objects
	private Path2D.Double secondaryPath;

	// was shift pressed when mod began?
	private boolean shift;

	// was control pressed when mod began?
	private boolean control;

	// the item's azimuth before any modification
	private double startAzimuth;

	// used by pixel based items
	private Point startLocation;

	// for attaching user data
	private Object userObject;

	/**
	 * Create an ItemModification. The type is the default: DRAG. It can be
	 * changed via setType.
	 * 
	 * @param item
	 *            the item being modified.
	 * @param container
	 *            the item's container.
	 * @param startMousePoint
	 *            the mouse point where the item was grabbed.
	 * @param currentMousePoint
	 *            the current mouse point.
	 * @param shift
	 *            was shift pressed when modification began
	 * @param control
	 *            was control pressed when modification began
	 */
	public ItemModification(AItem item, IContainer container,
			Point startMousePoint, Point currentMousePoint, boolean shift,
			boolean control) {
		super();
		this.item = item;
		this.container = container;
		this.startMousePoint = startMousePoint;
		this.currentMousePoint = currentMousePoint;
		this.shift = shift;
		this.control = control;
		this.startPath = item.getPath();
		this.startLine = item.getLine();

		if (item.getSecondaryPoints() != null) {
			secondaryPath = WorldGraphicsUtilities.worldPolygonToPath(item
					.getSecondaryPoints());
		}

		startWorldPoint = new Point2D.Double();
		currentWorldPoint = new Point2D.Double();
		container.localToWorld(startMousePoint, startWorldPoint);
		container.localToWorld(currentMousePoint, currentWorldPoint);

		Point2D.Double focus = item.getFocus();
		if (focus != null) {
			setStartFocus(new Point2D.Double(focus.x, focus.y));
		}

		setStartFocusPoint(item.getFocusPoint(container));

		startAzimuth = item.getAzimuth();
	}

	/**
	 * Get the type of modification.
	 * 
	 * @return the type of modification
	 */
	public ModificationType getType() {
		return type;
	}

	/**
	 * Set the type of modification.
	 * 
	 * @param type
	 *            the type to set
	 */
	public void setType(ModificationType type) {
		this.type = type;
	}

	/**
	 * Get the current location of the mouse.
	 * 
	 * @return the currentMousePoint
	 */
	public Point getCurrentMousePoint() {
		return currentMousePoint;
	}

	/**
	 * Set the current mouse location. This will be done by the PointerButton.
	 * 
	 * @param currentMousePoint
	 *            the currentMousePoint to set
	 */
	public void setCurrentMousePoint(Point currentMousePoint) {
		this.currentMousePoint = currentMousePoint;
		container.localToWorld(currentMousePoint, currentWorldPoint);
	}

	/**
	 * The focus is the world reference point for the item. It might be its
	 * location or its centroid. It has to be set by the item when
	 * 
	 * @return the startFocus
	 */
	public Point2D.Double getStartFocus() {
		return startFocus;
	}

	/**
	 * The focus is the world reference point for the item. It might be its
	 * location or its centroid. It has to be set by the item when the
	 * modification starts.
	 * 
	 * @param startFocus
	 *            the startFocus to set
	 */
	public void setStartFocus(Point2D.Double startFocus) {
		this.startFocus = startFocus;
	}

	/**
	 * The focus is the screen (pixel) version of reference point for the item.
	 * It might be its location or its centroid. It has to be set by the item
	 * when the modification starts.
	 * 
	 * @return the startFocusPoint
	 */
	public Point getStartFocusPoint() {
		return startFocusPoint;
	}

	/**
	 * The focus is the screen (pixel) version reference point for the item. It
	 * might be its location or its centroid. It has to be set by the item when
	 * the modification starts.
	 * 
	 * @param startFocusPoint
	 *            the startFocusPoint to set
	 */
	public void setStartFocusPoint(Point startFocusPoint) {
		this.startFocusPoint = startFocusPoint;
	}

	/**
	 * The selectIndex can be used by the item to cache the index of the
	 * selectRect that started the modification. This is often needed for
	 * resizing and rotating.
	 * 
	 * @return the selectIndex
	 */
	public int getSelectIndex() {
		return selectIndex;
	}

	/**
	 * The selectIndex can be used by the item to cache the index of the
	 * selectRect that started the modification. This is often needed for
	 * resizing and rotating.
	 * 
	 * @param selectIndex
	 *            the selectIndex to set
	 */
	public void setSelectIndex(int selectIndex) {
		this.selectIndex = selectIndex;
	}

	/**
	 * Get the item being modified.
	 * 
	 * @return the item being modified.
	 */
	public AItem getItem() {
		return item;
	}

	/**
	 * Get the container that holds the item on one of its layers.
	 * 
	 * @return the container holding the item.
	 */
	public IContainer getContainer() {
		return container;
	}

	/**
	 * Get the point where the grab was initiated.
	 * 
	 * @return the startMousePoint the point where the grab was initiated.
	 */
	public Point getStartMousePoint() {
		return startMousePoint;
	}

	/**
	 * @return the startPath
	 */
	public Path2D.Double getStartPath() {
		return startPath;
	}

	/**
	 * @return the original secondary path
	 */
	public Path2D.Double getSecondaryPath() {
		return secondaryPath;
	}

	/**
	 * @return the startLine
	 */
	public Line2D.Double getStartLine() {
		return startLine;
	}

	/**
	 * @return the startWorldPoint
	 */
	public Point2D.Double getStartWorldPoint() {
		return startWorldPoint;
	}

	/**
	 * @return the currentWorldPoint
	 */
	public Point2D.Double getCurrentWorldPoint() {
		return currentWorldPoint;
	}

	/**
	 * @return the shift flag
	 */
	public boolean isShift() {
		return shift;
	}

	/**
	 * @return the control falg
	 */
	public boolean isControl() {
		return control;
	}

	/**
	 * @return the startAzimuth
	 */
	public double getStartAzimuth() {
		return startAzimuth;
	}

	/**
	 * @return the startLocation
	 */
	public Point getStartLocation() {
		return startLocation;
	}

	/**
	 * @param startLocation
	 *            the startLocation to set
	 */
	public void setStartLocation(Point startLocation) {
		this.startLocation = startLocation;
	}

	/**
	 * @return the userObject
	 */
	public Object getUserObject() {
		return userObject;
	}

	/**
	 * @param userObject
	 *            the userObject to set
	 */
	public void setUserObject(Object userObject) {
		this.userObject = userObject;
	}

}
