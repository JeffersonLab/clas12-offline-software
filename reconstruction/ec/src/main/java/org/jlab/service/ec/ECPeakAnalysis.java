package org.jlab.service.ec;

import java.util.List;

/**
 *
 * @author gavalian
 * modified lcsmith 
 */

public class ECPeakAnalysis {
    
    public static int[] getPeakSplitIndex(List<ECPeak> peaks){ 
    	int [] split = {-1,-1}; 
        for(int i = 0; i < peaks.size(); i++){
            split[0] = peaks.get(i).getSplitIndex(ECCommon.splitMethod); //index of strip used to split peak
            split[1] = peaks.get(i).getSplitStrip(); //strip used to split peak
            if(split[0]>=0) return split; //index of peak tagged to be split
        }
        return split;
    }
    
    public static void splitPeaks(List<ECPeak> peaks){
        int [] split;
        while(true){ //repeat processing all peaks until no split found
        	if(ECCommon.debugSplit) System.out.println(" ");
            split = getPeakSplitIndex(peaks);
        	if(ECCommon.debugSplit) System.out.println("New Iteration "+split[0]+" "+split[1]);
            if(split[0]<0){
                return; // no split was found in any peak.  Exit.
            } else {
                ECPeak  peak = peaks.get(split[0]); //retrieve tagged peak with split candidate
                peaks.remove(split[0]); //tagged peak removed from list 
                peaks.addAll(peak.splitPeak(split[0])); //two split peaks returned to list
            }
        }
    }   
      
}