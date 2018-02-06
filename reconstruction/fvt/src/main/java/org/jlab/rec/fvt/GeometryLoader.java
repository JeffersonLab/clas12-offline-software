/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rec.fvt;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import org.jlab.detector.base.DetectorType;
import org.jlab.detector.base.GeometryFactory;
import org.jlab.detector.geant4.v2.DCGeant4Factory;
import org.jlab.detector.geant4.v2.FTOFGeant4Factory;
import org.jlab.detector.geant4.v2.ECGeant4Factory;
import org.jlab.detector.geant4.v2.PCALGeant4Factory;
import org.jlab.detector.geant4.v2.RICHGeant4Factory;
import org.jlab.detector.hits.DetHit;
import org.jlab.detector.hits.FTOFDetHit;
import org.jlab.geom.base.ConstantProvider;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.geometry.prim.Line3d;
import org.jlab.rec.dc.trajectory.DCSwimmer;
import org.jlab.rec.fvt.track.trajectory.Surface;
/**
 * A class to load the geometry constants used in the DC reconstruction. The
 * coordinate system used in the Tilted Sector coordinate system.
 *
 * @author ziegler
 *
 */
public class GeometryLoader {

    private static List<ArrayList<Surface>> _DetectorPlanes = new ArrayList<ArrayList<Surface>>();
    
    public static synchronized List<ArrayList<Surface>> getDetectorPlanes() {
        return _DetectorPlanes;
    }

    public static synchronized void setDetectorPlanes(List<ArrayList<Surface>> aDetectorPlanes) {
        _DetectorPlanes = aDetectorPlanes;
    }

    boolean isGeometryLoaded = false;
    private static DCGeant4Factory dcDetector;
    private static FTOFGeant4Factory ftofDetector;
    private static ECGeant4Factory ecDetector;
    private static PCALGeant4Factory pcalDetector;
    private static RICHGeant4Factory richDetector;

    
    public static synchronized void Load(int runNb, String var) {
        // the geometry is different is hardware and geometry... until GEMC gets updated we need to run with this flag
        ConstantProvider providerDC = GeometryFactory.getConstants(DetectorType.DC, runNb, var);
        //dcDetector = new DCGeant4Factory(providerDC);
        dcDetector = new DCGeant4Factory(providerDC, DCGeant4Factory.MINISTAGGERON);
        
        ConstantProvider providerFTOF = GeometryFactory.getConstants(DetectorType.FTOF, runNb, var);
        ftofDetector = new FTOFGeant4Factory(providerFTOF);
        
        ConstantProvider providerEC = GeometryFactory.getConstants(DetectorType.ECAL, runNb, var);
        ecDetector = new ECGeant4Factory(providerEC);
        pcalDetector = new PCALGeant4Factory(providerEC);
        System.out.println(" -- Det Geometry constants are Loaded for RUN   " + runNb + " with VARIATION " +var);
        
        System.out.println(" TOF "+ftofDetector.getFrontalFace(3, 2).point().toString());
    }
    
    public static int getFTOFPanel(Line3d trk) {
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
    public synchronized void LoadSurfaces() {
       
        int iw =0;
        
        double d = 0;
        Vector3D n;
        for(int is =0; is<6; is++) {
            int index =0;
        
            System.out.println(" CREATING SURFACES FOR SECTOR "+(is+1));
            this._DetectorPlanes.add(new ArrayList<Surface>());
            //add FMT
            for(int i=0;i<org.jlab.rec.fvt.fmt.Constants.FVT_Nlayers;i++) { 
		d = org.jlab.rec.fvt.fmt.Constants.FVT_Z1stlayer+i*org.jlab.rec.fvt.fmt.Constants.FVT_Interlayer;
                this._DetectorPlanes.get(is).add(new Surface("FMT"+(index+1), index++, i+1, d, 0., 0., 1.));
            }
            index=7; // end of MM + HTCC
            // Add DC
            n = this.RotateFromTSCtoLabC(0,0,1, is+1).toVector3D();
            for(int isup =0; isup<6; isup++) {
                for(int il =0; il<6; il++) {
                    d = GeometryLoader.dcDetector.getWireMidpoint(isup, il, iw).z; 
                    this._DetectorPlanes.get(is).add(new Surface("DC"+(index-6), index++, is*6+il+1,d, n.x(), n.y(), n.z()));                    
                }
            }
            //outer detectors           
            //LTCC
            this._DetectorPlanes.get(is).add(new Surface("LTCC", index++,1, 624.23, n.x(), n.y(), n.z()));       
             //FTOF 2 
            Vector3D  P = ftofDetector.getFrontalFace(is+1, 3).point().toVector3D();
            n = ftofDetector.getFrontalFace(is+1, 3).normal();
            d = P.dot(n);
            this._DetectorPlanes.get(is).add(new Surface("FTOF2", index++, 3, -d, -n.x(), -n.y(), -n.z()));
            //FTOF 18
            P = ftofDetector.getFrontalFace(is+1, 2).point().toVector3D();
            n = ftofDetector.getFrontalFace(is+1, 2).normal();
            d = P.dot(n);
            this._DetectorPlanes.get(is).add(new Surface("FTOF1b", index++, 2, -d, -n.x(), -n.y(), -n.z()));
            //FTOF 1A
            P = ftofDetector.getFrontalFace(is+1, 1).point().toVector3D();
            n = ftofDetector.getFrontalFace(is+1, 1).normal();
            d = P.dot(n);
            this._DetectorPlanes.get(is).add(new Surface("FTOF1a", index++, 1, -d, -n.x(), -n.y(), -n.z()));
            //PCAL (3)
            P = pcalDetector.getFrontalFace(is+1).point().toVector3D();
            n = pcalDetector.getFrontalFace(is+1).normal();
            d = P.dot(n);            
            this._DetectorPlanes.get(is).add(new Surface("PCAL", index++, 1, -d, -n.x(), -n.y(), -n.z()));
            //ECin (3)
            //U
            P = ecDetector.getFrontalFace(is+1).point().toVector3D();
            n = ecDetector.getFrontalFace(is+1).normal();
            d = P.dot(n);
            this._DetectorPlanes.get(is).add(new Surface("EC", index++, 1, -d, -n.x(), -n.y(), -n.z()));
            
        }
    }
    private Point3D RotateFromTSCtoLabC(double X, double Y, double Z, int sector) {
        double rzs = -X * Math.sin(Math.toRadians(25.)) + Z * Math.cos(Math.toRadians(25.));
        double rxs = X * Math.cos(Math.toRadians(25.)) + Z * Math.sin(Math.toRadians(25.));
        
        double rx = rxs * Math.cos((sector - 1) * Math.toRadians(60.)) - Y * Math.sin((sector - 1) * Math.toRadians(60.));
        double ry = rxs * Math.sin((sector - 1) * Math.toRadians(60.)) + Y * Math.cos((sector - 1) * Math.toRadians(60.));

        return new Point3D(rx,ry,rzs);
    }
}
