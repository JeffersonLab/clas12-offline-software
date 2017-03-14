package org.jlab.geom.detector.ft;

import org.jlab.geom.base.ConstantProvider;
import org.jlab.geom.base.DetectorTransformation;
import org.jlab.geom.base.Factory;
import org.jlab.geom.component.ScintillatorPaddle;
import org.jlab.geom.prim.Triangle3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Transformation3D;

/**
 * A Forward Tagger Calorimeter (FTCAL) {@link org.jlab.geom.base.Factory Factory}.
 * <p>
 * Factory: <b>{@link org.jlab.geom.detector.ft.FTCALFactory FTCALFactory}</b><br> 
 * Hierarchy: 
 * <code>
 * {@link org.jlab.geom.detector.ft.FTCALDetector FTCALDetector} → 
 * {@link org.jlab.geom.detector.ft.FTCALSector FTCALSector} → 
 * {@link org.jlab.geom.detector.ft.FTCALSuperlayer FTCALSuperlayer} → 
 * {@link org.jlab.geom.detector.ft.FTCALLayer FTCALLayer} → 
 * {@link org.jlab.geom.component.ScintillatorPaddle ScintillatorPaddle}
 * </code>
 * 
 * @author jnhankins
 */
public class FTCALFactory implements Factory <FTCALDetector, FTCALSector, FTCALSuperlayer, FTCALLayer> {
    // Detectors are arranged in a 22x22 grid. In the following array a 1
    // denotes the presence of a detector while a 0 denotes an absence.
    private static final char[][] geom = new char[][] {
    //  11 10  9  8  7  6  5  4  3  2  1 -1 -2 -3 -4 -5 -6 -7 -8 -9-10-11
        {0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0}, // 11 00
        {0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0}, // 10 01
        {0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0}, //  9 02
        {0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0}, //  8 03
        {0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0}, //  7 04
        {0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0}, //  6 05
        {0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0}, //  5 06
        {0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0}, //  4 07
        {1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1}, //  3 08
        {1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1}, //  2 09
        {1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1}, //  1 10
        {1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1}, // -1 11
        {1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1}, // -2 12
        {1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1}, // -3 13
        {0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0}, // -4 14
        {0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0}, // -5 15
        {0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0}, // -6 16
        {0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0}, // -7 17
        {0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0}, // -8 18
        {0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0}, // -9 19
        {0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0}, //-10 20
        {0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0}, //-11 21
    };
    
    
    @Override
    public FTCALDetector createDetectorCLAS(ConstantProvider cp) {
        return createDetectorSector(cp);
    }
    
    @Override
    public FTCALDetector createDetectorSector(ConstantProvider cp) {
        return createDetectorTilted(cp);
    }
    
    @Override
    public FTCALDetector createDetectorTilted(ConstantProvider cp) {
        FTCALDetector detector = createDetectorLocal(cp);
        for (FTCALSector sector: detector.getAllSectors()) {
            for (FTCALSuperlayer superlayer: sector.getAllSuperlayers()) {
                double Cfront  = cp.getDouble("/geometry/ft/ftcal/Cfront", 0)*0.1;
                Transformation3D trans = new Transformation3D();
                trans.translateXYZ(0, 0, Cfront);
                superlayer.setTransformation(trans);
            }
        }
        return detector;
    }
            
    @Override
    public FTCALDetector createDetectorLocal(ConstantProvider cp) {
        FTCALDetector detector = new FTCALDetector();
        for (int sectorId=0; sectorId<1; sectorId++)
            detector.addSector(createSector(cp, sectorId));
        return detector;
    }

    @Override
    public FTCALSector createSector(ConstantProvider cp, int sectorId) {
        if(!(0<=sectorId && sectorId<1))
            throw new IllegalArgumentException("Error: invalid sector="+sectorId);
        FTCALSector sector = new FTCALSector(sectorId);
        for (int superlayerId=0; superlayerId<1; superlayerId++)
            sector.addSuperlayer(createSuperlayer(cp, sectorId, superlayerId));
        return sector;
    }
    
    @Override
    public FTCALSuperlayer createSuperlayer(ConstantProvider cp, int sectorId, int superlayerId) {
        if(!(0<=sectorId && sectorId<1))
            throw new IllegalArgumentException("Error: invalid sector="+sectorId);
        if(!(0<=superlayerId && superlayerId<1))
            throw new IllegalArgumentException("Error: invalid superlayer="+superlayerId);
        FTCALSuperlayer superlayer = new FTCALSuperlayer(sectorId, superlayerId);
        for (int layerId=0; layerId<1; layerId++)
            superlayer.addLayer(createLayer(cp, sectorId, superlayerId, layerId));
        return superlayer;
    }
    
    @Override
    public FTCALLayer createLayer(ConstantProvider cp, int sectorId, int superlayerId, int layerId) {
        if(!(0<=sectorId && sectorId<1))
            throw new IllegalArgumentException("Error: invalid sector="+sectorId);
        if(!(0<=superlayerId && superlayerId<1))
            throw new IllegalArgumentException("Error: invalid superlayer="+superlayerId);
        if(!(0<=layerId && layerId<1))
            throw new IllegalArgumentException("Error: invalid layer="+layerId);
        
        double Clength = cp.getDouble("/geometry/ft/ftcal/Clength", 0)*0.1;
        double Cwidth  = cp.getDouble("/geometry/ft/ftcal/Cwidth", 0)*0.1;
        double VM2000  = cp.getDouble("/geometry/ft/ftcal/VM2000", 0)*0.1;
        double Agap    = cp.getDouble("/geometry/ft/ftcal/Agap", 0)*0.1;
        double Vwidth  = Cwidth + VM2000 + Agap;
                
        FTCALLayer layer = new FTCALLayer(sectorId, superlayerId, layerId);
        
        double xmin = Double.POSITIVE_INFINITY;
        double xmax = Double.NEGATIVE_INFINITY;
        double ymin = Double.POSITIVE_INFINITY;
        double ymax = Double.NEGATIVE_INFINITY;
        for (int row=0; row<22; row++) {
            for (int col=0; col<22; col++) {
                if (geom[row][col] == 1) {
                    int componentId = row*22+col;
                    int idX = col<11 ? 11-col : 10-col;
                    int idY = row<11 ? 11-row : 10-row;                    
                    double x = Vwidth*(Math.abs(idX)-0.5)*Math.signum(idX);
                    double y = Vwidth*(Math.abs(idY)-0.5)*Math.signum(idY);
                    
                    ScintillatorPaddle paddle = new ScintillatorPaddle(componentId, Cwidth, Clength, Cwidth);
                    paddle.rotateX(Math.toRadians(90));
                    paddle.rotateZ(Math.toRadians(180));
                    paddle.translateXYZ(x, y, Clength/2);
                    
                    layer.addComponent(paddle);
                    
                    xmin = Math.min(xmin, x-Cwidth/2);
                    xmax = Math.max(xmax, x+Cwidth/2);
                    ymin = Math.min(ymin, y-Cwidth/2);
                    ymax = Math.max(ymax, y+Cwidth/2);
                }
            }
        }
        
        layer.getPlane().set(0, 0, 0, 0, 0, 1);
        
        Point3D p0 = new Point3D(xmin, ymin, 0);
        Point3D p1 = new Point3D(xmax, ymin, 0);
        Point3D p2 = new Point3D(xmax, ymax, 0);
        Point3D p3 = new Point3D(xmin, ymax, 0);
        layer.getBoundary().addFace(new Triangle3D(p0, p3, p1));
        layer.getBoundary().addFace(new Triangle3D(p2, p1, p3));
        
        return layer;
    }

    /**
     * Returns "FTCAL Factory".
     * @return "FTCAL Factory"
     */
    @Override
    public String getType() {
        return "FTCAL Factory";
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