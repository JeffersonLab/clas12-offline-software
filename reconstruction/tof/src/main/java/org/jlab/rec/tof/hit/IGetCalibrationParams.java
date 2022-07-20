package org.jlab.rec.tof.hit;

import org.jlab.utils.groups.IndexedTable;

public interface IGetCalibrationParams {
    // the Calibration constants representing L (1) or R (2) parameters for the
    // FTOF and U (1) or D (2) parameters for the CTOF

    public double TW01(IndexedTable tab);

    public double TW02(IndexedTable tab);

    public double TW11(IndexedTable tab);

    public double TW12(IndexedTable tab);

    public double TW1P(IndexedTable tab);

    public double TW2P(IndexedTable tab);

    public double TW0E(IndexedTable tab);

    public double TW1E(IndexedTable tab);

    public double TW2E(IndexedTable tab);

    public double TW3E(IndexedTable tab);

    public double TW4E(IndexedTable tab);

    public double HPOSa(IndexedTable tab);

    public double HPOSb(IndexedTable tab);

    public double HPOSc(IndexedTable tab);

    public double HPOSd(IndexedTable tab);

    public double HPOSe(IndexedTable tab);

    public double[] HPOSBIN(IndexedTable tab);

    public double lambda1(IndexedTable tab);

    public double lambda2(IndexedTable tab);

    public double lambda1Unc(IndexedTable tab);

    public double lambda2Unc(IndexedTable tab);

    public double yOffset(IndexedTable tab);

    public double v1(IndexedTable tab);

    public double v2(IndexedTable tab);

    public double v1Unc(IndexedTable tab);

    public double v2Unc(IndexedTable tab);

    public double PED1();

    public double PED2();

    public double PED1Unc();

    public double PED2Unc();

    public double ADC1Unc();

    public double ADC2Unc();

    public double TDC1Unc();

    public double TDC2Unc();

    public double PaddleToPaddle(IndexedTable tab);

    public double TimeOffset(IndexedTable tab);

    public double[] LSBConversion(IndexedTable tab);

    public double LSBConversionUnc();

    public double ADC_MIP(IndexedTable tab);

    public double ADC_MIPUnc(IndexedTable tab);

    public double DEDX_MIP();

    public int Status1(IndexedTable tab);
    
    public int Status2(IndexedTable tab);

}
