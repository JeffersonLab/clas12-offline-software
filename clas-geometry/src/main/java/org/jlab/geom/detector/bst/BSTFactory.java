package org.jlab.geom.detector.bst;

import org.jlab.geom.base.ConstantProvider;
import org.jlab.geom.base.DetectorTransformation;
import org.jlab.geom.base.Factory;
import org.jlab.geom.component.SiStrip;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Plane3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Transformation3D;
import org.jlab.geom.prim.Triangle3D;
import org.jlab.geom.prim.Vector3D;

/**
 * A Barrel Silicon Tracker (BST) {@link org.jlab.geom.base.Factory Factory}.
 * <p>
 * Factory: <b>{@link org.jlab.geom.detector.bst.BSTFactory BSTFactory}</b><br> 
 * Hierarchy: 
 * <code>
 * {@link org.jlab.geom.detector.bst.BSTDetector BSTDetector} → 
 * {@link org.jlab.geom.detector.bst.BSTSector BSTSector} → 
 * {@link org.jlab.geom.detector.bst.BSTSuperlayer BSTSuperlayer} → 
 * {@link org.jlab.geom.detector.bst.BSTLayer BSTLayer} → 
 * {@link org.jlab.geom.component.SiStrip SiStrip}
 * </code>
 * <p>
 * Due to the way the BST is organized, it would be most convenient to organize
 * the BST hierarchy such that superlayers contain layers which contain sectors.
 * However, this is not directly possible within the clasrec-geometry API.  To
 * approximate the desired hierarchy within the framework of the API,
 * {@code BSTSector} contains one ring of sensor modules at a constant radius
 * from the beam. Each {@code BSTSector} contains two {@code BSTSuperlayer}s,
 * one representing the u-layer and the other the v-layer.  Each 
 * {@code BSTSuperlayer} has a number of {@code BSTLayer}s equal to the number
 * of sectors in the superlayer. Then, each {@code BSTLayer} contains a 256
 * {@code SiStrips}.
 * <p>
 * <font color="red"><b>Note:</b></font> "/geometry/bst/region/radius" is assumed
 * to measure the distance from the beam to the beam-side surtriangle of the 
 * u-sensor <b>not</b> the distance from the beam to the beam-side surtriangle of the
 * backing structure.
 * @author jnhankins
 */
public class BSTFactory implements Factory <BSTDetector, BSTSector, BSTSuperlayer, BSTLayer> {
    
    @Override
    public BSTDetector createDetectorCLAS(ConstantProvider cp) {
        return createDetectorSector(cp);
    }
    
    @Override
    public BSTDetector createDetectorSector(ConstantProvider cp) {
        return createDetectorTilted(cp);
    }
    
    @Override
    public BSTDetector createDetectorTilted(ConstantProvider cp) {
        return createDetectorLocal(cp);
    }
            
    @Override
    public BSTDetector createDetectorLocal(ConstantProvider cp) {
        BSTDetector detector = new BSTDetector();
        for (int sectorId=0; sectorId<4; sectorId++)
            detector.addSector(createSector(cp, sectorId));
        return detector;
    }

    public BSTRing createRing(ConstantProvider cp, int ring){
        BSTRing bstRing = new BSTRing();
        int nsectors = cp.getInteger("/geometry/bst/region/nsectors", ring);
        for(int loop = 0; loop < nsectors; loop++){
            BSTSector sector = new BSTSector(loop);
            bstRing.addSector(sector);
            BSTLayer layer_UP   = this.createRingLayer(cp, ring, 0, loop);
            BSTLayer layer_DOWN = this.createRingLayer(cp, ring, 1, loop);
            BSTSuperlayer superlayer = new BSTSuperlayer(loop,0);
            superlayer.addLayer(layer_UP);
            superlayer.addLayer(layer_DOWN);
            sector.addSuperlayer(superlayer);
            bstRing.addSector(sector);
            //for()
        }
        return bstRing;
    }
    
    @Override
    public BSTSector createSector(ConstantProvider cp, int sectorId) {
        if(!(0<=sectorId && sectorId<4))
            throw new IllegalArgumentException("Error: invalid sector="+sectorId);
        BSTSector sector = new BSTSector(sectorId);
        for (int superlayerId=0; superlayerId<2; superlayerId++)
            sector.addSuperlayer(createSuperlayer(cp, sectorId, superlayerId));
        return sector;
    }

    @Override
    public BSTSuperlayer createSuperlayer(ConstantProvider cp, int sectorId, int superlayerId) {
        if(!(0<=sectorId && sectorId<4))
            throw new IllegalArgumentException("Error: invalid sector="+sectorId);
        if(!(0<=superlayerId && superlayerId<2))
            throw new IllegalArgumentException("Error: invalid superlayer="+superlayerId);
        BSTSuperlayer superlayer = new BSTSuperlayer(sectorId, superlayerId);
        
        final int nsectors    = cp.getInteger("/geometry/bst/region/nsectors", sectorId);
        for (int layerId=0; layerId<nsectors; layerId++)
            superlayer.addLayer(createLayer(cp, sectorId, superlayerId, layerId));
        return superlayer;
    }
    
    public BSTLayer  createRingLayer(ConstantProvider cp, int ringId, int superlayerId, int layerIdtrue){
        
        int layerId = 0;
        if(layerIdtrue==0) layerId = 1;
        final int nsectors    = cp.getInteger("/geometry/bst/region/nsectors", ringId);
        
        if(!(0<=ringId && ringId<4))
            throw new IllegalArgumentException("Error: invalid sector="+ringId);
        if(!(0<=superlayerId && superlayerId<2))
            throw new IllegalArgumentException("Error: invalid superlayer="+superlayerId);
        if(!(0<=layerId && layerId<nsectors))
            throw new IllegalArgumentException("Error: invalid layer="+layerId);
        
        final double deadLen1     = cp.getDouble("/geometry/bst/sector/deadZnSenLen1", 0)*0.1;
        final double deadLen2     = cp.getDouble("/geometry/bst/sector/deadZnSenLen2", 0)*0.1;
        final double deadLen3     = cp.getDouble("/geometry/bst/sector/deadZnSenLen3", 0)*0.1;
        final double activeLength = cp.getDouble("/geometry/bst/sector/activeSenLen",  0)*0.1;
        final double activeWidth  = cp.getDouble("/geometry/bst/sector/activeSenWid",  0)*0.1;
        
        final double radius   = cp.getDouble("/geometry/bst/region/radius",    ringId)*0.1;
        final double zstart   = cp.getDouble("/geometry/bst/region/zstart",    ringId)*0.1;
        final double layergap = cp.getDouble("/geometry/bst/region/layergap",  ringId)*0.1;
        final double readoutPitch  = cp.getDouble("/geometry/bst/bst/readoutPitch",  0)*0.1;
        final double siliconWidth = cp.getDouble("/geometry/bst/bst/siliconWidth", 0)*0.1;
        
        double totLength = 3*activeLength + deadLen1*2 + deadLen2 + deadLen3;
        double halfPitch = readoutPitch*0.5;
        double y = radius;
        if (superlayerId%2 == 1)
            y += siliconWidth + layergap;
        
        Plane3D bPlane = new Plane3D(0, 0, totLength - deadLen1, 0, 0, -1);
        /*
        Plane3D lPlane = (superlayerId == 0)?
                new Plane3D( activeWidth*0.5, 0, 0, 1, 0, 0) :
                new Plane3D(-activeWidth*0.5, 0, 0, 1, 0, 0);
        */
         
        Plane3D lPlane = (layerId == 0)?
                new Plane3D( activeWidth*0.5, 0, 0, 1, 0, 0) :
                new Plane3D(-activeWidth*0.5, 0, 0, 1, 0, 0);
        
        
        Point3D p0 = new Point3D();
        Point3D p1 = new Point3D();
        Point3D p2 = new Point3D();
        Point3D p3 = new Point3D();
        Point3D p4 = new Point3D();
        Point3D p5 = new Point3D();
        Point3D p6 = new Point3D();
        Point3D p7 = new Point3D();
        Point3D pT = new Point3D();
        
        Line3D line = new Line3D();
        Vector3D dir = new Vector3D();
        
        Point3D b0 = new Point3D();
        Point3D b1 = new Point3D();
        Point3D b2 = new Point3D();
        Point3D b3 = new Point3D();
        Triangle3D triangle0 = new Triangle3D();
        Triangle3D triangle1 = new Triangle3D();

        //BSTLayer layer = new BSTLayer(ringId, superlayerId, layerId);
        BSTLayer layer = new BSTLayer(layerId, 0, superlayerId);
        double phi = -layerId*Math.PI*2.0/nsectors;

        for (int componentId=0; componentId<256; componentId++) {
            //double angle = layerId==0 ? Math.toRadians((255-componentId)/85.0) : -Math.toRadians(componentId/85.0);
            double angle = layerId==0 ?  Math.toRadians(componentId/85.0) : -Math.toRadians(componentId/85.0);
            dir.setXYZ(0, 0, 30);
            dir.rotateY(angle);

            double x = -(componentId-127.5)*readoutPitch;
            if(layerId==0){
                x = (componentId-127.5)*readoutPitch;
            }
            
            p0.set(x-halfPitch, 0, deadLen1);
            p1.set(x+halfPitch, 0, deadLen1);
            p2.set(x+halfPitch, siliconWidth, deadLen1);
            p3.set(x-halfPitch, siliconWidth, deadLen1);
            
            line.set(p0, dir);
            bPlane.intersection(line, p4);
            if (lPlane.intersection(line, pT)==1 && p0.distance(pT) < p0.distance(p4))
                p4.copy(pT);

            line.set(p1, dir);
            bPlane.intersection(line, p5);
            if (lPlane.intersection(line, pT)==1 && p0.distance(pT) < p0.distance(p5))
                p5.copy(pT);

            line.set(p2, dir);
            bPlane.intersection(line, p6);
            if (lPlane.intersection(line, pT)==1 && p0.distance(pT) < p0.distance(p6))
                p6.copy(pT);

            line.set(p3, dir);
            bPlane.intersection(line, p7);
            if (lPlane.intersection(line, pT)==1 && p0.distance(pT) < p0.distance(p7))
                p7.copy(pT);

            SiStrip strip = new SiStrip(componentId, p4, p5, p6, p7, p0, p1, p2, p3);
            
            //strip.translateXYZ(0, y, zstart);
            //strip.rotateZ(phi);

            layer.addComponent(strip);
        }

        for (int s=0; s<3; s++) {
            double dz = 0;
            switch (s) { // this switch statement does not use break statments
                case 2: dz += activeLength+deadLen3;
                case 1: dz += activeLength+deadLen2;
                case 0: dz += deadLen1; 
            }
            b0.set( activeWidth*0.5, 0, dz);
            b1.set(-activeWidth*0.5, 0, dz);
            b2.set(-activeWidth*0.5, 0, dz+activeLength);
            b3.set( activeWidth*0.5, 0, dz+activeLength);
            triangle0.set(b1, b0, b2);
            triangle1.set(b3, b2, b0);
            //triangle0.translateXYZ(0, y+0.5*siliconWidth, zstart);
            //triangle1.translateXYZ(0, y+0.5*siliconWidth, zstart);
            triangle0.rotateZ(-phi);
            triangle1.rotateZ(-phi);
            layer.getBoundary().addFace(triangle0);
            layer.getBoundary().addFace(triangle1);
        }
        
        layer.getPlane().set(0, 0, zstart, 0, 0, 1);
        
        return layer;
    }
    
    @Override
    public BSTLayer createLayer(ConstantProvider cp, int sectorId, int superlayerId, int layerId) {
        final int nsectors    = cp.getInteger("/geometry/bst/region/nsectors", sectorId);
        
        if(!(0<=sectorId && sectorId<4))
            throw new IllegalArgumentException("Error: invalid sector="+sectorId);
        if(!(0<=superlayerId && superlayerId<2))
            throw new IllegalArgumentException("Error: invalid superlayer="+superlayerId);
        if(!(0<=layerId && layerId<nsectors))
            throw new IllegalArgumentException("Error: invalid layer="+layerId);
        
        final double deadLen1     = cp.getDouble("/geometry/bst/sector/deadZnSenLen1", 0)*0.1;
        final double deadLen2     = cp.getDouble("/geometry/bst/sector/deadZnSenLen2", 0)*0.1;
        final double deadLen3     = cp.getDouble("/geometry/bst/sector/deadZnSenLen3", 0)*0.1;
        final double activeLength = cp.getDouble("/geometry/bst/sector/activeSenLen",  0)*0.1;
        final double activeWidth  = cp.getDouble("/geometry/bst/sector/activeSenWid",  0)*0.1;
        
        final double radius   = cp.getDouble("/geometry/bst/region/radius",    sectorId)*0.1;
        final double zstart   = cp.getDouble("/geometry/bst/region/zstart",    sectorId)*0.1;
        final double layergap = cp.getDouble("/geometry/bst/region/layergap",  sectorId)*0.1;
        final double readoutPitch  = cp.getDouble("/geometry/bst/bst/readoutPitch",  0)*0.1;
        final double siliconWidth = cp.getDouble("/geometry/bst/bst/siliconWidth", 0)*0.1;
        
        double totLength = 3*activeLength + deadLen1*2 + deadLen2 + deadLen3;
        double halfPitch = readoutPitch*0.5;
        double y = radius;
        if (superlayerId%2 == 1)
            y += siliconWidth + layergap;
        
        Plane3D bPlane = new Plane3D(0, 0, totLength - deadLen1, 0, 0, -1);
        Plane3D lPlane = (superlayerId == 0)?
                new Plane3D( activeWidth*0.5, 0, 0, 1, 0, 0) :
                new Plane3D(-activeWidth*0.5, 0, 0, 1, 0, 0);
        
        Point3D p0 = new Point3D();
        Point3D p1 = new Point3D();
        Point3D p2 = new Point3D();
        Point3D p3 = new Point3D();
        Point3D p4 = new Point3D();
        Point3D p5 = new Point3D();
        Point3D p6 = new Point3D();
        Point3D p7 = new Point3D();
        Point3D pT = new Point3D();
        
        Line3D line = new Line3D();
        Vector3D dir = new Vector3D();
        
        Point3D b0 = new Point3D();
        Point3D b1 = new Point3D();
        Point3D b2 = new Point3D();
        Point3D b3 = new Point3D();
        Triangle3D triangle0 = new Triangle3D();
        Triangle3D triangle1 = new Triangle3D();

        BSTLayer layer = new BSTLayer(sectorId, superlayerId, layerId);
        
        double phi = -layerId*Math.PI*2.0/nsectors;

        for (int componentId=0; componentId<256; componentId++) {
            double angle = superlayerId==0 ? Math.toRadians((255-componentId)/85.0) : -Math.toRadians(componentId/85.0);
            dir.setXYZ(0, 0, 30);
            dir.rotateY(angle);

            double x = -(componentId-127.5)*readoutPitch;
            p0.set(x-halfPitch, 0, deadLen1);
            p1.set(x+halfPitch, 0, deadLen1);
            p2.set(x+halfPitch, siliconWidth, deadLen1);
            p3.set(x-halfPitch, siliconWidth, deadLen1);
            
            line.set(p0, dir);
            bPlane.intersection(line, p4);
            if (lPlane.intersection(line, pT)==1 && p0.distance(pT) < p0.distance(p4))
                p4.copy(pT);

            line.set(p1, dir);
            bPlane.intersection(line, p5);
            if (lPlane.intersection(line, pT)==1 && p0.distance(pT) < p0.distance(p5))
                p5.copy(pT);

            line.set(p2, dir);
            bPlane.intersection(line, p6);
            if (lPlane.intersection(line, pT)==1 && p0.distance(pT) < p0.distance(p6))
                p6.copy(pT);

            line.set(p3, dir);
            bPlane.intersection(line, p7);
            if (lPlane.intersection(line, pT)==1 && p0.distance(pT) < p0.distance(p7))
                p7.copy(pT);

            SiStrip strip = new SiStrip(componentId, p4, p5, p6, p7, p0, p1, p2, p3);
            
            strip.translateXYZ(0, y, zstart);
            strip.rotateZ(phi);

            layer.addComponent(strip);
        }

        for (int s=0; s<3; s++) {
            double dz = 0;
            switch (s) { // this switch statement does not use break statments
                case 2: dz += activeLength+deadLen3;
                case 1: dz += activeLength+deadLen2;
                case 0: dz += deadLen1; 
            }
            b0.set( activeWidth*0.5, 0, dz);
            b1.set(-activeWidth*0.5, 0, dz);
            b2.set(-activeWidth*0.5, 0, dz+activeLength);
            b3.set( activeWidth*0.5, 0, dz+activeLength);
            triangle0.set(b1, b0, b2);
            triangle1.set(b3, b2, b0);
            triangle0.translateXYZ(0, y+0.5*siliconWidth, zstart);
            triangle1.translateXYZ(0, y+0.5*siliconWidth, zstart);
            triangle0.rotateZ(-phi);
            triangle1.rotateZ(-phi);
            layer.getBoundary().addFace(triangle0);
            layer.getBoundary().addFace(triangle1);
        }
        
        layer.getPlane().set(0, 0, zstart, 0, 0, 1);
        
        return layer;
    }

    /**
     * Returns "BST Factory".
     * @return "BST Factory"
     */
    @Override
    public String getType() {
        return "BST Factory";
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
    public Transformation3D getTransformation(ConstantProvider cp, int sectorId, int superlayerId, int layerId) {
        final int nsectors    = cp.getInteger("/geometry/bst/region/nsectors", sectorId);
        
        if(!(0<=sectorId && sectorId<4))
            throw new IllegalArgumentException("Error: invalid sector="+sectorId);
        if(!(0<=superlayerId && superlayerId<2))
            throw new IllegalArgumentException("Error: invalid superlayer="+superlayerId);
        if(!(0<=layerId && layerId<nsectors))
            throw new IllegalArgumentException("Error: invalid layer="+layerId);
        
        final double deadLen1     = cp.getDouble("/geometry/bst/sector/deadZnSenLen1", 0)*0.1;
        final double deadLen2     = cp.getDouble("/geometry/bst/sector/deadZnSenLen2", 0)*0.1;
        final double deadLen3     = cp.getDouble("/geometry/bst/sector/deadZnSenLen3", 0)*0.1;
        final double activeLength = cp.getDouble("/geometry/bst/sector/activeSenLen",  0)*0.1;
        final double activeWidth  = cp.getDouble("/geometry/bst/sector/activeSenWid",  0)*0.1;
        
        final double radius   = cp.getDouble("/geometry/bst/region/radius",    sectorId)*0.1;
        final double zstart   = cp.getDouble("/geometry/bst/region/zstart",    sectorId)*0.1;
        final double layergap = cp.getDouble("/geometry/bst/region/layergap",  sectorId)*0.1;
        final double readoutPitch  = cp.getDouble("/geometry/bst/bst/readoutPitch",  0)*0.1;
        final double siliconWidth = cp.getDouble("/geometry/bst/bst/siliconWidth", 0)*0.1;
        
        double totLength = 3*activeLength + deadLen1*2 + deadLen2 + deadLen3;
        double halfPitch = readoutPitch*0.5;
        double y = radius;
        if (superlayerId%2 == 1)
            y += siliconWidth + layergap;
        
        double phi = -layerId*Math.PI*2.0/nsectors;
        Transformation3D  layerTransform = new Transformation3D();
        layerTransform.translateXYZ(0.0, y, zstart);
        layerTransform.rotateZ(-phi);
        return layerTransform;
                //.append(new TranslationXYZ(0.0,y,zstart));
    }

    @Override
    public DetectorTransformation getDetectorTransform(ConstantProvider cp) {
        DetectorTransformation detTrans = new DetectorTransformation();
        for(int sector = 0; sector<4 ; sector++){
            for(int superlayer = 0; superlayer < 2; superlayer++){
                int nsectors    = cp.getInteger("/geometry/bst/region/nsectors", sector);
                for(int layer = 0; layer < nsectors; layer++){
                    //detTrans.add(sector, superlayer, layer, 
                    //        this.getTransformation(cp, sector, superlayer, layer));
                    detTrans.add(layer, sector, superlayer, 
                            this.getTransformation(cp, sector, superlayer, layer));
                }
            }
        }
        return detTrans;
    }
}
