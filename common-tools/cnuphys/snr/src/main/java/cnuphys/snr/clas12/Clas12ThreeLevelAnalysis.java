package cnuphys.snr.clas12;

import cnuphys.snr.NoiseReductionParameters;

public class Clas12ThreeLevelAnalysis {

	/** the raw hit count */
	public int rawHitCount;

	/** level of analysis */
	private SnrLevel _snrLevel = SnrLevel.LEVEL_ONE;

	// there is superlayer dependence on the parameters but not sector dependence
	private final NoiseReductionParameters _parameters[][] = new NoiseReductionParameters[Clas12Constants.NUM_SECTOR][Clas12Constants.NUM_SUPERLAYER];

	public Clas12ThreeLevelAnalysis() {
		initialize();
	}

	// initialize arrays
	private void initialize() {

		for (int sect = 0; sect < Clas12Constants.NUM_SECTOR; sect++) {
			for (int supl = 0; supl < Clas12Constants.NUM_SUPERLAYER; supl++) {

				// parameters contain the number of layers, the number of wires
				// allowed missing layers, the left shifts and the right shifts
				_parameters[sect][supl] = new NoiseReductionParameters(Clas12Constants.NUM_LAYER,
						Clas12Constants.NUM_WIRE, Clas12Constants.missingLayers_Lev1[supl],
						Clas12Constants.leftShifts_Lev1[supl], Clas12Constants.rightShifts_Lev1[supl]);
			}

		}
	}

	/**
	 * Set the SNR analysis level
	 * 
	 * @param level the new level
	 */
	public void setLevel(SnrLevel level) {
		_snrLevel = level;
	}

	/**
	 * Get the SNR analysis level
	 * 
	 * @return the SNR analysis level
	 */
	public SnrLevel getLevel() {
		return _snrLevel;
	}

	/**
	 * Get the level1 parameters for a given 0-based superlayer
	 * 
	 * @param sector the 0-based sector
	 * @param supl   the 0-based superlayer in question
	 * @return the parameters for that superlayer
	 */
	public NoiseReductionParameters getParameters(int sector, int supl) {
		return _parameters[sector][supl];
	}

	/**
	 * Set the leve1 parameters for a given 0-based superlayer
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
		for (int sect = 0; sect < Clas12Constants.NUM_SECTOR; sect++) {
			for (int supl = 0; supl < Clas12Constants.NUM_SUPERLAYER; supl++) {
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
		for (int sect = 0; sect < Clas12Constants.NUM_SECTOR; sect++) {
			for (int supl = 0; supl < Clas12Constants.NUM_SUPERLAYER; supl++) {
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
