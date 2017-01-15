package cnuphys.ced.noise;

import java.awt.Color;

import org.jlab.clas.physics.PhysicsEvent;
import org.jlab.io.base.DataEvent;

import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.clasio.IClasIoEventListener;
import cnuphys.ced.event.data.DC;
import cnuphys.ced.fastmc.FastMCManager;
import cnuphys.ced.fastmc.NoiseData;
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

	/**
	 * Get the noise array which is parallel to the other dc_dgtz arrays such as
	 * dgtz_sector etc.
	 * 
	 * @return the noise array
	 */
	public boolean[] getNoise() {
		return _noiseResults.noise;
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
	
	/**
	 * New fast mc event
	 * @param event the generated physics event
	 */
	@Override
	public void newFastMCGenEvent(PhysicsEvent event) {
		noisePackage.clear();
		_noiseResults.clear();
		
		NoiseData noiseData = FastMCManager.getInstance().getNoiseData();
		
		if ((noiseData != null) && (noiseData.count > 0)) {
			noisePackage.findNoise(noiseData.sector,
					noiseData.superlayer, noiseData.layer,
					noiseData.wire, _noiseResults);
		}
	}
	

	@Override
	public void newClasIoEvent(DataEvent event) {
		noisePackage.clear();
		_noiseResults.clear();
		
		int hitCount = DC.hitCount();
		
		if (hitCount > 0) {
			byte sector[] = DC.sector();
			byte superlayer[] = DC.superlayer();
			byte layer[] = DC.layer();
			short wire[] = DC.wire();
			
			noisePackage.findNoise(sector,
					superlayer, layer,
					wire, _noiseResults);
		}		
	}

	@Override
	public void openedNewEventFile(String path) {
	}
	
	/**
	 * Change the event source type
	 * @param source the new source: File, ET, FastMC
	 */
	@Override
	public void changedEventSource(ClasIoEventManager.EventSourceType source) {
	}

}
