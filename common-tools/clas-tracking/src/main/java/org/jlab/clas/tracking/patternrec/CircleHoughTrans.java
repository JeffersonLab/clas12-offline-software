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
    
    public List<ArrayList<Point2D>> set = new ArrayList<ArrayList<Point2D>>();
        
    public void findCircles(List<Double>X, List<Double>Y) {
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
                        
                        if((int)x0+ (int) (width/2) + (((int)y0 + (int) (height/2)) * width)==(int)entry.getKey()) {
                            xy.add(new Point2D(x,y));
                        }
                    }
                }
            }
            if(this.contains(xy,set)==false){
                set.add((ArrayList<Point2D>) xy);
            }
        }
    }

    private boolean contains(List<Point2D> x0y0, List<ArrayList<Point2D>> set) {
        
        boolean isc = false;
        
        x0y0.sort(Comparator.comparing(Point2D::getX).thenComparing(Point2D::getY));
        
        for(int i = 0; i < set.size(); i++) {
            List<Point2D> xy = set.get(i);
            
            xy.sort(Comparator.comparing(Point2D::getX).thenComparing(Point2D::getY));
            
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
        }
        
        // Overriding equals() to compare two Complex objects 
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
        private double _x;
        private double _y;
    }
}
