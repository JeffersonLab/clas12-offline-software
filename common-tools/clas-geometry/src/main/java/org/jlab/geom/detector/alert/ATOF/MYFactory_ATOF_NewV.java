/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jlab.geom.detector.alert.ATOF;



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
 *
 * @author viktoriya
 */
public class MYFactory_ATOF_NewV implements Factory <MYDetector, MYSector, MYSuperlayer, MYLayer> {
    @Override
    public MYDetector createDetectorCLAS(ConstantProvider cp) {
        return createDetectorSector(cp);
    }
    
    @Override
    public MYDetector createDetectorSector(ConstantProvider cp) {
        return createDetectorTilted(cp);
    }
    
    // From here and on, I take CND example to test how I can create scintillator bars 
    // Creating new objects: detector, sector, superlayer, layer from here
    // say how much sectors I want 
    int nsectors = 15;
    double sector_angle_rot = Math.toRadians(360.0/nsectors);
    
    @Override
    public MYDetector createDetectorTilted(ConstantProvider cp) {
        
        MYDetector detector = createDetectorLocal(cp);
        
        //Transformation3D trans = new Transformation3D();
        
        //for (MYSector sector: detector.getAllSectors())
        //{
        //    int sectorId = sector.getSectorId();
        //    trans.rotateZ(sector_angle_rot*sectorId);
        //    sector.setTransformation(trans);
        //}
        
        return detector;
    }
    
    @Override
    public MYDetector createDetectorLocal(ConstantProvider cp) {
        MYDetector detector = new MYDetector();
        for (int sectorId=0; sectorId<nsectors; sectorId++)
            detector.addSector(createSector(cp, sectorId));
        return detector;
    }

    // say how much superlayers I want in one sector
    int nsuperl = 10;
    @Override
    public MYSector createSector(ConstantProvider cp, int sectorId) {
        if(!(0<=sectorId && sectorId<nsectors))
            throw new IllegalArgumentException("Error: invalid sector="+sectorId);
        MYSector sector = new MYSector(sectorId);
        for (int superlayerId=0; superlayerId<nsuperl; superlayerId++)
            sector.addSuperlayer(createSuperlayer(cp, sectorId, superlayerId));
       
        return sector;
    }

    // say how much layers I want in one superlayer (so, in one sector)
    int nlayers = 2;
    @Override
    public MYSuperlayer createSuperlayer(ConstantProvider cp, int sectorId, int superlayerId) {
        if(!(0<=sectorId && sectorId<nsectors))
            throw new IllegalArgumentException("Error: invalid sector="+sectorId);
        if(!(0<=superlayerId && superlayerId<nsuperl))
            throw new IllegalArgumentException("Error: invalid superlayer="+superlayerId);
        MYSuperlayer superlayer = new MYSuperlayer(sectorId, superlayerId);
        for (int layerId=0; layerId<nlayers; layerId++)
            superlayer.addLayer(createLayer(cp, sectorId, superlayerId, layerId));
        return superlayer;
    }
    
    int npaddles = 4;
    
    double openAng_pad_deg = 6.0;
    double openAng_pad_rad = Math.toRadians(openAng_pad_deg);
    double openAng_sector_deg = npaddles*openAng_pad_deg;
    double openAng_sector_rad = npaddles*openAng_pad_rad;
    
    // here I create a layer, using a set of elementary objects that are called "component"; 
    // here, "component" = ScintillatorPaddle;
    @Override
    public MYLayer createLayer(ConstantProvider cp, int sectorId, int superlayerId, int layerId) {
        if(!(0<=sectorId && sectorId<nsectors))
            throw new IllegalArgumentException("Error: invalid sector="+sectorId);
        if(!(0<=superlayerId && superlayerId<nsuperl))
            throw new IllegalArgumentException("Error: invalid superlayer="+superlayerId);
        if(!(0<=layerId && layerId<nlayers))
            throw new IllegalArgumentException("Error: invalid layer="+layerId);

        double R0       = 77.0d; // InnerRadius layer 0 mm
        double R1       = 80.0d; // InnerRadius layer 1 mm
        double dR0       = 3.0d; // Thickness layer 0 mm 
        double dR1       = 20.0d;// Thickness layer 1 mm
        
        // trapezoide dimensions for a bigger paddle (external)
        double pad_b1 = 8.17369; // mm
        double pad_b2 = 10.27; // mm
        double pad_h = 20.0; // mm
        double pad_z = 30.0; // mm, length in Z of one superlayer
        
        // trapezoide dimensions for a smaller paddle (internal)
        double small_pad_b1 = 7.85924; // mm
        double small_pad_b2 = 8.17369; // mm
        double small_pad_h = 3.0; // mm
        
        double gap_pad_z       = 0.0d; // Gap between paddles in z 
        double gap_pad_xy       = 0.0d; // Gap between paddles in XY
        
        //double len      = 30.0d; // Length in Z of one superlayer, mm!
        //double widthT   = 10.27d;// Higher base of one paddle
        //double widthM   = 8.17d;// Middle base of one paddle interface between layer 0 and 1
        //double widthB   = 7.86d;// Lower base of one paddle
        //double z        = 0.0d; // Upstream Z offset
        
        MYLayer layer = new MYLayer(sectorId, superlayerId, layerId);
        
        List<Plane3D> planes = new ArrayList();
    
        // we can have components grouped in blocks, here we have 4 component per 1 "block";
        
        //int component = sectorId*4;
        double len_b = superlayerId*pad_z; // back paddle plan
        double len_f = (superlayerId+1)*pad_z; // front paddle plan
        double Rl = R0;
        double dR = dR0;
        double widthTl = small_pad_b2;
        double widthBl = small_pad_b1;
        
        //double alpha   = Math.toRadians(3);
                
        if (layerId==1) {Rl = R1;
        dR = dR1; 
        widthTl = pad_b2;
        widthBl = pad_b1;
        }
        
        for(int padId=0; padId<npaddles; padId++)
        {
            Point3D p0 = new Point3D(-dR/2,      -widthBl/2,  len_f);
            Point3D p1 = new Point3D(dR/2, -widthTl/2,  len_f);
            Point3D p2 = new Point3D(dR/2, widthTl/2, len_f);
            Point3D p3 = new Point3D(-dR/2, widthBl/2, len_f);
            
            Point3D p4 = new Point3D(-dR/2,      -widthBl/2,  len_b);
            Point3D p5 = new Point3D(dR/2, -widthTl/2,  len_b);
            Point3D p6 = new Point3D(dR/2, widthTl/2, len_b);
            Point3D p7 = new Point3D(-dR/2, widthBl/2, len_b);
            ScintillatorPaddle Paddle = new ScintillatorPaddle(sectorId*4+padId, p0, p1, p2, p3, p4, p5, p6, p7);
            
            // Rotate the paddles into one sector
            
            double sector_ang_rad = sectorId*openAng_sector_rad;
            double sector_ang_deg = sectorId*openAng_sector_deg;
            
           Paddle.rotateZ(Math.toRadians(padId*openAng_pad_deg+sectorId*openAng_sector_deg));
           
            
        // Translate the paddles up to their proper radial distance and
        // along the z-axis
            double xoffset;
            double yoffset;
            
            xoffset = (Rl+dR/2)*Math.cos(padId*openAng_pad_rad+sectorId*openAng_sector_rad);
            yoffset = (Rl+dR/2)*Math.sin(padId*openAng_pad_rad+sectorId*openAng_sector_rad);
           
            Paddle.translateXYZ(xoffset, yoffset, 0); 
            
        // Add the paddles to the list
            layer.addComponent(Paddle);
            
        }    
        
            Plane3D plane = new Plane3D(0, Rl, 0, 0, 1, 0);
            plane.rotateZ(sectorId*openAng_sector_rad - Math.toRadians(90));
            planes.add(plane);

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
