/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.clas.tracking.patternrec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import static java.util.stream.Collectors.toMap;
import org.jlab.clas.tracking.trackrep.Helix;

/**
 *
 * @author ziegler
 */
public class CircleHoughTrans {
    
    private double _RMin        = 200; // minimum helix radius
    private double _RWidth      = 2200;// maximum helix radius
    private double _RBinWidth   = 20;  // R bin width
    
    private int width  = 1400; // x range for helix (p~<3.25)
    private int height = 4100; // y range
    
    double R_Array[];
    int[] acc;
    int[] results;
    
    public CircleHoughTrans() {
        int nR = (int) (_RWidth/_RBinWidth);
        R_Array = new double[nR];
        for(int i = 0; i < nR; i++) {
            R_Array[i] = _RMin + (0.5 + i)*_RBinWidth;
        }
        acc     = new int[width * height];
        results = new int[width * height*3];
    }
    private Map<Integer, Integer> peaks = new HashMap<Integer, Integer>();
    public int minAccVal = 4;
    
    public void fillAccumulator(List<Double>X, List<Double>Y) {
        double t;
        double x;
        double y;
        double x0, y0;
        for(int j = 0; j < X.size(); j++) {
            x = X.get(j);
            y = Y.get(j);
            
            for(int i = 0; i < R_Array.length; i++) {
                for (int theta=0; theta<360; theta++) {
                    t = (theta * 3.14159265) / 180;
                    x0 = (x - R_Array[i] * Math.cos(t)) ;
                    y0 = (y - R_Array[i] * Math.sin(t)) ;
                    if(x0 < width/2 && x0 > -width/2 && y0 < height/2 && y0 > -height/2) {
			acc[(int)x0+ (int) (width/2) + (((int)y0 + (int) (height/2)) * width)] += 1;
                        if(acc[(int)x0+ (int) (width/2) + (((int)y0 + (int) (height/2)) * width)]>=minAccVal) {
                            peaks.put((int)x0+ (int) (width/2) + (((int)y0 + (int) (height/2)) * width), 
                                    acc[(int)x0+ (int) (width/2) + (((int)y0 + (int) (height/2)) * width)]);
                            
                        }                        
                    }
                }
            }
        }
    }
    List<Point2D> xy; // xy of seed points
    
    private List<ArrayList<Point2D>> set = new ArrayList<ArrayList<Point2D>>();
    public List<ArrayList<Point2D>> CHTSeeds = new ArrayList<ArrayList<Point2D>>();
        
    public void findCircles(List<Double>X, List<Double>Y, List<Integer>L) {
        for(int j = 0; j < set.size(); j++) {
            set.clear();
        }
        for(int j = 0; j < CHTSeeds.size(); j++) {
            CHTSeeds.clear();
        }
        fillAccumulator(X, Y);
        // find the peaks of the accumulator array
        Map<Integer, Integer> sorted = peaks.entrySet()
                                        .stream()
                                        .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                                        .collect(
                                            toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
                                                LinkedHashMap::new));
        
        for(Map.Entry entry:sorted.entrySet()){
            
            xy = new ArrayList<Point2D>((int)entry.getValue());
            
            double t;
            int l;
            double x;
            double y;
            double x0, y0;
            for(int j = 0; j < X.size(); j++) {
                l = L.get(j);
                x = X.get(j);
                y = Y.get(j);
                
                for(int i = 0; i < R_Array.length; i++) {
                    for (int theta=0; theta<360; theta++) {
                        t = (theta * 3.14159265) / 180;
                        x0 = (x - R_Array[i] * Math.cos(t)) ;
                        y0 = (y - R_Array[i] * Math.sin(t)) ;
                        
                        if((int)x0+ (int) (width/2) + (((int)y0 + (int) (height/2)) * width)==(int)entry.getKey()) {
                            xy.add(new Point2D(x,y,l,R_Array[i],t)); 
                        }
                    }
                }
            }
            
            if(this.contains(xy,set)==false){
                set.add((ArrayList<Point2D>) xy);
                List<ArrayList<Point2D>> newset = this.split();
                for(int i = 0; i < newset.size(); i++) { 
                    if(newset.get(i).size()>=this.minAccVal &&
                            this.contains(newset.get(i),CHTSeeds)==false) {
                        CHTSeeds.add(newset.get(i));
                        System.out.println("=================");
                        for(int j = 0; j < newset.get(i).size(); j++) {
                            Point2D p =newset.get(i).get(j);
                            System.out.println(p._layer+" "+p._x+" "+p._y+" rad "+p._radius+" pt "+(p._radius*(5*Helix.LIGHTVEL)));
                        }
                    }
                }
            }
        }
    }
    Map<Integer, List<Point2D>> hxy = new HashMap<>();         
    
    private List<ArrayList<Point2D>> split() {
        hxy.clear();
        List<ArrayList<Point2D>> newset = new ArrayList<ArrayList<Point2D>>();
        //get list with single hit per layer
        List<Point2D> base = new ArrayList<Point2D>(); // xy of seed points
        base.add(xy.get(0));
        for(int i = 1; i < xy.size(); i++) {
            if(xy.get(i-1).getLayer() != xy.get(i).getLayer()) {
                base.add(xy.get(i));
            } 
        }
        List<Point2D> offbase = new ArrayList<Point2D>(); 
        offbase.addAll(xy);
        offbase.removeAll(base);
        
        //get same layer hits
        for(Point2D p : offbase) {
            hxy.put(p.getLayer(),new ArrayList<Point2D>());
        }
        for(Point2D p : offbase) {
            hxy.get(p.getLayer()).add(p);
        }
        newset.add((ArrayList<Point2D>) base);
        
        for (Map.Entry<Integer, List<Point2D>> entry : hxy.entrySet()) {
            for(Point2D p : entry.getValue()) {
                List<Point2D> base2 = new ArrayList<Point2D>();
                base2.addAll(base);
                for(int k = 0; k < base2.size(); k++) {
                    Point2D bp = base2.get(k);
                    if(p.getLayer()==bp.getLayer()) {
                        base2.remove(k);
                        base2.add(k,p);
                    }
                }
                newset.add((ArrayList<Point2D>) base2);
            }
        }
        return newset;
    }
    private boolean contains(List<Point2D> x0y0, List<ArrayList<Point2D>> set) {
        boolean isc = false;
        
        x0y0.sort(Comparator.comparing(Point2D::getLayer).thenComparing(Point2D::getR));
        
        for(int i = 0; i < set.size(); i++) {
            List<Point2D> xy = set.get(i);
            
            xy.sort(Comparator.comparing(Point2D::getLayer).thenComparing(Point2D::getR));
            
            if(xy.size() == x0y0.size()) {
                int n = 0;
                for(int k =0; k < xy.size(); k++) { 
                    if(xy.get(k).equals(x0y0.get(k)))
                        n++;
                }
                if(n == xy.size())
                    isc = true;
            }
        }
        
        return isc;
    }

    public class Point2D {

        public Point2D(double x, double y) {
            this._x = x;
            this._y = y;
            this._r = Math.sqrt(x*x + y*y);
        }
        public Point2D(double x, double y, int layer) {
            this._layer = layer;
            this._x = x;
            this._y = y;
            this._r = Math.sqrt(x*x + y*y);
        }
        public Point2D(double x, double y, int layer, double rad, double azi) {
            this._layer = layer;
            this._x = x;
            this._y = y;
            this._r = Math.sqrt(x*x + y*y);
            this._radius  = rad;
            this._azimuth = azi;
        }
        public void set(Point2D p) {
            this._layer = p._layer;
            this._x = p._x;
            this._y = p._y;
            this._r = p._r;
        }
        // Overriding equals() to compare two objects 
        @Override
        public boolean equals(Object o) { 

            // If the object is compared with itself then return true   
            if (o == this) { 
                return true; 
            } 

            /* Check if o is an instance of Point2D or not 
              "null instanceof [type]" also returns false */
            if (!(o instanceof Point2D)) { 
                return false; 
            } 

            // typecast o to Point2D so that we can compare data members  
            Point2D c = (Point2D) o; 

            // Compare the data members and return accordingly  
            return Double.compare(_x, c._x) == 0
                    && Double.compare(_y, c._y) == 0; 
        }
        
         /**
         * @return the _x
         */
        public double getX() {
            return _x;
        }

        /**
         * @param _x the _x to set
         */
        public void setX(double _x) {
            this._x = _x;
        }

        /**
         * @return the _y
         */
        public double getY() {
            return _y;
        }

        /**
         * @param _y the _y to set
         */
        public void setY(double _y) {
            this._y = _y;
        }
        
        /**
         * @return the _layer
         */
        public int getLayer() {
            return _layer;
        }

        /**
         * @param _layer the _layer to set
         */
        public void setLayer(int _layer) {
            this._layer = _layer;
        }
        
        /**
         * @return the _r
         */
        public double getR() {
            return _r;
        }

        /**
         * @param _r the _r to set
         */
        public void setR(double _r) {
            this._r = _r;
        }

        /**
         * @return the _radius
         */
        public double getRadius() {
            return _radius;
        }

        /**
         * @param _radius the _radius to set
         */
        public void setRadius(double _radius) {
            this._radius = _radius;
        }

        /**
         * @return the _azimuth
         */
        public double getAzimuth() {
            return _azimuth;
        }

        /**
         * @param _azimuth the _azimuth to set
         */
        public void setAzimuth(double _azimuth) {
            this._azimuth = _azimuth;
        }

        private double _x;
        private double _y;
        private int _layer;
        private double _r;
        private double _radius;
        private double _azimuth;
    }
}
