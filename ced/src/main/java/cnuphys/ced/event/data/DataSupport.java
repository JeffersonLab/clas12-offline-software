package cnuphys.ced.event.data;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import org.jlab.geom.prim.Point3D;

import cnuphys.bCNU.log.Log;
import cnuphys.bCNU.util.Bits;
import cnuphys.bCNU.util.UnicodeSupport;
import cnuphys.bCNU.util.VectorSupport;
import cnuphys.ced.cedview.CedView;
import cnuphys.ced.geometry.PCALGeometry;
import cnuphys.ced.geometry.Transformations;
import cnuphys.lund.LundId;
import cnuphys.lund.LundSupport;
import cnuphys.splot.plot.DoubleFormat;

public class DataSupport {

	// for uniform feedback colors
	public static final String prelimColor = "$orange$";
	public static final String trueColor = "$Alice Blue$";
	public static final String dgtzColor = "$Moccasin$";
	public static final String reconColor = "$coral$";

	// ftof constants
	public static final int PANEL_1A = 0;
	public static final int PANEL_1B = 1;
	public static final int PANEL_2 = 2;
	public static final String panelNames[] = { "Panel 1A", "Panel 1B",
			"Panel 2" };

	// ec/pcal constants
	public static final int EC_OPTION = 0;
	public static final int PCAL_OPTION = 1;
	public static final String uvwStr[] = { "U", "V", "W" };
	public static final int FB_CLAS_XYZ = 01;
	public static final int FB_CLAS_RTP = 02; // r, theta, phi
	public static final int FB_TOTEDEP = 04; // total energy dep (MeV)
	public static final int FB_LOCAL_XYZ = 010;


	/**
	 * Add the pid information
	 * 
	 * @param pid the pid.array
	 * @param hitIndex the hit index
	 * @param feedbackStrings the collection of feedback strings
	 */
	public static void truePidFeedback(int pid[], int hitIndex,
			List<String> feedbackStrings) {

		if ((pid == null) || (hitIndex < 0) || (hitIndex >= pid.length)) {
			return;
		}

		feedbackStrings.add(trueColor + "pid " + pidStr(pid[hitIndex]));

	}

	/**
	 * Add some dgtz hit feedback for ec and pcal
	 * 
	 * @param hitIndex the hit index
	 * @param option determines which ec or pcal
	 * @param feedbackStrings the collection of feedback strings
	 */

	public static void ecDgtzFeedback(int hitIndex, int option,
			List<String> feedbackStrings) {

		if (hitIndex < 0) {
			return;
		}

		int hitn[] = null;
		int ADC[] = null;
		int TDC[] = null;

		if (option == EC_OPTION) {
			hitn = EC.hitn();
			ADC = EC.ADC();
			TDC = EC.TDC();
		}
		else {
			hitn = PCAL.hitn();
			ADC = PCAL.ADC();
			TDC = PCAL.TDC();
		}

		String hitStr = safeString(hitn, hitIndex);
		String adcStr = safeString(ADC, hitIndex);
		String tdcStr = safeString(TDC, hitIndex);

		feedbackStrings.add(dgtzColor + "adc " + adcStr + "  tdc " + tdcStr
				+ " ns" + "  hit " + hitStr);

	}

	/**
	 * Add some dgtz hit feedback for ftof
	 * 
	 * @param hitIndex the hit index
	 * @param option determines which ftof panel
	 * @param feedbackStrings the collection of feedback strings
	 */
	public static void ftofDgtzFeedback(int hitIndex, int option,
			List<String> feedbackStrings) {

		if (hitIndex < 0) {
			return;
		}

		int sector[] = null;
		int paddle[] = null;
		int ADCL[] = null;
		int ADCR[] = null;
		int TDCL[] = null;
		int TDCR[] = null;

		switch (option) {
		case PANEL_1A:
			sector = ColumnData.getIntArray("FTOF1A::dgtz.sector");
			if ((sector == null) || (hitIndex >= sector.length)) {
				return;
			}
			paddle = ColumnData.getIntArray("FTOF1A::dgtz.paddle");
			ADCL = ColumnData.getIntArray("FTOF1A::dgtz.ADCL");
			ADCR = ColumnData.getIntArray("FTOF1A::dgtz.ADCR");
			TDCL = ColumnData.getIntArray("FTOF1A::dgtz.TDCL");
			TDCR = ColumnData.getIntArray("FTOF1A::dgtz.TDCR");
			break;

		case PANEL_1B:
			sector = ColumnData.getIntArray("FTOF1B::dgtz.sector");
			if ((sector == null) || (hitIndex >= sector.length)) {
				return;
			}
			paddle = ColumnData.getIntArray("FTOF1B::dgtz.paddle");
			ADCL = ColumnData.getIntArray("FTOF1B::dgtz.ADCL");
			ADCR = ColumnData.getIntArray("FTOF1B::dgtz.ADCR");
			TDCL = ColumnData.getIntArray("FTOF1B::dgtz.TDCL");
			TDCR = ColumnData.getIntArray("FTOF1B::dgtz.TDCR");
			break;

		case PANEL_2:
			sector = ColumnData.getIntArray("FTOF2B::dgtz.sector");
			paddle = ColumnData.getIntArray("FTOF2B::dgtz.paddle");
			ADCL = ColumnData.getIntArray("FTOF2B::dgtz.ADCL");
			ADCR = ColumnData.getIntArray("FTOF2B::dgtz.ADCR");
			TDCL = ColumnData.getIntArray("FTOF2B::dgtz.TDCL");
			TDCR = ColumnData.getIntArray("FTOF2B::dgtz.TDCR");
			break;
		}

		if ((sector == null) || (paddle == null) || (ADCL == null)
				|| (ADCR == null) || (TDCL == null) || (TDCR == null)) {
			return;
		}

		feedbackStrings.add(dgtzColor + "panel_1B  sector " + sector[hitIndex]
				+ "  paddle " + paddle[hitIndex]);

		feedbackStrings.add(dgtzColor + "adc_left " + ADCL[hitIndex]
				+ "  adc_right " + ADCR[hitIndex]);
		feedbackStrings.add(dgtzColor + "tdc_left " + TDCL[hitIndex]
				+ "  tdc_right " + TDCR[hitIndex]);

	}


	/**
	 * Some true feedback for ec and pcal
	 * 
	 * @param hitIndex
	 * @param option determines whether ec or pcal
	 * @param bits controls what is displayed
	 * @param trans a geometry package transformation object
	 * @return a list of feedback strings
	 */
	public static List<String> ecGemcHitFeedback(int hitIndex, int option,
			int bits, Transformations trans) {

		Vector<String> fbs = new Vector<String>();

		if (hitIndex < 0) {
			return fbs;
		}

		int pid[] = null;
		double totEdep[] = null;
		double avgX[] = null;
		double avgY[] = null;
		double avgZ[] = null;

		if (option == EC_OPTION) {
			pid = EC.pid();
			totEdep = EC.totEdep();
			avgX = EC.avgX();
			avgY = EC.avgY();
			avgZ = EC.avgZ();
		}
		else {
			pid = PCAL.pid();
			totEdep = PCAL.totEdep();
			avgX = PCAL.avgX();
			avgY = PCAL.avgY();
			avgZ = PCAL.avgZ();
		}

		int trueCount = (avgX == null) ? 0 : avgX.length;
		if (hitIndex >= trueCount) {
			Log.getInstance()
					.warning("gemcHitFeedback index out of range: " + hitIndex);
			return fbs;
		}

		// some preliminaries
		double labXYZ[] = new double[3];
		labXYZ[0] = avgX[hitIndex] / 10; // mm to cm
		labXYZ[1] = avgY[hitIndex] / 10;
		labXYZ[2] = avgZ[hitIndex] / 10;

		int pdgid = 0;

		if ((pid != null) && (hitIndex < pid.length)) {
			pdgid = pid[hitIndex];
		}

		String prefix = trueColor + "Gemc Hit [" + (hitIndex + 1) + "] "
				+ pidStr(pdgid);

		try {
			if (Bits.checkBit(bits, FB_CLAS_XYZ)) {
				fbs.add(vecStr(prefix + "clas xyz", labXYZ, 2, "cm"));
			}

			if (Bits.checkBit(bits, FB_CLAS_RTP)) {
				fbs.add(sphericalStr(prefix, labXYZ, 3));
			}

			if (Bits.checkBit(bits, FB_LOCAL_XYZ)) {
				if (trans != null) {
					Point3D clasP = new Point3D(labXYZ[0], labXYZ[1],
							labXYZ[2]);
					Point3D localP = new Point3D();
					PCALGeometry.getTransformations().clasToLocal(localP,
							clasP);
					fbs.add(p3dStr(prefix + "loc xyz", localP, 2, "cm"));
				}
			}

			if ((totEdep != null) && Bits.checkBit(bits, FB_TOTEDEP)) {
				if (hitIndex < totEdep.length) {
					fbs.add(scalarStr(prefix + "tot edep", totEdep[hitIndex], 2,
							"MeV"));
				}
			}

		} catch (Exception e) {
			Log.getInstance().exception(e);
		}

		return fbs;
	}

	/**
	 * Some preliminary feedback for EC and PCAL
	 * 
	 * @param hitIndex the hit index
	 * @param option determines which ec or pcal
	 * @param feedbackStrings the collection of feedback strings
	 */
	public static void ecPreliminaryFeedback(int hitIndex, int option,
			List<String> feedbackStrings) {

		if (hitIndex < 0) {
			return;
		}

		int view[] = null;
		int strip[] = null;

		if (option == EC_OPTION) {
			view = EC.view();
			strip = EC.strip();
		}
		else {
			view = PCAL.view();
			strip = PCAL.strip();
		}

		if ((view == null) || (hitIndex >= view.length)) {
			return;
		}

		feedbackStrings.add("==== " + uvwStr[view[hitIndex] - 1] + " strip "
				+ strip[hitIndex] + " ====");

		double avgX[] = null;
		double avgY[] = null;
		double avgZ[] = null;
		double avgLx[] = null;
		double avgLy[] = null;
		double avgLz[] = null;

		if (option == EC_OPTION) {
			avgX = EC.avgX();
			avgY = EC.avgY();
			avgZ = EC.avgZ();
			avgLx = EC.avgLx();
			avgLy = EC.avgLy();
			avgLz = EC.avgLz();
		}
		else {
			avgX = PCAL.avgX();
			avgY = PCAL.avgY();
			avgZ = PCAL.avgZ();
			avgLx = PCAL.avgLx();
			avgLy = PCAL.avgLy();
			avgLz = PCAL.avgLz();
		}

		addXYZFeedback(hitIndex, avgX, avgY, avgZ, avgLx, avgLy, avgLz,
				feedbackStrings);
	}

	private static void addXYZFeedback(int hitIndex, double x[], double y[],
			double z[], double lx[], double ly[], double lz[],
			List<String> feedbackStrings) {

		if ((x != null) && (y != null) && (z != null)
				&& (hitIndex < x.length)) {
			double v[] = new double[3];
			// to cm
			v[0] = x[hitIndex] / 10;
			v[1] = y[hitIndex] / 10;
			v[2] = z[hitIndex] / 10;
			feedbackStrings.add(trueColor + "hit global xyz "
					+ VectorSupport.toString(v, 2));
		}

		if ((lx != null) && (ly != null) && (lz != null)
				&& (hitIndex < lx.length)) {
			double v[] = new double[3];
			// to cm
			v[0] = lx[hitIndex] / 10;
			v[1] = ly[hitIndex] / 10;
			v[2] = lz[hitIndex] / 10;
			feedbackStrings.add(trueColor + "hit local xyz "
					+ VectorSupport.toString(v, 2));
		}
	}

	/**
	 * Safe way to get an integer element from an array for printing
	 * 
	 * @param array the array in question
	 * @param index the array index
	 * @return a string for printing
	 */
	public static String safeString(int[] array, int index) {
		if (array == null) {
			return "null";
		}
		else if ((index < 0) || (index >= array.length)) {
			return "BADIDX: " + index;
		}
		else {
			return "" + array[index];
		}
	}

	/**
	 * Safe way to get an double element from an array for printing
	 * 
	 * @param array the array in question
	 * @param index the array index
	 * @param numdec the number of decimal points
	 * @return a string for printing
	 */
	public static String safeString(double[] array, int index, int numdec) {
		if (array == null) {
			return "null";
		}
		else if ((index < 0) || (index >= array.length)) {
			return "BADIDX: " + index;
		}
		else {
			return DoubleFormat.doubleFormat(array[index], numdec);
		}
	}

	/**
	 * Get a string for the particle Id
	 * 
	 * @param pid the Lund particle Id
	 * @return the string representation of the particle Id
	 */
	private static String pidStr(int pid) {

		if (pid == 0) {
			return "";
		}
		LundId lid = LundSupport.getInstance().get(pid);

		if (lid == null) {
			return "(" + pid + ") ??";
		}
		else {
			return lid.getName();
		}
	}

	/**
	 * Returns a string representation of the form: "(x,y,z)".
	 * 
	 * @param v the vector (any double array)
	 * @param numDec the number of decimal places for each coordinate.
	 * @return a String representation of the vector
	 */
	public static String vecStr(String prefix, double v[], int numDec,
			String postfix) {

		StringBuilder sb = new StringBuilder(128);
		int lm1 = v.length - 1;

		if (prefix != null) {
			sb.append(prefix + " ");
		}

		sb.append("(");

		for (int i = 0; i <= lm1; i++) {
			sb.append(DoubleFormat.doubleFormat(v[i], numDec));
			if (i != lm1) {
				sb.append(", ");
			}
		}

		sb.append(")");

		if (postfix != null) {
			sb.append(" " + postfix);
		}

		return sb.toString();
	}

	/**
	 * Returns a string representation of the form: "(x,y,z)".
	 * 
	 * @param p3d the 3D point
	 * @param numDec the number of decimal places for each coordinate.
	 * @return a String representation of the vector
	 */
	public static String p3dStr(String prefix, Point3D p3d, int numDec,
			String postfix) {

		StringBuilder sb = new StringBuilder(128);

		if (prefix != null) {
			sb.append(prefix + " ");
		}

		sb.append("(");

		sb.append(DoubleFormat.doubleFormat(p3d.x(), numDec) + ", ");
		sb.append(DoubleFormat.doubleFormat(p3d.y(), numDec) + ", ");
		sb.append(DoubleFormat.doubleFormat(p3d.z(), numDec));

		sb.append(")");

		if (postfix != null) {
			sb.append(" " + postfix);
		}

		return sb.toString();
	}

	/**
	 * Returns a string representation of the form: "(x,y,z)".
	 * 
	 * @param xyz the Cartesian coordinates in cm
	 * @param numDec the number of decimal places for each value.
	 * @return a String representation of the vector in spherical coordinates
	 */
	public static String sphericalStr(String prefix, double xyz[], int numDec) {
		double r = Math
				.sqrt(xyz[0] * xyz[0] + xyz[1] * xyz[1] + xyz[2] * xyz[2]);
		double theta = Math.toDegrees(Math.acos(xyz[2] / r));
		double phi = Math.toDegrees(Math.atan2(xyz[1], xyz[0]));

		StringBuilder sb = new StringBuilder(128);

		if (prefix != null) {
			sb.append(prefix + " ");
		}

		sb.append(CedView.rThetaPhi + " (");

		sb.append(DoubleFormat.doubleFormat(r, numDec) + "cm, ");
		sb.append(DoubleFormat.doubleFormat(theta, numDec));
		sb.append(UnicodeSupport.DEGREE + ", ");
		sb.append(DoubleFormat.doubleFormat(phi, numDec));
		sb.append(UnicodeSupport.DEGREE + ", ");
		sb.append(")");

		return sb.toString();

	}

	/**
	 * Returns a string representation of the form: "(x,y,z)".
	 * 
	 * @param v the value
	 * @param numDec the number of decimal places for each coordinate.
	 * @return a String representation of the vector
	 */
	public static String scalarStr(String prefix, double v, int numDec,
			String postfix) {

		StringBuilder sb = new StringBuilder(128);

		if (prefix != null) {
			sb.append(prefix + " ");
		}

		sb.append(DoubleFormat.doubleFormat(v, numDec));

		if (postfix != null) {
			sb.append(" " + postfix);
		}

		return sb.toString();
	}

	/**
	 * Safe way to get an int value
	 * 
	 * @param fullname the full name of the column
	 * @param index the index
	 * @return -2147483648 [0x80000000] or -2^31 on any error otherwise the
	 *         value
	 */
	public static int getInt(String fullName, int index) {

		ColumnData cd = ColumnData.getColumnData(fullName);
		if (cd == null) {
			return Integer.MIN_VALUE;
		}

		Object oa = cd.getDataArray();
		if ((oa == null) || !(oa instanceof int[])) {
			return Integer.MIN_VALUE;
		}

		int[] array = (int[]) oa;

		if ((index < 0) || (index >= array.length)) {
			return Integer.MIN_VALUE;
		}
		return array[index];
	}

	/**
	 * Safe way to get an double value
	 * 
	 * @param fullname the full name of the column
	 * @param index the index
	 * @return Double.NaN on any error otherwise the value
	 */
	public static double getDouble(String fullName, int index) {

		ColumnData cd = ColumnData.getColumnData(fullName);
		if (cd == null) {
			return Double.NaN;
		}

		Object oa = cd.getDataArray();
		if ((oa == null) || !(oa instanceof double[])) {
			return Double.NaN;
		}

		double[] array = (double[]) oa;

		if ((index < 0) || (index >= array.length)) {
			return Double.NaN;
		}
		return array[index];
	}


	/**
	 * Get the number of reconstructed crosses for bmt
	 * 
	 * @return the number of reconstructed crosses for bmy
	 */
	public static int bmtGetCrossCount() {
		int sector[] = ColumnData.getIntArray("BMTRec::Crosses.sector");
		return (sector == null) ? 0 : sector.length;
	}

	/**
	 * Get the number of reconstructed crosses
	 * 
	 * @return the number of reconstructed crosses
	 */
	public static int bstGetCrossCount() {
		int sector[] = ColumnData.getIntArray("BSTRec::Crosses.sector");
		return (sector == null) ? 0 : sector.length;
	}

	/**
	 * Get the name from the panel type
	 * 
	 * @param panelType the panel type
	 * @return the name of the panel type
	 */
	public static String ftofGetName(int panelType) {
		if ((panelType < 0) || (panelType > 2)) {
			return "???";
		}
		else {
			return panelNames[panelType];
		}
	}

	/**
	 * Get the index of the ftof hit
	 * 
	 * @param sect the 1-based sector
	 * @param paddle the 1-based paddle
	 * @return the index of a hit with these parameters, or -1 if not found
	 */
	public static int ftofGetHitIndex(int sect, int paddle, int panelType) {

		int sector[] = null;
		int paddles[] = null;

		switch (panelType) {
		case PANEL_1A:
			sector = ColumnData.getIntArray("FTOF1A::dgtz.sector");
			paddles = ColumnData.getIntArray("FTOF1A::dgtz.paddle");
			break;
		case PANEL_1B:
			sector = ColumnData.getIntArray("FTOF1B::dgtz.sector");
			paddles = ColumnData.getIntArray("FTOF1B::dgtz.paddle");
			break;
		case PANEL_2:
			sector = ColumnData.getIntArray("FTOF2B::dgtz.sector");
			paddles = ColumnData.getIntArray("FTOF2B::dgtz.paddle");
			break;
		}

		if (sector == null) {
			return -1;
		}

		if (paddles == null) {
			Log.getInstance()
					.warning("null paddles array in FTOFDataContainer");
			return -1;
		}

		for (int i = 0; i < ftofGetHitCount(panelType); i++) {
			if ((sect == sector[i]) && (paddle == paddles[i])) {
				// System.err.println("Computed Hit Index: " + i + " out of " +
				// getHitCount(panelType) + " panelType: " + panelType);
				return i;
			}
		}
		return -1;
	}

	/**
	 * Get the hit count for an ftof panel
	 * 
	 * @param option specifies the panel
	 * @return the hit count
	 */
	public static int ftofGetHitCount(int option) {
		int sector[] = null;

		switch (option) {
		case PANEL_1A:
			sector = ColumnData.getIntArray("FTOF1A::dgtz.sector");
			break;
		case PANEL_1B:
			sector = ColumnData.getIntArray("FTOF1B::dgtz.sector");
			break;
		case PANEL_2:
			sector = ColumnData.getIntArray("FTOF2B::dgtz.sector");
			break;
		}

		return (sector == null) ? 0 : sector.length;
	}

	
	/**
	 * Get the hit count for bst 
	 * @return the hit count
	 */
	public static int bstGetHitCount() {
		int sector[] = ColumnData.getIntArray("BST::dgtz.sector");
		return (sector == null) ? 0 : sector.length;
	}
	
	
	/**
	 * Get a collection of all strip, adc doublets for a given sector and layer
	 * 
	 * @param sector the 1-based sector
	 * @param layer the 1-based layer
	 * @return a collection of all strip, adc doublets for a given sector and
	 *         layer. It is a collection of integer arrays. For each array, the
	 *         0 entry is the 1-based strip and the 1 entry is the adc.
	 */
	public static Vector<int[]> bstAllStripsForSectorAndLayer(int sector,
			int layer) {
		Vector<int[]> strips = new Vector<int[]>();

		int sect[] = ColumnData.getIntArray("BST::dgtz.sector");

		if (sect != null) {
			int lay[] = ColumnData.getIntArray("BST::dgtz.layer");
			int strip[] = ColumnData.getIntArray("BST::dgtz.strip");
			int ADC[] = ColumnData.getIntArray("BST::dgtz.ADC");

			for (int hitIndex = 0; hitIndex < sect.length; hitIndex++) {
				if ((sect[hitIndex] == sector) && (lay[hitIndex] == layer)) {
					int data[] = { strip[hitIndex], ADC[hitIndex] };
					strips.add(data);
				}
			}
		}

		// sort based on strips
		if (strips.size() > 1) {
			Comparator<int[]> c = new Comparator<int[]>() {

				@Override
				public int compare(int[] o1, int[] o2) {
					return Integer.compare(o1[0], o2[0]);
				}
			};

			Collections.sort(strips, c);
		}

		return strips;
	}
	

	/**
	 * Get the index of the dc hit
	 * 
	 * @param sect
	 *            the 1-based sector
	 * @param stack
	 *            the 1-based stack (inner = 1, outer = 2) index
	 * @param view
	 *            the 1-based strip type (u, v, w) = (1, 2, 3)
	 * @param strip
	 *            the 1-based strip
	 * @param option
	 *            either EC_OPTION or PCAL_OPTION
	 * @return the index of a hit with these parameters, or -1 if not found
	 */
	public static Vector<HitRecord> ecGetMatchingHits(int sect, int stack, int view,
			int strip, int option) {

		
		int hitCount =  ecGetHitCount(option);
		if (hitCount < 1) {
			return null;
		}
		
		int sectors[] = null;
		int views[] = null;
		int stacks[] = null;
		int strips[] = null;
		double avgX[] = null;
		double avgY[] = null;
		double avgZ[] = null;
		
		Vector<HitRecord> hits = new Vector<HitRecord>();
		
		if (option == EC_OPTION) {
			sectors = EC.sector();
			views = EC.view();
			stacks = EC.stack();
			strips = EC.strip();
			avgX = EC.avgX();
			avgY = EC.avgY();
			avgZ = EC.avgZ();
		}
		else {  //PCAL
			sectors = PCAL.sector();
			views = PCAL.view();
			stacks = PCAL.stack(); //all 1's
			strips = PCAL.strip();
			avgX = PCAL.avgX();
			avgY = PCAL.avgY();
			avgZ = PCAL.avgZ();
		}
		
		for (int i = 0; i < hitCount; i++) {
			if ((sect == sectors[i]) && (stack == stacks[i])
					&& (view == views[i])
					&& (strip == strips[i])) {
				hits.add(new HitRecord(avgX, avgY, avgZ,
						i, sect, stack, view, strip));
			}

		}
		return hits;
	}
	
	/**
	 * Get the ec or pcal hit count
	 * 
	 * @param option specifies ec or pcal
	 * @return the hit count
	 */
	public static int ecGetHitCount(int option) {
		// option= 0 for ec, else pcal
		int count = 0;

		if (option == EC_OPTION) {
			count = EC.hitCount();
		}
		else if (option == PCAL_OPTION) {
			count = PCAL.hitCount();
		}
		return count;
	}


}
