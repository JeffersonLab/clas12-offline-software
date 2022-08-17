package org.jlab.detector.decode;

import org.jlab.detector.decode.DetectorDataDgtz.ADCData;

/**
 *
 * @author gavalian
 */
public interface IFADCFitter {
    void fit(ADCData data);
}
