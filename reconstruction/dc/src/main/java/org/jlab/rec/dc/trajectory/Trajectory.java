package org.jlab.rec.dc.trajectory;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.jlab.clas.swimtools.Swim;
import org.jlab.detector.base.DetectorLayer;
import org.jlab.detector.base.DetectorType;
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
        private double _Bdl;
        private double _dEdx;
        private int _DetId;
        private int _LayerId;
        private String _DetName;


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
            return _Bdl;
        }

        public void setiBdl(double _B) {
            this._Bdl = _B;
        }

        public double getdEdx() {
            return _dEdx;
        }

        public void setdEdx(double _dEdx) {
            this._dEdx = _dEdx;
        }

        public int getDetId() {
            return _DetId;
        }

        public void setDetId(int _DetId) {
            this._DetId = _DetId;
        }

        public int getLayerId() {
            return _LayerId;
        }

        public void setLayerId(int _LayerId) {
            this._LayerId = _LayerId;
        }

        public String getDetName() {
            return _DetName;
        }

        public void setDetName(String name) {
            this._DetName = name;
        }
    }
    
    
    
    
    public List<TrajectoryStateVec> trajectory;
    float b[] = new float[3];
    public void calcTrajectory(int id, Swim dcSwim, double x, double y,  double z, double px, double py, double pz, int q, TrajectorySurfaces ts) {
        trajectory = new ArrayList<>();
        dcSwim.SetSwimParameters(x, y, z, px, py, pz, q);
        dcSwim.BfieldLab(x, y, z, b);
        double pathLen =0;
        double iBdl = 0;
        int dir  = 1;
        
        //HTCC: swim to sphere and save end point
//        LOGGER.log(Level.FINE, "New track " + x + " " + y + " " + z);
        double[] trkParsCheren = dcSwim.SwimToSphere(Constants.HTCCRADIUS);
        if(trkParsCheren==null) return;
        this.FillTrajectory(id, trajectory, trkParsCheren, trkParsCheren[6], trkParsCheren[7], DetectorType.HTCC, 1); 
        pathLen = trkParsCheren[6];
        iBdl    = trkParsCheren[7]; 
//        LOGGER.log(Level.FINE,  "HTCC" + " " + trkParsCheren[0] + " " + trkParsCheren[1] + " " + trkParsCheren[2] + " " + trkParsCheren[6] + " " + trkParsCheren[7]);
       

        int is = _Sector-1;
        // loop over surfaces: Target, FMT, DC, LTCC, FTOF, ECAL
        double[] trkPars = null;
        double[] DCtrkPars = null;
        TrackVec tv = new TrackVec() ;
        tv.setSector(_Sector);
        tv.FrameRefId  = 0;
        for(int j = 0; j<ts.getDetectorPlanes().get(is).size(); j++) {
            
            Surface surface=ts.getDetectorPlanes().get(is).get(j);
            // set swimming starting point depending on surface
            
            //handle DC differently
            if(surface.getDetectorType()==DetectorType.DC) {
                // create a trackVec in the lab and rotate it to the DC TSC frame for track propagation
                tv.set(trkParsCheren[0], trkParsCheren[1],trkParsCheren[2], trkParsCheren[3], trkParsCheren[4], trkParsCheren[5]) ;
                tv.TransformToTiltSectorFrame();
                dcSwim.SetSwimParameters(tv.x(), tv.y(), tv.z(), tv.px(), tv.py(), tv.pz(), q);
                DCtrkPars = dcSwim.SwimToPlaneTiltSecSys(this.get_Sector(), surface.get_d());
                tv.set(DCtrkPars[0], DCtrkPars[1],DCtrkPars[2], DCtrkPars[3], DCtrkPars[4], DCtrkPars[5]) ;
                tv.TransformToLabFrame(_Sector);
                trkPars[0]=tv.x();
                trkPars[1]=tv.y();
                trkPars[2]=tv.z();
                trkPars[3]=tv.px();
                trkPars[4]=tv.py();
                trkPars[5]=tv.pz();
                trkPars[6]=DCtrkPars[6];
                trkPars[7]=DCtrkPars[7];
            } 
            // set swimming starting point depending on surface
            else {
                if(surface.getDetectorType()==DetectorType.TARGET) {
                    if(surface.getDetectorLayer()==DetectorLayer.TARGET_DOWNSTREAM) {
                        dcSwim.SetSwimParameters(trkParsCheren[0], trkParsCheren[1], trkParsCheren[2], -trkParsCheren[3], -trkParsCheren[4], -trkParsCheren[5], -q);
                        dir=-1;
                    }
                    else {
                        dcSwim.SetSwimParameters(trkPars[0], trkPars[1], trkPars[2], -trkPars[3], -trkPars[4], -trkPars[5], -q);
                        dir=-1;
                    }
                }
                else if(surface.getDetectorType()==DetectorType.FMT) {
                    dcSwim.SetSwimParameters(x, y, z, px, py, pz, q);
                    dir=1;
                }
                else {
                    dcSwim.SetSwimParameters(trkParsCheren[0], trkParsCheren[1], trkParsCheren[2], trkParsCheren[3], trkParsCheren[4], trkParsCheren[5], q);
                    dir=1;
                }
                
                // Swim in the lab for all detectors that are not DC
                trkPars = dcSwim.SwimToPlaneBoundary(surface.get_d(), new Vector3D(surface.get_nx(),surface.get_ny(),surface.get_nz()),dir);
            
            }    
            
//            if(surface.getDetectorIndex()==DetectorType.DC.getDetectorId()) {  // start swiming from previous DC layer
//                dcSwim.SetSwimParameters(trkPars[0], trkPars[1], trkPars[2], trkPars[3], trkPars[4], trkPars[5], q);
//            }
  
            if(trkPars==null) {
                //LOGGER.log(Level.FINE, " Failed swim");
                return;
            }
                
//            LOGGER.log(Level.FINE, surface.getDetectorType().getName() + " " + surface.getDetectorLayer() + " " + trkPars[0] + " " + trkPars[1] + " " + trkPars[2] + " " + trkPars[6] + " " + trkPars[7]);

            // if surface correspond to target, invert unit vector before is saved and calculate manually the pathlength
            if(surface.getDetectorType()==DetectorType.TARGET) {
                for (int b = 3; b<6; b++) {
                    trkPars[b]*=-1;
                }
                this.FillTrajectory(id, trajectory, trkPars, trkPars[2]-z, Math.abs((trkPars[2]-z)*b[2]), surface.getDetectorType(), surface.getDetectorLayer());                     
            }
            else if(surface.getDetectorType()==DetectorType.FMT){
                this.FillTrajectory(id, trajectory, trkPars, trkPars[6], trkPars[7], surface.getDetectorType(), surface.getDetectorLayer());
            }
            else {
//                LOGGER.log(Level.FINE, surface.getDetectorType() + " " + surface.getDetectorLayer() + " " + trkPars[2] + " " + pathLen + " " + trkPars[6]);
                this.FillTrajectory(id, trajectory, trkPars, pathLen+trkPars[6], iBdl+trkPars[7], surface.getDetectorType(), surface.getDetectorLayer());               
            }
            
        }
    }

    private void FillTrajectory(int id, List<TrajectoryStateVec> trajectory, double[] trkPars, double pathLen, double iBdl, DetectorType type, int layer) {
        TrajectoryStateVec sv = new TrajectoryStateVec();
        sv.setDetName(type.getName());
        sv.setDetId(type.getDetectorId());
        sv.setLayerId(layer);
        sv.setTrkId(id);
        sv.setX(trkPars[0]);
        sv.setY(trkPars[1]);
        sv.setZ(trkPars[2]);
        sv.setpX(trkPars[3]);
        sv.setpY(trkPars[4]);
        sv.setpZ(trkPars[5]);
        sv.setPathLen(pathLen);
        sv.setiBdl(iBdl);
        trajectory.add(sv);
    }
    
    
    ///
    private static final long serialVersionUID = 358913937206455870L;

}
