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
	
	public List<DetectorResponse> getDetectorResponses() {
		return null;
	}
			
	/**
	 * Get the sector array from the dgtz array
	 * @return the sector array
	 */
	public static byte[] sector() {
		return ColumnData.getByteArray("DC::tdc.sector");
	}

	/**
	 * Get the superlayer array from the dgtz array
	 * @return the superlayer array
	 */
	public static byte[] superlayer() {
		byte fulllayer[] =  ColumnData.getByteArray("DC::tdc.layer");
		if (fulllayer == null) {
			return null;
		}
		
		for (int index = 0; index < fulllayer.length; index++) {
			fulllayer[index] = (byte) (1 + ((fulllayer[index]-1) / 6));
		}
		
		return fulllayer;
	}

	/**
	 * Get the layer array from the dgtz array
	 * @return the layer array with layers [1..6]
	 */
	public static byte[] layer() {
		byte fulllayer[] =  ColumnData.getByteArray("DC::tdc.layer");
		if (fulllayer == null) {
			return null;
		}
		
		for (int index = 0; index < fulllayer.length; index++) {
			fulllayer[index] = (byte) (1 + ((fulllayer[index]-1) % 6));
		}
		
		return fulllayer;
	}

	/**
	 * Get the wire array from the dgtz array
	 * @return the wire array
	 */
	public static short[] wire() {
		return ColumnData.getShortArray("DC::tdc.component");
	}
	
	/**
	 * Get the sectors of time based segments
	 * 
	 * @return the sectors of time based segments
	 */
	public static int[] timeBasedSegmentSector() {
		return ColumnData.getIntArray("TimeBasedTrkg::TBSegments.sector");
	}

	/**
	 * Get the superlayers of time based segments
	 * 
	 * @return the superlayers of time based segments
	 */
	public static int[] timeBasedSegmentSuperlayer() {
		return ColumnData.getIntArray("TimeBasedTrkg::TBSegments.superlayer");
	}
	
	/**
	 * Get the start x coordinates in the midplane in sector system
	 * @return the start x coordinates
	 */
	public static double[] timeBasedSegment1X() {
		return ColumnData.getDoubleArray("TimeBasedTrkg::TBSegments.SegEndPoint1X");
	}

	/**
	 * Get the start z coordinates in the midplane in sector system
	 * @return the start z coordinates
	 */
	public static double[] timeBasedSegment1Z() {
		return ColumnData.getDoubleArray("TimeBasedTrkg::TBSegments.SegEndPoint1Z");
	}
	
	
	/**
	 * Get the end x coordinates in the midplane in sector system
	 * @return the end x coordinates
	 */
	public static double[] timeBasedSegment2X() {
		return ColumnData.getDoubleArray("TimeBasedTrkg::TBSegments.SegEndPoint2X");
	}

	/**
	 * Get the end z coordinates in the midplane in sector system
	 * @return the end z coordinates
	 */
	public static double[] timeBasedSegment2Z() {
		return ColumnData.getDoubleArray("TimeBasedTrkg::TBSegments.SegEndPoint2Z");
	}

	
	/**
	 * Get the number of time based segments
	 * 
	 * @return the number of time based segments
	 */
	public static int timeBasedSegmentCount() {
		int sector[] = timeBasedSegmentSector();
		return (sector == null) ? 0 : sector.length;
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
	 * Get the number of time based hits
	 * 
	 * @return the number of time based tracks
	 */
	public static int timeBasedTrkgHitCount() {
		int sector[] = timeBasedTrkgSector();
		return (sector == null) ? 0 : sector.length;
	}
	
//	bank name: [DC::doca] column name: [LR] full name: [DC::doca.LR] data type: byte
//	bank name: [DC::doca] column name: [doca] full name: [DC::doca.doca] data type: Unknown
//	bank name: [DC::doca] column name: [sdoca] full name: [DC::doca.sdoca] data type: Unknown
//	bank name: [DC::doca] column name: [time] full name: [DC::doca.time] data type: Unknown
//	bank name: [DC::doca] column name: [stime] full name: [DC::doca.stime] data type: Unknown

	/**
	 * Get the LR array from the dgtz data
	 * @return the LR array
	 */
	public static byte[] LR() {
		return ColumnData.getByteArray("DC::doca.LR");
	}
	
	/**
	 * Get the unsmeared doca array from the dgtz data
	 * @return the unsmeared simulation doca array
	 */
	public static float[] doca() {
		return ColumnData.getFloatArray("DC::doca.doca");
	}
		
	/**
	 * Get the time array from the dgtz data
	 * @return the time array
	 */
	public static float[] time() {
		return ColumnData.getFloatArray("DC::doca.time");
	}
	
	/**
	 * Get the sdoca array from the dgtz data
	 * @return the sdoca array
	 */
	public static float[] sdoca() {
		return ColumnData.getFloatArray("DC::doca.sdoca");
	}
	
	/**
	 * Get the stime array from the dgtz data
	 * @return the stime array
	 */
	public static float[] stime() {
		return ColumnData.getFloatArray("DC::doca.stime");
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
	 * Get the hit count 
	 * @return the hit count
	 */
	public static int hitCount() {
		byte sector[] = sector();
		return (sector == null) ? 0 : sector.length;
	}
	
	/**
	 * Get the time based tracking momentum array
	 * @return the time based tracking momentum array
	 */
	public static double[] timeBasedTrackP() {
		return ColumnData.getDoubleArray("TimeBasedTrkg::TBTracks.p");
	}
	
	/**
	 * Get the time based tracking crosses sector array
	 * @return the time based tracking crosses sector array
	 */
	public static int[] timeBasedCrossSector() {
		return ColumnData.getIntArray("TimeBasedTrkg::TBCrosses.sector");
	}

	/**
	 * Get the hit based tracking crosses sector
	 * @return the hit based tracking crosses sector array
	 */
	public static int[] hitBasedCrossSector() {
		return ColumnData.getIntArray("HitBasedTrkg::HBCrosses.sector");
	}
	
	
	/**
	 * Get the time based tracking crosses region array
	 * @return the time based tracking crosses region array
	 */
	public static int[] timeBasedCrossRegion() {
		return ColumnData.getIntArray("TimeBasedTrkg::TBCrosses.region");
	}

	/**
	 * Get the hit based tracking crosses region
	 * @return the hit based tracking crosses region array
	 */
	public static int[] hitBasedCrossRegion() {
		return ColumnData.getIntArray("HitBasedTrkg::HBCrosses.region");
	}

	
	/**
	 * Get the time based tracking crosses ID array
	 * @return the time based tracking crosses ID array
	 */
	public static int[] timeBasedCrossID() {
		return ColumnData.getIntArray("TimeBasedTrkg::TBCrosses.ID");
	}

	/**
	 * Get the hit based tracking crosses ID
	 * @return the hit based tracking crosses ID array
	 */
	public static int[] hitBasedCrossID() {
		return ColumnData.getIntArray("HitBasedTrkg::HBCrosses.ID");
	}

	/**
	 * get the time based crosses X position
	 * @return the time based crosses X position
	 */
	public static double[] timeBasedCrossX() {
		return ColumnData.getDoubleArray("TimeBasedTrkg::TBCrosses.x");
	}
	
	/**
	 * get the time based crosses Y position
	 * @return the time based crosses Y position
	 */
	public static double[] timeBasedCrossY() {
		return ColumnData.getDoubleArray("TimeBasedTrkg::TBCrosses.y");
	}
	
	/**
	 * get the time based crosses Z position
	 * @return the time based crosses Z position
	 */
	public static double[] timeBasedCrossZ() {
		return ColumnData.getDoubleArray("TimeBasedTrkg::TBCrosses.z");
	}
	
	/**
	 * get the time based crosses Ux direction
	 * @return the time based crosses X direction
	 */
	public static double[] timeBasedCrossUx() {
		return ColumnData.getDoubleArray("TimeBasedTrkg::TBCrosses.ux");
	}
	
	/**
	 * get the time based crosses Uy direction
	 * @return the time based crosses Uy direction
	 */
	public static double[] timeBasedCrossUy() {
		return ColumnData.getDoubleArray("TimeBasedTrkg::TBCrosses.uy");
	}
	
	/**
	 * get the time based crosses Uz direction
	 * @return the time based crosses Uz direction
	 */
	public static double[] timeBasedCrossUz() {
		return ColumnData.getDoubleArray("TimeBasedTrkg::TBCrosses.uz");
	}

	/**
	 * get the time based crosses X error
	 * @return the time based crosses X error
	 */
	public static double[] timeBasedCrossErrX() {
		return ColumnData.getDoubleArray("TimeBasedTrkg::TBCrosses.err_x");
	}
	
	/**
	 * get the time based crosses Y error
	 * @return the time based crosses Y error
	 */
	public static double[] timeBasedCrossErrY() {
		return ColumnData.getDoubleArray("TimeBasedTrkg::TBCrosses.err_y");
	}
	
	/**
	 * get the time based crosses Z error
	 * @return the time based crosses Z error
	 */
	public static double[] timeBasedCrossErrZ() {
		return ColumnData.getDoubleArray("TimeBasedTrkg::TBCrosses.err_z");
	}

	/**
	 * get the hit based crosses X position
	 * @return the hit based crosses X position
	 */
	public static double[] hitBasedCrossX() {
		return ColumnData.getDoubleArray("HitBasedTrkg::HBCrosses.x");
	}
	
	/**
	 * get the hit based crosses Y position
	 * @return the hit based crosses Y position
	 */
	public static double[] hitBasedCrossY() {
		return ColumnData.getDoubleArray("HitBasedTrkg::HBCrosses.y");
	}
	
	/**
	 * get the hit based crosses Z position
	 * @return the hit based crosses Z position
	 */
	public static double[] hitBasedCrossZ() {
		return ColumnData.getDoubleArray("HitBasedTrkg::HBCrosses.z");
	}
	
	/**
	 * get the hit based crosses Ux direction
	 * @return the hit based crosses X direction
	 */
	public static double[] hitBasedCrossUx() {
		return ColumnData.getDoubleArray("HitBasedTrkg::HBCrosses.ux");
	}
	
	/**
	 * get the hit based crosses Uy direction
	 * @return the hit based crosses Uy direction
	 */
	public static double[] hitBasedCrossUy() {
		return ColumnData.getDoubleArray("HitBasedTrkg::HBCrosses.uy");
	}
	
	/**
	 * get the hit based crosses Uz direction
	 * @return the hit based crosses Uz direction
	 */
	public static double[] hitBasedCrossUz() {
		return ColumnData.getDoubleArray("HitBasedTrkg::HBCrosses.uz");
	}

	/**
	 * get the hit based crosses X error
	 * @return the hit based crosses X error
	 */
	public static double[] hitBasedCrossErrX() {
		return ColumnData.getDoubleArray("HitBasedTrkg::HBCrosses.err_x");
	}
	
	/**
	 * get the hit based crosses Y error
	 * @return the hit based crosses Y error
	 */
	public static double[] hitBasedCrossErrY() {
		return ColumnData.getDoubleArray("HitBasedTrkg::HBCrosses.err_y");
	}
	
	/**
	 * get the hit based crosses Z error
	 * @return the hit based crosses Z error
	 */
	public static double[] hitBasedCrossErrZ() {
		return ColumnData.getDoubleArray("HitBasedTrkg::HBCrosses.err_z");
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
	public static void dcBanksFeedback(int hitIndex,
			List<String> feedbackStrings) {

		if (hitIndex < 0) {
			return;
		}
		
		int hitCount = hitCount();
		if ((hitCount > 0) && (hitIndex < hitCount)) {
			byte sector[] = sector();
			byte superlayer[] = superlayer();
			byte layer[] = layer();
			short wire[] = wire();
			int tdc[] = tdc();
			byte LR[] = LR();
			float doca[] = doca();
			float sdoca[] = sdoca();
			float time[] = time();
			float stime[] = stime();

			feedbackStrings.add(DataSupport.dgtzColor + "sector " + sector[hitIndex]
					+ "  superlayer " + superlayer[hitIndex] + "  layer "
					+ layer[hitIndex] + "  wire " + wire[hitIndex]);

			String lraStr = DataSupport.safeString(LR, hitIndex);
			String tdcStr = DataSupport.safeString(tdc, hitIndex);
			String docaStr = DataSupport.safeString(doca, hitIndex, 1);
			String timeStr = DataSupport.safeString(time, hitIndex, 1);
			String sdocaStr = DataSupport.safeString(sdoca, hitIndex, 1);
			String stimeStr = DataSupport.safeString(stime, hitIndex, 1);

			feedbackStrings.add(DataSupport.dgtzColor + "LRA " + lraStr + "  tdc " + tdcStr);
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
	public static int hitIndex(int sect, int supl, int lay, int wireid) {

		int hitCount = hitCount();
		if (hitCount > 0) {
			byte sector[] = DC.sector();
			byte superlayer[] = DC.superlayer();
			byte layer[] = DC.layer();
			short wire[] = DC.wire();
			
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
