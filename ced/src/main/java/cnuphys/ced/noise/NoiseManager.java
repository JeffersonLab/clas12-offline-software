package cnuphys.ced.noise;

import java.awt.Color;

import org.jlab.evio.clas12.EvioDataEvent;

import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.clasio.IClasIoEventListener;
import cnuphys.ced.event.data.DC;
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
	private Clas12NoiseResult noiseResults = new Clas12NoiseResult();

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

	/**
	 * Get the noise array which is parallel to the other dc_dgtz arrays such as
	 * dgtz_sector etc.
	 * 
	 * @return the noise array
	 */
	public boolean[] getNoise() {
		return noiseResults.noise;
	}

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
	public void newClasIoEvent(EvioDataEvent event) {
		noisePackage.clear();
		noiseResults.clear();
		
		int hitCount = DC.hitCount();
		
		if (hitCount > 0) {
			int sector[] = DC.sector();
			int superlayer[] = DC.superlayer();
			int layer[] = DC.layer();
			int wire[] = DC.wire();
			
			noisePackage.findNoise(sector,
					superlayer, layer,
					wire, noiseResults);
		}		
	}

	@Override
	public void openedNewEventFile(String path) {
	}
}
