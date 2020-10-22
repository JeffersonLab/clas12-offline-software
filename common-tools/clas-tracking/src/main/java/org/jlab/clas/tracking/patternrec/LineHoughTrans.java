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
public class LineHoughTrans {
   
    private double _TBinWidth;
    
    double C_Array[];
    double S_Array[];
    
    
    int[][] acc;
    int[] results;
    
    int nTh = 180;
    int n = 45;
    public LineHoughTrans() {
        _TBinWidth = nTh/ (double) nTh;
        
        C_Array = new double[nTh];
        S_Array = new double[nTh];
        for(int i = 0; i < nTh; i++) {
            C_Array[i] = Math.cos((0.5 + i)*_TBinWidth);
            S_Array[i] = Math.cos((0.5 + i)*_TBinWidth);
        }
        acc     = new int[n][nTh];
        results = new int[n * nTh *3];
    }
    private Map<Integer, Integer> peaks = new HashMap<Integer, Integer>();
    public int minAccVal = 4;
    
    public void fillAccumulator(List<Double>X, List<Double>Y) {
        double x;
        double y;
        
        for(int j = 0; j < X.size(); j++) {
            x = X.get(j);
            y = Y.get(j);
            
           for(int i = 0; i < nTh; i++) { 
                acc[(int) Math.floor(n * (y*C_Array[i] + x*S_Array[i] + 180.0) / 360.0)][i]++;
                peaks.put((int) Math.floor(n * (y*C_Array[i] + x*S_Array[i] + 180.0) / 360.0),
                    acc[(int) Math.floor(n * (y*C_Array[i] + x*S_Array[i] + 180.0) / 360.0)][i]);
            }
        }
    }
    List<Point2D> xy; // xy of seed points
    
    private List<ArrayList<Point2D>> set = new ArrayList<ArrayList<Point2D>>();
    public List<ArrayList<Point2D>> LHTSeeds = new ArrayList<ArrayList<Point2D>>();
        
    public void findLines(List<Double>X, List<Double>Y, List<Integer>L) {
        for(int j = 0; j < set.size(); j++) {
            set.clear();
        }
        for(int j = 0; j < LHTSeeds.size(); j++) {
            LHTSeeds.clear();
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
                
                for(int i = 0; i < nTh; i++) { 
                if((int) Math.floor(n * (y*C_Array[i] + x*S_Array[i] + 180.0) / 360.0)==(int)entry.getKey()) {
                            xy.add(new Point2D(x,y,l,(int)entry.getKey(),i)); 
                            xy.get(xy.size()-1).index = j;
                        }
                    }
                }
            
            
            if(this.contains(xy,set)==false){
                set.add((ArrayList<Point2D>) xy);
                List<ArrayList<Point2D>> newset = this.split();
                for(int i = 0; i < newset.size(); i++) { 
                    if(newset.get(i).size()>=this.minAccVal &&
                            this.contains(newset.get(i),LHTSeeds)==false) {
                        LHTSeeds.add(newset.get(i));
                        System.out.println("=================");
                        for(int j = 0; j < newset.get(i).size(); j++) {
                            Point2D p =newset.get(i).get(j);
                            System.out.println(p._layer+" "+p._x+" "+p._y+" rho "+p._rho+" phi "+(p._phi));
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
        public int index = -1;
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
            this._rho  = rad;
            this._phi = azi;
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
         * @return the _rho
         */
        public double getRho() {
            return _rho;
        }

        /**
         * @param _radius the _rho to set
         */
        public void setRho(double _radius) {
            this._rho = _radius;
        }

        /**
         * @return the _phi
         */
        public double getphi() {
            return _phi;
        }

        /**
         * @param _azimuth the _phi to set
         */
        public void setPhi(double _azimuth) {
            this._phi = _azimuth;
        }

        private double _x;
        private double _y;
        private int _layer;
        private double _r;
        private double _rho;
        private double _phi;
    }
}
