package org.jlab.rec.cvt.bmt;

import javax.swing.JFrame;
import org.jlab.detector.calib.utils.DatabaseConstantProvider;
import org.jlab.geom.prim.Arc3D;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.groot.data.H2F;
import org.jlab.groot.graphics.EmbeddedCanvas;
import org.jlab.groot.group.DataGroup;
import static org.jlab.rec.cvt.bmt.Constants.E_DRIFT_FF;
import static org.jlab.rec.cvt.bmt.Constants.E_DRIFT_MF;
import static org.jlab.rec.cvt.bmt.Lorentz.getLorentzAngle;

/**
 *
 * @author devita
 */
public class BMTGeometry {

    private final static int[] lZ = { 2, 3, 5};
    private final static int[] lC = { 1, 4, 6}; 
    private final static double accuracy =  1E-4; // mm
    private final static double udf      = -9999; // mm
        
    /**
     * Handles BMT geometry
     */
    public BMTGeometry() {
    }
    
    /**
     * Return layer number for a given region and detector type
     * @param region (1-3)
     * @param detector (C or Z)
     * @return layer (1-6) 
     */
    public int getLayer(int region, BMTType detector) {
    	int layer = -1;
        if(region>=1 && region<=3) {
            if( detector == BMTType.Z ) {
                    layer = lZ[ region - 1 ];
            }
            else if( detector == BMTType.C ) {
                    layer = lC[ region - 1 ];
            }
        }
        else System.out.println("ERROR: out of range region number in getLayer(int region, BMTType detector)");
    	return layer;
    }

    /**
     * Return region number for a given layer
     * @param layer (1-6)
     * @return region (1-3) 
     */
    public int getRegion(int layer) {
    	int region = -1;
        if(layer>=1 && layer<=6) {
            region = (int) Math.floor((layer+1)/2);
        }
        else System.out.println("ERROR: out of range layer number in getRegion(int layer)");
    	return region;
    }


    /**
     * Return region number for a given layer
     * @param layer (1-6)
     * @return type ("C" or "Z");
     */
    public static BMTType getDetectorType(int layer) {
    	if(layer == lC[0] || layer == lC[1] || layer == lC[2]) return BMTType.C;
        else if(layer == lZ[0] || layer == lZ[1] || layer == lZ[2]) return BMTType.Z;
        else {
            System.out.println("ERROR: out of range layer number in getDetectorType(int layer)");
            return null;
        }
    }
    
    
    /**
     * Return radius of the selected layer
     * @param layer (1-6)
     * @return radius (=0 if layer is out of range)
     */
    public double getRadius(int layer) {
        
        int region = this.getRegion(layer);
        BMTType det = this.getDetectorType(layer);
        
        double radius = 0;
        if(region>0 && det!=null) {
            if     (det == BMTType.C) radius = Constants.getCRCRADIUS()[region-1];
            else if(det == BMTType.Z) radius = Constants.getCRZRADIUS()[region-1];
        }
        else System.out.println("ERROR: out of range layer number in getRadius(int layer)");
        return radius;
    }
    
    /**
     * Return number of strips of the selected layer
     * @param layer (1-6)
     * @return nstrips (=0 if layer is out of range)
     */
    public int getNStrips(int layer) {
        
        int region = this.getRegion(layer);
        BMTType det = this.getDetectorType(layer);
        
        int nstrips = 0;
        if(region>0 && det!=null) {
            if     (det == BMTType.C) nstrips = Constants.getCRCNSTRIPS()[region-1];
            else if(det == BMTType.Z) nstrips = Constants.getCRZNSTRIPS()[region-1];
        }
        else System.out.println("ERROR: out of range layer number in getNStrips(int layer)");
        return nstrips;
    }
    
    /**
     * Return pitch for the selected layer and strip
     * @param layer (1-6)
     * @param strip
     * @return pitch (=0 if layer is out of range)
     */
    public double getPitch(int layer, int strip) {
        
        int region = this.getRegion(layer);
        BMTType det = this.getDetectorType(layer);
        
        double pitch = 0;
        if(region>0 && det!=null) {
            if     (det == BMTType.C) pitch = this.getCPitch(region, strip);
            else if(det == BMTType.Z) pitch = this.getZPitch(region, strip);
        }
        else System.out.println("ERROR: out of range layer number in getNStrips(int layer)");
        return pitch;
    }
    
    /**
     * Return pitch for C strips
     * @param region (1-3)
     * @param strip
     * @return pitch (0 if region or strip are out of range
     */
    public double getCPitch(int region, int strip) {
        double pitch=0;
        
        if(region>=1 && region<=3) {
            if(strip>0 && strip<=Constants.getCRCNSTRIPS()[region-1]) {
                int group = this.getCGroup(region, strip);
                pitch = Constants.getCRCWIDTH()[region-1][group-1];
            }
            else System.out.println("ERROR: out of range strip number in getZPitch(int region, int strip)");
        }
        else System.out.println("ERROR: out of range region number in getZPitch(int region, int strip)");
        return pitch;        
    }
    
    /**
     * Return pitch for Z strips
     * @param region (1-3)
     * @param strip
     * @return pitch (0 if region or strip are out of range
     */
    public double getZPitch(int region, int strip) {
        double pitch=0;
        
        if(region>=1 && region<=3) {
            if(strip>0 && strip<=Constants.getCRZNSTRIPS()[region-1]) {
                pitch = Constants.getCRZWIDTH()[region-1];
            }
            else System.out.println("ERROR: out of range strip number in getZPitch(int region, int strip)");
        }
        else System.out.println("ERROR: out of range region number in getZPitch(int region, int strip)");
        return pitch;        
    }
    
    /**
     * Return minimum z of the selected layer in the local frame
     * @param layer (1-6)
     * @return z (=udf if layer is out of range)
     */
    public double getZmin(int layer) {
        
        double z = udf;
        
        int region = this.getRegion(layer);
        BMTType det = this.getDetectorType(layer);
        
        if(region>0 && det!=null) {
            if     (det == BMTType.C) z = Constants.getCRCZMIN()[region-1];
            else if(det == BMTType.Z) z = Constants.getCRZZMIN()[region-1];
        }
        else System.out.println("ERROR: out of range layer number in getZmin(int layer)");
        return z;
    }
    
    /**
     * Return maximum z of the selected layer in the local frame
     * @param layer (1-6)
     * @return z (=udf if layer is out of range)
     */
    public double getZmax(int layer) {
        
        double z = udf;
        
        int region = this.getRegion(layer);
        BMTType det = this.getDetectorType(layer);
        
        if(region>0 && det!=null) {
            if     (det == BMTType.C) z = Constants.getCRCZMAX()[region-1];
            else if(det == BMTType.Z) z = Constants.getCRZZMAX()[region-1];
        }
        else System.out.println("ERROR: out of range layer number in getZmax(int layer)");
        return z;
    }
    
    /**
     * Return tile phi center
     * @param layer (1-6)
     * @param sector (1-3)
     * @return phi (=udf if layer is out of range)
     */
    public double getPhi(int layer, int sector) {
        
        double phi = udf;
        
        int region = this.getRegion(layer);
        BMTType det = this.getDetectorType(layer);
        
        if(region>0 && sector>0 && det!=null) {
            if     (det == BMTType.C) phi = Constants.getCRCPHI()[region-1][sector-1];
            else if(det == BMTType.Z) phi = Constants.getCRZPHI()[region-1][sector-1];
        }
        else System.out.println("ERROR: out of range inputs in getDPhi(int layer, int sector)"+" layer "+layer+" sector "+sector);
        return phi;
    }
 
    /**
     * Return half tile phi coverage
     * @param layer (1-6)
     * @param sector (1-3)
     * @return dz (=udf if layer is out of range)
     */
    public double getDPhi(int layer, int sector) {
        
        double dphi = udf;
        
        int region = this.getRegion(layer);
        BMTType det = this.getDetectorType(layer);
        
        if(region>0 && sector>0 && det!=null) {
            if     (det == BMTType.C) dphi = Constants.getCRCDPHI()[region-1][sector-1];
            else if(det == BMTType.Z) dphi = Constants.getCRZDPHI()[region-1][sector-1];
        }
        else System.out.println("ERROR: out of range inputs in getDPhi(int layer, int sector)");
        return dphi;
    }
    
    /**
     * Return thickness of the drift gap 
     * @return thickness 
     */
    public double getThickness() {
        
        return Constants.hDrift;
    }
    
    /**
     * Return offset of the selected tile, identified by layer and sector
     * @param layer (1-6)
     * @param sector (1-3)
     * @return Point3D offset: 3D offset
     */
    public Point3D getOffset(int layer, int sector) {
        
        Point3D offset = new Point3D();
        offset.copy(Constants.shifts[layer-1][sector-1]);
//        offset.translateXYZ(0, 0, org.jlab.rec.cvt.Constants.getZoffset());
        
        return offset;
    }
    
    /**
     * Return rotations for the selected tile, identified by layer and sector
     * @param layer (1-6)
     * @param sector (1-3)
     * @return Point3D offset: 3D offset
     */
    public Vector3D getRotation(int layer, int sector) {
        
        return Constants.rotations[layer-1][sector-1];
    }
    
    /**
     * Return axis for the selected tile, identified by layer and sector
     * @param layer (1-6)
     * @param sector (1-3)
     * @return Point3D offset: 3D offset
     */
    public Line3D getAxis(int layer, int sector) {
        
        return Constants.axes[layer-1][sector-1];
    }
    
    
    /**
     * Returns Line3D for Z detector strip identified from region, sector, strip numbers, for ideal geometry
     * @param region
     * @param sector
     * @param strip
     * @return Line3D
     */
    public Line3D getIdealZstrip(int region, int sector, int strip) {
        
        double radius = Constants.getCRZRADIUS()[region-1];
        double zmin   = Constants.getCRCZMIN()[region-1];
        double zmax   = Constants.getCRCZMAX()[region-1];
        double angle  = Constants.getCRZPHI()[region-1][sector-1] - Constants.getCRZDPHI()[region-1][sector-1] 
                      + ((double) strip-0.5) * Constants.getCRZWIDTH()[region-1] / Constants.getCRZRADIUS()[region-1];
        
        Point3D p1= new Point3D(radius, 0, zmin);
        p1.rotateZ(angle);
        Point3D p2= new Point3D(radius, 0, zmax);
        p2.rotateZ(angle);
                
        Line3D stripline = new Line3D(p1,p2);
        
        return stripline;
    }
    
    /**
     * Returns Line3D for Z detector strip identified from region, sector, strip numbers, for real geometry
     * @param region
     * @param sector
     * @param strip
     * @return stripline
     */
    public Line3D getZstrip(int region, int sector, int strip) {
        
        int layer = this.getLayer(region, BMTType.Z);
        Line3D stripline = this.getIdealZstrip(region, sector, strip);
        
        Point3D offset = this.getOffset(layer, sector);
        Vector3D rotation = this.getRotation(layer, sector);
        stripline.rotateX(rotation.x());
        stripline.rotateY(rotation.y());
        stripline.rotateZ(rotation.z());
        stripline.translateXYZ(offset.x(),offset.y(),offset.z());
                
        
        return stripline;
    }
    
    /**
     * Return the C detector strip group
     * @param region [1-3]
     * @param strip
     * @return group [1-...]
     */
    private int getCGroup(int region, int strip) {
        int group = 0;
        if(strip>0 && strip<=Constants.getCRCNSTRIPS()[region-1]) {
            for(int i=0; i<Constants.getCRCGRPNMAX()[region-1].length; i++) {
                if(strip<=Constants.getCRCGRPNMAX()[region-1][i]) {
                    group=i+1;
                    break;
                }       
            }
        }
        return group;
    }
    
    /**
     * Return the C detector strip group
     * @param region [1-3]
     * @param z: z coordinate
     * @return group [1-...]
     */
    private int getCGroup(int region, double z) {
        int group = 0;
        if(z>Constants.getCRCZMIN()[region-1] && z<Constants.getCRCZMAX()[region-1]) {
            for(int i=0; i<Constants.getCRCGRPZMIN()[region-1].length; i++) {
                if(z>Constants.getCRCGRPZMIN()[region-1][i] && z<Constants.getCRCGRPZMAX()[region-1][i]) {
                    group=i+1;
                    break;
                }       
            }
        }
        return group;
    }
    
    /**
     * Returns the Z position of the selected C-detector strip in the local frame
     * @param region
     * @param strip 
     * @return zc
     */
    private double getCstripZ(int region, int strip) {
        
        double z = udf;
        
        int group    = getCGroup(region,strip);
        if(group>0) {
            double zmin  = Constants.getCRCGRPZMIN()[region-1][group-1];     // group minimum z
            double pitch = Constants.getCRCWIDTH()[region-1][group-1];       // group pitch
            int    nmin  = Constants.getCRCGRPNMIN()[region-1][group-1];
            z  = zmin + (strip - nmin + 0.5) * pitch;
        }
        return z;
    }

    /**
     * Returns Arc3D corresponding to the selected C-detector strip according to ideal geometry (local frame) 
     * @param region (1-3)
     * @param sector (1-3)
     * @param strip
     * @return Arc3D striparc
     */
    public Arc3D getIdealCstrip(int region, int sector, int strip) {
        
        double radius = Constants.getCRCRADIUS()[region-1];
        double angle  = Constants.getCRCPHI()[region-1][sector-1] - Constants.getCRCDPHI()[region-1][sector-1];
        double theta  = Constants.getCRCDPHI()[region-1][sector-1]*2;
        double z      = this.getCstripZ(region, strip);
         
        Point3D origin  = new Point3D(radius,0,z);
        origin.rotateZ(angle);
        Point3D center  = new Point3D(0,0,z);
        Vector3D normal = new Vector3D(0,0,1);
        
        Arc3D striparc = new Arc3D(origin,center,normal,theta);
        return striparc;        
    }
    
    /**
     * Returns Arc3D corresponding to the selected C-detector strip according to real geometry
     * @param region
     * @param sector
     * @param strip
     * @return
     */
    public Arc3D getCstrip(int region, int sector, int strip) {

        int layer = this.getLayer(region, BMTType.C);
        Arc3D arcline = this.getIdealCstrip(region, sector, strip);
        
        Point3D    offset = this.getOffset(layer, sector);
        Vector3D rotation = this.getRotation(layer, sector);
        arcline.rotateX(rotation.x());
        arcline.rotateY(rotation.y());
        arcline.rotateZ(rotation.z());
        arcline.translateXYZ(offset.x(),offset.y(),offset.z());
        
        return arcline;
    }
   
    /**
     * Return the sector number
     * @param layer [0-6]
     * @param angle angle in the local frame in radians
     * @return sector [1-3] (not) accounting for dead areas if layer (0) [1-6] or 0 if layer is undefined
     */
    public int getSector(int layer, double angle) {
        
        boolean full = false;
        if(layer==0) {
            full  = true;
            layer = 1;
        }
        int region = this.getRegion(layer);   
        
        Vector3D vec = new Vector3D(Math.cos(angle),Math.sin(angle),0);
        
        int sector = 0;
        double width = 0.5; // Math.cos(60deg);
        double delta = -1;
        for (int i = 0; i < Constants.NSECTORS; i++) {
            double phi      = this.getPhi(layer, i+1);
            Vector3D center = new Vector3D(Math.cos(phi),Math.sin(phi),0);           
            double dcosphi  = center.dot(vec);
            if(dcosphi>width) {
                delta  = dcosphi; 
                sector = i+1; 
            }
        } 
        if(!full) {
            if(delta<Math.cos(this.getDPhi(layer, sector))) sector=0;
        }
        return sector;
    }
    
    /**
     * Return the layer number
     * @param traj point on one of the detector surfaces
     * @return layer [1-6] or 0 if undefined
     */
    public int getLayer(Point3D traj) {
        int layer=0;
        
        int sector = this.getSector(0, Math.atan2(traj.y(), traj.x()));
        for(int i=1; i<=Constants.NLAYERS; i++) {
            double radius = Constants.axes[i-1][sector-1].distance(traj).length();
            if(Math.abs(radius-this.getRadius(i))<accuracy) {
                layer = i;
                break;
            }
        }
        return layer;
    }
    
    /**
     * Checks whether a trajectory point is within the active area of a tile
     * @param traj
     * @return true/false
     */
    public boolean inDetector(Point3D traj) {
        
        int sector = this.getSector(0, Math.atan2(traj.y(), traj.x()));
        int layer  = this.getLayer(traj);
        return this.inDetector(layer, sector, traj);
    }

    /**
     * Checks whether a trajectory point is within the active area of a tile
     * @param layer
     * @param sector
     * @param traj
     * @return true/false
     */
    public boolean inDetector(int layer, int sector, Point3D traj) {
        boolean in = false;
        
        if(layer>0 && layer<=Constants.NLAYERS && sector>0 && sector<=Constants.NSECTORS) {
            Point3D    offset = this.getOffset(layer, sector);
            Vector3D rotation = this.getRotation(layer, sector);
            traj.translateXYZ(-offset.x(), -offset.y(), -offset.z());
            traj.rotateZ(-rotation.z());
            traj.rotateY(-rotation.y());
            traj.rotateX(-rotation.x());

            if(traj.z()>this.getZmin(layer) && traj.z()<this.getZmax(layer)) {      // check z position first
                int region = this.getRegion(layer);
                double phi = this.getPhi(layer, sector);
                Vector3D center = new Vector3D(Math.cos(phi),Math.sin(phi),0);           
                Vector3D vec    = new Vector3D(traj.x(),traj.y(),0);
                double dcosphi  = center.dot(vec)/vec.mag();
                if(dcosphi>Math.cos(this.getDPhi(layer, sector))) in=true;      
            }
        }
        return in;
    }

    /**
     * Checks whether a trajectory point is within the active area of a tile
     * @param traj
     * @return true/false
     */
    public int getStrip(Point3D traj) {
        int strip = 0;
        int layer = getLayer(traj);
        int sector = getSector(layer,Math.atan2(traj.y(), traj.x()));
        if(layer>0 && sector>0) strip = getStrip(layer,sector,traj);
        return strip;
    }


    /**
     * Return the number of the closest strip to the given trajectory point
     * Detector mis-alignments geometry are taken into account by transforming 
     * the trajectory point to detector local frame
     * @param layer (1-6)
     * @param sector (1-3)
     * @param traj trajectory point on the layer surface in the lab
     * @return strip number (0 if the point is not within the active area)
     */
    public int getStrip(int layer, int sector, Point3D traj) {
        int strip= 0;
        if(layer>0 && layer<=Constants.NLAYERS && sector>0 && sector<=Constants.NSECTORS) {
            Point3D    offset = this.getOffset(layer, sector);
            Vector3D rotation = this.getRotation(layer, sector);
            traj.translateXYZ(-offset.x(), -offset.y(), -offset.z());
            traj.rotateZ(-rotation.z());
            traj.rotateY(-rotation.y());
            traj.rotateX(-rotation.x());
            strip = this.getStripLocal(layer, traj);
        }
        return strip;
    }
    
    /**
     * Return the number of the closest strip to the given trajectory point
     * in the detector local frame
     * @param layer (1-6)
     * @param traj trajectory point on the layer surface in the local frame
     * @return strip number (0 if the point is not within the active area)
     */
    private int getStripLocal(int layer, Point3D traj) {
        
        BMTType type = this.getDetectorType(layer);
        int region = this.getRegion(layer);
        switch (type) {
            case C:
                return this.getCstrip(region,traj);
            case Z:
                return this.getZstrip(region,traj);
            default:
                return 0;
        }
    }

    /**
     * Return the number of the closest strip to the given trajectory point
     * in the detector local frame
     * @param region (1-3)
     * @param traj trajectory point on the layer surface in the local frame
     * @return strip number (0 if the point is not within the active area)
     */
    public int getCstrip(int region, Point3D traj) {
        
        int strip = 0;
        
        int group = getCGroup(region, traj.z());
        if(group>0) { 
            double zmin  = Constants.getCRCGRPZMIN()[region-1][group-1];
            double pitch = Constants.getCRCWIDTH()[region-1][group-1];
            strip = (int) Math.floor((traj.z()-zmin)/pitch);
            if(group>0) strip += Constants.getCRCGRPNMIN()[region-1][group-1];
        }
        return strip;
    }
    
    /**
     * Return the number of the closest strip to the given trajectory point
     * in the detector local frame
     * @param region (1-3)
     * @param traj trajectory point on the layer surface in the local frame
     * @return strip number (0 if the point is not within the active area)
     */
    public int getZstrip(int region, Point3D traj) {
        
        int strip = 0;
        
        int layer = getLayer(region, BMTType.Z);
        
        double angle = Math.atan2(traj.y(), traj.x());
        if(angle<0) angle += 2*Math.PI;
        
        int sector = getSector(layer,angle);
        if(sector>=1 && sector <=Constants.NSECTORS) {
            // CHECKME
            double edge   = Constants.getCRZPHI()[region-1][sector-1] - Constants.getCRZDPHI()[region-1][sector-1]; // 30 150 270
            double pitch  = Constants.getCRZWIDTH()[region-1];
            double radius = Constants.getCRZRADIUS()[region-1];
            double dphi = angle - edge; 
            if(dphi<0) dphi += 2*Math.PI;
            strip = (int) Math.floor(dphi*radius/pitch) + 1;
//            System.out.println(Math.toDegrees(angle) + " " + Math.toDegrees(dphi) + " " + sector + " " + strip_calc);
            if (strip < 1 || strip > Constants.getCRZNSTRIPS()[region-1]) {
                strip = -1;
            }
        }
        return strip;

    }
    
    /**
     * Calculate Theta Lorentz based on solenoid scale and drift settings
     * @param layer
     * @param sector
     * @return thetaL in radians
     */
    public double getThetaLorentz(int layer, int sector) {
         
        double thetaL = 0;
        
        double solenoidScale = org.jlab.rec.cvt.Constants.getSolenoidscale();
        
        if(Math.abs(solenoidScale)<0.001) {
            thetaL = 0;
        }
        else {
            if(Math.abs(solenoidScale)<0.8) {
                thetaL = Math.toRadians(getLorentzAngle(Constants.E_DRIFT_MF[layer-1][sector-1],Math.abs(solenoidScale*50)));
            } else {
                thetaL = Math.toRadians(getLorentzAngle(E_DRIFT_FF[layer-1][sector-1],Math.abs(solenoidScale*50)));
            }
        }
        if (solenoidScale<0) thetaL=-thetaL; 
        return thetaL;
    }

    /**
     * Calculate Lorentz angle correction
     * @param layer
     * @param sector
     * @return 
     */
    public double LorentzAngleCorr(int layer, int sector) {
        return (this.getThickness()/2 * Math.tan(this.getThetaLorentz(layer, sector))) / this.getRadius(layer);
    }


    /**
     * Executable method: implements checks
     * @param arg
     */
    public static void main (String arg[]) {
        
        CCDBConstantsLoader.Load(new DatabaseConstantProvider(11, "default"));
        
        Geometry    oldGeo = new Geometry();
        BMTGeometry newGeo = new BMTGeometry();
        
        System.out.println("\nLayer number for region and detector type:");
        System.out.println("\tRegion\tType\tLayer");
        for(int i=1; i<=Constants.NREGIONS; i++) {
            System.out.println("\t" + i + "\t" + newGeo.getLayer(i, BMTType.C) + "\t" + newGeo.getLayer(i, BMTType.Z));
        }

        System.out.println("\nDetector information by layer:");
        System.out.println("\tLayer\tRegion\tType\tRadius(mm)\tThickness(mm)\tZmin(mm)\tZmax(mm)\tNNstrips");
        for(int i=1; i<=Constants.NLAYERS; i++) {
            System.out.println("\t" + i + "\t"   + newGeo.getRegion(i) + "\t"   + newGeo.getDetectorType(i) 
                                        + "\t"   + newGeo.getRadius(i) + "\t\t" + newGeo.getThickness() 
                                        + "\t\t" + newGeo.getZmin(i)   + "\t\t" + newGeo.getZmax(i)
                                        + "\t\t" + newGeo.getNStrips(i)
                                );
        }

        System.out.println("\nOffsets and Rotations");
        System.out.println("\tLayer\tSector\tOffset and Rotation");
        for(int i=1; i<=Constants.NLAYERS; i++) {
            for(int j=1; j<=Constants.NSECTORS; j++) {
                System.out.println("\t" + i + "\t" + j + "\t" + newGeo.getOffset(i, j).toString() + "\t" + newGeo.getRotation(i, j).toString());
            }
        }

        System.out.println("\n\nC strip geometry check: z (mm), begin and end phi angles (radians)");
        System.out.println("\tRegion\tSector\tStrip\tZ\t\t\t\tBegin phi\t\tEnd phi");
        System.out.println("\t \t \t \tNew/Old/Comp \t\t\tNew/Old/Comp \t\tNew/Old/Comp");
        for(int i=1; i<=Constants.NREGIONS; i++) {
            for(int k=1; k<=Constants.NSECTORS; k++) {
                for(int j=1; j<=Constants.getCRCNSTRIPS()[i-1]; j++) {
                    // z
                    double ngeo = newGeo.getCstrip(i, 1, j).center().z();
                    double ogeo = oldGeo.CRCStrip_GetZ(newGeo.getLayer(i, BMTType.C),j);
                    String snew = String.format("%.4f", ngeo);
                    String sold = String.format("%.4f", ogeo);
                    String scom = String.format("%.4f", ngeo-ogeo);
                    // begin angle
                    double ngeo1 = Math.atan2(newGeo.getCstrip(i, k, j).origin().y(),newGeo.getCstrip(i, k, j).origin().x());
                    if(ngeo1<0) ngeo1 += 2*Math.PI;
                    double ogeo1 = oldGeo.CRC_GetBeginStrip(k, newGeo.getLayer(i, BMTType.C));
                    String snew1 = String.format("%.4f", ngeo1);
                    String sold1 = String.format("%.4f", ogeo1);
                    String scom1 = String.format("%.4f", ngeo1-ogeo1);
                    // end angle
                    double ngeo2 = Math.atan2(newGeo.getCstrip(i, k, j).end().y(),newGeo.getCstrip(i, k, j).end().x());
                    if(ngeo2<0) ngeo2 += 2*Math.PI;
                    double ogeo2 = oldGeo.CRC_GetEndStrip(k, newGeo.getLayer(i, BMTType.C));
                    String snew2 = String.format("%.4f", ngeo2);
                    String sold2 = String.format("%.4f", ogeo2);
                    String scom2 = String.format("%.4f", ngeo2-ogeo2);
                    if(j==1 || j==Constants.getCRCNSTRIPS()[i-1])
                    System.out.println("\t" + i + "\t" + k + "\t" + j + "\t" + snew + "/" + sold + "/" + scom + "\t" + snew1 + "/" + sold1 + "/" + scom1 + "\t" + snew2 + "/" + sold2 + "/" + scom2);
                }
            }
        }


        System.out.println("\n\nZ strip geometry check: phi (radians), begin and end z (mm)");
        System.out.println("\tRegion\tSector\tStrip\tphi\t\t\tBegin Z\t\t\t\tEnd Z");
        System.out.println("\t \t \t \tNew/Old/Comp \t\tNew/Old/Comp \t\t\tNew/Old/Comp");
        for(int i=1; i<=Constants.NREGIONS; i++) {
            for(int k=1; k<=Constants.NSECTORS; k++) {
                for(int j=1; j<=Constants.getCRZNSTRIPS()[i-1]; j++) {
                    // phi
                    double ngeo = Math.atan2(newGeo.getZstrip(i, k, j).origin().y(), newGeo.getZstrip(i, k, j).origin().x());
                    if(ngeo<0) ngeo += 2*Math.PI;
                    double ogeo = oldGeo.CRZStrip_GetPhi(k, newGeo.getLayer(i, BMTType.Z), j)%(2*Math.PI);
                    String snew = String.format("%.4f", ngeo);
                    String sold = String.format("%.4f", ogeo);
                    String scom = String.format("%.4f", ngeo-ogeo);
                    // begin z
                    double ngeo1 = newGeo.getZstrip(i, k, j).origin().z();
                    double ogeo1 = Constants.getCRCZMIN()[i-1];
                    String snew1 = String.format("%.4f", ngeo1);
                    String sold1 = String.format("%.4f", ogeo1);
                    String scom1 = String.format("%.4f", ngeo1-ogeo1);
                    // end z
                    double ngeo2 = newGeo.getZstrip(i, k, j).end().z();
                    double ogeo2 = Constants.getCRCZMAX()[i-1];
                    String snew2 = String.format("%.4f", ngeo2);
                    String sold2 = String.format("%.4f", ogeo2);
                    String scom2 = String.format("%.4f", ngeo2-ogeo2);
                    if(j==1 || j==Constants.getCRZNSTRIPS()[i-1])
                    System.out.println("\t" + i + "\t" + k + "\t" + j + "\t" + snew + "/" + sold + "/" + scom + "\t" + snew1 + "/" + sold1 + "/" + scom1 + "\t" + snew2 + "/" + sold2 + "/" + scom2);
                }
            }
        }
        
        
        System.out.println("\n\n Trajectory -> strip check: new/old strip numbers");;
        System.out.println("\tLayer\tz (mm)\t\tphi (deg)\tsector\tnew/old/comp strip numbers");
        for(int i=1; i<=Constants.NLAYERS; i++) {
            double radius = newGeo.getRadius(i);
            double zmin   = newGeo.getZmin(i);
            double zmax   = newGeo.getZmax(i);
            for(int j=0; j<5; j++) {
                double z   = Math.random()*(zmax-zmin)+zmin;
                //double z   = (1.0*j/500.0)*(zmax-zmin)+zmin;
                //double z = oldGeo.CRCStrip_GetZ(i, j);
                double phi = Math.random()*2*Math.PI;
                //double phi = (1.0*j/500.0)*2*Math.PI;
                //double phi = oldGeo.CRZStrip_GetPhi(3, i, j+1);
                Point3D traj = new Point3D(radius*Math.cos(phi),radius*Math.sin(phi),z);
                int nsect  = newGeo.getSector(i, phi);
                int nstrip = newGeo.getStrip(i,nsect,traj);
                int ostrip = -1;
                if     (newGeo.getDetectorType(i)==BMTType.C)            ostrip = oldGeo.getCStrip(i, z);
                else if(newGeo.getDetectorType(i)==BMTType.Z && nsect>0) ostrip = oldGeo.getZStrip(i, phi);
                int diff = -1;
                if(nstrip>0 && ostrip>0) diff = nstrip - ostrip;
                System.out.println("\t" + i + "\t" + String.format("%8.4f",z) + "\t" + String.format("%8.4f", Math.toDegrees(phi)) + "\t" + nsect + "\t" + nstrip + "/" + ostrip + "/" + diff);
            }
        }
    
    
        System.out.println("\n\n Strip -> Trajectory -> strip check: new/old strip numbers");;
        System.out.println("\tLayer\tz (mm)\t\tphi (deg)\tsector\tnew/old/comp strip numbers");
        boolean check = true;
        for(int i=1; i<=Constants.NLAYERS; i++) {
            double radius  = newGeo.getRadius(i);
            double zmin    = newGeo.getZmin(i);
            double zmax    = newGeo.getZmax(i);
            int    nstrips = newGeo.getNStrips(i);
            int    region  = newGeo.getRegion(i);
            for(int k=1; k<=Constants.NSECTORS; k++) {
                double phmin = Constants.getCRCPHI()[region-1][k-1]-Constants.getCRCDPHI()[region-1][k-1];
                double phmax = Constants.getCRCPHI()[region-1][k-1]+Constants.getCRCDPHI()[region-1][k-1];
                for(int j=1; j<=nstrips; j++) {
                    
                    double z   = Math.random()*(zmax-zmin)+zmin;
                    if(newGeo.getDetectorType(i)==BMTType.C) z = oldGeo.CRCStrip_GetZ(i, j);
                    
                    double phi = Math.random()*(phmax-phmin)+phmin;
                    if(newGeo.getDetectorType(i)==BMTType.Z) {
                        phi = oldGeo.CRZStrip_GetPhi(k, i, j);
                    }
                    
                    Point3D traj = new Point3D(radius*Math.cos(phi),radius*Math.sin(phi),z);
                    int nstrip = newGeo.getStrip(i,k,traj);
                    
                    int ostrip = -1;
                    if     (newGeo.getDetectorType(i)==BMTType.C) ostrip = oldGeo.getCStrip(i, z);
                    else if(newGeo.getDetectorType(i)==BMTType.Z) ostrip = oldGeo.getZStrip(i, phi);
                    int diff = -1;
                    if(nstrip>0 && ostrip>0) diff=nstrip-ostrip;
                    if(nstrip!=j) check=false;
                    if(diff!=0 || !check) System.out.println("\t" + i + "\t" + String.format("%8.4f",z) + "\t" + String.format("%8.4f", Math.toDegrees(phi)) + "\t" + k + "\t" 
                                           + j + "/" + nstrip + "/" + ostrip + "/" + diff + "\t" + newGeo.getPitch(i, j));
                }
            }
        }
        System.out.println("Check: " + check);


        System.out.println("\nPlotting acceptance");
        DataGroup acceptance = new DataGroup(3,2);
        for(int i=1; i<=Constants.NLAYERS; i++) {
            double radius = newGeo.getRadius(i);
            double zmin   = newGeo.getZmin(i);
            double zmax   = newGeo.getZmax(i);
            H2F hi_acc = new H2F("hi_acc","Layer "+i, 50, -200, 300, 120, 0, 360);
            acceptance.addDataSet(hi_acc, i-1);
            for(int j=0; j<5000; j++) {
                double z   = Math.random()*(zmax-zmin)+zmin;
                double phi = Math.random()*2*Math.PI;
                Point3D traj = new Point3D(radius*Math.cos(phi),radius*Math.sin(phi),z);
                if(i!=newGeo.getLayer(traj)) System.out.println("Error in getLayer");
                if(newGeo.inDetector(i, newGeo.getSector(i, phi), traj)) 
                    hi_acc.fill(z, Math.toDegrees(phi));
            }
        }
        JFrame frame = new JFrame("FT Reconstruction");
        frame.setSize(1200, 800);
        EmbeddedCanvas canvas = new EmbeddedCanvas();
        canvas.draw(acceptance);
        frame.add(canvas);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
       
    
    
    //added methods:
//    public int isInDetector(int layer, double angle, double jitter) {
//        int num_region = (int) (layer + 1) / 2 - 1; // region index (0...2) 0=layers 1&2, 1=layers 3&4, 2=layers 5&6
//        if (angle < 0) {
//            angle += 2 * Math.PI; // from 0 to 2Pi
//        }
//        double angle_i = 0; // first angular boundary init
//        double angle_f = 0; // second angular boundary for detector A, B, or C init
//        int num_detector = 2;
//        //double jitter = Math.toRadians(Constants.isInSectorJitter);
//        for (int i = 0; i < 3; i++) {
//
//            //angle_i=Constants.getCRCEDGE1()[num_region][i]+Constants.getCRCXPOS()[num_region]/Constants.getCRCRADIUS()[num_region];
//            //angle_f=Constants.getCRCEDGE1()[num_region][i]+(Constants.getCRCXPOS()[num_region]+Constants.getCRCLENGTH()[num_region])/Constants.getCRCRADIUS()[num_region];
//            angle_i = Constants.getCRCEDGE1()[num_region][i];
//            angle_f = Constants.getCRCEDGE2()[num_region][i];
//            if ((angle >= angle_i - jitter && angle <= angle_f + jitter)) {
//                num_detector = i;
//            }
//        }
//       
//        return num_detector;
//    }
//    public int isInSector(int layer, double angle, double jitter) {
//        //double jitter = Math.toRadians(Constants.isInSectorJitter);
//        int value = -1;
//        int num_det = this.isInDetector(layer, angle, jitter);
//        /*	if(num_det == 0)
//			value = 2;
//		if(num_det ==2)
//			value = 3;
//		if(num_det == 1)
//			value = 1; */
//        value = num_det + 1;
//
//        return value;
//    }
    
//    /**
//     *
//     * @param layer the hit layer
//     * @param strip the hit strip
//     * @return the z position in mm for the C-detectors
//     */
//    public double CRCStrip_GetZ(int layer, int strip) {
//
//        int num_strip = strip - 1;     			// index of the strip (starts at 0)
//        int num_region = (int) (layer + 1) / 2 - 1; // region index (0...2) 0=layers 1&2, 1=layers 3&4, 2=layers 5&6
//
//        //For CR6C, this function returns the Z position of the strip center
//        int group = 0;
//        int limit = Constants.getCRCGROUP()[num_region][group];
//        double zc = Constants.getCRCZMIN()[num_region];
//
//        if (num_strip > 0) {
//            for (int j = 1; j < num_strip + 1; j++) {
//                zc += Constants.getCRCWIDTH()[num_region][group];
//                if (j >= limit) { //test if we change the width
//                    group++;
//                    limit += Constants.getCRCGROUP()[num_region][group];
//                }
//              }
//        }
//        zc += Constants.getCRCWIDTH()[num_region][group]/2.;
//        return zc; //in mm
//    }

//    public double CRCStrip_GetPitch(int layer, int strip) {
//
//        int num_strip = strip - 1;     			// index of the strip (starts at 0)
//        int num_region = (int) (layer + 1) / 2 - 1; // region index (0...2) 0=layers 1&2, 1=layers 3&4, 2=layers 5&6
//
//        //For CR6C, this function returns the Z position of the strip center
//        int group = 0;
//        int limit = Constants.getCRCGROUP()[num_region][group];
//
//        if (num_strip > 0) {
//            for (int j = 1; j < num_strip + 1; j++) {
//
//                if (j >= limit) { //test if we change the width
//                    group++;
//                    limit += Constants.getCRCGROUP()[num_region][group];
//                }
//            }
//        }
//
//        return Constants.getCRCWIDTH()[num_region][group]; //
//    }
/**
     *
     * @param sector the sector in CLAS12 1...3
     * @param layer the layer 1...6
     * @param strip the strip number (starts at 1)
     * @return the angle to localize the center of strip
     */
    public double CRZStrip_GetPhi(int sector, int layer, int strip) {

        // Sector = num_detector + 1;	
        // num_detector = 0 (region A), 1 (region B), 2, (region C)
        //For CRZ, this function returns the angle to localize the  center of strip "num_strip" for the "num_detector"
        int num_detector = sector - 1;				// index of the detector (0...2)

        int num_strip = strip - 1;     									// index of the strip (starts at 0)
        int num_region = (int) (layer + 1) / 2 - 1; 						// region index (0...2) 0=layers 1&2, 1=layers 3&4, 2=layers 5&6
        double angle = Constants.getCRZEDGE1()[num_region][num_detector] + ((double) num_strip+0.5) * Constants.getCRZWIDTH()[num_region] / Constants.getCRZRADIUS()[num_region];
        return angle; //in rad 
    }
//    /**
//     *
//     * @param layer the layer 1...6
//     * @param angle the position angle of the hit in the Z detector
//     * @return the Z strip as a function of azimuthal angle
//     */
//    public int getZStrip(int layer, double angle) { // the angle is the Lorentz uncorrected angle
//        double jitter = Math.toRadians(Constants.isInSectorJitter);
//        int num_region = (int) (layer + 1) / 2 - 1; // region index (0...2) 0=layers 1&2, 1=layers 3&4, 2=layers 5&6
//        int num_detector = isInDetector(layer, angle, jitter);
//        if (num_detector == -1) {
//            return -1;
//        }
//
//        if (angle < 0) {
//            angle += 2 * Math.PI; // from 0 to 2Pi
//        }
//        if (num_detector == 1) {
//            double angle_f = Constants.getCRCEDGE1()[num_region][1] + (Constants.getCRCXPOS()[num_region] + Constants.getCRCLENGTH()[num_region]) / Constants.getCRCRADIUS()[num_region] - 2 * Math.PI;
//            if (angle >= 0 && angle <= angle_f) {
//                angle += 2 * Math.PI;
//            }
//        }
//        //double strip_calc = ( (angle-Constants.getCRZEDGE1()[num_region][num_detector])*Constants.getCRZRADIUS()[num_region]-Constants.getCRZXPOS()[num_region]-Constants.getCRZWIDTH()[num_region]/2.)/(Constants.getCRZWIDTH()[num_region]+Constants.getCRZSPACING()[num_region]);
//        //double strip_calc = ((angle - Constants.getCRZEDGE1()[num_region][num_detector]) * Constants.getCRZRADIUS()[num_region]) / (Constants.getCRZWIDTH()[num_region]);
//        double strip_calc = ((angle - Constants.getCRZEDGE1()[num_region][num_detector]) * Constants.getCRZRADIUS()[num_region]) / (Constants.getCRZWIDTH()[num_region])-0.5;
//        strip_calc = (int) (Math.round(strip_calc * 1d) / 1d);
//        int strip_num = (int) Math.floor(strip_calc);
//
//        int value = strip_num + 1;
//        //int value = strip_num;
//
//        if (value < 1 || value > Constants.getCRZNSTRIPS()[num_region]) {
//            value = -1;
//        }
//
//        return value;
//    }

//    public void setLorentzAngle(int layer, int sector) {
//     	org.jlab.rec.cvt.bmt.Constants.setThetaL(layer, sector); 
//    }
////    // Correct strip position before clustering
////    public int getLorentzCorrectedZStrip(int sector, int layer, int theMeasuredZStrip) {
////
////        double theMeasuredPhi = this.CRZStrip_GetPhi(sector, layer, theMeasuredZStrip);
////        double theLorentzCorrectedAngle = this.LorentzAngleCorr(theMeasuredPhi, layer);
////        
////        return this.getZStrip(layer, theLorentzCorrectedAngle);
////    }   
//    public double LorentzAngleCorr(double phi, int layer) {
//
//        int num_region = (int) (layer + 1) / 2 - 1; // region index (0...2) 0=layers 1&2, 1=layers 3&4, 2=layers 5&6
//        //return phi +( Constants.hDrift/2*Math.tan(Constants.getThetaL()) )/Constants.getCRZRADIUS()[num_region];
//        //return phi + (Constants.hDrift * Math.tan(Constants.getThetaL())) / (Constants.getCRZRADIUS()[num_region]);
//        return phi + (Constants.hStrip2Det * Math.tan(Constants.getThetaL())) / (Constants.getCRZRADIUS()[num_region]);
//    }
//    
//    /**
//     *
//     * @param sector
//     * @param layer
//     * @param x
//     * @return a boolean indicating is the track hit is in the fiducial detector
//     */
//    public boolean isInFiducial(int sector, int layer, int axis,double[] x) {
//
//        boolean isInFid = false;
//
//        int num_region = (int) (layer + 1) / 2 - 1; // region index (0...2) 0=layers 1&2, 1=layers 3&4, 2=layers 5&6;
//
//        double z_i = CRZ_GetZStrip(layer) - Constants.getCRZLENGTH()[num_region] / 2.; // fiducial z-profile lower limit
//        double z_f = CRZ_GetZStrip(layer) + Constants.getCRZLENGTH()[num_region] / 2.; // fiducial z-profile upper limit
//
//        double R_i = 0; // inner radius init
//        double R_f = 0; // outer radius init for a C or Z detector
//        if (org.jlab.rec.cvt.bmt.Geometry.getZorC(layer) == 1) {
//            R_i = Constants.getCRZRADIUS()[num_region]; // Z layer
//        }
//        if (org.jlab.rec.cvt.bmt.Geometry.getZorC(layer) == 0) {
//            R_i = Constants.getCRCRADIUS()[num_region]; // // C-dtectors 
//        }
//        R_f = R_i + Constants.hDrift;
//
//        double angle_i = 0; // first angular boundary init
//        double angle_f = 0; // second angular boundary for detector A, B, or C init
//        double A_i = CRC_GetBeginStrip(sector, layer);
//        double A_f = CRC_GetEndStrip(sector, layer);
//        angle_i = A_i;
//        angle_f = A_f;
//        if (A_i > A_f) { // for B-detector
//            angle_f = A_i;
//            angle_i = A_f;
//        }
//        // the hit parameters
//        double angle = Math.atan2(x[1], x[0]);
//        if (angle > 2 * Math.PI) {
//            angle -= 2 * Math.PI;
//        }
//        double R = Math.sqrt(x[0] * x[0] + x[1] * x[1]);
//        double z = x[2];
//
//        if ((angle_i - angle) < (angle_f - angle_i) && (R - R_i) < (R_f - R_i) && (z - z_i) < (z_f - z_i)) {
//            isInFid = true;
//        }
//
//        return isInFid;
//    }
//    /**
//     *
//     * @param sector the sector in CLAS12 1...3
//     * @param layer the layer 1...6
//     * @return the angle to localize the beginning of the strips
//     */
//    public double CRC_GetBeginStrip(int sector, int layer) {
//        // Sector = num_detector + 1;	
//        // num_detector = 0 (region A), 1 (region B), 2, (region C)
//
//        int num_detector = sector -1; 			// index of the detector (0...2)
//        int num_region = (int) (layer + 1) / 2 - 1; // region index (0...2) 0=layers 1&2, 1=layers 3&4, 2=layers 5&6
//
//        //For CRC, this function returns the angle to localize the beginning of the strips
//        double angle = Constants.getCRCEDGE1()[num_region][num_detector] + Constants.getCRCXPOS()[num_region] / Constants.getCRCRADIUS()[num_region];
//        if (angle > 2 * Math.PI) {
//            angle -= 2 * Math.PI;
//        }
//        return angle; //in rad
//    }
//
//    /**
//     *
//     * @param sector the sector in CLAS12 1...3
//     * @param layer the layer 1...6
//     * @return the angle to localize the end of the strips
//     */
//    public double CRC_GetEndStrip(int sector, int layer) {
//        // Sector = num_detector + 1;	
//        // num_detector = 0 (region A), 1 (region B), 2, (region C)
//
//        int num_detector = sector -1; 			// index of the detector (0...2)
//        int num_region = (int) (layer + 1) / 2 - 1; 					// region index (0...2) 0=layers 1&2, 1=layers 3&4, 2=layers 5&6
//
//        //For CRC, this function returns the angle to localize the end of the strips
//        double angle = Constants.getCRCEDGE2()[num_region][num_detector] + Constants.getCRCXPOS()[num_region] / Constants.getCRCRADIUS()[num_region];
//        if (angle > 2 * Math.PI) {
//            angle -= 2 * Math.PI;
//        }
//        return angle; //in rad
//    }
//    /**
//    *
//    * @param angle
//    * @param sector
//    * @param layer
//    * @param x
//    * @return a boolean indicating if the given angle is the sector 
//    */
//    public boolean checkIsInSector( double angle, int sector, int layer, double jitter ) {
//    	if( layer < 1 || layer > 6 ) {
//    		System.err.println(" BMT layer has to be 1 <= layer <= 6");
//    		return false;
//    	}
//    	if( sector < 1 || sector > 3 ) {
//    		System.err.println(" BMT sector has to be 1 <= layer <= 3");
//    		return false;
//    	}
//    	
//    	int num_region = (int) (layer + 1) / 2 - 1; // region index (0...2) 0=layers 1&2, 1=layers 3&4, 2=layers 5&6
//        double angle_i = 0; // first angular boundary init
//        double angle_f = 0; // second angular boundary for detector A, B, or C init
//        angle_i = Constants.getCRCEDGE1()[num_region][sector-1];
//        angle_f = Constants.getCRCEDGE2()[num_region][sector-1];
//        
//
//        if (angle < 0) {
//            angle += 2 * Math.PI; // from 0 to 2Pi
//        }
//        
//        if( sector == 3 ) {
//        	if( angle < Math.PI ) {
//        		if( angle < angle_f + jitter ) return true;
//        		else return false;
//        	}
//        	else {
//        		if( angle > angle_i - jitter ) return true;
//        		else return false;
//        	}
//        }
//        else {
//            if ( (angle >= angle_i - jitter && angle <= angle_f + jitter))
//            	return true;
//            else
//            	return false;
//            
//        }
//    }
    
//    /**
//     *
//     * @param layer
//     * @param trk_z the track z position of intersection with the C-detector
//     * @return the C-strip
//     */
//    public int getCStrip(int layer, double trk_z) {
//
//        int num_region = (int) (layer + 1) / 2 - 1; // region index (0...2) 0=layers 1&2, 1=layers 3&4, 2=layers 5&6
//        int strip_group = 0;
//        int ClosestStrip = -1;
//        // get group
//        int len = Constants.getCRCGROUP()[num_region].length;
//        double[] Z_lowBound = new double[len];
//        double[] Z_uppBound = new double[len];
//        int[] NStrips = new int[len];
//
//        double zi = Constants.getCRCZMIN()[num_region] + Constants.getCRCOFFSET()[num_region];
//        double z = trk_z - zi;
//
//        for (int i = 0; i < len; i++) {
//            if(i==0) {
//                Z_lowBound[i] = 0; 
//                NStrips[i] = Constants.getCRCGROUP()[num_region][i];
//            }
//            else {
//                Z_lowBound[i] = Z_uppBound[i - 1];
//                NStrips[i] = NStrips[i - 1] + Constants.getCRCGROUP()[num_region][i];
//            }
//            Z_uppBound[i] = Z_lowBound[i] + Constants.getCRCGROUP()[num_region][i] * Constants.getCRCWIDTH()[num_region][i];
//            
//            if (z >= Z_lowBound[i] && z <= Z_uppBound[i]) {
//                strip_group = i;
//                ClosestStrip = 1 + (int) Math.floor((z - Z_lowBound[strip_group]) / Constants.getCRCWIDTH()[num_region][strip_group]);
//                if(i>0)  ClosestStrip += NStrips[i - 1];
//                //ClosestStrip = (int) (Math.round(((z-Z_lowBound[strip_group])/(Constants.getCRCWIDTH()[num_region][strip_group] + Constants.getCRCSPACING()[num_region]))))+NStrips[i-1];
//
//                len = i;
//            }
//        }
//        return ClosestStrip;
//    }
//    
//    public boolean isInFiducial(double x, double y, double z, int layer) {
//
//        boolean isOK = false;
//
//        int num_region = (int) (layer + 1) / 2 - 1;
//
//        int axis = Geometry.getZorC(layer);
//
//        double R = 0;
//        if (axis == 0) {
//            R = org.jlab.rec.cvt.bmt.Constants.getCRCRADIUS()[num_region];
//        }
//        if (axis == 1) {
//            R = org.jlab.rec.cvt.bmt.Constants.getCRZRADIUS()[num_region];
//        }
//
//        double CRZLENGTH = org.jlab.rec.cvt.bmt.Constants.getCRCLENGTH()[num_region];
//        double CRZZMIN = org.jlab.rec.cvt.bmt.Constants.getCRZZMIN()[num_region];
//        double CRZOFFSET = org.jlab.rec.cvt.bmt.Constants.getCRZOFFSET()[num_region];
//
//        double z_min = CRZZMIN + CRZOFFSET;
//        double z_max = z_min + CRZLENGTH;
//
//        double epsilon = 1e-1;
//
//        if (Math.abs(x) < R + epsilon && Math.abs(y) < R + epsilon && z > z_min - epsilon && z < z_max + epsilon) {
//            isOK = true;
//        }
//        return isOK;
//    }
//    
//    /**
//     *
//     * @param layer the layer 1...6
//     * @return the Z position of the strip center
//     */
//    private double CRZ_GetZStrip(int layer) {
//        int num_region = (int) (layer + 1) / 2 - 1; // region index (0...2) 0=layers 1&2, 1=layers 3&4, 2=layers 5&6
//        //For CRZ, this function returns the Z position of the strip center
//        double zc = Constants.getCRZZMIN()[num_region] + Constants.getCRZOFFSET()[num_region] + Constants.getCRZLENGTH()[num_region] / 2.;
//        return zc; //in mm
//    }


}
