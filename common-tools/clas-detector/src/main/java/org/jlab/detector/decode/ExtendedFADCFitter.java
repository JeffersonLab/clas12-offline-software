package org.jlab.detector.decode;

/**
 *
 * @author gavalian
 * @author baltzell
 */
public class ExtendedFADCFitter implements IFADCFitter {

    private int pedestalMinBin = 1;
    private int pedestalMaxBin = 15;
    public int t0;
    public int adc;
    public int ped;
    public int thresholdCrossingBin;
    public int pulsePeakValue;
    public int pulsePeakBin;
    public int pulseWidth;
    public double rms;
    private int tcoarse, tfine;

    public ExtendedFADCFitter() {}
    
    public ExtendedFADCFitter(int pedMaxBin) {
        this.pedestalMaxBin = pedMaxBin;
    }

    public ExtendedFADCFitter(int pedMinBin, int pedMaxBin) {
        this.pedestalMinBin = pedMinBin;
        this.pedestalMaxBin = pedMaxBin;
    }

    private void calculatePedestal(int minBin, int maxBin, short[] waveform) {
        int sum = 0;
        int noise = 0;
        for (int bin = minBin; bin <= maxBin; bin++) {
            sum += waveform[bin];
            noise  += waveform[bin] * waveform[bin];
        }
        ped = sum / (maxBin - minBin + 1);	
        rms = Math.sqrt(noise / (maxBin - minBin + 1) - ped*ped);
    }

    public boolean fit(int nsa, int nsb, int tet, int pedr, short[] waveform) {

        t0=0;
        adc=0;
        thresholdCrossingBin=0;
        pulsePeakValue=0;
        pulsePeakBin=0;
        pulseWidth=0;
        tcoarse=0;
        tfine=0;

        // if user-supplied pedestal is nonzero, use it:
        if (pedr!=0) ped = pedr;

        // not enough samples, just use the whole window for pedestal and abort:
        else if (waveform.length < pedestalMaxBin+1) {
            calculatePedestal(0, waveform.length, waveform);
            return false;
        }

        // otherwise use the specified sample range:
        else {
            calculatePedestal(pedestalMinBin, pedestalMaxBin, waveform);
        }

        // find threshold crossing
        for (int bin=pedestalMaxBin+1; bin<waveform.length; bin++) {
            if(waveform[bin] > ped+tet) {
                thresholdCrossingBin = bin;
                break;
            }
        }

        // if no threshold crossing, abort:
        if(thresholdCrossingBin <= 0) {
            return false;
        }
            
        // calculate integral and find maximum:
        final int firstBin = Math.max(0, thresholdCrossingBin - nsb);
        final int lastBin = Math.min(waveform.length, thresholdCrossingBin + nsa);
        for (int bin = firstBin; bin <= lastBin; bin++) {
            adc += waveform[bin]-ped;
            if(bin >= thresholdCrossingBin && waveform[bin] > pulsePeakValue) {
                pulsePeakValue = waveform[bin];
                pulsePeakBin = bin;
            }
        }

        // calculating mode 7 pulse time:
        final double halfMax = (pulsePeakValue+ped)/2;
        int s0 = -1;
        int s1 = -1;

        // find the leading-edge bin at half-height: 
        for (int bin=thresholdCrossingBin-1; bin<Math.min(waveform.length-1,pulsePeakBin+1); bin++) {
            if (waveform[bin]<=halfMax && waveform[bin+1]>halfMax) {
                s0 = bin;
                break;
            }
        }

        // find the trailing-edge bin at half-height:
        for (int bin=pulsePeakBin; bin<Math.min(waveform.length-1,thresholdCrossingBin+nsa); bin++) {
            if (waveform[bin]>halfMax && waveform[bin+1]<=halfMax) {
                s1 = bin;
                break;
            }
        }

        // set the leading-edge times: 
        if(s0 > 0) {
            // set coarse time to be the sample before the 50% crossing
            tcoarse = s0;
            // set the fine time from interpolation between the two samples before and after the 50% crossing (6 bits resolution)
            tfine   = ((int) ((halfMax - waveform[s0])/(waveform[s0+1]-waveform[s0]) * 64));
            t0      = (tcoarse << 6) + tfine;
            // set the width based on trailing-edge time:
            if (s1 > 0) {
                pulseWidth  = s1 - s0;
            }
        }
 
        return true;
    }

    @Override
    public void fit(DetectorDataDgtz.ADCData data) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
