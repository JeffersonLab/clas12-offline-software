package cnuphys.snr.clas12;

import cnuphys.snr.ExtendedWord;
import cnuphys.snr.NoiseReductionParameters;
import cnuphys.snr.SNRAnalysisLevel;

/**
 * One stop shopping for clas12 noise analysis. Should finally be thread safe.
 * 
 * @author heddle
 *
 */
public class Clas12NoiseAnalysis {

	private static final int NUM_SECTOR = 6;
	private static final int NUM_SUPERLAYER = 6;
	private static final int NUM_LAYER = 6;
	private static final int NUM_WIRE = 112;

	/** the raw hit count */
	public int rawHitCount;

	// default num missing layers
	private final int[] _defaultMissingLayers = { 2, 2, 2, 2, 2, 2 };


	// default layers shifts
	private final int[][] _defaultLeftShifts = { 
			{ 0, 1, 2, 2, 2, 2 }, 
			{ 0, 1, 2, 2, 2, 2 }, 
			{ 0, 1, 2, 2, 2, 2 },
			{ 0, 1, 2, 2, 2, 2 }, 
			{ 0, 3, 4, 4, 5, 5 }, 
			{ 0, 3, 4, 4, 5, 5 } };

	// default layers shifts
	private final int[][] _defaultRightShifts = { 
			{ 0, 1, 2, 2, 2, 2 }, 
			{ 0, 1, 2, 2, 2, 2 }, 
			{ 0, 1, 2, 2, 2, 2 },
			{ 0, 1, 2, 2, 2, 2 }, 
			{ 0, 3, 4, 4, 5, 5 }, 
			{ 0, 3, 4, 4, 5, 5 } };


	// there is superlayer dependence on the parameters but not sector
	// dependence
	private final NoiseReductionParameters _parameters[][] = new NoiseReductionParameters[NUM_SECTOR][NUM_SUPERLAYER];

	/**
	 * Create an analysis object for CLAS12 DCs.
	 * Uses the basic single stage.
	 */
	public Clas12NoiseAnalysis() {
		this(SNRAnalysisLevel.ONESTAGE);
	}
	
	/**
	 * Create an analysis object for CLAS12 DCs
	 */
	public Clas12NoiseAnalysis(SNRAnalysisLevel level) {
		NoiseReductionParameters.setSNRAnalysisLevel(level);
		initialize();
	}


	// initialize arrays
	private void initialize() {

		for (int sect = 0; sect < NUM_SECTOR; sect++) {
			for (int supl = 0; supl < NUM_SUPERLAYER; supl++) {
				_parameters[sect][supl] = new NoiseReductionParameters(NUM_LAYER, NUM_WIRE, _defaultMissingLayers[supl],
						_defaultLeftShifts[supl], _defaultRightShifts[supl]);
			}

		}
	}

	/**
	 * Get the parameters for a given 0-based superlayer
	 * 
	 * @param sector the 0-based sector
	 * @param supl   the 0-based superlayer in question
	 * @return the parameters for that superlayer
	 */
	public NoiseReductionParameters getParameters(int sector, int supl) {
		return _parameters[sector][supl];
	}

	/**
	 * Set the parameters for a given 0-based superlayer
	 * 
	 * @param sector the 0-based sector
	 * @param supl   the 0-based superlayer in question
	 * @param params the parameters for that superlayer
	 */
	public void setParameters(int sector, int supl, NoiseReductionParameters params) {
		_parameters[sector][supl] = params;
	}


	/**
	 * Clear all the data
	 */
	public void clear() {
		rawHitCount = 0;
		for (int sect = 0; sect < NUM_SECTOR; sect++) {
			for (int supl = 0; supl < NUM_SUPERLAYER; supl++) {
				_parameters[sect][supl].clear();
			}
		}
	}

	/**
	 * This methods takes the data arrays and generates the results. The input
	 * arrays contain 1-based indices, just like in the clasio banks
	 * 
	 * @param sector     the 1-based sector array
	 * @param superlayer the 1-based superlayer array
	 * @param layer      the 1-based layer array
	 * @param wire       the 1-based wire array
	 * @param results    container for the results
	 */
	public void findNoise(int sector[], int superlayer[], int layer[], int wire[], Clas12NoiseResult results) {
		if (sector == null) {
			rawHitCount = 0;
			return;
		}

		rawHitCount = sector.length;

		// pack the data
		for (int hit = 0; hit < rawHitCount; hit++) {
			// get 0-based indices
			int sect0 = sector[hit] - 1;
			int supl0 = superlayer[hit] - 1;
			int lay0 = layer[hit] - 1;
			int wire0 = wire[hit] - 1;

			_parameters[sect0][supl0].packHit(lay0, wire0);
		}

		// remove the noise
		// 1st pass
		for (int sect = 0; sect < NUM_SECTOR; sect++) {
			for (int supl = 0; supl < NUM_SUPERLAYER; supl++) {
				_parameters[sect][supl].removeNoise();
			}
		}


		// now stuff the results object;
		results.noise = new boolean[rawHitCount];
		for (int hit = 0; hit < rawHitCount; hit++) {
			// get 0-based indices
			int sect0 = sector[hit] - 1;
			int supl0 = superlayer[hit] - 1;
			int lay0 = layer[hit] - 1;
			int wire0 = wire[hit] - 1;

			results.noise[hit] = _parameters[sect0][supl0].isNoiseHit(lay0, wire0);
		}
	}


}
