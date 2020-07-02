/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.geom.detector.alert.AHDC;

import java.util.ArrayList;
import java.util.List;
import org.jlab.geom.base.ConstantProvider;
import org.jlab.geom.base.DetectorTransformation;
import org.jlab.geom.base.Factory;
import org.jlab.geom.component.PrismaticComponent;
import org.jlab.geom.component.RectangularComponent;
import org.jlab.geom.prim.Plane3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Shape3D;
import org.jlab.geom.prim.Transformation3D;
import org.jlab.geom.prim.Triangle3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.abs.AbstractComponent;
/**
 * A Low Energy Recoil Tracker (ALERT)
 * This is to implement ALERT Drift Chamber detector (AHDC).
 * There are 1 {@code MYSector_AHDC}
 * and 5 {@code MYSuperlayer_AHDC} per AHDC Detector.
 * There are 2 {@code MYLayer_AHDC}s for Superlayers 1,2,3
 * and 1 {@code MYLayer_AHDC} for Superlayers 0,4.
 * Each layer contains N {@code DriftChamberWire}s. Number of wires is different from one Superlayer to another. 
 * However, real AHDC cell shape is different from regular hexagon given by {@code DriftChamberWire}.
 * Implemented following the DCDetector example
 */
/**
 *
 * @author sergeyeva
 */
public class MYFactory_ALERTDCWire implements Factory<MYDetector_ALERTDCWire, MYSector_ALERTDCWire, MYSuperlayer_ALERTDCWire, MYLayer_ALERTDCWire> {
    @Override
    public MYDetector_ALERTDCWire createDetectorCLAS(ConstantProvider cp) {
        MYDetector_ALERTDCWire detector = createDetectorSector(cp);
        return detector;
    }
    
    @Override
    public MYDetector_ALERTDCWire createDetectorSector(ConstantProvider cp) {
        MYDetector_ALERTDCWire detector = createDetectorTilted(cp);
        return detector;
    }
    
    @Override
    public MYDetector_ALERTDCWire createDetectorTilted(ConstantProvider cp) {
        MYDetector_ALERTDCWire detector = createDetectorLocal(cp);
        return detector;
    }
    
    int nsectors = 1;
    @Override
    public MYDetector_ALERTDCWire createDetectorLocal(ConstantProvider cp) {
        MYDetector_ALERTDCWire detector = new MYDetector_ALERTDCWire();
        for (int sectorId=0; sectorId<nsectors; sectorId++)
            detector.addSector(createSector(cp, sectorId));
        return detector;
    }
    
    int nsuperl = 5; // 5 for AHDC
    @Override
    public MYSector_ALERTDCWire createSector(ConstantProvider cp, int sectorId) {
        if(!(0<=sectorId && sectorId<nsectors))
            throw new IllegalArgumentException("Error: invalid sector="+sectorId);
        MYSector_ALERTDCWire sector = new MYSector_ALERTDCWire(sectorId);
        for (int superlayerId=0; superlayerId<nsuperl; superlayerId++)
            sector.addSuperlayer(createSuperlayer(cp, sectorId, superlayerId));
        return sector;
    }
    
    

    int nlayers = 2; // 1 if superlayerId = 0 OR 4, 2 if superlayerId = 1 OR 2 OR 3 for AHDC
    @Override
    public MYSuperlayer_ALERTDCWire createSuperlayer(ConstantProvider cp, int sectorId, int superlayerId) {
        if(!(0<=sectorId && sectorId<nsectors))
            throw new IllegalArgumentException("Error: invalid sector="+sectorId);
        if(!(0<=superlayerId && superlayerId<nsuperl))
            throw new IllegalArgumentException("Error: invalid superlayer="+superlayerId);
        MYSuperlayer_ALERTDCWire superlayer = new MYSuperlayer_ALERTDCWire(sectorId, superlayerId);
        
        for (int layerId=0; layerId<nlayers; layerId++)
            superlayer.addLayer(createLayer(cp, sectorId, superlayerId, layerId));

        return superlayer;
    }

    @Override
    public MYLayer_ALERTDCWire createLayer(ConstantProvider cp, int sectorId, int superlayerId, int layerId) {
        if(!(0<=sectorId && sectorId<nsectors))
            throw new IllegalArgumentException("Error: invalid sector="+sectorId);
        if(!(0<=superlayerId && superlayerId<nsuperl))
            throw new IllegalArgumentException("Error: invalid superlayer="+superlayerId);
        if(!(0<=layerId && layerId<nlayers))
            throw new IllegalArgumentException("Error: invalid layer="+layerId);
        
        MYLayer_ALERTDCWire layer = new MYLayer_ALERTDCWire(sectorId, superlayerId, layerId);

        // Load constants AHDC
        double len      = 300.0d; // Length in Z mm!
        double round    = 360.0d;
        double numWires    = 47.0d; // 94, layer 0 superlayer 0
        double R_layer  = 32.0d;
        double DR_layer  = 4.0d;
        double thopen   = Math.toRadians(60);
        double thtilt   = Math.toRadians(30);
        
        double zoff1 = 0.0d;
        double zoff2 = 300.0d;
        Point3D  p1 = new Point3D(R_layer, 0, zoff1);
        Vector3D n1 = new Vector3D(0, 0, 1);
        //n1.rotateY(-thopen);
        //n1.rotateZ(thtilt);
        Plane3D lPlane = new Plane3D(p1, n1);
        
        Point3D  p2 = new Point3D(R_layer, 0, zoff2);
        Vector3D n2 = new Vector3D(0, 0, 1);
        //n2.rotateY(thopen);
        //n2.rotateZ(thtilt);
        Plane3D rPlane = new Plane3D(p2, n2);
        
        boolean flipReadoutDirection = true;
        //if (superlayerId == 1) {
         //   flipReadoutDirection = false;
        //}
        
        if (superlayerId == 0 || superlayerId == 4)
        {
            if (layerId == 1) return layer;
           
        }
       
        if (superlayerId == 0) 
        {
            numWires = 47.0d; //47
            R_layer = 32.0d;
        }
        
        else if (superlayerId == 1) 
        {
            numWires = 56.0d; //56
            R_layer = 38.0d;
        }
        
        else if (superlayerId == 2)
        {
            numWires = 72.0d; //72
            R_layer = 48.0d;
        }
        
        else if (superlayerId == 3)
        {
            numWires = 87.0d;
            R_layer = 58.0d;
        }
        else
        {
            numWires = 99.0d;
            R_layer = 68.0d;
        }
       
        // Calculate the radius for the layers of sense wires
        R_layer=R_layer+DR_layer*layerId;
        
        double alphaW_layer = Math.toRadians(round/(numWires));
        System.out.println("angle (deg.) between the wires of layer = "+Math.toDegrees(alphaW_layer)  );
        
        double rcell = 2.0d; // detection cell size
 
        // shift the wire end point +-20deg in XY plan
        double thster   = Math.toRadians(20.0d);
        //double thsterZ   = Math.atan(R_layer*Math.sin(thster)/300.0d);
        double zl = 300.0d;
        
        // Create AHDC sense wires
        for(int wireId=0; wireId<numWires; wireId++) {
         
            // The point given by (wx, wy, wz) is the midpoint of the current
            // wire.
            double wx = -R_layer*Math.sin(alphaW_layer*wireId);
            double wy = -R_layer*Math.cos(alphaW_layer*wireId);
            Point3D wireMid = new Point3D(wx, wy, 0);
            
            System.out.println("wireMid wx   = "+wx  );
            System.out.println("wireMid wy   = "+wy  );
            System.out.println("wire number#   = "+wireId  );
            
            // Find the interesection of the current wire with the end-plate 
            // planes by construciting a long line that passes through the
            // the midpoint
            double wx_end = -R_layer*Math.sin(alphaW_layer*wireId+thster*(Math.pow(-1,superlayerId)));
            double wy_end = -R_layer*Math.cos(alphaW_layer*wireId+thster*(Math.pow(-1,superlayerId)));
            Line3D line = new Line3D(wx, wy, 0, wx_end, wy_end, zl);
            
            //line.rotateY(thsterZ*Math.pow(-1,superlayerId));
            //line.translateXYZ(wx, wy-R, 0);
            Point3D lPoint = new Point3D();
            Point3D rPoint = new Point3D();
            lPlane.intersection(line, lPoint);
            rPlane.intersection(line, rPoint);
            // All wire go from left to right
            Line3D wireLine = new Line3D(lPoint, rPoint);
            // Do not change the code above. It is for signal wires positioning
           
            // Construct the cell around the signal wires created above
            // top
            double px_0 = -(R_layer+2)*Math.sin(alphaW_layer*wireId);
            double py_0 = -(R_layer+2)*Math.cos(alphaW_layer*wireId);      
            double px_1 = -(R_layer+2)*Math.sin(alphaW_layer*wireId+alphaW_layer/2);
            double py_1 = -(R_layer+2)*Math.cos(alphaW_layer*wireId+alphaW_layer/2);
            double px_2 = -(R_layer-2)*Math.sin(alphaW_layer*wireId+alphaW_layer/2);
            double py_2 = -(R_layer-2)*Math.cos(alphaW_layer*wireId+alphaW_layer/2);
            double px_3 = -(R_layer-2)*Math.sin(alphaW_layer*wireId);
            double py_3 = -(R_layer-2)*Math.cos(alphaW_layer*wireId);
            double px_4 = -(R_layer-2)*Math.sin(alphaW_layer*wireId-alphaW_layer/2);
            double py_4 = -(R_layer-2)*Math.cos(alphaW_layer*wireId-alphaW_layer/2);
            double px_5 = -(R_layer+2)*Math.sin(alphaW_layer*wireId-alphaW_layer/2);
            double py_5 = -(R_layer+2)*Math.cos(alphaW_layer*wireId-alphaW_layer/2);  
            // bottom (do not forget to add the +20 deg. twist respect to the "straight" version)
            double px_6 = -(R_layer+2)*Math.sin(alphaW_layer*wireId+thster*(Math.pow(-1,superlayerId)));
            double py_6 = -(R_layer+2)*Math.cos(alphaW_layer*wireId+thster*(Math.pow(-1,superlayerId)));
            double px_7 = -(R_layer+2)*Math.sin(alphaW_layer*wireId+alphaW_layer/2+thster*(Math.pow(-1,superlayerId)));
            double py_7 = -(R_layer+2)*Math.cos(alphaW_layer*wireId+alphaW_layer/2+thster*(Math.pow(-1,superlayerId)));
            double px_8 = -(R_layer-2)*Math.sin(alphaW_layer*wireId+alphaW_layer/2+thster*(Math.pow(-1,superlayerId)));
            double py_8 = -(R_layer-2)*Math.cos(alphaW_layer*wireId+alphaW_layer/2+thster*(Math.pow(-1,superlayerId)));
            double px_9 = -(R_layer-2)*Math.sin(alphaW_layer*wireId+thster*(Math.pow(-1,superlayerId)));
            double py_9 = -(R_layer-2)*Math.cos(alphaW_layer*wireId+thster*(Math.pow(-1,superlayerId)));
            double px_10 = -(R_layer-2)*Math.sin(alphaW_layer*wireId-alphaW_layer/2+thster*(Math.pow(-1,superlayerId)));
            double py_10 = -(R_layer-2)*Math.cos(alphaW_layer*wireId-alphaW_layer/2+thster*(Math.pow(-1,superlayerId)));
            double px_11 = -(R_layer+2)*Math.sin(alphaW_layer*wireId-alphaW_layer/2+thster*(Math.pow(-1,superlayerId)));
            double py_11 = -(R_layer+2)*Math.cos(alphaW_layer*wireId-alphaW_layer/2+thster*(Math.pow(-1,superlayerId)));

            // Group into points with (x,y,z) coordinates
            List<Point3D> firstF = new ArrayList();
            List<Point3D> secondF = new ArrayList();
            // first Face
            Point3D p_0 = new Point3D(px_0,py_0,0.0d);
            Point3D p_1 = new Point3D(px_1,py_1,0.0d);
            Point3D p_2 = new Point3D(px_2,py_2,0.0d);
            Point3D p_3 = new Point3D(px_3,py_3,0.0d);
            Point3D p_4 = new Point3D(px_4,py_4,0.0d);
            Point3D p_5 = new Point3D(px_5,py_5,0.0d);
            // second Face
            Point3D p_6 = new Point3D(px_6,py_6,zl);
            Point3D p_7 = new Point3D(px_7,py_7,zl);
            Point3D p_8 = new Point3D(px_8,py_8,zl);
            Point3D p_9 = new Point3D(px_9,py_9,zl);
            Point3D p_10 = new Point3D(px_10,py_10,zl);
            Point3D p_11 = new Point3D(px_11,py_11,zl);
            // defining a cell around a wireLine, must be counter-clockwise!
            firstF.add(p_0);
            firstF.add(p_1);
            firstF.add(p_2);
            firstF.add(p_3);
            firstF.add(p_4);
            firstF.add(p_5);
            
            secondF.add(p_6);
            secondF.add(p_7);
            secondF.add(p_8);
            secondF.add(p_9);
            secondF.add(p_10);
            secondF.add(p_11);
            
            // Create the cell and signal wire inside
            // PrismaticComponent(int componentId, List<Point3D> firstFace, List<Point3D> secondFace)
            // not possible to add directly PrismaticComponent class because it is an ABSTRACT
            // a new class should be created: public class NewClassWire extends PrismaticComponent {...}
            // 5 top points & 5 bottom points with convexe shape. Concave shape is not supported.
            ALERTDCWire wire = new ALERTDCWire(wireId, wireLine, firstF, secondF);
            // Add wire object to the list
            layer.addComponent(wire); 
            
            //wireId=wireId+1;
        }
        
        /*
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
        */
        
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
