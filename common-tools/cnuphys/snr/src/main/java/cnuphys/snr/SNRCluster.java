package cnuphys.snr;

/**
 * 
 * @author heddle
 * A cluster candidate is a list of wires with the
 * requirement that there are no more than two wires
 * in a given layer
 */
public class SNRCluster {
		
	/** The number of layers, probably 6 */
	public final int numLayers;
	
	/** The number of wires, probably 112 */
	public final int numWires;

	
	/** a list for the segment starting points. */
	public final SegmentStartList segmentStartList = new SegmentStartList();
	
	/** one wirelist for each layer */
	public final WireList wireLists[];
		
	/** slope of linear fit */
	private double _slope = Double.NaN;
	
	/** intercept of linear fit */
	private double _intercept = Double.NaN;

	
	/**
	 * @param numLayers the number of layers, for CLAS12 6
	 * @param numWires the number of wires, for CLAS12 112
	 */
	public SNRCluster(int numLayers, int numWires) {
		this.numLayers = numLayers;
		this.numWires = numWires;
		wireLists = new WireList[numLayers];

		clear();
	}
	
	/**
	 * Add a wire to the wire list for a given layer. The wireList will
	 * enforce no duplicates, but will keep repeat count
	 * @param layer the zero-based layer, for CLAS12 [0..5]
	 * @param wire the zero-based wire, for CLAS12 [0..111]
	 */
	public void add(int layer, int wire) {
		wireLists[layer].add(wire);
	}
	
	/**
	 * Add a segment (candidate) start wire
	 * @param wire the zero-based wire, for CLAS12 [0..111]
	 * @param numMissing the number of missing layers required
	 */
	public void addSegmentStart(int wire) {
		segmentStartList.add(wire);
	}
		
	/**
	 * Clear the wire lists. So the object can be reused, but only for the same
	 * sector, superlayer, and numLayers
	 */
	public void clear() {
		
		segmentStartList.clear();

		for (int layer = 0; layer < numLayers; layer++) {
			if (wireLists[layer] == null) {
				wireLists[layer] = new WireList(numWires);
			}
			else {
				wireLists[layer].clear();
			}
		}
		

	}
	
	/**
	 * String representation
	 * @return a String representation of the cluster
	 */
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer(1024);
		
		sb.append(segmentStartList + " ");
		sb.append("{");
		for (WireList wl : wireLists) {
			if (wl != null) {
				sb.append(wl + " ");
			}
		}
		sb.append("}  ");
		
		if (Double.isNaN(_slope)) {
			computeLine();
		}
		sb.append(String.format(" M: %5.2f ", _slope));
		return sb.toString();
	}
	
	public void packData(NoiseReductionParameters params) {
		
	}
	
	/**
	 * Get the slope of the linear fit
	 * @return
	 */
	public  double getSlope() {
		if (Double.isNaN(_slope)) {
			computeLine();
		}
		return _slope;
	}
	
	/**
	 * This removes wires from the wire list if there are more than two in a given
	 * layer.
	 */
	public void clean() {
		for (int lay = 0; lay < numLayers; lay++) {
			if (wireLists[lay].size() > 2) {
				wireLists[lay].sort();
				
				while (wireLists[lay].size() > 2) {
					wireLists[lay].remove(wireLists[lay].size()-1);
				}
				
				_slope =  Double.NaN;
			}
		}
	}
	
	/**
	 * This computes slope and intercept a line using the layer as the X value and the 
	 * average wire number as the Y so a slope of 0 looks "vertical".
	 * We do it this way to avoid the possibility of an infinite slope.
	 */
	public void computeLine() {
		double sumx = 0;
		double sumy = 0;
		double sumxy = 0;
		double sumx2 = 0;
		
		int n = 0;
		for (int layer = 0; layer < numLayers; layer++) {
			double avgWire = wireLists[layer].averageWirePosition();
			if (!Double.isNaN(avgWire)) {
				n++;
				sumx += layer;
				sumy += avgWire;
				sumxy += layer*avgWire;
				sumx2 += (layer*layer);
			}
		}
		
		if (n >1) {
			_slope = (n*sumxy - sumx*sumy)/(n*sumx2 - sumx*sumx);
			_intercept = (sumy - _slope*sumx)/n;
		}
		else {
			_slope = Double.NaN;
			_intercept = Double.NaN;
		}
	}
	
	/**
	 * Used to draw a linear fit to the cluster
	 * @param layer the layer
	 * @return the wire position as a real (with fractional part) 
	 */
	public double getWirePosition(int layer) {
		if (Double.isNaN(_slope)) {
			computeLine();
		}
		
		//still NaN?
		if (Double.isNaN(_slope)) {
			return Double.NaN;
		}
		
		return _slope*layer + _intercept;
	}
	
	/**
	 * The given cluster is a subset of this cluster if
	 * all wirelists are subsets
	 * @param c the given cluster
	 * @return <code>true</code> if the given cluster is a subset of this cluster
	 */
	public boolean hasSubset (SNRCluster c) {
		
		for (int lay =  0; lay < numLayers; lay++) {
			if (!wireLists[lay].hasSubset(c.wireLists[lay])) {
				return false;
			}
		}
		
		return true;
	}

}
