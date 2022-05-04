package org.jlab.rec.cvt.track;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.jlab.clas.pdg.PDGDatabase;
import org.jlab.clas.swimtools.Swim;
import org.jlab.clas.tracking.kalmanfilter.AKFitter.HitOnTrack;
import org.jlab.clas.tracking.kalmanfilter.Surface;
import org.jlab.clas.tracking.kalmanfilter.helical.KFitter;
import org.jlab.detector.base.DetectorType;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.rec.cvt.cross.Cross;
import org.jlab.rec.cvt.Constants;
import org.jlab.rec.cvt.Geometry;
import org.jlab.rec.cvt.bmt.BMTGeometry;
import org.jlab.rec.cvt.bmt.BMTType;
import org.jlab.rec.cvt.cluster.Cluster;
import org.jlab.rec.cvt.measurement.MLayer;
import org.jlab.rec.cvt.trajectory.Helix;
import org.jlab.rec.cvt.trajectory.StateVec;
import org.jlab.rec.cvt.trajectory.Trajectory;

/**
 * A class representing track candidates in the CVT. A track has a trajectory
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
    private int    _PID;	// track pid
    
    private Seed    _seed;

    private Point3D  _trackPosAtCTOF;	        // a point of reference at the CTOF radius [the track is extrapolated to the CTOF radius and matched to CTOF hits to get the TOF]	
    private Vector3D _trackDirAtCTOF;	        // the direction of the track at the reference point described above.
    private double   _pathToCTOF;       	// the pathlength from the doca of the track to the z axis to the reference point described above
    private int    _NDF;
    private double _Chi2;
    private int kfIterations;
    private double[][] trackCovMat;
    private Map<Integer, HitOnTrack> trajs = null; // map of trajectories indexed by layer, to be filled based on the KF results
    private Helix secondaryHelix;                  // for track with no beamSpot information


    public Track(Helix helix) {
        super(helix);
        if (helix != null) {
            this.setPXYZ();
        }
    }
    
    public Track(Seed seed) {
        super(seed.getHelix());
        this.setSeed(seed);
        this.setPXYZ();
        this.setNDF(seed.getNDF());
        this.setChi2(seed.getChi2());
        this.addAll(seed.getCrosses());       
    }

    public Track(Seed seed, KFitter kf) {
        super(new Helix(kf.getHelix(), kf.getStateVec().covMat));
        this.setPXYZ();
        this.setSecondaryHelix(new Helix(kf.getHelix(1), kf.getStateVec(1).covMat));
        this.kfIterations = kf.numIter;
        this.setNDF(kf.NDF);
        this.setChi2(kf.chi2);
        this.setSeed(seed);
        this.addAll(seed.getCrosses());
        this.setKFTrajectories(kf.trajPoints);
    }
    
        
    public Track(Seed seed, KFitter kf, int pid) {
        this(seed, kf);
        this.setPID(pid);
    }

    /**
     *
     * @return the charge
     */
    public int getQ() {
        return _Q;
    }

    /**
     * Sets the charge
     *
     * @param _Q the charge
     */
    public void setQ(int _Q) {
        this._Q = _Q;
    }

    public double getPt() {
        return _Pt;
    }

    public void setPt(double _Pt) {
        this._Pt = _Pt;
    }

    public double getPz() {
        return _Pz;
    }

    public void setPz(double _Pz) {
        this._Pz = _Pz;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    /**
     *
     * @return the total momentum value
     */
    public double getP() {
        return _P;
    }

    /**
     * Sets the total momentum value
     *
     * @param _P the total momentum value
     */
    public void setP(double _P) {
        this._P = _P;
    }

    public Helix getSecondaryHelix() {
        return secondaryHelix;
    }

    public void setSecondaryHelix(Helix secondaryHelix) {
        this.secondaryHelix = secondaryHelix;
    }

    public Seed getSeed() {
        return _seed;
    }

    public final void setSeed(Seed seed) {
        this._seed = seed;
    }

    /**
     * Sets the track helical track parameters P, Pt, Pz
     *
     */
    public final void setPXYZ() {
        Helix helix = this.getHelix();
        if (helix != null) {
            setQ(((int) Math.signum(Constants.getSolenoidScale()) * helix.getCharge()));
            double calcPt = 10;
            if(Math.abs(helix.B)>0.0001) {
                calcPt = Constants.LIGHTVEL * helix.radius() * helix.B;
            } else {
                calcPt = 100;
                setQ(1);
            }
            double calcPz = 0;
            calcPz = calcPt * helix.getTanDip();
            double calcP = Math.sqrt(calcPt * calcPt + calcPz * calcPz);
            setPt(calcPt);
            setPz(calcPz);
            setP(calcP);
        }
    }

    /**
     * Updates the crosses positions based on trajectories or helix
     * @param trackId
     */
    public void update_Crosses(int trackId) {
        for (int i = 0; i < this.size(); i++) {
            Cross cross = this.get(i);
            cross.setAssociatedTrackID(trackId);
            Point3D  trackPos = null;
            Vector3D trackDir = null;
            if(this.getKFTrajectories()!=null && Math.abs(this.getHelix().B)>0.0001) {
                int layer = cross.getCluster1().getLayer();
                int index = MLayer.getType(cross.getDetector(), layer).getIndex();
                HitOnTrack traj = this.getKFTrajectories().get(index);
                if(traj==null) return; //RDV check why
                trackPos = new Point3D(traj.x, traj.y, traj.z);
                trackDir = new Vector3D(traj.px, traj.py, traj.pz).asUnit();
            }
            else if (this.getHelix() != null && this.getHelix().getCurvature() != 0) {
                double R = Math.sqrt(cross.getPoint().x() * cross.getPoint().x() + cross.getPoint().y() * cross.getPoint().y());
                trackPos = this.getHelix().getPointAtRadius(R);
                trackDir = this.getHelix().getTrackDirectionAtRadius(R);
            }
            cross.update(trackPos, trackDir);
        }
    }    
    

    public void update_Clusters(int trackId) {        
        if(this.getKFTrajectories()!=null) {
            for (int i = 0; i < this.getSeed().getClusters().size(); i++) {
                Cluster cluster = this.getSeed().getClusters().get(i);
                
                int layer = cluster.getLayer();
                int index = MLayer.getType(cluster.getDetector(), layer).getIndex();
                
                if(this.getKFTrajectories().get(index)!=null) // RDV check why it is necessary
                    cluster.update(trackId, this.getKFTrajectories().get(index));
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
            if (cand.get(i).getId() == cross.getId()) {
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
        if (this.getId() != other.getId()) {
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
        return (tr.getP() > this.getP()) ? 1 : -1;
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
        else if(this.getPt() < Constants.PTCUT) 
            return false;
        else if(Math.abs(this.getHelix().getZ0()) > Constants.ZRANGE) 
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
            if(c.getType()==BMTType.C) continue; //skim BMTC
            if(o.contains(c)) nc++;
        }
        if(nc >1) return true;
        else      return false;
    }

    public Point3D getTrackPosAtCTOF() {
        return _trackPosAtCTOF;
    }

    public void setTrackPosAtCTOF(Point3D _TrackPointAtCTOFRadius) {
        this._trackPosAtCTOF = _TrackPointAtCTOFRadius;
    }

    public Vector3D getTrackDirAtCTOF() {
        return _trackDirAtCTOF;
    }

    public void setTrackDirAtCTOF(Vector3D _TrackDirAtCTOFRadious) {
        this._trackDirAtCTOF = _TrackDirAtCTOFRadious;
    }

    public double getPathToCTOF() {
        return _pathToCTOF;
    }

    public void setPathToCTOF(double _pathLength) {
        this._pathToCTOF = _pathLength;
    }

    public int getPID() {
        return _PID;
    }

    public void setPID(int _PID) {
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

    public int getKFIterations() {
        return kfIterations;
    }

    public void setKFIterations(int iter) {
        kfIterations = iter;
    }
    
    public Map<Integer, HitOnTrack> getKFTrajectories() {
        return trajs;
    }
    
    public final void setKFTrajectories(Map<Integer, HitOnTrack> trajectory) {
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
            if (this.get(j).getDetector() == DetectorType.BST) {
                nSVT++;
            }
            if (this.get(j).getDetector() == DetectorType.BMT
                    && this.get(j).getType() == BMTType.Z) {
                nBMTZ++;
            }
            if (this.get(j).getDetector() == DetectorType.BMT
                    && this.get(j).getType() == BMTType.C) {
                nBMTC++;
            }
        }
        return 1000*this.kfIterations+nSVT*100+nBMTZ*10+nBMTC;
    }
    
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
    
    public void findTrajectory(Swim swimmer, List<Surface> outer) {
        
        List<StateVec> stateVecs = new ArrayList<>();
        
        //get KF trajectories first
        double path = 0;
        double mass = PDGDatabase.getParticleMass(this.getPID());
        double beta = this.getP()/Math.sqrt(this.getP()*this.getP()+mass*mass);
        
        HitOnTrack traj = null;       
        for(int index=1; index<trajs.size(); index++) {
            traj = this.trajs.get(index);            
            
            Vector3D mom = new Vector3D(traj.px, traj.py, traj.pz);
            Vector3D dir = mom.asUnit();
            Point3D  pos = new Point3D(traj.x, traj.y, traj.z);
            path += traj.path * beta/(mom.mag()/Math.sqrt(mom.mag2()+mass*mass));

            StateVec stVec = new StateVec(this.getId(), traj, MLayer.getDetectorType(index));
            stVec.setSurfaceLayer(MLayer.getType(index).getCVTLayer());
            stVec.setPath(path);
            
            if(MLayer.getDetectorType(index) == DetectorType.BST) {
                Vector3D localDir = Geometry.getInstance().getSVT().getLocalTrack(traj.layer, traj.sector, dir);
                stVec.setTrkPhiAtSurface(localDir.phi());
                stVec.setTrkThetaAtSurface(localDir.theta());
                stVec.setTrkToModuleAngle(Geometry.getInstance().getSVT().getLocalAngle(traj.layer, traj.sector, dir));
                stVec.setCalcCentroidStrip(Geometry.getInstance().getSVT().calcNearestStrip(traj.x, traj.y, traj.z, traj.layer, traj.sector));
            }
            else if(MLayer.getDetectorType(index) == DetectorType.BMT) {
                Vector3D localDir = Geometry.getInstance().getBMT().getLocalTrack(traj.layer, traj.sector, pos, dir);
                stVec.setTrkPhiAtSurface(localDir.phi());
                stVec.setTrkThetaAtSurface(localDir.theta());
                stVec.setTrkToModuleAngle(Geometry.getInstance().getBMT().getLocalAngle(traj.layer, traj.sector, pos, dir));
                stVec.setCalcCentroidStrip(Geometry.getInstance().getBMT().getStrip(traj.layer, traj.sector, pos));
            }
            if(stVec.getSurfaceDetector()>0) {
                stVec.setSurfaceDetector(DetectorType.CVT.getDetectorId());
                stateVecs.add(stVec);
            }
        }
        // add outer detectors
        if(traj!=null) {
            
            Vector3D mom = new Vector3D(traj.px, traj.py, traj.pz);
            Point3D  pos = new Point3D(traj.x, traj.y, traj.z);
            double maxPathLength = 1.5;   
            
            for(Surface surface : outer) {
            
                swimmer.SetSwimParameters(pos.x()/10, pos.y()/10, pos.z()/10, 
                                      Math.toDegrees(mom.phi()), Math.toDegrees(mom.theta()), 
                                      mom.mag(), this.getQ(), maxPathLength);                
                double   r = surface.cylinder.baseArc().radius(); 
                double[] inters = swimmer.SwimGenCylinder(new Point3D(0, 0, 0), new Point3D(0, 0, 1), r/10, Constants.SWIMACCURACYCD/10);                
                if(inters==null) break;
                pos.set(inters[0]*10, inters[1]*10, inters[2]*10);
                mom.setXYZ(inters[3],inters[4],inters[5]);
                path += inters[6]*10 * beta/(mom.mag()/Math.sqrt(mom.mag2()+mass*mass));
                
                StateVec stVec = new StateVec(this.getId(), pos, mom, surface, path);
                stateVecs.add(stVec);                
                
                if(surface.getIndex() == DetectorType.CTOF.getDetectorId()) {
                    this.setTrackPosAtCTOF(new Point3D(pos));
                    this.setTrackDirAtCTOF(mom.asUnit());
                    this.setPathToCTOF(path);
                }
                
                surface.getEloss(mom, mass, 1);
                if(mom.mag()==0) break;
            }
        }
        this.setTrajectory((ArrayList<StateVec>) stateVecs);
    }
    
    public static void removeOverlappingTracks(List<Track> tracks) {
            if(tracks==null)
                return;
            
        List<Track> selectedTracks =  new ArrayList<>();
        for (int i = 0; i < tracks.size(); i++) {
            boolean overlap = false;
            Track t1 = tracks.get(i);
            for(int j=0; j<tracks.size(); j++ ) {
                Track t2 = tracks.get(j);
                if(i!=j && t1.overlapWith(t2) && !t1.betterThan(t2)) {
                    overlap=true;
                }
            }
            if(!overlap) selectedTracks.add(t1);
        }

        tracks.removeAll(tracks);
        tracks.addAll(selectedTracks);
    }
    
    public static void checkForOverlaps(List<Track> tracks, String msg) {
        for (int i = 0; i < tracks.size(); i++) {
            Track t1 = tracks.get(i);
            for(int j=0; j<tracks.size(); j++ ) {
                Track t2 = tracks.get(j);
                if(i!=j && t1.overlapWith(t2)) {
                    System.out.println(msg + " " + "overlap");
                }
            }
        }        
    }


    @Override
    public String toString() {
        String str = String.format("Track id=%d, q=%d, p=%.3f GeV pt=%.3f GeV, d0=%.3f deg, phi=%.3f deg, z0=%.3f deg, tandip=%.3f deg, NDF=%d, chi2=%.3f, seed method=%d\n", 
                     this.getId(), this.getQ(), this.getP(), this.getPt(), this.getHelix().getDCA(),
                     Math.toDegrees(this.getHelix().getPhiAtDCA()), this.getHelix().getZ0(), this.getHelix().getTanDip(),
                     this.getNDF(), this.getChi2(), this.getSeed().getStatus());
        for(Cross c: this) str = str + c.toString() + "\n";
        for(Cluster c: this.getSeed().getClusters()) str = str + c.toString() + "\n";
        return str;
    }

    

}
