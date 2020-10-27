package org.jlab.rec.cvt.bmt;

import org.jlab.geom.prim.Arc3D;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;

/**
 *
 * @author devita
 */
public class BMTgeometry {

    private final static int[] lZ = { 2, 3, 5};
    private final static int[] lC = { 1, 4, 6}; 

        
    /**
     *
     */
    public BMTgeometry() {
    }
    
    /**
     * Return layer number for a given region and detector type
     * @param region (1-3)
     * @param detector (C or Z)
     * @return layer (1-6) 
     */
    public int getLayer(int region, String detector) {
    	int layer = -1;
        if(region>=1 && region<=3) {
            if( detector.equalsIgnoreCase("Z") ) {
                    layer = lZ[ region - 1 ];
            }
            if( detector.equalsIgnoreCase("C") ) {
                    layer = lC[ region - 1 ];
            }
        }
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
    	return region;
    }


    /**
     * Return region number for a given layer
     * @param layer (1-6)
     * @return type ("C" or "Z");
     */
    public String getDetectorType(int layer) {
    	if(layer == 1 || layer == 4 || layer == 6) return "C";
        else if(layer == 2 || layer == 3 || layer == 5) return "Z";
        else return null;
    }
    
    
    /**
     * Return radius of the selected layer
     * @param layer (1-6)
     * @return radius (=0 if layer is out of range)
     */
    public double getRadius(int layer) {
        
        int region = this.getRegion(layer);
        String det = this.getDetectorType(layer);
        
        double radius = 0;
        if(region>0 && det!=null) {
            if     (det.equalsIgnoreCase("C")) radius = Constants.getCRCRADIUS()[region-1];
            else if(det.equalsIgnoreCase("Z")) radius = Constants.getCRZRADIUS()[region-1];
        }
        return radius;
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
        offset.translateXYZ(0, 0, org.jlab.rec.cvt.Constants.getZoffset());
        
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
        double angle  = Constants.getCRZEDGE1()[region-1][sector-1] + ((double) strip-0.5) * Constants.getCRZWIDTH()[region-1] / Constants.getCRZRADIUS()[region-1];
        
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
        
        int layer = this.getLayer(region, "Z");
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
     * Returns the Z position of the selected C-detector strip in the local frame
     * @param region
     * @param strip 
     * @return zc
     */
    public double getCstripZ(int region, int strip) {

        //For CR6C, this function returns the Z position of the strip center
        int group = 0;
        int limit = Constants.getCRCGROUP()[region-1][group];
        double zc = Constants.getCRCZMIN()[region-1];

        if (strip > 1) {
            for (int j = 1; j < strip; j++) {
                zc += Constants.getCRCWIDTH()[region-1][group];
                if (j >= limit) { //test if we change the width
                    group++;
                    limit += Constants.getCRCGROUP()[region-1][group];
                }
              }
        }
        zc += Constants.getCRCWIDTH()[region-1][group]/2.;
        return zc; //in mm
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
        double angle  = Constants.getCRCEDGE1()[region-1][sector-1] + Constants.getCRCXPOS()[region-1] / Constants.getCRCRADIUS()[region-1];
        double theta  = (Constants.getCRCXPOS()[region-1]+Constants.getCRCLENGTH()[region-1]) / Constants.getCRCRADIUS()[region-1];
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

        int layer = this.getLayer(region, "C");
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
     * Return the number of the closest strip to the given trajectory point
     * Detector mis-alignments geometry are taken into account by transforming 
     * the trajectory point to detector local frame
     * @param layer (1-6)
     * @param sector (1-3)
     * @param traj trajectory point on the layer surface in the lab
     * @return strip number (0 if the point is not within the active area)
     */
    public int getStrip(int layer, int sector, Point3D traj) {
        
        Point3D    offset = this.getOffset(layer, sector);
        Vector3D rotation = this.getRotation(layer, sector);
        traj.translateXYZ(-offset.x(), -offset.y(), -offset.z());
        traj.rotateZ(-rotation.z());
        traj.rotateY(-rotation.y());
        traj.rotateX(-rotation.x());
        
        return this.getStripLocal(layer, sector, traj);
    }
    
    /**
     * Return the number of the closest strip to the given trajectory point
     * in the detector local frame
     * @param layer (1-6)
     * @param sector (1-3)
     * @param traj trajectory point on the layer surface in the local frame
     * @return strip number (0 if the point is not within the active area)
     */
    public int getStripLocal(int layer, int sector, Point3D traj) {
        
        String type = this.getDetectorType(layer);
        
        if(type == null) return 0;
        
        if(type.equalsIgnoreCase("C")) {
            return this.getCstrip(layer,traj);
        }
        else if(type.equalsIgnoreCase("Z")) {
            return this.getZstrip(layer,traj);
        }
        else return 0;        
    }

    /**
     * Return the number of the closest strip to the given trajectory point
     * in the detector local frame
     * @param layer (1-6)
     * @param sector (1-3)
     * @param traj trajectory point on the layer surface in the local frame
     * @return strip number (0 if the point is not within the active area)
     */
    public int getCstrip(int region, Point3D traj) {
        
        int strip = 0;
        return strip;
    }
    
    /**
     * Return the number of the closest strip to the given trajectory point
     * in the detector local frame
     * @param layer (1-6)
     * @param sector (1-3)
     * @param traj trajectory point on the layer surface in the local frame
     * @return strip number (0 if the point is not within the active area)
     */
    public int getZstrip(int region, Point3D traj) {
        
        int strip = 0;
        return strip;
    }
   
    
    
}
