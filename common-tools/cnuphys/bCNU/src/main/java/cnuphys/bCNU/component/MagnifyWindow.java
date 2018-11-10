package cnuphys.bCNU.component;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JWindow;

import cnuphys.bCNU.drawable.DrawableAdapter;
import cnuphys.bCNU.drawable.IDrawable;
import cnuphys.bCNU.graphics.container.BaseContainer;
import cnuphys.bCNU.graphics.container.IContainer;

public class MagnifyWindow extends JWindow {

	private static MagnifyWindow _magnifyWindow;

	// size of the magnify window
	private static int _WIDTH = 204;
	private static int _HEIGHT = 204;

	private static int OFFSET = 10; // from pointer location

	// mouse location relative to container
	private static Point _mouseLocation;

	private static Point.Double _worldCenter = new Point.Double();

	// the drawing container
	private static BaseContainer _container;

	private static IDrawable _extraAfterDraw;

	private static IDrawable _customDraw;

	// menu mag selector for options menu
	private static JMenu _magMenu;

	private static final Integer[] _mags = { 2, 3, 4, 5, 6, 7, 8, 9, 10 };
	private static int _selectedMag = 4;

	/**
	 * Create a translucent window
	 */
	@SuppressWarnings("serial")
	private MagnifyWindow() {
		setLayout(new BorderLayout(0, 0));
		setSize(_WIDTH, _HEIGHT);

		_container = new BaseContainer(new Rectangle.Double(0, 0, 1, 1)) {
			@Override
			public void paintComponent(Graphics g) {
				if (_customDraw == null) {
					super.paintComponent(g);
				}
				else {
	//				System.err.println("Custom draw");
					_customDraw.draw(g, _container);
				}
			}
		};

		add(_container, BorderLayout.CENTER);
	}

	/**
	 * The mag menu to put on the main menu
	 * 
	 * @return the mag menu
	 */
	public static JMenu magificationMenu() {

		if (_magMenu != null) {
			return _magMenu;
		}

		_magMenu = new JMenu("Magnification Factor");
		ButtonGroup bga = new ButtonGroup();

		ActionListener al = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				_selectedMag = Integer.parseInt(e.getActionCommand());
				// System.err.println("New mag selection " + _selectedMag);
			}

		};

		for (int i = 0; i < _mags.length; i++) {
			JRadioButtonMenuItem mitem = new JRadioButtonMenuItem(_mags[i].toString(), _selectedMag == _mags[i]);
			bga.add(mitem);
			mitem.addActionListener(al);
			_magMenu.add(mitem);
		}

		return _magMenu;
	}

	/**
	 * Magnify a view
	 * 
	 * @param sContainer
	 *            the container to magnify
	 * @param me
	 *            the mouse event which contains the location
	 */
	public static synchronized void magnify(final BaseContainer sContainer, MouseEvent me) {
		if (_magnifyWindow == null) {
			_magnifyWindow = new MagnifyWindow();
		}

		// do we have an optional drawer instead of simple magnification?
		_customDraw = sContainer.getMagnificationDraw();

		_mouseLocation = new Point(me.getPoint());

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

		_container.setWorldSystem(getMagWorld(sContainer));

		// share items or specialized draw?
		if (_customDraw == null) {
			_container.shareModel(sContainer);

			final IDrawable parentAD = sContainer.getAfterDraw();

			_extraAfterDraw = new DrawableAdapter() {

				@Override
				public void draw(Graphics g, IContainer container) {
					if (parentAD != null) {
						parentAD.draw(g, _container);
					}

					// draw the cross hair
					Rectangle bounds = container.getComponent().getBounds();
					int xc = bounds.x + bounds.width / 2;
					int yc = bounds.y + bounds.height / 2;

					int S2 = 8;
					g.setColor(Color.cyan);
					g.drawLine(xc - S2, yc - 1, xc - 1, yc - 1);
					g.drawLine(xc - 1, yc - S2, xc - 1, yc - 1);
					g.drawLine(xc + S2, yc + 1, xc + 1, yc + 1);
					g.drawLine(xc + 1, yc + S2, xc + 1, yc + 1);
					g.setColor(Color.red);
					g.drawLine(xc - S2, yc, xc + S2, yc);
					g.drawLine(xc, yc - S2, xc, yc + S2);
				}
			};

			_container.setAfterDraw(_extraAfterDraw);
		}
		_container.setDirty(true);
		_container.refresh();

	}

	// get the world for the mag container

	private static Rectangle2D.Double getMagWorld(BaseContainer sContainer) {

		Rectangle bounds = _container.getBounds();
		Rectangle sBounds = sContainer.getBounds();

		Rectangle2D.Double sWorld = sContainer.getWorldSystem();
		Rectangle2D.Double wr = new Rectangle2D.Double(sWorld.x, sWorld.y, sWorld.width, sWorld.height);

		Point pp = new Point(_mouseLocation.x, _mouseLocation.y);
		// Point.Double wp = new Point.Double();
		sContainer.localToWorld(pp, _worldCenter);

		double scaleX = ((double) sBounds.width) / ((double) bounds.width);
		double scaleY = ((double) sBounds.height) / ((double) bounds.height);

		double ww = sWorld.width / (scaleX * _selectedMag);
		double hh = sWorld.height / (scaleY * _selectedMag);

		wr.setFrame(_worldCenter.x - ww / 2, _worldCenter.y - hh / 2, ww, hh);

		return wr;
	}

	/**
	 * Get the world coordinates of the center of the mag window
	 * 
	 * @return the world coordinates of the center of the mag window
	 */
	public static Point2D.Double getWorldCenter() {
		return _worldCenter;
	}

	/**
	 * Hide the magnify window
	 */
	public static void closeMagnifyWindow() {
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
