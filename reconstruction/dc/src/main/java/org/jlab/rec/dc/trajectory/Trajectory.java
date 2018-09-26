package org.jlab.rec.dc.trajectory;

import eu.mihosoft.vrl.v3d.Vector3d;
import java.util.ArrayList;
import java.util.List;
import org.jlab.clas.swimtools.Swim;
import org.jlab.detector.geant4.v2.FTOFGeant4Factory;
import org.jlab.detector.hits.DetHit;
import org.jlab.detector.hits.FTOFDetHit;
import org.jlab.geom.prim.Vector3D;
import org.jlab.geometry.prim.Line3d;
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

    private int _Sector;     
    private List<StateVec> _Trajectory;
    private double _IntegralBdl;
    private double _pathLength;

    public int get_Sector() {
        return _Sector;
    }

    public void set_Sector(int _Sector) {
        this._Sector = _Sector;
    }

    public List<StateVec> get_Trajectory() {
        return _Trajectory;
    }

    public void set_Trajectory(List<StateVec> _Trajectory) {
        this._Trajectory = _Trajectory;
    }

    public double get_IntegralBdl() {
        return _IntegralBdl;
    }


    public void set_IntegralBdl(double _IntegralBdl) {
        this._IntegralBdl = _IntegralBdl;
    }

    public double get_PathLength() {
        return _pathLength;
    }



    public void set_PathLength(double _PathLength) {
        this._pathLength = _PathLength;
    }
//    /**
//     * 
//     */
//    private double _x;
//    private double _y;
//    private double _z;
//    private double _px;
//    private double _py;
//    private double _pz;
//    
//    public double getX() {
//        return _x;
//    }
//
//    public void setX(double _x) {
//        this._x = _x;
//    }
//
//    public double getY() {
//        return _y;
//    }
//
//    public void setY(double _y) {
//        this._y = _y;
//    }
//
//    public double getZ() {
//        return _z;
//    }
//
//    public void setZ(double _z) {
//        this._z = _z;
//    }
//
//    public double getPx() {
//        return _px;
//    }
//
//    public void setPx(double _px) {
//        this._px = _px;
//    }
//
//    public double getPy() {
//        return _py;
//    }
//
//    public void setPy(double _py) {
//        this._py = _py;
//    }
//
//    public double getPz() {
//        return _pz;
//    }
//
//    public void setPz(double _pz) {
//        this._pz = _pz;
//    }
//    
//    private int _q;
//
//    public int getQ() {
//        return _q;
//    }
//
//    public void setQ(int _q) {
//        this._q = _q;
//    }
    //
    public class TrajectoryStateVec {
    
        private int _TrkId;
        private double _X;
        private double _Y;
        private double _Z;
        private double _tX;
        private double _tY;
        private double _tZ;
        private double _pathLen;
        private double _B;
        private double _dEdx;

        public int getTrkId() {
            return _TrkId;
        }

        public void setTrkId(int _TrkId) {
            this._TrkId = _TrkId;
        }

        public double getX() {
            return _X;
        }

        public void setX(double _X) {
            this._X = _X;
        }

        public double getY() {
            return _Y;
        }

        public void setY(double _Y) {
            this._Y = _Y;
        }

        public double getZ() {
            return _Z;
        }

        public void setZ(double _Z) {
            this._Z = _Z;
        }

        public double getpX() {
            return _tX;
        }

        public void setpX(double _tX) {
            this._tX = _tX;
        }

        public double getpY() {
            return _tY;
        }

        public void setpY(double _tY) {
            this._tY = _tY;
        }

        public double getpZ() {
            return _tZ;
        }

        public void setpZ(double _tZ) {
            this._tZ = _tZ;
        }

        public double getPathLen() {
            return _pathLen;
        }

        public void setPathLen(double _pathLen) {
            this._pathLen = _pathLen;
        }

        public double getiBdl() {
            return _B;
        }

        public void setiBdl(double _B) {
            this._B = _B;
        }

        public double getdEdx() {
            return _dEdx;
        }

        public void setdEdx(double _dEdx) {
            this._dEdx = _dEdx;
        }

        private int _DetId;

        public int getDetId() {
            return _DetId;
        }

        public void setDetId(int _DetId) {
            this._DetId = _DetId;
        }

        private String _Name;

        public String getDetName() {
            return _Name;
        }

        public void setDetName(String name) {
            this._Name = name;
        }
    }
    
    //
    
    
    private int getFTOFPanel(Line3d trk, FTOFGeant4Factory ftofDetector) {
        List<DetHit> hits = ftofDetector.getIntersections(trk);
        
        int panel = -1;
        if (hits != null && hits.size() > 0) {
            for (DetHit hit : hits) {
                FTOFDetHit fhit = new FTOFDetHit(hit);
                panel = fhit.getLayer();
            }
        }
       return panel;
    }
    
    
    
    public List<TrajectoryStateVec> trajectory;
    public void calcTrajectory(int id, Swim dcSwim, double x, double y,  double z, double px, double py, double pz, int q, FTOFGeant4Factory ftofDetector, TrajectorySurfaces ts,double tarCenter) {
        trajectory = new ArrayList<TrajectoryStateVec>();
        dcSwim.SetSwimParameters(x, y, z, px, py, pz, q);
        
        double tarWall = tarCenter+5./2.;
        double[] trkPars = new double[8];
        if(trkPars==null)
            return;
        //HTCC
        double[] trkParsCheren = dcSwim.SwimToSphere(175.);
        if(trkParsCheren==null)
            return;
        this.FillTrajectory(id, trajectory, trkParsCheren, trkParsCheren[6], 0, ts); 
        // Swim to target
        dcSwim.SetSwimParameters(trkParsCheren[0], trkParsCheren[1], trkParsCheren[2], -trkParsCheren[3], -trkParsCheren[4], -trkParsCheren[5], -q);
        double[] trkTar3 = dcSwim.SwimToPlaneLab(tarWall);
        if(trkTar3==null)
            return;
        for (int b = 3; b<6; b++) {
            trkTar3[b]*=-1;
        } 
        this.FillTrajectory(id, trajectory, trkTar3, tarWall-z, 102, ts);
        
        double[] trkTar1 = dcSwim.SwimToPlaneLab(tarCenter);
        if(trkTar1==null)
            return;
        for (int b = 3; b<6; b++) {
            trkTar1[b]*=-1;
        } 
        this.FillTrajectory(id, trajectory, trkTar1, tarCenter-z, 101, ts);
        
        //reset track to swim forward
        dcSwim.SetSwimParameters(x, y, z, px, py, pz, q);
        //reinit Cheren
        for(int k =0; k<8; k++) {
            trkParsCheren[k] = 0;
            trkTar3[k] = 0;
            trkTar1[k] = 0;
        }
        
        int is = this._Sector-1;
        double pathLen =0;
        for(int j = 0; j<ts.getDetectorPlanes().get(is).size(); j++) {
            
            if(j>0 ) {
                dcSwim.SetSwimParameters(trkPars[0], trkPars[1], trkPars[2], trkPars[3], trkPars[4], trkPars[5], q);
            }
            trkPars = dcSwim.SwimToPlaneBoundary(ts.getDetectorPlanes().get(is).get(j).get_d(), new Vector3D(ts.getDetectorPlanes().get(is).get(j).get_nx(),
            ts.getDetectorPlanes().get(is).get(j).get_ny(),ts.getDetectorPlanes().get(is).get(j).get_nz()),1);
            if(trkPars==null)
                return;
            if(j==42) {
                for(int k =0; k<6; k++ )
                trkParsCheren[k] = trkPars[k];
            }
            if(ts.getDetectorPlanes().get(is).get(j).getDetectorName().startsWith("FTOF")) {
                int FTOFDt = getFTOFPanel(new Line3d(new Vector3d(trkPars[0]-100*trkPars[3],trkPars[1]-100*trkPars[4],trkPars[2]-100*trkPars[5]), new Vector3d(trkPars[0]+100*trkPars[3],trkPars[1]+100*trkPars[4],trkPars[2]+100*trkPars[5])), ftofDetector);
                if(FTOFDt==3) {
                    pathLen+=trkPars[6];
                    this.FillTrajectory(id, trajectory, trkPars, pathLen, j+1, ts); 
                    return;
                } else {
                    if(j==44) {
                        //reset start swim point
                        dcSwim.SetSwimParameters(trkParsCheren[0], trkParsCheren[1], trkParsCheren[2], trkParsCheren[3], trkParsCheren[4], trkParsCheren[5], q);
                        trkPars = dcSwim.SwimToPlaneBoundary(ts.getDetectorPlanes().get(is).get(j).get_d(), new Vector3D(ts.getDetectorPlanes().get(is).get(j).get_nx(),
                        ts.getDetectorPlanes().get(is).get(j).get_ny(),ts.getDetectorPlanes().get(is).get(j).get_nz()),1);
            
                        pathLen+=trkPars[6];
                        this.FillTrajectory(id, trajectory, trkPars, pathLen, j+1, ts); 
                    }
                    if(j==45) {
                        //1a
                        pathLen+=trkPars[6];
                        this.FillTrajectory(id, trajectory, trkPars, pathLen, j+1, ts); 
                    }
                }
            } else {
                pathLen+=trkPars[6];
                this.FillTrajectory(id, trajectory, trkPars, pathLen, j+1, ts); 
            }
            
        }
    }

    private void FillTrajectory(int id, List<TrajectoryStateVec> trajectory, double[] trkPars, double pathLen, int i, TrajectorySurfaces ts) {
        TrajectoryStateVec sv = new TrajectoryStateVec();
        sv.setDetId(i);
        if(i==0)
            sv.setDetName("HTCC");
        if(i>0 && i<100)
            sv.setDetName(ts.getDetectorPlanes().get(0).get(i-1).getDetectorName());
        if(i>100)
            sv.setDetName("TAR");
        sv.setTrkId(id);
        sv.setX(trkPars[0]);
        sv.setY(trkPars[1]);
        sv.setZ(trkPars[2]);
        sv.setpX(trkPars[3]);
        sv.setpY(trkPars[4]);
        sv.setpZ(trkPars[5]);
        sv.setPathLen(pathLen);
        trajectory.add(sv);
        return;
    }
    
    
    ///
    private static final long serialVersionUID = 358913937206455870L;

}
