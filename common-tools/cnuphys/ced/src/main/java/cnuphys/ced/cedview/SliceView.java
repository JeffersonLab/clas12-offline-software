package cnuphys.ced.cedview;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.util.List;

import org.jlab.geom.prim.Plane3D;

import cnuphys.bCNU.format.DoubleFormat;
import cnuphys.bCNU.graphics.GraphicsUtilities;
import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.graphics.container.ScaleDrawer;
import cnuphys.bCNU.graphics.style.LineStyle;
import cnuphys.bCNU.item.YouAreHereItem;
import cnuphys.bCNU.util.UnicodeSupport;
import cnuphys.ced.cedview.sectorview.DisplaySectors;
import cnuphys.ced.geometry.GeometryManager;
import cnuphys.ced.geometry.util.VectorSupport;
import cnuphys.magfield.MagneticFields;
import cnuphys.magfield.MagneticFields.FieldType;

public abstract class SliceView extends CedView {
	
	// each sector view has an upper and lower sector: 1-4, 2-5, 3-6
	public static final int UPPER_SECTOR = 0;
	public static final int LOWER_SECTOR = 1;

	// offset left and right
	protected static int LEFT = 140;
	protected static int DELTAH = 80;
	protected static int TOP = 40;
	protected static int DELTAV = 30;

	// for tilted axis
	protected static final Color TRANSCOLOR = new Color(0, 0, 0, 64);
	protected static final Color TRANSCOLOR2 = new Color(255, 255, 255, 64);

	// line stroke
	protected static Stroke stroke = GraphicsUtilities.getStroke(1.5f, LineStyle.SOLID);
	
	// for naming clones
	protected static int CLONE_COUNT[] = { 0, 0, 0 };

	// small number test
	protected static final double TINY = 1.0e-10;

	// Holds what pair of sectors are being displayed
	protected DisplaySectors _displaySectors;

	// the nominal target z in cm
	protected double _targetZ = 0.0;

	// a scale drawer
	protected ScaleDrawer _scaleDrawer = new ScaleDrawer("cm", ScaleDrawer.BOTTOMLEFT);


	/**
	 * Create a "phi slice" view, e.g. the famous sector views
	 * @param displaySectors which two sectors are displayed
	 * @param keyVals
	 */
	public SliceView(DisplaySectors displaySectors, Object... keyVals) {
		super(keyVals);
		
		_displaySectors = displaySectors;

		// the projection plane starts as midplane
		projectionPlane = GeometryManager.constantPhiPlane(0);
	}
	
	// draw the tilted axis
	protected void drawTiltedAxis(Graphics g, IContainer container, int sector) {

		Point2D.Double wp0 = new Point2D.Double();
		Point2D.Double wp1 = new Point2D.Double();
		Point p0 = new Point();
		Point p1 = new Point();

		double theta = Math.toRadians(25.);
		double phi;
		if (_displaySectors == DisplaySectors.SECTORS25) {
			phi = 60;
		} else if (_displaySectors == DisplaySectors.SECTORS36) {
			phi = 120.0;
		} else {
			phi = 0;
		}
		// lower sector
		if (sector == LOWER_SECTOR) {
			phi += 180;
		}
		phi = Math.toRadians(phi);

		projectClasToWorld(0, 0, 0, projectionPlane, wp0);
		container.worldToLocal(p0, wp0);

		for (int i = 1; i <= 10; i++) {
			double r = 100 * i;
			double rho = r * Math.sin(theta);
			double x = rho * Math.cos(phi);
			double y = rho * Math.sin(phi);
			double z = r * Math.cos(theta);
			projectClasToWorld(x, y, z, projectionPlane, wp1);

			container.worldToLocal(p1, wp1);

			if ((i % 2) == 0) {
				g.setColor(TRANSCOLOR);
			} else {
				g.setColor(TRANSCOLOR2);
			}
			g.drawLine(p0.x, p0.y, p1.x, p1.y);

			p0.x = p1.x;
			p0.y = p1.y;
		}
	}
	
	

	/**
	 * Every view should be able to say what sector the current point location
	 * represents.
	 * 
	 * @param container   the base container for the view.
	 * @param screenPoint the pixel point
	 * @param worldPoint  the corresponding world location.
	 * @return the sector [1..6] or -1 for none.
	 */
	@Override
	public int getSector(IContainer container, Point screenPoint, Point2D.Double worldPoint) {
		boolean positive = worldPoint.y > 0.0;
		switch (_displaySectors) {
		case SECTORS14:
			return positive ? 1 : 4;

		case SECTORS25:
			return positive ? 2 : 5;

		case SECTORS36:
			return positive ? 3 : 6;
		}
		return -1;
	}
	
	/**
	 * Is the sector one of the two on this view
	 * 
	 * @param sector the sector [1..6]
	 */
	public boolean containsSector(byte sector) {

		switch (_displaySectors) {
		case SECTORS14:
			return ((sector == 1) || (sector == 4));

		case SECTORS25:
			return ((sector == 2) || (sector == 5));

		case SECTORS36:
			return ((sector == 3) || (sector == 6));
		}

		return false;
	}
	
	/**
	 * Get the target z location.
	 * 
	 * @return the target position in cm on z axis
	 */
	public double getTargetZ() {
		return _targetZ;
	}
	
	/**
	 * Returns the absolute phi. This is the actual, global phi, e.g, -30 to 30 for
	 * sector 1, 30 to 90 for sector 2, etc.
	 * 
	 * @return the absolute phi from the relative phi
	 */
	public double getAbsolutePhi(IContainer container, Point screenPoint, Point2D.Double worldPoint) {
		int sector = getSector(container, screenPoint, worldPoint);
		// return (sector - 1) * 60.0 + _phiRelMidPlane;
		return (sector - 1) * 60.0 + getSliderPhi();
	}
	
	/**
	 * Get the rotation angle to transform from world coordinates to global
	 * coordinates. This is the sum of the phi for the upper sector of the view and
	 * the phi relative to the midplane.
	 * 
	 * @return the rotation angle to transform from world coordinates to global
	 *         coordinates, in degrees.
	 */
	public double getPhiRotate() {
		// double phiRotate = _phiRelMidPlane;
		double phiRotate = getSliderPhi();
		if (_displaySectors == DisplaySectors.SECTORS25) {
			phiRotate += 60.0;
		} else if (_displaySectors == DisplaySectors.SECTORS36) {
			phiRotate += 120.0;
		}
		return phiRotate;
	}

	public double getMidplanePhiRotate() {
		double phiRotate = 0;
		if (_displaySectors == DisplaySectors.SECTORS25) {
			phiRotate += 60.0;
		} else if (_displaySectors == DisplaySectors.SECTORS36) {
			phiRotate += 120.0;
		}
		return phiRotate;
	} 
	
	/**
	 * Get the relative phi value, int the range [-30, 30].
	 * 
	 * @return the phi value--the slider setting. This is between -30 and 30 for all
	 *         sectors--i.e., this is the relative phi, not the absolute phi.
	 */
	public double getSliderPhi() {
		// return _phiRelMidPlane;

		if (_controlPanel == null) {
			return 0.0;
		}
		return _controlPanel.getPhiSlider().getValue();
	}

	/**
	 * Get the relative phi (what the slider setting should be) corresponding to the
	 * absolute value of phi.
	 * 
	 * @param absPhi the value of phi in degrees, e.g., from a MC track.
	 * @return the corresponding slider value;
	 */
	protected double getRelativePhi(double absPhi) {
		while (absPhi < 360.0) {
			absPhi += 360.0;
		}

		while (absPhi > 30.0) {
			absPhi -= 60.0;
		}

		return absPhi;
	}

	/**
	 * Converts the local screen coordinate obtained by a previous localToWorld call
	 * to full 3D CLAS coordinates
	 * 
	 * @param screenPoint the pixel point
	 * @param worldPoint  the corresponding world location.
	 * @param result      holds the result. It has five elements. Cartesian x, y,
	 *                    and z are in 0, 1, and 2. Cylindrical rho and phi are in 3
	 *                    and 4. (And of course cylindrical z is the same as
	 *                    Cartesian z.)
	 */
	public void getCLASCordinates(IContainer container, Point screenPoint, Point2D.Double worldPoint, double result[]) {
		double x = worldPoint.y;
		double z = worldPoint.x;

		// we are essentially display a plan yp=0, with xp vertical and zp
		// horizontal. We need
		// to rotate around z by "phiRotate" to get the x and y coordinates.
		// Note
		// it is
		// a simple rotation since yp is zero
		double phiRotate = Math.toRadians(getPhiRotate());
		double y = x * Math.sin(phiRotate);
		x = x * Math.cos(phiRotate);

		double rho = x * x + y * y;
		rho = Math.sqrt(rho);

		// get absolute phi
		double absphi = getAbsolutePhi(container, screenPoint, worldPoint);

		result[0] = x;
		result[1] = y;
		result[2] = z;
		result[3] = rho;
		result[4] = absphi;
	}
	
	
	/**
	 * Are the given global x and y in this view? That is, does the sector they
	 * correspond to in this view based on the calculated phi?
	 * 
	 * @param x the global x
	 * @param y the global y in the same units as x
	 * @return <code>true</code> if the point is in
	 */
	public boolean inThisView(double x, double y) {
		if ((Math.abs(x) < TINY) && (Math.abs(y) < TINY)) {
			return true;
		}
		double tphi = Math.toDegrees(Math.atan2(y, x)) + 30;
		if (tphi < 0) {
			tphi = tphi + 360.0;
		}
		int sector = ((int) tphi) / 60;

		switch (sector) {
		case 0:
		case 3:
			return (_displaySectors == DisplaySectors.SECTORS14);
		case 1:
		case 4:
			return (_displaySectors == DisplaySectors.SECTORS25);
		case 2:
		case 5:
			return (_displaySectors == DisplaySectors.SECTORS36);
		default:
			System.err.println("Bad sector in inThisView: " + sector);
			return false;
		}
	}
	
	/**
	 * Check whether this sect is on this view
	 * 
	 * @param sector the sector [1..6]
	 * @return <code>true</code> if the sector is on the view.
	 */
	public boolean inThisView(int sector) {
		switch (_displaySectors) {
		case SECTORS14:
			return ((sector == 1) || (sector == 4));

		case SECTORS25:
			return ((sector == 2) || (sector == 5));

		case SECTORS36:
			return ((sector == 3) || (sector == 6));
		}

		return false;
	}
	
	/**
	 * Check whether a give phi is included in this view's range.
	 * 
	 * @param phi the value of phi on decimal degrees.
	 * @return <code>true</code> if it is included.
	 */
	public boolean inThisView(double phi) {
		while (phi < 0) {
			phi += 360.0;
		}
		switch (_displaySectors) {
		case SECTORS14:
			return between(phi, 330., 360.) || between(phi, 0., 30.) || between(phi, 150., 210.);

		case SECTORS25:
			return between(phi, 30., 90.) || between(phi, 210., 270.);

		case SECTORS36:
			return between(phi, 90., 150.) || between(phi, 270., 330.);
		}

		return false;
	}

	// convenience in-range test
	private boolean between(double x, double xmin, double xmax) {
		return (x >= xmin) && (x <= xmax);
	}



	/**
	 * Get the display sectors which tell us which pair of sectors are being
	 * displayed
	 * 
	 * @return the display sectors type
	 */
	public DisplaySectors getDisplaySectors() {
		return _displaySectors;
	}
	
	/**
	 * Convert world (not global, but graphical world) to clas global (lab)
	 * 
	 * @param wp     the world point
	 * @param labXYZ the clas global coordinates
	 */
	public void worldToLabXYZ(Point2D.Double wp, double[] labXYZ) {
		double perp = wp.y;
		double z = wp.x;

		// we are essentially display a plan yp=0, with xp vertical and zp
		// horizontal. We need to rotate around z by "phiRotate" to get the x
		// and y coordinates. Note
		// it is a simple rotation since yp is zero
		double phiRotate = Math.toRadians(getPhiRotate());
		labXYZ[0] = perp * Math.cos(phiRotate);
		labXYZ[1] = perp * Math.sin(phiRotate);
		labXYZ[2] = z;
	}
	
	/**
	 * Convert world (not global, but graphical world) to sector
	 * 
	 * @param wp        the world point
	 * @param sectorXYZ the sector coordinates
	 */
	public void worldToSector(Point2D.Double wp, double[] sectorXYZ) {
		double perp = wp.y;
		double sectz = wp.x;
		double sphi = Math.toRadians(getSliderPhi());
		if (perp < 0) {
			perp = -perp;
			sphi = -sphi;
		}
		double sectx = perp * Math.cos(sphi);
		double secty = perp * Math.sin(sphi);

		sectorXYZ[0] = sectx;
		sectorXYZ[1] = secty;
		sectorXYZ[2] = sectz;
	}
	/**
	 * From detector xyz get the projected world point.
	 * 
	 * @param x  the detector x coordinate
	 * @param y  the detector y coordinate
	 * @param z  the detector z coordinate
	 * @param wp the projected 2D world point.
	 */
	@Override
	public void projectClasToWorld(double x, double y, double z, Plane3D projectionPlane, Point2D.Double wp) {

		super.projectClasToWorld(x, y, z, projectionPlane, wp);
		int sector = GeometryManager.getSector(x, y);
		if (sector > 3) {
			wp.y = -wp.y;
		}
	}
	
	/**
	 * Some view specific feedback. Should always call super.getFeedbackStrings
	 * first.
	 * 
	 * @param container the base container for the view.
	 * @param pp        the pixel point
	 * @param wp        the corresponding world location.
	 */
	@Override
	public void getFeedbackStrings(IContainer container, Point pp, Point2D.Double wp, List<String> feedbackStrings) {
		super.getFeedbackStrings(container, pp, wp, feedbackStrings);
	}
	
	protected void commonFeedbackStrings(IContainer container, Point pp, Point2D.Double wp, List<String> feedbackStrings) {
		double result[] = new double[3];
		worldToLabXYZ(wp, result);
		float x = (float) result[0];
		float y = (float) result[1];
		float z = (float) result[2];

		String xyz = "xyz " + vecStr(result) + " cm";

		feedbackStrings.add(xyz);

		// anchor (urhere) feedback?
		YouAreHereItem item = getContainer().getYouAreHereItem();
		if (item != null) {
			Point2D.Double anchor = item.getFocus();
			String anchorStr = "$khaki$Dist from ref. point: " + valStr(anchor.distance(wp), 5) + " cm";
			feedbackStrings.add(anchorStr);
		}

		double rho = x * x + y * y;
		double r = Math.sqrt(rho + z * z);
		rho = Math.sqrt(rho);
		double theta = Math.toDegrees(Math.atan2(rho, z));

		// get absolute phi
		double absphi = getAbsolutePhi(container, pp, wp);

		String rtp = CedView.rThetaPhi + " (" + valStr(r, 2) + "cm, " + valStr(theta, 2) + UnicodeSupport.DEGREE + ", "
				+ valStr(absphi, 2) + UnicodeSupport.DEGREE + ")";
		feedbackStrings.add(rtp);

		// cylindrical coordinates which are just the world coordinates!
		String rzp = CedView.rhoZPhi + " (" + valStr(rho, 2) + "cm, " + valStr(z, 2) + "cm , " + valStr(absphi, 2)
				+ UnicodeSupport.DEGREE + ")";
		feedbackStrings.add(rzp);

		// sector coordinates
		worldToSector(wp, result);
		String sectxyz = "$yellow$Sector xyz " + vecStr(result) + " cm";
		feedbackStrings.add(sectxyz);

		// tilted sector
		sectorToTilted(result, result);
		String tiltsectxyz = "$yellow$Tilted sect xyz " + vecStr(result) + " cm";
		feedbackStrings.add(tiltsectxyz);

		if (_activeProbe != null) {
			float field[] = new float[3];
			_activeProbe.field(x, y, z, field);

			float grad[] = new float[3];
			_activeProbe.gradient(x, y, z, grad);

			// convert to Tesla from kG
			field[0] /= 10.0;
			field[1] /= 10.0;
			field[2] /= 10.0;

			// convert kG/cm to T/m
			grad[0] *= 10.0;
			grad[1] *= 10.0;
			grad[2] *= 10.0;

			double bmag = VectorSupport.length(field);
			double gmag = VectorSupport.length(grad);
			feedbackStrings.add("$Lawn Green$" + MagneticFields.getInstance().getActiveFieldDescription());

			boolean hasTorus = MagneticFields.getInstance().hasActiveTorus();
			boolean hasSolenoid = MagneticFields.getInstance().hasActiveSolenoid();

			// scale factors
			if (hasTorus || hasSolenoid) {
				String scaleStr = "";
				if (hasTorus) {
					double torusScale = MagneticFields.getInstance().getScaleFactor(FieldType.TORUS);
					scaleStr += "Torus scale " + valStr(torusScale, 3) + " ";
				}
				if (hasSolenoid) {
					double shiftZ = MagneticFields.getInstance().getShiftZ(FieldType.SOLENOID);
					String shiftStr = "Solenoid Z shift " + valStr(shiftZ, 3) + " cm ";
					feedbackStrings.add("$Lawn Green$" + shiftStr);

					double solenScale = MagneticFields.getInstance().getScaleFactor(FieldType.SOLENOID);
					scaleStr += "Solenoid scale " + valStr(solenScale, 3) + " ";
				}
				feedbackStrings.add("$Lawn Green$" + scaleStr);
			}

			feedbackStrings.add("$Lawn Green$Field " + valStr(bmag, 4) + " T " + vecStr(field) + " T");
			feedbackStrings.add("$Lawn Green$Grad " + valStr(gmag, 4) + " T/m " + vecStr(grad) + " T/m");
		} else {
			feedbackStrings.add("$Lawn Green$" + MagneticFields.getInstance().getActiveFieldDescription());
			feedbackStrings.add("$Lawn Green$Field is Zero");
		}
	}
	
	/**
	 * Returns a string representation of the form: "(x,y,z)".
	 * 
	 * @param numDec the number of decimal places for each coordinate.
	 * @return a String representation of the vector
	 */
	protected String vecStr(double v[]) {
		return "(" + DoubleFormat.doubleFormat(v[0], 2) + ", " + DoubleFormat.doubleFormat(v[1], 2) + ", "
				+ DoubleFormat.doubleFormat(v[2], 2) + ")";
	}

	/**
	 * Returns a string representation of the form: "(x,y,z)".
	 * 
	 * @param numDec the number of decimal places for each coordinate.
	 * @return a String representation of the vector
	 */
	protected String vecStr(float v[]) {
		return "(" + DoubleFormat.doubleFormat(v[0], 3) + ", " + DoubleFormat.doubleFormat(v[1], 3) + ", "
				+ DoubleFormat.doubleFormat(v[2], 3) + ")";
	}

	// convenience call for double formatter
	protected String valStr(double value, int numdec) {
		return DoubleFormat.doubleFormat(value, numdec);
	}



}
