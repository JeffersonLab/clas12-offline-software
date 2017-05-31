package cnuphys.magfieldC;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class MagneticFieldCanvas extends JComponent implements MouseListener, MouseMotionListener {

	RenderingHints renderHints = new RenderingHints(RenderingHints.KEY_ANTIALIASING,
			RenderingHints.VALUE_ANTIALIAS_OFF);
	{
		renderHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
	};

	protected AffineTransform _localToWorld;
	protected AffineTransform _worldToLocal;

	protected Rectangle.Double _worldSystem;

	protected Font font = new Font("SandSerif", Font.PLAIN, 10);
	protected Font font2 = new Font("SandSerif", Font.BOLD, 12);

	// coordinate system
	public enum CSType {
		XZ, YZ
	}

	private CSType _cstype;

	private ColorScaleModel _colorModel;

	// trajectories
	private Vector<Trajectory> _trajectories = new Vector<Trajectory>(5, 2);

	/**
	 * Create a canvas
	 * 
	 * @param xmin
	 * @param perpMin
	 *            min value in direction perpendicular to z (the beam direction)
	 * @param width
	 * @param height
	 */
	public MagneticFieldCanvas(double xmin, double perpMin, double width, double height, CSType type) {

		_worldSystem = new Rectangle.Double(xmin, perpMin, width, height);
		_cstype = type;
		ComponentAdapter componentAdapter = new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent ce) {
				setAffineTransforms();
				repaint();
			}
		};

		addComponentListener(componentAdapter);
		addMouseListener(this);
		addMouseMotionListener(this);

		// listen for magnetic field changes
		MagneticFieldChangeListener mflistener = new MagneticFieldChangeListener() {

			@Override
			public void magneticFieldChanged() {
				_colorModel = null;
				// System.err.println("Magnetic Field has changed");
				repaint();
			}

		};
		MagneticFields.getInstance().addMagneticFieldChangeListener(mflistener);

	}

	/**
	 * Remove all trajectories
	 */
	public void clearTrajectories() {
		(new Throwable()).printStackTrace();
		_trajectories.removeAllElements();
		repaint();
	}

	/**
	 * Set a trajectory
	 * 
	 * @param xx
	 * @param yy
	 * @param zz
	 */
	public void addTrajectory(double xx[], double yy[], double zz[], Color lc, Stroke ls) {
		_trajectories.addElement(new Trajectory(xx, yy, zz, lc, ls));
		repaint();
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		setAffineTransforms();
		drawMagneticField(g, getBounds());
		drawTrajectories(g, getBounds());
		drawGrid(g, getBounds());
	}

	/**
	 * Draw a single trajectory
	 * 
	 * @param g
	 *            the graphics context
	 * @param bounds
	 */
	protected void drawTrajectories(Graphics g, Rectangle bounds) {
		if (_trajectories.isEmpty()) {
			return;
		}

		Path2D poly = null;
		poly = new Path2D.Double();

		Point pp = new Point();
		Point.Double wp = new Point.Double();
		Graphics2D g2 = (Graphics2D) g;
		Stroke oldStroke = g2.getStroke();

		for (Trajectory traj : _trajectories) {
			switch (_cstype) {
			case XZ:
				wp.setLocation(traj.zz[0], traj.xx[0]);
				break;
			case YZ:
				wp.setLocation(traj.zz[0], traj.yy[0]);
				break;
			}
			worldToLocal(pp, wp);
			poly.moveTo(pp.x, pp.y);

			g2.setStroke(traj.stroke);
			g2.setColor(traj.lineColor);

			for (int i = 1; i < traj.zz.length; i++) {
				switch (_cstype) {
				case XZ:
					wp.setLocation(traj.zz[i], traj.xx[i]);
					break;
				case YZ:
					wp.setLocation(traj.zz[i], traj.yy[i]);
					break;
				}
				worldToLocal(pp, wp);
				poly.lineTo(pp.x, pp.y);
			}
			g2.draw(poly);

		} // over trajectories

		g2.setStroke(oldStroke);

	}

	// Get the transforms for world to local and vice versa
	protected void setAffineTransforms() {
		Rectangle bounds = getBounds();

		if ((bounds == null) || (bounds.width < 1) || (bounds.height < 1)) {
			_localToWorld = null;
			_worldToLocal = null;
			return;
		}

		double scaleX = _worldSystem.width / bounds.width;
		double scaleY = _worldSystem.height / bounds.height;

		_localToWorld = AffineTransform.getTranslateInstance(_worldSystem.x, _worldSystem.getMaxY());
		_localToWorld.concatenate(AffineTransform.getScaleInstance(scaleX, -scaleY));
		_localToWorld.concatenate(AffineTransform.getTranslateInstance(-bounds.x, -bounds.y));

		try {
			_worldToLocal = _localToWorld.createInverse();
		} catch (NoninvertibleTransformException e) {
			e.printStackTrace();
		}
	}

	// draw the grid
	protected void drawGrid(Graphics g, Rectangle bounds) {
		double hvals[] = { -25, 75, 175, 275, 375, 475, 575 };
		double vvals[] = { -300, -200, -100, 0, 100, 200, 300 };

		Point pp = new Point();
		Point2D.Double wp = new Point2D.Double();
		g.setColor(Color.black);
		g.setFont(font);

		FontMetrics fm = getFontMetrics(font);

		// vertical lines
		wp.y = 0.;
		for (int i = 0; i < hvals.length; i++) {
			wp.x = hvals[i];
			worldToLocal(pp, wp);
			g.drawLine(pp.x, bounds.y, pp.x, bounds.y + bounds.height);
			String s = String.format("%-4.0f", hvals[i]);

			int tx = pp.x - fm.stringWidth(s) / 2;
			int ty = bounds.y + bounds.height - fm.getHeight();

			g.setColor(Color.yellow);
			g.drawString(s, tx, ty);
			g.setColor(Color.black);
		}

		// horizontal lines
		wp.x = 200.;
		for (int i = 0; i < vvals.length; i++) {
			wp.y = vvals[i];
			worldToLocal(pp, wp);
			g.drawLine(bounds.x, pp.y, bounds.x + bounds.width, pp.y);

			if (wp.y == 0.) {
				g.setColor(Color.white);
				g.drawLine(bounds.x, pp.y - 1, bounds.x + bounds.width, pp.y - 1);
				g.setColor(Color.black);
			}
			String s = String.format(" %-4.0f ", vvals[i]);

			int tx = bounds.x + bounds.width - fm.stringWidth(s);
			int ty = pp.y + fm.getHeight() / 2;

			g.setColor(Color.yellow);
			g.drawString(s, tx, ty);
			g.setColor(Color.black);

		}

		// little cs
		int xs = bounds.x + 10;
		int ys = bounds.y + bounds.height - 60;
		int linlen = 40;
		g.setColor(Color.white);
		g.drawLine(xs, ys, xs + linlen, ys);
		g.drawLine(xs + 1, ys, xs + 1, ys - linlen);
		g.setColor(Color.black);
		g.drawLine(xs, ys + 1, xs + linlen, ys + 1);
		g.drawLine(xs, ys, xs, ys - linlen);

		g.setFont(font2);
		fm = getFontMetrics(font2);
		g.setColor(Color.white);
		g.drawString("Z", xs + linlen + 5, ys + fm.getHeight() / 2);
		g.setColor(Color.black);
		g.drawString("Z", xs + linlen + 4, ys + fm.getHeight() / 2);
		String s = null;
		switch (_cstype) {
		case XZ:
			s = "X";
			break;
		case YZ:
			s = "Y";
			break;
		}
		g.setColor(Color.white);
		g.drawString(s, xs - fm.stringWidth("s") / 2 + 1, ys - linlen - 4);
		g.setColor(Color.black);
		g.drawString(s, xs - fm.stringWidth("s") / 2, ys - linlen - 4);

	}

	// draw the magnetic field
	protected void drawMagneticField(Graphics g, Rectangle bounds) {

		if (_colorModel == null) {
			_colorModel = new ColorScaleModel(getFieldValues(), getFieldColors());
		}

		Point pp = new Point();
		Point2D.Double wp = new Point2D.Double();
		CartesianVector result = new CartesianVector();
		int w = 2;
		int h = 2;

		for (int sx = bounds.x; sx < bounds.x + bounds.width; sx += w) {
			pp.x = sx;

			for (int sy = bounds.y; sy < bounds.y + bounds.height; sy += h) {
				pp.y = sy;

				localToWorld(pp, wp);

				switch (_cstype) {
				case XZ:
					MagneticFields.getInstance().field((float) (wp.y), 0f, (float) (wp.x), result);
					break;
				case YZ:
					MagneticFields.getInstance().field(0f, (float) (wp.y), (float) (wp.x), result);
					break;
				}

				//convert to tesla
				double bmag = result.length() / 10.0;

				// color?

				g.setColor(_colorModel.getColor(bmag));
				g.fillRect(pp.x, pp.y, w, h);

			}
		}
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseDragged(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseMoved(MouseEvent me) {
		// localToWorld(me.getPoint(), _workPoint);
		// MagneticFields.field((float)(_workPoint.y), 0f,
		// (float)(_workPoint.x), _workResult);
		// float vx = _workResult[0];
		// float vy = _workResult[1];
		// float vz = _workResult[2];
		// double bmag = Math.sqrt(vx*vx + vy*vy + vz*vz);
		// System.err.println("location: " + _workPoint + " Bmag: " + bmag);
	}

	/**
	 * This converts a screen or pixel point to a world point.
	 * 
	 * @param pp
	 *            contains the local (screen-pixel) point.
	 * @param wp
	 *            will hold the resultant world point.
	 */
	public void localToWorld(Point pp, Point.Double wp) {
		if (_localToWorld != null) {
			_localToWorld.transform(pp, wp);
		}
	}

	/**
	 * This converts a world point to a screen or pixel point.
	 * 
	 * @param pp
	 *            will hold the resultant local (screen-pixel) point.
	 * @param wp
	 *            contains world point.
	 */
	public void worldToLocal(Point pp, Point.Double wp) {
		if (_worldToLocal != null) {
			_worldToLocal.transform(wp, pp);
		}
	}

	/**
	 * Get the values array for the plot.
	 * 
	 * @return the values array.
	 */
	public static double getFieldValues()[] {

		int len = getFieldColors().length + 1;

		double values[] = new double[len];
		double min = 0.0;
		double max = MagneticFields.getInstance().maxFieldMagnitude() / 10.0;

		values[0] = min;
		values[len - 1] = max;

		for (int i = 1; i < len - 1; i++) {

			double del = (max - min) / (len - 1);
			double speedup = 5.0;
			values[i] = min + (max - min) * Math.exp(-i * del * speedup / max);
		}
		return values;
	}

	/**
	 * Get the color array for the plot.
	 * 
	 * @return the color array for the plot.
	 */
	public static Color getFieldColors()[] {

		int r[] = { 176, 255, 176, 37, 132, 255, 255, 255, 127 };
		int g[] = { 176, 254, 224, 162, 155, 255, 128, 0, 0 };
		int b[] = { 176, 227, 230, 42, 51, 0, 0, 0, 127 };

		int n = r.length;
		int nm1 = n - 1;

		double f = 1.0 / n;

		int colorlen = nm1 * n + 1;
		Color colors[] = new Color[colorlen];

		int k = 0;
		for (int i = 0; i < nm1; i++) {
			for (int j = 0; j < n; j++) {
				int rr = r[i] + (int) (j * f * (r[i + 1] - r[i]));
				int gg = g[i] + (int) (j * f * (g[i + 1] - g[i]));
				int bb = b[i] + (int) (j * f * (b[i + 1] - b[i]));
				colors[k] = new Color(rr, gg, bb);
				k++;
			}
		}

		colors[nm1 * n] = new Color(r[nm1], g[nm1], b[nm1]);
		return colors;
	}

	public JPanel getPanelWithStatus(int width, int height) {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(2, 2));
		final Point2D.Double _workPoint = new Point2D.Double();
		final CartesianVector B = new CartesianVector();

		// add a status
		final JLabel label = new JLabel("Status");
		MouseMotionAdapter maa = new MouseMotionAdapter() {
			@Override
			public void mouseMoved(MouseEvent me) {
				localToWorld(me.getPoint(), _workPoint);

				String s = null;
				switch (_cstype) {
				case XZ:
					MagneticFields.getInstance().field((float) (_workPoint.y), 0f, (float) (_workPoint.x), B);
					s = String.format("  loc: ( %-4.2f,  0,  %-4.2f) Bmag %-4.2f T  B = (%-4.2f, %-4.2f,  %-4.2f)",
							_workPoint.y, _workPoint.x, B.length() / 10, B.x / 10, B.y / 10, B.z / 10);
					break;
				case YZ:
					MagneticFields.getInstance().field(0f, (float) (_workPoint.y), (float) (_workPoint.x), B);

					s = String.format("  loc: ( 0,  %-4.2f, %-4.2f) Bmag %-4.2f T  B = (%-4.2f, %-4.2f,  %-4.2f",
							_workPoint.y, _workPoint.x, B.length() / 10, B.x / 10, B.y / 10, B.z / 10);
					break;
				}
				label.setText(s);
			}

		};
		addMouseMotionListener(maa);

		panel.add(this, BorderLayout.CENTER);
		panel.add(label, BorderLayout.SOUTH);
		Dimension d = new Dimension(width, height);
		panel.setPreferredSize(d);
		return panel;
	}

	public class Trajectory {
		// single traj
		public double xx[];
		public double yy[];
		public double zz[];
		public Color lineColor;
		public Stroke stroke;

		public Trajectory(double[] xx, double[] yy, double[] zz, Color lineColor, Stroke stroke) {
			super();
			this.xx = xx;
			this.yy = yy;
			this.zz = zz;
			this.stroke = stroke;
			this.lineColor = lineColor;
		}

	}

}
