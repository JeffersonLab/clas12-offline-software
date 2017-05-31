package cnuphys.snr.test;

public class HitTest {

    public enum HitType {
	NOISE, TRACK, UNKNOWN
    };

    private int layer;
    private int wire;
    private HitType actualHitType;
    private HitType computedHitType = HitType.UNKNOWN;

    public HitTest(int layer, int wire, HitType actualHitType) {
	super();
	this.layer = layer;
	this.wire = wire;
	this.actualHitType = actualHitType;
    }

    /**
     * Get the layer index [0..] of the hit.
     * 
     * @return the layer index [0..] of the hit.
     */
    public int getLayer() {
	return layer;
    }

    /**
     * Get the wire index [0..] of the hit.
     * 
     * @return the wire index [0..] of the hit.
     */
    public int getWire() {
	return wire;
    }

    /**
     * Increments the hit count by one. Used for accumulating hits for a hit
     * map. Note: the hit is instantiated with a hit count of one.
     */
    public void incrementHitCount() {

    }

    /**
     * Gets the actual hit type. This is "truth." It can be compared to a
     * computed hit type to check the analysis.
     * 
     * @return the actual hit type. This is "truth." It can be compared to a
     *         computed hit type to check the analysis.
     */
    public HitType getActualHitType() {
	return actualHitType;
    }

    /**
     * Gets the computed hit type. If this hit was never analyzed, it will be
     * UNKNOWN.
     * 
     * @return the computed hit type. If this hit was never analyzed, it will be
     *         UNKNOWN.
     */
    public HitType getComputedHitType() {
	return computedHitType;
    }

    public void setComputedHitType(HitType computedHitType) {
	this.computedHitType = computedHitType;
    }
}
