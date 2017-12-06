/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rec.fvt.track;

import eu.mihosoft.vrl.v3d.Vector3d;
import java.util.ArrayList;
import java.util.List;
import org.jlab.geom.prim.Vector3D;
import org.jlab.geometry.prim.Line3d;
import org.jlab.rec.dc.trajectory.DCSwimmer;
import org.jlab.rec.fvt.GeometryLoader;
import org.jlab.rec.fvt.fmt.cluster.Cluster;
import org.jlab.rec.fvt.track.trajectory.TrajectoryStateVec;

/**
 *
 * @author ziegler
 */
public class Track {
    
    private int _Id;
    private int _Sector;
     
    public int get_Id() {
        return _Id;
    }

    public void set_Id(int id) {
        this._Id = id;
    }
    
    public int get_Sector() {
        return _Sector;
    }

    public void set_Sector(int sector) {
        this._Sector = sector;
    }
    
    private List<Cluster> _Clusters;

    public List<Cluster> get_Clusters() {
        return _Clusters;
    }

    public void set_Clusters(List<Cluster> _Clusters) {
        this._Clusters = _Clusters;
    }
    
    private double _x;
    private double _y;
    private double _z;
    private double _px;
    private double _py;
    private double _pz;
    
    public double getX() {
        return _x;
    }

    public void setX(double _x) {
        this._x = _x;
    }

    public double getY() {
        return _y;
    }

    public void setY(double _y) {
        this._y = _y;
    }

    public double getZ() {
        return _z;
    }

    public void setZ(double _z) {
        this._z = _z;
    }

    public double getPx() {
        return _px;
    }

    public void setPx(double _px) {
        this._px = _px;
    }

    public double getPy() {
        return _py;
    }

    public void setPy(double _py) {
        this._py = _py;
    }

    public double getPz() {
        return _pz;
    }

    public void setPz(double _pz) {
        this._pz = _pz;
    }
    
    private int _q;

    public int getQ() {
        return _q;
    }

    public void setQ(int _q) {
        this._q = _q;
    }
    public List<TrajectoryStateVec> trajectory;
    public void calcTrajectory(DCSwimmer dcSwim) {
        trajectory = new ArrayList<TrajectoryStateVec>();
        dcSwim.SetSwimParameters(this._x, this._y, this._z, this._px, this._py, this._pz, this._q);
       
        double[] trkPars = new double[8];
        //HTCC
        double[] trkParsCheren = dcSwim.SwimToSphere(20);
        this.FillTrajectory(trajectory, trkParsCheren, trkParsCheren[6], 0); 
        //reinit Cheren
        for(int k =0; k<8; k++)
            trkParsCheren[k] = 0;
        
        int is = this._Sector-1;
        double pathLen =0;
        for(int j = 0; j<GeometryLoader.getDetectorPlanes().get(is).size(); j++) {
            
            if(j>0 ) {
                dcSwim.SetSwimParameters(trkPars[0], trkPars[1], trkPars[2], trkPars[3], trkPars[4], trkPars[5], this._q);
            }
            trkPars = dcSwim.SwimToPlaneBoundary(GeometryLoader.getDetectorPlanes().get(is).get(j).get_d(), new Vector3D(GeometryLoader.getDetectorPlanes().get(is).get(j).get_nx(),
            GeometryLoader.getDetectorPlanes().get(is).get(j).get_ny(),GeometryLoader.getDetectorPlanes().get(is).get(j).get_nz()),1);
            if(trkPars==null)
                return;
            if(j==42) {
                for(int k =0; k<6; k++ )
                trkParsCheren[k] = trkPars[k];
            }
            if(GeometryLoader.getDetectorPlanes().get(is).get(j).getDetectorName().startsWith("FTOF")) {
                int FTOFDt = GeometryLoader.getFTOFPanel(new Line3d(new Vector3d(trkPars[0]-100*trkPars[3],trkPars[1]-100*trkPars[4],trkPars[2]-100*trkPars[5]), new Vector3d(trkPars[0]+100*trkPars[3],trkPars[1]+100*trkPars[4],trkPars[2]+100*trkPars[5])));
                if(FTOFDt==3) {
                    pathLen+=trkPars[6];
                    this.FillTrajectory(trajectory, trkPars, pathLen, j+1); 
                    return;
                } else {
                    if(j==44) {
                        //reset start swim point
                        dcSwim.SetSwimParameters(trkParsCheren[0], trkParsCheren[1], trkParsCheren[2], trkParsCheren[3], trkParsCheren[4], trkParsCheren[5], this._q);
                        trkPars = dcSwim.SwimToPlaneBoundary(GeometryLoader.getDetectorPlanes().get(is).get(j).get_d(), new Vector3D(GeometryLoader.getDetectorPlanes().get(is).get(j).get_nx(),
                        GeometryLoader.getDetectorPlanes().get(is).get(j).get_ny(),GeometryLoader.getDetectorPlanes().get(is).get(j).get_nz()),1);
            
                        pathLen+=trkPars[6];
                        this.FillTrajectory(trajectory, trkPars, pathLen, j+1); 
                    }
                    if(j==45) {
                        //1a
                        pathLen+=trkPars[6];
                        this.FillTrajectory(trajectory, trkPars, pathLen, j+1); 
                    }
                }
            } else {
                pathLen+=trkPars[6];
                this.FillTrajectory(trajectory, trkPars, pathLen, j+1); 
            }
            
        }
    }

    private void FillTrajectory(List<TrajectoryStateVec> trajectory, double[] trkPars, double pathLen, int i) {
        TrajectoryStateVec sv = new TrajectoryStateVec();
        sv.setDetId(i);
        if(i==0)
            sv.setDetName("HTCC");
        if(i>0)
            sv.setDetName(GeometryLoader.getDetectorPlanes().get(0).get(i-1).getDetectorName());
        sv.setTrkId(this._Id);
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
    
}
