package org.jlab.rec.cvt.track;

import java.util.Map;
import org.jlab.clas.tracking.kalmanfilter.AKFitter.HitOnTrack;
import org.jlab.detector.base.DetectorType;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.rec.cvt.cross.Cross;
import org.jlab.rec.cvt.Constants;
import org.jlab.rec.cvt.bmt.BMTGeometry;
import org.jlab.rec.cvt.bmt.BMTType;
import org.jlab.rec.cvt.cluster.Cluster;
import org.jlab.rec.cvt.svt.SVTGeometry;
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

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 1763744434903318419L;

    private int _Q;	        // track charge
    private double _Pt;		// track pt
    private double _Pz;		// track pz
    private double _P;		// track p
    private String _PID;	// track pid
    
    private Seed    _seed;

    private Point3D  _trackPosAtCTOF;	        // a point of reference at the CTOF radius [the track is extrapolated to the CTOF radius and matched to CTOF hits to get the TOF]	
    private Vector3D _trackDirAtCTOF;	        // the direction of the track at the reference point described above.
    private double   _pathToCTOF;       	// the pathlength from the doca of the track to the z axis to the reference point described above
    private int    _NDF;
    private double _Chi2;
    private Map<Integer, HitOnTrack> trajs = null; // map of trajectories indexed by layer, to be filled based on the KF results

    

    public Track(Helix helix) {
        super(helix);
        if (helix != null) {
            this.setPXYZ();
        }
    }
    
    public Track(Seed seed) {
        super(seed.get_Helix());
        this.set_Seed(seed);
        this.setPXYZ();
        this.setNDF(seed.getNDF());
        this.setChi2(seed.getChi2());
        this.addAll(seed.get_Crosses());       
    }

    public Track(Seed seed, org.jlab.clas.tracking.kalmanfilter.helical.KFitter kf) {
        super(new Helix(kf.KFHelix.getD0(), kf.KFHelix.getPhi0(), kf.KFHelix.getOmega(), 
                        kf.KFHelix.getZ0(), kf.KFHelix.getTanL()));
        this.get_helix().B = kf.KFHelix.getB();
        double c = Constants.LIGHTVEL;
        //convert from kf representation to helix repr
        double alpha = 1. / (c * Math.abs(kf.KFHelix.getB()));
        double[][] kfCov = kf.finalCovMat;
        for(int i = 0; i<5; i++) {
            for(int j = 0; j<5; j++) {
                if(i==2)
                    kfCov[i][j]/=alpha;
                if(j==2)
                    kfCov[i][j]/=alpha;
                
            }
        }
        
        //kfCov[0][0]/=100;
        //kfCov[3][3]/=100;
        kfCov[1][1]*=10;
        kfCov[2][2]*=10;
        kfCov[4][4]*=10;
        
        this.get_helix().set_covmatrix(kfCov);
        //this.get_helix().set_covmatrix(seed.get_Helix().get_covmatrix());
        this.setPXYZ();
        this.setNDF(kf.NDF);
        this.setChi2(kf.chi2);
        this.set_Seed(seed);
        this.addAll(seed.get_Crosses());
        this.setTrajectories(kf.TrjPoints);
    }
    
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

    public Seed get_Seed() {
        return _seed;
    }

    public final void set_Seed(Seed seed) {
        this._seed = seed;
    }

    /**
     * Sets the track helical track parameters P, Pt, Pz
     *
     */
    public final void setPXYZ() {
        Helix helix = this.get_helix();
        if (helix != null) {
            set_Q(((int) Math.signum(Constants.getSolenoidScale()) * helix.get_charge()));
            double calcPt = 10;
            if(Math.abs(helix.B)>0.0001) {
                calcPt = Constants.LIGHTVEL * helix.radius() * helix.B;
            } else {
                calcPt = 100;
                set_Q(1);
            }
            double calcPz = 0;
            calcPz = calcPt * helix.get_tandip();
            double calcP = Math.sqrt(calcPt * calcPt + calcPz * calcPz);
            set_Pt(calcPt);
            set_Pz(calcPz);
            set_P(calcP);
        }
    }

    /**
     * Updates the crosses positions based on trajectories or helix
     * @param sgeo
     * @param bgeo
     */
    public void update_Crosses(int trackId, SVTGeometry sgeo, BMTGeometry bgeo) {
        for (int i = 0; i < this.size(); i++) {
            Cross cross = this.get(i);
            cross.set_AssociatedTrackID(trackId);
            Point3D  trackPos = null;
            Vector3D trackDir = null;
            if(this.getTrajectories()!=null && Math.abs(this.get_helix().B)>0.0001) {
                int layer = cross.get_Cluster1().get_Layer();
                if(cross.get_Detector()==DetectorType.BMT) layer += SVTGeometry.NLAYERS;
                HitOnTrack traj = this.getTrajectories().get(layer);
                trackPos = new Point3D(traj.x, traj.y, traj.z);
                trackDir = new Vector3D(traj.px, traj.py, traj.pz).asUnit();
            }
            else if (this.get_helix() != null && this.get_helix().get_curvature() != 0) {
                double R = Math.sqrt(cross.get_Point().x() * cross.get_Point().x() + cross.get_Point().y() * cross.get_Point().y());
                trackPos = this.get_helix().getPointAtRadius(R);
                trackDir = this.get_helix().getTrackDirectionAtRadius(R);
//                System.out.println("Traj  " + cross.get_Cluster1().get_Layer() + " " + helixPos.toString());
//                System.out.println("Cross " + cross.get_Detector().getName() + " " + cross.get_Point().toString());
            }
            cross.update(trackPos, trackDir, sgeo);
        }
    }    
    

    public void update_Clusters(int trackId, SVTGeometry sgeo) {        
        if(this.getTrajectories()!=null) {
            for (int i = 0; i < this.get_Seed().get_Clusters().size(); i++) {
                Cluster cluster = this.get_Seed().get_Clusters().get(i);
                
                int layer = cluster.get_Layer();
                if(cluster.get_Detector()==DetectorType.BMT) layer += SVTGeometry.NLAYERS;
                
                if(this.getTrajectories().get(layer)!=null) // RDV check why it is necessary
                    cluster.update(trackId, this.getTrajectories().get(layer), sgeo);
            }
        }
    }
    
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
    
    /**
     * Check if track passes basic quality cuts
     * @return 
     */    
    public boolean isGood() {
        if(Double.isNaN(this.getChi2())) 
            return false;
        else if(this.getChi2() > Constants.CHI2CUT * (this.getNDF() + 5)) 
            return false;
        else if(this.getNDF() < Constants.NDFCUT) 
            return false;
        else if(this.get_Pt() < Constants.PTCUT) 
            return false;
        else if(Math.abs(this.get_helix().get_Z0()) > Constants.ZRANGE) 
            return false;
        else 
            return true;
    }
    
    /**
     * Compare this track quality with the given track
     * based on NDF and Chi2
     * @param o the other track
     * @return true if this track quality is better than the given track
     */    
    public boolean betterThan(Track o) {
        if(this.getNDF()>o.getNDF()) 
            return true;
        else if(this.getNDF()==o.getNDF()) {
            return this.getChi2()/this.getNDF() < o.getChi2()/o.getNDF();
        }
        else
            return false;
    }
    
    /**
     * Check track overlaps with the given track
     * an overlaps is detected if the tracks share at least two crosses
     * @param o the other track
     * @return true if this track overlaps with the given track, false otherwise
     */
    public boolean overlapWith(Track o) {
        int nc = 0;
        for(Cross c : this) {
            if(c.get_Type()==BMTType.C) continue; //skim BMTC
            if(o.contains(c)) nc++;
        }
        if(nc >1) return true;
        else      return false;
    }

    public Point3D get_TrackPosAtCTOF() {
        return _trackPosAtCTOF;
    }

    public void se_TrackPosAtCTOF(Point3D _TrackPointAtCTOFRadius) {
        this._trackPosAtCTOF = _TrackPointAtCTOFRadius;
    }

    public Vector3D get_TrackDirAtCTOF() {
        return _trackDirAtCTOF;
    }

    public void set_TrackDirAtCTOF(Vector3D _TrackDirAtCTOFRadious) {
        this._trackDirAtCTOF = _TrackDirAtCTOFRadious;
    }

    public double get_PathToCTOF() {
        return _pathToCTOF;
    }

    public void set_PathToCTOF(double _pathLength) {
        this._pathToCTOF = _pathLength;
    }

    public String get_PID() {
        return _PID;
    }

    public void set_PID(String _PID) {
        this._PID = _PID;
    }
    
    public int getNDF() {
        return _NDF;
    }

    public final void setNDF(int _NDF) {
        this._NDF = _NDF;
    }

    public double getChi2() {
        return _Chi2;
    }

    public final void setChi2(double _Chi2) {
        this._Chi2 = _Chi2;
    }
    
    public Map<Integer, HitOnTrack> getTrajectories() {
        return trajs;
    }
    
    public final void setTrajectories(Map<Integer, HitOnTrack> trajectory) {
        this.trajs = trajectory;
    }
    
    public int getStatus() {
        //for status word:
        int nSVT  = 0;
        int nBMTZ = 0;
        int nBMTC = 0;
        // fills the list of cross ids for crosses belonging to that reconstructed track
        for (int j = 0; j < this.size(); j++) {
            // counter to get status word    
            if (this.get(j).get_Detector() == DetectorType.BST) {
                nSVT++;
            }
            if (this.get(j).get_Detector() == DetectorType.BMT
                    && this.get(j).get_Type() == BMTType.Z) {
                nBMTZ++;
            }
            if (this.get(j).get_Detector() == DetectorType.BMT
                    && this.get(j).get_Type() == BMTType.C) {
                nBMTC++;
            }
        }
        return 1000+nSVT*100+nBMTZ*10+nBMTC;
    }
    
    private double[][] trackCovMat;
    /**
     * @return the trackCovMat
     */
    public double[][] getTrackCovMat() {
        return trackCovMat;
    }

    /**
     * @param trackCovMat the trackCovMat to set
     */
    public void setTrackCovMat(double[][] trackCovMat) {
        this.trackCovMat = trackCovMat;
    }
    
    public String toString() {
        String str = String.format("Track id=%d, q=%d, p=%.3f GeV pt=%.3f GeV, phi=%.3f deg, NDF=%d, chi2=%.3f\n", 
                     this.get_Id(), this.get_Q(), this.get_P(), this.get_Pt(), Math.toDegrees(this.get_helix().get_phi_at_dca()),
                     this.getNDF(), this.getChi2());
        for(Cross c: this) str = str + c.toString() + "\n";
        return str;
    }

    

}
