package org.jlab.geom.detector.cnd;

import java.util.ArrayList;
import java.util.List;
import org.jlab.geom.base.ConstantProvider;
import org.jlab.geom.base.DetectorTransformation;
import org.jlab.geom.base.Factory;
import org.jlab.geom.component.ScintillatorPaddle;
import org.jlab.geom.prim.Plane3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Shape3D;
import org.jlab.geom.prim.Transformation3D;
import org.jlab.geom.prim.Triangle3D;

/**
 * A Central Neutron Detector (CND) {@link org.jlab.geom.base.Factory Factory}.
 * <p>
 * Factory: <b>{@link org.jlab.geom.detector.cnd.CNDFactory CNDFactory}</b><br> 
 * Hierarchy: 
 * <code>
 * {@link org.jlab.geom.detector.cnd.CNDDetector CNDDetector} → 
 * {@link org.jlab.geom.detector.cnd.CNDSector CNDSector} → 
 * {@link org.jlab.geom.detector.cnd.CNDSuperlayer CNDSuperlayer} → 
 * {@link org.jlab.geom.detector.cnd.CNDLayer CNDLayer} → 
 * {@link org.jlab.geom.component.ScintillatorPaddle ScintillatorPaddle}
 * </code>
 * <p>
 * Due to the way the CND is organized, there is only one {@code CNDSector}
 * and {@code CNDSuperlayer} per CNDDetector.  There are three {@code CNDLayer}s
 * and each layer contains 48 {@code ScintillatorPaddles} grouped pairwise into
 * 24 sectors. The equation for the sector id given a component id for the CND 
 * is as follows<br>
 * &nbsp;&nbsp;sector = ceil((component%47)/2)<br>
 * This means that components 47 and 0 are both in sector 0, components 1 and 2
 * are in sector 1, components 3 and 4 are in sector 2, etc.
 * 
 * @author jnhankins
 */
public class CNDFactory implements Factory <CNDDetector, CNDSector, CNDSuperlayer, CNDLayer> {
    @Override
    public CNDDetector createDetectorCLAS(ConstantProvider cp) {
        return createDetectorSector(cp);
    }
    
    @Override
    public CNDDetector createDetectorSector(ConstantProvider cp) {
        return createDetectorTilted(cp);
    }
    
    @Override
    public CNDDetector createDetectorTilted(ConstantProvider cp) {
        return createDetectorLocal(cp);
    }
            
    @Override
    public CNDDetector createDetectorLocal(ConstantProvider cp) {
        CNDDetector detector = new CNDDetector();
        for (int sectorId=0; sectorId<1; sectorId++)
            detector.addSector(createSector(cp, sectorId));
        return detector;
    }

    @Override
    public CNDSector createSector(ConstantProvider cp, int sectorId) {
        if(!(0<=sectorId && sectorId<1))
            throw new IllegalArgumentException("Error: invalid sector="+sectorId);
        CNDSector sector = new CNDSector(sectorId);
        for (int superlayerId=0; superlayerId<1; superlayerId++)
            sector.addSuperlayer(createSuperlayer(cp, sectorId, superlayerId));
        return sector;
    }

    @Override
    public CNDSuperlayer createSuperlayer(ConstantProvider cp, int sectorId, int superlayerId) {
        if(!(0<=sectorId && sectorId<1))
            throw new IllegalArgumentException("Error: invalid sector="+sectorId);
        if(!(0<=superlayerId && superlayerId<1))
            throw new IllegalArgumentException("Error: invalid superlayer="+superlayerId);
        CNDSuperlayer superlayer = new CNDSuperlayer(sectorId, superlayerId);
        for (int layerId=0; layerId<3; layerId++)
            superlayer.addLayer(createLayer(cp, sectorId, superlayerId, layerId));
        return superlayer;
    }
    
    @Override
    public CNDLayer createLayer(ConstantProvider cp, int sectorId, int superlayerId, int layerId) {
        if(!(0<=sectorId && sectorId<1))
            throw new IllegalArgumentException("Error: invalid sector="+sectorId);
        if(!(0<=superlayerId && superlayerId<1))
            throw new IllegalArgumentException("Error: invalid superlayer="+superlayerId);
        if(!(0<=layerId && layerId<3))
            throw new IllegalArgumentException("Error: invalid layer="+layerId);
        
        double R0       = cp.getDouble("/geometry/cnd/cndgeom/InnerRadius",     layerId); 
        double dR       = cp.getDouble("/geometry/cnd/cndgeom/Thickness",       layerId);
        double gp       = cp.getDouble("/geometry/cnd/cndgeom/LateralGap",      layerId);
        double gl       = cp.getDouble("/geometry/cnd/cndgeom/AzimuthalGap",    layerId);
        double phi      = Math.toRadians(cp.getDouble("/geometry/cnd/cndgeom/OpenAngle", layerId));
        double len      = cp.getDouble("/geometry/cnd/cndgeom/Length",          layerId);
        double widthT   = cp.getDouble("/geometry/cnd/cndgeom/HigherBase",      layerId);
        double widthB   = cp.getDouble("/geometry/cnd/cndgeom/LowerBase",       layerId);
        double z        = cp.getDouble("/geometry/cnd/cndgeom/UpstreamZOffset", layerId);

        CNDLayer layer = new CNDLayer(sectorId, superlayerId, layerId);
        
        List<Plane3D> planes = new ArrayList();
        
        // Start the component # at 47 since the left paddle of the 0th 
        // block is component #47, then wrap back to 0 as soon as the left
        // paddle of the 0th block is added.
        int component = 47;
        for (int block=0; block<24; block++) {
            // Left Paddle, with right edge at x = 0
            Point3D p0L = new Point3D(0,      0,  len);
            Point3D p1L = new Point3D(widthB, 0,  len);
            Point3D p2L = new Point3D(widthT, dR, len);
            Point3D p3L = new Point3D(0,      dR, len);
            Point3D p4L = new Point3D(0,      0,  0);
            Point3D p5L = new Point3D(widthB, 0,  0);
            Point3D p6L = new Point3D(widthT, dR, 0);
            Point3D p7L = new Point3D(0,      dR, 0);
            ScintillatorPaddle lPaddle = new ScintillatorPaddle(component, p0L, p1L, p2L, p3L, p4L, p5L, p6L, p7L);
            component = block==0? 0 : component+1;
            // Right Paddle, with left edge at x = 0
            Point3D p0R = new Point3D(-widthB, 0,  len);
            Point3D p1R = new Point3D(0,       0,  len);
            Point3D p2R = new Point3D(0,       dR, len);
            Point3D p3R = new Point3D(-widthT, dR, len);
            Point3D p4R = new Point3D(-widthB, 0,  0);
            Point3D p5R = new Point3D(0,       0,  0);
            Point3D p6R = new Point3D(0,       dR, 0);
            Point3D p7R = new Point3D(-widthT, dR, 0);
            ScintillatorPaddle rPaddle = new ScintillatorPaddle(component, p0R, p1R, p2R, p3R, p4R, p5R, p6R, p7R);
            component = component+1;
            
            // Move the paddles into position relative to eachother
            lPaddle.translateXYZ( gp/2, 0, 0);
            rPaddle.translateXYZ(-gp/2, 0, 0);
            // Translate the paddles up to their proper radial distance and
            // along the z-axis
            double r = R0 + (dR + gl) * layerId;
            lPaddle.translateXYZ(0, r, -z);
            rPaddle.translateXYZ(0, r, -z);
            // Rotate the paddles into their final position
            double theta = block*phi - Math.toRadians(90);
            lPaddle.rotateZ(theta);
            rPaddle.rotateZ(theta);
            
            // Add the paddles to the list
            layer.addComponent(lPaddle);
            layer.addComponent(rPaddle);
            
            Plane3D plane = new Plane3D(0, r, 0, 0, 1, 0);
            plane.rotateZ(theta);
            planes.add(plane);
        }
        
        Plane3D fPlane = new Plane3D(0, 0, -z,     0, 0, 1);
        Plane3D bPlane = new Plane3D(0, 0, -z+len, 0, 0, 1);
        Shape3D boundary = layer.getBoundary();
        for (int block=0; block<24; block++) {
            Plane3D plane0 = planes.get(block);
            Plane3D plane1 = planes.get((block+1)%24);
            Plane3D plane2 = planes.get((block+2)%24);
            Point3D p0 = new Point3D();
            Point3D p1 = new Point3D();
            Point3D p2 = new Point3D();
            Point3D p3 = new Point3D();
            fPlane.intersection(plane0, plane1, p0);
            bPlane.intersection(plane0, plane1, p1);
            fPlane.intersection(plane1, plane2, p2);
            bPlane.intersection(plane1, plane2, p3);
            Triangle3D f1 = new Triangle3D(p0, p1, p2);
            Triangle3D f2 = new Triangle3D(p3, p2, p1);
            boundary.addFace(f1);
            boundary.addFace(f2);
        }
        
        layer.getPlane().copy(fPlane);
        
        return layer;
    }

    /**
     * Returns "CND Factory".
     * @return "CND Factory"
     */
    @Override
    public String getType() {
        return "CND Factory";
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
