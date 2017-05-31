package cnuphys.ced.event.data;

import org.jlab.io.base.DataEvent;

import cnuphys.bCNU.util.SoundUtils;
import cnuphys.ced.frame.Ced;

public class DC extends DetectorData {

	private static final int TOTALNUMWIRE = 24192;
	private static final int TOTALNUMWIRESECTOR = 4032;
	
	//tdc adc hit list
	DCTdcHitList _tdcHits = new DCTdcHitList();
	
	private static DC _instance;
	
	
	/**
	 * Public access to the singleton
	 * @return the singleton
	 */
	public static DC getInstance() {
		if (_instance == null) {
			_instance = new DC();
		}
		return _instance;
	}
	
	@Override
	public void newClasIoEvent(DataEvent event) {
		_tdcHits =  new DCTdcHitList();
		if (Ced.getCed().playDCOccupancy()) {
			playOccupancyTone(75);
		}
	}
	
	/**
	 * Update the list. This is probably needed only during accumulation
	 * @return the update l;ist
	 */
	public DCTdcHitList updateTdcAdcList() {
		_tdcHits =  new DCTdcHitList();
		return _tdcHits;
	}
	
	/**
	 * Get the tdc  hit list
	 * @return the tdc hit list
	 */
	public DCTdcHitList getHits() {
		return _tdcHits;
	}
	
	/**
	 * total DC occupancy all sectors all layers
	 * @return total DC occupancy
	 */
	public double totalOccupancy() {
		return ((double)_tdcHits.size())/TOTALNUMWIRE;
	}
	
	/**
	 * total DC occupancy for a sector
	 * 
	 * @return total DC occupancy for a sector
	 */
	public double totalSectorOccupancy(int sector) {
		if ((sector > 0) && (sector < 7)) {
			return ((double) _tdcHits.sectorCounts[sector]) / TOTALNUMWIRESECTOR;
		}
		else {
			return 0.;
		}
	}

	
	private static final int MIN_HZ = 1500;
	private static final int MAX_HZ = 4000;
	private static final int UNDER_HZ = 440;
	private static final int OVER_HZ = 5000;
	
	private static final double MIN_VAL = 0.0005;
	private static final double MAX_VAL = 0.07;
	
	public void playOccupancyTone(int  msec) {
		SoundUtils.playData(MIN_HZ, MAX_HZ, msec, 0.0005, 0.07, totalOccupancy(), UNDER_HZ, OVER_HZ, 1.);
	}
}
