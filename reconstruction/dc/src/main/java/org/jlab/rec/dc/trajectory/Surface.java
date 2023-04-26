package org.jlab.rec.dc.trajectory;

import org.jlab.detector.base.DetectorType;
import org.jlab.geom.prim.Arc3D;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Plane3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Sector3D;
import org.jlab.geom.prim.Trap3D;
import org.jlab.geom.prim.Triangle3D;
import org.jlab.geom.prim.Vector3D;

/**
 *
 * @author ziegler, devita
 */
public class Surface {
    
    private DetectorType  detectorType;
    private int           detectorSector;
    private int           detectorLayer;

    private Plane3D plane;    
    private Contour contour;
    private double  thickness;
    
    public Surface(DetectorType type, int sector, int layer) {
        this.detectorType   = type;
        this.detectorSector = sector;
        this.detectorLayer  = layer;
    }

    public Surface(DetectorType type, int sector, int layer, Plane3D plane) {
        this.detectorType   = type;
        this.detectorSector = sector;
        this.detectorLayer  = layer;
        this.plane          = plane;
    }

    public Surface(DetectorType type, int sector, int layer, Plane3D plane, Contour contour, double thickness) {
        this(type, sector, layer, plane);
        this.contour   = contour;
        this.thickness = thickness;
    }
    
    public Surface(DetectorType type, int sector, int layer, Triangle3D triangle, double thickness) {
        this(type, sector, layer, triangle.plane());
        this.contour   = new TriangleContour(triangle);
        this.thickness = thickness;
    }
        
    public Surface(DetectorType type, int sector, int layer, Trap3D trapezoid, double thickness) {
        this(type, sector, layer, trapezoid.plane());
        this.contour   = new TrapContour(trapezoid);
        this.thickness = thickness;
    }
        
    public Surface(DetectorType type, int sector, int layer, Sector3D arc, double thickness) {
        this(type, sector, layer, arc.plane());
        this.contour   = new ArcContour(arc);
        this.thickness = thickness;
    }
    
    public Surface (DetectorType type, int layer, double d, double nx, double ny, double nz) {
        detectorType  = type;
        detectorSector = 0;
        detectorLayer = layer;
        plane = new Plane3D(nx*d, ny*d, nz*d, nx,ny,nz);
    }    

    public Plane3D getPlane() {
        return plane;
    }

    public void setPlane(Plane3D plane) {
        this.plane = plane;
    }

    public Contour getContour() {
        return contour;
    }

    public void setContour(Contour contour) {
        this.contour = contour;
    }

    public double getThickness() {
        return thickness;
    }

    public void setThickness(double thickness) {
        this.thickness = thickness;
    }
    
    public DetectorType getDetectorType() {
        return detectorType;
    }
    
    public void setDetectorType(DetectorType type) {
        this.detectorType = type;
    }

    public int getDetectorSector() {
        return detectorSector;
    }

    public void setDetectorSector(int detectorSector) {
        this.detectorSector = detectorSector;
    }
    
    public int getDetectorLayer() {
        return detectorLayer;
    }
    
    public void setDetectorLayer(int layer) {
        this.detectorLayer = layer;
    }
    
    public boolean isInside(Point3D point) {
        if(contour != null)
            return this.contour.isInside(point);
        else
            return true;
    }

    public double distanceFromEdge(double x, double y, double z) {
        return this.distanceFromEdge(new Point3D(x, y, z));
    }
    
    public double distanceFromEdge(Point3D point) {
        if(contour != null) 
            return this.contour.distanceFromEdge(point);
        else
            return -99;
    }
    
    public double distanceFromPlane(Point3D point) {
        if(contour != null) 
            return this.contour.distanceFromPlane(point);
        else
            return -99;
    }
    
    public double distanceFromPlane(double x, double y, double z) {
        return this.distanceFromPlane(new Point3D(x, y, z));
    }
    
    public Vector3D vectorToPlane(Point3D point) {
        if(contour != null) 
            return this.contour.vectorToPlane(point);
        else
            return null;
    }
    
    public Vector3D vectorToPlane(double x, double y, double z) {
        return this.vectorToPlane(new Point3D(x, y, z));
    }
    
    public double dx(Point3D point, Vector3D dir) {
        if(this.isInside(point)) {
            double cosdir = dir.dot(this.plane.normal());
            if(cosdir!=0)
                return this.thickness/cosdir;
            else
                return 0;
        }
        return 0;
    }
    
    public double getD() {
        return this.plane.point().toVector3D().dot(this.plane.normal());
    }

    public Vector3D getNormal() {
        return this.plane.normal();
    }

    
    @Override
    public String toString() {
        String s = "Detector: " + this.getDetectorType().getName() + " sector: " + this.getDetectorSector() + " layer: " + this.getDetectorLayer();
        s += "\n\t Plane: "     + this.getPlane().toString();
        s += "\n\t Distance: "  + this.getPlane().point().vectorFrom(new Point3D(0,0,0)).dot(this.getPlane().normal());
        s += "\n\t Thickness: " + this.getThickness();
        if(this.getContour()!=null) {
            s += "\n\t Contour: " + this.getContour().toString();
        }
        return s;
    }
    
    
    public abstract class Contour {
        
        public abstract boolean isInside(Point3D p);

        public abstract double distanceFromEdge(Point3D p);

        public abstract double distanceFromPlane(Point3D p);

        public abstract Vector3D vectorToPlane(Point3D p);
    }
    
    public class TrapContour extends Contour{
        
        private Trap3D contour;

        public TrapContour(Trap3D contour) {
            this.contour = contour;
        }

        @Override
        public boolean isInside(Point3D p) {
            return this.contour.isInside(p);
        }

        @Override
        public double distanceFromEdge(Point3D p) {
            return this.contour.distanceFromEdge(p);
        }             
    
        @Override
        public double distanceFromPlane(Point3D p) {
            Line3D line = new Line3D();
            this.contour.plane().distance(p, line);
            return line.length();
        }

        @Override
        public Vector3D vectorToPlane(Point3D p) {
            Line3D line = new Line3D();
            this.contour.plane().distance(p, line);
            return line.direction().asUnit();            
        }

        @Override
        public String toString() {
            return this.contour.toString();
        } 
    }
    
    public class TriangleContour extends Contour {
        
        private Triangle3D contour;

        public TriangleContour(Triangle3D contour) {
            this.contour = contour;
        }

        @Override
        public boolean isInside(Point3D p) {
            return this.contour.isInside(p);
        }

        @Override
        public double distanceFromEdge(Point3D p) {
            return this.contour.distanceFromEdge(p);
        }             

        @Override
        public double distanceFromPlane(Point3D p) {
            Line3D line = new Line3D();
            this.contour.plane().distance(p, line);
            return line.length();
        }

        @Override
        public Vector3D vectorToPlane(Point3D p) {
            Line3D line = new Line3D();
            this.contour.plane().distance(p, line);
            return line.direction().asUnit();            
        }

        @Override
        public String toString() {
            return this.contour.toString();
        }
    }
    
    public class ArcContour extends Contour {
        
        private Sector3D contour;

        public ArcContour(Sector3D contour) {
            this.contour = contour;
        }
        
        @Override
        public boolean isInside(Point3D p) {
            return this.contour.isInside(p);
        }

        @Override
        public double distanceFromEdge(Point3D p) {
            return this.contour.distanceFromEdge(p);
        }                        

        @Override
        public double distanceFromPlane(Point3D p) {
            Line3D line = new Line3D();
            this.contour.plane().distance(p, line);
            return line.length();
        }

        @Override
        public Vector3D vectorToPlane(Point3D p) {
            Line3D line = new Line3D();
            this.contour.plane().distance(p, line);
            return line.direction().asUnit();            
        }

        @Override
        public String toString() {
            return this.contour.toString();
        }
    }
    
    
}
