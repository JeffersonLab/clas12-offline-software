package cnuphys.snr;

public class Adjacency {

	
	/**
	 * Used in second stage analysis. Adjaceny counts that are within "del"
	 * from the given wire, where del is 1 for the layer the wire is
	 * in an increases by 1 for every layer as you move up or down. Thus
	 * for CLAS12, del is [1..5]
	 * @param data probably the clean data after an snr noise analysis
	 * @param numLayer the number of layers, for CLAS12 6
	 * @param numWire the number of wires, for CLAS12 112
	 * @param layer the 0-based layer, for CLAS12 [0..5]
	 * @param wire  the 0-base wire, for CLAS12 [0..111]
	 * @return the adjacency value
	 */
	public static int computeAdjacency(ExtendedWord[] data, int numLayer, int numWire, int layer, int wire) {
		if (data == null) {
			return 0;
		}

		// if no hit, bail
		if (data[layer].checkBit(wire)) {
			int adj = 0;

			//layers below
			for (int tlay = 0; tlay < layer; tlay++) {
				int del = layer - tlay + 1;
				int wiremin = Integer.max(0, wire - del);
				int wiremax = Integer.min((numWire - 1), wire + del);
				for (int twire = wiremin; twire <= wiremax; twire++) {
										
					if (data[tlay].checkBit(twire)) {
						adj += proximityFactor(layer, wire, tlay, twire);
					}
				}
			}

			//layers above
			for (int tlay = (numLayer-1); tlay > layer; tlay--) {
				int del = tlay - layer + 1;
				int wiremin = Integer.max(0, wire - del);
				int wiremax = Integer.min((numWire - 1), wire + del);
				for (int twire = wiremin; twire <= wiremax; twire++) {
					if (data[tlay].checkBit(twire)) {
						adj += proximityFactor(layer, wire, tlay, twire);
					}
				}
			}
			
			//same layer
			int wirePlus = wire+1;
			int wireMinus = wire-1;
			if (wirePlus < numWire) {
				if (data[layer].checkBit(wirePlus)) {
					adj += 2;
				}
			}
			if (wireMinus >= 0) {
				if (data[layer].checkBit(wireMinus)) {
					adj += 2;
				}
			}
			
			return adj;
		}
		else {
			return 0;
		}
		
	}
	
	private static int proximityFactor(int layer, int wire, int tlayer, int twire) {
		
		int delL = Math.abs(layer-tlayer);
		int delW = Math.abs(wire-twire);
		

		int pW = Integer.max(0, 3 - delW);
		int pL = Integer.max(0, pW*(2 - delL));
		
		return pL + pW;
	}

}
