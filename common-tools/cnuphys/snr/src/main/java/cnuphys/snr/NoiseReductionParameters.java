package cnuphys.snr;

/**
 * All the parameters needed for noise reduction. Each superlayer should have
 * its own object. Now should be thread safe.
 * 
 * @author heddle
 */
public class NoiseReductionParameters {

	// track leaning directions
	public static final int LEFT_LEAN = 0;
	public static final int RIGHT_LEAN = 1;

	// number of words needed for the number of wires in a layer
	private int _numWordsNeeded;

	/** the number of layers per superlayer */
	protected int _numLayer;

	/** the number of wires per layer */
	protected int _numWire;

	/** The number of missing layers allowed */
	protected int _allowedMissingLayers;

	/**
	 * The shifts for left leaning tracks.
	 */
	protected int _leftLayerShifts[];

	/**
	 * The shifts for right leaning tracks.
	 */
	protected int _rightLayerShifts[];

	/**
	 * cumulative left segments. These are "results".
	 */
	protected ExtendedWord leftSegments;

	/**
	 * cumulative right segments. These are "results".
	 */
	protected ExtendedWord rightSegments;

	/**
	 * A workspace for a copy of the data.
	 */
	protected ExtendedWord copy[];

	/**
	 * A workspace used for storing an allocation of misses.
	 */
	protected ExtendedWord misses[];

	/**
	 * More workspace. This array has numLayers + 1 entries.
	 */
	protected ExtendedWord workSpace[];

	/**
	 * This is the actual data. Before noise reduction analysis is run, this
	 * contains all the hits. After the analysis, the noise hits are removed.
	 */
	protected ExtendedWord _packedData[];

	// keep a copy of the raw data
	private ExtendedWord _rawData[];

	// flag that specifies whether the data have been analyzed
	private boolean _analyzed = false;

	// look for tracks (or just segments)
	private static boolean _lookForTracks = false;

	public NoiseReductionParameters() {
	}

	/**
	 * Create a NoiseReductionParameter using defaults for the number of
	 * superlayers, layers and wires
	 * 
	 * @param allowedMissingLayers the number of missing layers permitted.
	 * @param leftLayerShifts the shifts for left leaning tracks. Length should
	 *            equal numLayers.
	 * @param rightLayerShifts the shifts for right leaning tracks. Length
	 *            should equal numLayers.
	 */
	public NoiseReductionParameters(int allowedMissingLayers,
			int[] leftLayerShifts, int[] rightLayerShifts) {
		this(6, 112, allowedMissingLayers, leftLayerShifts, rightLayerShifts);
	}

	/**
	 * Create a NoiseReductionParameter using defaults for the number of
	 * superlayers, layers and wires
	 * 
	 * @param allowedMissingLayers the number of missing layers permitted.
	 * @param leftLayerShifts the shifts for left leaning tracks. Length should
	 *            equal numLayers.
	 * @param rightLayerShifts the shifts for right leaning tracks. Length
	 *            should equal numLayers.
	 */
	public NoiseReductionParameters(int numLayer, int numWire,
			int allowedMissingLayers, int[] leftLayerShifts,
			int[] rightLayerShifts) {
		_numLayer = numLayer;
		_numWire = numWire;
		_allowedMissingLayers = allowedMissingLayers;
		_leftLayerShifts = leftLayerShifts.clone();
		_rightLayerShifts = rightLayerShifts.clone();
		createWorkSpace();
	}

	/**
	 * Get a default set of parameters with CLAS-like numbers
	 * 
	 * @return a default set of parameters
	 */
	public static NoiseReductionParameters getDefaultParameters() {
		int numLay = 6;
		int numWire = 112;
		int numMissing = 2;
//		int rightShifts[] = { 0, 1, 2, 2, 3, 3 };
//		int leftShifts[] = { 0, 1, 2, 2, 3, 3 };
		int rightShifts[] = { 0, 3, 4, 4, 5, 5 };
		int leftShifts[] = { 0, 3, 4, 4, 5, 5 };
		return new NoiseReductionParameters(numLay, numWire, numMissing,
				leftShifts, rightShifts);
	}

	/**
	 * Get a default set of parameters with CLAS-like numbers
	 * 
	 * @param option LEFT_LEAN (0) for left benders, RIGHT_LEAN (1) for right
	 *            benders
	 * @return a default set of parameters
	 */
	public static NoiseReductionParameters getDefaultCompositeParameters(
			int option) {
		int numLay = 6;
		int numWire = 112;
		int numMissing = 0;

		if (option == LEFT_LEAN) {
			int leftShifts[] = { 0, 8, 11, 14, 17, 20 };
			int rightShifts[] = { 0, 0, 0, 0, 0, 0 };
			return new NoiseReductionParameters(numLay, numWire, numMissing,
					leftShifts, rightShifts);
		}
		else { // right benders
			int leftShifts[] = { 0, 0, 0, 0, 0, 0 };
			int rightShifts[] = { 0, 8, 11, 14, 17, 20 };
			return new NoiseReductionParameters(numLay, numWire, numMissing,
					leftShifts, rightShifts);
		}

	}

	/**
	 * Get the number of layers per superlayer
	 * 
	 * @return the number of layers per superlayer
	 */
	public int getNumLayer() {
		return _numLayer;
	}

	/**
	 * Get the number of wires per layer
	 * 
	 * @return the number of wires per layer
	 */
	public int getNumWire() {
		return _numWire;
	}

	/**
	 * Copy those parameters that can be edited
	 * 
	 * @param source the source object.
	 */
	public void copyEditableParameters(NoiseReductionParameters source) {
		_allowedMissingLayers = source._allowedMissingLayers;
		_leftLayerShifts = source._leftLayerShifts.clone();
		_rightLayerShifts = source._rightLayerShifts.clone();
	}

	/**
	 * Create all the workspace needed to remove the noise.
	 */
	public void createWorkSpace() {
		int needed = 1 + (_numWire - 1) / 64;
		if (needed != _numWordsNeeded) {

			_numWordsNeeded = needed;

			// the segments
			leftSegments = new ExtendedWord(_numWire);
			rightSegments = new ExtendedWord(_numWire);

			_packedData = new ExtendedWord[_numLayer];
			_rawData = new ExtendedWord[_numLayer];
			copy = new ExtendedWord[_numLayer];
			misses = new ExtendedWord[_numLayer];
			workSpace = new ExtendedWord[_numLayer + 1];
			for (int layer = 0; layer < _numLayer; layer++) {
				_packedData[layer] = new ExtendedWord(_numWire);
				_rawData[layer] = new ExtendedWord(_numWire);
				copy[layer] = new ExtendedWord(_numWire);
				misses[layer] = new ExtendedWord(_numWire);
				workSpace[layer] = new ExtendedWord(_numWire);
			}
			workSpace[_numLayer] = new ExtendedWord(_numWire); // one extra
		}
	}

	/**
	 * Checks whether a given wire has a noise hit. Only sensible if analysis ic
	 * complete.
	 * 
	 * @param layer the 0-based layer 0..5
	 * @param wire the 0-base wire 0..
	 * @return true if this was a noise hit--i.e., it is in the raw data but
	 *         anot the analyzed data
	 */
	public boolean isNoiseHit(int layer, int wire) {
		if (_analyzed) {
			boolean inRaw = _rawData[layer].checkBit(wire);
			if (inRaw) {
				boolean inPacked = _packedData[layer].checkBit(wire);
				return !inPacked;
			}
		}
		return false;
	}

	/**
	 * Clear all data
	 */
	public void clear() {
		for (int layer = 0; layer < _numLayer; layer++) {
			_packedData[layer].clear();
		}
		leftSegments.clear();
		rightSegments.clear();
		_analyzed = false;
	}

	/**
	 * Returns the number of allowed missing layers.
	 * 
	 * @return the number of allowed missing layers.
	 */
	public int getAllowedMissingLayers() {
		return _allowedMissingLayers;
	}

	/**
	 * Get the layer shifts for left leaning tracks.
	 * 
	 * @return the layer shifts for left leaning tracks.
	 */
	public int[] getLeftLayerShifts() {
		return _leftLayerShifts;
	}

	/**
	 * Set the left layer shifts used for left leaning segments
	 * 
	 * @param shifts the left layer shifts
	 */
	public void setLeftLayerShifts(int[] shifts) {
		_leftLayerShifts = shifts.clone();
	}

	/**
	 * Get the layer shifts for right leaning tracks.
	 * 
	 * @return the layer shifts for right leaning tracks.
	 */
	public int[] getRightLayerShifts() {
		return _rightLayerShifts;
	}

	/**
	 * Set the right layer shifts used for right leaning segments
	 * 
	 * @param shifts the right layer shifts
	 */
	public void setRightLayerShifts(int[] shifts) {
		_rightLayerShifts = shifts.clone();
	}

	/**
	 * Get the left leaning segment staring wire positions. This is meaningful
	 * only if the analysis has been performed.
	 * 
	 * @return the left leaning segment staring wire positions.
	 */
	public ExtendedWord getLeftSegments() {
		return leftSegments;
	}

	/**
	 * Get the right leaning segment staring wire positions.This is meaningful
	 * only if the analysis has been performed.
	 * 
	 * @return the right leaning segment staring wire positions.
	 */
	public ExtendedWord getRightSegments() {
		return rightSegments;
	}

	/**
	 * Get the packed data arrays.
	 * 
	 * @return the packedData this may be raw or may have had the noise
	 *         removed--use "is anaylyzed" to distinguish
	 */
	public ExtendedWord[] getPackedData() {
		return _packedData;
	}

	/**
	 * Get the packed data for a specific layer
	 * 
	 * @param layer the 0-based layer
	 * @return the packed data for the given layer
	 */
	public ExtendedWord getPackedData(int layer) {
		return _packedData[layer];
	}

	/**
	 * Get the raw data arrays.
	 * 
	 * @return the raw data
	 */
	public ExtendedWord[] getRawData() {
		return _rawData;
	}

	/**
	 * Get the raw data for a specific layer
	 * 
	 * @param layer the 0-based layer
	 * @return the raw data for the given layer
	 */
	public ExtendedWord getRawData(int layer) {
		return _rawData[layer];
	}

	/**
	 * pack a hit
	 * 
	 * @param layer the 0-based layer
	 * @param wire the 0-based wire
	 */
	public void packHit(int layer, int wire) {
		_packedData[layer].setBit(wire);
	}

	/**
	 * pack a hit
	 * 
	 * @param layer the 1-based layer
	 * @param wire the 1-based wire
	 */
	public void packHitOneBased(int layer, int wire) {
		_packedData[layer - 1].setBit(wire - 1);
	}

	/**
	 * Set new raw data. The analyzed flag is set to false.
	 * 
	 * @param packedData the packedData to set. This should be new raw data.
	 */
	public void setPackedData(ExtendedWord[] packedData) {
		_packedData = packedData;
		_analyzed = false;
	}

	/**
	 * @return the analyzed flag. If <code>tue</code> the data have been
	 *         anaylzed, and noise bits removed from the packedData arrays.
	 */
	public boolean isAnalyzed() {
		return _analyzed;
	}

	/**
	 * Remove the noise. This is the actual algorithm.
	 * 
	 */
	public void removeNoise() {

		// keep a copy of the raw data. Not needed but convenient.

		for (int layer = 0; layer < _numLayer; layer++) {
			ExtendedWord.copy(_packedData[layer], _rawData[layer]);
		}

		// first find the left and then the right leaning segments
		findPossibleSegments(this, LEFT_LEAN);
		findPossibleSegments(this, RIGHT_LEAN);

		cleanFromSegments();
		_analyzed = true;
	}

	private void cleanFromSegments() {
		// now remove the noise first. Set packedData[0] to contain overlap
		// (union) of both
		// sets of segments and its own hits.
		// NOTE: the first layer (layer 0) NEVER has a layer shift.*/
		ExtendedWord.bitwiseOr(leftSegments, rightSegments, copy[0]);
		ExtendedWord.bitwiseAnd(_packedData[0], copy[0], _packedData[0]);

		// start loop at 1 since layer 0 never bled
		for (int i = 1; i < _numLayer; i++) {

			// copy segments onto a given layer and bleed to create left and
			// right buckets

			ExtendedWord.copy(leftSegments, copy[i]);
			copy[i].bleedLeft(_leftLayerShifts[i]);

			ExtendedWord.copy(rightSegments, workSpace[0]);
			workSpace[0].bleedRight(_rightLayerShifts[i]);

			// combine left and right buckets
			ExtendedWord.bitwiseOr(copy[i], workSpace[0], copy[i]);

			// now get overlap of original data with buckets
			ExtendedWord.bitwiseAnd(_packedData[i], copy[i], _packedData[i]);
		}
	}

	/**
	 * Computes the intersection of this parameter set with another. This is
	 * only used when "this" is an ordinary chamber and the other (passed in) is
	 * from a composite chamber. In other words, this is where the second pass
	 * occurs.
	 * 
	 * @param opt LEFT_LEAN (0) for left benders, RIGHT_LEAN (1) for right
	 *            benders
	 * @param compositeData this will be a "layer" from the composite detector
	 *            after it (the composite detector) has gone through its own
	 *            1-pass noise removal. What happens is those segments from the
	 *            real chamber that correspond to noise in the composite chamber
	 *            are removed and the real data is refiltered (cleaned) from the
	 *            reduced segments
	 */
	public void secondPass(int opt, ExtendedWord compositeData) {
		if (opt == LEFT_LEAN) {
			ExtendedWord.bitwiseAnd(leftSegments, compositeData, leftSegments);
		}
		else { // RIGHT_LEAN
			ExtendedWord.bitwiseAnd(rightSegments, compositeData,
					rightSegments);
		}
		cleanFromSegments();
	}

	/**
	 * Find possible segments.
	 * 
	 * @param data the actual data.
	 * @param parameters the parameters and workspace.
	 * @param direction either left or right.
	 */
	private void findPossibleSegments(NoiseReductionParameters parameters,
			int direction) {

		// set misses to all 1's. That makes our "reservoir" of misses
		for (int i = 0; i < parameters._allowedMissingLayers; i++) {
			parameters.misses[i].fill();
		}

		ExtendedWord segments = null;

		// copy the data. Bleed based on lean. If looking for right leaners,
		// bleed left to try to find a complete "vertical" segment. Similarly
		// for
		// left leaners--bleed right.
		// segments start out as copy of first layer.
		if (direction == LEFT_LEAN) {
			segments = parameters.leftSegments;
			for (int i = 0; i < parameters.getNumLayer(); i++) {
				ExtendedWord.copy(parameters._packedData[i],
						parameters.copy[i]);
				parameters.copy[i].bleedRight(parameters._leftLayerShifts[i]);
			}
			ExtendedWord.copy(parameters._packedData[0], segments);

		}
		else { // right leaners
			segments = parameters.rightSegments;
			for (int i = 0; i < parameters.getNumLayer(); i++) {
				ExtendedWord.copy(parameters._packedData[i],
						parameters.copy[i]);
				parameters.copy[i].bleedLeft(parameters._rightLayerShifts[i]);
			}
			ExtendedWord.copy(parameters._packedData[0], segments);
		}

		// now .AND. the other layers, which have been shifted to accommodate
		// the buckets
		int numCheck = 0;

		for (int i = 0; i < parameters.getNumLayer();) {
			if (i > 0) {
				ExtendedWord.bitwiseAnd(segments, parameters.copy[i], segments);
			}

			// Now take missing layers into account. missingLayers
			// is the max number of missing layers allowed. However
			// there is no need to check more misses the layer that we
			// are presently investigating.*

			if (++i < parameters._allowedMissingLayers) { /*
														   * note from this step
														   * i is the "NEXT"
														   * layer
														   */
				numCheck = i;
			}
			else {
				numCheck = parameters._allowedMissingLayers;
			}

			// note: numCheck is always > 0 unless a level shift is set
			// to zero which is unlikely. (in which case segments will
			// be unnecessarily copied onto workspace[0] and back again.
			// The algorithm still would work.

			ExtendedWord.copy(segments, parameters.workSpace[0]);
			for (int j = 0; j < numCheck; j++) {

				// first step: use whatever misses are left for this j
				ExtendedWord.bitwiseOr(parameters.workSpace[j],
						parameters.misses[j], parameters.workSpace[j + 1]);

				// second step: remove used up misses
				ExtendedWord.bitwiseAnd(parameters.misses[j],
						parameters.workSpace[j], parameters.misses[j]);
			}
			ExtendedWord.copy(parameters.workSpace[numCheck], segments);

		} /* end of layer loop */
	}

	/**
	 * Get the occupancy of the raw data. This should only be used by ced
	 * proper, not the test program.
	 * 
	 * @return the occupancy of the raw. Multiply by 100 to express as percent.
	 */
	public double getRawOccupancy() {
		return getOccupancy(_rawData);
	}

	/**
	 * Get the occupancy of the packed data. This should only be used by ced
	 * proper, not the test program.
	 * 
	 * @return the occupancy of the raw. Multiply by 100 to express as percent.
	 */
	public double getNoiseReducedOccupancy() {
		return getOccupancy(_packedData);
	}

	/**
	 * Get the occupancy of a set of chamber data. This should only be used by
	 * ced proper, not the test program.
	 * 
	 * @param data either the raw or packed data.
	 * @return the occupancy. Multiply by 100 to express as percent.
	 */
	private double getOccupancy(ExtendedWord data[]) {
		int numBits = hitCount(data);
		int numWires = GeoConstants.NUM_LAYER * GeoConstants.NUM_WIRE;
		return ((double) numBits) / numWires;
	}

	/**
	 * Set the number of allowed missing layers.
	 * 
	 * @param allowedMissingLayers the number to set
	 */
	public void setAllowedMissingLayers(int allowedMissingLayers) {
		_allowedMissingLayers = allowedMissingLayers;
	}

	private int hitCount(ExtendedWord data[]) {
		int numBits = 0;
		for (int layer = 0; layer < GeoConstants.NUM_LAYER; layer++) {
			numBits += data[layer].bitCount();
		}
		return numBits;
	}

	/**
	 * Total number of raw hits (all layers in this chamber/superlayer)
	 * 
	 * @return number of raw hits
	 */
	public int totalRawHitCount() {
		return (hitCount(_rawData));
	}

	/**
	 * Total number of noise reduced hits (all layers in this
	 * chamber/superlayer)
	 * 
	 * @return number of noise reduced hits
	 */
	public int totalReducedHitCount() {
		return (hitCount(_packedData));
	}

	/**
	 * Check whether snr looks for tracks. If true, it will make a second pass
	 * using composite detector. If false, it will stop at the
	 * segments-in-superlayers level.
	 * 
	 * @return <code>true</code> if a second pass to look for tracks is used.
	 */
	public static boolean lookForTracks() {
		return _lookForTracks;
	}

	/**
	 * Set whether snr looks for tracks. If true, it will make a second pass
	 * using composite detector. If false, it will stop at the
	 * segments-in-superlayers level.
	 * 
	 * @param lookForTracks flag determining if a second pass to look for tracks
	 *            is used.
	 */
	public static void setLookForTracks(boolean lookForTracks) {
		_lookForTracks = lookForTracks;
	}

}
