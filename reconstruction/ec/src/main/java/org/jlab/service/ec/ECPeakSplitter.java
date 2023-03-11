/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package org.jlab.service.ec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.jlab.display.ec.Renderer;

/**
 *
 * @author gavalian
 */
public interface ECPeakSplitter {
    public List<ECPeak>  split(ECPeak peak);
    
    public static class ECPeakSplitterOriginal implements ECPeakSplitter {                
       
        @Override
        public List<ECPeak> split(ECPeak peak) {
            int index = peak.gg1_getSplitIndex();
            if(index>0){                
                List<ECPeak> list = peak.splitPeak(index);
                List<ECPeak> newList = new ArrayList<>();
                for(ECPeak p : list) if(ECCommon.isGoodPeak(p)) newList.add(p);
                for(ECPeak p : newList) p.redoPeakLine();                
                return newList;
            }
            return Arrays.asList(peak);
        }
    }
    
    public static class ECPeakSplitterClas6 implements ECPeakSplitter {

        @Override
        public List<ECPeak> split(ECPeak peak) {
            throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
        }        
    } 
    
    public static class ECPeakSplitterMargin implements ECPeakSplitter {
        
        public static double     DROP_TRESHOLD = 0.0;
        public static double        MIN_HEIGHT = 0.25;
        public static double     DEEP_FRACTION = 0.60;
        
        /**
         * cluster groups in given buffer and return Group class
         * containing indicies for each cluster group
         * @param peak
         * @return 
         */
        public List<Group> cluster(double[] peak){
            List<Group> grp = new ArrayList<>();
            Group     cluster = new Group();
            
            for(int i = 0; i < peak.length; i++){
                if(peak[i]>0.00001){
                    if(cluster.index.isEmpty()){
                        cluster.index.add(i);
                    } else {
                        if(cluster.isNeighbor(i)==true){
                            cluster.index.add(i);
                        } else {
                            grp.add(cluster);
                            cluster = new Group();
                            cluster.index.add(i);
                        }
                    }
                } else {
                    if(!cluster.index.isEmpty()){
                        grp.add(cluster);
                        cluster = new Group();
                    }
                }
            }
            return grp;
        }
        
        /**
         * return maximum value in given array
         * @param peak peak array
         * @return maximum value
         */
        public double max(double[] peak){
            double max = 0;
            for(int k = 0; k < peak.length; k++) max = Math.max(max, peak[k]);
            return max;
        }
        /**
         * return the index of the minimum value in the given array between
         * indicies first and last (inclusive)
         * @param peak the array containing the peak data
         * @param first first index to consider
         * @param last last index to consider (inclusive)
         * @return 
         */
        public int min(double[] peak, int first, int last){
            int index = first; double min = peak[first];
            for(int i = first; i <= last; i++)
                if(peak[i]<min) { min = peak[i]; index = i;}
            return index;
        }
        
        public void evaluate(double[] original, double[] derived, double threshold){
            double max = max(original);
            for(int i = 0; i < original.length;i++) 
                if(original[i]/max>threshold) derived[i] = original[i]; else derived[i] = 0.0;
        }
        /*
        public void test(double[] array){
            double[] derived = new double[array.length];
            for(double t = 0.95; t>0.2; t-=0.05){
                evaluate(array,derived,t);
                List<Group> clusters = cluster(derived);
                System.out.printf("%f -> %s , size = %d",t,Arrays.toString(derived),clusters.size());
                if(clusters.size()==2) System.out.printf("  [%d,%d] min = %d",
                        clusters.get(0).last(),clusters.get(1).first(), min(array,
                                clusters.get(0).last(),clusters.get(1).first()));
                    System.out.println();
            }
        }
        */
        public List<Group> analyze(double[] buffer){
            double[] derived = new double[buffer.length];
            double    t = 0.95;
            double step = 0.05;
            while(t>ECPeakSplitterMargin.MIN_HEIGHT){
                evaluate(buffer,derived,t);
                List<Group> clusters = cluster(derived);
                if(clusters.size()==2) return clusters;
                t -= step;
            }
            return new ArrayList<Group>();
        }
        
        @Override
        public List<ECPeak> split(ECPeak peak) {
            double[] buffer = peak.getEnergies();
            List<Group>  group = analyze(buffer);
            if(group.size()==2){
                int splitIndex = this.min(buffer, group.get(0).last(), group.get(1).first());
                List<ECPeak> splitPeaks = peak.splitPeak(splitIndex);
                
                /*Renderer  r = new Renderer(8,peak.getEnergies());
                Renderer  r1 = new Renderer(8,splitPeaks.get(0).getEnergies());
                Renderer  r2 = new Renderer(8,splitPeaks.get(1).getEnergies());
                
                System.out.println(r);
                System.out.println(r1);
                System.out.println(r2);
                */
                double energy = peak.getStripEnergy(splitIndex);
                boolean split = true;
                double e1 = energy/splitPeaks.get(0).getMaxECStrip().getEnergy();
                double e2 = energy/splitPeaks.get(1).getMaxECStrip().getEnergy();
                if(energy/splitPeaks.get(0).getMaxECStrip().getEnergy()>DEEP_FRACTION) split = false;
                if(energy/splitPeaks.get(1).getMaxECStrip().getEnergy()>DEEP_FRACTION) split = false;
                
                
                if(split==true&&splitPeaks.get(0).isGood()>0&&splitPeaks.get(1).isGood()>0)
                    return splitPeaks;
                //System.out.printf("PEAK SPLIT GOODNESS : %d %d (%.5f %.5f) is being split = %s\n",
                //        splitPeaks.get(0).isGood(), splitPeaks.get(1).isGood(), e1,e2 ,split);
            }
            
            return Arrays.asList(peak);
        }
        /**
         * Group class for clustering nearby hits in an array.
         */
        public static class Group {
            List<Integer> index = new ArrayList<>();
            public boolean isNeighbor(int id){
                for(Integer item : index) if(Math.abs(item-id)==1) return true;
                return false;
            }
            
            public int first(){return index.get(0);}
            public int last(){return index.get(index.size()-1);}
        }
    }
    
    public static void main(String[] args){
        
        ECPeakSplitterMargin m = new ECPeakSplitterMargin();
        
        /*m.findSplitPoint(new double[]{0.00194,0.00304,0.01727,0.00667,0.05886,0.00912});
        m.test(new double[]{0.00194,0.00304,0.01727,0.00667,0.05886,0.00912});        
        m.findSplitPoint(new double[]{0.00427,0.00376,0.01815,0.00513,0.00941,0.00719,0.00336});
        m.test(new double[]{0.00427,0.00376,0.01815,0.00513,0.00941,0.00719,0.00336});*/
        //m.findSplitPoint(new double[]{0.01375,0.02480,0.00900,0.02374,0.00381});
    }
}
