package cnuphys.ced.event.data;

import java.util.List;
import java.util.Vector;

import org.jlab.geom.prim.Point3D;

import cnuphys.bCNU.log.Log;
import cnuphys.bCNU.util.Bits;
import cnuphys.ced.alldata.ColumnData;
import cnuphys.ced.geometry.PCALGeometry;
import cnuphys.ced.geometry.Transformations;

/**
 * static methods to centralize getting data arrays
 * @author heddle
 *
 */
public class ECAL {

	/**
	 * Get the pid array from the true data
	 * @return the pid array
	 */
	public static int[] pid() {
		return ColumnData.getIntArray("ECAL::true.pid");
	}
	
	/**
	 * Get the sector array from the dgtz array
	 * @return the sector array
	 */
	public static int[] sector() {
		return ColumnData.getIntArray("ECAL::dgtz.sector");
	}
	
	/**
	 * Get the stack array from the dgtz array
	 * @return the stack array
	 */
	public static int[] stack() {
		return ColumnData.getIntArray("ECAL::dgtz.stack");
	}
	
	/**
	 * Get the view array from the dgtz array
	 * @return the view array
	 */
	public static int[] view() {
		return ColumnData.getIntArray("ECAL::dgtz.view");
	}

	/**
	 * Get the strip array from the dgtz array
	 * @return the strip array
	 */
	public static int[] strip() {
		return ColumnData.getIntArray("ECAL::dgtz.strip");
	}
	
	/**
	 * Get the totEdep array from the true data
	 * @return the totEdep array
	 */
	public static double[] totEdep() {
		return ColumnData.getDoubleArray("ECAL::true.totEdep");
	}
	
	/**
	 * Get the avgX array from the true data
	 * @return the avgX array
	 */
	public static double[] avgX() {
		return ColumnData.getDoubleArray("ECAL::true.avgX");
	}
	
	/**
	 * Get the avgY array from the true data
	 * @return the avgY array
	 */
	public static double[] avgY() {
		return ColumnData.getDoubleArray("ECAL::true.avgY");
	}
	
	/**
	 * Get the avgZ array from the true data
	 * @return the avgZ array
	 */
	public static double[] avgZ() {
		return ColumnData.getDoubleArray("ECAL::true.avgZ");
	}
	
	/**
	 * Get the avgLx array from the true data
	 * @return the avgLx array
	 */
	public static double[] avgLx() {
		return ColumnData.getDoubleArray("ECAL::true.avgLx");
	}
	
	/**
	 * Get the avgLy array from the true data
	 * @return the avgLy array
	 */
	public static double[] avgLy() {
		return ColumnData.getDoubleArray("ECAL::true.avgLy");
	}
	
	/**
	 * Get the avgLz array from the true data
	 * @return the avgLz array
	 */
	public static double[] avgLz() {
		return ColumnData.getDoubleArray("ECAL::true.avgLz");
	}

	/**
	 * Get the hitn array from the dgtz data
	 * @return the hitn array
	 */
	public static int[] hitn() {
		return ColumnData.getIntArray("ECAL::dgtz.hitn");
	}
	
	/**
	 * Get the ADC array from the dgtz data
	 * @return the ADC array
	 */
	public static int[] ADC() {
		return ColumnData.getIntArray("ECAL::dgtz.ADC");
	}
	
	/**
	 * Get the TDC array from the dgtz data
	 * @return the TDC array
	 */
	public static int[] TDC() {
		return ColumnData.getIntArray("ECAL::dgtz.TDC");
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
	 * Get the index of the ec hit
	 * 
	 * @param sect
	 *            the 1-based sector
	 * @param stack
	 *            the 1-based stack (inner = 1, outer = 2) index
	 * @param view
	 *            the 1-based strip type (u, v, w) = (1, 2, 3)
	 * @param strip
	 *            the 1-based strip
	 * @return the list of matching hits
	 */
	public static Vector<HitRecord> matchingHits(int sect, int stack, int view,
			int strip) {

		
		int hitCount =  hitCount();
		if (hitCount < 1) {
			return null;
		}
				
		Vector<HitRecord> hits = new Vector<HitRecord>();
		
		int sectors[] = sector();
		int views[] = view();
		int stacks[] = stack();
		int strips[] = strip();
		double avgX[] = avgX();
		double avgY[] = avgY();
		double avgZ[] = avgZ();
		
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
		int ADC[] = ADC();
		int TDC[] = TDC();

		String hitStr = DataSupport.safeString(hitn, hitIndex);
		String adcStr = DataSupport.safeString(ADC, hitIndex);
		String tdcStr = DataSupport.safeString(TDC, hitIndex);

		feedbackStrings.add(DataSupport.dgtzColor + "adc " + adcStr + "  tdc " + tdcStr
				+ " ns" + "  hit " + hitStr);

	}
	

	/**
	 * Some true feedback for ec and pcal
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
	 * Some preliminary feedback for EC and PCAL
	 * 
	 * @param hitIndex the hit index
	 * @param feedbackStrings the collection of feedback strings
	 */
	public static void preliminaryFeedback(int hitIndex,
			List<String> feedbackStrings) {

		if (hitIndex < 0) {
			return;
		}

		int view[] = view();
		int strip[] = strip();

		if ((view == null) || (hitIndex >= view.length)) {
			return;
		}

		feedbackStrings.add("==== " + DataSupport.uvwStr[view[hitIndex] - 1] + " strip "
				+ strip[hitIndex] + " ====");

		double avgX[] = avgX();
		double avgY[] = avgY();
		double avgZ[] = avgZ();
		double avgLx[] = avgLx();
		double avgLy[] = avgLy();
		double avgLz[] = avgLz();

		DataSupport.addXYZFeedback(hitIndex, avgX, avgY, avgZ, avgLx, avgLy, avgLz,
				feedbackStrings);
	}

}
