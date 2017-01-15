package cnuphys.ced.event.data;

import java.awt.Color;
import java.util.List;
import java.util.Vector;

import org.jlab.geom.prim.Point3D;

import cnuphys.bCNU.graphics.colorscale.ColorScaleModel;
import cnuphys.bCNU.log.Log;
import cnuphys.bCNU.util.Bits;
import cnuphys.ced.alldata.ColumnData;
import cnuphys.ced.geometry.PCALGeometry;
import cnuphys.ced.geometry.Transformations;

public class HTCC {

	public static ColorScaleModel colorScaleModel = new ColorScaleModel(
			getNPhEValues(), getNPheColors());
	
	static {
		colorScaleModel.setTooBigColor(Color.red);
	}

	/**
	 * Get the pid array from the true data
	 * @return the pid array
	 */
	public static int[] pid() {
		return ColumnData.getIntArray("HTCC::true.pid");
	}

	/**
	 * Get the hitn array from the dgtz data
	 * @return the hitn array
	 */
	public static int[] hitn() {
		return ColumnData.getIntArray("HTCC::dgtz.hitn");
	}

	
	/**
	 * Get the sector array from the dgtz data
	 * @return the sector array
	 */
	public static int[] sector() {
		return ColumnData.getIntArray("HTCC::dgtz.sector");
	}

	/**
	 * Get the ring array from the dgtz data
	 * @return the ring array
	 */
	public static int[] ring() {
		return ColumnData.getIntArray("HTCC::dgtz.ring");
	}
	/**
	 * Get the half array from the dgtz data
	 * @return the half array
	 */
	public static int[] half() {
		return ColumnData.getIntArray("HTCC::dgtz.half");
	}
	
	/**
	 * Get the nphe (num phot electrons) array from the dgtz data
	 * @return the nphe array
	 */
	public static int[] nphe() {
		return ColumnData.getIntArray("HTCC::dgtz.nphe");
	}
	
	/**
	 * Get the time array from the dgtz data
	 * @return the time array
	 */
	public static double[] time() {
		return ColumnData.getDoubleArray("HTCC::dgtz.time");
	}
	
	/**
	 * Get the avgX array from the true data
	 * @return the avgX array
	 */
	public static double[] avgX() {
		return ColumnData.getDoubleArray("HTCC::true.avgX");
	}

	/**
	 * Get the avgY array from the true data
	 * @return the avgY array
	 */
	public static double[] avgY() {
		return ColumnData.getDoubleArray("HTCC::true.avgY");
	}
	
	/**
	 * Get the avgZ array from the true data
	 * @return the avgZ array
	 */
	public static double[] avgZ() {
		return ColumnData.getDoubleArray("HTCC::true.avgZ");
	}
	
	/**
	 * Get the avgLx array from the true data
	 * @return the avgLx array
	 */
	public static double[] avgLx() {
		return ColumnData.getDoubleArray("HTCC::true.avgLx");
	}

	/**
	 * Get the avgLy array from the true data
	 * @return the avgLy array
	 */
	public static double[] avgLy() {
		return ColumnData.getDoubleArray("HTCC::true.avgLy");
	}
	
	/**
	 * Get the avgLz array from the true data
	 * @return the avgLz array
	 */
	public static double[] avgLz() {
		return ColumnData.getDoubleArray("HTCC::true.avgLz");
	}


	/**
	 * Get the totEdep array from the true data
	 * @return the totEdep array
	 */
	public static double[] totEdep() {
		return ColumnData.getDoubleArray("HTCC::true.totEdep");
	}
	
	/**
	 * Get the hit count 
	 * @return the hit count
	 */
	public static int hitCount() {
		int sector[] = sector();
		return (sector == null) ? 0 : sector.length;
	}


	/**
	 * Add some dgtz hit feedback for ec and pcal
	 * 
	 * @param hitIndex the hit index
	 * @param feedbackStrings the collection of feedback strings
	 */
	public static void dgtzFeedback(int hitIndex, List<String> feedbackStrings) {

		if (hitIndex < 0) {
			return;
		}

		int hitn[] = hitn();
		int nphe[] = nphe();
		double time[] = time();

		String hitStr = DataSupport.safeString(hitn, hitIndex);
		String npheStr = DataSupport.safeString(nphe, hitIndex);
		String timeStr = DataSupport.safeString(time, hitIndex, 2);

		feedbackStrings.add(DataSupport.dgtzColor + "hitn " + hitStr + "  nphe " + npheStr
			 + "  time " + timeStr);

	}
	
	/**
	 * Get the index of the HTCC hit
	 * 
	 * @param sect
	 *            the 1-based sector
	 * @param ring
	 *            the 1-based ring [1..4]
	 * @param half
	 *            the 1-based half [1..2]
	 * @return the list of matching hits
	 */
	public static Vector<HitRecord> matchingHits(int sect, int ring, int half) {

		
		int hitCount =  hitCount();
		if (hitCount < 1) {
			return null;
		}
				
		Vector<HitRecord> hits = new Vector<HitRecord>();
		
		int sectors[] = sector();
		int rings[] = ring();
		int halfs[] = half();
		double avgX[] = avgX();
		double avgY[] = avgY();
		double avgZ[] = avgZ();
		
		for (int i = 0; i < hitCount; i++) {
			if ((sect == sectors[i]) && (ring == rings[i])
					&& (half == halfs[i])) {
				hits.add(new HitRecord(avgX, avgY, avgZ,
						i, sect, ring, half));
			}

		}
		return hits;
	}
	/**
	 * Some true feedback for htcc
	 * 
	 * @param hitIndex
	 * @param bits controls what is displayed
	 * @param trans a geometry package transformation object
	 * @return a list of feedback strings
	 */
	public static List<String> gemcHitFeedback(int hitIndex,
			int bits, Transformations trans) {

		Vector<String> fbs = new Vector<String>();

		if (hitIndex < 0) {
			return fbs;
		}

		int pid[] = pid();
		double totEdep[] = totEdep();
		double avgX[] = avgX();
		double avgY[] = avgY();
		double avgZ[] = avgZ();

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

		String prefix = DataSupport.trueColor + "Gemc Hit [" + (hitIndex + 1) + "] "
				+ DataSupport.pidStr(pdgid);

		try {
			if (Bits.checkBit(bits, DataSupport.FB_CLAS_XYZ)) {
				fbs.add(DataSupport.vecStr(prefix + "clas xyz", labXYZ, 2, "cm"));
			}

			if (Bits.checkBit(bits, DataSupport.FB_CLAS_RTP)) {
				fbs.add(DataSupport.sphericalStr(prefix, labXYZ, 3));
			}

			if (Bits.checkBit(bits, DataSupport.FB_LOCAL_XYZ)) {
				if (trans != null) {
					Point3D clasP = new Point3D(labXYZ[0], labXYZ[1],
							labXYZ[2]);
					Point3D localP = new Point3D();
					PCALGeometry.getTransformations().clasToLocal(localP,
							clasP);
					fbs.add(DataSupport.p3dStr(prefix + "loc xyz", localP, 2, "cm"));
				}
			}

			if ((totEdep != null) && Bits.checkBit(bits, DataSupport.FB_TOTEDEP)) {
				if (hitIndex < totEdep.length) {
					fbs.add(DataSupport.scalarStr(prefix + "tot edep", totEdep[hitIndex], 2,
							"MeV"));
				}
			}

		} catch (Exception e) {
			Log.getInstance().exception(e);
		}

		return fbs;
	}
	/**
	 * Some preliminary feedback for HTCC
	 * 
	 * @param hitIndex the hit index
	 * @param feedbackStrings the collection of feedback strings
	 */
	public static void preliminaryFeedback(int hitIndex,
			List<String> feedbackStrings) {

		if (hitIndex < 0) {
			return;
		}

		double avgX[] = avgX();
		double avgY[] = avgY();
		double avgZ[] = avgZ();
		double avgLx[] = avgLx();
		double avgLy[] = avgLy();
		double avgLz[] = avgLz();

		DataSupport.addXYZFeedback(hitIndex, avgX, avgY, avgZ, avgLx, avgLy, avgLz,
				feedbackStrings);
	}
	

	/**
	 * Get the values array for the color scale. Note the range is 0..1 so use
	 * fraction of max value to get color
	 * 
	 * @return the values array.
	 */
	
	private static double getNPhEValues()[] {

		double MAXNUMPHOTOELEC = 40.;

		int len = getNPheColors().length + 1;

		double values[] = new double[len];

		double min = 0.0;
		double max = MAXNUMPHOTOELEC;
		double del = (max - min) / (values.length - 1);
		for (int i = 0; i < values.length; i++) {
			values[i] = i * del;
		}
		return values;
	}

	/**
	 * Get the color array for the plot.
	 * 
	 * @return the color array for the plot.
	 */
	private static Color getNPheColors()[] {

		int r[] = { 176, 124, 173, 255, 255, 255, 255};
		int g[] = { 224, 255, 255, 255, 165, 69, 0};
		int b[] = { 230, 0, 47, 0, 0, 0, 0};

		int n = 4;

		double f = 1.0 / n;

		int len = r.length;
		int colorlen = (len - 1) * n + 1;
		Color colors[] = new Color[colorlen];

		int k = 0;
		for (int i = 0; i < (len - 1); i++) {
			for (int j = 0; j < n; j++) {
				int rr = r[i] + (int) (j * f * (r[i + 1] - r[i]));
				int gg = g[i] + (int) (j * f * (g[i + 1] - g[i]));
				int bb = b[i] + (int) (j * f * (b[i + 1] - b[i]));
				colors[k] = new Color(rr, gg, bb);
				k++;
			}
		}

		colors[(len - 1) * n] = new Color(r[len - 1], g[len - 1], b[len - 1]);
		return colors;
	}


}
