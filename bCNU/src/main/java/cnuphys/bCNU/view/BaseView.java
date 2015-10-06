package cnuphys.bCNU.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyVetoException;
import java.util.Properties;
import java.util.UUID;

import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.WindowConstants;

import cnuphys.bCNU.application.Desktop;
import cnuphys.bCNU.attributes.AttributeType;
import cnuphys.bCNU.attributes.Attributes;
import cnuphys.bCNU.component.MagnifyWindow;
import cnuphys.bCNU.format.DoubleFormat;
import cnuphys.bCNU.graphics.container.BaseContainer;
import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.graphics.toolbar.BaseToolBar;
import cnuphys.bCNU.graphics.toolbar.UserToolBarComponent;

/**
 * The BaseView class is the base class for all "views." Views are the internal
 * frames--the independent desktop windows.
 * 
 * @author heddle
 * 
 */
@SuppressWarnings("serial")
public class BaseView extends JInternalFrame {

    private static RenderingHints renderHints = new RenderingHints(
	    RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    static {
	renderHints.put(RenderingHints.KEY_RENDERING,
		RenderingHints.VALUE_RENDER_QUALITY);
    };

    // use to stack views as added
    private static int LASTLEFT = 0;
    private static int LASTTOP = 0;
    private static final int DEL_H = 40;
    private static final int DEL_V = 20;

    // generate a UUID for this view
    private UUID _uuid = UUID.randomUUID();

    // properties object
    private Properties _properties = new Properties();

    // user provided viewtype
    private int _viewType;

    // The desktop owner of this view (internal frame.)
    private JDesktopPane _desktop;

    // This view's container, if there is one (some views, such as the log view,
    // do not have a container)
    private IContainer _container;

    // an additional optional panel that can be added to the east
    private JPanel _userPanel;

    // attributes from var args
    protected Attributes _attributes;

    // virtual view item, if used
    protected VirtualWindowItem _virtualItem;

    /**
     * Constructor
     * 
     * @param keyVals
     *            an optional variable length list of attributes in type-value
     *            pairs. For example, AttributeType.NAME, "my application",
     *            AttributeType.MAXIMIZABE, true, etc.
     */
    public BaseView(Object... keyVals) {

	_desktop = Desktop.getInstance();

	// setLayout(new BorderLayout());

	_attributes = new Attributes(keyVals);
	// get the recognized attributes
	String title = _attributes.stringValue(AttributeType.TITLE);

	// view decorations
	boolean standardDecorations = _attributes
		.booleanValue(AttributeType.STANDARDVIEWDECORATIONS);
	boolean iconifiable = _attributes
		.booleanValue(AttributeType.ICONIFIABLE);
	boolean maximizable = _attributes
		.booleanValue(AttributeType.MAXIMIZABLE);
	boolean resizable = _attributes.booleanValue(AttributeType.RESIZABLE);
	boolean closable = _attributes.booleanValue(AttributeType.CLOSABLE);

	// view visible
	boolean visible = _attributes.booleanValue(AttributeType.VISIBLE);

	// view type
	setViewType(_attributes.intValue(AttributeType.VIEWTYPE));

	// apply the attributes
	setTitle((title != null) ? title : "A View");
	setIconifiable(standardDecorations || iconifiable);
	setMaximizable(standardDecorations || maximizable);
	setResizable(standardDecorations || resizable);
	setClosable(standardDecorations || closable);
	setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);

	int left = _attributes.intValue(AttributeType.LEFT);
	int top = _attributes.intValue(AttributeType.TOP);
	int width = _attributes.intValue(AttributeType.WIDTH);
	int height = _attributes.intValue(AttributeType.HEIGHT);
	if (left < 1) {
	    left = LASTLEFT;
	    LASTLEFT += DEL_H;
	}
	if (top < 1) {
	    top = LASTTOP;
	    LASTTOP += DEL_V;
	}

	setFrameIcon(null);
	ViewManager.getInstance().add(this);

	// if the world system is not null, add a container
	Rectangle2D.Double worldSystem = _attributes
		.worldRectangleValue(AttributeType.WORLDSYSTEM);
	if (worldSystem != null) {
	    setLocation(left, top);

	    // container in attributes? if not, use a BaseContainer
	    _container = _attributes.containerValue(AttributeType.CONTAINER);
	    if (_container == null) {
		_container = new BaseContainer(this, worldSystem,
			_attributes.booleanValue(AttributeType.HEADSUP));
	    }

	    if (_container instanceof BaseContainer) {
		int lmargin = _attributes.intValue(AttributeType.LEFTMARGIN);
		int tmargin = _attributes.intValue(AttributeType.TOPMARGIN);
		int rmargin = _attributes.intValue(AttributeType.RIGHTMARGIN);
		int bmargin = _attributes.intValue(AttributeType.BOTTOMMARGIN);
		_container.setLeftMargin(lmargin);
		_container.setTopMargin(tmargin);
		_container.setRightMargin(rmargin);
		_container.setBottomMargin(bmargin);
	    }

	    // background color applies to the container
	    Color background = _attributes.colorValue(AttributeType.BACKGROUND);
	    if (background != null) {
		_container.getComponent().setBackground(background);
	    }

	    if ((width > 0) && (height > 0)) {
		_container.getComponent().setPreferredSize(
			new Dimension(width, height));
	    }

	    // split west component? (like a file tree)
	    JComponent westComponent = _attributes
		    .componentValue(AttributeType.SPLITWESTCOMPONENT);
	    if (westComponent != null) {
		JSplitPane splitPane = new JSplitPane(
			JSplitPane.HORIZONTAL_SPLIT, false, westComponent,
			_container.getComponent());
		splitPane.setResizeWeight(0.0);
		add(splitPane, BorderLayout.CENTER);
	    } else {
		add(_container.getComponent(), BorderLayout.CENTER);
	    }

	    // add a toolbar?
	    boolean addToolBar = _attributes
		    .booleanValue(AttributeType.TOOLBAR);
	    if (addToolBar) {
		int bits = _attributes.intValue(AttributeType.TOOLBARBITS);
		if (bits == Integer.MIN_VALUE) {
		    bits = BaseToolBar.EVERYTHING;
		} else { // normal
		    BaseToolBar toolBar = new BaseToolBar(_container, bits);
		    add(toolBar, BorderLayout.NORTH);
		}

	    } else {
		// hack: invis toolbar, pointer selected
		BaseToolBar toolBar = new BaseToolBar(_container, 1);
		// add(toolBar, BorderLayout.NORTH);
	    }

	    pack();
	} // end world system not null (added a container)
	else { // no container, e.g., log view
	    if ((width > 0) && (height > 0)) {
		setBounds(left, top, width, height);
	    } else {
		setLocation(left, top);
	    }
	}

	// add to the desktop
	if (_desktop != null) {

	    //keep virtual view on top?
//	    if (this instanceof VirtualView) {
//		_desktop.setLayer(this, 1);
//	    }

	    _desktop.add(this, 0);
	}

	if (visible) {
	    // now make the view visible, in the AWT thread
	    EventQueue.invokeLater(new Runnable() {

		@Override
		public void run() {
		    setVisible(true);
		}

	    });
	}

	// look for view resizes. It seems to be a hack, but at least for the
	// open maps
	// implementation I had to add this desktop repaint or when the view was
	// made
	// smaller it would leave garbage behind
	ComponentAdapter ca = new ComponentAdapter() {
	    @Override
	    public void componentResized(ComponentEvent ce) {
		// hard to believe is necessary but funny things happen without
		// it
		if (_desktop != null) {
		    _desktop.repaint();
		}
	    }

	    @Override
	    public void componentMoved(ComponentEvent ce) {
		Point p = getLocation();
		if (p.y < -9) {
		    p.y = -9;
		    setLocation(p);
		}
	    }
	};
	addComponentListener(ca);
    }

    /**
     * Checks whether this view is on top. This is needed for the HeadsUp
     * display, so that it only provides headsup on the active view.
     * 
     * @return <code>true</code> if this view (internal frame) is on top.
     */
    public boolean isOnTop() {
	// is it active?
	if (isSelected()) {
	    return true;
	}

	// is it showing?
	if (!isShowing()) {
	    return false;
	}

	JInternalFrame frames[] = _desktop.getAllFrames();

	for (int i = 0; i < frames.length; i++) {
	    if (frames[i].isShowing()) {
		return frames[i] == this;
	    }
	}

	return false;
    }

    /**
     * Get the view's layer and item container, which may be <code>null</code>.
     * 
     * @return the view's layer and item container, which may be
     *         <code>null</code>.
     */
    @SuppressWarnings("all")
    public IContainer getContainer() {
	return _container;
    }

    /**
     * Override getName to return the title of the view.
     * 
     * @return the title of the view as the name of the view.
     */
    @Override
    public String getName() {
	return getTitle();
    }

    /**
     * Get the user panel for adding more widgets
     * 
     * @return the userPanel
     */
    public JPanel getUserPanel() {
	return _userPanel;
    }

    /**
     * Called by a container when a right click is not handled. The usual reason
     * is that the right click was on an inert spot.
     * 
     * @param mouseEvent
     *            the causal event.
     */
    public void rightClicked(MouseEvent mouseEvent) {
    }

    /**
     * The control panel toggle button has been hit.
     */
    public void controlPanelButtonHit() {
	System.err.println("Control Panel Button Hit");
    }

    /**
     * Checks if the view is ready for display.
     * 
     * @return <code>true</code> if the view is ready for display.
     */
    public boolean isReady() {
	return true;
    }

    /**
     * Set the view arrangement from the properties
     * 
     * @param properties
     *            the properties to use
     */
    public void setFromProperties(Properties properties) {
	if (properties == null) {
	    return;
	}

	String name = getName();
	if (name == null) {
	    return;
	}

	name = name + ".";

	// try to get the bounds
	Rectangle viewRect = rectangleFromProperties(properties);
	if (viewRect != null) {
	    setBounds(viewRect);

	    // try to get world bounds and zoom
	    if (_container != null) {
		Rectangle2D.Double wr = worldRectangleFromProperties(properties);
		if (wr != null) {
		    _container.zoom(wr.getMinX(), wr.getMaxX(), wr.getMinY(),
			    wr.getMaxY());
		}
	    }
	} // viewRect != null

	boolean vis = getBoolean(name + "visible", properties, false);
	boolean ontop = getBoolean(name + "ontop", properties, false);
	boolean maximized = getBoolean(name + "maxmized", properties, false);
	setVisible(vis);

	if (ontop) {
	    toFront();
	}

	if (maximized) {
	    try {
		this.setMaximum(true);
	    } catch (PropertyVetoException e) {
		e.printStackTrace();
	    }
	}

    }

    // pull out a boolean property
    private boolean getBoolean(String key, Properties properties, boolean defVal) {
	String str = properties.getProperty(key);
	return str == null ? defVal : Boolean.parseBoolean(str);
    }

    /**
     * Get the properties for this view
     * 
     * @return the properties for this view
     */
    public Properties getProperties() {
	return _properties;
    }

    /**
     * Checks a boolean property. Returns <code>false</code> if the property is
     * not found
     * 
     * @param key
     *            the key matched to the boolean we are checking
     * @return <code>true</code> if the key is found and the value is "true"
     *         (case insensitive)
     */
    public boolean checkBooleanProperty(String key) {
	String s = _properties.getProperty(key);
	if (s == null) {
	    return false;
	}
	return s.equalsIgnoreCase("true");
    }

    /**
     * Set t a boolean property
     * 
     * @param key
     *            the key matched to the boolean we are setting
     * @param val
     *            the value to set
     */
    public void setBooleanProperty(String key, boolean val) {
	_properties.put(key, val ? "true" : "false");
    }

    /**
     * Obtain the properties of this view that can be used to save a
     * configuration.
     * 
     * @return the properties that define the configuration
     */
    public Properties getConfigurationProperties() {
	Properties properties = new Properties();
	String name = getName();

	if (name != null) {
	    name = name + ".";

	    properties.put(name + "visible", "" + isVisible());
	    properties.put(name + "closed", "" + isClosed());
	    properties.put(name + "icon", "" + isIcon());
	    properties.put(name + "ontop", "" + isOnTop());
	    properties.put(name + "maximized", "" + isMaximum());

	    Rectangle viewRect = getBounds();
	    properties.put(name + "x", "" + viewRect.x);
	    properties.put(name + "y", "" + viewRect.y);
	    properties.put(name + "width", "" + viewRect.width);
	    properties.put(name + "height", "" + viewRect.height);

	    if (_container != null) {
		Rectangle b = _container.getComponent().getBounds();
		b.x = 0;
		b.y = 0;
		Rectangle2D.Double wr = new Rectangle2D.Double();
		_container.localToWorld(b, wr);
		properties.put(name + "xmin",
			DoubleFormat.doubleFormat(wr.x, 8));
		properties.put(name + "ymin",
			DoubleFormat.doubleFormat(wr.y, 8));
		properties.put(name + "xmax",
			DoubleFormat.doubleFormat(wr.getMaxX(), 8));
		properties.put(name + "ymax",
			DoubleFormat.doubleFormat(wr.getMaxY(), 8));
	    }
	}

	return properties;
    }

    // get a world rect from the properties
    private Rectangle2D.Double worldRectangleFromProperties(
	    Properties properties) {

	String name = getName() + "."; // serves as prefix

	String xminStr = properties.getProperty(name + "xmin");
	if (xminStr != null) {
	    String yminStr = properties.getProperty(name + "ymin");
	    if (yminStr != null) {
		String xmaxStr = properties.getProperty(name + "xmax");
		if (xmaxStr != null) {
		    String ymaxStr = properties.getProperty(name + "ymax");
		    if (ymaxStr != null) {
			try {
			    double xmin = Double.parseDouble(xminStr);
			    double ymin = Double.parseDouble(yminStr);
			    double xmax = Double.parseDouble(xmaxStr);
			    double ymax = Double.parseDouble(ymaxStr);
			    return new Rectangle2D.Double(xmin, ymin, xmax
				    - xmin, ymax - ymin);
			} catch (Exception e) {
			    e.printStackTrace();
			}
		    }

		}
	    }
	} // xStr != null

	return null;
    }

    // get a pixel rect from the properties
    private Rectangle rectangleFromProperties(Properties properties) {

	String name = getName() + "."; // serves as prefix

	String xStr = properties.getProperty(name + "x");
	if (xStr != null) {
	    String yStr = properties.getProperty(name + "y");
	    if (yStr != null) {
		String wStr = properties.getProperty(name + "width");
		if (wStr != null) {
		    String hStr = properties.getProperty(name + "height");
		    if (hStr != null) {
			try {
			    int x = Integer.parseInt(xStr);
			    int y = Integer.parseInt(yStr);
			    int w = Integer.parseInt(wStr);
			    int h = Integer.parseInt(hStr);

			    if ((w > 2) && (h > 2)) {
				return new Rectangle(x, y, w, h);
			    }
			} catch (Exception e) {
			    e.printStackTrace();
			}
		    }

		}
	    }
	} // xStr != null

	return null;
    }

    /**
     * Get the UUID for this view
     * 
     * @return the uuid for this view.
     */
    public UUID getUuid() {
	return _uuid;
    }

    /**
     * Get the view type. This is not guaranteed to be meaningful or unique. It
     * is up to the application to set unique view types if it intends to use
     * them.
     * 
     * @return the viewType the application provided view type.
     */
    public int getViewType() {
	return _viewType;
    }

    /**
     * Set the view type. This is not guaranteed to be meaningful or unique. It
     * is up to the application to set unique view types if it intends to use
     * them.
     * 
     * @param viewType
     *            the viewType to set
     */
    public void setViewType(int viewType) {
	this._viewType = viewType;
    }

    /**
     * Get a view from its container
     * 
     * @param container
     *            the container
     * @return the view which holds this container
     */
    public static BaseView getViewFromContainer(IContainer container) {
	if (container == null) {
	    System.err.println("NULL container");
	    return null;
	}

	if (Desktop.getInstance() == null) {
	    return null;
	}

	JInternalFrame frames[] = Desktop.getInstance().getAllFrames();
	if (frames == null) {
	    return null;
	}
	if (frames.length == 0) {
	    return null;
	}

	for (JInternalFrame tf : frames) {
	    if (tf instanceof BaseView) {
		BaseView bv = (BaseView) tf;
		if (container == bv.getContainer()) {
		    return (BaseView) tf;
		}
	    }
	}
	return null;
    }

    /**
     * Get the virtual window item used on the virtual view
     * 
     * @return the virtual window item used on the virtual view
     */
    protected VirtualWindowItem getVirtualItem() {
	return _virtualItem;
    }

    /**
     * Set the virtual window item used on the virtual view
     * 
     * @param virtualItem
     *            the virtual window item used on the virtual view
     */
    protected void setVirtualItem(VirtualWindowItem virtualItem) {
	_virtualItem = virtualItem;
    }

    /**
     * Offset the view's location
     * 
     * @param dh
     *            the horizontal change
     * @param dv
     *            the vertical change
     */
    public void offset(int dh, int dv) {
	Rectangle b = getBounds();

	b.x += dh;
	b.y += dv;
	setBounds(b);
    }

    /**
     * Used if this view has some special clipping needs
     * 
     * @return a special clip, or <code>null</code> if no special clipping
     */
    public Shape getSpecialClip() {
	return null;
    }

    /**
     * Convenience method for getting the view's toolbar, if there is one.
     * 
     * @return the view's toolbar, or <code>null</code>.
     */
    public BaseToolBar getToolBar() {
	if (getContainer() != null) {
	    return getContainer().getToolBar();
	}
	return null;
    }

    /**
     * Convenience method for getting the user component on the view's toolbar,
     * if there is one.
     * 
     * @return the the user component on the view's toolbar, or
     *         <code>null</code>.
     */
    public UserToolBarComponent getUserComponent() {
	if (getToolBar() != null) {
	    return getToolBar().getUserComponent();
	}
	return null;
    }
    
    /**
     * Handle a mgnificaction
     */
    public void handleMagnify(MouseEvent me) {
	MagnifyWindow.magnify(this, me, null);
    }

}
