package org.jlab.rec.cvt.hit;

import org.jlab.detector.base.DetectorType;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.rec.cvt.bmt.BMTType;

/**
 * A hit characterized by layer, sector, wire number, and Edep. The ADC to time
 * conversion has been done.
 *
 * @author ziegler
 *
 */
public class Hit implements Comparable<Hit> {
    // class implements Comparable interface to allow for sorting a collection of hits by wire number values

    private int _Id;		       //    Hit Id

    private DetectorType _Detector;    //   the detector SVT or BMT
    private BMTType      _Type;        //   for the BMT, either C or Z

    private int _Sector;      	       //   sector[1...24] for SVT, [1..3] for BMT
    private int _Layer;    	       //   layer [1,...]
    private Strip _Strip;    	       //   Strip object
    
    private double _docaToTrk;              // 3-D distance of closest approach of the helix to the wire 
    private double _stripResolutionAtDoca;    
    private int _TrkgStatus = -1;           // TrkgStatusFlag factor (-1: no fit; 0: global helical fit; 1: KF fit)
    public double _QualityFac;	            // a quality factor depending on the hit status and goodness of fit
    private int _AssociatedClusterID = -1;  // the cluster ID associated with that hit
    private int AssociatedTrackID = -1;     // the track ID associated with that hit

    public boolean newClustering = false;

    // constructor
    public Hit(DetectorType detector, BMTType type, int sector, int layer, Strip strip) {
        this._Detector = detector;     // 0 = SVT, 1 = BMT
        this._Type     = type;               // set according to BMTType
        this._Sector   = sector;
        this._Layer    = layer;
        this._Strip    = strip;

    }

    public DetectorType get_Detector() {
        return _Detector;
    }

    public void set_Detector(DetectorType _detector) {
        this._Detector = _detector;
    }

    public BMTType get_Type() {
        return _Type;
    }

    public void set_Type(BMTType type) {
        this._Type = type;
    }

    /**
     *
     * @return the sector (1...24)
     */
    public int get_Sector() {
        return _Sector;
    }

    /**
     * Sets the sector
     *
     * @param _Sector
     */
    public void set_Sector(int _Sector) {
        this._Sector = _Sector;
    }

    /**
     *
     * @return the layer (1...8)
     */
    public int get_Layer() {
        return _Layer;
    }

    /**
     * Sets the layer
     *
     * @param _Layer
     */
    public void set_Layer(int _Layer) {
        this._Layer = _Layer;
    }

    public Strip get_Strip() {
        return _Strip;
    }

    public void set_Strip(Strip _Strip) {
        this._Strip = _Strip;
    }

    /**
     *
     * @return the ID
     */
    public int get_Id() {
        return _Id;
    }

    /**
     * Sets the hit ID. The ID corresponds to the hit index in the EvIO column.
     *
     * @param _Id
     */
    public void set_Id(int _Id) {
        this._Id = _Id;
    }

    /**
     *
     * @return region (1...4)
     */
    public int get_Region() {
        return (int) (this._Layer + 1) / 2;
    }

    /**
     *
     * @return superlayer 1 or 2 in region (1...4)
     */
    public int get_RegionSlayer() {
        return (this._Layer + 1) % 2 + 1;
    }

    /**
     *
     * @param arg
     * @return an int used to sort a collection of hits by wire number. Sorting
     * by wire is used in clustering.
     */
    @Override
    public int compareTo(Hit arg) {
        if(this.newClustering) {
            //sort by layer, then time, then edep
            int CompLyr = this.get_Layer() < arg.get_Layer() ? -1 : this.get_Layer() == arg.get_Layer() ? 0 : 1;
            int CompEdep = this.get_Strip().get_Edep() > arg.get_Strip().get_Edep() ? -1 : this.get_Strip().get_Edep() == arg.get_Strip().get_Edep() ? 0 : 1;
            int CompTime = this.get_Strip().get_Time() < arg.get_Strip().get_Time() ? -1 : this.get_Strip().get_Time() == arg.get_Strip().get_Time() ? 0 : 1;

            int return_val1 = ((CompTime == 0) ? CompEdep : CompTime);
            int return_val = ((CompLyr == 0) ? return_val1 : CompLyr);

            return return_val;
        } else {
            int CompLyr = this.get_Layer() < arg.get_Layer() ? -1 : this.get_Layer() == arg.get_Layer() ? 0 : 1;         
            int return_val1 = this.get_Strip().get_Strip() < arg.get_Strip().get_Strip() ? -1 : this.get_Strip().get_Strip() == arg.get_Strip().get_Strip() ? 0 : 1;
            return ((CompLyr == 0) ? return_val1 : CompLyr);
        }
    }
    
    
    public void printInfo() {
        String s = " Hit: Detector " + this.get_Detector() + "ID " + this.get_Id() + " Sector " + this.get_Sector() + " Layer " + this.get_Layer() + " Strip " + this.get_Strip().get_Strip() 
                + " Edep " + this.get_Strip().get_Edep()+ " Time " + this.get_Strip().get_Time();
        System.out.println(s);
    }

    /**
     *
     * @param otherHit
     * @return a boolean comparing 2 hits based on basic descriptors; returns
     * true if the hits are the same
     */
    public boolean isSameAs(Hit otherHit) {
        Hit thisHit = (Hit) this;
        boolean cmp = false;
        if ((thisHit.get_Detector()==otherHit.get_Detector())
                && thisHit.get_Sector() == otherHit.get_Sector()
                && thisHit.get_Layer() == otherHit.get_Layer()
                && thisHit.get_Strip().get_Strip() == otherHit.get_Strip().get_Strip()
                && thisHit.get_Strip().get_Edep() == otherHit.get_Strip().get_Edep()) {
            cmp = true;
        }
        return cmp;
    }

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
        if(this.get_Detector()==DetectorType.BST) {
            Line3D dist = this.get_Strip().get_Line().distance(traj);
            double side = -Math.signum(this.get_Strip().get_Line().direction().cross(dist.direction()).dot(this.get_Strip().get_Normal()));
            value = dist.length()*side;
        }
        else {
            Point3D local = new Point3D(traj);
            this.get_Strip().toLocal().apply(local);
            if(this.get_Type()==BMTType.C)                
                value = local.z()-this.get_Strip().get_Z();
            else {
                value = Math.atan2(local.y(),local.x())-this.get_Strip().get_Phi();
                if(Math.abs(value)>Math.PI) value-=Math.signum(value)*2*Math.PI;
                value = value*this.get_Strip().get_Tile().baseArc().radius();
            }
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
        return get_docaToTrk();
    }


    public int get_AssociatedClusterID() {
        return _AssociatedClusterID;
    }

    public void set_AssociatedClusterID(int _AssociatedClusterID) {
        this._AssociatedClusterID = _AssociatedClusterID;
    }


    public int get_AssociatedTrackID() {
        return AssociatedTrackID;
    }

    public void set_AssociatedTrackID(int associatedTrackID) {
        AssociatedTrackID = associatedTrackID;
    }

}
