package cnuphys.snr.clas12;

import java.util.Random;

import cnuphys.snr.ExtendedWord;
import cnuphys.snr.NoiseReductionParameters;

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

	// unlike the test program, the clas composite chambers have three layers
	private static final int NUM_COMPOSITE_LAYER = 3;

	private static final int LEFT = NoiseReductionParameters.LEFT_LEAN; // 0
	private static final int RIGHT = NoiseReductionParameters.RIGHT_LEAN; // 1

	// Superlayers 1,3,5 are PLUS (tilt)
	// Superlayers 2, 4, 6 are MINUS (tilt)
	private static final int LEFT_PLUS = 0;
	private static final int RIGHT_PLUS = 1;
	private static final int LEFT_MINUS = 2;
	private static final int RIGHT_MINUS = 3;

	/** the raw hit count */
	public int rawHitCount;

	// default num missing layers
	private final int[] _defaultMissingLayers = { 2, 2, 2, 2, 2, 2 };
	
//	// default layers shifts
//	private final int[][] _defaultLeftShifts = { { 0, 1, 2, 2, 3, 3 }, { 0, 1, 2, 2, 3, 3 }, { 0, 1, 2, 2, 3, 3 },
//			{ 0, 1, 2, 2, 3, 3 }, { 0, 1, 2, 2, 3, 3 }, { 0, 1, 2, 2, 3, 3 } };
//
//	// default layers shifts
//	private final int[][] _defaultRightShifts = { { 0, 1, 2, 2, 3, 3 }, { 0, 1, 2, 2, 3, 3 }, { 0, 1, 2, 2, 3, 3 },
//			{ 0, 1, 2, 2, 3, 3 }, { 0, 1, 2, 2, 3, 3 }, { 0, 1, 2, 2, 3, 3 } };


	// default layers shifts
	private final int[][] _defaultLeftShifts = { 
			{ 0, 1, 2, 2, 2, 2 },
			{ 0, 1, 2, 2, 2, 2 }, 
			{ 0, 1, 2, 2, 2, 2 },
			{ 0, 1, 2, 2, 2, 2 },
			{ 0, 3, 4, 4, 5, 5 }, 
			{ 0, 3, 4, 4, 5, 5 } 
			};

	// default layers shifts
	private final int[][] _defaultRightShifts = { 
			{ 0, 1, 2, 2, 2, 2 },
			{ 0, 1, 2, 2, 2, 2 }, 
			{ 0, 1, 2, 2, 2, 2 },
			{ 0, 1, 2, 2, 2, 2 }, 
			{ 0, 3, 4, 4, 5, 5 }, 
			{ 0, 3, 4, 4, 5, 5 } 
			};

	// default num missing layers for composite chambers (superlayers);
	// unlike the test program there are 4 composite chambers here
	// because of the wire rotations
	private final int[] _comp_defaultMissingLayers = { 0, 0, 0, 0 };

	// default layers shifts for composite chambers (superlayers)
	// for clas data (unlike the test program) there are
	// 4 composite chambers each with 3 layers
	private final int[][] _comp_defaultRightShifts = {
			// {0, 0, 0}, //LEFT_PLUS
			// {0, 22, 34}, //RIGHT_PLUS
			// {0, 0, 0}, //LEFT_MINUS
			// {0, 22, 34} //RIGHT_MINUS
			{ 0, 0, 0 }, // LEFT_PLUS
			{ 0, 37, 50 }, // RIGHT_PLUS
			{ 0, 0, 0 }, // LEFT_MINUS
			{ 0, 37, 50 } // RIGHT_MINUS
	};

	// default layers shifts for composite chambers (superlayers)
	private final int[][] _comp_defaultLeftShifts = {
			// {0, 22, 34}, //LEFT_PLUS
			// {0, 0, 0}, //RIGHT_PLUS
			// {0, 22, 34}, //LEFT_MINUS
			// {0, 0, 0} //RIGHT_MINUS
			{ 0, 37, 50 }, // LEFT_PLUS
			{ 0, 0, 0 }, // RIGHT_PLUS
			{ 0, 37, 50 }, // LEFT_MINUS
			{ 0, 0, 0 } // RIGHT_MINUS
	};

	// there is superlayer dependence on the parameters but not sector
	// dependence
	private final NoiseReductionParameters _parameters[][] = new NoiseReductionParameters[NUM_SECTOR][NUM_SUPERLAYER];

	// four sets of parameters for the composite chambers (superlayers)
	private final NoiseReductionParameters _compositeParameters[][] = new NoiseReductionParameters[NUM_SECTOR][4];

	public Clas12NoiseAnalysis() {
		initialize();
	}

	// initialize arrays
	private void initialize() {

		for (int sect = 0; sect < NUM_SECTOR; sect++) {
			for (int supl = 0; supl < NUM_SUPERLAYER; supl++) {
				_parameters[sect][supl] = new NoiseReductionParameters(NUM_LAYER, NUM_WIRE, _defaultMissingLayers[supl],
						_defaultLeftShifts[supl], _defaultRightShifts[supl]);
			}

			// The composite has 4 "superlayers" each with three "layers"
			for (int i = 0; i < 4; i++) {
				_compositeParameters[sect][i] = new NoiseReductionParameters(NUM_COMPOSITE_LAYER, NUM_WIRE,
						_comp_defaultMissingLayers[i], _comp_defaultLeftShifts[i], _comp_defaultRightShifts[i]);
			}
		}
	}

	/**
	 * Get the parameters for a given 0-based superlayer
	 * 
	 * @param sector
	 *            the 0-based sector
	 * @param supl
	 *            the 0-based superlayer in question
	 * @return the parameters for that superlayer
	 */
	public NoiseReductionParameters getParameters(int sector, int supl) {
		return _parameters[sector][supl];
	}

	/**
	 * Set the parameters for a given 0-based superlayer
	 * 
	 * @param sector
	 *            the 0-based sector
	 * @param supl
	 *            the 0-based superlayer in question
	 * @param params
	 *            the parameters for that superlayer
	 */
	public void setParameters(int sector, int supl, NoiseReductionParameters params) {
		_parameters[sector][supl] = params;
	}

	/**
	 * Get the parameters for a composite chamber/superlayer
	 * 
	 * @param sector
	 *            the 0-based sector
	 * @param lrpm
	 *            should be LEFT_PLUS (0) RIGHT_PLUS (1) LEFT_MINUS (2)
	 *            RIGHT_MINUS (3)
	 * @return the parameters for that composite chamber/superlayer
	 */
	public NoiseReductionParameters getCompositeParameters(int sector, int lrpm) {
		return _compositeParameters[sector][lrpm];
	}

	/**
	 * Set the parameters for a composite chamber/superlayer
	 * 
	 * @param sector
	 *            the 0-based sector
	 * @param lrpm
	 *            should be LEFT_PLUS (0) RIGHT_PLUS (1) LEFT_MINUS (2)
	 *            RIGHT_MINUS (3)
	 * @param params
	 *            the parameters for that composite chamber/superlayer
	 */
	public void setCompositeParameters(int sector, int lrpm, NoiseReductionParameters params) {
		_compositeParameters[sector][lrpm] = params;
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
			for (int lrpm = 0; lrpm < 4; lrpm++) {
				_compositeParameters[sect][lrpm].clear();
			}
		}
	}
	
	/**
	 * This methods takes the data arrays and generates the results. The input
	 * arrays contain 1-based indices, just like in the clasio banks
	 * 
	 * @param sector
	 *            the 1-based sector array
	 * @param superlayer
	 *            the 1-based superlayer array
	 * @param layer
	 *            the 1-based layer array
	 * @param wire
	 *            the 1-based wire array
	 * @param results
	 *            container for the results
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
				// prepare for second pass
				//if we are looking for TRACKS
				if (NoiseReductionParameters.lookForTracks()) {
					
					boolean plus = (supl % 2) == 0;
					int compositeLayer = supl / 2;

					if (plus) {
						setCompositeLayerPackedData(_compositeParameters[sect][LEFT_PLUS], compositeLayer,
								_parameters[sect][supl].getLeftSegments());
						setCompositeLayerPackedData(_compositeParameters[sect][RIGHT_PLUS], compositeLayer,
								_parameters[sect][supl].getRightSegments());
					} else {
						setCompositeLayerPackedData(_compositeParameters[sect][LEFT_MINUS], compositeLayer,
								_parameters[sect][supl].getLeftSegments());
						setCompositeLayerPackedData(_compositeParameters[sect][RIGHT_MINUS], compositeLayer,
								_parameters[sect][supl].getRightSegments());
					}
				}

			}
		}

		// 2nd pass for composite detector to find track candidates

		if (NoiseReductionParameters.lookForTracks()) {
			for (int sect = 0; sect < NUM_SECTOR; sect++) {
				_compositeParameters[sect][LEFT_PLUS].removeNoise();
				_compositeParameters[sect][RIGHT_PLUS].removeNoise();
				_compositeParameters[sect][LEFT_MINUS].removeNoise();
				_compositeParameters[sect][RIGHT_MINUS].removeNoise();

				int plusSegLeftCount = _compositeParameters[sect][LEFT_PLUS].getLeftSegments().bitCount();
				int plusSegRightCount = _compositeParameters[sect][RIGHT_PLUS].getRightSegments().bitCount();
				int minusSegLeftCount = _compositeParameters[sect][LEFT_MINUS].getLeftSegments().bitCount();
				int minusSegRightCount = _compositeParameters[sect][RIGHT_MINUS].getRightSegments().bitCount();

				// must have a left seg in plus and minus or a right seg in plus
				// and minus
				boolean noRight = (plusSegRightCount == 0) || (minusSegRightCount == 0);
				boolean noLeft = (plusSegLeftCount == 0) || (minusSegLeftCount == 0);

				for (int supl = 0; supl < NUM_SUPERLAYER; supl++) {
					boolean plus = (supl % 2) == 0;
					int compositeLayer = supl / 2;

					if (plus) {
						if (noLeft) {
							_compositeParameters[sect][LEFT_PLUS].getPackedData(compositeLayer).clear();
						}
						if (noRight) {
							_compositeParameters[sect][RIGHT_PLUS].getPackedData(compositeLayer).clear();
						}

						//this removes from the NORMAL superlayers was is now noise according to tack finding
						_parameters[sect][supl].secondPass(LEFT,
								_compositeParameters[sect][LEFT_PLUS].getPackedData(compositeLayer));
						_parameters[sect][supl].secondPass(RIGHT,
								_compositeParameters[sect][RIGHT_PLUS].getPackedData(compositeLayer));
					} else {
						if (noLeft) {
							_compositeParameters[sect][LEFT_MINUS].getPackedData(compositeLayer).clear();
						}
						if (noRight) {
							_compositeParameters[sect][RIGHT_MINUS].getPackedData(compositeLayer).clear();
						}

						_parameters[sect][supl].secondPass(LEFT,
								_compositeParameters[sect][LEFT_MINUS].getPackedData(compositeLayer));
						_parameters[sect][supl].secondPass(RIGHT,
								_compositeParameters[sect][RIGHT_MINUS].getPackedData(compositeLayer));
					}

				}
			}
		} // look for tracks

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

	/**
	 * Set the data for the composite chamber from the left and right segments
	 * of the corresponding real chamber
	 * 
	 * @param compParams
	 *            the composite chamber/superlayer parameters
	 * @param layer
	 *            the composite 0-based layer. E.g., if this is 3, then we are
	 *            setting the results from chamber 3. This "layer" corresponds
	 *            to an actual SUPERlayer.
	 * @param segments
	 *            the segments (left or right) from the real chamber
	 *            (superlayer)
	 */
	private void setCompositeLayerPackedData(NoiseReductionParameters compParams, int layer, ExtendedWord segments) {
		ExtendedWord.copy(segments, compParams.getPackedData(layer));
	}
	
	
	
//	public static void main(String arg[]) {
//		ExtendedWord a = new ExtendedWord(112);
//		ExtendedWord b = new ExtendedWord(112);
//		ExtendedWord c = new ExtendedWord(112);
//		
//		Random rand = new Random();
//		a.getWords()[0] = rand.nextLong() >>> 5;
//		a.getWords()[1] = rand.nextLong() >>> 5;
//		b.getWords()[0] = rand.nextLong() >>> 5;
//		b.getWords()[1] = 0;
//		c.getWords()[0] = 0;
//		c.getWords()[1] = rand.nextLong() << 5;
//		
//		System.out.println(a + "  MSB: " + leftMostBitIndex(a) + "  LSB: " + rightMostBitIndex(a));
//		System.out.println(b + "  MSB: " + leftMostBitIndex(b) + "  LSB: " + rightMostBitIndex(b));
//		System.out.println(c + "  MSB: " + leftMostBitIndex(c) + "  LSB: " + rightMostBitIndex(c));
//
//	}
//	

}
