/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rec.dc.trajectory;

import java.util.ArrayList;
import java.util.List;
import org.jlab.detector.base.DetectorLayer;
import org.jlab.detector.base.DetectorType;

import org.jlab.detector.geant4.v2.DCGeant4Factory;
import org.jlab.detector.geant4.v2.FTOFGeant4Factory;
import org.jlab.geom.base.Detector;
import org.jlab.geom.prim.Vector3D;
/**
 * A class to load the geometry constants used in the DC reconstruction. The
 * coordinate system used in the Tilted Sector coordinate system.
 *
 * @author ziegler
 *
 */
public class TrajectorySurfaces {

    private List<ArrayList<Surface>> _DetectorPlanes = new ArrayList<ArrayList<Surface>>();
    
    public List<ArrayList<Surface>> getDetectorPlanes() {
        return _DetectorPlanes;
    }

    public synchronized void setDetectorPlanes(List<ArrayList<Surface>> aDetectorPlanes) {
        _DetectorPlanes = aDetectorPlanes;
    }
    
    double FVT_Z1stlayer = 30.2967; // z-distance between target center and strips of the first layer.
    double FVT_Interlayer = 1.190;  // Keep this for now until the Geometry service is ready... or remove FMT from traj.
    public void LoadSurfaces(double targetPosition, double targetLength,
            DCGeant4Factory dcDetector,
            FTOFGeant4Factory ftofDetector,
            Detector ecalDetector) {
       
        double d = 0;
        Vector3D n;
        for(int is =0; is<6; is++) {
            int index =0;

            System.out.println(" CREATING SURFACES FOR SECTOR "+(is+1));
            this._DetectorPlanes.add(new ArrayList<Surface>());
            
            // add target center and downstream wall
            this._DetectorPlanes.get(is).add(new Surface(DetectorType.TARGET, DetectorLayer.TARGET_DOWNSTREAM, targetPosition+targetLength/2, 0., 0., 1.));
            this._DetectorPlanes.get(is).add(new Surface(DetectorType.TARGET, DetectorLayer.TARGET_CENTER, targetPosition, 0., 0., 1.));
            
            //add FMT
            for(int i=0;i<6;i++) { 
                d = FVT_Z1stlayer+i*FVT_Interlayer;               
                this._DetectorPlanes.get(is).add(new Surface(DetectorType.FMT, i+1, d, 0., 0., 1.));
            } 
            index=7; // end of MM + HTCC(7)
            // Add DC
            //n = this.RotateFromTSCtoLabC(0,0,1, is+1).toVector3D();
            // don't rotate to the lab
            n = new Vector3D(0,0,1);
            for(int isup =0; isup<6; isup++) {
                for(int il =5; il<6; il++) { // include only layer 6
                    d = dcDetector.getWireMidpoint(is, isup, il, 0).z; 
                    this._DetectorPlanes.get(is).add(new Surface(DetectorType.DC, isup*6+il+1,d, n.x(), n.y(), n.z()));
                    
                }
            } 
            //outer detectors           
             //FTOF 2 
            Vector3D  P = ftofDetector.getMidPlane(is+1, DetectorLayer.FTOF2).point().toVector3D();
            n = ftofDetector.getMidPlane(is+1, DetectorLayer.FTOF2).normal();
            d = P.dot(n);
            this._DetectorPlanes.get(is).add(new Surface(DetectorType.FTOF, DetectorLayer.FTOF2, -d, -n.x(), -n.y(), -n.z())); 
            //FTOF 18 
            P = ftofDetector.getMidPlane(is+1, DetectorLayer.FTOF1B).point().toVector3D();
            n = ftofDetector.getMidPlane(is+1, DetectorLayer.FTOF1B).normal();
            d = P.dot(n);
            this._DetectorPlanes.get(is).add(new Surface(DetectorType.FTOF, DetectorLayer.FTOF1B, -d, -n.x(), -n.y(), -n.z())); 
            //FTOF 1A
            P = ftofDetector.getMidPlane(is+1, DetectorLayer.FTOF1A).point().toVector3D();
            n = ftofDetector.getMidPlane(is+1, DetectorLayer.FTOF1A).normal();
            d = P.dot(n);
            this._DetectorPlanes.get(is).add(new Surface(DetectorType.FTOF, DetectorLayer.FTOF1A, -d, -n.x(), -n.y(), -n.z())); 
            //LTCC
            this._DetectorPlanes.get(is).add(new Surface(DetectorType.LTCC,1, 653.09, -n.x(), -n.y(), -n.z())); 
            //PCAL
            int superLayer = (int) ((DetectorLayer.PCAL_V-1)/3);
            int localLayer = (DetectorLayer.PCAL_V-1)%3;
            P = ecalDetector.getSector(is).getSuperlayer(superLayer).getLayer(localLayer).getPlane().point().toVector3D();
            n = ecalDetector.getSector(is).getSuperlayer(superLayer).getLayer(localLayer).getPlane().normal();
            d = P.dot(n);            
            this._DetectorPlanes.get(is).add(new Surface(DetectorType.ECAL, DetectorLayer.PCAL_V, d, n.x(), n.y(), n.z())); 
            //ECin
            superLayer = (int) ((DetectorLayer.EC_INNER_V-1)/3);
            localLayer = (DetectorLayer.EC_INNER_V-1)%3;
            P = ecalDetector.getSector(is).getSuperlayer(superLayer).getLayer(localLayer).getPlane().point().toVector3D();
            n = ecalDetector.getSector(is).getSuperlayer(superLayer).getLayer(localLayer).getPlane().normal();
            d = P.dot(n);
            this._DetectorPlanes.get(is).add(new Surface(DetectorType.ECAL, DetectorLayer.EC_INNER_V, d, n.x(), n.y(), n.z())); 
            //ECout
            superLayer = (int) ((DetectorLayer.EC_OUTER_V-1)/3);
            localLayer = (DetectorLayer.EC_OUTER_V-1)%3;
            P = ecalDetector.getSector(is).getSuperlayer(superLayer).getLayer(localLayer).getPlane().point().toVector3D();
            n = ecalDetector.getSector(is).getSuperlayer(superLayer).getLayer(localLayer).getPlane().normal();
            d = P.dot(n);
            this._DetectorPlanes.get(is).add(new Surface(DetectorType.ECAL, DetectorLayer.EC_OUTER_V, d, n.x(), n.y(), n.z())); 
        }
    }
//    private Point3D RotateFromTSCtoLabC(double X, double Y, double Z, int sector) {
//        double rzs = -X * Math.sin(Math.toRadians(25.)) + Z * Math.cos(Math.toRadians(25.));
//        double rxs = X * Math.cos(Math.toRadians(25.)) + Z * Math.sin(Math.toRadians(25.));
//        
//        double rx = rxs * Math.cos((sector - 1) * Math.toRadians(60.)) - Y * Math.sin((sector - 1) * Math.toRadians(60.));
//        double ry = rxs * Math.sin((sector - 1) * Math.toRadians(60.)) + Y * Math.cos((sector - 1) * Math.toRadians(60.));
//
//        return new Point3D(rx,ry,rzs);
//    }
}
