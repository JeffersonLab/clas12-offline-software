package org.jlab.clas.tracking.kalmanfilter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jlab.clas.swimtools.Swim;
import org.jlab.clas.tracking.kalmanfilter.AStateVecs.StateVec;
import org.jlab.clas.tracking.objects.Strip;
import org.jlab.geom.prim.Arc3D;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Transformation3D;

/**
 *
 * @author ziegler
 */

public abstract class AMeasVecs {
   
    public List<MeasVec> measurements = new ArrayList<>();

    public void setMeasVecs(List<Surface> measSurfaces) {
        measurements = new ArrayList<>();
        Collections.sort(measSurfaces);
        for(int i = 0; i < measSurfaces.size(); i++) {
            MeasVec mvec = new MeasVec();
            mvec.k = i ;
            mvec.layer = measSurfaces.get(i).getIndex();
            mvec.surface = measSurfaces.get(i);
            if(mvec.surface.getError()!=0)
                mvec.error = mvec.surface.getError();
            mvec.skip = mvec.surface.passive;
            mvec.hemisphere = measSurfaces.get(i).hemisphere;
            measurements.add(mvec);
        }
    }
    
    
    public double dh(int k, StateVec stateVec) {
        
        double value = Double.NaN;

        if (stateVec == null|| this.measurements.get(stateVec.k) == null) {
            return value;
        }
        
        if (this.measurements.get(stateVec.k).surface.type == Type.LINE) {
            Point3D sv = new Point3D(stateVec.x, stateVec.y, stateVec.z);
            Line3D l = new Line3D(this.measurements.get(stateVec.k).surface.lineEndPoint1,
                                  this.measurements.get(stateVec.k).surface.lineEndPoint2);
            value = l.distance(sv).length();
        }
        if( this.measurements.get(stateVec.k).surface.type == Type.PLANEWITHPOINT || 
                this.measurements.get(stateVec.k).surface.type == Type.CYLINDERWITHPOINT) {
            Point3D p = new Point3D(this.measurements.get(stateVec.k).surface.refPoint);
            value = p.distance(stateVec.x, stateVec.y, stateVec.z);
        }
        if( this.measurements.get(stateVec.k).surface.type == Type.CYLINDERWITHARC) {
            double phia = this.measurements.get(stateVec.k).surface.arc.theta();
            value = Math.atan2(stateVec.y, stateVec.x)-phia;
            System.err.println("ARC MEAS. NOT FULLY IMPLEMENTED!!!!!!");
        }
        if( this.measurements.get(stateVec.k).surface.type == Type.PLANEWITHLINE || 
                this.measurements.get(stateVec.k).surface.type == Type.CYLINDERWITHLINE) {
            Line3D l = new Line3D(this.measurements.get(stateVec.k).surface.lineEndPoint1, 
            this.measurements.get(stateVec.k).surface.lineEndPoint2);
            //value = l.distance(new Point3D(stateVec.x, stateVec.y, stateVec.z)).length();
            Line3D WL = new Line3D();
            WL.copy(l);
            WL.copy(WL.distance(new Point3D(stateVec.x, stateVec.y, stateVec.z)));
            
            value = WL.length();
        }
        if( this.measurements.get(stateVec.k).surface.type == Type.PLANEWITHSTRIP || 
                this.measurements.get(stateVec.k).surface.type == Type.CYLINDERWITHSTRIP) { 
            if(this.measurements.get(stateVec.k).surface.strip.type == Strip.Type.XYZ) {
                Line3D l = new Line3D(this.measurements.get(stateVec.k).surface.lineEndPoint1, 
                this.measurements.get(stateVec.k).surface.lineEndPoint2);
                //value = l.distance(new Point3D(stateVec.x, stateVec.y, stateVec.z)).length(); 
                if(l.direction().z()<0) {
                    l.setEnd(this.measurements.get(stateVec.k).surface.lineEndPoint1);
                    l.setOrigin(this.measurements.get(stateVec.k).surface.lineEndPoint2);
                }
                Line3D WL = new Line3D();
                WL.copy(l);
                Point3D svP = new Point3D(stateVec.x, stateVec.y, stateVec.z);
                WL.copy(WL.distance(svP));
                double sideStrip = -Math.signum(l.direction().cross(WL.direction()).
                        dot(this.measurements.get(stateVec.k).surface.plane.normal()));
                //double sideStrip = Math.signum(l.direction().y()*WL.direction().x()+l.direction().x()*WL.direction().y());
                value = WL.length()*sideStrip; 
            }
            if(this.measurements.get(stateVec.k).surface.strip.type == Strip.Type.Z) { 
                Transformation3D toLocal =this.measurements.get(stateVec.k).surface.toLocal();
                Point3D stV = new Point3D(stateVec.x, stateVec.y, stateVec.z); 
                toLocal.apply(stV);
                value = stV.z()-this.measurements.get(stateVec.k).surface.strip.getZ();
            }
            if(this.measurements.get(stateVec.k).surface.strip.type == Strip.Type.ARC) {
                Transformation3D toLocal =this.measurements.get(stateVec.k).surface.toLocal();
                Arc3D arc = new Arc3D();
                arc.copy(this.measurements.get(stateVec.k).surface.strip.getArc()); 
                toLocal.apply(arc);
                Point3D stV = new Point3D(stateVec.x, stateVec.y, stateVec.z); 
                toLocal.apply(stV);
                value = stV.z()-arc.center().z(); 
            }
            if(this.measurements.get(stateVec.k).surface.strip.type == Strip.Type.PHI) {
                Transformation3D toLocal =this.measurements.get(stateVec.k).surface.toLocal();
                Point3D sv = new Point3D(stateVec.x, stateVec.y, stateVec.z);
                toLocal.apply(sv);
                value = sv.toVector3D().phi()-this.measurements.get(stateVec.k).surface.strip.getPhi();
                if(Math.abs(value)>Math.PI) value-=Math.signum(value)*2*Math.PI;
            }
        }
        return value;
    }
      
    public double h(int k, StateVec stateVec) {
        
        double value = Double.NaN;
        
        if (stateVec == null|| this.measurements.get(stateVec.k) == null) {
            return value;
        }
        
        if (this.measurements.get(stateVec.k).surface.type == Type.LINE) {
            Point3D sv = new Point3D(stateVec.x, stateVec.y, stateVec.z);
            Line3D l = new Line3D(this.measurements.get(stateVec.k).surface.lineEndPoint1,
                                  this.measurements.get(stateVec.k).surface.lineEndPoint2);
            value = l.distance(sv).length();
        }
        if( this.measurements.get(stateVec.k).surface.type == Type.PLANEWITHPOINT || 
                this.measurements.get(stateVec.k).surface.type == Type.CYLINDERWITHPOINT) {
            Point3D p = new Point3D(this.measurements.get(stateVec.k).surface.refPoint);
            p.setZ(0);
            value = p.distance(stateVec.x, stateVec.y, 0);
        }
        if( this.measurements.get(stateVec.k).surface.type == Type.CYLINDERWITHARC) {
            double phia = this.measurements.get(stateVec.k).surface.arc.theta();
            value = value = Math.atan2(stateVec.y, stateVec.x);
            System.err.println("ARC MEAS. NOT FULLY IMPLEMENTED!!!!!!");
        }
        if( this.measurements.get(stateVec.k).surface.type == Type.PLANEWITHLINE || 
                this.measurements.get(stateVec.k).surface.type == Type.CYLINDERWITHLINE) {
            Line3D l = new Line3D(this.measurements.get(stateVec.k).surface.lineEndPoint1, 
            this.measurements.get(stateVec.k).surface.lineEndPoint2);
            value = l.distance(new Point3D(stateVec.x, stateVec.y, stateVec.z)).length();
        }
        if( this.measurements.get(stateVec.k).surface.type == Type.PLANEWITHSTRIP || 
                this.measurements.get(stateVec.k).surface.type == Type.CYLINDERWITHSTRIP) { 
            if(this.measurements.get(stateVec.k).surface.strip.type == Strip.Type.XYZ) {
                Line3D l = new Line3D(this.measurements.get(stateVec.k).surface.lineEndPoint1, 
                this.measurements.get(stateVec.k).surface.lineEndPoint2);
                //value = l.distance(new Point3D(stateVec.x, stateVec.y, stateVec.z)).length();
                if(l.direction().z()<0) {
                    l.setEnd(this.measurements.get(stateVec.k).surface.lineEndPoint1);
                    l.setOrigin(this.measurements.get(stateVec.k).surface.lineEndPoint2);
                }
                Line3D WL = new Line3D();
                WL.copy(l);
                Point3D svP = new Point3D(stateVec.x, stateVec.y, stateVec.z);
                WL.copy(WL.distance(svP));
                double sideStrip = -Math.signum(l.direction().cross(WL.direction()).
                        dot(this.measurements.get(stateVec.k).surface.plane.normal())); 
                //double sideStrip = Math.signum(l.direction().y()*WL.direction().x()+l.direction().x()*WL.direction().y());
                value = WL.length()*sideStrip;
                
            }
            if(this.measurements.get(stateVec.k).surface.strip.type == Strip.Type.Z) {
               value = stateVec.z;
            }
            if(this.measurements.get(stateVec.k).surface.strip.type == Strip.Type.ARC) {
                Transformation3D toLocal =this.measurements.get(stateVec.k).surface.toLocal();
                Point3D stV = new Point3D(stateVec.x, stateVec.y, stateVec.z); 
                toLocal.apply(stV);
                value = stV.z(); 
            }
            if(this.measurements.get(stateVec.k).surface.strip.type == Strip.Type.PHI) {
                Transformation3D toLocal =this.measurements.get(stateVec.k).surface.toLocal();
                Point3D sv = new Point3D(stateVec.x, stateVec.y, stateVec.z);
                toLocal.apply(sv);
                value = sv.toVector3D().phi()-this.measurements.get(stateVec.k).surface.strip.getPhi();
                if(Math.abs(value)>Math.PI) value-=Math.signum(value)*2*Math.PI;
            }
            if(this.measurements.get(stateVec.k).surface.type == Type.LINE) {
                Point3D sv = new Point3D(stateVec.x, stateVec.y, stateVec.z);
                Line3D l   = new Line3D(this.measurements.get(stateVec.k).surface.lineEndPoint1, 
                                        this.measurements.get(stateVec.k).surface.lineEndPoint2);
                value = l.distance(sv).length();
            }        
        }
        return value;
    }

    public double[] delta_d_a = new double[5];//{1, Math.toRadians(0.25),  0.05, 1, 0.01};
    public double sqrt_epsilon = Math.sqrt(2.2*1.e-16);
    public double rollBackAngle = Math.toRadians(0.5); // angular shift applied to the stateVec to move it "before" the surface when swimming
    public double[] Hval = new double[5];
    public abstract double[] H(AStateVecs.StateVec stateVec, AStateVecs sv, MeasVec mv, Swim swimmer) ;
     
    public class MeasVec implements Comparable<MeasVec> {
        public Surface surface;
        public int layer    = -1;
        public double error = 1;
        public int k        = -1;
        public boolean passive = false;
        public boolean skip  = false;
        public double hemisphere = 1;


        @Override
        public int compareTo(MeasVec arg) {
            int CompLay = this.layer < arg.layer ? -1 : this.layer == arg.layer ? 0 : 1;
            return CompLay;
        }
    }

    /**
     * @return the delta_d_a
     */
    public double[] getDelta_d_a() {
        return delta_d_a;
    }

    public void setDelta_d_a(double[] values) {
        delta_d_a = values;
    }

    /**
     * @return the Hval
     */
    public double[] getHval() {
        return Hval;
    }

    /**
     * @param Hval the Hval to set
     */
    public void setHval(double[] Hval) {
        this.Hval = Hval;
    }
    
}
