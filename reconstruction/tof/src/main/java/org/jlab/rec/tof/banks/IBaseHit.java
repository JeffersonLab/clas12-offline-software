package org.jlab.rec.tof.banks;

public interface IBaseHit {

    public int get_Sector();

    public int get_Layer();

    public int get_Component();

    public int get_ADC1();

    public int get_ADC2();

    public int get_TDC1();

    public int get_TDC2();

    public double get_ADCTime1();

    public int get_ADCpedestal1();

    public double get_ADCTime2();

    public int get_ADCpedestal2();

}
