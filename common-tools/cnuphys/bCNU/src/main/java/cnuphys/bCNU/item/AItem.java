package cnuphys.bCNU.item;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPopupMenu;

import cnuphys.bCNU.drawable.DrawableChangeType;
import cnuphys.bCNU.drawable.IDrawable;
import cnuphys.bCNU.feedback.IFeedbackProvider;
import cnuphys.bCNU.format.DoubleFormat;
import cnuphys.bCNU.graphics.GraphicsUtilities;
import cnuphys.bCNU.graphics.ImageManager;
import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.graphics.style.IStyled;
import cnuphys.bCNU.graphics.style.Styled;
import cnuphys.bCNU.item.ItemModification.ModificationType;
import cnuphys.bCNU.layer.LogicalLayer;
import cnuphys.bCNU.util.UnicodeSupport;
import cnuphys.bCNU.util.X11Colors;
import cnuphys.bCNU.view.BaseView;

/**
 * This is the base class for custom items that are rendered on an AContainer.
 * 
 * @author heddle
 * 
 */
public abstract class AItem implements IDrawable, IFeedbackProvider {

	// usef for drawing a the focus point
	protected static final Color _FOCUSFILL = new Color(128, 128, 128, 128);

	// used for selecting
	protected static final int SPSIZE = 10;

	// used for selecting
	protected static final int SPSIZE2 = SPSIZE / 2;

	// used for rotating
	protected static final int RPSIZE = 14;

	// used for rotating
	protected static final int RPSIZE2 = RPSIZE / 2;

	// icon for rotation
	protected static ImageIcon rotateIcon = ImageManager.getInstance()
			.loadImageIcon("images/rotate.png");

	/**
	 * The path is used by some items (not point or line based items). NOTE: it
	 * is world coordinate based.
	 */
	protected Path2D.Double _path;

	/**
	 * The line is used by line based items
	 */
	protected Line2D.Double _line;

	/**
	 * Optionaly secondary points (such as internal points)
	 */
	protected Point2D.Double _secondaryPoints[];

	/**
	 * The focus of the item--often it is the center.
	 */
	protected Point2D.Double _focus;

	/**
	 * What layer the item is on.
	 */
	protected LogicalLayer _layer;

	/**
	 * The parent of this item.
	 */
	protected AItem _parent;

	/**
	 * The children of this item. Children on only used for grouping items that
	 * are possibly deleted together and possibly dragged together. Children are
	 * selected, rotated and resized separately. When an item is deleted, all
	 * descendants are deleted too, but not ancestors. Just like deleted a
	 * folder.
	 */
	protected Vector<AItem> _children;

	/**
	 * The style for this item.
	 */
	protected IStyled _style = new Styled();

	/**
	 * Resize policy (assuming this is resizable)
	 */
	protected ResizePolicy _resizePolicy = ResizePolicy.NORMAL;

	/**
	 * Visibility flag for this item.
	 */
	protected boolean _visible = true;

	/**
	 * Controls whether the item can be dragged.
	 */
	protected boolean _draggable = false;

	/**
	 * Controls whether the item can be rotated.
	 */
	protected boolean _rotatable = false;

	/**
	 * Controls whether the item responds to a righjt click.
	 */
	protected boolean _rightClickable = false;

	/**
	 * Controls whether the item is locked-which takes precedence over other
	 * flags. A locked item cannot be dragged, rotated, resized, or
	 * deleted--regardless of the values of those flags.
	 */
	protected boolean _locked = true;

	/**
	 * Controls whether the item can be resized.
	 */
	private boolean _resizable = false;

	/**
	 * Controls whether the item can be deleted.
	 */
	protected boolean _deletable = false;

	/**
	 * Flag indicating whether the item is selected.
	 */
	protected boolean _selected = false;

	/**
	 * Flag indicating whether the item is enabled. Objects that are not enabled
	 * are inert and might be drawn "ghosted."
	 */
	protected boolean _enabled = true;

	/**
	 * Flag indicating whether the item is dirty. If the item is dirty, the next
	 * time it is drawn it must be drawn from scratch. Many items are drawn from
	 * scratch anyway--but some complicated items may be caching data for quick
	 * redraw.
	 */
	protected boolean _dirty = true;

	// polygon from last draw. Some items will not use this
	protected Polygon _lastDrawnPolygon;

	/**
	 * Used to modify items (drag, resize, rotate)
	 */
	protected ItemModification _modification;

	// reference rotation angle in degrees.
	private double _azimuth = 0.0;

	/**
	 * The name of the item.
	 */
	protected String _name = "no name";

	// used for select points
	private static final Color _selectFill = Color.white;

	// used for select points
	private static final Color _rotateFill = X11Colors
			.getX11Color("yellow", 64);

	// used for select points
	private static final Color _selectLine = Color.black;

	/**
	 * Create an item on a specific layer.
	 * 
	 * @param layer
	 *            the layer it is on.
	 */
	public AItem(LogicalLayer layer) {
		_layer = layer;
		_layer.add(this);

		_layer.getContainer().getFeedbackControl().addFeedbackProvider(this);

	}

	/**
	 * Draw the item.
	 * 
	 * @param g
	 *            the graphics context.
	 * @param container
	 *            the graphical container being rendered.
	 */
	@Override
	public void draw(Graphics g, IContainer container) {

		if (_visible) {
						
			if (shouldDraw(g, container)) {

				// special clip?
				Shape oldClip = g.getClip();
				BaseView bview = container.getView();

				if (bview != null) {
					Shape clip = bview.getSpecialClip();
					if (clip != null) {
						g.setClip(clip);
					}
				}

				Stroke oldStroke = ((Graphics2D) g).getStroke();
				drawItem(g, container);
				setDirty(false);
				((Graphics2D) g).setStroke(oldStroke);

				g.setClip(oldClip);
			}

			drawSelections(g, container);
		}
	}

	/**
	 * Draws any selection or rotation rectangles
	 * 
	 * @param g
	 *            the graphics context.
	 * @param container
	 *            the graphical container being rendered.
	 */
	public void drawSelections(Graphics g, IContainer container) {

		if (!isSelected()) {
			return;
		}

		Point selectPoints[] = getSelectionPoints(container);
		if (selectPoints != null) {
			for (Point p : selectPoints) {
				g.setColor(_selectFill);
				g.fillOval(p.x - SPSIZE2, p.y - SPSIZE2, SPSIZE, SPSIZE);
				g.setColor(_selectLine);
				g.drawOval(p.x - SPSIZE2, p.y - SPSIZE2, SPSIZE, SPSIZE);
			}
		}

		if (isRotatable()) {
			Point rp = getRotatePoint(container);

			if ((rp != null) && (rotateIcon != null)) {
				g.setColor(_rotateFill);
				g.fillOval(rp.x - RPSIZE2, rp.y - RPSIZE2, RPSIZE, RPSIZE);

				g.drawImage(rotateIcon.getImage(),
						rp.x - rotateIcon.getIconHeight() / 2 + 1, rp.y
								- rotateIcon.getIconHeight() / 2 + 1,
						container.getComponent());
			}
		}

		// draw the focus
		focusFill(g, container);
	}

	// draw the focus for selected items
	protected void focusFill(Graphics g, IContainer container) {
		Point pp = getFocusPoint(container);
		if (pp != null) {
			g.setColor(_FOCUSFILL);
			g.fillRect(pp.x - 3, pp.y - 3, 6, 6);
		}
	}

	/**
	 * Check whether this item is marked as visible.
	 * 
	 * @return <code>true</code> is this item is marked as visible.
	 */
	@Override
	public boolean isVisible() {
		return _visible;
	}

	/**
	 * Sets the visibility flag.
	 * 
	 * @param visible
	 *            the new value of the flag
	 */
	@Override
	public void setVisible(boolean visible) {
		_visible = visible;
	}

	/**
	 * Check whether the item can be dragged.
	 * 
	 * @return <code>true</code> if the item can be dragged.
	 */
	public boolean isDraggable() {
		if (_locked || !isLayerEnabled()) {
			return false;
		}

		return _draggable;
	}

	/**
	 * Set whether the item can be dragged.
	 * 
	 * @param draggable
	 *            if <code>true</code>, the item can be dragged.
	 */
	public void setDraggable(boolean draggable) {
		_draggable = draggable;
	}

	/**
	 * Check whether the item can be deleted.
	 * 
	 * @return <code>true</code> if the item can be deleted.
	 */
	public boolean isDeletable() {
		if (_locked) {
			return false;
		}
		return _deletable;
	}

	/**
	 * Set whether the item can be deleted.
	 * 
	 * @param deletable
	 *            if <code>true</code>, the item can be deleted.
	 */
	public void setDeletable(boolean deletable) {
		_deletable = deletable;
	}

	/**
	 * Check whether the item can be resized.
	 * 
	 * @return <code>true</code> if the item can be resized.
	 */
	public boolean isResizable() {
		if (_locked || !isLayerEnabled()) {
			return false;
		}
		return _resizable;
	}

	/**
	 * Set whether the item can be resized.
	 * 
	 * @param resizable
	 *            if <code>true</code>, the item can be resized.
	 */
	public void setResizable(boolean resizable) {
		_resizable = resizable;
	}

	/**
	 * Check whether the item can be rotated.
	 * 
	 * @return <code>true</code> if the item can be rotated.
	 */
	public boolean isRotatable() {
		if (_locked || !isLayerEnabled()) {
			return false;
		}
		return _rotatable;
	}

	/**
	 * /** Set whether the item can be rotated.
	 * 
	 * @param rotatable
	 *            if <code>true</code>, the item can be rotated.
	 */
	public void setRotatable(boolean rotatable) {
		_rotatable = rotatable;
	}

	/**
	 * Check whether the item is locked, which takes precedence over other
	 * flags. A locked item cannot be dragged, rotated, resized, or
	 * deleted--regardless of the values of those flags.
	 * 
	 * @return <code>true</code> if the item is locked.
	 */
	public boolean isLocked() {
		return _locked;
	}

	/**
	 * Set whether the item is locked.
	 * 
	 * @param locked
	 *            if <code>true</code>, the item is set to locked.
	 */
	public void setLocked(boolean locked) {
		_locked = locked;
	}

	/**
	 * Check whether this item is marked as selected.
	 * 
	 * @return <code>true</code> is this item is marked as selected.
	 */
	public boolean isSelected() {
		return _selected;
	}

	/**
	 * Sets whether this item is marked as selected.
	 * 
	 * @param selected
	 *            the new value of the flag.
	 */
	public void setSelected(boolean selected) {
		_selected = selected;
		_layer.getContainer().setDirty(true);
	}

	/**
	 * Check whether this item is marked as enabled. If the item is enabled, it
	 * can be selected, otherwise it is inert.
	 * 
	 * @return <code>true</code> is this item is marked as enabled.
	 */
	@Override
	public boolean isEnabled() {
		return _enabled;
	}

	/**
	 * Sets whether this item is marked as enabled.
	 * 
	 * @param enabled
	 *            the new value of the flag. If the item is enabled, it can be
	 *            selected, otherwise it is inert.
	 */
	@Override
	public void setEnabled(boolean enabled) {
		_enabled = enabled;
	}

	/**
	 * Check whether this item is marked as dirty. If the item is dirty, the
	 * next time it is drawn it must be drawn from scratch. Many items are drawn
	 * from scratch anyway--but some complicated items may be caching data for
	 * quick redraw.
	 * 
	 * @return <code>true</code> is this item is marked as dirty.
	 */
	public boolean isDirty() {
		return _dirty;
	}

	/**
	 * Sets whether this item is marked as dirty.
	 * 
	 * @param dirty
	 *            the new value of the flag. If the item is dirty, the next time
	 *            it is drawn it must be drawn from scratch. Many items are
	 *            drawn from scratch anyway--but some complicated items may be
	 *            caching data for quick redraw.
	 */
	@Override
	public void setDirty(boolean dirty) {
		_dirty = dirty;
		if (dirty) {
			_lastDrawnPolygon = null;
		}
	}

	/**
	 * Convenience routine to see if this item should be ignored.
	 * 
	 * @return <code>true</code> if the item should be ignored in terms of
	 *         dragging, ro
	 */
	public boolean isTrackable() {
		if (_locked) {
			return false;
		}

		if (!_enabled) {
			return false;
		}

		if (!isLayerEnabled()) {
			return false;
		}

		return _rotatable || _draggable || _resizable;
	}

	/**
	 * Custom drawer for the item.
	 * 
	 * @param g
	 *            the graphics context.
	 * @param container
	 *            the graphical container being rendered.
	 */
	public abstract void drawItem(Graphics g, IContainer container);

	/**
	 * Checks whether the item should be drawn. This is an additional check,
	 * beyond the simple visibility flag check. For example, it might check
	 * whether the item intersects the area being drawn.
	 * 
	 * @param g
	 *            the graphics context.
	 * @param container
	 *            the graphical container being rendered.
	 * @return <code>true</code> if the item passes any and all tests, and
	 *         should be drwan.
	 */
	public abstract boolean shouldDraw(Graphics g, IContainer container);

	/**
	 * Check whether the (rendered) item contains the given screen point.
	 * 
	 * @param container
	 *            the graphical container rendering the item.
	 * @param screenPoint
	 *            a pixel location.
	 * @return <code>true</code> if the item, as rendered on the given
	 *         container, contains the given screen point.
	 */
	public boolean contains(IContainer container, Point screenPoint) {
		// do we have a cached polygon?

		if (_lastDrawnPolygon != null) {
			if (_lastDrawnPolygon.contains(screenPoint)) {
				return true;
			}
		} else { // try simple bounds
			Rectangle r = getBounds(container);
			if ((r != null) && r.contains(screenPoint)) {
				return true;
			}
		}
		return inASelectRect(container, screenPoint);
	}

	// Are we in any select rect?
	protected boolean inASelectRect(IContainer container, Point screenPoint) {

		// still have to consider rotate and select points
		if (inSelectPoint(container, screenPoint, false) >= 0) {
			return true;
		}

		// last hope
		if (inRotatePoint(container, screenPoint)) {
			return true;
		}

		return false;
	}

	/**
	 * Get the drawing style for this item. Through this object you can set the
	 * fill color, line style, etc.
	 * 
	 * @return the style for this item.
	 */
	public IStyled getStyle() {
		return _style;
	}

	/**
	 * Set the drawing style for this item.
	 * 
	 * @param style
	 *            the style to set.
	 */
	public void setStyle(IStyled style) {
		this._style = style;
	}

	/**
	 * Set the name of the item.
	 * 
	 * @param name
	 *            the name of the item.
	 */
	public void setName(String name) {
		_name = name;
	}

	/**
	 * Return the name of the item.
	 * 
	 * @return the name of the item.
	 */
	@Override
	public String getName() {
		return _name;
	}

	/**
	 * Equality check.
	 * 
	 * @return <code>true</code> if objects are equal.
	 */
	@Override
	public boolean equals(Object o) {

		if ((o != null) && (o instanceof AItem)) {
			return (this == o);
		}
		return false;
	}

	/**
	 * Add any appropriate feedback strings
	 * panel. Default implementation returns the item's name.
	 * 
	 * @param container
	 *            the Base container.
	 * @param pp
	 *            the mouse location.
	 * @param wp
	 *            the corresponding world point.
	 * @param feedbackStrings
	 *            the List of feedback strings to add to.
	 */
	@Override
	public void getFeedbackStrings(IContainer container, Point pp,
			Point2D.Double wp, List<String> feedbackStrings) {
	}

	/**
	 * Check if the given rectangle completely encloses the item. This is used
	 * for rubber band selection. The default implementation should work for
	 * rectangular objects. More complicated objects (polygons) should
	 * overwrite.
	 * 
	 * @param container
	 *            the rendering container.
	 * @param r
	 *            the bounds
	 * @return <code>true</code> if the bounds (r) completely enclose the item.
	 */
	public boolean enclosed(IContainer container, Rectangle r) {

		if (_lastDrawnPolygon != null) {
			return r.contains(_lastDrawnPolygon.getBounds());
		}

		Rectangle myBounds = getBounds(container);
		if (myBounds == null) {
			return false;
		}
		return r.contains(myBounds);
	}

	/**
	 * get the bounding rectangle of the item.
	 * 
	 * @param container
	 *            the container being rendered
	 * @return the box around the item.
	 */
	public Rectangle getBounds(IContainer container) {
		return null;
	}

	/**
	 * Get the world bounding rectangle of the item.
	 * 
	 * @return the world box containing the item.
	 */
	public abstract Rectangle2D.Double getWorldBounds();

	/**
	 * A modification such as a drag, resize or rotate has begun.
	 */
	public void startModification() {

		IContainer container = _modification.getContainer();
		Point smp = _modification.getStartMousePoint();

		// check rotate first
		if (inRotatePoint(container, smp)) {
			_modification.setType(ModificationType.ROTATE);
			return;
		}

		// is this a resize?
		int index = inSelectPoint(container, smp, true);
		if (index >= 0) {
			_modification.setSelectIndex(index);
			_modification.setType(ModificationType.RESIZE);
			return;
		}

		_modification.setType(ModificationType.DRAG);
	}

	/**
	 * A modification such as a drag, resize or rotate is continuing.
	 */
	public abstract void modify();

	/**
	 * A modification such as a drag, resize or rotate has ended.
	 */
	public void stopModification() {
		switch (_modification.getType()) {
		case DRAG:
			_layer.notifyDrawableChangeListeners(this, DrawableChangeType.MOVED);
			break;

		case ROTATE:
			_layer.notifyDrawableChangeListeners(this,
					DrawableChangeType.ROTATED);
			break;

		case RESIZE:
			_layer.notifyDrawableChangeListeners(this,
					DrawableChangeType.RESIZED);
			break;
		}
		_modification = null;
	}

	/**
	 * This gets the focus of the item. Fot pointlike items it will be the
	 * location. For polygonal items it might be the centroid.
	 * 
	 * @return the focus of the item.
	 */
	public Point2D.Double getFocus() {
		return _focus;
	}

	/**
	 * This should be overridden by items to do something sensible. They should
	 * set their focus point which for simple items may their location and for
	 * complicated items may be where their new centroid should be.
	 * 
	 * @param wp
	 *            the new focus.
	 */
	public void setFocus(Point2D.Double wp) {
		_focus = wp;
	}

	/**
	 * This gets the screen (pixel) version focus of the item. Fot pointlike
	 * items it will be the location. For polygobal items it might be the
	 * centroid.
	 * 
	 * @return the focus of the item.
	 */
	public Point getFocusPoint(IContainer container) {
		Point2D.Double wp = getFocus();
		if (wp == null) {
			return null;
		}
		Point pp = new Point();
		container.worldToLocal(pp, wp);
		return pp;
	}

	/**
	 * Get the rotation point
	 * 
	 * @param container
	 *            the container bing rendered
	 * @return the rotation point where rotations are initiated
	 */
	public Point getRotatePoint(IContainer container) {
		return null;
	}

	/**
	 * Obtain the selection points used to indicate this item is selected.
	 * 
	 * @return the selection points used to indicate this item is selected.
	 */
	public Point[] getSelectionPoints(IContainer container) {
		// if the item cached a last drawn polygon lets use it--it better be
		// right!

		if ((_lastDrawnPolygon != null) && (_lastDrawnPolygon.npoints > 1)) {
			Point pp[] = new Point[_lastDrawnPolygon.npoints];
			;
			for (int i = 0; i < _lastDrawnPolygon.npoints; i++) {
				pp[i] = new Point(_lastDrawnPolygon.xpoints[i],
						_lastDrawnPolygon.ypoints[i]);
			}
			return pp;
		}

		// else just use the bounds
		Rectangle r = getBounds(container);
		if (r == null) {
			return null;
		} else {
			Point p[] = new Point[4];
			int bottom = r.y + r.height;
			int right = r.x + r.width;
			p[0] = new Point(r.x, r.y);
			p[1] = new Point(r.x, bottom);
			p[2] = new Point(right, bottom);
			p[3] = new Point(right, r.y);
			return p;
		}
	}

	/**
	 * Get the layer this item is on.
	 * 
	 * @return the layer this item is on.
	 */
	public LogicalLayer getLayer() {
		return _layer;
	}

	/**
	 * Get the modification record which will not be null while the item is
	 * being modified.
	 * 
	 * @return the itemModification record.
	 */
	public ItemModification getItemModification() {
		return _modification;
	}

	/**
	 * Check whether the item will respond to a right click.
	 * 
	 * @return the rightClickable flag.
	 */
	public boolean isRightClickable() {
		// turn off the layer check--allow even disabled layers
		// to process right clicks
		return _rightClickable;
		// return _rightClickable && isLayerEnabled();
	}

	/**
	 * Set whether the item will respond to a right click.
	 * 
	 * @param rightClickable
	 *            the new rightClickable flag to set
	 */
	public void setRightClickable(boolean rightClickable) {
		_rightClickable = rightClickable;
	}

	/**
	 * Called when the item was double clicked. The default implementation is to
	 * do nothing.
	 * 
	 * @param mouseEvent
	 *            the causal event.
	 */
	public void doubleClicked(MouseEvent mouseEvent) {
	}
	

	/**
	 * Is the given point int a select rect?
	 * 
	 * @param container
	 *            the container being rendered
	 * @param screenPoint
	 *            the point in question
	 * @return the index of the select point, or -1 if not in one
	 */
	public int inSelectPoint(IContainer container, Point screenPoint,
			boolean checkResizable) {
		if (checkResizable && !isResizable()) {
			return -1;
		}
		if (!isSelected()) {
			return -1;
		}

		Point pp[] = getSelectionPoints(container);
		if (pp == null) {
			return -1;
		}

		int index = 0;
		for (Point lp : pp) {
			Rectangle r = new Rectangle(lp.x - SPSIZE2, lp.y - SPSIZE2, SPSIZE,
					SPSIZE);
			if (r.contains(screenPoint)) {
				return index;
			}
			index++;
		}

		return -1;
	}

	/**
	 * See if we are in the rotate point
	 * 
	 * @param container
	 *            the container being rendered
	 * @param screenPoint
	 *            the point in question
	 * @return <code>true<code> if we are in the rotate rect.
	 */
	public boolean inRotatePoint(IContainer container, Point screenPoint) {
		if (!isRotatable()) {
			return false;
		}
		if (!isSelected()) {
			return false;
		}
		Point p = getRotatePoint(container);

		if (p == null) {
			return false;
		}
		Rectangle r = new Rectangle(p.x - RPSIZE2, p.y - RPSIZE2, RPSIZE,
				RPSIZE);

		return r.contains(screenPoint);
	}

	/**
	 * Called only when a modification starts.
	 * 
	 * @param itemModification
	 *            the itemModification to set
	 */
	public void setModificationItem(ItemModification itemModification) {
		_modification = itemModification;
	}

	/**
	 * Get this item's popup menu
	 * 
	 * @param container
	 *            the containe the item lives on
	 * @param pp
	 *            the location of the click
	 * @return the item's popup menu
	 */
	public JPopupMenu getPopupMenu(final IContainer container, Point pp) {
		JPopupMenu menu = new JPopupMenu();
		menu.add(ItemOrderingMenu.getItemOrderingMenu(this, true));

		final JCheckBoxMenuItem cbitem = new JCheckBoxMenuItem("Locked",
				isLocked());

		final AItem titem = this;
		ItemListener il = new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent arg0) {
				titem.setLocked(cbitem.isSelected());
				if (titem.isLocked()) {
					titem.setSelected(false);
					container.refresh();
				}
			}
		};
		cbitem.addItemListener(il);
		menu.add(cbitem);

		// properties
//		JMenuItem pitem = new JMenuItem("Properties...");
//		pitem.setEnabled(isLayerEnabled());
//
//		pitem.addActionListener(_editAction);
//		menu.add(pitem);

		return menu;
	}

	/**
	 * Get the reference rotation angle in degrees.
	 * 
	 * @return the azimuth
	 */
	public double getAzimuth() {
		return _azimuth;
	}

	/**
	 * Set the reference rotation angle in degrees.
	 * 
	 * @param azimuth
	 *            the azimuth to set in degrees
	 */
	public void setAzimuth(double azimuth) {
		_azimuth = azimuth;
		while (_azimuth > 180.0) {
			_azimuth -= 360.0;
		}
		while (_azimuth < -180.0) {
			_azimuth += 360.0;
		}
	}

	/**
	 * Get the parent of this item (often it is <code>null</code>.
	 * 
	 * @return the parent of this item (often it is <code>null</code>.
	 */
	public AItem getParent() {
		return _parent;
	}

	/**
	 * Set the parent of this item.
	 * 
	 * @param parent
	 *            the parent to set
	 */
	public void setParent(AItem parent) {
		this._parent = parent;
	}

	/**
	 * Get the children of this item, if any.
	 * 
	 * @return the children of this item.
	 */
	public Vector<AItem> getChildren() {
		return _children;
	}

	/**
	 * Remove an item as a child of this item.
	 * 
	 * @param child
	 *            the child item to remove, leaving it an orphan (but not
	 *            deleting it.) The child will still have a reference in some
	 *            logical layer.
	 */
	public void removeChild(AItem child) {
		if (child != null) {
			if (_children != null) {
				_children.remove(child);
				child.setParent(null);
			}
		}
	}

	/**
	 * Add an item as a child of this item.
	 * 
	 * @param child
	 *            the child item to add.
	 */
	public void addChild(AItem child) {
		if (child != null) {
			if (_children == null) {
				_children = new Vector<AItem>(10);
			}
			child.setParent(this);
			_children.add(child);
		}
	}

	/**
	 * Removes and deletes all children. They will also be removed from their
	 * host layer, so they are gone as far as this application is concerned.
	 */
	@SuppressWarnings("unchecked")
	public void deleteAllChildren() {
		if (_children == null) {
			return;
		}

		// use clone to avoid concurrency exception
		Vector<AItem> clone = (Vector<AItem>) _children.clone();

		for (IDrawable drawable : clone) {
			AItem item = (AItem) drawable;
			if (item != null) {
				item.getLayer().remove(item);
			}
		}

		_children = null;
	}

	/**
	 * Called when the drawable is about to be removed from a layer.
	 */
	@Override
	public void prepareForRemoval() {
		// tell my parent I am no longer its child
		if (_parent != null) {
			_parent.removeChild(this);

			_focus = null;
			_lastDrawnPolygon = null;
			_layer = null;
			_path = null;
			_secondaryPoints = null;
			_style = null;
		}

		// remove my children
		deleteAllChildren();
	}

	/**
	 * Add descendants form a given item to a vector
	 * 
	 * @param item
	 *            the item in question
	 * @param v
	 *            the vector, which should be instantiated.
	 */
	private static void addDescendants(AItem item, Vector<AItem> v) {
		if (item.getChildren() != null) {
			for (AItem child : item.getChildren()) {
				v.add(child);
				addDescendants(child, v);
			}
		}
	}

	/**
	 * Get all descendants of this item. The vector returned does not include
	 * this item itself--so if this item has no children, it returns
	 * <code>null</code>.
	 * 
	 * @return all descendants of all generations of this item.
	 */
	public Vector<AItem> getAllDescendants() {
		if (_children == null) {
			return null;
		}

		Vector<AItem> allDescendents = new Vector<AItem>(25, 5);
		AItem.addDescendants(this, allDescendents);
		return allDescendents;

	}

	/**
	 * @return the path
	 */
	public Path2D.Double getPath() {
		return _path;
	}

	/**
	 * @return the line
	 */
	public Line2D.Double getLine() {
		return _line;
	}

	/**
	 * Get the container this item lives on
	 * 
	 * @return the container this item lives on.
	 */
	public IContainer getContainer() {
		return getLayer().getContainer();
	}


	/**
	 * @return the secondary points
	 */
	public Point2D.Double[] getSecondaryPoints() {
		return _secondaryPoints;
	}

	/**
	 * @return the resizePolicy
	 */
	public ResizePolicy getResizePolicy() {
		return _resizePolicy;
	}

	/**
	 * @param resizePolicy
	 *            the resizePolicy to set
	 */
	public void setResizePolicy(ResizePolicy resizePolicy) {
		this._resizePolicy = resizePolicy;
	}

	/**
	 * Convenience check to see whether the item's layer is enabled. If it is
	 * not, you should not be able to select this item (independent of item's
	 * local selectability)
	 * 
	 * @return <code>true</code> if the item's layer is enabled.
	 */
	public boolean isLayerEnabled() {
		if (_layer == null) {
			return false;
		}
		return _layer.isEnabled();
	}

}
