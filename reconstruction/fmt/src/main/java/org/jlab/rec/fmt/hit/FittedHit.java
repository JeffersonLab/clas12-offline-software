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
             super(hit.get_Index(), hit.get_Layer(), hit.get_Strip(), hit.get_Energy(), hit.get_Time());
         }

	private double _residual;             // distance to track intersect
	private int _TrkgStatus = -1 ;        //  TrkgStatusFlag factor (-1: no fit; 0: global helical fit; 1: KF fit)

         private int _AssociatedCrossIndex = -1;
         private int _AssociatedTrackIndex = -1;

         public double get_docaToTrk() {
		return _residual;
	}

	public void set_docaToTrk(double _docaToTrk) {
		this._residual = _docaToTrk;
	}

	/**
	 *
	 * @return an integer representative of the stage of the pattern recognition and subsequent KF
     * fit for that hit. -1: no fit; 0: global helical fit; 1: KF fit
	 */
	public int get_TrkgStatus() {
		return _TrkgStatus;
	}

	/**
	 * @param trkgStatus is an integer representative of the stage of the pattern recognition and subsequent KF fit
	 * for that hit. -1: no fit; 0: global helical fit; 1: KF fit
     *
	 */
	public void set_TrkgStatus(int trkgStatus) {
		_TrkgStatus = trkgStatus;
	}

	/**
	 *
	 * @param arg0 the other hit
	 * @return an int used to sort a collection of hits by layer number
	 */
	public int compareTo(FittedHit arg0) {
		if (this.get_Layer() > arg0.get_Layer()) return 1;
		else                                     return 0;
	}

	public double get_Residual() {
            return this._residual;
	}

	public void set_Residual(double trackLocalY) {
            this._residual = this.get_StripLocalSegment().origin().y()-trackLocalY;
	}

	public int get_CrossIndex() {
		return _AssociatedCrossIndex;
	}

	public void set_CrossIndex(int _AssociatedCrossIndex) {
		this._AssociatedCrossIndex = _AssociatedCrossIndex;
	}

	public int get_TrackIndex() {
		return _AssociatedTrackIndex;
	}

	public void set_TrackIndex(int _AssociatedTrackIndex) {
		this._AssociatedTrackIndex = _AssociatedTrackIndex;
	}
}
