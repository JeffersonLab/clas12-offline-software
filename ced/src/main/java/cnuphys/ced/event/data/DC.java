package cnuphys.ced.event.data;

import java.util.List;

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
	 * Get the sector array from the dgtz array
	 * @return the sector array
	 */
	public static int[] sector() {
		return ColumnData.getIntArray("DC::dgtz.sector");
	}

	/**
	 * Get the superlayer array from the dgtz array
	 * @return the superlayer array
	 */
	public static int[] superlayer() {
		return ColumnData.getIntArray("DC::dgtz.superlayer");
	}

	/**
	 * Get the layer array from the dgtz array
	 * @return the layer array
	 */
	public static int[] layer() {
		return ColumnData.getIntArray("DC::dgtz.layer");
	}

	/**
	 * Get the wire array from the dgtz array
	 * @return the wire array
	 */
	public static int[] wire() {
		return ColumnData.getIntArray("DC::dgtz.wire");
	}

	/**
	 * Get the hitn array from the dgtz data
	 * @return the hitn array
	 */
	public static int[] hitn() {
		return ColumnData.getIntArray("DC::dgtz.hitn");
	}
		
	/**
	 * Get the LR array from the dgtz data
	 * @return the LR array
	 */
	public static int[] LR() {
		return ColumnData.getIntArray("DC::dgtz.LR");
	}
	
	/**
	 * Get the doca array from the dgtz data
	 * @return the doca array
	 */
	public static double[] doca() {
		return ColumnData.getDoubleArray("DC::dgtz.doca");
	}
	
	/**
	 * Get the time array from the dgtz data
	 * @return the time array
	 */
	public static double[] time() {
		return ColumnData.getDoubleArray("DC::dgtz.time");
	}
	
	/**
	 * Get the sdoca array from the dgtz data
	 * @return the sdoca array
	 */
	public static double[] sdoca() {
		return ColumnData.getDoubleArray("DC::dgtz.sdoca");
	}
	
	/**
	 * Get the stime array from the dgtz data
	 * @return the stime array
	 */
	public static double[] stime() {
		return ColumnData.getDoubleArray("DC::dgtz.stime");
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
	 * Get the tdc array from the dgtz data
	 * @return the tdc array
	 */
	public static int[] tdc() {
		return ColumnData.getIntArray("DC::dgtz.tdc");
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
	 * Some DC noise feedback
	 * 
	 * @param hitIndex the hit index
	 * @param feedbackStrings the collection of feedback strings
	 */
	public static void noiseFeedback(int hitIndex,
			List<String> feedbackStrings) {
		if (hitIndex < 0) {
			return;
		}

		boolean noise[] = NoiseManager.getInstance().getNoise();

		if ((noise == null) || (hitIndex >= noise.length)) {
			return;
		}

		String noiseStr = DataSupport.prelimColor + "Noise Hit Guess: "
				+ (((noise != null) && noise[hitIndex]) ? "noise"
						: "not noise");
		feedbackStrings.add(noiseStr);

	}

	
	/**
	 * Add some dgtz hit feedback for dc
	 * 
	 * @param hitIndex the hit index
	 * @param feedbackStrings the collection of feedback strings
	 */
	public static void dgtzFeedback(int hitIndex,
			List<String> feedbackStrings) {

		if (hitIndex < 0) {
			return;
		}
		
		int hitCount = hitCount();
		if ((hitCount > 0) && (hitIndex < hitCount)) {
			int sector[] = sector();
			int superlayer[] = superlayer();
			int layer[] = layer();
			int wire[] = wire();
			int hitn[] = hitn();
			int LR[] = LR();
			double doca[] = doca();
			double sdoca[] = sdoca();
			double time[] = time();
			double stime[] = stime();

			feedbackStrings.add(DataSupport.dgtzColor + "sector " + sector[hitIndex]
					+ "  superlayer " + superlayer[hitIndex] + "  layer "
					+ layer[hitIndex] + "  wire " + wire[hitIndex]);

			String lraStr = DataSupport.safeString(LR, hitIndex);
			String hitStr = DataSupport.safeString(hitn, hitIndex);
			String docaStr = DataSupport.safeString(doca, hitIndex, 1);
			String timeStr = DataSupport.safeString(time, hitIndex, 1);
			String sdocaStr = DataSupport.safeString(sdoca, hitIndex, 1);
			String stimeStr = DataSupport.safeString(stime, hitIndex, 1);

			feedbackStrings.add(DataSupport.dgtzColor + "LRA " + lraStr + "  hit " + hitStr);
			feedbackStrings.add(
					DataSupport.dgtzColor + "doca " + docaStr + "  sdoca " + sdocaStr + " mm");
			feedbackStrings.add(
					DataSupport.dgtzColor + "time " + timeStr + "  stime " + stimeStr + " ns");
			
		} //hitCount > 0

	}
	
	/**
	 * Get the index of the dc hit
	 * 
	 * @param sect the 1-based sector
	 * @param supl the 1-based superlayer
	 * @param lay the 1-based layer
	 * @param wireid the 1-based wire
	 * @return the index of a hit with these parameters, or -1 if not found
	 */
	public static int getHitIndex(int sect, int supl, int lay, int wireid) {

		int hitCount = hitCount();
		if (hitCount > 0) {
			int sector[] = DC.sector();
			int superlayer[] = DC.superlayer();
			int layer[] = DC.layer();
			int wire[] = DC.wire();
			
			for (int i = 0; i < hitCount; i++) {
				if ((sect == sector[i]) && (supl == superlayer[i])
						&& (lay == layer[i]) && (wireid == wire[i])) {
					return i;
				}
			}

		}
		
		return -1;
	}


	/**
	 * Get the number of hit based crosses
	 * 
	 * @return he number of hit based crosses
	 */
	public static int hitBasedCrossCount() {
		int sector[] = ColumnData.getIntArray("HitBasedTrkg::HBCrosses.sector");
		return (sector == null) ? 0 : sector.length;
	}

	/**
	 * Get the number of time based crosses
	 * 
	 * @return the number of time based crosses
	 */
	public static int timeBasedCrossCount() {
		int sector[] = ColumnData
				.getIntArray("TimeBasedTrkg::TBCrosses.sector");
		return (sector == null) ? 0 : sector.length;
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
