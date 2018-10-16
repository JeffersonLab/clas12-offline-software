package cnuphys.snr.clas12;

import cnuphys.snr.NoiseReductionParameters;

public class Clas12ThreeLevelAnalysis {
	
	
	// there is superlayer dependence on the parameters but not sector dependence
	private final NoiseReductionParameters _parameters[][] = new NoiseReductionParameters[Clas12Constants.NUM_SECTOR][Clas12Constants.NUM_SUPERLAYER];


	public Clas12ThreeLevelAnalysis() {
		initialize();
	}

	// initialize arrays
	private void initialize() {

		for (int sect = 0; sect < Clas12Constants.NUM_SECTOR; sect++) {
			for (int supl = 0; supl < Clas12Constants.NUM_SUPERLAYER; supl++) {
				_parameters[sect][supl] = new NoiseReductionParameters(Clas12Constants.NUM_LAYER,
						Clas12Constants.NUM_WIRE, Clas12Constants.missingLayers[supl], Clas12Constants.leftShifts[supl],
						Clas12Constants.rightShifts[supl]);
			}

		}
	}

}
