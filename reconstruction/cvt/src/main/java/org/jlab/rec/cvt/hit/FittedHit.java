package org.jlab.rec.cvt.hit;

import org.jlab.detector.base.DetectorType;
import org.jlab.geom.prim.Point3D;
import org.jlab.rec.cvt.bmt.BMTType;

/**
 * A hit that is used in a fitted track.
 *
 * @author ziegler
 *
 */
public class FittedHit extends Hit implements Comparable<Hit> {

    /**
     * @param detector SVT (0) or BMT (1)
     * @param type
     * @param sector (1...)
     * @param layer (1...)
     * @param strip (1...)
     */
    public FittedHit(DetectorType detector, BMTType type, int sector, int layer, Strip strip) {
        super(detector, type, sector, layer, strip);
    }

    private double _docaToTrk;                                                  // 3-D distance of closest approach of the helix to the wire 
    private double _stripResolutionAtDoca;    
    private int _TrkgStatus = -1;                                               //  TrkgStatusFlag factor (-1: no fit; 0: global helical fit; 1: KF fit)

    public double get_docaToTrk() {
        return _docaToTrk;
    }

    public void set_docaToTrk(double _docaToTrk) {
        this._docaToTrk = _docaToTrk;
    }

    public void set_docaToTrk(Point3D traj) {
        this.set_docaToTrk(this.residual(traj));
    }
    
    public double residual(Point3D traj) {
        double value = 0;
        if(this.get_Detector()==DetectorType.BST)
            ;
        else {
            Point3D local = new Point3D(traj);
            this.get_Strip().toLocal().apply(local);
            if(this.get_Type()==BMTType.C)                
                value = local.z()-this.get_Strip().get_Z();
            else
                ;
        }      
        return value;
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

//    /**
//     *
//     * @param arg0 the other hit
//     * @return an int used to sort a collection of hits by layer number
//     */
//    public int compareTo(FittedHit arg) {
//
//        //sort by layer, then time, then edep
//        int CompLyr = this.get_Layer() < arg.get_Layer() ? -1 : this.get_Layer() == arg.get_Layer() ? 0 : 1;
//        int CompEdep = this.get_Strip().get_Edep() > arg.get_Strip().get_Edep() ? -1 : this.get_Strip().get_Edep() == arg.get_Strip().get_Edep() ? 0 : 1;
//        int CompTime = this.get_Strip().get_Time() < arg.get_Strip().get_Time() ? -1 : this.get_Strip().get_Time() == arg.get_Strip().get_Time() ? 0 : 1;
//
//        int return_val1 = ((CompTime == 0) ? CompEdep : CompTime);
//        int return_val = ((CompLyr == 0) ? return_val1 : CompLyr);
//        
//        return return_val;
//    }
    
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
