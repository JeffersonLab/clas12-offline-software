package org.jlab.clas.detector;

/**
 *
 * @author baltzell
 */
public class DetectorResponseFactory {
    
    public static DetectorResponse create(DetectorResponse r) {
        if (r instanceof ScintillatorResponse) {
            return new ScintillatorResponse((ScintillatorResponse)r);
        }
        else if (r instanceof CalorimeterResponse) {
            return new CalorimeterResponse((CalorimeterResponse)r);
        }
        else if (r instanceof CherenkovResponse) {
            return new CherenkovResponse((CherenkovResponse)r);
        }
        else if (r instanceof RingCherenkovResponse) {
            return new RingCherenkovResponse((RingCherenkovResponse)r);
        }
        else if (r instanceof TaggerResponse) {
            return new TaggerResponse((TaggerResponse)r);
        }
        else {
            return new DetectorResponse(r);
        }
    }

}
