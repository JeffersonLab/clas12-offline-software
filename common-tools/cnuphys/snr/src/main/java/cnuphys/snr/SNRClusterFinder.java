package cnuphys.snr;

import java.util.ArrayList;

public class SNRClusterFinder {
	
	public static final int LEFT_LEAN = NoiseReductionParameters.LEFT_LEAN;
	public static final int RIGHT_LEAN = NoiseReductionParameters.RIGHT_LEAN;

	//the parameters
	private NoiseReductionParameters _params;
	
	//the clusters
	protected ArrayList<SNRCluster> clusters;

	/**
	 * Create a cluster finder, tied to a parameters object
	 * @param params the owner of the cluster object
	 */
	public SNRClusterFinder(NoiseReductionParameters params) {
		_params = params;
	}
	
	/**
	 * Find the clusters.
	 */
	public void findClusters() {
		
		if (clusters == null) {
			clusters = new ArrayList<>();
		}
		else {
			clusters.clear();
		}
		
		int maxShift = Integer.max(_params.maxShift(LEFT_LEAN), _params.maxShift(LEFT_LEAN));
		
		boolean noSegs = _params.leftSegments.isZero() && _params.rightSegments.isZero();
		
		//build clusters
		if (!noSegs) {
			boolean connected = false;
			SNRCluster currentCluster = null;
			
			int maxCount = maxShift + 1;
			int count = 0;
			
			for (int wire = 0; wire < _params.getNumWire(); wire++) {

				boolean inLeft = _params.leftSegments.checkBit(wire);
				boolean inRight = _params.rightSegments.checkBit(wire);

				if (inLeft || inRight) {

					if (!connected) { // create a new one
						currentCluster = createCluster();
						clusters.add(currentCluster);
						count = 0;
						connected = true;
					}
					currentCluster.addSegmentStart(wire);

					if (inLeft) {
						fillWireLists(currentCluster, wire, LEFT_LEAN);
					}
					if (inRight) {
						fillWireLists(currentCluster, wire, RIGHT_LEAN);
					}

					count++;
					connected = count < maxCount;

					if (!connected) {
						currentCluster.clean();
					}
				} // end wire was hit (checkBit)
				else {
					if (currentCluster != null) {
						currentCluster.clean();
					}
					connected = false;
					currentCluster = null;
				}
			} // wire loop
		}
		
		//remove subsets
		int index = 0;
		
		ArrayList<SNRCluster> subsets = new ArrayList<>();
		while (index < clusters.size()-1) {
			SNRCluster cmain = clusters.get(index);
			for (int j = index+1; j < clusters.size(); j++) {
				SNRCluster ctest = clusters.get(j);
				if (cmain.hasSubset(ctest)) {
					subsets.add(ctest);
				}
			}
			clusters.removeAll(subsets);

			subsets.clear();
			index++;
		}

	}

	//create a cluster
	private SNRCluster createCluster() {
		return new SNRCluster(_params.getNumLayer(),  _params.getNumWire());
	}
	
	//fill the wire lists
	private void fillWireLists(SNRCluster cluster, int segStartWire, int direction) {
		for (int lay = 0; lay < _params.getNumLayer(); lay++) {
			_params.addHitsInMask(lay, segStartWire, direction, cluster.wireLists[lay]);
		}

	}
	
	
	/**
	 * Get the list of  clusters
	 * @return the list of clusters
	 */
	public ArrayList<SNRCluster> getClusters() {
		return clusters;
	}
	

}
