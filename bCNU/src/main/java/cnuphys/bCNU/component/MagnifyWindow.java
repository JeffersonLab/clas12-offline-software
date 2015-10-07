package cnuphys.bCNU.component;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
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
import javax.swing.SwingUtilities;

import cnuphys.bCNU.drawable.IDrawable;
import cnuphys.bCNU.graphics.GraphicsUtilities;
import cnuphys.bCNU.graphics.container.BaseContainer;
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
	
	private static Point _oldMouseLocation = new Point(Integer.MAX_VALUE, Integer.MAX_VALUE);

	// the drawing component
	private static JComponent _content;

	//for offscreen drawing
	private static BufferedImage _offscreenBuffer;
		
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
		BaseContainer container = (BaseContainer)(_view.getContainer());
		if (!(container instanceof BaseContainer)) {
			return;
		}

		
		Rectangle2D.Double saveWorld = container.getWorldSystem();
		Rectangle2D.Double wr = new Rectangle2D.Double(saveWorld.x, saveWorld.y, saveWorld.width, saveWorld.height);
		
		Point pp = new Point(_mouseLocation.x, _mouseLocation.y);
		Point.Double wp = new Point.Double();
		container.localToWorld(pp, wp);
		
		Rectangle b = container.getComponent().getBounds();
		
		double ww = saveWorld.width/_MAGFACTOR;
		double hh = saveWorld.height/_MAGFACTOR;
		
		wr.setFrame(wp.x-ww/2, wp.y-hh/2, ww, hh);

		synchronized(container.getWorldSystem()) {
		container.setWorldSystem(wr);
		container.setDirty(true);
		
		Component component = container.getComponent();
		Dimension size = component.getSize();
		
		if ((_offscreenBuffer == null) || 
				(_offscreenBuffer.getWidth(this) < size.width) ||
				(_offscreenBuffer.getHeight(this) < size.height)) {
			 _offscreenBuffer = GraphicsUtilities.getComponentImageBuffer(component);
		}
		else {
			System.err.println("reusing buffer");
		}
		
		if (_offscreenBuffer != null) {
			int sx = (b.x + b.width - _WIDTH) / 2;
			int sy = (b.y + b.height - _HEIGHT) / 2;
			GraphicsUtilities.paintComponentOnImage(component, _offscreenBuffer);
			g.drawImage(_offscreenBuffer, 0, 0, _WIDTH, _HEIGHT, sx, sy, sx + _WIDTH, sy + _HEIGHT, component);
		}
		else {
			System.err.println("NULL OFFSCREEN BUFFER!");
		}

		container.setDirty(true);
		container.setWorldSystem(saveWorld);
		}
//		container.refresh();
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
		
		_mouseLocation = new Point(me.getPoint());

		if (!mouseChangedEnough()) {
			return;
		}
		
		
		// get the screen mouse location
		Point p = MouseInfo.getPointerInfo().getLocation();

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
		_content.repaint();
	}
	
	private static boolean mouseChangedEnough() {
		
		int x = _mouseLocation.x;
		int y = _mouseLocation.y;
		int ox = _oldMouseLocation.x;
		int oy = _oldMouseLocation.y;
		
		if ((Math.abs(x-ox) > 0) || (Math.abs(y-oy) > 0)) {
			_oldMouseLocation.setLocation(_mouseLocation);
			return true;
		}
		
		System.err.println("Mouse didn't move enough");
		return false;
	}


	/**
	 * Hide the magnify window
	 */
	public static void closeMagnifyWindow() {
		System.err.println("CLOSE MAG WINDOW");
		if ((_magnifyWindow != null) && (_magnifyWindow.isVisible())) {
			_magnifyWindow.setVisible(false);
			_offscreenBuffer = null;
		}
	}

	@Override
	public Insets getInsets() {
		Insets def = super.getInsets();
		return new Insets(def.top + 2, def.left + 2, def.bottom + 2, def.right + 2);
	}

}
