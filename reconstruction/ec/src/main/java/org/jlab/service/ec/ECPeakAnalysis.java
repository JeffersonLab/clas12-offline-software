package org.jlab.service.ec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.jlab.geom.prim.Line3D;

/**
 *
 * @author gavalian
 * @modified lcsmith 
 * @re-modified gavalian
 * 
 * Added a new functionality to split only peaks after constructing a cluster
 * then checking if the other views in the cluster need splitting. Hopefully 
 * this will work. This is the way.
 */

public class ECPeakAnalysis {
    
    static final int[][] otherLayers = new int[][]{
        {0,0},{0,0},{0,0},{0,0},{5,6},{4,6},{4,5},{8,9},{7,9},{7,8}
    };
    
    public static int[] getPeakSplitIndex(List<ECPeak> peaks){ 
    	int [] split = {-1,-1,-1}; int[] zero = {-1,-1,-1}; 
        for(int i = 0; i < peaks.size(); i++){
            split[0] = peaks.get(i).getSplitIndex(ECCommon.splitMethod); //index of strip used to split peak
            split[1] = peaks.get(i).getSplitStrip(); //strip used to split peak
            split[2] = i;
            if(split[0]>=0) return split; //index of peak tagged to be split
        }
        return zero;
    }
    
    public static void splitPeaks(List<ECPeak> peaks){
        while(true){ //repeat processing all peaks until no split found
        	if(ECCommon.debugSplit) System.out.println(" ");
            int[] split = getPeakSplitIndex(peaks);
        	if(ECCommon.debugSplit) System.out.println("New Iteration "+split[0]+" "+split[1]);
            if(split[2]<0){
                return; // no split was found in any peak.  Exit.
            } else {
                ECPeak  peak = peaks.get(split[2]); //retrieve tagged peak with split candidate
                peaks.remove(split[2]); //tagged peak removed from list 
                peaks.addAll(peak.splitPeak(split[0])); //two split peaks returned to list
            }
        }
    }   
      
    /**
     * This part is added to acommodate for tricky algorithm to check if the 
     * splitting makes sense. Implemented on 02/20/2023
     * @param peak
     * @param allPeaks
     * @return 
     */
    public static int[] getPeakSplitIndex(ECPeak peak, List<ECPeak> allPeaks){
        int [] split = {-1,-1,-1};
        split[0] = peak.getSplitIndex(ECCommon.splitMethod);
        split[1] = peak.getSplitStrip(); //strip used to split peak
        split[2] = 0;
        if(split[0]>=0) {
            int layer = peak.getDescriptor().getLayer();
            if(layer==1||layer==2||layer==3) return split;
            List<ECPeak>  others = ECPeakAnalysis.getMatchingPeaks(peak, allPeaks);
            if(others.size()!=2) return split;

            int splitIndexOne = others.get(0).getSplitIndex(ECCommon.splitMethod);
            int splitIndexTwo = others.get(1).getSplitIndex(ECCommon.splitMethod);
            //System.out.printf("\t >>> inside the loop where matching others were found, so one = %d, two = %d\n",
            //        splitIndexOne, splitIndexTwo);
            if(splitIndexOne>=0||splitIndexTwo>=0) return split;
            
            //System.out.printf(" I was gonna split this peak, but decided not to : this is the way\n");
            //System.out.println(peak);
        }
        return new int[]{-1,-1,-1};
    }
    
    public static List<ECPeak>  getListForSectorLayer(List<ECPeak> peaks, int sector, int layer){
        List<ECPeak> list = new ArrayList<>();
        for(ECPeak p : peaks)
            if(p.getDescriptor().getSector()==sector&&p.getDescriptor().getLayer()==layer) list.add(p);
        return list;
    }
    
    
    public static int[] getBestCluster(ECPeak p, List<ECPeak> viewOne, List<ECPeak> viewTwo){
        double distance = 25.0; 
        int[]  indexMatch = new int[]{-1,-1};
        for(int ione = 0; ione < viewOne.size(); ione++){
            for(int itwo = 0; itwo < viewTwo.size(); itwo++){
                Line3D line = ECCluster.getClusterGeometry(p, 
                        viewOne.get(ione),viewTwo.get(itwo));
                //System.out.println("\t" + p);
                //System.out.printf("\t\t %5d %5d, distance = %9.3f\n",ione,itwo, line.length());
                if(line.length()<distance){
                    distance = line.length();
                    indexMatch[0] = ione; indexMatch[1] = itwo;
                }
            }
        }
        return indexMatch;
    }
    
    public static List<ECPeak>  getMatchingPeaks(ECPeak p, List<ECPeak> peaks){
        int sector = p.getDescriptor().getSector();
        int  layer = p.getDescriptor().getLayer();
        int[] layers = ECPeakAnalysis.otherLayers[layer];
        
        //System.out.printf("\t LOOKING: peak at = %d , looking for %d %d\n",layer, layers[0], layers[1]);
        List<ECPeak> one = ECPeakAnalysis.getListForSectorLayer(peaks, sector, layers[0]);
        List<ECPeak> two = ECPeakAnalysis.getListForSectorLayer(peaks, sector, layers[1]);
        //System.out.printf("\t LOOKING: peak at = %d , looking for %d %d and got count %d,%d\n",
         //       layer, layers[0], layers[1], one.size(), two.size());
        int[] cluster = ECPeakAnalysis.getBestCluster(p, one, two);
        if(cluster[0]>=0&&cluster[1]>=0) return Arrays.asList(one.get(cluster[0]),two.get(cluster[1]));
        return new ArrayList<ECPeak>();
    }
    
    public static void splitPeaksAlternative(List<ECPeak> peaks){
        
        List<ECPeak> current = new ArrayList<>();
        current.addAll(peaks);
        
        peaks.clear();
        //System.out.printf("--- split peaks start current = %d, peaks = %d\n",current.size(), peaks.size());
        
        while(!current.isEmpty()){
            ECPeak peak = current.get(0); current.remove(0);
            int[] split = ECPeakAnalysis.getPeakSplitIndex(peak, current);
            if(split[0]>=0){
                peaks.addAll(peak.splitPeak(split[0]));
            } else {
                peaks.add(peak);
            }
        }
        //System.out.printf("--- split peaks  end current = %d, peaks = %d\n",current.size(), peaks.size());
        /*
        while(true){ //repeat processing all peaks until no split found
        	if(ECCommon.debugSplit) System.out.println(" ");
            int[] split = getPeakSplitIndex(peaks);
        	if(ECCommon.debugSplit) System.out.println("New Iteration "+split[0]+" "+split[1]);
            if(split[2]<0){
                return; // no split was found in any peak.  Exit.
            } else {
                ECPeak  peak = peaks.get(split[2]); //retrieve tagged peak with split candidate
                peaks.remove(split[2]); //tagged peak removed from list 
                peaks.addAll(peak.splitPeak(split[0])); //two split peaks returned to list
            }
        }*/
    }
}