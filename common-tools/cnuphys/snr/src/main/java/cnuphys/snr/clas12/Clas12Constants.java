package cnuphys.snr.clas12;

public class Clas12Constants {

	// obvious geometric constants
	protected static final int NUM_SECTOR = 6;
	protected static final int NUM_SUPERLAYER = 6;
	protected static final int NUM_LAYER = 6;
	protected static final int NUM_WIRE = 112;

	// default num missing layers for level 1
	protected static int[] missingLayers = { 2, 2, 2, 2, 2, 2 };

	// default left layer shifts for level 1
	protected static int[][] leftShifts = { { 0, 1, 2, 2, 2, 2 }, { 0, 1, 2, 2, 2, 2 }, { 0, 1, 2, 2, 2, 2 },
			{ 0, 1, 2, 2, 2, 2 }, { 0, 3, 4, 4, 5, 5 }, { 0, 3, 4, 4, 5, 5 } };

	// default right layer shifts for level 1
	protected static int[][] rightShifts = { { 0, 1, 2, 2, 2, 2 }, { 0, 1, 2, 2, 2, 2 }, { 0, 1, 2, 2, 2, 2 },
			{ 0, 1, 2, 2, 2, 2 }, { 0, 3, 4, 4, 5, 5 }, { 0, 3, 4, 4, 5, 5 } };

}
