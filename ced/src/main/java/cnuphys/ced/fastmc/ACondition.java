package cnuphys.ced.fastmc;

import java.util.List;

import org.jlab.geom.DetectorHit;

import cnuphys.bCNU.util.Bits;

public abstract class ACondition {
	
	protected static FastMCManager _fastMCManager = FastMCManager.getInstance();
	
	//some lund ids
	protected static final int ELECTRON = 11;
	protected static final int PHOTON   = 22;
	protected static final int PROTON   = 2212;
	
	protected boolean active = false;
	
	protected ACondition(boolean active) {
		this.active = active;
	}
	
	public boolean isActive() {
		return active;
	}
	
	public void setActive(boolean val) {
		active = val;
	}
	
	/**
	 * Is the condition passed
	 * @return <code>true</code> if the condition is passed
	 */
	public abstract boolean pass();

	/**
	 * Get a description to but in the list
	 * @return a description to but in the list
	 */
	public abstract String getDescription();
	
	@Override
	public String toString() {
		return getDescription();
	}
	
	protected boolean inNUniqueDCLayers(int lundId, int n) {
		//first test, at least that many total hits
		if (_fastMCManager.particleDCHitCount(lundId) < n) {
			return false;
		}
		
		long sectorHits[] = new long[6];
		
		List<ParticleHits> allHits = _fastMCManager.getFastMCHits();
		if (allHits == null) {
			return false;
		}
		
		for (ParticleHits phits : _fastMCManager.getFastMCHits()) {
			if (phits.lundId() == lundId) {
				List<DetectorHit> dchits = phits.getDCHits();
				if (dchits != null) {
					for (DetectorHit hit : dchits) {
						int sector = hit.getSectorId();
						int superlayer = hit.getSuperlayerId();
						//layer 0..35
						int layer = 6*superlayer + hit.getLayerId();
						sectorHits[sector] = Bits.setBitAtLocation(sectorHits[sector], layer);
//						System.err.println(sectorHits[sector]);
//						System.err.println("SECT: " + (sector+1) + " layer: " + (layer+1) + "  layers: " + 
//						String.format("%064d", new BigInteger(Long.toBinaryString(sectorHits[sector]))));
					}
				}
			}
		}
		
		for (int sect = 0; sect < 6; sect++) {
			if (Bits.countBits(sectorHits[sect]) >= n) {
				return true;
			}
		}
		
		return false;
	}

}
