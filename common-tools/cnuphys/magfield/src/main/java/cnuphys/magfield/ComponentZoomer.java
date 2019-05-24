package cnuphys.magfield;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;

import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

public class ComponentZoomer implements MouseListener, MouseMotionListener {

	private boolean _isDragging;
	private Point _startPP;
	private Point _endPP;
	private Rectangle _oldXorRect;

	// component being zoomed
	private JComponent _component;

	// the zoomable, which is probable the same as the component
	private IComponentZoomable _zoomable;

	// the original world system
	private Rectangle.Double _defaultWorld;

	public ComponentZoomer(IComponentZoomable zoomable) {
		_zoomable = zoomable;
		_component = _zoomable.getComponent();

		_component.addMouseListener(this);
		_component.addMouseMotionListener(this);

		Rectangle.Double wr = _zoomable.getWorldSystem();
		_defaultWorld = new Rectangle.Double();
		_defaultWorld.setRect(wr.x, wr.y, wr.width, wr.height);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		System.out.println("Mouse Clicked");
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (SwingUtilities.isRightMouseButton(e) || (e.getButton() == 3) || e.isControlDown()) {
			handleRightClick(e);
			return;
		}
		System.out.println("Mouse Pressed button = " + e.getButton());
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (_isDragging) {
			_isDragging = false;
			System.out.println("Dragging ended.");
			_endPP = new Point(e.getX(), e.getY());

			int delX = (Math.abs(_endPP.x - _startPP.x));
			int delY = (Math.abs(_endPP.y - _startPP.y));

			if ((delX > 4) && delY > 4) {
				Rectangle r = rectangleFromPoints(_startPP, _endPP);
				Rectangle.Double wr = new Rectangle.Double();
				localToWorld(r, wr);
//				System.out.println("Zoom rect: " + wr);

				_zoomable.setWorldSystem(wr);
				_component.repaint();
			}

			_startPP = null;
			_endPP = null;
			_oldXorRect = null;

		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (!_isDragging) {
			_startPP = new Point(e.getX(), e.getY());
			_isDragging = true;
			return;
		}

		Rectangle cr = rectangleFromPoints(_startPP, e.getPoint());

		Graphics g = _component.getGraphics();

		g.setColor(Color.black);
		g.setXORMode(Color.white);

		if (_oldXorRect != null) {
			g.drawRect(_oldXorRect.x, _oldXorRect.y, _oldXorRect.width, _oldXorRect.height);
		}

		g.drawRect(cr.x, cr.y, cr.width, cr.height);
		_oldXorRect = cr;
		g.dispose();
	}

	@Override
	public void mouseMoved(MouseEvent e) {
	}

	// popup trigger
	private void handleRightClick(MouseEvent e) {
		System.out.println("Mouse Pressed Right Click");

		JPopupMenu popupMenu = new JPopupMenu();
		popupMenu.setLightWeightPopupEnabled(false);

		ActionListener al = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				_zoomable.setWorldSystem(_defaultWorld);
				_component.repaint();
			}

		};

		JMenuItem openItem = new JMenuItem("Restore Default Zoom");
		openItem.addActionListener(al);
		popupMenu.add(openItem);

		popupMenu.show(_component, e.getX(), e.getY());

	}

	/**
	 * Given two points, return the rectangle
	 * 
	 * @param p1 one point
	 * @param p2 the other point
	 * @return the rectangle created from two points
	 */

	public static Rectangle rectangleFromPoints(Point p1, Point p2) {

		if ((p1 == null) && (p2 == null)) {
			return null;
		}

		if (p1 == null) {
			return new Rectangle(p2.x, p2.y, 0, 0);
		} else if (p2 == null) {
			return new Rectangle(p1.x, p1.y, 0, 0);
		}

		int w = Math.abs(p2.x - p1.x);
		int h = Math.abs(p2.y - p1.y);
		int x = Math.min(p1.x, p2.x);
		int y = Math.min(p1.y, p2.y);
		return new Rectangle(x, y, w, h);
	}

	public void localToWorld(Rectangle r, Rectangle.Double wr) {
		Point p0 = new Point(r.x, r.y);
		Point p1 = new Point(r.x + r.width, r.y + r.height);
		Point2D.Double wp0 = new Point2D.Double();
		Point2D.Double wp1 = new Point2D.Double();
		_zoomable.localToWorld(p0, wp0);
		_zoomable.localToWorld(p1, wp1);

		// New version to accommodate world with x decreasing right
		double x = wp0.x;
		double y = wp1.y;
		double w = wp1.x - wp0.x;
		double h = wp0.y - wp1.y;
		wr.setFrame(x, y, w, h);

	}

}
