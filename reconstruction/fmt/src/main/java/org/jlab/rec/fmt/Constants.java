package org.jlab.rec.fmt;

import org.jlab.geom.base.Detector;
import org.jlab.geom.detector.fmt.FMTLayer;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Transformation3D;
import org.jlab.geom.prim.Vector3D;

/**
 *
 * @author devita
 */
public class Constants {
    
    public static final int NLAYERS = 6;
    
    // DC-tracks to FMT-clusters matching parameter
    public static double CIRCLECONFUSION = 1.2; // cm

    // min path for final swimming to beamline to reject failed swimming
    public static double MIN_SWIM_PATH = 0.2; 
    
    // small distance (cm) for derivatives calculations
    public static double EPSILON = 1e-4;
    
    public static int MAX_NB_CROSSES = 30;
    
    private static Detector fmtDetector = null;

    
    
    public static void setDetector(Detector detector) {
        Constants.fmtDetector = detector;
    }
    
    public static Vector3D getDerivatives(int layer, double x, double y, double z) {
        Vector3D p0 = new Vector3D(x,y,z);
        Vector3D p1 = new Vector3D(x,y+Constants.EPSILON,z);
        Constants.getInverseTransform(layer).apply(p0);
        Constants.getInverseTransform(layer).apply(p1);
        return p1.sub(p0).divide(Constants.EPSILON);
    }
    
    public static FMTLayer getLayer(int layer) {
        if(layer<1 || layer>NLAYERS)
            throw new IllegalArgumentException("Error: invalid layer="+layer);
        return (FMTLayer) Constants.fmtDetector.getSector(0).getSuperlayer(0).getLayer(layer-1);
    }
    
    public static double getPitch() {
        return Constants.getLayer(1).getComponent(0).getWidth();
    }
    
    public static Line3D getStrip(int layer, int strip) {
        if(strip<1 || strip>Constants.getLayer(layer).getNumComponents())
            throw new IllegalArgumentException("Error: invalid strip="+strip);
        return Constants.getLayer(layer).getComponent(strip-1).getLine();
    }
    
    public static Line3D getLocalStrip(int layer, int strip) {
        Line3D local = new Line3D(Constants.getStrip(layer, strip));
        Constants.getInverseTransform(layer).apply(local);
        return local;
    }
    
    public static double getThickness() {
        return Constants.getLayer(1).getComponent(0).getThickness();
    }
    
    public static Transformation3D getTransform(int layer) {
        return Constants.getLayer(layer).getTransformation();
    }

    public static Transformation3D getInverseTransform(int layer) {
        Transformation3D inverse = new Transformation3D(Constants.getTransform(layer));
        return inverse.inverse();
    }

    /**
     *
     * @param layer
     * @param strip1
     * @param strip2
     * @return a boolean comparing 2 hits based on basic descriptors; returns
     * true if the hits are the same
     */
    public static boolean areClose(int layer, int strip1, int strip2) {
        Point3D p0 = Constants.getLocalStrip(layer, strip1).midpoint();
        Point3D p1 = Constants.getLocalStrip(layer, strip2).midpoint();
        if(Math.round(Math.abs(p0.y()-p1.y())/Constants.getPitch())==1 && 
           Math.abs(Math.signum(p0.x())-Math.signum(p1.x()))<=1) {
            return true;
        }
        else return false;
    }    
    
    public static Point3D toLocal(int layer, Point3D p) {
        Point3D local = new Point3D(p);
        Constants.getInverseTransform(layer).apply(local);
        return local;
    }

    public static Point3D toLocal(int layer, double x, double y, double z) {
        Point3D local = new Point3D(x,y,z);
        Constants.getInverseTransform(layer).apply(local);
        return local;
    }

}
