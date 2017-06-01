package org.jlab.rec.tof.hit;

public interface IGetCalibrationParams {
    // the Calibration constants representing L (1) or R (2) parameters for the
    // FTOF and U (1) or D (2) parameters for the CTOF

    public double TW01();

    public double TW02();

    public double TW11();

    public double TW12();

    public double lambda1();

    public double lambda2();

    public double lambda1Unc();

    public double lambda2Unc();

    public double yOffset();

    public double v1();

    public double v2();

    public double v1Unc();

    public double v2Unc();

    public double PED1();

    public double PED2();

    public double PED1Unc();

    public double PED2Unc();

    public double ADC1Unc();

    public double ADC2Unc();

    public double TDC1Unc();

    public double TDC2Unc();

    public double PaddleToPaddle();

    public double TimeOffset();

    public double[] LSBConversion();

    public double LSBConversionUnc();

    public double ADC_MIP();

    public double ADC_MIPUnc();

    public double DEDX_MIP();

    public double ScinBarThickn();

}
