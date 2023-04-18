package org.jlab.rec.tof.banks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jlab.detector.banks.RawDataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.utils.groups.IndexedTable;

public class BaseHitReader {

    public BaseHitReader() {
        // TODO Auto-generated constructor stub
    }

    public class DetectorLocation {

        int[] SLC = new int[3];

        DetectorLocation(int[] SecLayComp) {
            SLC = SecLayComp;
        }

        @Override
        public int hashCode() {
            int hc = this.SLC[0] * 10000 + this.SLC[1] * 1000 + this.SLC[2];
            return hc;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof DetectorLocation) {
                DetectorLocation loc = (DetectorLocation) obj;
                return (loc.SLC[0] == this.SLC[0] && loc.SLC[1] == this.SLC[1] && loc.SLC[2] == this.SLC[2]);
            } else {
                return false;
            }
        }

    }

    public Map<DetectorLocation, ArrayList<BaseHit>> get_Hits(DataEvent event,
            String detector) {

        String detADC = "";
        detADC += detector;
        detADC += "::adc";
        String detTDC = "";
        detTDC += detector;
        detTDC += "::tdc";

        if (event.hasBank(detADC) == false && event.hasBank(detTDC) == false) {
            return null;
        }

        Map<BaseHit, DetectorLocation> hmap = new HashMap<BaseHit, DetectorLocation>();

        int[] sectorADC = null;
        int[] layerADC = null;
        int[] componentADC = null;
        int[] ADC1 = null;
        int[] ADC2 = null;
        double[] time1 = null;
        int[] pedestalADC1 = null;
        double[] time2 = null;
        int[] pedestalADC2 = null;
        int[] ADCIdx1 = null;
        int[] ADCIdx2 = null;

        int[] sectorTDC = null;
        int[] layerTDC = null;
        int[] componentTDC = null;
        int[] TDC1 = null;
        int[] TDC2 = null;
        int[] TDCIdx1 = null;
        int[] TDCIdx2 = null;

        if (event.hasBank(detADC) == true) {
            
            RawDataBank bank = new RawDataBank(detADC);
            bank.read(event);
            //DataBank bank = event.getBank(detADC);
            
            int bankSize = bank.rows();

            sectorADC = new int[bankSize];
            layerADC = new int[bankSize];
            componentADC = new int[bankSize];
            ADC1 = new int[bankSize];
            ADC2 = new int[bankSize];
            time1 = new double[bankSize];
            pedestalADC1 = new int[bankSize];
            time2 = new double[bankSize];
            pedestalADC2 = new int[bankSize];
            ADCIdx1 = new int[bankSize];
            ADCIdx2 = new int[bankSize];

            for (int i = 0; i < bankSize; i++) {
                sectorADC[i] = bank.getByte("sector", i);
                layerADC[i] = bank.getByte("layer", i);
                componentADC[i] = bank.getShort("component", i);
                int order = bank.trueOrder(i);
                int ADC = bank.getInt("ADC", i);
                double time = bank.getFloat("time", i);
                int pedestalADC = bank.getShort("ped", i);

                ADC1[i] = -1;
                ADC2[i] = -1;
                time1[i] = -1;
                pedestalADC1[i] = -1;
                time2[i] = -1;
                pedestalADC2[i] = -1;

                if (order == 0) {
                    ADC1[i] = ADC;
                    time1[i] = time;
                    pedestalADC1[i] = pedestalADC;
                    ADCIdx1[i] = i;
                }
                if (order == 1) {
                    ADC2[i] = ADC;
                    time2[i] = time;
                    pedestalADC2[i] = pedestalADC;
                    ADCIdx2[i] = i;
                }

                BaseHit newHit = new BaseHit(sectorADC[i], layerADC[i],
                        componentADC[i]);

                newHit.ADC1 = ADC1[i];
                newHit.ADC2 = ADC2[i];
                newHit.ADCpedestal1 = pedestalADC1[i];
                newHit.ADCTime1 = time1[i];
                newHit.ADCpedestal2 = pedestalADC2[i];
                newHit.ADCTime2 = time2[i];
                newHit.ADCbankHitIdx1 = ADCIdx1[i];
                newHit.ADCbankHitIdx2 = ADCIdx2[i];

                int[] _SLC = {newHit.get_Sector(), newHit.get_Layer(),
                    newHit.get_Component()};
                DetectorLocation DL = new DetectorLocation(_SLC);
                hmap.put(newHit, DL);

            }
        }
        if (event.hasBank(detTDC) == true) {
            
            RawDataBank bank = new RawDataBank(detTDC);
            bank.read(event);
            //DataBank bank = event.getBank(detTDC);
            
            int bankSize = bank.rows();

            sectorTDC = new int[bankSize];
            layerTDC = new int[bankSize];
            componentTDC = new int[bankSize];
            TDC1 = new int[bankSize];
            TDC2 = new int[bankSize];
            TDCIdx1 = new int[bankSize];
            TDCIdx2 = new int[bankSize];

            for (int i = 0; i < bankSize; i++) {
                sectorTDC[i] = bank.getByte("sector", i);
                layerTDC[i] = bank.getByte("layer", i);
                componentTDC[i] = bank.getShort("component", i);
                int order = bank.trueOrder(i);
                int TDC = bank.getInt("TDC", i);

                TDC1[i] = -1;
                TDC2[i] = -1;

                if (order == 2) {
                    TDC1[i] = TDC;
                    TDCIdx1[i] = i;
                }
                if (order == 3) {
                    TDC2[i] = TDC;
                    TDCIdx2[i] = i;
                }

                BaseHit newHit = new BaseHit(sectorTDC[i], layerTDC[i],
                        componentTDC[i]);
                newHit.TDC1 = TDC1[i];
                newHit.TDC2 = TDC2[i];
                newHit.TDCbankHitIdx1 = TDCIdx1[i];
                newHit.TDCbankHitIdx2 = TDCIdx2[i];

                int[] _SLC = {newHit.get_Sector(), newHit.get_Layer(),
                    newHit.get_Component()};
                DetectorLocation DL = new DetectorLocation(_SLC);
                hmap.put(newHit, DL);
            }
        }

        Map<DetectorLocation, ArrayList<BaseHit>> reverseMap = new HashMap<>();

        for (Map.Entry<BaseHit, DetectorLocation> entry : hmap.entrySet()) {

            if (!reverseMap.containsKey(entry.getValue())) {
                reverseMap.put(entry.getValue(), new ArrayList<BaseHit>());
            }

            ArrayList<BaseHit> keys = reverseMap.get(entry.getValue());
            keys.add(entry.getKey());
            reverseMap.put(entry.getValue(), keys);
        }

        return reverseMap;

    }

    public List<BaseHit> get_MatchedHits(DataEvent event, IMatchedHit MH, double jitter, IndexedTable tdcconv, IndexedTable offsets) {
        List<BaseHit> finalHitList = new ArrayList<BaseHit>();

        Map<DetectorLocation, ArrayList<BaseHit>> hitMap = this.get_Hits(event,
                MH.DetectorName());
        if (hitMap != null) {

            Set entrySet = hitMap.entrySet();
            Iterator it = entrySet.iterator();

            while (it.hasNext()) {
                Map.Entry me = (Map.Entry) it.next();
                ArrayList<BaseHit> hitList = (ArrayList<BaseHit>) me.getValue();

                finalHitList.addAll(MH.MatchHits(hitList,jitter,tdcconv,offsets));
            }
        }

        return finalHitList;
    }
}
