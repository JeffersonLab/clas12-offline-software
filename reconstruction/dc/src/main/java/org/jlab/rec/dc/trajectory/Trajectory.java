package org.jlab.rec.dc.trajectory;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.jlab.clas.swimtools.Swim;
import org.jlab.detector.base.DetectorType;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.rec.dc.Constants;
import org.jlab.rec.dc.cross.Cross;


/**
 * The trajectory is a set of state vectors at DC wire planes along the particle path.  * A StateVec describes a cross measurement in the DC.  It is characterized by a point in the DC
 * tilted coordinate system at each wire plane (i.e. constant z) and by unit tangent vectors in the x and y
 * directions in that coordinate system.
 * @author ziegler
 *
 */
public class Trajectory extends ArrayList<Cross> {

    public Trajectory() {
    }

    private static final Logger LOGGER = Logger.getLogger(Trajectory.class.getName());
    
    private int id;
    private int sector;
    private double integratedBdL;
    private double pathLength;
    private List<StateVec> stateVecs;
    private List<TrajectoryStateVec> trajStateVecs = new ArrayList<>();

    private final static double TOLERANCE = 0.1; // trajectory toleerance (cm)
   
    public List<StateVec> getStateVecs() {
        return stateVecs;
    }

    public void setStateVecs(List<StateVec> vecs) {
        this.stateVecs = vecs;
    }

    public List<TrajectoryStateVec> getTrajectory() {
        return trajStateVecs;
    }

    private void addTrajectoryPoint(double x, double y, double z, double px, double py, double pz, double path, double bdl, Surface surface) {
        TrajectoryStateVec sv = new TrajectoryStateVec();
        sv.setDetector(surface.getDetectorType().getDetectorId());
        sv.setLayer(surface.getDetectorLayer());
        sv.setPoint(x, y, z);
        sv.setMomentum(px, py, pz);
        sv.setPath(path);
        sv.setDx(surface.dx(sv.getPoint(), sv.getDirection()));
        sv.setEdge(surface.distanceFromEdge(sv.getPoint()));
        sv.setiBdl(bdl);
        trajStateVecs.add(sv);
    }

    private void addTrajectoryPoint(double x, double y, double z, double px, double py, double pz, double path, double bdl, DetectorType type, int layer) {
        TrajectoryStateVec sv = new TrajectoryStateVec();
        sv.setDetector(type.getDetectorId());
        sv.setLayer(layer);
        sv.setPoint(x, y, z);
        sv.setMomentum(px, py, pz);
        sv.setPath(path);
        sv.setiBdl(bdl);
        trajStateVecs.add(sv);
    }

    public int getId() {
        return id;
    }

    public void setId(int _TrkId) {
        this.id = _TrkId;
    }

    public int getSector() {
        return sector;
    }

    public void setSector(int sector) {
        this.sector = sector;
    }

    public double getIntegralBdl() {
        return integratedBdL;
    }


    public void setIntegralBdl(double _IntegralBdl) {
        this.integratedBdL = _IntegralBdl;
    }

    public double getPathLength() {
        return pathLength;
    }

    public void setPathLength(double _PathLength) {
        this.pathLength = _PathLength;
    }
    
    
    public class TrajectoryStateVec {
    
        private int detectorId;
        private int layer;
        private Point3D  point;
        private Vector3D mom;
        private double _pathLen;
        private double _dx;
        private double _Bdl;
        private double _edge;

        public Point3D getPoint() {
            return point;
        }

        public void setPoint(double x, double y, double z) {
            this.point = new Point3D(x, y, z);
        }

        public Vector3D getMomentum() {
            return mom;
        }

        public void setMomentum(double px, double py, double pz) {
            this.mom = new Vector3D(px, py, pz);
        }

        public Vector3D getDirection() {
            return mom.asUnit();
        }

        public double getPath() {
            return _pathLen;
        }

        public void setPath(double _pathLen) {
            this._pathLen = _pathLen;
        }

        public double getiBdl() {
            return _Bdl;
        }

        public void setiBdl(double _B) {
            this._Bdl = _B;
        }

        public double getDx() {
            return _dx;
        }

        public void setDx(double _dx) {
            this._dx = _dx;
        }

        public double getEdge() {
            return _edge;
        }

        public void setEdge(double distance) {
            this._edge = distance;
        }

        public int getDetector() {
            return detectorId;
        }

        public void setDetector(int id) {
            this.detectorId = id;
        }

        public int getLayer() {
            return layer;
        }

        public void setLayer(int layer) {
            this.layer = layer;
        }

    }
    

    public void calcTrajectory(int trackId, Swim dcSwim, Point3D v, Vector3D p, int q) {
        
        this.getTrajectory().clear();

        int is = sector-1;
        
        //HTCC: swim to sphere and save end point
        dcSwim.SetSwimParameters(v.x(), v.y(), v.z(), p.x(), p.y(), p.z(), q);
        double[] htccPars = dcSwim.SwimToSphere(Constants.HTCCRADIUS);
        if(htccPars==null) return;
        this.addTrajectoryPoint(htccPars[0], htccPars[1], htccPars[2], htccPars[3], htccPars[4], htccPars[5], htccPars[6], htccPars[7], DetectorType.HTCC, 1); 

        //Swim to planes
        for(int j = 0; j<Constants.getInstance().trajSurfaces.getDetectorPlanes().get(is).size(); j++) {
            
            TrajectoryStateVec last = this.getTrajectory().get(this.getTrajectory().size()-1);
            
            Surface surface=Constants.getInstance().trajSurfaces.getDetectorPlanes().get(is).get(j);

            // swim backward from HTCC to Target
            if(surface.getDetectorType() == DetectorType.TARGET) {
                int   dir = -1;
                float b[] = new float[3];
                dcSwim.BfieldLab(v.x(), v.y(), v.z(), b);
                dcSwim.SetSwimParameters(last.getPoint().x(),     last.getPoint().y(),     last.getPoint().z(), 
                                        -last.getMomentum().x(), -last.getMomentum().y(), -last.getMomentum().z(), 
                                        -q);
                double[] tPars = dcSwim.SwimToPlaneBoundary(surface.getD(), surface.getNormal(), dir);
                if(tPars==null || surface.distanceFromPlane(tPars[0], tPars[1], tPars[2])>TOLERANCE) return;
                this.addTrajectoryPoint(tPars[0], tPars[1], tPars[2], -tPars[3], -tPars[4], -tPars[5], tPars[2]-v.z(), Math.abs((tPars[2]-v.z())*b[2]), surface);
            }
            // swim forward from vertex to FMT and from HTCC to FD planes
            else {
                int dir = 1;
                double path = 0;
                double bdl  = 0;
                if(surface.getDetectorType() == DetectorType.FMT) {
                    if(surface.vectorToPlane(v).dot(p)<=0) continue; // skip FMT is track vertex is on downstream side
                    dcSwim.SetSwimParameters(v.x(), v.y(), v.z(), p.x(), p.y(), p.z(), q);
                }
                else {
                    dcSwim.SetSwimParameters(htccPars[0], htccPars[1], htccPars[2], htccPars[3], htccPars[4], htccPars[5], q);
                    path = htccPars[6];
                    bdl  = htccPars[7];
                }
                double[] tPars = dcSwim.SwimToPlaneBoundary(surface.getD(), surface.getNormal(), dir);
                if(tPars==null) return;
                if(surface.distanceFromPlane(tPars[0], tPars[1], tPars[2])<TOLERANCE && surface.distanceFromEdge(tPars[0], tPars[1], tPars[2])>=-99) // save trajectory only if on surface (
                    this.addTrajectoryPoint(tPars[0], tPars[1], tPars[2], tPars[3], tPars[4], tPars[5], tPars[6]+path, tPars[7]+bdl, surface);
            }            
        }
    }


    @Override
    public String toString() {
        String s = "";
        for(int j = 0; j< this.getTrajectory().size(); j++) {
            TrajectoryStateVec traj = this.getTrajectory().get(j);
            s += this.getId() + " " + this.getTrajectory().size() +
                                " ("+ traj.getDetector() +") ["+ DetectorType.getType(traj.getDetector()).getName() +"] " +
                                String.format("xyz: (%.3f, %.3f, %.3f) ", traj.getPoint().x(), traj.getPoint().y(), traj.getPoint().z()) +
                                String.format("dir: (%.3f, %.3f, %.3f) ", traj.getDirection().x(), traj.getDirection().y(), traj.getDirection().z()) +
                                String.format("path: %.3f ", traj.getPath()) +
                                String.format("dx: %.3f ", traj.getDx()) +
                                String.format("edge: %.3f\n", traj.getEdge());
        }  
        return s;
    }
}
