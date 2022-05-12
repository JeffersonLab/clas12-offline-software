package org.jlab.rec.cvt.bmt;

import javax.swing.JFrame;
import java.util.ArrayList;
import java.util.List;
import org.jlab.detector.calib.utils.DatabaseConstantProvider;
import org.jlab.geom.prim.Arc3D;
import org.jlab.geom.prim.Cylindrical3D;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.groot.data.H2F;
import org.jlab.groot.group.DataGroup;
import static org.jlab.rec.cvt.bmt.BMTConstants.E_DRIFT_FF;
import static org.jlab.rec.cvt.bmt.BMTConstants.E_DRIFT_MF;
import static org.jlab.rec.cvt.bmt.Lorentz.getLorentzAngle;
import org.jlab.clas.swimtools.Swim;
import org.jlab.clas.tracking.kalmanfilter.Surface;
import org.jlab.clas.tracking.kalmanfilter.Units;
import org.jlab.clas.tracking.objects.Strip;
import org.jlab.geom.prim.Transformation3D;
import org.jlab.groot.data.H1F;
import org.jlab.groot.graphics.EmbeddedCanvasTabbed;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.hipo.HipoDataSource;
import org.jlab.rec.cvt.Constants;

/**
 *
 * @author devita
 */
public class BMTGeometry {

    private final static int[] lZ = { 2, 3, 5};
    private final static int[] lC = { 1, 4, 6}; 
    private final static double accuracy =  1E-4; // mm
    private final static double udf      = -9999; // mm
    public final static int NLAYERS = 6;
    public final static int NSECTORS = 3;
    public static final int NPASSIVE = 2;

    private final static double[] INNERTUBEDIM = {140, 141, 366, 4}; // inner radius, outer radius, halflength, offset}
    private final static double[] OUTERTUBEDIM = {234, 235, 372, 5}; // inner radius, outer radius, halflength, offset}
    private final static double[] TUBEMAT = {1.75E-3, 0.51342, 250.0, 78}; // density, Z/A, X0, I
    /**
     * Handles BMT geometry
     */
    public BMTGeometry() {
    }
    
    public int getNLayers() {
        return BMTConstants.NLAYERS;
    }
    
    public int getNSectors() {
        return BMTConstants.NSECTORS;
    }
    
    /**
     * Return layer number for a given region and detector type
     * @param region (1-3)
     * @param detector (C or Z)
     * @return layer (1-6) 
     */
    public int getLayer(int region, BMTType detector) {
        
        if(!(region>=1 && region<=3)) 
            throw new IllegalArgumentException("Error: invalid region="+region);
        if(detector!=BMTType.C && detector!=BMTType.Z) 
            throw new IllegalArgumentException("Error: invalid detector type");

        if( detector == BMTType.Z ) {
                return lZ[ region - 1 ];
        }
        else {
                return lC[ region - 1 ];
        }
    }

    /**
     * @return the lZ
     */
    public static int[] getlZ() {
        return lZ;
    }

    /**
     * @return the lC
     */
    public static int[] getlC() {
        return lC;
    }

    /**
     * Return region number for a given layer
     * @param layer (1-6)
     * @return region (1-3) 
     */
    public int getRegion(int layer) {
        if(!(layer>=1 && layer<=BMTConstants.NLAYERS)) 
            throw new IllegalArgumentException("Error: invalid layer="+layer);
        
        return (int) Math.floor((layer+1)/2);
    }

    public static int getModuleId(int layer, int sector) {
        return layer*100+sector;
    }

    /**
     * Return region number for a given layer
     * @param layer (1-6)
     * @return type ("C" or "Z");
     */
    public static BMTType getDetectorType(int layer) {
        if(!(layer>=1 && layer<=BMTConstants.NLAYERS)) 
            throw new IllegalArgumentException("Error: invalid layer="+layer);
    	
        if(layer == lC[0] || layer == lC[1] || layer == lC[2]) return BMTType.C;
        else return BMTType.Z;
    }
    
    
    /**
     * Return radius of the selected strip layer
     * @param layer (1-6)
     * @return radius
     */
    public double getRadius(int layer) {
        
        int region = this.getRegion(layer);
        BMTType det = BMTGeometry.getDetectorType(layer);
        
        if(det == BMTType.C) return BMTConstants.getCRCRADIUS()[region-1];
        else                 return BMTConstants.getCRZRADIUS()[region-1];
    }
    
    /**
     * Return radius of the selected layer corresponding to the center of the drift gap
     * @param layer (1-6)
     * @return radius (=0 if layer is out of range)
     */
    public double getRadiusMidDrift(int layer) {        
        return this.getRadius(layer) + BMTConstants.HDRIFT/2;
    }
    
    /**
     * Return number of strips of the selected layer
     * @param layer (1-6)
     * @return nstrips (=0 if layer is out of range)
     */
    public int getNStrips(int layer) {
        
        int region = this.getRegion(layer);
        BMTType det = BMTGeometry.getDetectorType(layer);
        
        int nstrips = 0;
        if     (det == BMTType.C) nstrips = BMTConstants.getCRCNSTRIPS()[region-1];
        else if(det == BMTType.Z) nstrips = BMTConstants.getCRZNSTRIPS()[region-1];
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
        BMTType det = BMTGeometry.getDetectorType(layer);
        
        double pitch = 0;
        if     (det == BMTType.C) pitch = this.getCPitch(region, strip);
        else if(det == BMTType.Z) pitch = this.getZPitch(region, strip);
        return pitch;
    }
    
    /**
     * Return pitch for C strips
     * @param region (1-3)
     * @param strip
     * @return pitch (0 if region or strip are out of range
     */
    private double getCPitch(int region, int strip) {
        if(!(region>=1 && region<=3)) 
            throw new IllegalArgumentException("Error: invalid region="+region);
        if(!(strip>=1 && strip<=BMTConstants.getCRCNSTRIPS()[region-1])) 
            throw new IllegalArgumentException("Error: invalid strip="+strip);
        
        int group = this.getCGroup(region, strip);
        double pitch = BMTConstants.getCRCWIDTH()[region-1][group-1];
        return pitch;        
    }
    
    /**
     * Return pitch for Z strips
     * @param region (1-3)
     * @param strip
     * @return pitch (0 if region or strip are out of range
     */
    private double getZPitch(int region, int strip) {
        if(!(region>=1 && region<=3)) 
            throw new IllegalArgumentException("Error: invalid region="+region);
        if(!(strip>=1 && strip<=BMTConstants.getCRZNSTRIPS()[region-1])) 
            throw new IllegalArgumentException("Error: invalid strip="+strip);
        
        return BMTConstants.getCRZWIDTH()[region-1];       
    }
    
    /**
     * Return minimum z of the selected layer in the local frame
     * @param layer (1-6)
     * @return z (=udf if layer is out of range)
     */
    public double getZmin(int layer) {
        
        double z = udf;
        
        int region = this.getRegion(layer);
        BMTType det = BMTGeometry.getDetectorType(layer);
        
        if     (det == BMTType.C) z = BMTConstants.getCRCZMIN()[region-1];
        else if(det == BMTType.Z) z = BMTConstants.getCRZZMIN()[region-1];
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
        BMTType det = BMTGeometry.getDetectorType(layer);
        
        if     (det == BMTType.C) z = BMTConstants.getCRCZMAX()[region-1];
        else if(det == BMTType.Z) z = BMTConstants.getCRZZMAX()[region-1];
        return z;
    }
    
    /**
     * Return tile phi center
     * @param layer (1-6)
     * @param sector (1-3)
     * @return phi (=udf if layer is out of range)
     */
    public double getPhi(int layer, int sector) {
        
        if(!(0<sector && sector<=BMTConstants.NSECTORS))
            throw new IllegalArgumentException("Error: invalid sector="+sector);

        double phi = udf;
        
        int region = this.getRegion(layer);
        BMTType det = this.getDetectorType(layer);
        
        if     (det == BMTType.C) phi = BMTConstants.getCRCPHI()[region-1][sector-1];
        else if(det == BMTType.Z) phi = BMTConstants.getCRZPHI()[region-1][sector-1];
        return phi;
    }
 
    /**
     * Return half tile phi coverage
     * @param layer (1-6)
     * @param sector (1-3)
     * @return dz (=udf if layer is out of range)
     */
    public double getDPhi(int layer, int sector) {
        
        if(!(0<sector && sector<=BMTConstants.NSECTORS))
            throw new IllegalArgumentException("Error: invalid sector="+sector);

        double dphi = udf;
        
        int region = this.getRegion(layer);
        BMTType det = BMTGeometry.getDetectorType(layer);
        
        if     (det == BMTType.C) dphi = BMTConstants.getCRCDPHI()[region-1][sector-1];
        else if(det == BMTType.Z) dphi = BMTConstants.getCRZDPHI()[region-1][sector-1];
        return dphi;
    }
    
    /**
     * Return thickness of the drift gap 
     * @return thickness 
     */
    public double getThickness() {
        
        return BMTConstants.HDRIFT;
    }    
    
    /**
     * Return offset of the selected tile, identified by layer and sector
     * @param layer (1-6)
     * @param sector (1-3)
     * @return Point3D offset: 3D offset
     */
    public Point3D getOffset(int layer, int sector) {
        Point3D offset = new Point3D();
        if(layer>0 && layer<BMTConstants.NLAYERS+1 && sector > 0 && sector<BMTConstants.NSECTORS+1) {    
            offset.copy(BMTConstants.SHIFTS[layer-1][sector-1]); 
        }
       else {
            System.out.println("ERROR: out of layer number in getOffset(int layer, region)");
        }
        return offset;
    }
    
    /**
     * Return ROTATIONS for the selected tile, identified by layer and sector
     * @param layer (1-6)
     * @param sector (1-3)
     * @return Point3D offset: 3D offset
     */
    public Vector3D getRotation(int layer, int sector) {
        Vector3D rot = new Vector3D();
        if(layer>0 && layer<BMTConstants.NLAYERS+1 && sector > 0 && sector<BMTConstants.NSECTORS+1) {    
            rot.copy(BMTConstants.ROTATIONS[layer-1][sector-1]);
            return rot;
        } else {
            System.out.println("ERROR: out of layer sector number in getRotation(int layer, region)");
            return new Vector3D();
        }
    }
    
    public Transformation3D toGlobal(int layer, int sector) {
        if(!(0<layer && layer<=BMTConstants.NLAYERS))
            throw new IllegalArgumentException("Error: invalid layer="+layer);
        if(!(0<sector && sector<=BMTConstants.NSECTORS))
            throw new IllegalArgumentException("Error: invalid sector="+sector);
        
        return BMTConstants.TOGLOBAL[layer-1][sector-1];
    }
    
    public Transformation3D toLocal(int layer, int sector) {
        if(!(0<layer && layer<=BMTConstants.NLAYERS))
            throw new IllegalArgumentException("Error: invalid layer="+layer);
        if(!(0<sector && sector<=BMTConstants.NSECTORS))
            throw new IllegalArgumentException("Error: invalid sector="+sector);
        
        return BMTConstants.TOLOCAL[layer-1][sector-1];
    }
    
    /**
     * Return axis for the selected tile, identified by layer and sector
     * @param layer (1-6)
     * @param sector (1-3)
     * @return Point3D offset: 3D offset
     */
    public Line3D getAxis(int layer, int sector) {
        Line3D axis = new Line3D();
        if(!(0<layer && layer<=BMTConstants.NLAYERS))
            throw new IllegalArgumentException("Error: invalid layer="+layer);
        if(!(0<sector && sector<=BMTConstants.NSECTORS))
            throw new IllegalArgumentException("Error: invalid sector="+sector);
        axis.copy(BMTConstants.AXES[layer-1][sector-1]);
        return axis;
    }
    
    public Point3D toLocal(Point3D global, int layer, int sector) {
        Point3D local = new Point3D(global);
        this.toLocal(layer, sector).apply(local);
        return local;
    }
    
    public Point3D toGlobal(Point3D local, int layer, int sector) {
        Point3D global = new Point3D(local);
        this.toGlobal(layer, sector).apply(global);
        return global;
    }
          
    /**
     * Returns Line3D for Z detector strip identified from region, sector, strip numbers, for ideal geometry
     * @param region
     * @param sector
     * @param strip
     * @return Line3D
     */
    private Line3D getIdealZstrip(int region, int sector, int strip) {
        
        if(!(0<region && region<=3))
            throw new IllegalArgumentException("Error: invalid region="+region);
        
        int layer = this.getLayer(region, BMTType.Z);
        
        double radius = this.getRadiusMidDrift(layer);
        double zmin   = this.getZmin(layer);
        double zmax   = this.getZmax(layer);
        double angle  = this.getZstripPhi(region,sector,strip);
        
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
        
        this.toGlobal(layer, sector).apply(stripline);
        
        return stripline;
    }

    /**
     * Returns Line3D for Z detector pseudostrip identified from region, sector, strip numbers, for ideal geometry
     * After Loentz angle correction
     * @param region
     * @param sector
     * @param strip
     * @param swim
     * @return Line3D
     */
    public Line3D getIdealLCZstrip(int region, int sector, int strip, Swim swim) {
        
        if(!(0<region && region<=3))
            throw new IllegalArgumentException("Error: invalid region="+region);
       
        int layer = this.getLayer(region, BMTType.Z);
        double radius = this.getRadiusMidDrift(layer);
        
        Line3D stripline = this.getZstrip(region, sector, strip);
        double x = stripline.midpoint().x();
        double y = stripline.midpoint().y();
        double z = stripline.midpoint().z();
        double alpha = this.getThetaLorentz(layer, sector, x, y, z, swim);   
        double ralpha = Math.atan2(this.getThickness()/2*Math.tan(alpha), this.getRadius(layer));
//        System.out.println(region + " " + alpha + " " + ralpha);
        
        Point3D np1= new Point3D(radius * Math.cos(ralpha), radius * Math.sin(ralpha), this.getZmin(layer));
        Point3D np2= new Point3D(radius * Math.cos(ralpha), radius * Math.sin(ralpha), this.getZmax(layer));
        Line3D nstripline = new Line3D(np1,np2);
        nstripline.rotateZ(this.getZstripPhi(region, sector, strip));
        
        return nstripline;
    }
    
    /**
     * Returns Line3D for Z detector pseudo-strip identified from region, sector, strip numbers, for real geometry
     * After Lorentz angle correction
     * @param region
     * @param sector
     * @param strip
     * @param swim
     * @return stripline
     */
    public Line3D getLCZstrip(int region, int sector, int strip, Swim swim) {
        
        int layer = this.getLayer(region, BMTType.Z);
        Line3D stripline = this.getIdealLCZstrip(region, sector, strip, swim);
        
        this.toGlobal(layer, sector).apply(stripline);
               
        return stripline;
    }
    
    /**
     * @param region
     * @param sector
     * @param strip
     * @return the phi angle of the strip center in radians
     */
    public double getZstripPhi(int region, int sector, int strip) {
        if(!(0<region && region<=3))
            throw new IllegalArgumentException("Error: invalid region="+region);
        double angle = BMTConstants.getCRZPHI()[region-1][sector-1] - BMTConstants.getCRZDPHI()[region-1][sector-1] 
                     + ((double) strip-0.5) * BMTConstants.getCRZWIDTH()[region-1] / BMTConstants.getCRZRADIUS()[region-1];
        if(Math.abs(angle)>Math.PI) angle -= 2*Math.PI*Math.signum(angle);
        return angle; //in rad 
    }

    /**
     * Return the C detector strip group
     * @param region [1-3]
     * @param strip
     * @return group [1-...]
     */
    private int getCGroup(int region, int strip) {
        if(!(0<region && region<=3))
            throw new IllegalArgumentException("Error: invalid region="+region);
        int group = 0;
        if(strip>0 && strip<=BMTConstants.getCRCNSTRIPS()[region-1]) {
            for(int i=0; i<BMTConstants.getCRCGRPNMAX()[region-1].length; i++) {
                if(strip<=BMTConstants.getCRCGRPNMAX()[region-1][i]) {
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
        if(!(0<region && region<=3))
            throw new IllegalArgumentException("Error: invalid region="+region);
        int group = 0;
        if(z>BMTConstants.getCRCZMIN()[region-1] && z<BMTConstants.getCRCZMAX()[region-1]) {
            for(int i=0; i<BMTConstants.getCRCGRPZMIN()[region-1].length; i++) {
                if(z>BMTConstants.getCRCGRPZMIN()[region-1][i] && z<BMTConstants.getCRCGRPZMAX()[region-1][i]) {
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
    public double getCstripZ(int region, int strip) {
        
        double z = udf;
        
        int group    = getCGroup(region,strip);
        if(group>0) {
            double zmin  = BMTConstants.getCRCGRPZMIN()[region-1][group-1];     // group minimum z
            double pitch = BMTConstants.getCRCWIDTH()[region-1][group-1];       // group pitch
            int    nmin  = BMTConstants.getCRCGRPNMIN()[region-1][group-1];
            z  = zmin + (strip - nmin + 0.5) * pitch ;
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
    private Arc3D getIdealCstrip(int region, int sector, int strip) {
        
        if(!(0<region && region<=3))
            throw new IllegalArgumentException("Error: invalid region="+region);
        
        int layer = this.getLayer(region, BMTType.C);
        
        double radius = this.getRadiusMidDrift(layer);
        double angle  = this.getPhi(layer, sector) - this.getDPhi(layer, sector);
        double theta  = this.getDPhi(layer, sector)*2;
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
        
        this.toGlobal(layer, sector).apply(arcline);
        
        return arcline;
    }
    
    public Cylindrical3D getTileSurface(int layer, int sector) {
        double phMin  = this.getPhi(layer, sector)-this.getDPhi(layer, sector);
        double radius = this.getRadiusMidDrift(layer);
        Point3D origin = new Point3D(radius*Math.cos(phMin), 
                                     radius*Math.sin(phMin),
                                     this.getZmin(layer));
        Point3D  center = new Point3D(0,0,this.getZmin(layer));
        Vector3D axis   = new Vector3D(0,0,1);
        Arc3D base = new Arc3D(origin, center, axis, 2*this.getDPhi(layer, sector));
        Cylindrical3D surface = new Cylindrical3D(base, this.getZmax(layer)-this.getZmin(layer));
        this.toGlobal(layer, sector).apply(surface);
        return surface;
    }
    
    /**
     * Return the sector number
     * @param layer [0-6]
     * @param localAngle track angle in the local frame in radians
     * @return sector [1-3] (not) accounting for dead areas if layer (0) [1-6] or 0 if layer is undefined
     */
    public int getSector(int layer, double localAngle) {
        boolean full = false;
        if(layer==0) {
            full  = true;
            layer = 1;
        }
        int region = this.getRegion(layer);   
        
        Vector3D vec = new Vector3D(Math.cos(localAngle),Math.sin(localAngle),0);
        if(Double.isNaN(localAngle)) vec = null;
        int sector = 0;
        double width = 0.5; // Math.cos(60deg);
        double delta = -1;
        for (int i = 0; i < BMTConstants.NSECTORS; i++) {
            double phi      = this.getPhi(layer, i+1); 
            Vector3D center = new Vector3D(Math.cos(phi),Math.sin(phi),0);           
            double dcosphi  = center.dot(vec);
            if(dcosphi>width) {
                delta  = dcosphi; 
                sector = i+1; 
            }
        } 
        if(!full) {
            if(sector!=0)
                if(delta<Math.cos(this.getDPhi(layer, sector))) sector=0;
        } 
        return sector;
    }
    
    
    /**
     * Return the sector number
     * @param layer [0-6]
     * @param localTraj trajectory point in the local frame
     * @return sector [1-3] (not) accounting for dead areas if layer (0) [1-6] or 0 if layer is undefined
     */
    public int getSector(int layer, Point3D localTraj)  {
        return this.getSector(layer, Math.atan2(localTraj.y(), localTraj.x()));
    }
    /**
     * Return the layer number
     * @param localtraj point on one of the detector surfaces
     * @param strip2Det
     * @return layer [1-6] or 0 if undefined
     */
    public int getLayer(Point3D localtraj, double strip2Det) {
        int layer=0;
        
        int sector = this.getSector(0, Math.atan2(localtraj.y(), localtraj.x()));
        if(sector ==0) return 0;
        for(int i=1; i<=BMTConstants.NLAYERS; i++) {
            double radius = BMTConstants.AXES[i-1][sector-1].distance(localtraj).length();
            if(Math.abs(radius-this.getRadiusMidDrift(i)-strip2Det)<accuracy) {
                layer = i;
                break;
            }
        }
        return layer;
    }

    /**
     * Checks whether a trajectory point is within the active area of a tile
     * @param layer
     * @param sector
     * @param traj
     * @return true/false
     */
    public boolean inDetector(int layer, int sector, Point3D traj) {
       return this.getTileSurface(layer, sector).isOnSurface(traj);
    }

    /**
     * Checks whether a trajectory point is within the active area of a tile
     * @param traj
     * @return true/false
     */
    public int getStrip(Point3D traj, double strip2Det) {
        int strip = 0;
        int layer = getLayer(traj, strip2Det);
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
        if(layer>0 && layer<=BMTConstants.NLAYERS && sector>0 && sector<=BMTConstants.NSECTORS) {
            Point3D local = this.toLocal(traj, layer, sector);
            strip = this.getStripLocal(layer, local);
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
        
        BMTType type = BMTGeometry.getDetectorType(layer);
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
            double zmin  = BMTConstants.getCRCGRPZMIN()[region-1][group-1];
            double pitch = BMTConstants.getCRCWIDTH()[region-1][group-1];
            strip = (int) Math.floor((traj.z()-zmin)/pitch);
            if(group>0) strip += BMTConstants.getCRCGRPNMIN()[region-1][group-1];
        }
        return strip;
    }
    
    /**
     * Return the number of the closest strip to the given trajectory point
     * in the detector local frame
     * @param region (1-3)
     * @param localTraj trajectory point on the layer surface in the local frame
     * @return strip number (0 if the point is not within the active area)
     */
    public int getZstrip(int region, Point3D localTraj) {
        
        int strip = 0;
        
        int layer = getLayer(region, BMTType.Z);
        
        double angle = Math.atan2(localTraj.y(), localTraj.x());
        if(angle<0) angle += 2*Math.PI;
        
        int sector = getSector(layer,angle);
        if(sector>=1 && sector <=BMTConstants.NSECTORS) {
            // CHECKME
            double edge   = this.getPhi(layer, sector) - this.getDPhi(layer, sector); // 30 150 270
            double pitch  = this.getZPitch(region,1);
            double radius = this.getRadiusMidDrift(layer);
            double dphi = angle - edge; 
            if(dphi<0) dphi += 2*Math.PI;
            strip = (int) Math.floor(dphi*radius/pitch) + 1;
            if (strip < 1 || strip > this.getNStrips(layer)) {
                strip = -1;
            }
        }
        return strip;

    }
    
    public double getPhi(int layer, int sector, Point3D traj) {
        Point3D local = this.toLocal(traj, layer, sector);
        return local.toVector3D().phi();
    }
     /**
     * Calculate Theta Lorentz based on solenoid scale and drift settings
     * @param layer
     * @param sector
     * @return thetaL in radians
     */
    public double getThetaLorentz(int layer, int sector) {
         
        if(!(0<layer && layer<=BMTConstants.NLAYERS))
            throw new IllegalArgumentException("Error: invalid layer="+layer);
        if(!(0<sector && sector<=BMTConstants.NSECTORS))
            throw new IllegalArgumentException("Error: invalid sector="+sector);

        double thetaL = 0;
        
        double solenoidScale = Constants.getSolenoidScale();
        
        if(Math.abs(solenoidScale)<0.001) {
            thetaL = 0;
        }
        else {
            if(Math.abs(solenoidScale)<0.8) {
                thetaL = Math.toRadians(getLorentzAngle(E_DRIFT_MF[layer-1][sector-1],Math.abs(solenoidScale*50)));
            } else {
                thetaL = Math.toRadians(getLorentzAngle(E_DRIFT_FF[layer-1][sector-1],Math.abs(solenoidScale*50)));
            }
        }
        if (solenoidScale<0) thetaL=-thetaL; 
        return thetaL;
    }

    /**
     * Calculate Theta Lorentz based on solenoid scale and drift settings
     * @param layer
     * @param sector
     * @param x
     * @param y
     * @param z
     * @param swim
     * @return thetaL in radians
     */
    public double getThetaLorentz(int layer, int sector, double x, double y, double z, Swim swim) {
         
        if(!(0<layer && layer<=BMTConstants.NLAYERS))
            throw new IllegalArgumentException("Error: invalid layer="+layer);
        if(!(0<sector && sector<=BMTConstants.NSECTORS))
            throw new IllegalArgumentException("Error: invalid sector="+sector);

        double thetaL = 0;
        float[] b = new float[3];
        swim.BfieldLab(x/10, y/10, z/10, b);
        double EDrift=0;
        if(Math.abs(Constants.getSolenoidScale())<0.8) {
            EDrift = BMTConstants.E_DRIFT_MF[layer-1][sector-1];
        } else {
            EDrift = BMTConstants.E_DRIFT_FF[layer-1][sector-1];
        }
        if(Math.abs(Constants.getSolenoidScale())<0.001) {
            thetaL = 0;
        }
        else {
            thetaL = Math.toRadians(getLorentzAngle(EDrift,Math.abs(b[2]*10)));
        }
        if (Constants.getSolenoidScale()<0) thetaL=-thetaL; 
        return thetaL;
    }

    /**
     * Return track vector for local angle calculations
     * 
     * 1) transform to the geometry service local frame first,
     * 2) rotates to bring the track intersection at phi=90.
     * 
     * The final frame has:
     * - z along the axis of the tile cylinder
     * - y perpendicular to the surface
     * 
     * The y and x components determine the local angle for Z strips
     * The x and z components determine the local angle for C strips
     * @param layer
     * @param sector
     * @param trackPos
     * @param trackDir
     * @return track direction unit vector
    **/
    public Vector3D getLocalTrack(int layer, int sector, Point3D trackPos, Vector3D trackDir) {               
        Vector3D dir = new Vector3D(trackDir).asUnit();
        Point3D  pos = new Point3D(trackPos);
        this.toLocal(layer, sector).apply(dir);
        this.toLocal(layer, sector).apply(pos);
        Vector3D n = pos.toVector3D().asUnit();
        dir.rotateZ(-n.phi()+Math.PI/2);
        return dir;
    }
    
    /**
     * Returns the local angle of the track for Z detectors
     * the angle is positive for tracks going toward positive phi
     * @param layer
     * @param sector
     * @param trackPos
     * @param trackDir
     * @return local angle
     */
    public double getThetaZ(int layer, int sector, Point3D trackPos, Vector3D trackDir) { 
        Vector3D dir = this.getLocalTrack(layer, sector, trackPos, trackDir);
        return Math.atan(dir.x()/dir.y());
    }
    
    /**
     * Returns the local angle of the track for C detectors
     * the angle is positive for tracks going at positive z
     * @param layer
     * @param sector
     * @param trackPos
     * @param trackDir
     * @return local angle
     */
    public double getThetaC(int layer, int sector, Point3D trackPos, Vector3D trackDir) { 
        Vector3D dir = this.getLocalTrack(layer, sector, trackPos, trackDir);
        return Math.atan(dir.z()/dir.y());
    }
    
    public double getLocalAngle(int layer, int sector,  Point3D trackPos, Vector3D trackDir) {
        if(getDetectorType(layer) == BMTType.C)
            return this.getThetaC(layer, sector, trackPos, trackDir);
        else 
            return this.getThetaZ(layer, sector, trackPos, trackDir);
    }
    
    public List<Surface> getSurfaces() {
        List<Surface> surfaces = new ArrayList<>();
        for(int i=1; i<=NLAYERS; i++)
            surfaces.add(this.getSurface(i, 1, new Strip(0, 0, 0)));
        return surfaces;
    }
    
    public Surface getSurfaceC(int layer, int sector, int stripId, double centroid, double centroidValue) {
        Strip strip = new Strip(stripId, centroid, centroidValue);
        Surface surface = this.getSurface(layer, sector, strip);
        return surface;
    }
    
    public Surface getSurfaceZ(int layer, int sector, int stripId, double centroid, double x, double y, double centroidValue) {
        Strip strip = new Strip(stripId, centroid, x, y, centroidValue);
        Surface surface = this.getSurface(layer, sector, strip);
        return surface;
    }
    
    public Surface getSurface(int layer, int sector, Strip strip) {
        Surface surface = new Surface(this.getTileSurface(layer, sector), 
                                      strip, 
                                      Constants.SWIMACCURACYBMT);
        surface.hemisphere = 0;
        surface.setLayer(layer);
        surface.setSector(sector);
        surface.setTransformation(this.toGlobal(layer, sector));
        surface.setError(0); 
        for(String key : BMTConstants.getMaterials().keySet()) {
            double[] p = BMTConstants.getMaterials().get(key);
            surface.addMaterial(key, p[0], p[1], p[2], p[3], p[4], Units.MM);
        }
        surface.passive=false;
        return surface;
    }
    
    public Surface getInnerTube() {
        Point3D origin = new Point3D(INNERTUBEDIM[0], 0, INNERTUBEDIM[3]-INNERTUBEDIM[2]);
        Point3D  center = new Point3D(0,0,INNERTUBEDIM[3]-INNERTUBEDIM[2]);
        Vector3D axis   = new Vector3D(0,0,1);
        Arc3D base = new Arc3D(origin, center, axis, 2*Math.PI);
        Cylindrical3D tube = new Cylindrical3D(base, 2*INNERTUBEDIM[2]);
        Surface surface = new Surface(tube, new Strip(0,0,0), Constants.DEFAULTSWIMACC);
        surface.addMaterial("CarbonFiber", INNERTUBEDIM[1]-INNERTUBEDIM[0],
                            TUBEMAT[0], TUBEMAT[1], TUBEMAT[2], TUBEMAT[3], Units.MM);
        surface.passive=true;
        return surface;
    }
    
    public Surface getOuterTube() {
        Point3D origin = new Point3D(OUTERTUBEDIM[0], 0, OUTERTUBEDIM[3]-OUTERTUBEDIM[2]);
        Point3D  center = new Point3D(0,0,OUTERTUBEDIM[3]-OUTERTUBEDIM[2]);
        Vector3D axis   = new Vector3D(0,0,1);
        Arc3D base = new Arc3D(origin, center, axis, 2*Math.PI);
        Cylindrical3D tube = new Cylindrical3D(base, 2*OUTERTUBEDIM[2]);
        Surface surface = new Surface(tube, new Strip(0,0,0), Constants.DEFAULTSWIMACC);
        surface.addMaterial("CarbonFiber", OUTERTUBEDIM[1]-OUTERTUBEDIM[0],
                            TUBEMAT[0], TUBEMAT[1], TUBEMAT[2], TUBEMAT[3], Units.MM);
        surface.passive=true;
        return surface;
    }
    
    /**
     * Executable method: implements checks
     * @param arg
     */
    public static void main (String arg[]) {
        
        CCDBConstantsLoader.Load(new DatabaseConstantProvider(11, "default"));
        
        
        BMTGeometry newGeo = new BMTGeometry();
        
        System.out.println("\nLayer number for region and detector type:");
        System.out.println("\tRegion\tType\tLayer");
        for(int i=1; i<=BMTConstants.NREGIONS; i++) {
            System.out.println("\t" + i + "\t" + newGeo.getLayer(i, BMTType.C) + "\t" + newGeo.getLayer(i, BMTType.Z));
        }

        System.out.println("\nDetector information by layer:");
        System.out.println("\tLayer\tRegion\tType\tRadius(mm)\tThickness(mm)\tZmin(mm)\tZmax(mm)\tNNstrips");
        for(int i=1; i<=BMTConstants.NLAYERS; i++) {
            System.out.println("\t" + i + "\t"   + newGeo.getRegion(i) + "\t"   + newGeo.getDetectorType(i) 
                                        + "\t"   + newGeo.getRadius(i) + "\t\t" + newGeo.getThickness() 
                                        + "\t\t" + newGeo.getZmin(i)   + "\t\t" + newGeo.getZmax(i)
                                        + "\t\t" + newGeo.getNStrips(i)
                                );
        }

        System.out.println("\nOffsets and Rotations");
        System.out.println("\tLayer\tSector\tOffset and Rotation");
        for(int i=1; i<=BMTConstants.NLAYERS; i++) {
            for(int j=1; j<=BMTConstants.NSECTORS; j++) {
                System.out.println("\t" + i + "\t" + j + "\t" + newGeo.getOffset(i, j).toString() + "\t" + newGeo.getRotation(i, j).toString());
            }
        }

        System.out.println("\nPlotting acceptance");
        DataGroup acceptance = new DataGroup(3,2);
        for(int i=1; i<=BMTConstants.NLAYERS; i++) {
            double radius = newGeo.getRadius(i);
            double zmin   = newGeo.getZmin(i);
            double zmax   = newGeo.getZmax(i);
            H2F hi_acc = new H2F("hi_acc","Layer "+i, 50, -200, 300, 120, 0, 360);
            acceptance.addDataSet(hi_acc, i-1);
            for(int j=0; j<5000; j++) {
                double z   = Math.random()*(zmax-zmin)+zmin;
                double phi = Math.random()*2*Math.PI;
                Point3D traj = new Point3D(radius*Math.cos(phi),radius*Math.sin(phi),z);
//                if(i!=newGeo.getLayer(traj,0)) System.out.println("Error in getLayer");
                if(newGeo.getSector(i, phi)>0)
                    if(newGeo.getTileSurface(i, newGeo.getSector(i, phi)).isOnSurface(traj)) 
                        hi_acc.fill(z, Math.toDegrees(phi));
            }
        }
        
        DataGroup dgBMT = new DataGroup(4, 3);
        for (int i = 0; i < 3; i++) {
            int region = i+1;
            H2F hiz_res_z = new H2F("hiz_res_z" + region, "BMTZ R" + region, 100, -150, 150, 100, -1.5, 1.5);
            hiz_res_z.setTitleX("z (cm)");
            hiz_res_z.setTitleY("Residual (mm)");
            H1F hiz_res = new H1F("hiz_res" + region, "BMTZ R" + region, 500, -5, 5);
            hiz_res.setTitleX("Residual (mm)");
            hiz_res.setTitleY("Counts");
            H2F hic_res_z = new H2F("hic_res_z" + region, "BMTC R" + region, 100, -150, 150, 100, -1.5, 1.5);
            hic_res_z.setTitleX("z (cm)");
            hic_res_z.setTitleY("Residual (mm)");
            H1F hic_res = new H1F("hic_res" + region, "BMTC R" + region, 500, -5, 5);
            hic_res.setTitleX("Residual (mm)");
            hic_res.setTitleY("Counts");
            dgBMT.addDataSet(hiz_res_z, 0 + i * 4);
            dgBMT.addDataSet(hiz_res,   1 + i * 4);
            dgBMT.addDataSet(hic_res_z, 2 + i * 4);
            dgBMT.addDataSet(hic_res,   3 + i * 4);
        }
        
        
        HipoDataSource reader = new HipoDataSource();
        reader.open("/Users/devita/Work/clas12/simulations/bmt/out.rec.hipo");
        while (reader.hasEvent() == true) {
            DataEvent event = reader.getNextEvent();
            DataBank bmtADC = null;
            DataBank bmtHit = null;
            DataBank bmtCluster = null;
            DataBank bmtCross = null;
            DataBank mcTrue = null;
            DataBank mcPart = null;

            if (event.hasBank("BMT::adc")) {
                bmtADC = event.getBank("BMT::adc");
            }
            if (event.hasBank("BMTRec::Hits")) {
                bmtHit = event.getBank("BMTRec::Hits");
            }
            if (event.hasBank("BMTRec::Clusters")) {
                bmtCluster = event.getBank("BMTRec::Clusters");
            }
            if (event.hasBank("BMTRec::Crosses")) {
                bmtCross = event.getBank("BMTRec::Crosses");
            }
            if (event.hasBank("MC::Particle")) {
                mcPart = event.getBank("MC::Particle");
            }
            if (event.hasBank("MC::True")) {
                mcTrue = event.getBank("MC::True");
            }        
        
            if (bmtCluster != null && bmtHit != null && bmtADC != null && mcPart != null && mcTrue != null) {
                Vector3D dir = new Vector3D(mcPart.getFloat("px", 0),mcPart.getFloat("py", 0),mcPart.getFloat("pz", 0));
                Line3D particle = new Line3D();
                particle.set(new Point3D(mcPart.getFloat("vx", 0),mcPart.getFloat("vy", 0),mcPart.getFloat("vz", 0)), dir);
                
                for (int j = 0; j < bmtHit.rows(); j++) {
                    int id     = bmtHit.getShort("ID", j);
                    int sector = bmtHit.getByte("sector", j);
                    int layer  = bmtHit.getByte("layer", j);
                    int strip  = bmtHit.getInt("strip", j);
                    int iclus  = bmtHit.getShort("clusterID", j)-1;
                    int region = newGeo.getRegion(layer);
                    int seed   = bmtCluster.getInt("seedStrip", iclus);
                    double centroid = bmtCluster.getFloat("centroid", iclus);
                    double measure  = bmtCluster.getFloat("centroidValue", iclus);
                    
                    ArrayList<Point3D> trajs = new ArrayList<>();
                    newGeo.getTileSurface(layer,sector).intersection(particle, trajs);
                    if(trajs.size()==0) continue;
                    newGeo.toLocal(layer, sector).apply(trajs.get(0));
 //                   for(Point3D traj : trajs) System.out.println(layer + " " + sector + " " + newGeo.getRadius(layer) + " " + traj.toString());
                    
                    if(BMTGeometry.getDetectorType(layer) == BMTType.Z) {
//                        measure=newGeo.getZstripPhi(region, sector, seed);
                        double residual=Math.atan2(trajs.get(0).y(),trajs.get(0).x())-measure;
                        if(Math.abs(residual)>Math.PI) residual-=Math.signum(residual)*2*Math.PI;
                        dgBMT.getH1F("hiz_res" + region).fill(residual);
//                              System.out.println(newGeo.getCylinder(layer, sector).getAxis().distance(hit).length() + " " + newGeo.getRadius(layer));
                        dgBMT.getH2F("hiz_res_z" + region).fill(trajs.get(0).z(), residual);
                    }
                    else {
//                        measure=newGeo.getCstripZ(region, strip);
                        double residual=trajs.get(0).z()-measure;
                        dgBMT.getH1F("hic_res" + region).fill(residual);
                        dgBMT.getH2F("hic_res_z" + region).fill(trajs.get(0).z(), residual);
                    }
//                    for (int k = 0; k < mcTrue.rows(); k++) {
//                        if (mcTrue.getInt("hitn", k) == id && mcTrue.getByte("detector", k) == DetectorType.BMT.getDetectorId()) {
//                            double xTrue = mcTrue.getFloat("avgX", k);
//                            double yTrue = mcTrue.getFloat("avgY", k);
//                            double zTrue = mcTrue.getFloat("avgZ", k);
//                            Point3D hit = new Point3D(xTrue, yTrue, zTrue);
////                            Line3D hitR = newGeo.getAxis(layer, sector).distance(hit);
////                            Line3D hitR = new Line3D(new Point3D(0,0,0), hit);
////                            hit = hitR.lerpPoint(newGeo.getRadius(layer)/(newGeo.getRadius(layer)+newGeo.getThickness()/2));
//                            double residual = -newGeo.getResidual(trajs.get(0), layer, sector, seed);
//                            if(BMTGeometry.getDetectorType(layer) == BMTType.Z) {
////                                Line3D strip = newGeo.getZstrip(region, sector, component);                       
////                                Line3D dist  = strip.distance(hit);
//                                dgBMT.getH1F("hiz_res" + region).fill(residual);
//  //                              System.out.println(newGeo.getCylinder(layer, sector).getAxis().distance(hit).length() + " " + newGeo.getRadius(layer));
//                                dgBMT.getH2F("hiz_res_z" + region).fill(zTrue, residual);
//                            }
//                            else {
//                                dgBMT.getH1F("hic_res" + region).fill(residual);
//                                dgBMT.getH2F("hic_res_z" + region).fill(zTrue, residual);
//                            }
//                        }
//                    }
                }
            }
        }
        
        JFrame frame = new JFrame("BMT geometry");
        frame.setSize(1200, 800);
        EmbeddedCanvasTabbed canvas = new EmbeddedCanvasTabbed("BMT");
        canvas.getCanvas("BMT").draw(dgBMT);
        frame.add(canvas);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
    
}
