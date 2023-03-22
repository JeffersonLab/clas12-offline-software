/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.jlab.rec.vtx;

import cnuphys.swim.SwimTrajectory;
import org.jlab.clas.swimtools.Swim;
/**
 *
 * @author ziegler
 */
public class DoubleSwim  extends Swim {
    private double _x01;
    private double _y01;
    private double _z01;
    private double _px01;
    private double _py01;
    private double _pz01;
    private int _charge1;
    private double _x02;
    private double _y02;
    private double _z02;
    private double _px02;
    private double _py02;
    private double _pz02;
    private int _charge2;
    public double Z;
    private Swim swim1;
    private Swim swim2;
    
    private double r = 99999;
   
    public DoubleSwim(double x01, double y01, double z01, double px01, double py01, double pz01, int charge1,
            double x02, double y02, double z02, double px02, double py02, double pz02, int charge2) {
        _x01 = x01;
        _y01 = y01;
        _z01 = z01;
        _px01 = px01;
        _py01 = py01;
        _pz01 = pz01;
        _charge1 = charge1;
        _x02 = x02;
        _y02 = y02;
        _z02 = z02;
        _px02 = px02;
        _py02 = py02;
        _pz02 = pz02;
        _charge2 = charge2;
        
        swim1 = new Swim();
        swim2 = new Swim();
        
        swim1.stepSize= 500.00* 1.e-6; // 500 microns
        swim2.stepSize= 500.00* 1.e-6; // 500 microns
        swim1.distanceBetweenSaves=500.00 * 1.e-6; // 500 microns
        swim2.distanceBetweenSaves=500.00 * 1.e-6; // 500 microns
        
    }
    
    public double[][] getDoubleSwimVertexes() {
        double buffer = 2.0;
        swim1.SetSwimParameters(_x01, _y01, _z01, -_px01, -_py01, -_pz01, -_charge1);
        swim2.SetSwimParameters(_x02, _y02, _z02, -_px02, -_py02, -_pz02, -_charge2);
        double initZ;
        if(_z01<_z02) {
            initZ = _z01;
        } else {
            initZ = _z02;
        }
        
        double finZ ;//= getZ(swim1, swim2);
        if(_z01>_z02) {
            finZ = _z01;
        } else {
            finZ = _z02;
        }
        finZ+=Constants.DZ;
        double[] tr1 = swim1.SwimToZ(initZ-buffer, -1);
        double[] tr2 = swim2.SwimToZ(initZ-buffer, -1);
        swim1.SetSwimParameters(tr1[0], tr1[1], tr1[2], -tr1[3], -tr1[4], -tr1[5], _charge1);
        swim2.SetSwimParameters(tr2[0], tr2[1], tr2[2], -tr2[3], -tr2[4], -tr2[5], _charge2);
        
        double[][] vtxx = new double[2][6];
        swim1.SwimToZ(finZ+buffer, 1); 
        swim2.SwimToZ(finZ+buffer, 1);
        
        SwimTrajectory[] swts = new SwimTrajectory[2];
        swts[0] = swim1.getSwimTraj();
        swts[1] = swim2.getSwimTraj();
        double[] vtx1 = swim1.SwimToDCA(swts[1]);
        double[] vtx2 = swim2.SwimToDCA(swts[0]);
        vtxx[0]=vtx1;
        vtxx[1]=vtx2;
        
        return vtxx;
    }
    
    public SwimTrajectory[] getTrajectories(double Z) {
        swim1.SetSwimParameters(_x01, _y01, _z01, _px01, _py01, _pz01, _charge1);
        swim2.SetSwimParameters(_x02, _y02, _z02, _px02, _py02, _pz02, _charge2);
        
        double[] tr1 = swim1.SwimToZ(Z, 1); 
        double[] tr2 = swim2.SwimToZ(Z, 1);
        
        SwimTrajectory[] swt = new SwimTrajectory[2];
        swt[0] = swim1.getSwimTraj();
        swt[1] = swim2.getSwimTraj();
        
        return swt;
    }
    
//    public List<List<Line3D>> trajToPolylines(SwimTrajectory[] swts) {
//        List<List<Line3D>> polylines = new ArrayList<>();
//        for(int j = 0; j<2; j++) {
//            polylines.add(new ArrayList<>());
//            for(int i = 0; i < swts[j].size()-1; i++) {
//                polylines.get(j).add(new Line3D(swts[j].get(i)[0]*100,swts[j].get(i)[1]*100,swts[j].get(i)[2]*100,
//                        swts[j].get(i+1)[0]*100,swts[j].get(i+1)[1]*100,swts[j].get(i+1)[2]*100));
//            }
//        }
//        return polylines;
//    }  
//    
//    public Point3D interpPolylines(List<List<Line3D>> polylines) {
//        double[] value = new double[13];
//        double dmin = Double.POSITIVE_INFINITY;
//        Point3D vtx = null;
//        
//        for(int i1 = 0; i1 < polylines.get(0).size(); i1++) {
//            for(int i2 = 0; i2 < polylines.get(1).size(); i2++) {
//                Line3D l1 = polylines.get(0).get(i1);
//                Line3D l2 = polylines.get(1).get(i2);
//                Line3D lc = l2.distance(l1);
//                Point3D mid = lc.midpoint();
//                double d = lc.origin().distance(lc.end());
//                if(d<dmin) {
//                    dmin = d;
//                    vtx = mid;
//                }
//            }
//        }
//        return vtx;
//    }
//    
//    public double getZ(Swim swim1, Swim swim2) {
//        //swim until the path split more than X value
//        swim1.SetSwimParameters(_x01, _y01, _z01, -_px01, -_py01, -_pz01, -_charge1);
//        swim2.SetSwimParameters(_x02, _y02, _z02, -_px02, -_py02, -_pz02, -_charge2);
//        double initZ;
//        double[] tr1 = new double[]{_x01, _y01, _z01, _px01, _py01, _pz01,0,0}; 
//        double[] tr2 = new double[]{_x02, _y02, _z02, _px02, _py02, _pz02,0,0}; 
//        double delta= 1.0;
//        if(_z01<_z02) {
//            initZ = _z01-delta;
//        } else {
//            initZ = _z02-delta;
//        }
//        tr1 = swim1.SwimToZ(initZ, -1);
//        tr2 = swim2.SwimToZ(initZ, -1);
//        swim1.SetSwimParameters(tr1[0], tr1[1], tr1[2], -tr1[3], -tr1[4], -tr1[5], _charge1);
//        swim2.SetSwimParameters(tr2[0], tr2[1], tr2[2], -tr2[3], -tr2[4], -tr2[5], _charge2);
//
//        double dR2 = (tr2[0]-tr1[0])*(tr2[0]-tr1[0]) + (tr2[1]-tr1[1])*(tr2[1]-tr1[1]);
//        int s = 1;
//        double z = initZ+this.stepSize*(double)s;
//        tr1 = swim1.SwimToZ(z, 1);
//        tr2 = swim2.SwimToZ(z, 1);
//        while((tr2[0]-tr1[0])*(tr2[0]-tr1[0]) + (tr2[1]-tr1[1])*(tr2[1]-tr1[1])<dR2) {
//            dR2 = (tr2[0]-tr1[0])*(tr2[0]-tr1[0]) + (tr2[1]-tr1[1])*(tr2[1]-tr1[1]);
//            s++;
//            z = initZ+this.stepSize*(double)s;
//            tr1 = swim1.SwimToZ(z, 1);
//            tr2 = swim2.SwimToZ(z, 1);
//        }
//        //System.out.println("Z = "+z);
//        return z;
//    }
//    
    
}
