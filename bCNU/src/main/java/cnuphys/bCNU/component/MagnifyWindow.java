package cnuphys.bCNU.component;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;
import javax.swing.JWindow;

import cnuphys.bCNU.drawable.IDrawable;
import cnuphys.bCNU.graphics.GraphicsUtilities;
import cnuphys.bCNU.graphics.container.BaseContainer;
import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.log.Log;
import cnuphys.bCNU.util.X11Colors;
import cnuphys.bCNU.view.BaseView;

public class MagnifyWindow extends JWindow {

	private static MagnifyWindow _magnifyWindow;

	// size
	private static int _WIDTH = 200;
	private static int _HEIGHT = 200;

	private static int OFFSET = 10; // from pointer location

	// mag factor
	private static double _MAGFACTOR = 3.;

	private static final Color _defaultBackground = X11Colors.getX11Color("Alice Blue");

	// the view being magnified
	private static BaseView _view;

	// mouse location relative to container
	private static Point _mouseLocation;

	// the drawing component
	private static JComponent _content;

	// for offscreen drawing
	private BufferedImage _offscreenBuffer;

	/**
	 * Create a translucent window
	 */
	private MagnifyWindow() {
		setBackground(_defaultBackground);
		setLayout(new BorderLayout(0, 0));
		setSize(_WIDTH, _HEIGHT);

		// create the drawing component
		_content = new JComponent() {
			@Override
			public void paintComponent(Graphics g) {
				draw(g);
			}
		};

		add(_content, BorderLayout.CENTER);
	}

	// draw the content
	private synchronized void draw(Graphics g) {
		// Rectangle b = _content.getBounds();
		IContainer container = _view.getContainer();
		if (!(container instanceof BaseContainer)) {
			return;
		}

		// g.setColor(container.getComponent().getBackground());
		// g.fillRect(0, 0, _WIDTH, _HEIGHT);

		Rectangle2D.Double wr = magWorld(container);
		System.err.println("MAG WORLD: " + wr + " xc: " + wr.getCenterX() + " yc: " + wr.getCenterY());

		Rectangle2D.Double saveWorld = container.getWorldSystem();

		container.setWorldSystem(wr);
		container.setDirty(true);

		getImage((BaseContainer) container);
		if (_offscreenBuffer != null) {
			g.drawImage(_offscreenBuffer, 0, 0, this);
		}

		// ((BaseContainer) container).paintComponent(g);

		container.setDirty(true);
		container.setWorldSystem(saveWorld);
	}

	private void getImage(BaseContainer container) {
		// if null, create sized to current component size

		if (_offscreenBuffer == null) {
			_offscreenBuffer = GraphicsUtilities.getComponentImageBuffer(_content);
		}

		// if dirty, paint on it
		if (_offscreenBuffer != null) {
			GraphicsUtilities.paintComponentOnImage(container.getComponent(), _offscreenBuffer);
		}
	}

	// get the appropriate world for the zoom
	private Rectangle2D.Double magWorld(IContainer container) {
		Rectangle r = new Rectangle();
		Rectangle b = container.getComponent().getBounds();
		System.err.println("BOUNDS: " + b);
		int xc = _mouseLocation.x;
		int yc = _mouseLocation.y;
		
		System.err.println("Mouse Location: " + _mouseLocation);

		int w = (int) (_WIDTH / _MAGFACTOR);
		int h = (int) (_HEIGHT / _MAGFACTOR);

		r.setBounds(xc - w / 2, yc - h / 2, w, h);
		
		System.err.println("RECT: " + r);

		Rectangle2D.Double wr = new Rectangle2D.Double();
		container.localToWorld(r, wr);
		return wr;
	}

	/**
	 * Magnify a view
	 * 
	 * @param view
	 *            the view to magnify
	 * @param me
	 *            the mouse event which contains the location
	 */
	public static synchronized void magnify(BaseView view, MouseEvent me, IDrawable drawable) {
		if (_magnifyWindow == null) {
			_magnifyWindow = new MagnifyWindow();
		}
		// get the screen mouse location
		Point p = MouseInfo.getPointerInfo().getLocation();
		System.err.println("Handle magnification at " + p);

		// where to place magnify window
		int screenX = p.x;
		int screenY = p.y;

		int x = screenX - _WIDTH - OFFSET;
		if (x < 0) {
			x = screenX + OFFSET;
		}
		int y = screenY - _HEIGHT - OFFSET;
		if (y < 0) {
			y = screenY + OFFSET;
		}

		_magnifyWindow.setLocation(x, y);

		_magnifyWindow.setVisible(true);
		_magnifyWindow.toFront();

		_view = view;
		_mouseLocation = new Point(me.getPoint());
		_content.repaint();
	}

	/**
	 * Hide the magnify window
	 */
	public static void closeMagnifyWindow() {
		System.err.println("CLOSE MAG WINDOW");
		if ((_magnifyWindow != null) && (_magnifyWindow.isVisible())) {
			_magnifyWindow.setVisible(false);
		}
	}

	@Override
	public Insets getInsets() {
		Insets def = super.getInsets();
		return new Insets(def.top + 2, def.left + 2, def.bottom + 2, def.right + 2);
	}

}
