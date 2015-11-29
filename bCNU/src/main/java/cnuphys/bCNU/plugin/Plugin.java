package cnuphys.bCNU.plugin;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Properties;

import cnuphys.bCNU.graphics.toolbar.ToolBarToggleButton;
import cnuphys.bCNU.item.AItem;
import cnuphys.bCNU.plugin.shapes.PluginCircle;
import cnuphys.bCNU.plugin.shapes.PluginEllipse;
import cnuphys.bCNU.plugin.shapes.PluginLine;
import cnuphys.bCNU.plugin.shapes.PluginPolygon;
import cnuphys.bCNU.plugin.shapes.PluginPolyline;
import cnuphys.bCNU.plugin.shapes.PluginRectangle;
import cnuphys.bCNU.plugin.shapes.PluginShape;
import cnuphys.bCNU.plugin.shapes.PluginSquare;
import cnuphys.bCNU.plugin.shapes.PluginSymbol;
import cnuphys.bCNU.view.PluginView;
import cnuphys.bCNU.view.VirtualView;

public abstract class Plugin {
    
    //virtual view constraints
    public static final int UPPERLEFT = VirtualView.UPPERLEFT;
    public static final int UPPERRIGHT = VirtualView.UPPERRIGHT;
    public static final int BOTTOMLEFT = VirtualView.BOTTOMLEFT;
    public static final int BOTTOMRIGHT = VirtualView.BOTTOMRIGHT;
    public static final int CENTER = VirtualView.CENTER;
        
    //count of plugins
    private static int _pluginCount;
    
    //the underlying view
    private final PluginView _view;
    
    //the plugin properties
    private Properties _props;
    
    //hashtable of shapes
    private Hashtable<AItem, PluginShape> _shapes = new Hashtable<AItem, PluginShape>();
    
    //most recently mouseovered shape
    private PluginShape _hotShape;
    
    /**
     * Create a plugin
     */
    public Plugin() {
	Rectangle2D.Double world = new Rectangle2D.Double();
	_pluginCount++;
	
	_props = new Properties();
	
	//initialize 
	initializePluginWorld(world);
	
	//get the title
	String title = getPluginTitle();
	if (title == null) {
	    title = "Plugin " + _pluginCount;
	}
	
	//customize
	customizePlugin();
	
	//create the underlying bCNU view
	_view = new PluginView(title, 
		world.x, world.y,
		world.x + world.width,
		world.y + world.height, _props);
	_view.setPlugin(this);
	
	// the mouse listener
	MouseAdapter ml = new MouseAdapter() {
	    
	    @Override
	    public void mousePressed(MouseEvent e) {
		if (e.isPopupTrigger()) {
		    handlePopupTrigger(e);
		}
	    }

	    @Override
	    public void mouseClicked(MouseEvent e) {
		if (e.isControlDown()) {
		    return;
		}
		
		boolean mb1 = (e.getButton() == MouseEvent.BUTTON1);
		if (mb1) {
		    handleMouseClick(e);
		}
	    }
	};
	
	//mouse motion listener
	MouseMotionAdapter mml = new MouseMotionAdapter() {

	    @Override
	    public void mouseMoved(MouseEvent e) {
		handleMouseMoved(e);
	    }
	    
	};
	
	_view.getContainer().getComponent().addMouseListener(ml);
	_view.getContainer().getComponent().addMouseMotionListener(mml);
	
	//add the shapes
	addInitialShapes();
    }
    
    //handle that a popup trigger occurred
    private void handlePopupTrigger(MouseEvent e) {
	PluginShape shape = PluginSupport.getShapeAtPoint(this, e.getPoint());
	if (shape != null) {
	    shapePopupTrigger(shape, e.getPoint());
	}
    }

    //handle that a mouse click on a shape
    private void handleMouseClick(MouseEvent e) {
	PluginShape shape = PluginSupport.getShapeAtPoint(this, e.getPoint());
	if (shape != null) {
	    if (e.getClickCount() == 1) { // single click
		shapeClick(shape, 1, e.getPoint());
	    }
	    else if (e.getClickCount() == 2) { // double clicks
		shapeClick(shape, 2, e.getPoint());
	    }
	}
    }

    
    //handle that a mouse has moved inside the plugin's view
    private void handleMouseMoved(MouseEvent e) {
	PluginShape shape = PluginSupport.getShapeAtPoint(this, e.getPoint());
	if (_hotShape == shape) {
	    return;
	}
	_hotShape = shape;
	mouseOverShape(_hotShape);
    }

    /**
     * Get the collection of all plugin shapes for this plugin.
     * @return the collection of all plugin shapes
     */
    public Collection<PluginShape> getShapes() {
	return _shapes.values();
    }
    
    
    /**
     * Obtain the plugin shape from the BCNU item. This is for expert use.
     * Routine plugin developers should never call this.
     * @param item the underlying bCNU item
     * @return the plugin shape.
     */
    public PluginShape fromItem(AItem item) {
	if (item == null) {
	    return null;
	}
	
	return _shapes.get(item);
    }
    
    
    /**
     * Initialize the plugin size in world (physical) coordinates (not pixel size). The world must be
     * set using the provided object. The units are arbitrary. An example:
     * <br>
     * <code>
     * world.x = -200;
     * world.y = 0;
     * world.width = 600;
     * world.height = 400;
     * </code>
     * </br>
     * sets a world system from (-200, 0) at the lower left to (400, 400) at the upper right. 
     * @param world use this object to fill in the world default coordinate extents.
     */
    public abstract void initializePluginWorld(Rectangle2D.Double world);
    
    /**
     * Called by the constructor so you can customize your plugin. 
     * This can be implemented as an empty method
     * in which case you get a default plug-in. Customizing is done by a series of calls
     * to <code>addProperty</code>. For example:
     * <p>
     * <code>
     * addProperty(PluginProperties.WIDTH, 800);
     * addProperty(PluginProperties.HEIGHT, 600);
     * addProperty(PluginProperties.BACKGROUND, Color.yellow);
     * </code>
     * <p>
     * will set the pixel size of the plugin to 800x600 and the background to yellow.
     */
    public abstract void customizePlugin();
    
    /**
     * Get the underlying BCNU view. Note: Expert use only. Routine plugin developers
     * should never need to use this method.
     * @return the plugin's underlying BCNU view
     */
    public PluginView getView() {
	return _view;
    }
    
    /**
     * Called by the constructor to get the title.
     * Return the desired title of the plugin. Return some relevant text, such as
     * "HTCC"
     * @return the title of the plugin (this will be view's title).
     */
    public abstract String getPluginTitle();
    
    /**
     * Add a key-value property to the plugin. This is only effectual when used inside
     * the {@link customizePlugin()} callback.
     * An example is 
     * <p>
     * <code>
     * addProperty(PluginProperties.BACKGROUND, "red");
     * </code>
     * <p>
     * @param key the key. A String, safest to use the predefined constants in the 
     * class PluginProperties.
     * @param value the corresponding value. Can be any type of object or primitive
     * type like an <code>int</code>.
     * @see PluginProperties
     */
    protected void addProperty(String key, Object value) {
	_props.put(key, value);
    }
    
    /**
     * Add the initial shapes.
     */
    public abstract void addInitialShapes();

    /**
     * Refresh the plugin. This causes a  complete redraw.
     */
    public void refresh() {
	_view.getContainer().refresh();
    }
    
    /**
     * Add a shape to the plugin. This is called by the PluginShape constructor,
     * so it should not be needed by a routine plugin developer.
     * 
     * @param shape the shape to add
     */
    public void addShape(PluginShape shape) {
	if ((shape != null) && (shape.getItem() != null)) {
	    _shapes.put(shape.getItem(), shape);
	}
    }
    
    /**
     * Remove the shape from the plugin.
     * 
     * @param shape the shape to remove
     */
    public void removeShape(PluginShape shape) {
	if ((shape != null) && (shape.getItem() != null)) {
	    
	    shape = _shapes.remove(shape.getItem());
	    
	    if (shape != null) {
		//delete underlying bCNU item
		_view.getShapeLayer().deleteItem(shape.getItem());
	    }
	}
    }
    
    /**
     * Check whether the pointer bar is active on the tool bar
     * @return <code>true</code> if the Pointer button is active.
     */
    protected boolean isPointerButtonActive() {
	ToolBarToggleButton mtb = _view.getContainer().getActiveButton();
	return (mtb == _view.getContainer().getToolBar().getPointerButton());
    }
    
    /**
     * The mouse is over a shape. If the shape is big and the mouse is moving
     * over the shape, this just gets called once.
     * @param shape the topmost shape under the mouse.
     */
    public abstract void mouseOverShape(PluginShape shape);
    
    /**
     * A shape has been clicked.
     * @param shape the shape in question.
     * @param clickCount either 1 (regular) or 2 (double-click).
     * @param pixelPoint the pixel location of the click
     */
    public abstract void shapeClick(PluginShape shape, int clickCount, Point pixelPoint);
    
    /**
     * The popup trigger (usually a right-click) has occurred for a shape.
     * @param shape the shape in question.
     * @param pixelPoint the pixel location of the click
     */
    public abstract void shapePopupTrigger(PluginShape shape, Point pixelPoint);
    
    /**
     * Update the status line with a new message
     * @param str the new message
     */
    public void updateStatus(String str) {
	_view.updateStatus(str);
    }
    
    /**
     * Create a symbol shape. The defaul is an 8x8 (pixel) square
     * @param info a descriptive string that may appear on mouseovers
     * @param x the horizontal location of the symbol in world (not pixel) coordinates
     * @param y the vertical location of the symbol in world (not pixel) coordinates
     * @param properties extra custom properties
     * @return the new shape
     */
    public PluginShape addSymbol(String info, double x, double y, Object ...properties) {
	return new PluginSymbol(this, info, x, y, properties);
    }

    
    /**
     * Create a square shape
     * @param info a descriptive string that may appear on mouseovers
     * @param xc the horizontal center of the square in world (not pixel) coordinates
     * @param yc the vertical center of the square in world (not pixel) coordinates
     * @param length the length of a side in world coordinates. Note: the square will only look
     * square if the plugin's world system has a 1:1 aspect ratio.
     * @param properties extra custom properties
     * @return the new shape
     */
    public PluginShape addSquare(String info, double xc, double yc, double length, Object ...properties) {
	return new PluginSquare(this, info, xc, yc, length, properties);
    }

    /**
     * Create a circle shape
     * @param info a descriptive string that may appear on mouseovers
     * @param xc the horizontal center of the circle in world (not pixel) coordinates
     * @param yc the vertical center of the circle in world (not pixel) coordinates
     * @param radius the radius of the circle in world coordinates. Note: the circle will only look
     * circular if the plugin's world system has a 1:1 aspect ratio.
     * @param properties extra custom properties
     * @return the new shape
     */
    public PluginShape addCircle(String info, double xc, double yc, double radius, Object ...properties) {
	return new PluginCircle(this, info, xc, yc, radius, properties);
    }
    
    /**
     * Create a line shape
     * @param info a descriptive string that may appear on mouseovers
     * @param xc the horizontal center of the square in world (not pixel) coordinates
     * @param yc the vertical center of the square in world (not pixel) coordinates
     * @param length the length of a side in world coordinates. Note: the square will only look
     * square if the plugin's world system has a 1:1 aspect ratio.
     * @param properties extra custom properties
     * @return the new shape
     */
    public PluginShape addLine(String info, double x1, double y1, double x2, double y2, Object ...properties) {
	return new PluginLine(this, info, x1, y1, x2, y2, properties);
    }
    
    /**
     * Create a rectangle shape
     * @param info a descriptive string that may appear on mouseovers
     * @param x the left of the rectangle in world (not pixel) coordinates
     * @param y the bottom of the rectangle  in world (not pixel) coordinates
     * @param w the width of the rectangle in world coordinates
     * @param h the height of the rectangle in world coordinates
     * @param properties extra custom properties
     * @return the new shape
     */
    public PluginShape addRectangle(String info, double x, double y, double w, double h, Object... properties) {
	return new PluginRectangle(this, info, x, y, w, h, properties);
   }
    
    /**
     * Create an ellipse shape
     * @param info a descriptive string that may appear on mouseovers
     * @param x the left of the bounding rectangle in world (not pixel) coordinates
     * @param y the bottom of the bounding rectangle  in world (not pixel) coordinates
     * @param w the width of the bounding rectangle in world coordinates
     * @param h the height of the bounding rectangle in world coordinates
     * @param properties extra custom properties
     * @return the new shape
     */
    public PluginShape addEllipse(String info, double x, double y, double w, double h, Object... properties) {
	return new PluginEllipse(this, info, x, y, w, h, properties);
   }
    
    /**
     * Create a closed polygon shape
     * 
     * @param info a descriptive string that may appear on mouseovers
     * @param points array of vertex points in world (not pixel) coordinates.
     * @param properties extra custom properties
     * @return the new shape
     * @see java.awt.geom.Point2D
     */
    public PluginShape addPolygon(String info, Point2D.Double points[], Object ...properties) {
	return new PluginPolygon(this, info, points, properties);
    }
    
    /**
     * Create a closed polygon shape from separate (ordered) x and y arrays. The x and
     * y arrays should have the same length,
     * 
     * @param info a descriptive string that may appear on mouseovers
     * @param x horizontal values of vertex points in world (not pixel) coordinates.
     * @param y vertical values of vertex points in world (not pixel) coordinates.
     * @param properties extra custom properties
     * @return the new shape
     * @see java.awt.geom.Point2D
     */
    public PluginShape addPolygon(String info, double x[], double y[], Object ...properties) {
	return new PluginPolygon(this, info, PluginSupport.fromXYArrays(x, y), properties);
    }

    /**
     * Create a polyline shape
     * 
     * @param info a descriptive string that may appear on mouseovers
     * @param points array of vertex points in world (not pixel) coordinates.
     * @param properties extra custom properties
     * @return the new shape
     * @see java.awt.geom.Point2D
     */
    public PluginShape addPolyline(String info, Point2D.Double points[], Object ...properties) {
	return new PluginPolyline(this, info, points, properties);
    }
    
    /**
     * Create a polyline shape from separate (ordered) x and y arrays. The x and
     * y arrays should have the same length,
     * 
     * @param info a descriptive string that may appear on mouseovers
     * @param x horizontal values of vertex points in world (not pixel) coordinates.
     * @param y vertical values of vertex points in world (not pixel) coordinates.
     * @param properties extra custom properties
     * @return the new shape
     * @see java.awt.geom.Point2D
     */
    public PluginShape addPolyline(String info, double x[], double y[], Object ...properties) {
	return new PluginPolyline(this, info, PluginSupport.fromXYArrays(x, y), properties);
    }

    /**
     * Convert a point in pixels to a point in world coordinates
     * @param pixelPoint the pixel values of the point (input)
     * @param worldPoint the world coordinates of the point (output)
     */
    public void pixelToWorld(Point pixelPoint, Point2D.Double worldPoint) {
	_view.getContainer().localToWorld(pixelPoint, worldPoint);
    }
    

    /**
     * Convert a point in world coordinates to a point in pixels
     * @param pixelPoint the pixel values of the point (output)
     * @param worldPoint the world coordinates of the point (input)
     */
    public void worldToPixel(Point pixelPoint, Point2D.Double worldPoint) {
	_view.getContainer().worldToLocal(pixelPoint, worldPoint);
    }

        
}
