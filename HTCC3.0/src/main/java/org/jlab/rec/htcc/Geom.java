package org.jlab.rec.htcc;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


/**
 *
 * @author markovnick
 */
public class Geom {
    
     
    public static double [] eXR = new double[]{1728.6727, 1612.9958, 1497.6043, 1383.6210};
    public static  double [] eYR = new double[]{1907.8098, 1846.1550, 1786.8590,1728.3754};
    public static  double [] eZR = new double[]{1728.6727, 1612.9958, 1497.6043, 1383.6210};   
    public static  double[]fXR = new double[]{0,0,0,0};
    public static  double[]fYR = new double[]{1613.0299,1788.6919,1902.4068, 1946.9473};
    public static  double[]fZR = new double[]{-62.0467, 163.3327,425.4815, 707.7389};

        
    static class Ellipse{
    double a;
    double b;
    double c;
    double cX;
    double cY;
    double cZ;
    double x0;
    double y0;
    double z0;
    double x1;
    double y1;
    double z1;
    public Ellipse(double aI, double bI, double cI, double x0I, double y0I, double z0I,double x1I, double y1I, double z1I){
    a = aI;
    b = bI;
    c = cI;
    x0 = x0I;
    y0 = y0I;
    z0 = z0I;
    x1 = x1I;
    y1 = y1I;
    z1 = z1I;
    cX = (x0 + x1)/2;
    cY = (y0 + y1)/2;
    cZ = (z0 + z1)/2;

    }
    
} 
public class line{
    double angle;
    public  line(double a){
        angle = a;
    }
}

static class Rotate3D{
    
private double x;
private double y;
private double z;
private double angle;

public Rotate3D(double angleI, double xI, double yI, double zI){
        x = xI;
        y = yI;
        z = zI;
        angle = angleI;
    }
double getXPrime(){
    return x*Math.sin(angle) + y*Math.cos(angle);
}
double getYPrime(){
    return - x*Math.cos(angle) + y*Math.sin(angle);
}
double getZPrime(){
    return z;
}
}
static class FindIntersect{
   
    double angle;
    double cy;
    double cz;
    double ry;
    double rz;
    double alfa;
    double a;
    double b;
    double c;
    double d;
    public FindIntersect(double angleI,  double ycI, double zcI,  double ryI, double rzI){
        angle = angleI;
        cy = ycI;
        cz = zcI;
        ry = ryI;
        rz = rzI;
        alfa = angleI;
        a = rz*rz*Math.sin(alfa)*Math.sin(alfa) + ry*ry;
        b = -2*Math.sin(alfa)*cy*rz*rz - 2*cz*ry*ry;
        c = cy*cy*rz*rz + cz*cz*ry*ry - rz*rz*ry*ry;
        d = b*b - 4*a*c;
    }
   
   public double getxIntersect(){
   return  0;
   }
   public double getyIntersect(){
   return  ((-b + Math.sqrt(d))/(2*a))*Math.sin(alfa);
   }
   public double getzIntersect(){
   return  (-b + Math.sqrt(d))/(2*a);
   }
}
}