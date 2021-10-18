package cnuphys.snr;

import java.util.ArrayList;

/**
 * All the parameters needed for noise reduction. Each superlayer should have
 * its own object. Now should be thread safe.
 * 
 * @author heddle
 */
public class NoiseReductionParameters {
	
	//the analysis level
	public static SNRAnalysisLevel _analysisLevel = SNRAnalysisLevel.ONESTAGE;

	// track leaning directions
	public static final int LEFT_LEAN = 0;
	public static final int RIGHT_LEAN = 1;
	

	private int _numWordsNeeded;

	/** the number of layers per superlayer */
	private int _numLayer;

	/** the number of wires per layer */
	private int _numWire;

	/** The number of missing layers allowed */
	private int _allowedMissingLayers;

	/** The shifts for left leaning tracks. */
	private int _leftLayerShifts[];

	/** The shifts for right leaning tracks. */
	private int _rightLayerShifts[];

	/**
	 * cumulative left segments. These are "results". When
	 * analysis is complete, this will contain an on bit
	 * at any location in layer 1 that is a potential start
	 * of a left leaning segment.
	 */
	protected ExtendedWord leftSegments;

	/**
	 * cumulative right segments. These are "results". When
	 * analysis is complete, this will contain an on bit
	 * at any location in layer 1 that is a potential start
	 * of a right leaning segment.
	 */
	protected ExtendedWord rightSegments;

	/**
	 * A workspace used for storing a reservoir of misses. When
	 * the analysis is done, this can be used to determine a quality
	 * factor for the potential left segment, based on the number of
	 * misses that had to be used.
	 */
	private ExtendedWord _leftMisses[];
	
	/**
	 * A workspace used for storing a reservoir of misses. When
	 * the analysis is done, this can be used to determine a quality
	 * factor for the potential right segment, based on the number of
	 * misses that had to be used.
	 */
	private ExtendedWord _rightMisses[];


	/** This is the raw data. */
	private ExtendedWord _rawData[];
	
	/** The cleaned data, with noise removed */
	private ExtendedWord _cleanData[];
	
	/** The data bled left, used for RIGHT segments */
	private ExtendedWord _bledLeftData[];
	
	/** The data bled right, used for LEFT segments */
	private ExtendedWord _bledRightData[];


	//for workspace
	private ExtendedWord _oldSegments;
	private ExtendedWord _leftClean;
	private ExtendedWord _rightClean;


	// flag that specifies whether the data have been analyzed
	private boolean _analyzed = false;
	
	//experimental stage 2 adjecency test
	private int _adjacencyThreshold = 6;
	
	//experimental stage2 cluster finder
	private SNRClusterFinder _clusterFinder;


	/**
	 * Create the parameters used for SNR analysis
	 */
	public NoiseReductionParameters() {
	}

	/**
	 * Create a NoiseReductionParameter using CLAS12 defaults for the number of
	 * superlayers, layers and wires
	 * 
	 * @param allowedMissingLayers the number of missing layers permitted.
	 * @param leftLayerShifts      the shifts for left leaning tracks. Length should
	 *                             equal numLayers.
	 * @param rightLayerShifts     the shifts for right leaning tracks. Length
	 *                             should equal numLayers.
	 */
	public NoiseReductionParameters(int allowedMissingLayers, int[] leftLayerShifts, int[] rightLayerShifts) {
		this(6, 112, allowedMissingLayers, leftLayerShifts, rightLayerShifts);
	}

	/**
	 * Create a NoiseReductionParameter using defaults for the number of
	 * superlayers, layers and wires
	 * 
	 * @param allowedMissingLayers the number of missing layers permitted.
	 * @param leftLayerShifts      the shifts for left leaning tracks. Length should
	 *                             equal numLayers.
	 * @param rightLayerShifts     the shifts for right leaning tracks. Length
	 *                             should equal numLayers.
	 */
	public NoiseReductionParameters(int numLayer, int numWire, int allowedMissingLayers, int[] leftLayerShifts,
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
		int rightShifts[] = { 0, 3, 4, 4, 5, 5 };
		int leftShifts[] = { 0, 3, 4, 4, 5, 5 };
		return new NoiseReductionParameters(numLay, numWire, numMissing, leftShifts, rightShifts);
	}

	/**
	 * Get the adjacency threshold, used in stage 2 analysis
	 * where we look for clusters. This is used to remove additional
	 * noise in the vicinity of segments that do not pass a connectedness
	 * test.
	 * @return the adjacency threshold
	 */
	public int getAdjacencyThreshold() {
		return _adjacencyThreshold;
	}
	
	/**
	 * Set the adjacency threshold, used in stage 2 analysis
	 * where we look for clusters. This is used to remove additional
	 * noise in the vicinity of segments that do not pass a connectedness
	 * test.
	 * @param adjacencyThreshold hte new threshold
	 */
	public void setAdjacencyThreshold(int adjacencyThreshold) {
		_adjacencyThreshold = adjacencyThreshold;
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

			_rawData = new ExtendedWord[_numLayer];
			_cleanData = new ExtendedWord[_numLayer];
			_bledLeftData = new ExtendedWord[_numLayer];
			_bledRightData = new ExtendedWord[_numLayer];
			_leftMisses = new ExtendedWord[_numLayer];
			_rightMisses = new ExtendedWord[_numLayer];
			
			
			for (int layer = 0; layer < _numLayer; layer++) {
				_rawData[layer] = new ExtendedWord(_numWire);
				_cleanData[layer] = new ExtendedWord(_numWire);
				_bledLeftData[layer] = new ExtendedWord(_numWire);
				_bledRightData[layer] = new ExtendedWord(_numWire);
				_leftMisses[layer] = new ExtendedWord(_numWire);
				_rightMisses[layer] = new ExtendedWord(_numWire);
			}
			
			//workspace
			_oldSegments =  new ExtendedWord(_numWire);
			_leftClean =  new ExtendedWord(_numWire);
			_rightClean =  new ExtendedWord(_numWire);

		}
	}

	/**
	 * Checks whether a given wire has a noise hit. Only sensible if analysis is
	 * complete.
	 * 
	 * @param layer the 0-based layer 0..5
	 * @param wire  the 0-base wire 0..
	 * @return true if this was a noise hit--i.e., it is in the raw data but not
	 *         the analyzed data
	 */
	public boolean isNoiseHit(int layer, int wire) {
		if (_analyzed) {
			boolean inRaw = _rawData[layer].checkBit(wire);
			if (inRaw) {
				boolean inClean = _cleanData[layer].checkBit(wire);
				return !inClean;
			}
		}
		return false;
	}

	/**
	 * Clear all data
	 */
	public void clear() {
		for (int layer = 0; layer < _numLayer; layer++) {
			_rawData[layer].clear();
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
	 * Get the left leaning segment staring wire positions. This is meaningful only
	 * if the analysis has been performed.
	 * 
	 * @return the left leaning segment staring wire positions.
	 */
	public ExtendedWord getLeftSegments() {
		return leftSegments;
	}

	/**
	 * Get the right leaning segment staring wire positions.This is meaningful only
	 * if the analysis has been performed.
	 * 
	 * @return the right leaning segment staring wire positions.
	 */
	public ExtendedWord getRightSegments() {
		return rightSegments;
	}


	/**
	 * pack a hit into the raw data
	 * 
	 * @param layer the 0-based layer
	 * @param wire  the 0-based wire
	 */
	public void packHit(int layer, int wire) {
		_rawData[layer].setBit(wire);
	}


	/**
	 * Set new raw data. The analyzed flag is set to false.
	 * 
	 * @param rawData the new raw data to set.
	 */
	public void setPackedData(ExtendedWord[] packedData) {
		_rawData = packedData;
		_analyzed = false;
	}

	/**
	 * @return the analyzed flag. If <code>tue</code> the data have been anaylzed,
	 *         and noise bits removed from the packedData arrays.
	 */
	public boolean isAnalyzed() {
		return _analyzed;
	}
	

	/**
	 * Remove the noise. This is the actual algorithm.
	 */
	public void removeNoise() {
		
		
		// first find the left and then the right leaning segments
		initBledData(_rawData);
		findPossibleSegments(LEFT_LEAN);
		findPossibleSegments(RIGHT_LEAN);

		//now clean the data (remove the noise)
		cleanFromSegments();

		if (_analysisLevel == SNRAnalysisLevel.TWOSTAGE) {

			if (hitCount(_cleanData) > 0) {

				applyAdjacency();
				initBledData(_cleanData);
				findPossibleSegments(LEFT_LEAN);
				findPossibleSegments(RIGHT_LEAN);
				cleanFromSegments();
				applyAdjacency(); // yep, again

				// find cluster candidates
				if (_clusterFinder == null) {
					_clusterFinder = new SNRClusterFinder(this);
				}
				_clusterFinder.findClusters();
			}
		}
		
		_analyzed = true;
	}
	
	
	//apple the ajacency hueristic
	private void applyAdjacency() {

		boolean again = true;
		int oldHitCount = hitCount(_cleanData);
		int newHitCount = -1;

		while (again) {
			stage2Analysis();
			newHitCount = hitCount(_cleanData);

			if (newHitCount == oldHitCount) {
				again = false;
			} else {
				oldHitCount = newHitCount;
			}

		}
	}

	//prepare the bled data for finding segments
	private void initBledData(ExtendedWord[] data) {
		//do the bleeding. First layer (layer 0) never bled
		ExtendedWord.copy(data[0], _bledLeftData[0]);
		ExtendedWord.copy(data[0], _bledRightData[0]);
		
		for (int lay = 1; lay < _numLayer; lay++) {
			ExtendedWord.copy(data[lay], _bledLeftData[lay]);
			ExtendedWord.copy(data[lay], _bledRightData[lay]);
			_bledLeftData[lay].bleedLeft(_leftLayerShifts[lay]);
			_bledRightData[lay].bleedRight(_rightLayerShifts[lay]);
		}

	}
	
	//experimental stage 2 uses the adjacency test
	//to remove additional noise
	private void stage2Analysis() {
//		System.err.println("Stage 2 analysis");
		
		for (int layer = 0; layer < _numLayer; layer++) {
			for (int wire = 0; wire < _numWire; wire++) {
				if (_cleanData[layer].checkBit(wire)) {
					if (computeAdjacency(layer, wire) < _adjacencyThreshold) {
						_cleanData[layer].clearBit(wire);
					}
				}
			}
		}
		
		
	}

 
	//d = a & (Left | Right)
	private void aAndLeftOrRight(ExtendedWord a, ExtendedWord left, ExtendedWord right, ExtendedWord d) {
		ExtendedWord.bitwiseOr(left, right, d);
		ExtendedWord.bitwiseAnd(d, a, d);
	}
	
	
	//used to mask for clean data
	private void rawAndSegBledRight(ExtendedWord raw, ExtendedWord seg, int shift, ExtendedWord c) {
		ExtendedWord.copy(seg, c);
		c.bleedRight(shift);
		ExtendedWord.bitwiseAnd(c, raw , c);
	}
	
	//used to mask for clean data
	private void rawAndSegBledLeft(ExtendedWord raw, ExtendedWord seg, int shift, ExtendedWord c) {
		ExtendedWord.copy(seg, c);
		c.bleedLeft(shift);
		ExtendedWord.bitwiseAnd(c, raw , c);
	}


	// this creates the masks and .ANDS. them with the data
	private void cleanFromSegments() {
		// Set clean[0] to contain overlap
		// (union) of both sets of segments and its own hits.
		// NOTE: the first layer (layer 0) NEVER has a layer shift.*/
		
		aAndLeftOrRight(_rawData[0], leftSegments, rightSegments, _cleanData[0]);
		
		
		// start loop at 1 since layer 0 never bled
		for (int lay = 1; lay < _numLayer; lay++) {
			rawAndSegBledLeft(_rawData[lay], leftSegments, _leftLayerShifts[lay], _leftClean);
			rawAndSegBledRight(_rawData[lay], rightSegments, _rightLayerShifts[lay], _rightClean);
			ExtendedWord.bitwiseOr(_leftClean, _rightClean, _cleanData[lay]);
		}
	}
	
	/**
	 * Get the maximum shift for a given direction. It is assumed that
	 * this is in the shift for the last layer.
	 * @param direction either left (0) or right (1)
	 * @return the maximum shift for a given direction
	 */
	public int maxShift(int direction) {
		return (direction == LEFT_LEAN) ? _leftLayerShifts[_numLayer-1] : _rightLayerShifts[_numLayer-1];
	}

	/**
	 * Add wires that are in the masks of the clean data corresponding to a given
	 * wire (segment candidate start), layer, and direction
	 * @param layer the 0 based layer, for CLAS12 [0..5]
	 * @param wire the 0 based wire, for CLAS12 [0..111]
	 * @param direction either left (0) or right (1)
	 * @param list the list to add to
	 */
	public void addHitsInMask(int layer, int wire, int direction, WireList list) {
		
		if (direction == LEFT_LEAN) {
			int shift = _leftLayerShifts[layer];
			int maxWire = Integer.min(_numWire-1, wire+shift);
			for (int tw = wire; tw <= maxWire; tw++) {
				if (_cleanData[layer].checkBit(tw)) {
					list.add(tw);
				}
			}
		}
		else {
			int shift = _rightLayerShifts[layer];
			int minWire = Integer.max(0, wire-shift);
			for (int tw = wire; tw >= minWire; tw--) {
				if (_cleanData[layer].checkBit(tw)) {
					list.add(tw);
				}
			}
		}
	}


	/**
	 * Find possible segments.
	 * 
	 * @param data       the actual data.
	 * @param parameters the parameters and workspace.
	 * @param direction  either left or right.
	 */
	private void findPossibleSegments(int direction) {
		
		ExtendedWord misses[] = (direction == LEFT_LEAN) ? _leftMisses : _rightMisses;

		// set misses to all 1's. That makes our "reservoir" of misses
		for (int i = 0; i < _allowedMissingLayers; i++) {
			misses[i].fill();
		}

		ExtendedWord segments = null;
		ExtendedWord bledData[] = null;

		// copy the data. Bleed based on lean. If looking for right leaners,
		// bled left data.
		if (direction == LEFT_LEAN) {
			segments = leftSegments;
			bledData = _bledRightData;

		} else { // right leaners use bled left data
			segments = rightSegments;
			bledData = _bledLeftData;
		}
		
		// segments start out as copy of first layer.
		//bledData[0] is same as first raw data layer
		ExtendedWord.copy(bledData[0], segments);

		for (int lay = 0; lay < _numLayer; lay++) {
			if (lay > 0) {
				ExtendedWord.bitwiseAnd(segments, bledData[lay], segments);
			}


			// See how many deep we can go into the missing hit reservoir
			// There is no need to check more misses the layer that we
			// are presently investigating.
			int numToCheck = Integer.min((lay+1), _allowedMissingLayers);
			
			
			for (int j = 0; j < numToCheck; j++) {
				ExtendedWord.copy(segments,  _oldSegments);
				ExtendedWord.bitwiseOr(segments, misses[j], segments);
				ExtendedWord.bitwiseAnd(misses[j], _oldSegments, misses[j]);
			}
			
		}
	}
	
	/**
	 * Get the number of missing layers used to find a segment candidate
	 * starting at the given wire in layer 1 (1..6)
	 * @param direction LEFT_LEAN (0) or RIGHT_LEAN (1)
	 * @param wire the 0-based wire
	 * @return the number of missing layers used at that position
	 */
	public int missingLayersUsed(int direction, int wire) {
		ExtendedWord misses[] = (direction == LEFT_LEAN) ? _leftMisses : _rightMisses;
		
		int numUsed = 0;
		
		for (int lay = 0; lay < _allowedMissingLayers; lay++) {			

			if (misses[lay].checkBit(wire)) {
				return numUsed;
			}
			
			numUsed++;
		}
		
		return numUsed;
	}

	/**
	 * Get the occupancy of the raw data. This should only be used by ced proper,
	 * not the test program.
	 * 
	 * @return the occupancy of the raw. Multiply by 100 to express as percent.
	 */
	public double getRawOccupancy() {
		return getOccupancy(_rawData);
	}

	/**
	 * Get the occupancy of the packed data. This should only be used by ced proper,
	 * not the test program.
	 * 
	 * @return the occupancy of the raw. Multiply by 100 to express as percent.
	 */
	public double getNoiseReducedOccupancy() {
		return getOccupancy(_cleanData);
	}

	/**
	 * Get the occupancy of a set of chamber data. This should only be used by ced
	 * proper, not the test program.
	 * 
	 * @param data either the raw or packed data.
	 * @return the occupancy. Multiply by 100 to express as percent.
	 */
	private double getOccupancy(ExtendedWord data[]) {
		int numBits = hitCount(data);
		int numWires = _numLayer * _numWire;
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

	//get the total hit count all layers
	private int hitCount(ExtendedWord data[]) {
		int numBits = 0;
		for (int layer = 0; layer < _numLayer; layer++) {
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
	 * Total number of noise reduced hits (all layers in this chamber/superlayer)
	 * 
	 * @return number of noise reduced hits
	 */
	public int totalReducedHitCount() {
		return (hitCount(_cleanData));
	}
	
	/**
	 * Set the analysis level
	 * OneStage: the classic SNR noise analysis
	 * TwoStage: the centers of mass are used to remove noise within the masks
	 * but disjoint from the segment (hopefully). This might result in the loss
	 * of the segment!
	 * @param level new level
	 */
	public static void setSNRAnalysisLevel(SNRAnalysisLevel level) {
		_analysisLevel = level;
	}
	
	/**
	 * Get the cluster finder
	 * @return ther cluster finder
	 */
	public SNRClusterFinder getClusterFinder() {
		return _clusterFinder;
	}
	
	/**
	 * Used in second stage analysis. Adjaceny counts that are within "del" from the
	 * given wire, where del is 1 for the layer the wire is in an increases by 1 for
	 * every layer as you move up or down. Thus for CLAS12, del is [1..5]
	 * 
	 * @param layer the 0-based layer, for CLAS12 [0..5]
	 * @param wire  the 0-base wire, for CLAS12 [0..111]
	 * @return the adjacency value
	 */
	public int computeAdjacency(int layer, int wire) {
		return Adjacency.computeAdjacency(_cleanData, _numLayer, _numWire, layer, wire);
	}
	
	/**
	 * Get the list of clusters
	 * @return the list of clusters
	 */
	public ArrayList<SNRCluster> getClusters() {
		return (_clusterFinder == null) ? null : _clusterFinder.getClusters();
	}
	
   /**
     * @deprecated doesn't do anything, kept for backwards capability.
     * @param lookForTracks
     */
    public static void setLookForTracks(boolean lookForTracks) {
    }
    
            


}
