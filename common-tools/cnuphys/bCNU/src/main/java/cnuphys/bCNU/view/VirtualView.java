package cnuphys.bCNU.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import cnuphys.bCNU.drawable.DrawableAdapter;
import cnuphys.bCNU.drawable.IDrawable;
import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.item.AItem;
import cnuphys.bCNU.util.Environment;
import cnuphys.bCNU.util.PropertySupport;
import cnuphys.bCNU.util.X11Colors;

public class VirtualView extends BaseView
		implements InternalFrameListener, IViewListener, MouseMotionListener, MouseListener {

	private JFrame _parent;

	private static final String VVTITLE = "Desktop";

	private Vector<BaseView> _views = new Vector<BaseView>();

	private static int _numcol = 8;

	private int _currentCol = 0;
	private Point _offsets[] = new Point[_numcol];

	// private static final Color _bg = X11Colors.getX11Color("dark blue");
	private static final Color _bg = Color.gray;
	private static final Color _fill = X11Colors.getX11Color("alice blue");
	private static final Color _vwfillInactive = new Color(255, 200, 120, 128);
	private static final Color _vwfillActive = new Color(0, 0, 200, 128);

	private Point2D.Double _wp = new Point2D.Double();

	// constraint
	public static final int UPPERLEFT = 0;
	public static final int UPPERRIGHT = 1;
	public static final int BOTTOMLEFT = 2;
	public static final int BOTTOMRIGHT = 3;
	public static final int TOPCENTER = 4;
	public static final int BOTTOMCENTER = 5;
	public static final int CENTER = 6;
	public static final int CENTERLEFT = 7;
	public static final int CENTERRIGHT = 8;

	// for public access
	private static VirtualView _instance;

	/**
	 * Create a virtual view view
	 * 
	 * @param keyVals
	 *            variable set of arguments.
	 */
	private VirtualView(Object... keyVals) {
		super(keyVals);
		ViewManager.getInstance().addViewListener(this);

		_parent = (JFrame) SwingUtilities.getWindowAncestor(this);
		ComponentAdapter ca = new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent ce) {
				reconfigure();
			}
		};
		_parent.addComponentListener(ca);

		addItems();

		setBackground(_bg);
		getContainer().getComponent().setBackground(_bg);
		
		
		getContainer().getComponent().addMouseMotionListener(this);
		getContainer().getComponent().addMouseListener(this);

		// set the offsets
		setOffsets();

		// System.err.println("[VV] world: " + getContainer().getWorldSystem());
		setBeforeDraw();
		setAfterDraw();

		_instance = this;
	}

	/**
	 * Public access to the virtual view.
	 * 
	 * @return the virtual view.
	 */
	public static VirtualView getInstance() {
		return _instance;
	}

	/**
	 * Reconfigure the virtual view
	 */
	public void reconfigure() {
		Dimension d = _parent.getSize();

		int width = _numcol * d.width;
		int height = d.height;
		getContainer().getWorldSystem().width = width;
		getContainer().getWorldSystem().height = height;
		setOffsets();

		for (BaseView view : _views) {
			if (view.getVirtualItem() != null) {
				view.getVirtualItem().setLocation();
			}
		}

		getContainer().refresh();
	}

	private void setOffsets() {
		Rectangle2D.Double world = getContainer().getWorldSystem();
		double dx = world.width / _numcol;
		double dy = world.height;

		for (int col = 0; col < _numcol; col++) {

			_offsets[col] = new Point((int) (col * dx), (int) (dy));
		}
	}

	/**
	 * Create the view's before drawer.
	 */
	private void setBeforeDraw() {
		// use a before-drawer to sector dividers and labels
		IDrawable beforeDraw = new DrawableAdapter() {

			@Override
			public void draw(Graphics g, IContainer container) {
				Rectangle cr = getColRect(_currentCol);
				g.setColor(_fill);
				g.fillRect(cr.x + 1, cr.y + 1, cr.width - 1, cr.height - 1);

			}

		};

		getContainer().setBeforeDraw(beforeDraw);
	}

	/**
	 * Create the view's before drawer.
	 */
	private void setAfterDraw() {
		// use a before-drawer to sector dividers and labels
		IDrawable afterDraw = new DrawableAdapter() {

			@Override
			public void draw(Graphics g, IContainer container) {

				Rectangle b = container.getComponent().getBounds();
				Rectangle2D.Double world = getContainer().getWorldSystem();
				Point2D.Double wp = new Point2D.Double();
				Point pp = new Point();
				double dx = world.width / _numcol;

				g.setColor(Color.red);
				wp.y = world.y + world.height / 2;
				for (int i = 1; i < _numcol; i++) {
					wp.x = i * dx;
					container.worldToLocal(pp, wp);
					g.drawLine(pp.x, 0, pp.x, b.height);
				}

				double dy = world.height;
				wp.x = world.x + world.width / 2;

				Rectangle cr = getColRect(_currentCol);

				g.setColor(Color.red);
				g.drawRect(0, 0, b.width - 1, b.height - 1);

				g.setColor(Color.green);
				g.drawRect(cr.x, cr.y, cr.width - 1, cr.height - 1);

			}

		};

		getContainer().setAfterDraw(afterDraw);
	}

	/**
	 * This adds the screen items.
	 */
	private void addItems() {
		// add items for all views except me
		for (BaseView view : ViewManager.getInstance()) {
			addView(view);
		}
	}

	/**
	 * Convenience method for creating a Drawing View.
	 * 
	 * @return a new DrawingView object
	 */
	public static VirtualView createVirtualView(int numcol) {

		_numcol = numcol;
		VirtualView view = null;
		Rectangle2D.Double world = getWorld();
		
		int cell_width = 40;
		int cell_height = 1 + ((9*cell_width)/16);
		int width = numcol * cell_width;
//		int height = (int) ((width * world.height) / world.width);
		int height = cell_height;
		
		if (Environment.getInstance().isLinux()) {
			height += 23;
		}
		if (Environment.getInstance().isWindows()) {
			height += 23;
		}


		// create the view
		view = new VirtualView(PropertySupport.WORLDSYSTEM, world, PropertySupport.LEFT, 0, PropertySupport.TOP, 0,
				PropertySupport.WIDTH, width, PropertySupport.HEIGHT, height, PropertySupport.TOOLBAR, false,
				PropertySupport.VISIBLE, true, PropertySupport.BACKGROUND, Color.white,
				PropertySupport.TITLE, VVTITLE, PropertySupport.STANDARDVIEWDECORATIONS, false,
				PropertySupport.ICONIFIABLE, false, PropertySupport.RESIZABLE, true, 
				PropertySupport.MAXIMIZABLE, false, PropertySupport.CLOSABLE, false);

		view._offsets = new Point[_numcol];
		//view.pack();
		
		
		Insets insets = view.getInsets();
		view.setSize(width, height + insets.top);
		return view;
	}
		

	/**
	 * Get the number of columns
	 * 
	 * @return the number of columns
	 */
	public int getNumCol() {
		return _numcol;
	}

	// get the world system
	private static Rectangle2D.Double getWorld() {
		// System.err.println("VV getting world");
		GraphicsEnvironment g = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] devices = g.getScreenDevices();

		int maxw = 0;
		int maxh = 0;

		for (int i = 0; i < devices.length; i++) {
			maxw = Math.max(maxw, devices[i].getDisplayMode().getWidth());
			maxh = Math.max(maxh, devices[i].getDisplayMode().getHeight());
		}

		int width = _numcol * maxw;
		int height = maxh;
		return new Rectangle2D.Double(0, 0, width, height);
	}

	@Override
	public void internalFrameOpened(InternalFrameEvent e) {
		Object source = e.getSource();
		if (source instanceof BaseView) {
			BaseView view = (BaseView) source;
			// System.err.println("[VV] " + view.getTitle() + " opened");
			if (view.getVirtualItem() != null) {
				view.getVirtualItem().setLocation();
				view.getVirtualItem().setVisible(true);
				getContainer().refresh();
			}
		}
	}

	@Override
	public void internalFrameClosing(InternalFrameEvent e) {
	}

	@Override
	public void internalFrameClosed(InternalFrameEvent e) {
		Object source = e.getSource();
		if (source instanceof BaseView) {
			BaseView view = (BaseView) source;
			view.getVirtualItem().setVisible(false);
			getContainer().refresh();

		}
	}

	@Override
	public void internalFrameIconified(InternalFrameEvent e) {
		Object source = e.getSource();
		if (source instanceof BaseView) {
			BaseView view = (BaseView) source;
			view.getVirtualItem().setVisible(false);
			getContainer().refresh();
			// System.err.println("[VV] " + view.getTitle() + " iconified");
		}
	}

	@Override
	public void internalFrameDeiconified(InternalFrameEvent e) {
		Object source = e.getSource();
		if (source instanceof BaseView) {
			BaseView view = (BaseView) source;
			view.getVirtualItem().setVisible(true);
			getContainer().refresh();

			// System.err.println("[VV] " + view.getTitle() + " deiconified");
		}
	}

	@Override
	public void internalFrameActivated(InternalFrameEvent e) {
		Object source = e.getSource();
		if (source instanceof BaseView) {
			BaseView view = (BaseView) source;
			view.getVirtualItem().setVisible(true);
			getContainer().getAnnotationLayer().sendToFront(view.getVirtualItem());
			view.getVirtualItem().getStyle().setFillColor(_vwfillActive);
			view.getVirtualItem().getStyle().setLineColor(Color.white);
			getContainer().refresh();
		}
	}

	@Override
	public void internalFrameDeactivated(InternalFrameEvent e) {
		Object source = e.getSource();
		if (source instanceof BaseView) {
			BaseView view = (BaseView) source;
			view.getVirtualItem().setVisible(true);
			view.getVirtualItem().getStyle().setFillColor(_vwfillInactive);
			view.getVirtualItem().getStyle().setLineColor(Color.blue);
			getContainer().refresh();
		}
	}

	@Override
	public void viewAdded(BaseView view) {
		addView(view);
		getContainer().refresh();
	}

	@Override
	public void viewRemoved(BaseView view) {
		if (_views.contains(view)) {
			view.removeInternalFrameListener(this);

			if (view.getVirtualItem() != null) {
				getContainer().getAnnotationLayer().remove(view.getVirtualItem());
				view.getContainer().refresh();
			}

			_views.remove(view);
		}
	}

	// add a view and create the item that represents it
	private void addView(BaseView view) {
		// don't add myself
		if (view == this) {
			return;
		}

		if (_views.contains(view)) {
			return;
		}

		_views.add(view);

		final VirtualWindowItem vitem = new VirtualWindowItem(this, view);
		vitem.getStyle().setFillColor(_vwfillInactive);
		vitem.getStyle().setLineColor(Color.blue);

		view.addInternalFrameListener(this);

		ComponentListener cl = new ComponentListener() {

			@Override
			public void componentResized(ComponentEvent e) {
				vitem.setLocation();
				getContainer().refresh();
			}

			@Override
			public void componentMoved(ComponentEvent e) {
				vitem.setLocation();
				getContainer().refresh();
			}

			@Override
			public void componentShown(ComponentEvent e) {
				vitem.setLocation();
				getContainer().refresh();
			}

			@Override
			public void componentHidden(ComponentEvent e) {
				vitem.setLocation();
				getContainer().refresh();
			}

		};

		view.addComponentListener(cl);

		getContainer().refresh();
	}

	@Override
	public void mouseDragged(MouseEvent e) {
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		getContainer().localToWorld(e.getPoint(), _wp);

		AItem item = getContainer().getItemAtPoint(e.getPoint());
		if ((item != null) && (item instanceof VirtualWindowItem)) {
			VirtualWindowItem vvi = (VirtualWindowItem) item;
			setTitle(VVTITLE + " [" + vvi.getBaseView().getTitle() + "]");
		} else {
			setTitle(VVTITLE);
		}
	}

	/**
	 * Virtual view: no offesetting!
	 * 
	 * @param dh
	 *            the horizontal change
	 * @param dv
	 *            the vertical change
	 */
	@Override
	public void offset(int dh, int dv) {
	}

	@Override
	public void mouseClicked(MouseEvent mouseEvent) {
//		System.err.println("HEY MAN");
		switch (mouseEvent.getButton()) {
		case MouseEvent.BUTTON1:
			if (mouseEvent.getClickCount() == 1) { // single click
			} else { // double (or more) clicks
				handleDoubleClick(mouseEvent);
			}
			return;

		case MouseEvent.BUTTON3:
			return;
		}
	}

	// handle a double click
	private void handleDoubleClick(MouseEvent mouseEvent) {
		Point rc = getRowCol(mouseEvent.getPoint());
//		 System.err.println("Double clicked on: " + rc.y + ", " + rc.x);

		int clickCol = rc.x;
		if ((clickCol == _currentCol)) {
			return;
		}

		int dh = _offsets[_currentCol].x - _offsets[clickCol].x;

		// can't do dv because can't give internal frames -y
		int dv = 0;

		for (BaseView view : _views) {
			view.offset(dh, dv);
		}

		_currentCol = clickCol;
		getContainer().refresh();
//		reportVisibility();
	}

	private Rectangle getColRect(int col) {

		Rectangle2D.Double world = getContainer().getWorldSystem();
		double dx = world.width / _numcol;
		double dy = world.height;

		double x = col * dx;
		double y = world.y + world.height - dy;

		Rectangle2D.Double wr = new Rectangle2D.Double(x, y, dx, dy);
		Rectangle r = new Rectangle();
		getContainer().worldToLocal(r, wr);
		return r;
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
		setTitle(VVTITLE);
	}

	// col is in x
	// row = 0 is in y
	private Point getRowCol(Point p) {

		Point2D.Double wp = new Point2D.Double();

		getContainer().localToWorld(p, wp);

		Rectangle2D.Double world = getContainer().getWorldSystem();
		double dx = world.width / _numcol;

		int col = (int) (wp.x / dx);

		return new Point(col, 0);

	}

	/**
	 * Total offset based on the current cell
	 * 
	 * @return dh in x, dv in y
	 */
	public Point2D.Double totalOffset() {
		Rectangle2D.Double world = getContainer().getWorldSystem();
		double dx = world.width / _numcol;
		return new Point2D.Double(_currentCol * dx, 0);
	}

	/**
	 * Move a view to the center of a specific virtual cell
	 * 
	 * @param view
	 *            the view to move
	 * @param col
	 *            the col
	 */
	public void moveTo(BaseView view, int col) {
		if (view == null) {
			return;
		}

		moveTo(view, col, 0, 0);
	}
	
	/**
	 * Move a view to a specific virtual cell
	 * 
	 * @param view
	 *            the view to move
	 * @param col
	 *            the col
	 * @param dh
	 *            additional horizontal offset
	 * @param dv
	 *            additional vertical offset
	 */
	public void moveToStart(BaseView view, int col, int constraint) {
		Point start = view.getStartingLocation();
		moveTo(view, col, start.x, start.y, constraint);
	}
	

	/**
	 * Move a view to a specific virtual cell
	 * 
	 * @param view
	 *            the view to move
	 * @param col
	 *            the col
	 * @param dh
	 *            additional horizontal offset
	 * @param dv
	 *            additional vertical offset
	 */
	public void moveTo(BaseView view, int col, int dh, int dv) {

		if (view == null) {
			return;
		}

		col = Math.max(0, Math.min(col, (_numcol - 1)));

		boolean managed = false;

		for (BaseView bv : _views) {
			if (bv == view) {
				managed = true;
				break;
			}
		}

		if (!managed) {
			return;
		}

		Rectangle2D.Double world = getContainer().getWorldSystem();
		double dx = world.width / _numcol;
		double dy = world.height;

		double x = (col - _currentCol) * dx;
		double y = world.y + world.height - dy;

		int xc = (int) (x + dx / 2);
		int yc = (int) (y + dy / 2);

		Rectangle bounds = view.getBounds();
		int delx = xc - (bounds.x + bounds.width / 2);
		int dely = (yc - 40) - (bounds.y + bounds.height / 2);

		view.offset(delx + dh, dely + dv);
	}

	/**
	 * Move a view to a specific virtual cell
	 * 
	 * @param view
	 *            the view to move
	 * @param col
	 *            the col
	 */
	public void moveTo(BaseView view, int col, boolean fit) {

		if (view == null) {
			return;
		}

		if (fit == false) {
			moveTo(view, col, 0, 0);
			return;
		}

		col = Math.max(0, Math.min(col, (_numcol - 1)));

		boolean managed = false;

		for (BaseView bv : _views) {
			if (bv == view) {
				managed = true;
				break;
			}
		}

		if (!managed) {
			return;
		}

		Rectangle2D.Double world = getContainer().getWorldSystem();
		double dx = world.width / _numcol;
		double dy = world.height;

		double x = (col - _currentCol) * dx;

		int margin = 40;

		int left = (int) (x + margin);
		int top = (margin);
		int w = (int) (dx - 3 * margin);
		int h = (int) (dy - 3 * margin);
		Rectangle bounds = view.getBounds();
		bounds.setFrame(left, top, w, h);
		view.setBounds(bounds);
	}

	/**
	 * Move a view to a specific virtual cell
	 * 
	 * @param view
	 *            the view to move
	 * @param col
	 *            the col
	 * @param constraint
	 *            constraint constant
	 */
	public void moveTo(BaseView view, int col, int constraint) {
		moveTo(view, col, 0, 0, constraint);

	}
	
	/**
	 * Move a view to a specific virtual cell
	 * 
	 * @param view
	 *            the view to move
	 * @param col
	 *            the col
	 * @param delh
	 *            additional horizontal offset
	 * @param delv
	 *            additional vertical offset
	 * @param constraint
	 *            constraint constant
	 */
	public void moveTo(BaseView view, int col, int delh, int delv, int constraint) {

		if (constraint == CENTER) {
			moveTo(view, col);
			return;
		}

		col = Math.max(0, Math.min(col, (_numcol - 1)));

		boolean managed = false;

		for (BaseView bv : _views) {
			if (bv == view) {
				managed = true;
				break;
			}
		}

		if (!managed) {
			return;
		}

		Rectangle2D.Double world = getContainer().getWorldSystem();
		double dx = world.width / _numcol;
		double dy = world.height;

		double left = (col - _currentCol) * dx;
		double top = world.y + world.height - dy;
		double right = left + dx;
		double bottom = top + dy;

		Rectangle bounds = view.getBounds();
		int x0 = bounds.x;
		int y0 = bounds.y;
		int dh = 0;
		int dv = 0;

		int slop = 10;

		if (constraint == UPPERRIGHT) {
			int xf = (int) (right - bounds.width - 2 * slop);
			int yf = (int) (top + slop);
			dh = xf - x0;
			dv = yf - y0;
		} else if (constraint == UPPERLEFT) {
			int xf = (int) (left + slop);
			int yf = (int) (top + slop);
			dh = xf - x0;
			dv = yf - y0;
		} else if (constraint == BOTTOMLEFT) {
			int xf = (int) (left + slop);
			int yf = (int) (bottom - bounds.height - 7 * slop);
			dh = xf - x0;
			dv = yf - y0;
		} else if (constraint == BOTTOMRIGHT) {
			int xf = (int) (right - bounds.width - 2 * slop);
			int yf = (int) (bottom - bounds.height - 7 * slop);
			dh = xf - x0;
			dv = yf - y0;
		} else if (constraint == TOPCENTER) {
			int xf = (int) (left + right - bounds.width - slop) / 2;
			int yf = (int) (top + slop);
			dh = xf - x0;
			dv = yf - y0;
		} else if (constraint == BOTTOMCENTER) {
			int xf = (int) (left + right - bounds.width - slop) / 2;
			int yf = (int) (bottom - bounds.height - 7 * slop);
			dh = xf - x0;
			dv = yf - y0;
		}
		else if (constraint == CENTERLEFT) {
			int xf = (int) (left + slop);
			dh = xf - x0;
			System.err.println("CENTERLEFT DV, DELV = " + dv + "," + delv);
		}
		else if (constraint == CENTERRIGHT) {
			int xf = (int) (right - bounds.width - 2 * slop);
			dh = xf - x0;
			System.err.println("CENTERRIGHT DV, DELV = " + dv + "," + delv);
		}

		view.offset(dh + delh, dv + delv);
	}


	/**
	 * Activates the view's cell so that it is visible
	 * 
	 * @param view
	 *            the view
	 */
	public void activateViewCell(BaseView view) {

		if (view.getVirtualItem() == null) {
			return;
		}

		Rectangle b = view.getVirtualItem().getBounds(getContainer());
		Point pp = new Point(b.x + b.width / 2, b.y + b.height / 2);
		Point rc = getRowCol(pp);

		int col = Math.max(0, Math.min(_numcol - 1, rc.x));

		if (col == _currentCol) {
			return;
		}

		int dh = _offsets[_currentCol].x - _offsets[col].x;

		// can't do dv because can't give internal frames -y
		int dv = 0;

		for (BaseView bview : _views) {
			bview.offset(dh, dv);
		}

		_currentCol = col;
		getContainer().refresh();

	}

	/**
	 * Is a given view visible (crue test)
	 * 
	 * @param view
	 *            the view to check
	 * @return <code>true</code> if the view appears to be visible.
	 */
	public boolean isViewVisible(BaseView view) {
		if (view == null) {
			return false;
		}
		if (view.isIcon()) {
			return false;
		}

		Rectangle b = view.getBounds();
		Dimension d = _parent.getSize();
		Rectangle c = new Rectangle(0, 0, d.width, d.height);
		return b.intersects(c);
	}

//	public void reportVisibility() {
//		System.err.println("-------------");
//		for (BaseView view : _views) {
//			System.err.println("View " + view.getTitle() + " VIS: " + isViewVisible(view));
//		}
//	}

}
