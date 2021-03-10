package cnuphys.ced.event.data;

import org.jlab.io.base.DataEvent;


public class AIDC extends DetectorData {

	// AI HB reconstructed hits
	private DCReconHitList _hbHits;

	// AI TB reconstructed hits
	private DCReconHitList _tbHits;
	
	// AI HB reconstructed clusters
	private DCClusterList _hbClusters;

	// AI TB reconstructed clusters
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
	

	// singleton
	private static AIDC _instance;

	/**
	 * Public access to the singleton
	 * 
	 * @return the singleton
	 */
	public static AIDC getInstance() {
		if (_instance == null) {
			_instance = new AIDC();
		}
		return _instance;
	}

	@Override
	public void newClasIoEvent(DataEvent event) {
		
		
		//the reconstructed hits, HB and TB
		try {
			_hbHits = new DCReconHitList("HitBasedTrkg::AIHits");
		} catch (EventDataException e) {
			_hbHits = null;
			e.printStackTrace();
		}
		try {
			_tbHits = new DCReconHitList("TimeBasedTrkg::AIHits");
		} catch (EventDataException e) {
			_tbHits = null;
			e.printStackTrace();
		}
		
		//the clusters
		try {
			_hbClusters = new DCClusterList("HitBasedTrkg::AIClusters");
		} catch (EventDataException e) {
			_hbClusters = null;
			e.printStackTrace();
		}
		try {
			_tbClusters = new DCClusterList("TimeBasedTrkg::AIClusters");
		} catch (EventDataException e) {
			_tbClusters = null;
			e.printStackTrace();
		}

	}


	/**
	 * Get the hit based hit list
	 * 
	 * @return the hit based hit list
	 */
	public DCReconHitList getAIHBHits() {
		return _hbHits;
	}

	/**
	 * Get the time based hit list
	 * 
	 * @return the time based hit list
	 */
	public DCReconHitList getAITBHits() {
		return _tbHits;
	}
	
	/**
	 * Get the hit based cluster list
	 * 
	 * @return the hit based cluster list
	 */
	public DCClusterList getAIHBClusters() {
		return _hbClusters;
	}

	/**
	 * Get the time based cluster list
	 * 
	 * @return the time based cluster list
	 */
	public DCClusterList getAITBClusters() {
		return _tbClusters;
	}

}