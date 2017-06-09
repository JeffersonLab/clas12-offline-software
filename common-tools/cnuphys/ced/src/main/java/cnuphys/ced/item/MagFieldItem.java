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
import cnuphys.ced.cedview.central.CentralZView;
import cnuphys.ced.cedview.sectorview.SectorView;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.component.MagFieldDisplayArray;
import cnuphys.ced.fastmc.FastMCManager;
import cnuphys.magfield.FieldProbe;
import cnuphys.magfield.GridCoordinate;
import cnuphys.magfield.MagneticFields;

/**
 * This is a magnetic field item. It is restricted to live only on sector views.
 * It will show the composite magnetic field--the sum of all fields loaded and
 * not set to the zero field.
 * 
 * @author heddle
 * 
 */
public class MagFieldItem extends AItem {

	// sector view parent
	private CedView _view;

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
	 * @param layer
	 *            the layer this item lives on
	 * @param view
	 *            the view--which must be a SectorView
	 */
	public MagFieldItem(LogicalLayer layer, CedView view) {
		super(layer);
		_view = view;
		_style.setFillColor(null);
		_style.setLineColor(Color.red);
		_style.setLineStyle(LineStyle.DASH);
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
	 * @param g
	 *            the Graphics context.
	 * @param container
	 *            the rendering container.
	 */
	@Override
	public void drawItem(Graphics g, IContainer container) {
		if (_failedToLoad) {
			return;
		}

		if (MagneticFields.getInstance().getActiveField() == null) {
			return;
		}

		if (ClasIoEventManager.getInstance().isAccumulating() || FastMCManager.getInstance().isStreaming()) {
			return;
		}

		// what is the display option?
		int displayOption = _view.getMagFieldDisplayOption();
		// should be unnecessary
		if (displayOption == MagFieldDisplayArray.NOMAGDISPLAY) {
			return;
		}

		boolean hasTorus = MagneticFields.getInstance().hasTorus();
		boolean hasSolenoid = MagneticFields.getInstance().hasSolenoid();

		if (_view instanceof SectorView) {
			drawItemSectorView(g, container, displayOption, hasTorus, hasSolenoid);
		}
		else if (_view instanceof CentralZView) {
			drawItemCentralZView(g, container, displayOption, hasTorus, hasSolenoid);
		}

	}

	// drawer for Central Z views
	private void drawItemCentralZView(Graphics g,
			IContainer container,
			int displayOption,
			boolean hasTorus,
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
		FieldProbe probe = FieldProbe.factory();

		float result[] = new float[3];
		double coords[] = new double[5];

		pp.x = updateRect.x + pstep2;
		for (int i = 0; i < xsteps; i++) {
			pp.y = updateRect.y + pstep2;
			for (int j = 0; j < ysteps; j++) {
				container.localToWorld(pp, wp);

				// get the true Cartesian coordinates
				((CentralZView) (_view)).getCLASCordinates(container, pp, wp, coords);

				double z = coords[2];
				double rho = coords[3];
				double phi = coords[4];

				if (displayOption == MagFieldDisplayArray.BMAGDISPLAY) {
					// note conversion to cm from mm
					double bmag = probe.fieldMagnitudeCylindrical(phi, rho / 10, z / 10) / 10.;

					Color color = _colorScaleModelSolenoid.getColor(bmag);
					g.setColor(color);
					g.fillRect(pp.x - pstep2, pp.y - pstep2, pixelStep, pixelStep);
				}
				else if (displayOption == MagFieldDisplayArray.BGRADDISPLAY) {
					probe.gradientCylindrical(phi, rho, z, result);
					double gmag = Math.sqrt(result[0]*result[0] +
							result[1]*result[1] + result[2]*result[2]);
					
					//convert to T/m
					gmag *= 10;
					Color color = _colorScaleModelGradient.getColor(gmag);
					g.setColor(color);
					g.fillRect(pp.x - pstep2, pp.y - pstep2, pixelStep, pixelStep);
				}
				else { // one of the components
					// note conversion to cm from mm
					probe.fieldCylindrical(phi, rho / 10, z / 10, result);
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
					}
					else {
						g.drawRect(pp.x - pstep2, pp.y - pstep2, pixelStep - 1, pixelStep - 1);
					}

				}

				pp.y += pixelStep;
			}
			pp.x += pixelStep;
		} // end for (xsteps)
	}

	private Rectangle getFieldRect(IContainer container, boolean hasTorus, boolean hasSolenoid) {

		fieldBoundary = new Rectangle2D.Double();
		GridCoordinate rCoordinate = null;
		GridCoordinate zCoordinate = null;
		if (hasTorus) {
			rCoordinate = MagneticFields.getInstance().getTorus().getRCoordinate();
			zCoordinate = MagneticFields.getInstance().getTorus().getZCoordinate();
		}
		else {
			rCoordinate = MagneticFields.getInstance().getSolenoid().getRCoordinate();
			zCoordinate = MagneticFields.getInstance().getSolenoid().getZCoordinate();
		}

		fieldBoundary.x = zCoordinate.getMin();
		if (hasSolenoid) {
			fieldBoundary.x = MagneticFields.getInstance().getSolenoid().getZCoordinate().getMin();
		}
		fieldBoundary.width = (zCoordinate.getMax() - fieldBoundary.x);

		fieldBoundary.y = -rCoordinate.getMax();
		fieldBoundary.height = 2 * rCoordinate.getMax();

		Rectangle fieldRect = new Rectangle();
		container.worldToLocal(fieldRect, fieldBoundary);
		return fieldRect;
	}

	// drawer for sector views
	private void drawItemSectorView(Graphics g,
			IContainer container,
			int displayOption,
			boolean hasTorus,
			boolean hasSolenoid) {

		// get the boundary
		Rectangle fieldRect = getFieldRect(container, hasTorus, hasSolenoid);

		// get a probe
		FieldProbe probe = FieldProbe.factory();

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
				((SectorView) (_view)).getCLASCordinates(container, pp, wp, coords);

				double z = coords[2];
				double rho = coords[3];
				double phi = coords[4];

				if (displayOption == MagFieldDisplayArray.BMAGDISPLAY) {
					double bmag = probe.fieldMagnitudeCylindrical(phi, rho, z) / 10.;

					Color color = _colorScaleModelTorus.getColor(bmag);
					g.setColor(color);
					g.fillRect(pp.x - pstep2, pp.y - pstep2, pixelStep, pixelStep);
				}
				else if (displayOption == MagFieldDisplayArray.BGRADDISPLAY) {
					probe.gradientCylindrical(phi, rho, z, result);
					double gmag = Math.sqrt(result[0]*result[0] +
							result[1]*result[1] + result[2]*result[2]);
					
					//convert to T/m
					gmag *= 10;
					Color color = _colorScaleModelGradient.getColor(gmag);
					g.setColor(color);
					g.fillRect(pp.x - pstep2, pp.y - pstep2, pixelStep, pixelStep);
				}
				else { // one of the components
					probe.fieldCylindrical(phi, rho, z, result);
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
					}
					else {
						// g.drawRect(pp.x - pstep2, pp.y - pstep2, pixelStep -
						// 1,
						// pixelStep - 1);
						g.fillOval(pp.x - pstep2, pp.y - pstep2, pixelStep, pixelStep);
					}

				} //a component

				pp.y += pixelStep;
			}
			pp.x += pixelStep;
		} // end for (xsteps)
	}

	/**
	 * Checks whether the item should be drawn. This is an additional check,
	 * beyond the simple visibility flag check. For example, it might check
	 * whether the item intersects the area being drawn.
	 * 
	 * @param g
	 *            the graphics context.
	 * @param container
	 *            the graphical container being rendered.
	 * @return <code>true</code> if the item passes any and all tests, and
	 *         should be drwan.
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
		double max = 10; //T/m
		int len = getGradientColors().length + 1;
		double values[] = new double[len];
		values[0] = 0;
		values[len - 1] = max;
		double del = max / (len - 1);
		for (int i = 1; i < len - 1; i++) {
			values[i] = i*del;
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
		double min = 0.05;
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
	private static double[] getSolenoidValues() {

		int len = getTorusColors().length + 1;

		double values[] = new double[len];
		double min = 0.1;
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

		// int r[] = {255, 176, 37, 132, 253, 205, 130, 127};
		// int g[] = {254, 224, 162, 155, 189, 94, 0, 0};
		// int b[] = {227, 230, 42, 51, 6, 5, 2, 127};
		// int r[] = { 176, 255, 176, 37, 132, 255, 255, 255, 127 };
		// int g[] = { 176, 254, 224, 162, 155, 255, 128, 0, 0 };
		// int b[] = { 176, 227, 230, 42, 51, 0, 0, 0, 127 };
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

				if (k < 2) {
					// colors[k] = Color.cyan;
					colors[k] = new Color(rr, gg, bb, 64);
				}
				else {
					colors[k] = new Color(rr, gg, bb);
				}
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

		// int r[] = {255, 176, 37, 132, 253, 205, 130, 127};
		// int g[] = {254, 224, 162, 155, 189, 94, 0, 0};
		// int b[] = {227, 230, 42, 51, 6, 5, 2, 127};
		// int r[] = { 176, 255, 176, 37, 132, 255, 255, 255, 127 };
		// int g[] = { 176, 254, 224, 162, 155, 255, 128, 0, 0 };
		// int b[] = { 176, 227, 230, 42, 51, 0, 0, 0, 127 };
		// int r[] = { 255, 216, 176, 106, 37, 132, 255, 255, 255, 127 };
		// int g[] = { 255, 239, 224, 193, 162, 155, 255, 128, 0, 0 };
		// int b[] = { 255, 242, 230, 136, 42, 51, 0, 0, 0, 127 };
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

				if (k < 2) {
					// colors[k] = Color.cyan;
					colors[k] = new Color(rr, gg, bb, 64);
				}
				else {
					colors[k] = new Color(rr, gg, bb);
				}
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

}
