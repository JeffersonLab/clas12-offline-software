package cnuphys.ced.event.data;

import org.jlab.io.base.DataEvent;

import cnuphys.bCNU.util.SoundUtils;
import cnuphys.ced.alldata.ColumnData;
import cnuphys.ced.frame.Ced;

public class DC extends DetectorData {

	private static final int TOTALNUMWIRE = 24192;
	private static final int TOTALNUMWIRESECTOR = 4032;

	// tdc adc hit list
	private DCTdcHitList _tdcHits = new DCTdcHitList();

	// HB reconstructed hits
	private DCReconHitList _hbHits;

	// TB reconstructed hits
	private DCReconHitList _tbHits;
	
	// HB reconstructed clusters
	private DCClusterList _hbClusters;

	// TB reconstructed clusters
	private DCClusterList _tbClusters;
	
	
	/** 1-based sectors */
	public byte sector[];
	
	/** 1-based wires */
	public short wire[]; 
	
	/** 1-based superlayers */
	public byte superlayer[];
	
	/** 1-based layers 1..36 */
	public byte layer36[];
	
	/** 1-based layers 1..6 */
	public byte layer6[];
	
	/** tdc values */
	public int tdc[];
			


	// singleton
	private static DC _instance;

	/**
	 * Public access to the singleton
	 * 
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
		
		layer36 = null;
		layer6 = null;
		tdc = null;
		superlayer = null;
		wire = null;
		
		sector = ColumnData.getByteArray("DC::tdc.sector");
		int length = (sector == null) ? 0 : sector.length;
		
		if (length > 0)  {
			layer36 = ColumnData.getByteArray("DC::tdc.layer");
			wire = ColumnData.getShortArray("DC::tdc.component");
			tdc = ColumnData.getIntArray("DC::tdc.TDC");
			
			layer6 = new byte[length];
			superlayer = new byte[length];
			for (int i = 0; i < length; i++) {
				superlayer[i] = (byte) (((layer36[i] - 1) / 6) + 1);
				layer6[i] = (byte) (((layer36[i] - 1) % 6) + 1);
			}
			
			
		}
		
		
		//the base tdc hits
		_tdcHits = new DCTdcHitList();
		
		//the reconstructed hits, HB and TB
		try {
			_hbHits = new DCReconHitList("HitBasedTrkg::HBHits");
		} catch (EventDataException e) {
			_hbHits = null;
			e.printStackTrace();
		}
		try {
			_tbHits = new DCReconHitList("TimeBasedTrkg::TBHits");
		} catch (EventDataException e) {
			_tbHits = null;
			e.printStackTrace();
		}
		
		//the clusters
		try {
			_hbClusters = new DCClusterList("HitBasedTrkg::HBClusters");
		} catch (EventDataException e) {
			_hbClusters = null;
			e.printStackTrace();
		}
		try {
			_tbClusters = new DCClusterList("TimeBasedTrkg::TBClusters");
		} catch (EventDataException e) {
			_tbClusters = null;
			e.printStackTrace();
		}

		if (Ced.getCed().playDCOccupancy()) {
			playOccupancyTone(75);
		}
	}

	/**
	 * Update the list. This is probably needed only during accumulation
	 * 
	 * @return the update l;ist
	 */
	public DCTdcHitList updateTdcAdcList() {
		_tdcHits = new DCTdcHitList();
		return _tdcHits;
	}

	/**
	 * Get the tdc hit list
	 * 
	 * @return the tdc hit list
	 */
	public DCTdcHitList getTDCHits() {
		return _tdcHits;
	}

	/**
	 * Get the hit based hit list
	 * 
	 * @return the hit based hit list
	 */
	public DCReconHitList getHBHits() {
		return _hbHits;
	}

	/**
	 * Get the time based hit list
	 * 
	 * @return the time based hit list
	 */
	public DCReconHitList getTBHits() {
		return _tbHits;
	}
	
	/**
	 * Get the hit based cluster list
	 * 
	 * @return the hit based cluster list
	 */
	public DCClusterList getHBClusters() {
		return _hbClusters;
	}

	/**
	 * Get the time based cluster list
	 * 
	 * @return the time based cluster list
	 */
	public DCClusterList getTBClusters() {
		return _tbClusters;
	}

	/**
	 * total DC occupancy all sectors all layers
	 * 
	 * @return total DC occupancy
	 */
	public double totalOccupancy() {
		return ((double) _tdcHits.size()) / TOTALNUMWIRE;
	}

	/**
	 * total DC occupancy for a sector
	 * 
	 * @return total DC occupancy for a sector
	 */
	public double totalSectorOccupancy(int sector) {
		if ((sector > 0) && (sector < 7)) {
			return ((double) _tdcHits.sectorCounts[sector]) / TOTALNUMWIRESECTOR;
		} else {
			return 0.;
		}
	}

	private static final int MIN_HZ = 1500;
	private static final int MAX_HZ = 4000;
	private static final int UNDER_HZ = 440;
	private static final int OVER_HZ = 5000;

	public void playOccupancyTone(int msec) {
		SoundUtils.playData(MIN_HZ, MAX_HZ, msec, 0.0005, 0.07, totalOccupancy(), UNDER_HZ, OVER_HZ, 1.);
	}
}
