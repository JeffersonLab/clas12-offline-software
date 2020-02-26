package org.jlab.rec.tof.banks;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jlab.utils.groups.IndexedTable;

public class FTOFMatch implements IMatchedHit {

    @Override
    public String DetectorName() {
        return "FTOF";
    }

    @Override
    public List<BaseHit> MatchHits(ArrayList<BaseHit> ADCandTDCLists, double timeJitter, IndexedTable tdcConv, IndexedTable ADCandTDCOffsets) {
        ArrayList<BaseHit> matchLists = new ArrayList<BaseHit>();

        if (ADCandTDCLists != null) {
            Collections.sort(ADCandTDCLists);
            double t1 = -1;
            double t2 = -1; // t1, t2 not yet used in selection
            int adc1 = -1;
            int adc2 = -1;
            int tdc1 = -1;
            int tdc2 = -1;

            List<ArrayList<BaseHit>> hitlists = new ArrayList<ArrayList<BaseHit>>();
            for (int i = 0; i < ADCandTDCLists.size(); i++) {
                hitlists.add(new ArrayList<BaseHit>());
            }
            int index1 = 0;
            int index2 = 0;
            int index3 = 0;
            int index4 = 0;

            for (int i = 0; i < ADCandTDCLists.size(); i++) {
                BaseHit h = ADCandTDCLists.get(i);
                if (h.get_ADC1() > 0) {
                    adc1 = h.get_ADC1();
                    if (h.get_ADCTime1() > 0) {
                        t1 = h.get_ADCTime1();
                    }

                    hitlists.get(index1).add(h);
                    index1++;
                }
                if (h.get_ADC2() > 0) {
                    adc2 = h.get_ADC2();
                    if (h.get_ADCTime2() > 0) {
                        t2 = h.get_ADCTime2();
                    }

                    if (adc1 > 0 && Math.abs(adc1 - adc2) < 8000) {
                        hitlists.get(index2).add(h); // matched hit
                        index2++;
                    }
                    if (adc1 == -1) {
                        hitlists.get(index2).add(h); // not matched hit
                        index2++;
                    }
                }
                if (h.get_TDC1() > 0) {
                    tdc1 = h.get_TDC1();
                    hitlists.get(index3).add(h);
                    index3++;
                }
                if (h.get_TDC2() > 0) {
                    tdc2 = h.get_TDC2();
                    if (tdc1 > 0 && Math.abs(tdc1 - tdc2) * 24. / 1000. < 35) {
                        hitlists.get(index4).add(h);
                        index4++;
                    }
                    if (tdc1 == -1) {
                        hitlists.get(index4).add(h); // not matched hit
                        index4++;
                    }
                }
            }
            int hitNb = 0;
            for (int i = 0; i < hitlists.size(); i++) {
                if (hitlists.get(i).size() > 0) {
                    // Make the new hit
                    BaseHit hit = new BaseHit(hitlists.get(i).get(0)
                            .get_Sector(), hitlists.get(i).get(0).get_Layer(),
                            hitlists.get(i).get(0).get_Component());
                    hit.set_Id(hitNb++);
                    double t_1 = -1;
                    double t_2 = -1;
                    int ped_1 = -1;
                    int ped_2 = -1;
                    int adc_1 = -1;
                    int adc_2 = -1;
                    int tdc_1 = -1;
                    int tdc_2 = -1;

                    for (BaseHit h : hitlists.get(i)) {
                        if (h.get_ADC1() > 0) {
                            adc_1 = h.get_ADC1();
                            if (h.get_ADCTime1() > 0) {
                                t_1 = h.get_ADCTime1();
                            }
                            if (h.get_ADCpedestal1() > 0) {
                                ped_1 = h.get_ADCpedestal1();
                            }
                        }
                        if (h.get_ADC2() > 0) {
                            adc_2 = h.get_ADC2();
                            if (h.get_ADCTime2() > 0) {
                                t_2 = h.get_ADCTime2();
                            }
                            if (h.get_ADCpedestal2() > 0) {
                                ped_2 = h.get_ADCpedestal2();
                            }
                        }
                        if (h.get_TDC1() > 0) {
                            tdc_1 = h.get_TDC1();
                        }
                        if (h.get_TDC2() > 0) {
                            tdc_2 = h.get_TDC2();
                        }
                        hit.ADC1 = adc_1;
                        hit.ADC2 = adc_2;
                        hit.TDC1 = tdc_1;
                        hit.TDC2 = tdc_2;
                        hit.ADCpedestal1 = ped_1;
                        hit.ADCpedestal2 = ped_2;
                        hit.ADCTime1 = t_1;
                        hit.ADCTime2 = t_2;

                        matchLists.add(hit);
                        System.out.println(i + ")  s " + h.get_Sector() + " l "
                                + h.get_Layer() + " c " + h.get_Component()
                                + " adcL " + h.get_ADC1() + " adcR "
                                + h.get_ADC2() + " tdcL " + h.get_TDC1()
                                + " tdcR " + h.get_TDC2());
                    }
                }
            }

        }

        return matchLists;
    }

    public static void main(String arg[]) throws IOException {
    }

}
