/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.geom.detector.dc;

import java.util.ArrayList;
import java.util.List;
import org.jlab.geom.base.ConstantProvider;
import org.jlab.geom.base.DetectorTransformation;
import org.jlab.geom.base.Factory;
import org.jlab.geom.component.DriftChamberWire;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Plane3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Transformation3D;
import org.jlab.geom.prim.Triangle3D;
import org.jlab.geom.prim.Vector3D;

/**
 *
 * @author gavalian
 */
public class DCFactoryUpdated implements Factory<DCDetector, DCSector, DCSuperlayer, DCLayer> {
    
    @Override
    public DCDetector createDetectorCLAS(ConstantProvider cp) {
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
                trans.translateXYZ(0, 0, gz);
                superlayer.setTransformation(trans);
            }
        }
        return detector;
    }
            
    @Override
    public DCDetector createDetectorLocal(ConstantProvider cp) {
        DCDetector detector = new DCDetector();
        for (int sectorId=0; sectorId<6; sectorId++)
            detector.addSector(createSector(cp, sectorId));
        return detector;
    }
    
    @Override
    public DCSector createSector(ConstantProvider cp, int sectorId) {
        if(!(0<=sectorId && sectorId<6))
            throw new IllegalArgumentException("Error: invalid sector="+sectorId);
        DCSector sector = new DCSector(sectorId);
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
        DCSuperlayer superlayer = new DCSuperlayer(sectorId, superlayerId);
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
        
        DCLayer layer = new DCLayer(sectorId, superlayerId, layerId);
        
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
        
//        System.out.println("sectorId="+sectorId+" superlayerId="+superlayerId+" layerId="+layerId);
//        System.out.println("double dist2tgt = "+dist2tgt);
//        System.out.println("double midgap   = "+midgap  );
//        System.out.println("double thtilt   = "+thtilt  );
//        System.out.println("double thopen   = "+thopen  );
//        System.out.println("double xdist    = "+xdist   );
//        System.out.println("double d_layer  = "+d_layer );
//        System.out.println("double thmin    = "+thmin   );
//        System.out.println("double thster   = "+thster  );
//        System.out.println("int    numWires = "+numWires);
        
        // Calculate the midpoint (gx, 0, gz) of the guard wire nearest to the 
        // beam in the first guard wire layer of the current superlayer
        double gz = dist2tgt;
        if (superlayerId%2 == 1) {
            gz += midgap + 21*cp.getDouble("/geometry/dc/superlayer/wpdist", superlayerId-1);
        }
        double gx = -gz*Math.tan(thtilt-thmin);
        gz = -(3*d_layer); // <-- Build DC Layers in local coordinates
        
        // Calculate the distance between the line of intersection of the two 
        // end-plate planes and the z-axis
        double xoff = dist2tgt*Math.tan(thtilt) - xdist/Math.sin(Math.PI/2-thtilt);
        
        // Construct the "left" end-plate plane
        Point3D  p1 = new Point3D(-xoff, 0, 0);
        Vector3D n1 = new Vector3D(0, 1, 0);
        n1.rotateZ(-thopen*0.5);
        n1.rotateY(thtilt);
        Plane3D lPlane = new Plane3D(p1, n1);
        
        // Construct the "right" end-plate plane
        Point3D  p2 = new Point3D(-xoff, 0, 0);
        Vector3D n2 = new Vector3D(0, -1, 0);
        n2.rotateZ(thopen*0.5);
        n2.rotateY(thtilt);
        Plane3D rPlane = new Plane3D(p2, n2);
        
        boolean flipReadoutDirection = true;
        if (superlayerId == 1) {
            flipReadoutDirection = false;
        }
        
        // Calculate the distance between wire midpoints in the current layer
        double w_layer = Math.sqrt(3)*d_layer/Math.cos(thster);
        
        // Calculate the point the midpoint (mx, 0, mz) of the first sense wire 
        // of the current layer
        double mx = gx + midpointXOffset(layerId, w_layer);
        double mz = gz + (layerId + 1)*(3*d_layer);
        
        // Iterate through all of the sense wires and store them as detector
        // paddles in a list
        for(int sesnorId=0; sesnorId<numWires; sesnorId++) {
            
            // The point given by (wx, 0, wz) is the midpoint of the current
            // wire.
            double wx = mx + sesnorId*2*w_layer;
            double wz = mz;
            Point3D wireMid = new Point3D(wx, 0, wz);
            
//            System.out.println((layer+1)+" "+(wire+1)+" "+wx+"\t"+wz);
            
            // Find the interesection of the current wire with the end-plate 
            // planes by construciting a long line that pbasses through the
            // the midpoint and which incorporates the wire's angle (thster)
            Line3D line = new Line3D(0, 1000, 0, 0, -1000, 0);
            line.rotateZ(thster);
            line.translateXYZ(wx, 0, wz);
            Point3D lPoint = new Point3D();
            Point3D rPoint = new Point3D();
            lPlane.intersection(line, lPoint);
            rPlane.intersection(line, rPoint);
            
            // All wire go from left to right
            Line3D wireLine = new Line3D(lPoint, rPoint);
            
            // Construct the hexagons the same way that the wire was constructed
            List<Point3D> botHex = new ArrayList();
            List<Point3D> topHex = new ArrayList();
            for (int h=0; h<6; h++) {
                double hexRadiusX = w_layer*Math.sqrt(5)*0.5;
                double hexRadiusZ = d_layer*2;
                double hx = wx + hexRadiusX*Math.sin(Math.toRadians(60*h));
                double hz = wz - hexRadiusZ*Math.cos(Math.toRadians(60*h));
                line = new Line3D(0, 1000, 0, 0, -1000, 0);
                line.rotateZ(thster);
                line.translateXYZ(hx, 0, hz);
                lPoint = new Point3D();
                rPoint = new Point3D();
                lPlane.intersection(line, lPoint);
                rPlane.intersection(line, rPoint);
                botHex.add(lPoint);
                topHex.add(rPoint);
            }
            
            // Create the wire
            DriftChamberWire wire = new DriftChamberWire(sesnorId, wireMid, wireLine, 
                    flipReadoutDirection, botHex, topHex);

            // Add wire's paddle object to the list
            layer.addComponent(wire);
        }
        
        List<DriftChamberWire> wires = layer.getAllComponents();
        
        Point3D pLL = new Point3D(
                wires.get(0).getLine().origin().x(),
                wires.get(0).getLine().origin().y(),
                mz);
        Point3D pLR = new Point3D(
                wires.get(0).getLine().end().x(),
                wires.get(0).getLine().end().y(),
                mz);
        Point3D pUL = new Point3D(
                wires.get(numWires-1).getLine().origin().x(),
                wires.get(numWires-1).getLine().origin().y(),
                mz);
        Point3D pUR = new Point3D(
                wires.get(numWires-1).getLine().end().x(),
                wires.get(numWires-1).getLine().end().y(),
                mz);
        layer.getBoundary().addFace(new Triangle3D(pLL, pLR, pUL));
        layer.getBoundary().addFace(new Triangle3D(pUR, pUL, pLR));
        
        layer.getMidplane().set(0, 0, mz, 0, 1, 0);
        layer.getPlane().set(0, 0, mz, 0, 0, 1);
        
        return layer;
    }
    
    /**
     * Returns the xoffset between the first guard wire of the first guard wire
     * layer of the current superlayer and the the first sense wire of the 
     * current layer.
     * <p><font color="red"><b>Developer's Note: </b></font>
     * The old geometry implementation produces this value incorrectly.  It
     * performs the layer staggering so that every other layer is shifted down
     * towards the x-axis.  However, it should shift every other layer up away
     * from the x-axis.  The correct and incorrect methods are both included.
     * When ready, comment out the old method and uncomment the new method.
     * 
     * @param layer the layer index within superlayer where the first sense wire
     * layer has index 0
     * @param w_layer the distance between midpoints of wires in the same layer
     * in the x-axis
     * @return the x-offset
     */
    protected static double midpointXOffset(int layer, double w_layer) {
        
        // old, incorrect method:
        //return (1 + (layer%2))*w_layer;
        
        // new, correct method:
        return (3 - (layer%2))*w_layer;   // KEEP
    }

    /**
     * Returns "DC Factory".
     * @return "DC Factory"
     */
    @Override
    public String getType() {
        return "DC Factory";
    }

    @Override
    public void show() {
        System.out.println(this);
    }
    
    @Override
    public String toString() {
        return getType();
    }    

    @Override
    public Transformation3D getTransformation(ConstantProvider cp, int sector, int superlayer, int layer) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public DetectorTransformation getDetectorTransform(ConstantProvider cp) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
