package org.jlab.rec.fmt.hit;

/**
 * A hit that was used in a fitted track.
 *
 * @author ziegler
 * @author benkel
 * @author devita
*/

public class FittedHit extends Hit implements Comparable<Hit> {

	/**
          * @param index
	 * @param layer (1...6)
	 * @param strip (1...1024)
          * @param time
	 * @param energy 
	 */

	public FittedHit(int index, int layer, int strip, double energy, double time) {
		super(index, layer, strip, energy, time);
	}
        
         public FittedHit(Hit hit) {
             super(hit.getIndex(), hit.getLayer(), hit.getStrip(), hit.getEnergy(), hit.getTime());
         }

	private double _residual;             // distance to track intersect
	private int _TrkgStatus = -1 ;        //  TrkgStatusFlag factor (-1: no fit; 0: global helical fit; 1: KF fit)

         private int _AssociatedCrossIndex = -1;
         private int _AssociatedTrackIndex = -1;

         public double getdocaToTrk() {
		return _residual;
	}

	public void setdocaToTrk(double _docaToTrk) {
		this._residual = _docaToTrk;
	}

	/**
	 *
	 * @return an integer representative of the stage of the pattern recognition and subsequent KF
     * fit for that hit. -1: no fit; 0: global helical fit; 1: KF fit
	 */
	public int getTrkgStatus() {
		return _TrkgStatus;
	}

	/**
	 * @param trkgStatus is an integer representative of the stage of the pattern recognition and subsequent KF fit
	 * for that hit. -1: no fit; 0: global helical fit; 1: KF fit
     *
	 */
	public void setTrkgStatus(int trkgStatus) {
		_TrkgStatus = trkgStatus;
	}

	/**
	 *
	 * @param arg0 the other hit
	 * @return an int used to sort a collection of hits by layer number
	 */
	public int compareTo(FittedHit arg0) {
		if (this.getLayer() > arg0.getLayer()) return 1;
		else                                     return 0;
	}

	public double getResidual() {
            return this._residual;
	}

	public void setResidual(double trackLocalY) {
            this._residual = this.getStripLocalSegment().origin().y()-trackLocalY;
	}

	public int getCrossIndex() {
		return _AssociatedCrossIndex;
	}

	public void setCrossIndex(int _AssociatedCrossIndex) {
		this._AssociatedCrossIndex = _AssociatedCrossIndex;
	}

	public int getTrackIndex() {
		return _AssociatedTrackIndex;
	}

	public void setTrackIndex(int _AssociatedTrackIndex) {
		this._AssociatedTrackIndex = _AssociatedTrackIndex;
	}
}
