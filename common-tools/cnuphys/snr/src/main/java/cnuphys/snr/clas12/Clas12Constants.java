package cnuphys.snr.clas12;

import cnuphys.snr.NoiseReductionParameters;

public class Clas12Constants {

	//obvious geometric constants
	protected static final int NUM_SECTOR = 6;
	protected static final int NUM_SUPERLAYER = 6;
	protected static final int NUM_LAYER = 6;
	protected static final int NUM_WIRE = 112;

	//lean directions
	protected static final int LEFT = NoiseReductionParameters.LEFT_LEAN; // 0
	protected static final int RIGHT = NoiseReductionParameters.RIGHT_LEAN; // 1
	
	// default num missing layers
	protected static int[] missingLayers = { 2, 2, 2, 2, 2, 2 };


	// default left layer shifts
	protected static int[][] leftShifts = { 
			{ 0, 1, 2, 2, 2, 2 }, 
			{ 0, 1, 2, 2, 2, 2 }, 
			{ 0, 1, 2, 2, 2, 2 },
			{ 0, 1, 2, 2, 2, 2 }, 
			{ 0, 3, 4, 4, 5, 5 }, 
			{ 0, 3, 4, 4, 5, 5 } };

	// default right layer shifts
	protected static int[][] rightShifts = { 
			{ 0, 1, 2, 2, 2, 2 }, 
			{ 0, 1, 2, 2, 2, 2 }, 
			{ 0, 1, 2, 2, 2, 2 },
			{ 0, 1, 2, 2, 2, 2 }, 
			{ 0, 3, 4, 4, 5, 5 }, 
			{ 0, 3, 4, 4, 5, 5 } };


}
