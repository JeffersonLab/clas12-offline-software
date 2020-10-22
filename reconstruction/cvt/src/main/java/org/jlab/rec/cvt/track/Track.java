package org.jlab.rec.cvt.track;

import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.rec.cvt.cross.Cross;
import org.jlab.rec.cvt.Constants;
import org.jlab.rec.cvt.svt.Geometry;
import org.jlab.rec.cvt.trajectory.Helix;
import org.jlab.rec.cvt.trajectory.Trajectory;

/**
 * A class representing track candidates in the BST. A track has a trajectory
 * represented by an ensemble of geometrical state vectors along its path, a
 * charge and a momentum
 *
 * @author ziegler
 *
 */
public class Track extends Trajectory implements Comparable<Track> {

    private int _TrackingStatus;

    
    public void set_TrackingStatus(int ts) {
        _TrackingStatus = ts;
    }

    public int get_TrackingStatus() {
        return _TrackingStatus;
    }
    /**
     *
     * @param helix helix track parameterization
     */
    public Track(Helix helix) {
        super(helix);
        if (helix != null) {
            set_HelicalTrack(helix);
        }
    }
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 1763744434903318419L;

    private int _Q;			// track charge
    private double _Pt;		// track pt
    private double _Pz;		// track pz
    private double _P;		// track p

    private String _PID;	// track pid

    /**
     *
     * @return the charge
     */
    public int get_Q() {
        return _Q;
    }

    /**
     * Sets the charge
     *
     * @param _Q the charge
     */
    public void set_Q(int _Q) {
        this._Q = _Q;
    }

    public double get_Pt() {
        return _Pt;
    }

    public void set_Pt(double _Pt) {
        this._Pt = _Pt;
    }

    public double get_Pz() {
        return _Pz;
    }

    public void set_Pz(double _Pz) {
        this._Pz = _Pz;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    /**
     *
     * @return the total momentum value
     */
    public double get_P() {
        return _P;
    }

    /**
     * Sets the total momentum value
     *
     * @param _P the total momentum value
     */
    public void set_P(double _P) {
        this._P = _P;
    }

    /**
     * Sets the track helical track parameters P, Pt, Pz
     *
     * @param Helix the track helix
     */
    public void set_HelicalTrack(Helix Helix) {
        if (Helix != null) {
            set_Q(((int) Math.signum(Constants.getSolenoidscale()) * Helix.get_charge()));
            double calcPt = 10;
            if(Math.abs(Helix.B)>0.0001) {
                calcPt = Constants.LIGHTVEL * Helix.radius() * Helix.B;
            } else {
                calcPt = 100;
                set_Q(1);
            }
            double calcPz = 0;
            calcPz = calcPt * Helix.get_tandip();
            double calcP = Math.sqrt(calcPt * calcPt + calcPz * calcPz);
            set_Pt(calcPt);
            set_Pz(calcPz);
            set_P(calcP);
        }
    }

    /**
     * updates the crosses positions based on the track direction for a helical
     * trajectory
     *
     * @param geo the SVT geometry
     */
    public void update_Crosses(Geometry geo) {
        if (this.get_helix() != null && this.get_helix().get_curvature() != 0) {

            Helix helix = this.get_helix();
            for (int i = 0; i < this.size(); i++) {
                if (!this.get(i).get_Detector().equalsIgnoreCase("SVT")) {
                    continue;
                }
                double R = Math.sqrt(this.get(i).get_Point().x() * this.get(i).get_Point().x() + this.get(i).get_Point().y() * this.get(i).get_Point().y());
                Vector3D helixTanVecAtLayer = helix.getTrackDirectionAtRadius(R);
                this.get(i).set_CrossParamsSVT(helixTanVecAtLayer, geo);
                if (this.get(i).get_Cluster2().get_Centroid() <= 1) {
                    //recalculate z using track pars:
                    double z = helix.getPointAtRadius(R).z();
                    double x = this.get(i).get_Point().x();
                    double y = this.get(i).get_Point().y();
                    double z1 = geo.getPlaneModuleOrigin(this.get(i).get_Cluster2().get_Sector(), this.get(i).get_Cluster2().get_Layer()).z();
                    double z2 = geo.getPlaneModuleEnd(this.get(i).get_Cluster2().get_Sector(), this.get(i).get_Cluster2().get_Layer()).z();
                    if (z - z1 < z2 - z1) {
                        this.get(i).set_Point(new Point3D(x, y, z));
                    }

                }

            }

        }

    }

    public void finalUpdate_Crosses(Geometry geo) {
        if (this.get_helix() != null && this.get_helix().get_curvature() != 0) {

            Helix helix = this.get_helix();
            for (int i = 0; i < this.size(); i++) {
                if (!this.get(i).get_Detector().equalsIgnoreCase("SVT")) {
                    continue;
                }
                double R = Math.sqrt(this.get(i).get_Point().x() * this.get(i).get_Point().x() + this.get(i).get_Point().y() * this.get(i).get_Point().y());
                Vector3D helixTanVecAtLayer = helix.getTrackDirectionAtRadius(R);
                Point3D helixPosAtLayer = helix.getPointAtRadius(R);
                this.get(i).set_Point(helixPosAtLayer);
                this.get(i).set_Dir(helixTanVecAtLayer);

            }

        }

    }

    private double _circleFitChi2PerNDF;		// the chi2 for the helical track circle fit
    private double _lineFitChi2PerNDF;			// the linear fit to get the track dip angle

    private Point3D _TrackPointAtCTOFRadius;	// a point of reference at the CTOF radius [the track is extrapolated to the CTOF radius and matched to CTOF hits to get the TOF]	
    private Vector3D _TrackDirAtCTOFRadious;	// the direction of the track at the reference point described above.
    private double _pathLength;				// the pathlength from the doca of the track to the z axis to the reference point described above
    public boolean passCand;					// a flag to pass the candidate.

    /**
     *
     * @param cross the cross
     * @return a boolean to indicate if a cross belongs to the track
     */
    public boolean containsCross(Cross cross) {
        Track cand = this;
        boolean isInTrack = false;

        for (int i = 0; i < cand.size(); i++) {
            if (cand.get(i).get_Id() == cross.get_Id()) {
                isInTrack = true;
            }

        }

        return isInTrack;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Track other = (Track) obj;
        if (this.get_Id() != other.get_Id()) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 101;
        int result = super.hashCode();
        result += prime * result + this.get(0).hashCode() + this.get(this.size() - 1).hashCode();
        return result;
    }

    @Override
    public int compareTo(Track tr) {
//    	return ( tr.size() >= this.size() ) ? 1 : -1;
        return (tr.get_P() > this.get_P()) ? 1 : -1;
    }

    public Point3D get_TrackPointAtCTOFRadius() {
        return _TrackPointAtCTOFRadius;
    }

    public void set_TrackPointAtCTOFRadius(Point3D _TrackPointAtCTOFRadius) {
        this._TrackPointAtCTOFRadius = _TrackPointAtCTOFRadius;
    }

    public Vector3D get_TrackDirAtCTOFRadius() {
        return _TrackDirAtCTOFRadious;
    }

    public void set_TrackDirAtCTOFRadius(Vector3D _TrackDirAtCTOFRadious) {
        this._TrackDirAtCTOFRadious = _TrackDirAtCTOFRadious;
    }

    public double get_pathLength() {
        return _pathLength;
    }

    public void set_pathLength(double _pathLength) {
        this._pathLength = _pathLength;
    }

    public String get_PID() {
        return _PID;
    }

    public void set_PID(String _PID) {
        this._PID = _PID;
    }

    public double get_circleFitChi2PerNDF() {
        return _circleFitChi2PerNDF;
    }

    public void set_circleFitChi2PerNDF(double _circleFitChi2PerNDF) {
        this._circleFitChi2PerNDF = _circleFitChi2PerNDF;
    }

    public double get_lineFitChi2PerNDF() {
        return _lineFitChi2PerNDF;
    }

    public void set_lineFitChi2PerNDF(double _lineFitChi2PerNDF) {
        this._lineFitChi2PerNDF = _lineFitChi2PerNDF;
    }

    private int _NDF;
    private double _Chi2;

    public int getNDF() {
        return _NDF;
    }

    public void setNDF(int _NDF) {
        this._NDF = _NDF;
    }

    public double getChi2() {
        return _Chi2;
    }

    public void setChi2(double _Chi2) {
        this._Chi2 = _Chi2;
    }

}
