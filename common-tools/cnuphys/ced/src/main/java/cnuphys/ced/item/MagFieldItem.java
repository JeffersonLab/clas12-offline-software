package cnuphys.ced.item;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import cnuphys.bCNU.graphics.colorscale.ColorScaleModel;
import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.graphics.style.LineStyle;
import cnuphys.bCNU.item.AItem;
import cnuphys.bCNU.layer.LogicalLayer;
import cnuphys.ced.cedview.CedView;
import cnuphys.ced.cedview.SliceView;
import cnuphys.ced.cedview.central.CentralZView;
import cnuphys.ced.cedview.magfieldview.MagfieldView;
import cnuphys.ced.cedview.sectorview.SectorView;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.component.MagFieldDisplayArray;
import cnuphys.magfield.FieldProbe;
import cnuphys.magfield.GridCoordinate;
import cnuphys.magfield.MagneticFieldChangeListener;
import cnuphys.magfield.MagneticFields;
import cnuphys.magfield.Solenoid;
import cnuphys.magfield.Torus;

/**
 * This is a magnetic field item. It is restricted to live only on sector views.
 * It will show the composite magnetic field--the sum of all fields loaded and
 * not set to the zero field.
 * 
 * @author heddle
 * 
 */
public class MagFieldItem extends AItem implements MagneticFieldChangeListener {

	// sector view parent
	private CedView _view;

	private FieldProbe _activeProbe;

	// if mag field failed to , give up
	private static boolean _failedToLoad = false;

	// common colorscale
	public static ColorScaleModel _colorScaleModelTorus = new ColorScaleModel("", getTorusValues(), getTorusColors(), 2,
			1);
	public static ColorScaleModel _colorScaleModelSolenoid = new ColorScaleModel("", getSolenoidValues(),
			getSolenoidColors(), 2, 1);
	public static ColorScaleModel _colorScaleModelGradient = new ColorScaleModel("", getGradientValues(),
			getGradientColors(), 2, 1);

	// pixel step size
	private int pixelStep = 3;

	// the world coordinate boundary of the field
	private static Rectangle2D.Double fieldBoundary;

	/**
	 * Create a magnetic field item. Only allowed on sector views
	 * 
	 * @param layer the layer this item lives on
	 * @param view  the view--which must be a SectorView
	 */
	public MagFieldItem(LogicalLayer layer, CedView view) {
		super(layer);
		_view = view;
		_style.setFillColor(null);
		_style.setLineColor(Color.red);
		_style.setLineStyle(LineStyle.DASH);
		MagneticFields.getInstance().addMagneticFieldChangeListener(this);
	}

	/**
	 * Always return false for this item.
	 * 
	 * @return false--never care if mouse is inside the field.
	 */
	@Override
	public boolean contains(IContainer container, Point screenPoint) {
		return false;
	}

	/**
	 * Custom drawing for the field.
	 * 
	 * @param g         the Graphics context.
	 * @param container the rendering container.
	 */
	@Override
	public void drawItem(Graphics g, IContainer container) {
		
		if (_failedToLoad) {
			return;
		}

		if (_activeProbe == null) {
			_activeProbe = FieldProbe.factory();
		}

		if (_activeProbe == null) {
			return;
		}

		if (ClasIoEventManager.getInstance().isAccumulating()) {
			return;
		}

		// what is the display option?
		int displayOption = _view.getMagFieldDisplayOption();
		// should be unnecessary
		if (displayOption == MagFieldDisplayArray.NOMAGDISPLAY) {
			return;
		}

		boolean hasTorus = MagneticFields.getInstance().hasActiveTorus();
		boolean hasSolenoid = MagneticFields.getInstance().hasActiveSolenoid();

		if (_view instanceof SectorView) {
			drawItemSectorView(g, container, displayOption, hasTorus, hasSolenoid);
		} else if (_view instanceof MagfieldView) {
			drawItemMagfieldView(g, container, displayOption, hasTorus, hasSolenoid);
		} else if (_view instanceof CentralZView) {
			drawItemCentralZView(g, container, displayOption, hasTorus, hasSolenoid);
		}

	}

	// drawer for Central Z views
	private void drawItemCentralZView(Graphics g, IContainer container, int displayOption, boolean hasTorus,
			boolean hasSolenoid) {

		if (!hasSolenoid) {
			return;
		}

		Rectangle fieldRect = getFieldRect(container, hasTorus, hasSolenoid);

		Rectangle bounds = container.getComponent().getBounds();
		bounds.x = 0;
		bounds.y = 0;

		Rectangle updateRect = bounds.intersection(fieldRect);

		int xsteps = updateRect.width / pixelStep + 1;
		int ysteps = updateRect.height / pixelStep + 1;

		Point2D.Double wp = new Point2D.Double();
		Point pp = new Point();

		int pstep2 = pixelStep / 2;

		float result[] = new float[3];
		double coords[] = new double[5];

		pp.x = updateRect.x + pstep2;
		for (int i = 0; i < xsteps; i++) {
			pp.y = updateRect.y + pstep2;
			for (int j = 0; j < ysteps; j++) {
				container.localToWorld(pp, wp);

				// get the true Cartesian coordinates
				((CentralZView) (_view)).getCLASCordinates(container, pp, wp, coords);

				float x = (float) coords[0];
				float y = (float) coords[1];
				float z = (float) coords[2];
				double phi = coords[4];

				if (displayOption == MagFieldDisplayArray.BMAGDISPLAY) {
					// note conversion to cm from mm
					double bmag = _activeProbe.fieldMagnitude(x / 10, y / 10, z / 10) / 10.;

					Color color = _colorScaleModelSolenoid.getColor(bmag);
					g.setColor(color);
					g.fillRect(pp.x - pstep2, pp.y - pstep2, pixelStep, pixelStep);
				} else if (displayOption == MagFieldDisplayArray.BGRADDISPLAY) {
					_activeProbe.gradient(x, y, z, result);
					double gmag = Math.sqrt(result[0] * result[0] + result[1] * result[1] + result[2] * result[2]);

					// convert to T/m
					gmag *= 10;
					Color color = _colorScaleModelGradient.getColor(gmag);
					g.setColor(color);
					g.fillRect(pp.x - pstep2, pp.y - pstep2, pixelStep, pixelStep);
				} else { // one of the components
							// note conversion to cm from mm
					_activeProbe.field(x / 10, y / 10, z / 10, result);
					double comp = 0.0;
					switch (displayOption) {
					case MagFieldDisplayArray.BXDISPLAY:
						comp = result[0] / 10.;
						break;
					case MagFieldDisplayArray.BYDISPLAY:
						comp = result[1] / 10.;
						break;
					case MagFieldDisplayArray.BZDISPLAY:
						comp = result[2] / 10.;
						break;
					case MagFieldDisplayArray.BPERPDISPLAY:
						// normal vect to sect view is
						// -sin(phi)*i + cos(phi)*j
						double sinp = Math.sin(Math.toRadians(phi));
						double cosp = Math.cos(Math.toRadians(phi));
						comp = (-result[0] * sinp + result[1] * cosp) / 10.;
						break;
					}
					Color color = _colorScaleModelSolenoid.getColor(Math.abs(comp));
					g.setColor(color);

					if (comp > 0) {
						g.fillRect(pp.x - pstep2, pp.y - pstep2, pixelStep, pixelStep);
					} else {
						g.drawRect(pp.x - pstep2, pp.y - pstep2, pixelStep - 1, pixelStep - 1);
					}

				}

				pp.y += pixelStep;
			}
			pp.x += pixelStep;
		} // end for (xsteps)
	}

	private Rectangle getFieldRect(IContainer container, boolean hasTorus, boolean hasSolenoid) {

		Solenoid solenoid = MagneticFields.getInstance().getSolenoid();
		Torus torus = MagneticFields.getInstance().getTorus();

		double solZmin = hasSolenoid ? (solenoid.getZMin() + solenoid.getShiftZ()) : Double.POSITIVE_INFINITY;
		double solZmax = hasSolenoid ? (solenoid.getZMax() + solenoid.getShiftZ()) : Double.NEGATIVE_INFINITY;
		double torZmin = hasTorus ? (torus.getZMin() + torus.getShiftZ()) : Double.POSITIVE_INFINITY;
		double torZmax = hasTorus ? (torus.getZMax() + torus.getShiftZ()) : Double.NEGATIVE_INFINITY;

		double zmin = Math.min(solZmin, torZmin);
		double zmax = Math.max(solZmax, torZmax);

		fieldBoundary = new Rectangle2D.Double();
		GridCoordinate rCoordinate = null;
		if (hasTorus) {
			rCoordinate = MagneticFields.getInstance().getTorus().getRCoordinate();
		} else {
			rCoordinate = MagneticFields.getInstance().getSolenoid().getRCoordinate();
		}

		fieldBoundary.x = zmin;
		fieldBoundary.width = (zmax - zmin);

		fieldBoundary.y = -rCoordinate.getMax();
		fieldBoundary.height = 2 * rCoordinate.getMax();

		Rectangle fieldRect = new Rectangle();
		container.worldToLocal(fieldRect, fieldBoundary);
		return fieldRect;
	}
	
	// drawer for sector views
	private void drawItemMagfieldView(Graphics g, IContainer container, 
			int displayOption, boolean hasTorus,
			boolean hasSolenoid) {
		drawItemSectorView(g, container, displayOption, hasTorus, hasSolenoid);
	}

	// drawer for sector views
	private void drawItemSectorView(Graphics g, IContainer container, int displayOption, boolean hasTorus,
			boolean hasSolenoid) {

		if (_activeProbe == null) {
			return;
		}

		Rectangle bounds = container.getComponent().getBounds();
		bounds.x = 0;
		bounds.y = 0;

		// get the boundary
		Rectangle fieldRect;

		fieldRect = getFieldRect(container, hasTorus, hasSolenoid);


		Rectangle updateRect = bounds.intersection(fieldRect);

		int xsteps = updateRect.width / pixelStep + 1;
		int ysteps = updateRect.height / pixelStep + 1;

		Point2D.Double wp = new Point2D.Double();
		Point pp = new Point();

		int pstep2 = pixelStep / 2;

		float result[] = new float[3];
		double coords[] = new double[5];

		pp.x = updateRect.x + pstep2;
		for (int i = 0; i < xsteps; i++) {
			pp.y = updateRect.y + pstep2;
			for (int j = 0; j < ysteps; j++) {
				container.localToWorld(pp, wp);

				// get the true Cartesian coordinates
				((SliceView) (_view)).getCLASCordinates(container, pp, wp, coords);

				float x = (float) coords[0];
				float y = (float) coords[1];
				float z = (float) coords[2];
				double phi = coords[4];

				// if (_activeProbe.containsCylindrical(phi, rho, z)) {

				if (displayOption == MagFieldDisplayArray.BMAGDISPLAY) {
					double bmag = _activeProbe.fieldMagnitude(x, y, z) / 10.;

					Color color = _colorScaleModelTorus.getColor(bmag);
					g.setColor(color);
					g.fillRect(pp.x - pstep2, pp.y - pstep2, pixelStep, pixelStep);
				} else if (displayOption == MagFieldDisplayArray.BGRADDISPLAY) {
					_activeProbe.gradient(x, y, z, result);
					double gmag = Math.sqrt(result[0] * result[0] + result[1] * result[1] + result[2] * result[2]);

					// convert to T/m
					gmag *= 10;

					Color color = _colorScaleModelGradient.getColor(gmag);

					if (color.getAlpha() < 255) {
						color = new Color(color.getRed(), color.getGreen(), color.getBlue(), 255);
					}

					g.setColor(color);
					g.fillRect(pp.x - pstep2, pp.y - pstep2, pixelStep, pixelStep);

				} else { // one of the components
					_activeProbe.field(x, y, z, result);
					double comp = 0.0;
					switch (displayOption) {
					case MagFieldDisplayArray.BXDISPLAY:
						comp = result[0] / 10.;
						break;
					case MagFieldDisplayArray.BYDISPLAY:
						comp = result[1] / 10.;
						break;
					case MagFieldDisplayArray.BZDISPLAY:
						comp = result[2] / 10.;
						break;
					case MagFieldDisplayArray.BPERPDISPLAY:
						// normal vect to sect view is
						// -sin(phi)*i + cos(phi)*j
						double sinp = Math.sin(Math.toRadians(phi));
						double cosp = Math.cos(Math.toRadians(phi));
						comp = (-result[0] * sinp + result[1] * cosp) / 10.;
						break;

					}
					Color color = _colorScaleModelTorus.getColor(Math.abs(comp));
					g.setColor(color);

					// distinguish positive and negative
					if (comp > 0) {
						g.fillRect(pp.x - pstep2, pp.y - pstep2, pixelStep, pixelStep);
					} else {
						// g.drawRect(pp.x - pstep2, pp.y - pstep2,
						// pixelStep -
						// 1,
						// pixelStep - 1);
						g.fillOval(pp.x - pstep2, pp.y - pstep2, pixelStep, pixelStep);
					}

				} // a component
				// } //active probe contains

				pp.y += pixelStep;
			}
			pp.x += pixelStep;
		} // end for (xsteps)
	}

	/**
	 * Checks whether the item should be drawn. This is an additional check, beyond
	 * the simple visibility flag check. For example, it might check whether the
	 * item intersects the area being drawn.
	 * 
	 * @param g         the graphics context.
	 * @param container the graphical container being rendered.
	 * @return <code>true</code> if the item passes any and all tests, and should be
	 *         drwan.
	 */
	@Override
	public boolean shouldDraw(Graphics g, IContainer container) {

		if (fieldBoundary == null) {
			return true;
		}
		Rectangle r = new Rectangle();
		container.worldToLocal(r, fieldBoundary);
		return container.getComponent().getBounds().intersects(r);
	}

	private static double[] getGradientValues() {
		int len = getGradientColors().length + 1;

		double min = 0.0;
		double max = 15; // T/m
		double del = (max - min) / (len - 1);
		double values[] = new double[len];
		values[0] = min;
		values[len - 1] = max;
		for (int i = 1; i < len - 1; i++) {
			// double speedup = 5.0;
			double speedup = 6.0;
			values[i] = min + (max - min) * Math.exp(-i * del * speedup / max);

			// double x = (Math.PI * i) / (2.0 * (values.length - 1));
			// values[i] = min + (max - min) * (1.0 - Math.cos(x));
		}
		return values;
	}

	/**
	 * Get the values array for the plot.
	 * 
	 * @return the values array.
	 */
	private static double[] getTorusValues() {

		int len = getTorusColors().length + 1;

		double values[] = new double[len];
	//	double min = 0.05;
		double min = 0;
		double max = MagneticFields.getInstance().maxFieldMagnitude() / 10.0;
		double del = (max - min) / (len - 1);

		values[0] = min;
		values[len - 1] = max;

		for (int i = 1; i < len - 1; i++) {
			// double speedup = 5.0;
			double speedup = 6.0;
			values[i] = min + (max - min) * Math.exp(-i * del * speedup / max);
		}
		return values;
	}

	/**
	 * Get the values array for the plot.
	 * 
	 * @return the values array.
	 */
	private static double[] getSolenoidValues() {

		int len = getSolenoidColors().length + 1;

		double values[] = new double[len];
//		double min = 0.1;
		double min = 0;
		double max = MagneticFields.getInstance().maxFieldMagnitude() / 10.0;
		// double del = (max-min)/(values.length-1);

		values[0] = min;
		values[len - 1] = max;

		for (int i = 1; i < len - 1; i++) {
			// values[i] = i*del;
			// use nonlinear cosine scale
			// double x = (Math.PI * i) / (2.0 * (values.length - 1));
			// values[i] = min + (max - min) * (1.0 - Math.cos(x));

			double del = (max - min) / (len - 1);
			double speedup = 6.0;
			values[i] = min + (max - min) * Math.exp(-i * del * speedup / max);

			// double x = (Math.PI * i) / (2.0 * (values.length - 1));
			// values[i] = min + (max - min) * (1.0 - Math.cos(x));
		}
		return values;
	}

	private static Color[] getGradientColors() {
		return getTorusColors();
	}
	

	/**
	 * Get the color array for the plot.
	 * 
	 * @return the color array for the plot.
	 */
	private static Color[] getTorusColors() {
		
		int r[] = { 255, 216, 176, 106, 37, 132, 193, 255, 255, 255, 255, 127 };
		int g[] = { 255, 239, 224, 193, 162, 155, 205, 255, 191, 128, 0, 0 };
		int b[] = { 255, 242, 230, 136, 42, 51, 25, 0, 0, 0, 0, 127 };

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

	/**
	 * Get the color array for the plot.
	 * 
	 * @return the color array for the plot.
	 */
	private static Color[] getSolenoidColors() {
		
		int r[] = { 255, 106, 37, 132, 255, 255, 255, 127 };
		int g[] = { 255, 193, 162, 155, 255, 128, 0, 0 };
		int b[] = { 255, 136, 42, 51, 0, 0, 0, 127 };

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

	@Override
	public void modify() {
	}

	@Override
	public Rectangle2D.Double getWorldBounds() {
		return null;
	}

	@Override
	public void magneticFieldChanged() {
		_activeProbe = FieldProbe.factory();
	}

}
