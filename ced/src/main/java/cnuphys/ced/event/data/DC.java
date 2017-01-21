package cnuphys.ced.event.data;

import java.util.List;

import org.jlab.clas.detector.DetectorResponse;
import org.jlab.io.base.DataEvent;

import cnuphys.ced.alldata.ColumnData;
import cnuphys.ced.alldata.DataManager;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.noise.NoiseManager;
import cnuphys.splot.plot.DoubleFormat;

/**
 * static methods to centralize getting data arrays related to DC
 * @author heddle
 *
 */
public class DC {
	
	  
	/**
	 * Get the pid array from the true data
	 * @return the pid array
	 */
	public static int[] pid() {
		return ColumnData.getIntArray("DC::true.pid");
	}


	/**
	 * Get the DOCAs from time based reconstruction
	 * @return the DOCAs from reconstruction
	 */
	public static double[] timeBasedTrkgDoca() {
		return ColumnData.getDoubleArray("TimeBasedTrkg::TBHits.doca");
	}
	
	/**
	 * Get the sectors of time based hits
	 * 
	 * @return the sectors of time based hits
	 */
	public static int[] timeBasedTrkgSector() {
		return ColumnData.getIntArray("TimeBasedTrkg::TBHits.sector");
	}

	/**
	 * Get the superlayer of time based hits
	 * 
	 * @return the super layers of time based hits
	 */
	public static int[] timeBasedTrkgSuperlayer() {
		return ColumnData.getIntArray("TimeBasedTrkg::TBHits.superlayer");
	}

	/**
	 * Get the layers of time based hits
	 * 
	 * @return the layers of time based hits
	 */
	public static int[] timeBasedTrkgLayer() {
		return ColumnData.getIntArray("TimeBasedTrkg::TBHits.layer");
	}

	/**
	 * Get the wires of time based hits
	 * 
	 * @return the wires of time based hits
	 */
	public static int[] timeBasedTrkgWire() {
		return ColumnData.getIntArray("TimeBasedTrkg::TBHits.wire");
	}
	
	/**
	 * Get the trackE array from the true data
	 * @return the trackE array
	 */
	public static double[] trackE() {
		return ColumnData.getDoubleArray("DC::true.trackE");
	}

	/**
	 * Get the avgX array from the true data
	 * @return the avgX array
	 */
	public static double[] avgX() {
		return ColumnData.getDoubleArray("DC::true.avgX");
	}
	
	/**
	 * Get the avgY array from the true data
	 * @return the avgY array
	 */
	public static double[] avgY() {
		return ColumnData.getDoubleArray("DC::true.avgY");
	}
	
	/**
	 * Get the avgZ array from the true data
	 * @return the avgZ array
	 */
	public static double[] avgZ() {
		return ColumnData.getDoubleArray("DC::true.avgZ");
	}
		
	/**
	 * Get the tdc array from the tdc data
	 * @return the tdc array
	 */
	public static int[] tdc() {
		return ColumnData.getIntArray("DC::tdc.TDC");
	}
		
	/**
	 * Get the time based tracking momentum array
	 * @return the time based tracking momentum array
	 */
	public static double[] timeBasedTrackP() {
		return ColumnData.getDoubleArray("TimeBasedTrkg::TBTracks.p");
	}	
	
	/**
	 * Some truth feedback for DC
	 * 
	 * @param hitIndex the hit index
	 * @param feedbackStrings the collection of feedback strings
	 */
	public static void trueFeedback(int hitIndex,
			List<String> feedbackStrings) {

		if (hitIndex < 0) {
			return;
		}

		double trackE[] = trackE();
		if ((trackE == null) || (hitIndex >= trackE.length)) {
			return;
		}

		double etrack = trackE[hitIndex] / 1000; // to gev
		feedbackStrings.add(DataSupport.trueColor + "true energy "
				+ DoubleFormat.doubleFormat(etrack, 2) + " GeV");
	}

	/**
	 * Get the number of time based tracks
	 * 
	 * @return the number of time based tracks
	 */
	public static int timeBasedTrackCount() {
		int sector[] = ColumnData.getIntArray("TimeBasedTrkg::TBTracks.sector");
		return (sector == null) ? 0 : sector.length;
	}
	
}
