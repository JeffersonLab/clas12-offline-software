/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.detector.geom.dc;

import eu.mihosoft.vrl.v3d.Vector3d;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jlab.detector.geant4.v2.DCGeant4Factory;
import org.jlab.geom.DetectorId;
import org.jlab.geom.base.ConstantProvider;
import org.jlab.geom.base.DetectorTransformation;
import org.jlab.geom.base.Factory;
import org.jlab.geom.component.DriftChamberWire;
import org.jlab.geom.detector.dc.DCDetector;
import org.jlab.geom.detector.dc.DCLayer;
import org.jlab.geom.detector.dc.DCSector;
import org.jlab.geom.detector.dc.DCSuperlayer;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Plane3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Transformation3D;
import org.jlab.geom.prim.Vector3D;

/**
 *
 * @author gavalian
 */
public class DCGeantFactory implements Factory<DCDetector, DCSector, DCSuperlayer, DCLayer>{

    
    String geomDBVar = "";
    private  DCGeant4Factory dcDetector = null;
    
    private void initDetector(ConstantProvider provider){
        if(dcDetector==null){
            dcDetector = new DCGeant4Factory(provider, DCGeant4Factory.MINISTAGGERON);    
        }
    }
    
    @Override
    public DCDetector createDetectorCLAS(ConstantProvider cp) {
        //ConstantProvider provider = GeometryFactory.getConstants(DetectorType.DC, 11, Optional.ofNullable(geomDBVar).orElse("default"));
        initDetector(cp);
        DCDetector detector = createDetectorSector(cp);
        for (DCSector sector : detector.getAllSectors()) {
            for (DCSuperlayer superlayer : sector.getAllSuperlayers()) {
                for (DCLayer layer: superlayer.getAllLayers()) {
                    int sectorId = superlayer.getSectorId();
                    Transformation3D trans = layer.getTransformation();
                    trans.rotateZ(Math.toRadians(60*sectorId));
                    layer.setTransformation(trans);
                }
            }
        }
        return detector;
    }

    @Override
    public DCDetector createDetectorSector(ConstantProvider cp) {
        initDetector(cp);
        DCDetector detector = createDetectorTilted(cp);
        for (DCSector sector: detector.getAllSectors()) {
            for (DCSuperlayer superlayer: sector.getAllSuperlayers()) {
                for (DCLayer layer: superlayer.getAllLayers()) {
                    int regionId = superlayer.getSuperlayerId()/2;
                    double thtilt = Math.toRadians(cp.getDouble("/geometry/dc/region/thtilt", regionId));
                    Transformation3D trans = layer.getTransformation();
                    trans.rotateY(thtilt);
                    layer.setTransformation(trans);
                }
            }
        }
        return detector;       
    }

    @Override
    public DCDetector createDetectorTilted(ConstantProvider cp) {
        initDetector(cp);
        DCDetector detector = createDetectorLocal(cp);
        for (DCSector sector: detector.getAllSectors()) {
            for (DCSuperlayer superlayer: sector.getAllSuperlayers()) {
                int superlayerId = superlayer.getSuperlayerId();
                int region = superlayerId/2;
                double dist2tgt = cp.getDouble("/geometry/dc/region/dist2tgt", region);
                double midgap   = cp.getDouble("/geometry/dc/region/midgap", region);
                double d_layer  = cp.getDouble("/geometry/dc/superlayer/wpdist", superlayerId);
                double gz = dist2tgt + 3*d_layer;
                if (superlayerId%2 == 1) {
                    gz += midgap + 21*cp.getDouble("/geometry/dc/superlayer/wpdist", superlayerId-1);
                }
                Transformation3D trans = new Transformation3D();
                //trans.translateXYZ(0, 0, gz);
                superlayer.setTransformation(trans);
            }
        }
        return detector;
    }

    @Override
    public DCDetector createDetectorLocal(ConstantProvider cp) {
        initDetector(cp);
        DCDetector detector = new DCDetector(DetectorId.DC);
        for (int sectorId=0; sectorId<6; sectorId++)
            detector.addSector(createSector(cp, sectorId));
        return detector;
    }

    @Override
    public DCSector createSector(ConstantProvider cp, int sectorId) {
        if(!(0<=sectorId && sectorId<6))
            throw new IllegalArgumentException("Error: invalid sector="+sectorId);
        DCSector sector = new DCSector(DetectorId.DC,sectorId);
        for (int superlayerId=0; superlayerId<6; superlayerId++)
            sector.addSuperlayer(createSuperlayer(cp, sectorId, superlayerId));
        return sector;
    }

    @Override
    public DCSuperlayer createSuperlayer(ConstantProvider cp, int sectorId, int superlayerId) {
        if(!(0<=sectorId && sectorId<6))
            throw new IllegalArgumentException("Error: invalid sector="+sectorId);
        if(!(0<=superlayerId && superlayerId<6))
            throw new IllegalArgumentException("Error: invalid superlayer="+superlayerId);
        DCSuperlayer superlayer = new DCSuperlayer(DetectorId.DC,sectorId, superlayerId);
        for (int layerId=0; layerId<6; layerId++)
            superlayer.addLayer(createLayer(cp, sectorId, superlayerId, layerId));
        return superlayer;
    }

    @Override
    public DCLayer createLayer(ConstantProvider cp, int sectorId, int superlayerId, int layerId) {
        if(!(0<=sectorId && sectorId<6))
            throw new IllegalArgumentException("Error: invalid sector="+sectorId);
        if(!(0<=superlayerId && superlayerId<6))
            throw new IllegalArgumentException("Error: invalid superlayer="+superlayerId);
        if(!(0<=layerId && layerId<6))
            throw new IllegalArgumentException("Error: invalid layer="+layerId);
        
        DCLayer layer = new DCLayer(DetectorId.DC,sectorId, superlayerId, layerId);
        
        int regionId = superlayerId/2;
        
        // Load constants
        double dist2tgt =                cp.getDouble("/geometry/dc/region/dist2tgt",   regionId);
        double midgap   =                cp.getDouble("/geometry/dc/region/midgap",     regionId);
        double thtilt   = Math.toRadians(cp.getDouble("/geometry/dc/region/thtilt",     regionId));
        double thopen   = Math.toRadians(cp.getDouble("/geometry/dc/region/thopen",     regionId));
        double xdist    =                cp.getDouble("/geometry/dc/region/xdist",      regionId);
        double d_layer  =                cp.getDouble("/geometry/dc/superlayer/wpdist", superlayerId);
        double thmin    = Math.toRadians(cp.getDouble("/geometry/dc/superlayer/thmin",  superlayerId));
        // Quick Dirty fix for the stereo tilt first superlayer should be at -6 !
        //
        double thster   = -Math.toRadians(cp.getDouble("/geometry/dc/superlayer/thster", superlayerId));
        int numWires    =                cp.getInteger("/geometry/dc/layer/nsensewires", 0);
        
        Plane3D lPlane = new Plane3D();
        Plane3D rPlane = new Plane3D();
        
        for(int wireId = 0; wireId < 112; wireId++){            
            Vector3d vOrigin = dcDetector.getWireLeftend(superlayerId, layerId, wireId);
            Vector3d vEnd    = dcDetector.getWireRightend(superlayerId, layerId, wireId);            
            Vector3d vMidpoint = dcDetector.getWireMidpoint(superlayerId, layerId, wireId);
            Line3D   wireLine  = new Line3D(
                    vOrigin.x,vOrigin.y,vOrigin.z,
                    vEnd.x, vEnd.y, vEnd.z
            );
            
            Point3D wireMid = new Point3D(vMidpoint.x, vMidpoint.y,vMidpoint.z);
            
            /*System.out.println(" sector " + sectorId + " sl = " +  superlayerId 
                    + " layer " + layerId + "  wire " + wireId + "  midpoint "
                    + wireMid.toString());*/
            List<Point3D> botHex = new ArrayList();
            List<Point3D> topHex = new ArrayList();
            
            Vector3D originDir = wireLine.originDir();
            Vector3D    endDir = wireLine.originDir();
            
            lPlane.set(wireLine.origin(), originDir);
            rPlane.set(wireLine.end(), originDir);
            double w_layer = Math.sqrt(3)*d_layer/Math.cos(thster);
            Point3D lPoint = new Point3D();
            Point3D rPoint = new Point3D();
            
            double gz = dist2tgt;
            if (superlayerId%2 == 1) {
                gz += midgap + 21*cp.getDouble("/geometry/dc/superlayer/wpdist", superlayerId-1);
            }
            double gx = -gz*Math.tan(thtilt-thmin);
            gz = -(3*d_layer); // <-- Build DC Layers in local coordinates
            
            // Calculate the distance between the line of intersection of the two 
            // end-plate planes and the z-axis
            double xoff = dist2tgt*Math.tan(thtilt) - xdist/Math.sin(Math.PI/2-thtilt);
            double mx = gx + midpointXOffset(layerId, w_layer);
            double mz = gz + (layerId + 1)*(3*d_layer);
            double wx = mx + wireId*2*w_layer;
            double wz = mz;
            
            for (int h=0; h<6; h++) {
                double hexRadiusX = w_layer*Math.sqrt(5)*0.5;
                double hexRadiusZ = d_layer*2;
                double hx = wireMid.x() + hexRadiusX*Math.sin(Math.toRadians(60*h));
                double hz = wireMid.z() - hexRadiusZ*Math.cos(Math.toRadians(60*h));
                Line3D line = new Line3D(0, 1000, 0, 0, -1000, 0);
                line.rotateZ(thster);
                line.translateXYZ(hx, 0, hz);
                //line.translateXYZ(hx, 0.0, 0.0);
                lPoint = new Point3D();
                rPoint = new Point3D();
                
                lPoint.translateXYZ(0.0, 0.0, -hz);
                rPoint.translateXYZ(0.0, 0.0, -hz);
                
                lPlane.intersection(line, lPoint);
                rPlane.intersection(line, rPoint);
            
                botHex.add(rPoint);
                topHex.add(lPoint);     

            }
            Collections.reverse(botHex);
            Collections.reverse(topHex);
            DriftChamberWire wire = new DriftChamberWire(wireId, wireMid, wireLine, 
                    false, botHex, topHex);

            layer.addComponent(wire);
            
            
        }
        return layer;
    }

    protected static double midpointXOffset(int layer, double w_layer) {        
        // old, incorrect method:
        return (1 + (layer%2))*w_layer;        
        // new, correct method:
        //return (3 - (layer%2))*w_layer;   // KEEP
    }
    
    @Override
    public String getType() {
        return "DC Factory";
    }

    @Override
    public Transformation3D getTransformation(ConstantProvider cp, int sector, int superlayer, int layer) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public DetectorTransformation getDetectorTransform(ConstantProvider cp) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void show() {
        
    }
    
}
