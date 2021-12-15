package org.jlab.geom.detector.fmt;

import java.util.List;
import org.jlab.geom.base.ConstantProvider;
import org.jlab.geom.base.DetectorTransformation;
import org.jlab.geom.base.Factory;
import org.jlab.geom.component.TrackerStrip;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Transformation3D;
import org.jlab.geom.prim.Triangle3D;

/**
 * A Forward Micromegas Tracker (FMT) {@link org.jlab.geom.base.Factory Factory}.
 * <p>
 * Factory: <b>{@link org.jlab.geom.detector.fmt.FMTFactory FMTFactory}</b><br> 
 * Hierarchy: 
 * <code>
 * {@link org.jlab.geom.detector.fmt.FMTDetector FMTDetector} → 
 * {@link org.jlab.geom.detector.fmt.FMTSector FMTSector} → 
 * {@link org.jlab.geom.detector.fmt.FMTSuperlayer FMTSuperlayer} → 
 * {@link org.jlab.geom.detector.fmt.FMTLayer FMTLayer} → 
 * {@link org.jlab.geom.component.ScintillatorPaddle ScintillatorPaddle}
 * </code>
 * <p>
 * Due to the way the FMT is organized, there is only one {@code FMTSector}
 * and {@code FMTSuperlayer} per FMTDetector.  There are three {@code FMTLayer}s
 * and each layer contains 48 {@code ScintillatorPaddles} grouped pairwise into
 * 24 sectors. The equation for the sector id given a component id for the FMT 
 * is as follows<br>
 * &nbsp;&nbsp;sector = ceil((component%47)/2)<br>
 * This means that components 47 and 0 are both in sector 0, components 1 and 2
 * are in sector 1, components 3 and 4 are in sector 2, etc.
 * 
 * @author devita
 */
public class FMTFactory implements Factory <FMTDetector, FMTSector, FMTSuperlayer, FMTLayer> {
    
    private int nLayers;
    private int nStrips;     // Number of strips: 1024
    private int nHalfStrips; // In the middle of the FMT, 320 strips are split in two.

    private double pitch;      // strip width 525 um
    private double interStrip; // inter strip
    private double hDrift;     // Thickness of the drift region

    private double beamHole; // Radius of the hole in the center for the beam
    private double rMax;     // Outer radius
    
    private static double[] layerZ;     // z-coordinate of the layer
    private static double[] layerAngle; // rotation angles

    private static Point3D[] offset;
    private static Point3D[] rotation;

    @Override
    public FMTDetector createDetectorCLAS(ConstantProvider cp) {
                
        hDrift      = cp.getDouble("/geometry/fmt/fmt_global/hDrift", 0) / 10.;
        pitch       = cp.getDouble("/geometry/fmt/fmt_global/Pitch", 0) / 10.;
        interStrip  = cp.getDouble("/geometry/fmt/fmt_global/Interstrip", 0) / 10.;
        beamHole    = cp.getDouble("/geometry/fmt/fmt_global/R_min", 0) / 10.;
        nStrips     = cp.getInteger("/geometry/fmt/fmt_global/N_strip", 0);
        nHalfStrips = cp.getInteger("/geometry/fmt/fmt_global/N_halfstr", 0);
        int nSideStrips = (nStrips - 2*nHalfStrips)/2;          // 192
        rMax        = pitch * (nHalfStrips + 2*nSideStrips)/2.; // 184.8 mm

        // Position and strip orientation of each disks
        // ===============
        nLayers = cp.length("/geometry/fmt/fmt_layer_noshim/Z");
        layerZ     = new double[nLayers];
        layerAngle = new double[nLayers];
        for (int i = 0; i < cp.length("/geometry/fmt/fmt_layer_noshim/Z"); i++) {
            layerZ[i]     = cp.getDouble("/geometry/fmt/fmt_layer_noshim/Z", i) / 10.;
            layerAngle[i] = Math.toRadians(cp.getDouble("/geometry/fmt/fmt_layer_noshim/Angle", i));
        }

        // Layer alignment constants
        // ===============
        offset   = new Point3D[nLayers];
        rotation = new Point3D[nLayers];
        for (int i = 0; i < nLayers; i++) {
            double deltaX = cp.getDouble("/geometry/fmt/alignment/deltaX", i) / 10.;
            double deltaY = cp.getDouble("/geometry/fmt/alignment/deltaY", i) / 10.;
            double deltaZ = cp.getDouble("/geometry/fmt/alignment/deltaZ", i) / 10.;
            double rotX   = Math.toRadians(cp.getDouble("/geometry/fmt/alignment/rotX", i));
            double rotY   = Math.toRadians(cp.getDouble("/geometry/fmt/alignment/rotY", i));
            double rotZ   = Math.toRadians(cp.getDouble("/geometry/fmt/alignment/rotZ", i));
            layerZ[i]     += deltaZ + hDrift/2;
            layerAngle[i] += rotZ;
            offset[i]   = new Point3D(deltaX, deltaY, layerZ[i]);
            rotation[i] = new Point3D(rotX, rotY, layerAngle[i]);
        }        
        
        return createDetectorSector(cp);
    }
    
    @Override
    public FMTDetector createDetectorSector(ConstantProvider cp) {
        return createDetectorTilted(cp);
    }
    
    @Override
    public FMTDetector createDetectorTilted(ConstantProvider cp) {
        return createDetectorLocal(cp);
    }
            
    @Override
    public FMTDetector createDetectorLocal(ConstantProvider cp) {
        FMTDetector detector = new FMTDetector();
        for (int sectorId=0; sectorId<1; sectorId++)
            detector.addSector(createSector(cp, sectorId));
        return detector;
    }

    @Override
    public FMTSector createSector(ConstantProvider cp, int sectorId) {
        if(!(0<=sectorId && sectorId<1))
            throw new IllegalArgumentException("Error: invalid sector="+sectorId);
        FMTSector sector = new FMTSector(sectorId);
        for (int superlayerId=0; superlayerId<1; superlayerId++)
            sector.addSuperlayer(createSuperlayer(cp, sectorId, superlayerId));
        return sector;
    }

    @Override
    public FMTSuperlayer createSuperlayer(ConstantProvider cp, int sectorId, int superlayerId) {
        
        if(!(0<=sectorId && sectorId<1))
            throw new IllegalArgumentException("Error: invalid sector="+sectorId);
        if(!(0<=superlayerId && superlayerId<1))
            throw new IllegalArgumentException("Error: invalid superlayer="+superlayerId);
        FMTSuperlayer superlayer = new FMTSuperlayer(sectorId, superlayerId);
        for (int layerId=0; layerId<nLayers; layerId++)
            superlayer.addLayer(createLayer(cp, sectorId, superlayerId, layerId));
        return superlayer;
    }
    
    @Override
    public FMTLayer createLayer(ConstantProvider cp, int sectorId, int superlayerId, int layerId) {
        if(!(0<=sectorId && sectorId<1))
            throw new IllegalArgumentException("Error: invalid sector="+sectorId);
        if(!(0<=superlayerId && superlayerId<1))
            throw new IllegalArgumentException("Error: invalid superlayer="+superlayerId);
        if(!(0<=layerId && layerId<nLayers))
            throw new IllegalArgumentException("Error: invalid layer="+layerId);
        
        FMTLayer layer = new FMTLayer(sectorId, superlayerId, layerId);
        for(int i=0; i<nStrips; i++) {
            // define strips as rectangles
            Line3D line    = getStripLine(i);
            Point3D origin = line.origin();
            Point3D end    = line.end();
            TrackerStrip strip = new TrackerStrip(i,origin,end,pitch,hDrift);
            layer.addComponent(strip);            
        }
        
        List<TrackerStrip> strip = layer.getAllComponents();
        Point3D pLL = strip.get(0).getVolumePoint(0);
        Point3D pLR = strip.get(0).getVolumePoint(4);
        Point3D pUL = strip.get(nStrips-1).getVolumePoint(1);
        Point3D pUR = strip.get(nStrips-1).getVolumePoint(5);
        Point3D pBL = strip.get(nStrips-1).getVolumePoint(0);
        Point3D pBR = strip.get(nStrips-1).getVolumePoint(4);
        
        pUL = new Point3D(pUL.x(), pUL.y() + pitch*(pBL.y()-pLL.y())/(pBL.x()-pLL.x()) , pUL.z());
        pUR = new Point3D(pUR.x(), pUR.y() + pitch*(pBR.y()-pLR.y())/(pBR.x()-pLR.x()) , pUR.z());
        layer.getBoundary().addFace(new Triangle3D(pLL, pLR, pUL));
        layer.getBoundary().addFace(new Triangle3D(pUR, pUL, pLR));
                
        layer.getPlane().set(0, 0, 0, 0, 0, -1);
        layer.setRmin(beamHole);
        layer.setRmax(rMax);
        
        Transformation3D transform = new Transformation3D();
        transform.rotateY(Math.toRadians(180));
        transform.rotateZ(rotation[layerId].z());
        transform.rotateY(rotation[layerId].y());
        transform.rotateX(rotation[layerId].x());
        transform.translateXYZ(offset[layerId].x(), offset[layerId].y(), offset[layerId].z());
        layer.setTransformation(transform);
                
        return layer;
    }

    /**
     * Returns "FMT Factory".
     * @return "FMT Factory"
     */
    @Override
    public String getType() {
        return "FMT Factory";
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

    private Line3D getStripLine(int i) {

        // Give the Y of the middle of the strip
        double x1=0;
        double x2=0;
        double y=0;
        if (i < 512){
            y =  -rMax + (511-i+0.5)*pitch;
        } else {
            y = rMax - (1023-i+0.5)*pitch;
        }

        int localRegion = getLocalRegion(i);
        switch(localRegion) {
        case 1:
            x1 = -getLayerXEdge(rMax,y);
            x2 =  0;
            if(Math.abs(y)/beamHole<1) {
                x2 = -getLayerXEdge(beamHole,y);
            }
            break;
        case 3:
            x1 = 0;
            x2 = getLayerXEdge(rMax,y);
            if(Math.abs(y)/beamHole<1) {
                x1 = getLayerXEdge(beamHole,y);
            }
            break;
        default:
            x1 = -getLayerXEdge(rMax,y);
            x2 =  getLayerXEdge(rMax,y);
            break;
        }
//            stripLocal[i]  = new Line3D(x1,y,0,x2,y,0);    
//            for (int j = 0; j < nLayers; j++) { // x sign flipgit s
//                stripGlobal[j][i] = new Line3D(stripLocal[i]);
//                stripGlobal[j][i].rotateY(Math.toRadians(180));
//                stripGlobal[j][i].rotateZ(rotation[j].z());
//                stripGlobal[j][i].rotateY(rotation[j].y());
//                stripGlobal[j][i].rotateX(rotation[j].x());
//                stripGlobal[j][i].translateXYZ(offset[j].x(), offset[j].y(), offset[j].z());
//            }
        return new Line3D(x1,y,0,x2,y,0);  
    }

    private static double getLayerXEdge(double radius, double y) {
        return radius*Math.sin(Math.acos(Math.abs(y)/radius));
    }
    
    private static int getLocalRegion(int i) {
            // To represent the geometry we divide the barrel micromega disk into 4 regions according to the strip numbering system.
            // Here i = strip_number -1;
            // Region 1 is the region in the negative x part of inner region: the strips range is from   1 to 320  (   0 <= i < 320)
            // Region 2 is the region in the negative y part of outer region: the strips range is from 321 to 512  ( 320 <= i < 512)
            // Region 3 is the region in the positive x part of inner region: the strips range is from 513 to 832  ( 512 <= i < 832)
            // Region 4 is the region in the positive y part of outer region: the strips range is from 833 to 1024 ( 832 <= i < 1024)
            if (i>=  0 && i< 320) return 1;
            if (i>=320 && i< 512) return 2;
            if (i>=512 && i< 832) return 3;
            if (i>=832 && i<1024) return 4;
            return 0;
    }

}
