package org.jlab.service.ec;

import java.util.List;

/**
 *
 * @author gavalian
 * modified lcsmith 2/22/22
 */

public class ECPeakAnalysis {
	
	static int splitIndex;
	static int splitStrip; //for debugging
    
    public static int getPeakSplitIndex(List<ECPeak> peaks){        
        for(int i = 0; i < peaks.size(); i++){
            splitIndex = peaks.get(i).getSplitIndex(ECCommon.splitMethod); //index of strip used to split peak
            splitStrip = peaks.get(i).getSplitStrip(); //strip used to split peak
            if(splitIndex>=0) return i; //index of peak tagged to be split
        }
        return -1;
    }
    
    public static void splitPeaks(List<ECPeak> peaks){
        
        while(true){ //repeat processing all peaks until no split found
        	if(ECCommon.debugSplit) System.out.println(" ");
            int index = getPeakSplitIndex(peaks);
        	if(ECCommon.debugSplit) System.out.println("New Iteration "+splitIndex+" "+splitStrip);
            if(index<0){
                return; // no split was found in any peak.  Exit.
            } else {
                ECPeak  peak = peaks.get(index); //retrieve tagged peak with split candidate
                peaks.remove(index); //tagged peak removed from list 
                peaks.addAll(peak.splitPeak(splitIndex)); //two split peaks returned to list
            }
        }
    }
    
    
    
}