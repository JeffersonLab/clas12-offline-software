package org.jlab.rec.cvt.hit;

/**
 * A hit that is used in a fitted track.
 *
 * @author ziegler
 *
 */
public class FittedHit extends Hit implements Comparable<Hit> {

    /**
     * @param detector SVT (0) or BMT (1)
     * @param sector (1...)
     * @param layer (1...)
     * @param strip (1...)
     * @param Edep (for gemc output without digitization)
     */
    public FittedHit(int detector, int detectortype, int sector, int layer, Strip strip) {
        super(detector, detectortype, sector, layer, strip);

    }

    private double _docaToTrk;                                                  // 3-D distance of closest approach of the helix to the wire 
    private double _stripResolutionAtDoca;                                      // position resolution at distance of closest approach of the helix to the wire 

    private int _TrkgStatus = -1;                                               //  TrkgStatusFlag factor (-1: no fit; 0: global helical fit; 1: KF fit)

    public double get_docaToTrk() {
        return _docaToTrk;
    }

    public void set_docaToTrk(double _docaToTrk) {
        this._docaToTrk = _docaToTrk;
    }

    public double get_stripResolutionAtDoca() {
        return _stripResolutionAtDoca;
    }

    public void set_stripResolutionAtDoca(double _stripResolutionAtDoca) {
        this._stripResolutionAtDoca = _stripResolutionAtDoca;
    }

    /**
     *
     * @return an integer representative of the stage of the pattern recognition
     * and subsequent KF fit for that hit. -1: no fit; 0: global helical fit; 1:
     * KF fit
     */
    public int get_TrkgStatus() {
        return _TrkgStatus;
    }

    /**
     *
     * @param trkgStatus is an integer representative of the stage of the
     * pattern recognition and subsequent KF fit for that hit. -1: no fit; 0:
     * global helical fit; 1: KF fit
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
        if (this.get_Layer() > arg0.get_Layer()) {
            return 1;
        } else {
            return 0;
        }
    }

    public double _QualityFac;	// a quality factor depending on the hit status and goodness of fit

    public double get_QualityFac() {
        return _QualityFac;
    }

    public void set_QualityFac(double QF) {
        _QualityFac = QF;
    }

    /**
     *
     * @return the hit residual = doca to track
     */
    public double get_Residual() {
        //if (get_docaToTrk() == 0) {
        //    return Double.NaN;
        //}
        return get_docaToTrk();
    }

    private int _AssociatedClusterID; // the cluster ID associated with that hit

    public int get_AssociatedClusterID() {
        return _AssociatedClusterID;
    }

    public void set_AssociatedClusterID(int _AssociatedClusterID) {
        this._AssociatedClusterID = _AssociatedClusterID;
    }

    private int AssociatedTrackID = -1; // the track ID associated with that hit

    public int get_AssociatedTrackID() {
        return AssociatedTrackID;
    }

    public void set_AssociatedTrackID(int associatedTrackID) {
        AssociatedTrackID = associatedTrackID;
    }

}
