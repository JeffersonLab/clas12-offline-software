package cnuphys.fastMCed.fastmc;

import java.util.ArrayList;
import java.util.List;

import org.jlab.geom.DetectorHit;

import cnuphys.bCNU.util.Bits;

/**
 * Holds hits separated by sector and superLayer. So the final
 * lists will have a mix of layer and component Ids
 * @author heddle
 *
 */
public class HitHolder {

	private ArrayList<AugmentedDetectorHit> hits[][];
	
	//dimensions are detector specific
	private int _numSector;
	private int _numSuperLayer;

	/**
	 * Create a HitHolder for a specific detector type
	 * @param numSector the number of sectors (can be 0)
	 * @param numSuperLayer the number of superLayers (can be 0)
	 */
	public HitHolder(int numSector, int numSuperLayer) {
		
		_numSector = numSector;
		_numSuperLayer = numSuperLayer;
		
		hits = new ArrayList[_numSector][_numSuperLayer];
		for (int sect0 = 0; sect0 < _numSector; sect0++) {
			for (int supl0 = 0; supl0 < _numSuperLayer; supl0++) {
					hits[sect0][supl0] = new ArrayList<AugmentedDetectorHit>();
			}
		}
	}
	
	/**
	 * Clear all hit data
	 */
	public void clear() {
		for (int sect0 = 0; sect0 < _numSector; sect0++) {
			for (int supl0 = 0; supl0 < _numSuperLayer; supl0++) {
					hits[sect0][supl0] = new ArrayList<AugmentedDetectorHit>();
			}
		}
	}
	
	/**
	 * Fill using a list of DtectorHit objects from FastMC
	 * @param dhits a list of DtectorHit objects from FastMC
	 */
	public void fill(List<DetectorHit> dhits) {
		clear();
		if (dhits != null) {
			for (DetectorHit dhit : dhits) {
				int sect0 = dhit.getSectorId();
				int supl0 = dhit.getSuperlayerId();
				hits[sect0][supl0].add(new AugmentedDetectorHit(dhit));
			}
		}
	}
	
	/**
	 * Get all the hits, all sectors and superLayers
	 * @return all the hits, all sectors and superLayers
	 */
	public ArrayList<AugmentedDetectorHit> getAllHits() {
		ArrayList<AugmentedDetectorHit> allhits = new ArrayList<AugmentedDetectorHit>(totalHitCount()+1);
		for (int sect0 = 0; sect0 < _numSector; sect0++) {
			for (int supl0 = 0; supl0 < _numSuperLayer; supl0++) {
					allhits.addAll(hits[sect0][supl0]);
			}
		}
		return allhits;
	}
	
	/**
	 * Get the list of hits
	 * @param sect0 zero based sector
	 * @param supl0 zero based superlayer
	 * @return the list of hits which will have a mix of layers and component ids
	 */
	public ArrayList<AugmentedDetectorHit> getHits(int sect0, int supl0) {
		if (inRange(sect0, supl0)) {
			return hits[sect0][supl0];
		}
		else {
			System.err.println("Out of range in HitHolder getHits.");
			(new Throwable()).printStackTrace();
			return null;
		}
	}
	
	/**
	 * Get the total hit count all sectors, superlayers and layers
	 * @return the total hit count
	 */
	public int totalHitCount() {
		int sum = 0;
		for (int sect0 = 0; sect0 < _numSector; sect0++) {
			sum += sectorHitCount(sect0);
		}

		return sum;
	}
	
	/**
	 * Get the total hit count for a given sector
	 * 
	 * @param sect0
	 *            the zero based sector
	 * @return the total hit count for a sector
	 */
	public int sectorHitCount(int sect0) {
		int sum = 0;
		for (int supl0 = 0; supl0 < _numSuperLayer; supl0++) {
			sum += hitCount(sect0, supl0);
		}

		return sum;
	}
	
	
	/**
	 * Get the hit count of hits with the matching sector and superLayer.
	 * They will be a mix of layers and components
	 * @param sect0 zero based sector
	 * @param supl0 zero based superLayer
	 * @return the hit count
	 */
	public int hitCount(int sect0, int supl0) {
		if (inRange(sect0, supl0)) {
			return hits[sect0][supl0].size();
		}
		else {
			System.err.println("Out of range in HitHolder hitCount.");
			(new Throwable()).printStackTrace();
			return 0;
		}
	}
	
	//see if the zero based indices are in range
	private boolean inRange(int sect0, int supl0) {
		return (((sect0 >= 0) && (sect0 < _numSector)) &&
				((supl0 >= 0) && (supl0 < _numSuperLayer)));
	}
	
	/**
	 * For a given sector , count the number of unique layer hits
	 * @param sect0 the zero based sector
	 * @return the number of unique layer hits for this sector and superlayer
	 */
	public int sectorUniqueLayerCount(int sect0) {
		int sum = 0;
		for (int supl0 = 0; supl0 < _numSuperLayer; supl0++) {
			sum += superLayerUniqueLayerCount(sect0, supl0);
		}
		return sum;
	}

	
	/**
	 * For a given sector and super layer, count the number of unique layer hits
	 * @param sect0 the zero based sector
	 * @param supl0 the zero based later
	 * @return the number of unique layer hits for this sector and superlayer
	 */
	public int superLayerUniqueLayerCount(int sect0, int supl0) {
		long word = 0;
		for (AugmentedDetectorHit hit : hits[sect0][supl0]) {
			int lay0 = hit.getLayerId();
			word = Bits.setBitAtLocation(word, lay0);
		}
		return Bits.countBits(word);
	}
}
