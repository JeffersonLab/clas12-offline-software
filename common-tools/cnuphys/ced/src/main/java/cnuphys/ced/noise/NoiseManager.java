package cnuphys.ced.noise;

import java.awt.Color;

import org.jlab.io.base.DataEvent;

import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.clasio.IClasIoEventListener;
import cnuphys.ced.event.data.DC;
import cnuphys.ced.event.data.DCTdcHit;
import cnuphys.ced.event.data.DCTdcHitList;
import cnuphys.snr.NoiseReductionParameters;
import cnuphys.snr.clas12.Clas12NoiseAnalysis;
import cnuphys.snr.clas12.Clas12NoiseResult;

public class NoiseManager implements IClasIoEventListener {

	/** noise mask color for left benders */
	public static final Color maskFillLeft = new Color(255, 128, 0, 48);

	/** noise mask color for right benders */
	public static final Color maskFillRight = new Color(0, 128, 255, 48);

	// singleton
	private static NoiseManager instance;

	// The analysis package
	private Clas12NoiseAnalysis noisePackage = new Clas12NoiseAnalysis();

	// result container
	private Clas12NoiseResult _noiseResults = new Clas12NoiseResult();

	// event manager
	private ClasIoEventManager _eventManager = ClasIoEventManager.getInstance();

	// private constructor
	private NoiseManager() {
		// I need to be notified before the views
		_eventManager.addClasIoEventListener(this, 1);
	}

	/**
	 * Public access to the singleton
	 * 
	 * @return the NoiseManager singleton
	 */
	public static NoiseManager getInstance() {
		if (instance == null) {
			instance = new NoiseManager();
		}
		return instance;
	}

//	/**
//	 * Get the noise array which is parallel to the other dc_dgtz arrays such as
//	 * dgtz_sector etc.
//	 * 
//	 * @return the noise array
//	 */
//	public boolean[] getNoise() {
//		return _noiseResults.noise;
//	}

	/**
	 * Get the parameters for a given 0-based superlayer
	 * 
	 * @param sect0
	 *            the 0-based sector
	 * @param supl0
	 *            the 0-based superlayer in question
	 * @return the parameters for that superlayer
	 */
	public NoiseReductionParameters getParameters(int sect0, int supl0) {
		return noisePackage.getParameters(sect0, supl0);
	}
	

	@Override
	public void newClasIoEvent(DataEvent event) {
		noisePackage.clear();
		_noiseResults.clear();
		
		DCTdcHitList hits = DC.getInstance().getTDCHits();
		
		if ((hits != null) && !hits.isEmpty()) {
			int sector[] = hits.sectorArray();
			int superlayer[] = hits.superlayerArray();
			int layer[] = hits.layer6Array();
			int wire[] = hits.wireArray();
			
			noisePackage.findNoise(sector,
					superlayer, layer,
					wire, _noiseResults);
			
			//mark the hits
			int index = 0;
			for (DCTdcHit hit : hits) {
				hit.noise = _noiseResults.noise[index];
				index++;
			}
		}		
	}
	
	//HACK
	private int[] toIntArray(byte[] bytes) {
		if (bytes == null) {
			return null;
		}
		int ints[] = new int[bytes.length];
		for (int i = 0; i < bytes.length; i++) {
			ints[i]= bytes[i];
		}
		return ints;
	}
	
	//HACK
	private int[] toIntArray(short[] shorts) {
		if (shorts == null) {
			return null;
		}
		int ints[] = new int[shorts.length];
		for (int i = 0; i < shorts.length; i++) {
			ints[i]= shorts[i];
		}
		return ints;
	}


	@Override
	public void openedNewEventFile(String path) {
	}
	
	/**
	 * Change the event source type
	 * @param source the new source: File, ET
	 */
	@Override
	public void changedEventSource(ClasIoEventManager.EventSourceType source) {
	}
	
	/**
	 * Tests whether this listener is interested in events while accumulating
	 * @return <code>true</code> if this listener is NOT interested in  events while accumulating
	 */
	@Override
	public boolean ignoreIfAccumulating() {
		return true;
	}


}
