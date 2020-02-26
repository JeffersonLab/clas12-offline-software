package cnuphys.bCNU.graphics.container;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JFrame;

import cnuphys.bCNU.drawable.DrawableChangeType;
import cnuphys.bCNU.drawable.DrawableList;
import cnuphys.bCNU.drawable.IDrawable;
import cnuphys.bCNU.drawable.IDrawableListener;
import cnuphys.bCNU.feedback.FeedbackControl;
import cnuphys.bCNU.feedback.FeedbackPane;
import cnuphys.bCNU.graphics.GraphicsUtilities;
import cnuphys.bCNU.graphics.toolbar.BaseToolBar;
import cnuphys.bCNU.graphics.toolbar.ToolBarToggleButton;
import cnuphys.bCNU.graphics.world.WorldPolygon;
import cnuphys.bCNU.item.AItem;
import cnuphys.bCNU.item.EllipseItem;
import cnuphys.bCNU.item.LineItem;
import cnuphys.bCNU.item.PlotItem;
import cnuphys.bCNU.item.PolygonItem;
import cnuphys.bCNU.item.PolylineItem;
import cnuphys.bCNU.item.RadArcItem;
import cnuphys.bCNU.item.YouAreHereItem;
import cnuphys.bCNU.layer.LogicalLayer;
import cnuphys.bCNU.layer.LayerControl;
import cnuphys.bCNU.log.Log;
import cnuphys.bCNU.util.Point2DSupport;
import cnuphys.bCNU.view.BaseView;
import cnuphys.bCNU.visible.VisibilityTableScrollPane;

/**
 * This the primary component. It contains a list of layers (each containing a
 * list of items.)
 * 
 * @author heddle
 * 
 */

@SuppressWarnings("serial")
public class BaseContainer extends JComponent
		implements IContainer, MouseListener, MouseMotionListener,
		MouseWheelListener, IDrawableListener {

	/**
	 * A collection of layers. This is the container's model.
	 */
	protected DrawableList _layers = new DrawableList("Layers");

	/**
	 * Keeps track of current mouse position
	 */
	private Point _currentMousePoint;

	/**
	 * Each container may or may not have a tool bar.
	 */
	protected BaseToolBar _toolBar;


	/**
	 * The logical layer visibility control. It is created when requested.
	 */
	protected VisibilityTableScrollPane _visTable;

	/**
	 * The optional feedback pane.
	 */
	protected FeedbackPane _feedbackPane;

	// location of last mouse event
	protected MouseEvent _lastLocationMouseEvent;

	/**
	 * Used for offscreen drawing
	 */
	private BufferedImage _offscreenBuffer;

	/**
	 * Determines whether the off screen buffer is dirty
	 */
	private boolean _offscreenBufferDirty = true;

	/**
	 * This optional drawable is called after the layers are drawn.
	 */
	protected IDrawable _afterDraw;

	/**
	 * This optional drawable is called before the layers are drawn.
	 */
	protected IDrawable _beforeDraw;

	/**
	 * Option drawer for magnification window rather than just simple
	 * magnification
	 */
	protected IDrawable _magDraw;

	/**
	 * Used to print one page
	 */
	public int m_maxNumPage = 1;

	/**
	 * The view that holds this container (might be null for viewless
	 * container).
	 */
	protected BaseView _view;

	// used for things like a YouAreHereItem reference point
	private LogicalLayer _glassLayer;

	/**
	 * The world coordinate system,
	 */
	protected Rectangle2D.Double _worldSystem;

	/**
	 * Original, default world system.
	 */
	protected Rectangle2D.Double _defaultWorldSystem;

	/**
	 * Previous world system, for undoing the last zoom.
	 */
	protected Rectangle2D.Double _previousWorldSystem;

	/**
	 * The annotation layer. Every container has one.
	 */
	protected LogicalLayer _annotationLayer;

	// A map of layers added by users.
	private Hashtable<String, LogicalLayer> _userLayers = new Hashtable<String, LogicalLayer>(
			47);

	/**
	 * Controls the feedback for the container. You can add and remove feedback
	 * providers to this object.
	 */
	protected FeedbackControl _feedbackControl;

	/**
	 * Optional anchor item.
	 */
	protected YouAreHereItem _youAreHereItem;

	// for world to local transformations (and vice versa)

	private int _lMargin = 0;
	private int _tMargin = 0;
	private int _rMargin = 0;
	private int _bMargin = 0;
	protected AffineTransform localToWorld;
	protected AffineTransform worldToLocal;

	/**
	 * Constructor for a container that does not live in a view. It might live
	 * on a panel, for example
	 * 
	 * @param worldSystem the default world system.
	 */
	public BaseContainer(Rectangle2D.Double worldSystem) {
		this(null, worldSystem);
	}

	/**
	 * Constructor
	 * 
	 * @param view Every container lives on one view. This is the view, which is
	 *            an internal frame, that owns this container.
	 * @param worldSystem the default world system.
	 */
	public BaseContainer(BaseView view, Rectangle2D.Double worldSystem) {
		_view = view;
		_worldSystem = worldSystem;
		_feedbackControl = new FeedbackControl(this);

		_defaultWorldSystem = copy(worldSystem);
		_previousWorldSystem = copy(worldSystem);


		// create the annotation layer. (not added to userlayer hash)
		_annotationLayer = new LogicalLayer(this, "Annotations");
		addLogicalLayer(_annotationLayer);

		ComponentAdapter componentAdapter = new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent ce) {
				setDirty(true);
				repaint();
				setAffineTransforms();
			}
		};

		addComponentListener(componentAdapter);
		addMouseListener(this);
		addMouseMotionListener(this);

		// add a controller that listens for layer changes.
		_layers.addDrawableListener(new LayerControl(this));
	}

	/**
	 * Share the model of another view. Note, this is not a copy, either view
	 * can modify the layers and items.
	 * 
	 * @param sContainer the source container
	 */
	public void shareModel(BaseContainer sContainer) {
		_layers = sContainer._layers;
		_afterDraw = sContainer._afterDraw;
		_beforeDraw = sContainer._beforeDraw;
		_userLayers = sContainer._userLayers;
		setBackground(sContainer.getBackground());
		setForeground(sContainer.getForeground());
	}

	/**
	 * Just give the container a simple drawer
	 * 
	 * @param drawer the only drawer
	 */
	public void noModel(IDrawable drawer) {
		_layers = null;
		_afterDraw = drawer;
		_beforeDraw = null;
		_userLayers = null;
		setBackground(Color.gray);
		setForeground(Color.black);
	}
	
	/**
	 * Test whether the view is on screen
	 * @return <code>true</code> if the view is on screen
	 */
	public boolean isOnScreen() {
		if (_view == null) {
//			System.err.println("null view");
			return false;
		}
		
		JFrame jf = _view.getParentFrame();
		if (jf == null) {
//			System.err.println("null jframe for view " + _view.getTitle());
			return false;
		}
		
		Dimension d = jf.getSize();
		Rectangle pr = new Rectangle(0, 0, d.width, d.height);
		Rectangle r = _view.getBounds();
//		System.err.println("pr: " + pr + "  r: " + r);
		return ((pr != null) && (r != null) && pr.intersects(r));
	}

	/**
	 * Override the paint command. Draw all the layers.
	 * 
	 * @param g the graphics context.
	 */
	@Override
	public void paintComponent(Graphics g) {
		
		// if our offscreen buffer is not dirty, use it and we are done.
		if (!_offscreenBufferDirty) {
			g.drawImage(_offscreenBuffer, 0, 0, this);
			return;
		}

		setAffineTransforms();
		if (localToWorld == null) {
			return;
		}
		Dimension size = checkOffscreenBuffer();

		if ((size.width < 1) || (size.height < 1)) {
			return;
		}

		Graphics osg = _offscreenBuffer.getGraphics();

		// normal drawing
		osg.setColor(getBackground());
		osg.fillRect(0, 0, size.width, size.height);

		// any before layer drawing?
		if (_beforeDraw != null) {
			_beforeDraw.draw(osg, this);
		}

		// draw the layers
		if (_layers != null) {
			_layers.draw(osg, this);
		}

		// any post layer drawing?
		if (_afterDraw != null) {
			_afterDraw.draw(osg, this);
		}

		// copy onto real screen
		_offscreenBufferDirty = false;
		osg.dispose();
		g.drawImage(_offscreenBuffer, 0, 0, this);
	}

	/**
	 * Add a layer for containing items rendered on this container..
	 * 
	 * @param name the name of the layer. If one with that name already exists,
	 *            it is returned.
	 */
	@Override
	public LogicalLayer addLogicalLayer(String name) {
		if (name == null) {
			return null;
		}
		LogicalLayer layer = _userLayers.get(name);
		if (layer != null) {
			Log.getInstance().warning(
					"Asked to add layer: " + name + " which already exists.");
		}
		else {
			layer = new LogicalLayer(this, name);
			_userLayers.put(name, layer);
			addLogicalLayer(layer);
		}
		return layer;
	}

	/**
	 * Add a layer to this container.
	 * 
	 * @param layer the layer to add.
	 */
	@Override
	public void addLogicalLayer(LogicalLayer layer) {

		_layers.add(layer);
		if (layer != _annotationLayer) {
			_layers.sendToFront(_annotationLayer);
		}
		layer.addDrawableListener(this);
	}

	/**
	 * Get the annotation layer for this obtainer.
	 * 
	 * @return the annotation layer for this obtainer. All drawing tools draw on
	 *         the annotation layer, which is kept on top.
	 */
	@Override
	public LogicalLayer getAnnotationLayer() {
		return _annotationLayer;
	}

	/**
	 * Gets a user layer by name. Do not use for the annotation layer-- for that
	 * use getAnnotationLayer().
	 * 
	 * @param name the name of the user layer.
	 * @return the layer, or <code>null</code>.
	 */
	@Override
	public LogicalLayer getLogicalLayer(String name) {
		LogicalLayer layer = _userLayers.get(name);
		if (layer == null) {
			Log.getInstance().warning("Requested nonexistant layer: " + name);
		}
		return layer;
	}

	/**
	 * Add a layer for containing items rendered on this container..
	 * 
	 * @param layer the layer to add.
	 */
	@Override
	public void removeLogicalLayer(LogicalLayer layer) {
		if (layer != null) {
			_layers.remove(layer);
			// also remove from hash
			if (_userLayers.contains(layer)) {
				_userLayers.remove(layer.getName());
			}
		}
	}

	/**
	 * See if we need to create a new buffer
	 * 
	 * @return the size of the offscreen buffer.
	 */
	protected Dimension checkOffscreenBuffer() {
		Dimension size = getSize();
		boolean newbuffer = false;
		if (_offscreenBuffer == null) {
			newbuffer = true;
		}
		else if ((_offscreenBuffer.getWidth() != size.width)
				|| (_offscreenBuffer.getHeight() != size.height)) {
			newbuffer = true;
		}

		if (newbuffer) {
			try {
				_offscreenBuffer = new BufferedImage(size.width, size.height,
						BufferedImage.TYPE_INT_RGB);
			} catch (Exception e) {
				Log.getInstance().exception(e);
				e.printStackTrace();
				_offscreenBuffer = null;
			}

		}
		return size;
	}

	/**
	 * This converts a screen or pixel point to a world point.
	 * 
	 * @param pp contains the local (screen-pixel) point.
	 * @param wp will hold the resultant world point.
	 */
	@Override
	public void localToWorld(Point pp, Point2D.Double wp) {
		if (localToWorld != null) {
			localToWorld.transform(pp, wp);
		}
	}

	/**
	 * This converts a world point to a screen or pixel point.
	 * 
	 * @param pp will hold the resultant local (screen-pixel) point.
	 * @param wp contains world point.
	 */
	@Override
	public void worldToLocal(Point pp, Point2D.Double wp) {
		if (worldToLocal != null) {
			try {
				worldToLocal.transform(wp, pp);
			} catch (NullPointerException npe) {

				System.err.println(
						"Null pointer exception in BaseContainer worldToLocal pp = "
								+ pp + "  wp = " + wp);
				npe.printStackTrace();
			}
		}
		else {
			// System.err.println("null world to local for " +
			// _view.getTitle());
			// Throwable t = new Throwable();
			// t.printStackTrace();
		}
	}

	/**
	 * This converts a world rectangle to a screen or pixel rectangle.
	 * 
	 * @param r will hold the resultant local (screen-pixel) rectangle.
	 * @param wr contains the world rectangle.
	 */
	@Override
	public void worldToLocal(Rectangle r, Rectangle.Double wr) {
		// New version to accommodate world with x decreasing right
		Point2D.Double wp0 = new Point2D.Double(wr.getMinX(), wr.getMinY());
		Point2D.Double wp1 = new Point2D.Double(wr.getMaxX(), wr.getMaxY());
		Point p0 = new Point();
		Point p1 = new Point();
		worldToLocal(p0, wp0);
		worldToLocal(p1, wp1);

		int x = Math.min(p0.x, p1.x);
		int y = Math.min(p0.y, p1.y);
		int w = Math.abs(p1.x - p0.x);
		int h = Math.abs(p1.y - p0.y);
		r.setBounds(x, y, w, h);
	}

	/**
	 * This converts a screen or local rectangle to a world rectangle.
	 * 
	 * @param r contains the local (screen-pixel) rectangle.
	 * @param wr will hold the resultant world rectangle.
	 */
	@Override
	public void localToWorld(Rectangle r, Rectangle.Double wr) {
		Point p0 = new Point(r.x, r.y);
		Point p1 = new Point(r.x + r.width, r.y + r.height);
		Point2D.Double wp0 = new Point2D.Double();
		Point2D.Double wp1 = new Point2D.Double();
		localToWorld(p0, wp0);
		localToWorld(p1, wp1);

		// New version to accommodate world with x decreasing right
		double x = wp0.x;
		double y = wp1.y;
		double w = wp1.x - wp0.x;
		double h = wp0.y - wp1.y;
		wr.setFrame(x, y, w, h);

	}

	/**
	 * This converts a world point to a screen or pixel point.
	 * 
	 * @param pp will hold the resultant local (screen-pixel) point.
	 * @param wx the world x coordinate.
	 * @param wy the world y coordinate.
	 */
	@Override
	public void worldToLocal(Point pp, double wx, double wy) {
		worldToLocal(pp, new Point2D.Double(wx, wy));
	}

	/**
	 * Pan the container.
	 * 
	 * @param dh the horizontal step in pixels.
	 * @param dv the vertical step in pixels.
	 */
	@Override
	public void pan(int dh, int dv) {

		Rectangle r = getBounds();
		int xc = r.width / 2;
		int yc = r.height / 2;

		xc -= dh;
		yc -= dv;

		Point p = new Point(xc, yc);
		recenter(p);
	}

	/**
	 * Recenter the container at the point of a click.
	 * 
	 * @param pp the point in question. It will be the new center.
	 */
	@Override
	public void recenter(Point pp) {
		Point2D.Double wp = new Point2D.Double();
		localToWorld(pp, wp);
		recenter(_worldSystem, wp);
		setDirty(true);
		refresh();
	}

	/**
	 * Begin preparations for a zoom.
	 */
	@Override
	public void prepareToZoom() {
		_previousWorldSystem = copy(_worldSystem);
	}

	/**
	 * Restore the default world. This gets us back to the original zoom level.
	 */
	@Override
	public void restoreDefaultWorld() {
		_worldSystem = copy(_defaultWorldSystem);
		setDirty(true);
		refresh();
	}

	/**
	 * Refresh the container. Base implementation is the offscreen refresh. Sets
	 * the offscreen buffer to dirty.
	 */
	@Override
	public void refresh() {
		_offscreenBufferDirty = true;
		repaint();

		if (getToolBar() != null) {
			if (getToolBar().getUserComponent() != null) {
				getToolBar().getUserComponent().repaint();
			}
		}
	}

	/**
	 * Convenience routine to scale the container.
	 * 
	 * @param scaleFactor the scale factor.
	 */
	@Override
	public void scale(double scaleFactor) {
		prepareToZoom();
		scale(_worldSystem, scaleFactor);
		setDirty(true);
		refresh();
	}

	/**
	 * Undo that last zoom.
	 */
	@Override
	public void undoLastZoom() {
		Rectangle2D.Double temp = _worldSystem;
		_worldSystem = copy(_previousWorldSystem);
		_previousWorldSystem = temp;
		setDirty(true);
		refresh();
	}

	/**
	 * This is called when we have completed a rubber banding. pane.
	 * 
	 * @param b The rubber band bounds.
	 */

	@Override
	public void rubberBanded(Rectangle b) {
		// if too small, don't zoom
		if ((b.width < 10) || (b.height < 10)) {
			return;
		}
		localToWorld(b, _worldSystem);
		setDirty(true);
		refresh();
	}

	/**
	 * Convenience method for setting the dirty flag for all items on all
	 * layers. Things that make a container dirty:
	 * <ol>
	 * <li>container was resized
	 * <li>zooming
	 * <li>undo zooming
	 * <li>scaling
	 * <li>restoring default world
	 * <li>panning
	 * <li>recenter
	 * </ol>
	 * 
	 * @param dirty the new value of the dirty flag.
	 */
	@Override
	public void setDirty(boolean dirty) {

		setAffineTransforms();
		_offscreenBuffer = null;
		_offscreenBufferDirty = true;

		if (_layers != null) {
			for (IDrawable layer : _layers) {
				layer.setDirty(dirty);
			}
		}
	}

	/**
	 * Find an item, if any, at the point.
	 * 
	 * @param lp The pixel point in question.
	 * @return the topmost satisfying item, or null.
	 */
	@Override
	public AItem getItemAtPoint(Point lp) {
		if (_layers == null) {
			return null;
		}

		for (int i = _layers.size() - 1; i >= 0; i--) {
			LogicalLayer layer = ((LogicalLayer) _layers.get(i));
			AItem item = layer.getItemAtPoint(this, lp);
			if (item != null) {
				return item;
			}
		}
		return null;
	}

	/**
	 * Obtain a collection of all enclosed items across all layers.
	 * 
	 * @param rect the rectangle in question.
	 * @return all items on all layers enclosed by the rectangle.
	 */

	@Override
	public Vector<AItem> getEnclosedItems(Rectangle rect) {

		if (rect == null) {
			return null;
		}

		Vector<AItem> items = new Vector<AItem>(25);
		for (IDrawable drawable : _layers) {
			((LogicalLayer) drawable).addEnclosedItems(this, items, rect);
		}
		return items;
	}

	/**
	 * Find all items, if any, at the point.
	 * 
	 * @param lp the pixel point in question.
	 * @return all items across all layers that contain the given point. It may
	 *         be an empty vector, but it won't be <code>null</null>.
	 */
	@Override
	public Vector<AItem> getItemsAtPoint(Point lp) {
		Vector<AItem> items = new Vector<AItem>(25, 10);

		if (_layers != null) {
			for (int i = _layers.size() - 1; i >= 0; i--) {
				LogicalLayer layer = ((LogicalLayer) _layers.get(i));
				layer.addItemsAtPoint(items, this, lp);
			}
		}

		return items;
	}

	/**
	 * Check whether at least one item on any layer is selected.
	 * 
	 * @return <code>true</code> if at least one item on any layer is selected.
	 */
	@Override
	public boolean anySelectedItems() {
		if (_layers != null) {
			for (IDrawable drawable : _layers) {
				LogicalLayer layer = (LogicalLayer) drawable;
				if (layer.anySelected()) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Delete all selected items, across all layers.
	 * 
	 * @param container the container they lived on.
	 */
	@Override
	public void deleteSelectedItems(IContainer container) {
		if (_layers != null) {
			for (IDrawable drawable : _layers) {
				LogicalLayer layer = (LogicalLayer) drawable;
				layer.deleteSelectedItems(container);
			}
		}
	}

	/**
	 * Select or deselect all items, across all layers.
	 * 
	 * @param select the selection flag.
	 */
	@Override
	public void selectAllItems(boolean select) {
		if (_layers != null) {
			for (IDrawable drawable : _layers) {
				LogicalLayer layer = (LogicalLayer) drawable;
				layer.selectAllItems(select);
			}
		}
	}

	/**
	 * Get the background image.
	 * 
	 * @return the fully painted background image.
	 */
	@Override
	public BufferedImage getImage() {
		// if null, create sized to current component size
		if (_offscreenBuffer == null) {
			_offscreenBuffer = GraphicsUtilities.getComponentImageBuffer(this);
			_offscreenBufferDirty = true;
		}
		// if dirty, paint on it
		if (_offscreenBufferDirty) {
			GraphicsUtilities.paintComponentOnImage(this, _offscreenBuffer);
			_offscreenBufferDirty = false;
		}
		return _offscreenBuffer;
	}

	/**
	 * Set whether or not the offscreen buffer is dirty.
	 * 
	 * @param dirty If <code>true</code>, offscreen buffer is dirty
	 */
	public void setOffscreenBufferDirty(boolean dirty) {
		_offscreenBufferDirty = dirty;
	}

	/**
	 * Zooms to the specified area.
	 * 
	 * @param xmin minimum x coordinate.
	 * @param xmax maximum x coordinate.
	 * @param ymin minimum y coordinate.
	 * @param ymax maximum y coordinate.
	 */
	@Override
	public void zoom(final double xmin, final double xmax, final double ymin,
			final double ymax) {
		prepareToZoom();
		_worldSystem = new Rectangle2D.Double(xmin, ymin, xmax - xmin,
				ymax - ymin);
		setDirty(true);
		refresh();
	}

	/**
	 * Reword the container. I.e., the new values will be the default world
	 * 
	 * @param xmin minimum x coordinate.
	 * @param xmax maximum x coordinate.
	 * @param ymin minimum y coordinate.
	 * @param ymax maximum y coordinate.
	 */
	@Override
	public void reworld(final double xmin, final double xmax, final double ymin,
			final double ymax) {
		_defaultWorldSystem.setFrame(xmin, ymin, xmax - xmin, ymax - ymin);
		_previousWorldSystem.setFrame(_defaultWorldSystem);
		zoom(xmin, xmax, ymin, ymax);
	}

	/**
	 * Get this container's tool bar.
	 * 
	 * @return this container's tool bar, or <code>null</code>.
	 */
	@Override
	public BaseToolBar getToolBar() {
		return _toolBar;
	}

	/**
	 * Set this container's tool bar.
	 * 
	 * @param toolBar the new toolbar.
	 */
	@Override
	public void setToolBar(BaseToolBar toolBar) {
		_toolBar = toolBar;
	}

	/**
	 * The mouse has been clicked.
	 * 
	 * @param mouseEvent the causal event.
	 */
	@Override
	public void mouseClicked(MouseEvent mouseEvent) {

		if (!isEnabled()) {
			Toolkit.getDefaultToolkit().beep();
			return;
		}
	}

	/**
	 * The mouse has entered the container.
	 * 
	 * @param mouseEvent the causal event.
	 */
	@Override
	public void mouseEntered(MouseEvent mouseEvent) {
		_currentMousePoint = mouseEvent.getPoint();

		ToolBarToggleButton mtb = getActiveButton();
		if (mtb != null) {
			setCursor(mtb.canvasCursor());
		}
	}

	/**
	 * The mouse has exited the container.
	 * 
	 * @param mouseEvent the causal event.
	 */
	@Override
	public void mouseExited(MouseEvent mouseEvent) {
		_currentMousePoint = null;
	}

	/**
	 * The mouse was pressed in the container.
	 * 
	 * @param mouseEvent the causal event.
	 */
	@Override
	public void mousePressed(MouseEvent mouseEvent) {
	}

	/**
	 * The mouse was released in the container.
	 * 
	 * @param mouseEvent the causal event.
	 */
	@Override
	public void mouseReleased(MouseEvent mouseEvent) {
		if (_toolBar != null) {
			_toolBar.checkButtonState();
		}
	}

	/**
	 * The mouse was dragged in the container.
	 * 
	 * @param mouseEvent the causal event.
	 */
	@Override
	public void mouseDragged(MouseEvent mouseEvent) {
		if (!isEnabled()) {
			return;
		}
		locationUpdate(mouseEvent, true);
	}

	/**
	 * The mouse has moved in the container.
	 * 
	 * @param mouseEvent the causal event.
	 */
	@Override
	public void mouseMoved(MouseEvent mouseEvent) {
		if (!isEnabled()) {
			return;
		}
		_currentMousePoint = mouseEvent.getPoint();
		locationUpdate(mouseEvent, false);
	}

	/**
	 * Convert the mouse event location to a world point.
	 * 
	 * @param me the mouse event
	 * @return the world location of the mouse click
	 */
	protected Point2D.Double getLocation(MouseEvent me) {
		if (me == null) {
			return null;
		}

		Point2D.Double wp = new Point2D.Double();
		localToWorld(me.getPoint(), wp);
		return wp;
	}

	/**
	 * Gets the current mouse position.
	 * 
	 * @return the current mouse position.
	 */
	public Point getCurrentMousePoint() {
		return _currentMousePoint;
	}

	/**
	 * Get the active button on the toolbar, if there is a toolbar.
	 * 
	 * @return the active toggle button.
	 */
	@Override
	public ToolBarToggleButton getActiveButton() {
		BaseToolBar tb = getToolBar();
		if (tb == null) {
			return null;
		}
		else {
			return tb.getActiveButton();
		}
	}

	/**
	 * Convenience method to update the location string in the toolbar.
	 * 
	 * @param mouseEvent the causal event.
	 * @param dragging <code>true</code> if we are dragging
	 */
	@Override
	public void locationUpdate(MouseEvent mouseEvent, boolean dragging) {

		_lastLocationMouseEvent = mouseEvent;
		ToolBarToggleButton mtb = getActiveButton();
		Point2D.Double wp = null;
		wp = getLocation(mouseEvent);

		if (mtb == null) {
			if (_feedbackControl != null) {
				_feedbackControl.updateFeedback(mouseEvent, wp, dragging);
			}
			return;
		}

		if (mtb == _toolBar.getPointerButton()) { // pointer active
			getToolBar().setText(Point2DSupport.toString(wp));
			if (_feedbackControl != null) {
				_feedbackControl.updateFeedback(mouseEvent, wp, dragging);
			}
		}
		else if (mtb == _toolBar.getPanButton()) { // pan active
			// do nothing
		}
		else { // default case
			wp = getLocation(mouseEvent);
			getToolBar().setText(Point2DSupport.toString(wp));
			if (_feedbackControl != null) {
				_feedbackControl.updateFeedback(mouseEvent, wp, dragging);
			}
		}
	}

	/**
	 * Force a redo of the feedback even though the mouse didn't move. This is
	 * useful, for example, when control-N'ing events.
	 */
	@Override
	public void redoFeedback() {
		if (_lastLocationMouseEvent != null) {
			locationUpdate(_lastLocationMouseEvent, false);
		}
	}

	/**
	 * Get the view (internal frame) that holds this container.
	 * 
	 * @return the view (internal frame) that holds this container.
	 */
	@Override
	public BaseView getView() {
		return _view;
	}

	/**
	 * An item has changed.
	 * 
	 * @param list the list it was on, which will be a layer.
	 * @param drawable the drawable (item) that changed.
	 * @param type the type of the change.
	 */
	@Override
	public void drawableChanged(DrawableList list, IDrawable drawable,
			DrawableChangeType type) {

		LogicalLayer layer = (LogicalLayer) list;
		AItem item = (drawable == null) ? null : (AItem) drawable;

		switch (type) {
		case ADDED:
			// Log.getInstance().info("Item added: " + item);
			break;

		case DESELECTED:
			// Log.getInstance().info("Item deselected: " + item);
			break;

		case DOUBLECLICKED:
			// Log.getInstance().info("Item double clicked: " + item);
			break;

		case HIDDEN:
			// Log.getInstance().info("Item hidden: " + item);
			break;

		case MODIFIED:
			// Log.getInstance().info("Item modified: " + item);
			break;

		case MOVED:
			// Log.getInstance().info("Item moved: " + item);
			break;

		case REMOVED:
			// Log.getInstance().info("Item removed: " + item);
			if (item == _youAreHereItem) {
				_youAreHereItem = null;
			}
			break;

		case RESIZED:
			// Log.getInstance().info("Item resized: " + item);
			break;

		case ROTATED:
			// Log.getInstance().info("Item rotated: " + item);
			break;

		case SELECTED:
			// Log.getInstance().info("Item selected: " + item);
			break;

		case SHOWN:
			// Log.getInstance().info("Item shown: " + item);
			break;

		case LISTCLEARED:
			// Log.getInstance().info("Layer cleared: " + layer.getName());
			break;

		case LISTHIDDEN:
			// Log.getInstance().info("Layer hidden: " + layer.getName());
			break;

		case LISTSHOWN:
			// Log.getInstance().info("Layer shown: " + layer.getName());
			break;
		}

		// for now, lets not quibble
		if (item != null) {
			item.setDirty(true);
		}
		_offscreenBufferDirty = true;
	}

	/**
	 * Sets the feedback pane. This is an optional alternative to a HUD.
	 * 
	 * @param feedbackPane the feedback pane.
	 */
	@Override
	public void setFeedbackPane(FeedbackPane feedbackPane) {
		_feedbackPane = feedbackPane;
	}

	/**
	 * Get the optional feedback pane.
	 * 
	 * @return the feedbackPane
	 */
	@Override
	public FeedbackPane getFeedbackPane() {
		return _feedbackPane;
	}

	/**
	 * Return the object that controls the container's feedback. You can and and
	 * remove feedback providers using this object.
	 * 
	 * @return the object that controls the container's feedback.
	 */
	@Override
	public FeedbackControl getFeedbackControl() {
		return _feedbackControl;
	}

	/**
	 * Get the optional YouAreHereItem
	 * 
	 * @return the youAreHereItem
	 */
	@Override
	public YouAreHereItem getYouAreHereItem() {
		return _youAreHereItem;
	}

	/**
	 * Set the optional YouAreHereItem.
	 * 
	 * @param youAreHereItem the youAreHereItem to set
	 */
	@Override
	public void setYouAreHereItem(YouAreHereItem youAreHereItem) {
		_youAreHereItem = youAreHereItem;
	}

	/**
	 * This is sometimes used as needed (i.e., not created until requested).
	 * That will generally make it the topmost view--so it is good for things
	 * like a reference point (YouAreHereItem).
	 * 
	 * @return the glass layer.
	 */
	@Override
	public LogicalLayer getGlassLayer() {
		if (_glassLayer == null) {
			_glassLayer = new LogicalLayer(this, "Glass Layer");
			_layers.add(_glassLayer);
		}
		return _glassLayer;
	}

	/**
	 * Get the underlying component, which is me.
	 * 
	 * @return the underlying component, which is me.
	 */
	@Override
	public Component getComponent() {
		return this;
	}

	/**
	 * Set the after-draw drawable for this container.
	 * 
	 * @param afterDraw the new after-draw drawable.
	 */
	@Override
	public void setAfterDraw(IDrawable afterDraw) {
		_afterDraw = afterDraw;
	}

	/**
	 * get the after drawer
	 * 
	 * @return the after drawer
	 */
	public IDrawable getAfterDraw() {
		return _afterDraw;
	}

	/**
	 * Set the before-draw drawable.
	 * 
	 * @param beforeDraw the new before-draw drawable.
	 */
	@Override
	public void setBeforeDraw(IDrawable beforeDraw) {
		_beforeDraw = beforeDraw;
	}

	/**
	 * get the before drawer
	 * 
	 * @return the before drawer
	 */
	public IDrawable getBeforeDraw() {
		return _beforeDraw;
	}

	/**
	 * Set the optional magnification drawer
	 * 
	 * @param mdraw the optional magnification drawer
	 */
	public void setMagnificationDraw(IDrawable mdraw) {
		_magDraw = mdraw;
	}

	/**
	 * Get the optional magnification drawer
	 * 
	 * @return the optional magnification drawer
	 */
	public IDrawable getMagnificationDraw() {
		return _magDraw;
	}

	/**
	 * From a given screen rectangle, create an ellipse item.
	 * 
	 * @param layer the layer to put the item on
	 * @param rect the bounding screen rectangle, probably from rubber banding.
	 * @return the new item
	 */
	@Override
	public AItem createEllipseItem(LogicalLayer layer, Rectangle rect) {
		int l = rect.x;
		int t = rect.y;
		int r = l + rect.width;
		int b = t + rect.height;

		int xc = (l + r) / 2;
		int yc = (t + b) / 2;

		Point p0 = new Point(l, yc);
		Point p1 = new Point(r, yc);

		Point2D.Double wp0 = new Point2D.Double();
		Point2D.Double wp1 = new Point2D.Double();
		localToWorld(p0, wp0);
		localToWorld(p1, wp1);
		double width = wp0.distance(wp1);

		p0.setLocation(xc, t);
		p1.setLocation(xc, b);
		localToWorld(p0, wp0);
		localToWorld(p1, wp1);
		double height = wp0.distance(wp1);

		Point pc = new Point(xc, yc);
		Point2D.Double center = new Point2D.Double();
		localToWorld(pc, center);

		return new EllipseItem(layer, width, height, 0.0, center);
	}

	/**
	 * From a given screen rectangle, create a rectangle item.
	 * 
	 * @param layer the layer to put the item on
	 * @param b the screen rectangle, probably from rubber banding.
	 * @return the new item
	 */
	@Override
	public AItem createRectangleItem(LogicalLayer layer, Rectangle b) {
		Rectangle2D.Double wr = new Rectangle2D.Double();
		localToWorld(b, wr);

		// return new PanelItem(layer, new Point2D.Double(wr.x, wr.y),
		// b.width, b.height);
		// return new PlotItem(layer, new Point2D.Double(wr.x, wr.y),
		// b.width, b.height);

		return PlotItem.createHistogram(layer, new Point2D.Double(wr.x, wr.y),
				b.width, b.height, "Title", "X axis", "Y axis", "curve 1", 1,
				112, 112);
		//// return new RectangleItem(layer, wr);
	}

	/**
	 * From two given screen points, create a line item
	 * 
	 * @param layer the layer to put the item on
	 * @param p0 one screen point, probably from rubber banding.
	 * @param p1 another screen point, probably from rubber banding.
	 * @return the new item
	 */
	@Override
	public AItem createLineItem(LogicalLayer layer, Point p0, Point p1) {
		Point2D.Double wp0 = new Point2D.Double();
		Point2D.Double wp1 = new Point2D.Double();
		localToWorld(p0, wp0);
		localToWorld(p1, wp1);
		return new LineItem(layer, wp0, wp1);
	}

	/**
	 * Create a radarc item from the given parameters, probably obtained by
	 * rubberbanding.
	 * 
	 * @param layer the layer to put the item on
	 * @param pc the center of the arc
	 * @param p1 the point at the end of the first leg. Thus pc->p1 determine
	 *            the radius.
	 * @param arcAngle the opening angle COUNTERCLOCKWISE in degrees.
	 * @return the new item
	 */
	@Override
	public AItem createRadArcItem(LogicalLayer layer, Point pc, Point p1,
			double arcAngle) {
		Point2D.Double wpc = new Point2D.Double();
		Point2D.Double wp1 = new Point2D.Double();
		localToWorld(pc, wpc);
		localToWorld(p1, wp1);
		return new RadArcItem(layer, wpc, wp1, arcAngle);
		// return new ArcItem(layer, wpc, wp1, arcAngle);
	}

	/**
	 * From a given screen polygon, create a polygon item.
	 * 
	 * @param layer the layer to put the item on
	 * @param pp the screen polygon, probably from rubber banding.
	 * @return the new item
	 */
	@Override
	public AItem createPolygonItem(LogicalLayer layer, Point pp[]) {
		if ((pp == null) || (pp.length < 3)) {
			return null;
		}
		Point2D.Double wp[] = new Point2D.Double[pp.length];
		for (int index = 0; index < pp.length; index++) {
			wp[index] = new Point2D.Double();
			localToWorld(pp[index], wp[index]);
		}

		return new PolygonItem(layer, wp);
	}

	/**
	 * From a given screen polygon, create a polyline item.
	 * 
	 * @param layer the layer to put the item on
	 * @param pp the screen polyline, probably from rubber banding.
	 * @return the new item
	 */
	@Override
	public AItem createPolylineItem(LogicalLayer layer, Point pp[]) {
		if ((pp == null) || (pp.length < 3)) {
			return null;
		}

		Point2D.Double wp[] = new Point2D.Double[pp.length];
		for (int index = 0; index < pp.length; index++) {
			wp[index] = new Point2D.Double();
			localToWorld(pp[index], wp[index]);
		}

		AItem item = new PolylineItem(layer, wp);

		return item;
	}

	protected void disposeOffscreenBuffer() {
		_offscreenBuffer = null;
		_offscreenBufferDirty = true;
	}

	/**
	 * Get a scroll pane with a table for controlling logical layer visibility
	 * 
	 * @return a scroll pane with a table for controlling logical layer
	 *         visibility
	 */
	@Override
	public VisibilityTableScrollPane getVisibilityTableScrollPane() {
		if (_visTable == null) {
			_visTable = new VisibilityTableScrollPane(this, _layers,
					"Layers (drag to reorder)");
		}
		return _visTable;
	}

	/**
	 * Handle a file, one that probably result from a drag and drop or a double
	 * click. Treat it like an "open".
	 * 
	 * @param file the file to handle.
	 */
	@Override
	public void handleFile(File file) {
		// TODO implement
	}

	/**
	 * Get a location string for a point
	 * 
	 * @param wp the world point in question
	 * @return a location string for a point
	 */
	@Override
	public String getLocationString(Point2D.Double wp) {
		return Point2DSupport.toString(wp);
	}

	/**
	 * Get all the layers.
	 * 
	 * @return all logical layers in the container.
	 */
	@Override
	public DrawableList getLogicalLayers() {
		return _layers;
	}

	/**
	 * Create a Point2D.Double or subclass thereof that is appropriate for this
	 * container.
	 * 
	 * @return a Point2D.Double or subclass thereof that is appropriate for this
	 *         container.
	 */
	@Override
	public Point2D.Double getWorldPoint() {
		return new Point2D.Double();
	}

	/**
	 * Get the current world system
	 * 
	 * @return the world system
	 */
	@Override
	public Rectangle2D.Double getWorldSystem() {
		return _worldSystem;
	}

	/**
	 * Set the world system (does not cause redraw)
	 * 
	 * @param wr the new world system
	 */
	@Override
	public void setWorldSystem(Rectangle2D.Double wr) {
		_worldSystem = new Rectangle2D.Double(wr.x, wr.y, wr.width, wr.height);
	}

	// Get the transforms for world to local and vice versa
	protected void setAffineTransforms() {
		Rectangle bounds = getInsetRectangle();

		if ((bounds == null) || (bounds.width < 1) || (bounds.height < 1)) {
			localToWorld = null;
			worldToLocal = null;
			return;
		}

		if ((_worldSystem == null) || (Math.abs(_worldSystem.width) < 1.0e-12)
				|| (Math.abs(_worldSystem.height) < 1.0e-12)) {
			localToWorld = null;
			worldToLocal = null;
			return;
		}

		double scaleX = _worldSystem.width / bounds.width;
		double scaleY = _worldSystem.height / bounds.height;

		localToWorld = AffineTransform.getTranslateInstance(
				_worldSystem.getMinX(), _worldSystem.getMaxY());
		localToWorld
				.concatenate(AffineTransform.getScaleInstance(scaleX, -scaleY));
		localToWorld.concatenate(
				AffineTransform.getTranslateInstance(-bounds.x, -bounds.y));

		try {
			worldToLocal = localToWorld.createInverse();
		} catch (NoninvertibleTransformException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Convert a pixel based polygon to a world based polygon.
	 * 
	 * @param polygon the pixel based polygon
	 * @param worldPolygon the world based polygon
	 */
	@Override
	public void localToWorld(Polygon polygon, WorldPolygon worldPolygon) {
		Point2D.Double wp = new Point2D.Double();
		Point pp = new Point();
		for (int i = 0; i < polygon.npoints; ++i) {
			pp.setLocation(polygon.xpoints[i], polygon.ypoints[i]);
			localToWorld(pp, wp);
			worldPolygon.addPoint(wp.x, wp.y);
		}
	}

	/**
	 * Convert a world based polygon to a pixel based polygon.
	 * 
	 * @param polygon the pixel based polygon
	 * @param worldPolygon the world based polygon
	 */
	@Override
	public void worldToLocal(Polygon polygon, WorldPolygon worldPolygon) {
		Point pp = new Point();
		for (int i = 0; i < worldPolygon.npoints; ++i) {
			worldToLocal(pp, worldPolygon.xpoints[i], worldPolygon.ypoints[i]);
			polygon.addPoint(pp.x, pp.y);
		}
	}

	@Override
	public void setView(BaseView view) {
		_view = view;
	}

	/**
	 * The mouse scroll wheel has been moved.
	 * 
	 * @param mouseEvent the causal event.
	 */
	@Override
	public void mouseWheelMoved(MouseWheelEvent mouseEvent) {
		// TODO properly implement
		// Cannot scroll HUD as it is not scrollable, or is it?
		// Is it possible to encapsulate HUD in a transparent JScrollPane?
	}

	/**
	 * Obtain the inset rectangle. Insets are the inert region around the
	 * container's active area. Often there are no insets. Sometimes they are
	 * used so that text can be written in the inset area, such as for plot
	 * view.
	 * 
	 * @return the inset rectangle.
	 */
	@Override
	public Rectangle getInsetRectangle() {
		Rectangle b = getComponent().getBounds();
		if (b == null) {
			return null;
		}

		// ignore b.x and b.y as usual
		int left = _lMargin;
		int top = _tMargin;
		int right = b.width - _rMargin;
		int bottom = b.height - _bMargin;

		Rectangle screenRect = new Rectangle(left, top, right - left,
				bottom - top);
		return screenRect;

	}

	/**
	 * Set the left margin
	 * 
	 * @param lMargin the left margin
	 */
	@Override
	public void setLeftMargin(int lMargin) {
		_lMargin = lMargin;
	}

	/**
	 * Set the top margin
	 * 
	 * @param tMargin the top margin
	 */
	@Override
	public void setTopMargin(int tMargin) {
		_tMargin = tMargin;
	}

	/**
	 * Set the right margin
	 * 
	 * @param rMargin the right margin
	 */
	@Override
	public void setRightMargin(int rMargin) {
		_rMargin = rMargin;
	}

	/**
	 * Set the bottom margin
	 * 
	 * @param bMargin the bottom margin
	 */
	@Override
	public void setBottomMargin(int bMargin) {
		_bMargin = bMargin;
	}

	/**
	 * Recenter the world rectangle.
	 * 
	 * @param wr the affected rectangle
	 * @param newCenter the new center.
	 */
	private void recenter(Rectangle2D.Double wr, Point2D.Double newCenter) {
		wr.x = newCenter.x - wr.width / 2.0;
		wr.y = newCenter.y - wr.height / 2.0;
	}

	/**
	 * SCale the world rectangle, keeping the center fixed.
	 * 
	 * @param wr the affected rectangle
	 * @param scale the factor to scale by.
	 */
	private void scale(Rectangle2D.Double wr, double scale) {
		double xc = wr.getCenterX();
		double yc = wr.getCenterY();
		wr.width *= scale;
		wr.height *= scale;
		wr.x = xc - wr.width / 2.0;
		wr.y = yc - wr.height / 2.0;
	}

	// copier
	private Rectangle2D.Double copy(Rectangle2D.Double wr) {
		return new Rectangle2D.Double(wr.x, wr.y, wr.width, wr.height);
	}

	/**
	 * The active toolbar button changed.
	 * 
	 * @param activeButton the new active button.
	 */
	@Override
	public void activeToolBarButtonChanged(ToolBarToggleButton activeButton) {
	}

	/**
	 * Have you handled the print button so the default action is ignored.
	 * @return <code>true</code> if the printer button was handled.
	 */
	@Override
	public boolean handledPrint() {
		return false;
	}
	
	/**
	 * Have you handled the camera button so the default action is ignored.
	 * @return <code>true</code> if the camera button was handled.
	 */
	@Override
	public boolean handledCamera() {
		return false;
	}

}
