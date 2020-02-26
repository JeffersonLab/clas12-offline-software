package cnuphys.fastMCed.fastmc;

import org.jlab.geom.DetectorHit;
import org.jlab.geom.DetectorId;

/**
 * Augmented hits wrap an underlying FastMC Detector hit so that additional information 
 * cane be attached.
 * @author heddle
 *
 */
public class AugmentedDetectorHit  {
	
	/** The underlying raw detector hit from FastMC */
	public DetectorHit _hit;
		
	//was this noise? (relevant for DC only)
	private boolean _noise;
	
	/**
	 * Create from a FastMC hit. This just wraps it with additional information.
	 * @param hit
	 */
	public AugmentedDetectorHit(DetectorHit hit) {
		_hit = hit;
	}
	
	/**
	 * Get the detector id enum.
	 * @return the detector id enum value.
	 */
	public DetectorId getDetectorId() {
		return _hit.getDetectorId();
	}
	
	/**
	 * get the energy from the underlying raw FastMC DetectorHit
	 * @return the energy (Units?)
	 */
	public double getEnergy() {
		return _hit.getEnergy();
	}
	
	/**
	 * get the time from the underlying raw FastMC DetectorHit
	 * @return the time (Units?)
	 */
	public double getTime() {
		return _hit.getTime();
	}
	
	/**
	 * Convenience check as to whether this is for a given DetectorId
	 * @return <code>true</code> if this hit is for the given detector.
	 */
	public boolean isDetectorHit(DetectorId id) {
		return _hit.getDetectorId() == id;
	}
	
	/**
	 * Is this considered a noise hit (Relevant for DC only.)
	 * @return <code>true</code> if this is a DC noise hit as determined
	 * by the cnuphys SNR package. Return <code>false</code> it it wasn't,
	 * or if this hit is to a Drift Chamber hit.
	 */
	public boolean isNoise() {
		return _noise;
	}

	/**
	 * Set whether this is noise (Relevant for DC only.)
	 * @param noise the noise parameter value
	 */
	public void setNoise(boolean noise) {
		_noise = noise;
	}
	
	/**
	 * Get the zero-based sector from the underlying raw FastMC DetectorHit
	 * @return the zero-based sector
	 */
	public int getSectorId() {
		return _hit.getSectorId();
	}
	
	/**
	 * Get the zero-based superlayer from the underlying raw FastMC DetectorHit
	 * @return the zero-based superlayer
	 */
	public int getSuperlayerId() {
		return _hit.getSuperlayerId();
	}

	/**
	 * Get the zero-based layer from the underlying raw FastMC DetectorHit
	 * @return the zero-based layer
	 */
	public int getLayerId() {
		return _hit.getLayerId();
	}
	
	/**
	 * Get the zero-based component from the underlying raw FastMC DetectorHit
	 * @return the zero-based conponent
	 */
	public int getComponentId() {
		return _hit.getComponentId();
	}


}
