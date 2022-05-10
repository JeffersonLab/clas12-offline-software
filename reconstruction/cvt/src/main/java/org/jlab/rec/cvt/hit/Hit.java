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

    public DetectorType getDetector() {
        return _Detector;
    }

    public void setDetector(DetectorType _detector) {
        this._Detector = _detector;
    }

    public BMTType getType() {
        return _Type;
    }

    public void setType(BMTType type) {
        this._Type = type;
    }

    /**
     *
     * @return the sector (1...24)
     */
    public int getSector() {
        return _Sector;
    }

    /**
     * Sets the sector
     *
     * @param _Sector
     */
    public void setSector(int _Sector) {
        this._Sector = _Sector;
    }

    /**
     *
     * @return the layer (1...8)
     */
    public int getLayer() {
        return _Layer;
    }

    /**
     * Sets the layer
     *
     * @param _Layer
     */
    public void setLayer(int _Layer) {
        this._Layer = _Layer;
    }

    public Strip getStrip() {
        return _Strip;
    }

    public void setStrip(Strip _Strip) {
        this._Strip = _Strip;
    }

    /**
     *
     * @return the ID
     */
    public int getId() {
        return _Id;
    }

    /**
     * Sets the hit ID. The ID corresponds to the hit index in the EvIO column.
     *
     * @param _Id
     */
    public void setId(int _Id) {
        this._Id = _Id;
    }

    /**
     *
     * @return region (1...4)
     */
    public int getRegion() {
        return (int) (this._Layer + 1) / 2;
    }

    /**
     *
     * @return superlayer 1 or 2 in region (1...4)
     */
    public int getRegionSlayer() {
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
            int CompLyr = this.getLayer() < arg.getLayer() ? -1 : this.getLayer() == arg.getLayer() ? 0 : 1;
            int CompStr = this.getStrip().getStrip() < arg.getStrip().getStrip()? -1 : this.getStrip().getStrip() == arg.getStrip().getStrip() ? 0 : 1;
            int CompEdep = this.getStrip().getEdep() > arg.getStrip().getEdep() ? -1 : this.getStrip().getEdep() == arg.getStrip().getEdep() ? 0 : 1;
            int CompTime = this.getStrip().getTime() < arg.getStrip().getTime() ? -1 : this.getStrip().getTime() == arg.getStrip().getTime() ? 0 : 1;

            int return_val2 = ((CompEdep == 0) ? CompStr : CompEdep);
            int return_val1 = ((CompTime == 0) ? return_val2 : CompTime);
            int return_val = ((CompLyr == 0) ? return_val1 : CompLyr);

            return return_val;
        } else {
            int CompLyr = this.getLayer() < arg.getLayer() ? -1 : this.getLayer() == arg.getLayer() ? 0 : 1;         
            int return_val1 = this.getStrip().getStrip() < arg.getStrip().getStrip() ? -1 : this.getStrip().getStrip() == arg.getStrip().getStrip() ? 0 : 1;
            return ((CompLyr == 0) ? return_val1 : CompLyr);
        }
    }
    
    
    public void printInfo() {
        String s = " Hit: Detector " + this.getDetector() + "ID " + this.getId() + " Sector " + this.getSector() + " Layer " + this.getLayer() + " Strip " + this.getStrip().getStrip() 
                + " Edep " + this.getStrip().getEdep()+ " Time " + this.getStrip().getTime();
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
        if ((thisHit.getDetector()==otherHit.getDetector())
                && thisHit.getSector() == otherHit.getSector()
                && thisHit.getLayer() == otherHit.getLayer()
                && thisHit.getStrip().getStrip() == otherHit.getStrip().getStrip()
                && thisHit.getStrip().getEdep() == otherHit.getStrip().getEdep()) {
            cmp = true;
        }
        return cmp;
    }

    public double getdocaToTrk() {
        return _docaToTrk;
    }

    public void setdocaToTrk(double _docaToTrk) {
        this._docaToTrk = _docaToTrk;
    }

    public void setdocaToTrk(Point3D traj) {
        this.setdocaToTrk(this.residual(traj));
    }
    
    public double residual(Point3D traj) {
        double value = 0;
        if(this.getDetector()==DetectorType.BST) {
            Line3D dist = this.getStrip().getLine().distance(traj);
            double side = -Math.signum(this.getStrip().getLine().direction().cross(dist.direction()).dot(this.getStrip().getNormal()));
            value = dist.length()*side;
        }
        else {
            Point3D local = new Point3D(traj);
            this.getStrip().toLocal().apply(local);
            if(this.getType()==BMTType.C)                
                value = local.z()-this.getStrip().getZ();
            else {
                value = Math.atan2(local.y(),local.x())-this.getStrip().getPhi();
                if(Math.abs(value)>Math.PI) value-=Math.signum(value)*2*Math.PI;
                value = value*this.getStrip().getTile().baseArc().radius();
            }
        }     
        return value;
    }   
    
    
    public double getstripResolutionAtDoca() {
        return _stripResolutionAtDoca;
    }

    public void setstripResolutionAtDoca(double _stripResolutionAtDoca) {
        this._stripResolutionAtDoca = _stripResolutionAtDoca;
    }

    /**
     *
     * @return an integer representative of the stage of the pattern recognition
     * and subsequent KF fit for that hit. -1: no fit; 0: global helical fit; 1:
     * KF fit
     */
    public int getTrkgStatus() {
        return _TrkgStatus;
    }

    /**
     *
     * @param trkgStatus is an integer representative of the stage of the
     * pattern recognition and subsequent KF fit for that hit. -1: no fit; 0:
     * global helical fit; 1: KF fit
     */
    public void setTrkgStatus(int trkgStatus) {
        _TrkgStatus = trkgStatus;
    }

    public double getQualityFac() {
        return _QualityFac;
    }

    public void setQualityFac(double QF) {
        _QualityFac = QF;
    }

    /**
     *
     * @return the hit residual = doca to track
     */
    public double getResidual() {
        return getdocaToTrk();
    }


    public int getAssociatedClusterID() {
        return _AssociatedClusterID;
    }

    public void setAssociatedClusterID(int _AssociatedClusterID) {
        this._AssociatedClusterID = _AssociatedClusterID;
    }


    public int getAssociatedTrackID() {
        return AssociatedTrackID;
    }
    

    public void setAssociatedTrackID(int associatedTrackID) {
        AssociatedTrackID = associatedTrackID;
    }

    public String toString() {
        String str = String.format("Hit id=%d, layer=%d, sector=%d, strip=%d, energy=%.3f, time=%.3f, residual=%.3f, clusterID=%d, trackID=%d", 
                     this.getId(), this.getLayer(), this.getSector(), 
                     this.getStrip().getStrip(), 
                     this.getStrip().getEdep(), 
                     this.getStrip().getTime(),
                     this.getResidual(), this.getAssociatedClusterID(), this.getAssociatedTrackID());
        return str;
    }
}
