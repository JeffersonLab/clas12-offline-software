package cnuphys.ced.event.data;

import java.awt.Color;

import org.jlab.io.base.DataEvent;

import cnuphys.bCNU.util.SoundUtils;
import cnuphys.ced.frame.Ced;
import cnuphys.splot.plot.X11Colors;

public class DC extends DetectorData {

	private static final int TOTALNUMWIRE = 24192;
	private static final int TOTALNUMWIRESECTOR = 4032;
	
	
	//colors for hb based and time based distinction
	/** hit based color */
	public static final Color HB_COLOR = Color.yellow;
	
	/** slightly transparent HB color */
	public static final Color HB_TRANS = new Color(255, 255, 0, 240);
	
	/** time based color */
	public static final Color TB_COLOR = X11Colors.getX11Color("dark orange");
	
	/** slightly transparent TB color */
	public static final Color TB_TRANS = X11Colors.getX11Color("dark orange", 240);

	
	//tdc adc hit list
	private DCTdcHitList _tdcHits = new DCTdcHitList();
	
	//HB reconstructed hits
	private DCHitList _hbHits;
	
	//TB reconstructed hits
	private DCHitList _tbHits;

	
	//singleton
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
		try {
			_hbHits = new DCHitList("HitBasedTrkg::HBHits");
//			System.err.println("HB HIT COUNT = " + ((_hbHits == null) ? 0 : _hbHits.size()));
		} catch (EventDataException e) {
			_hbHits = null;
			e.printStackTrace();
		}
		try {
			_tbHits = new DCHitList("TimeBasedTrkg::TBHits");
//			System.err.println("TB HIT COUNT = " + ((_tbHits == null) ? 0 : _tbHits.size()));
		} catch (EventDataException e) {
			_tbHits = null;
			e.printStackTrace();
		}

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
	public DCTdcHitList getTDCHits() {
		return _tdcHits;
	}
	
	/**
	 * Get the hit based hit list
	 * @return the hit based hit list
	 */
	public DCHitList getHBHits() {
		return _hbHits;
	}
	
	/**
	 * Get the time based hit list
	 * @return the time based hit list
	 */
	public DCHitList getTBHits() {
		return _tbHits;
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
		
	public void playOccupancyTone(int  msec) {
		SoundUtils.playData(MIN_HZ, MAX_HZ, msec, 0.0005, 0.07, totalOccupancy(), UNDER_HZ, OVER_HZ, 1.);
	}
}
