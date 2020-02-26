package org.jlab.service.ec;

import java.util.List;

/**
 *
 * @author gavalian
 */

public class ECPeakAnalysis {
    
    public static int getPeakSplitIndex(List<ECPeak> peaks){
        int index = -1;
        for(int i = 0; i < peaks.size(); i++){
            int si = peaks.get(i).getSplitStrip();
            if(si>=0) return i;
        }
        return index;
    }
    
    public static void splitPeaks(List<ECPeak> peaks){
        boolean isSplited = true;
        while(isSplited==true){
            int index = ECPeakAnalysis.getPeakSplitIndex(peaks);
            if(index<0){
                isSplited = false;
            } else {
                ECPeak  peak = peaks.get(index);
                peaks.remove(index);
                int strip = peak.getSplitStrip();
                List<ECPeak> twoPeaks = peak.splitPeak(strip);
                peaks.addAll(twoPeaks);
            }
        }
    }
    
}
