package org.jlab.rec.tof.banks.ftof;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.jlab.detector.geant4.v2.FTOFGeant4Factory;
import org.jlab.detector.hits.DetHit;
import org.jlab.detector.hits.FTOFDetHit;
import org.jlab.geom.prim.Point3D;
import org.jlab.geometry.prim.Line3d;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.tof.banks.BaseHit;
import org.jlab.rec.tof.banks.BaseHitReader;
import org.jlab.rec.tof.banks.IMatchedHit;
import org.jlab.rec.tof.hit.ftof.Hit;
import org.jlab.utils.groups.IndexedTable;

/**
 *
 * @author ziegler
 *
 */
public class HitReader implements IMatchedHit {

    public HitReader() {
        // TODO Auto-generated constructor stub
    }

    private List<Hit> _FTOF1AHits;
    private List<Hit> _FTOF1BHits;
    private List<Hit> _FTOF2Hits;

    public List<Hit> get_FTOF1AHits() {
        return _FTOF1AHits;
    }

    public void set_FTOF1AHits(List<Hit> _FTOF1AHits) {
        this._FTOF1AHits = _FTOF1AHits;
    }

    public List<Hit> get_FTOF1BHits() {
        return _FTOF1BHits;
    }

    public void set_FTOF1BHits(List<Hit> _FTOF1BHits) {
        this._FTOF1BHits = _FTOF1BHits;
    }

    public List<Hit> get_FTOF2Hits() {
        return _FTOF2Hits;
    }

    public void set_FTOF2Hits(List<Hit> _FTOF2Hits) {
        this._FTOF2Hits = _FTOF2Hits;
    }

    private int _numTrks;

    /**
     *
     * @param event the evio event
     * @param geometry the FTOF geometry from package
     */
    public void fetch_Hits(DataEvent event, long timeStamp, FTOFGeant4Factory geometry,
            List<Line3d> trks, double[] paths, int[] ids, 
            IndexedTable constants0, 
            IndexedTable constants1, 
            IndexedTable constants2, 
            IndexedTable constants3, 
            IndexedTable constants4, 
            IndexedTable constants5, 
            IndexedTable constants6, 
            IndexedTable constants7) {/*
        0: "/calibration/ftof/attenuation"),
        1: "/calibration/ftof/effective_velocity"),
        2: "/calibration/ftof/time_offsets"),
        3: "/calibration/ftof/time_walk"),
        4: "/calibration/ftof/status"),
        5: "/calibration/ftof/gain_balance"),
        6: "/calibration/ftof/tdc_conv"),
        7: "/calibration/ftof/time_jitter") );
        */
        _numTrks = trks.size();

        double triggerPhase = this.getTriggerPhase(timeStamp, constants7);
        
        BaseHitReader hitReader = new BaseHitReader();
        IMatchedHit MH = this;
        List<BaseHit> hitList = hitReader.get_MatchedHits(event, MH, triggerPhase, constants6, constants7);

        if (hitList.size() == 0) {
            // System.err.println("there is no FTOF bank ");

            _FTOF1AHits = new ArrayList<Hit>();
            _FTOF1BHits = new ArrayList<Hit>();
            _FTOF2Hits = new ArrayList<Hit>();

            return;
        }
        
        // Instantiates the lists of hits
        List<Hit> hits = new ArrayList<Hit>();

        int[] id = new int[hitList.size()];
        int[] sector = new int[hitList.size()];
        int[] panel = new int[hitList.size()];
        int[] paddle = new int[hitList.size()];
        int[] ADCL = new int[hitList.size()];
        int[] ADCR = new int[hitList.size()];
        int[] TDCL = new int[hitList.size()];
        int[] TDCR = new int[hitList.size()];
        int[] ADCLIdx = new int[hitList.size()];
        int[] ADCRIdx = new int[hitList.size()];
        int[] TDCLIdx = new int[hitList.size()];
        int[] TDCRIdx = new int[hitList.size()];

        for (int i = 0; i < hitList.size(); i++) {
            id[i] = (i + 1);
            sector[i] = hitList.get(i).get_Sector();
            panel[i] = hitList.get(i).get_Layer();
            paddle[i] = hitList.get(i).get_Component();
            ADCL[i] = hitList.get(i).ADC1;
            ADCR[i] = hitList.get(i).ADC2;
            TDCL[i] = hitList.get(i).TDC1;
            TDCR[i] = hitList.get(i).TDC2;
            ADCLIdx[i] = hitList.get(i).ADCbankHitIdx1;
            ADCRIdx[i] = hitList.get(i).ADCbankHitIdx2;
            TDCLIdx[i] = hitList.get(i).TDCbankHitIdx1;
            TDCRIdx[i] = hitList.get(i).TDCbankHitIdx2;

            /*
			 * System.out.println("hit "+hitList.get(i).get_Id()+ " sector "+
			 * hitList.get(i).get_Sector()+ " panel "+
			 * hitList.get(i).get_Layer()+ " paddle "+
			 * hitList.get(i).get_Component()+ " ADCL "+ hitList.get(i).ADC1+
			 * " ADCR "+ hitList.get(i).ADC2+ " TDCL "+ hitList.get(i).TDC1+
			 * " TDCR "+ hitList.get(i).TDC2);
             */
            if (passADC(ADCL[i]) == 0 || passADC(ADCR[i]) == 0
                    || passTDC(TDCL[i]) == 0 || passTDC(TDCR[i]) == 0) {
                continue;
            }

            // get the status
            //int statusL = CCDBConstants.getSTATUSL()[sector[i] - 1][panel[i] - 1][paddle[i] - 1];
            //int statusR = CCDBConstants.getSTATUSR()[sector[i] - 1][panel[i] - 1][paddle[i] - 1];
            
            //String statusWord = this.set_StatusWord(statusL, statusR, ADCL[i],
            //        TDCL[i], ADCR[i], TDCR[i]);

            // create the hit object
            Hit hit = new Hit(id[i], panel[i], sector[i], paddle[i], ADCL[i],
                    TDCL[i], ADCR[i], TDCR[i]);
            hit.set_ADCbankHitIdx1(ADCLIdx[i]);
            hit.set_ADCbankHitIdx2(ADCRIdx[i]);
            hit.set_TDCbankHitIdx1(TDCLIdx[i]);
            hit.set_TDCbankHitIdx2(TDCRIdx[i]);
            //hit.set_StatusWord(statusWord);
            hit.set_StatusWord(this.set_StatusWord(hit.Status1(constants4), hit.Status2(constants4), ADCL[i], TDCL[i], ADCR[i], TDCR[i]));
            hit.setPaddleLine(geometry);
            // add this hit
            hits.add(hit);
        }
        List<Hit> updated_hits = matchHitsToDCTrk(hits, geometry, trks, paths, ids);

        ArrayList<ArrayList<Hit>> DetHits = new ArrayList<ArrayList<Hit>>();
        for (int j = 0; j < 3; j++) {
            DetHits.add(j, new ArrayList<Hit>());
        }

        for (Hit hit : updated_hits) {
            // set the layer to get the paddle position from the geometry
            // package
            hit.set_HitParameters(hit.get_Panel(), 
                triggerPhase,
                constants0, 
                constants1, 
                constants2, 
                constants3, 
                constants5, 
                constants6);
            // DetHits.get(hit.get_Panel()-1).add(hit);
        }
        // List<Hit> unique_hits = this.removeDuplicatedHits(updated_hits);

        for (Hit hit : updated_hits) {
            DetHits.get(hit.get_Panel() - 1).add(hit);
        }
        if (DetHits.get(0).size() > 0) {
            Collections.sort(DetHits.get(0));
            // fill the list of TOF hits
            this.set_FTOF1AHits(DetHits.get(0));
        }
        if (DetHits.get(1).size() > 0) {
            Collections.sort(DetHits.get(1));
            // fill the list of TOF hits
            this.set_FTOF1BHits(DetHits.get(1));
        }
        if (DetHits.get(2).size() > 0) {
            Collections.sort(DetHits.get(2));
            // fill the list of TOF hits
            this.set_FTOF2Hits(DetHits.get(2));
        }
    }

    private List<Hit> removeDuplicatedHits(List<Hit> updated_hits) {

        List<Hit> unique_hits = new ArrayList<Hit>();

        ArrayList<ArrayList<Hit>> lists = new ArrayList<ArrayList<Hit>>();
        for (int j = 0; j < this._numTrks; j++) {
            lists.add(new ArrayList<Hit>());
        }

        for (Hit h : updated_hits) {
            if (h._AssociatedTrkId == -1) {
                unique_hits.add(h);
            }
            if (h._AssociatedTrkId != -1) {
                lists.get(h._AssociatedTrkId - 1).add(h);
            }
        }
        for (int j = 0; j < this._numTrks; j++) {
            if (lists.get(j).size() > 0) {
                Hit bestMatch = null;
                double delta = Double.POSITIVE_INFINITY;
                double delta_new = Double.POSITIVE_INFINITY;
                for (Hit h : lists.get(j)) {
                    delta_new = h.get_TrkPosition().distance(h.get_Position());
                    if (delta_new < delta) {
                        bestMatch = h;
                        delta = delta_new;
                    }
                }

                if (bestMatch != null) {
                    unique_hits.add(bestMatch);
                }
            }
        }
        return unique_hits;
    }

    public String set_StatusWord(int statusL, int statusR, int ADCL, int TDCL,
            int ADCR, int TDCR) {
        String statusWord = new String(); // ADCL TDCL ADCR TDCR
        // selected ranges TDC in [0,1000], ADC in [0, 8192] requirement given
        // by passTDC and passADC methods

        switch (statusL) {
            case 0:
                statusWord = ("" + 1 * passADC(ADCL) + "" + 1 * passTDC(TDCL) + "");// fully
                // functioning
                break;
            case 1:
                statusWord = ("0" + "" + 1 * passTDC(TDCL) + ""); // no ADC
                break;
            case 2:
                statusWord = ("" + 1 * passADC(ADCL) + "" + "0"); // no TDC
                break;
            case 3:
                statusWord = "00"; // no TDC, no ADC
                break;
        }
        switch (statusR) {
            case 0:
                statusWord += ("" + 1 * passADC(ADCR) + "" + 1 * passTDC(TDCR) + "");// fully
                // functioning
                break;
            case 1:
                statusWord += ("0" + "" + 1 * passTDC(TDCR) + ""); // no ADC
                break;
            case 2:
                statusWord += ("" + 1 * passADC(ADCR) + "" + "0"); // no TDC
                break;
            case 3:
                statusWord += "00"; // no TDC, no ADC
                break;

        }
        return statusWord;

    }

    private int passTDC(int tDC) {
        // selected ranges TDC
        int pass = 0;
        // if(Constants.LSBCONVFAC*tDC>Constants.TDCMINSCALE &&
        // Constants.LSBCONVFAC*tDC<Constants.TDCMAXSCALE)
        // pass = 1;
        if (tDC > 0) {
            pass = 1;
        }
        return pass;
    }

    private int passADC(int aDC) {
        // selected ranges ADC
        int pass = 0;
        // if(aDC>Constants.ADCMIN && aDC<Constants.ADCMAX)
        // pass = 1;
        if (aDC > 0) {
            pass = 1;
        }
        return pass;
    }

    private List<Hit> matchHitsToDCTrk(List<Hit> FTOFhits,
            FTOFGeant4Factory ftofDetector, List<Line3d> trks, double[] paths, int[] ids) {
        if (trks == null || trks.size() == 0) {
            return FTOFhits; // no hits were matched with DC tracks
        }
        Map<String, Hit> ftofHits = new LinkedHashMap<String, Hit>();
        
        // Instantiates the list of hits
        List<Hit> hitList = new ArrayList<Hit>();
        
        String det ;
        for (Hit fhit : FTOFhits) {
            det = new String("S");
            det+=fhit.get_Sector();
            det+="L";
            det+=fhit.get_Panel();
            det+="C";
            det+=fhit.get_Paddle();
            ftofHits.put(det, fhit);
        }
        for (int i = 0; i < trks.size(); i++) { // looping over the tracks find
            // the intersection of the track
            // with that plane
            Line3d trk = trks.get(i);
            List<DetHit> hits = ftofDetector.getIntersections(trk);
            String mdet ; 
            if (hits != null && hits.size() > 0) {
                for (DetHit hit : hits) {
                    FTOFDetHit fhit = new FTOFDetHit(hit);
                    
                    // add buffer for matched hits
                    //----------------------------
                    for (int j = -1; j<=1; j++) {
                        mdet = new String("S");
                        mdet+=fhit.getSector();
                        mdet+="L";
                        mdet+=fhit.getLayer();
                        mdet+="C";
                        mdet+=fhit.getPaddle() + j;

                        if(ftofHits.containsKey(mdet)) {
                            ftofHits.get(mdet)._AssociatedTrkId = ids[i];
                            ftofHits.get(mdet).set_matchedTrackHit(fhit);
                            ftofHits.get(mdet).set_matchedTrack(trks.get(i));
                            double deltaPath = trks.get(i).origin().distance(
                                        fhit.mid());
                            double pathLenTruBar = fhit.origin().distance(
                                    fhit.end());
                            ftofHits.get(mdet).set_TrkPathLenThruBar(pathLenTruBar);
                            ftofHits.get(mdet).set_TrkPathLen(paths[i] + deltaPath);
                            // get the coordinates for the track hit, which is defined
                            // as the mid-point between its entrance and its exit from
                            // the bar
                            ftofHits.get(mdet).set_TrkPosition(new Point3D(fhit.mid().x,
                                    fhit.mid().y, fhit.mid().z));
                            // compute the local y at the middle of the bar :
                            // ----------------------------------------------
                            Point3D origPaddleLine = ftofHits.get(mdet).get_paddleLine().origin();
                            Point3D trkPosinMidlBar = new Point3D(fhit.mid().x,
                                    fhit.mid().y, fhit.mid().z);
                            double Lov2 = ftofHits.get(mdet).get_paddleLine().length() / 2;
                            double barOrigToTrkPos = origPaddleLine
                                    .distance(trkPosinMidlBar);
                            // local y:
                            ftofHits.get(mdet).set_yTrk(barOrigToTrkPos - Lov2);
                           // hitList.add(ftofHits.get(mdet));
                        }
                    }
                }
            }
        }
        for (Map.Entry<String, Hit> hits : ftofHits.entrySet()) {
            hitList.add(hits.getValue());
        }
        
        return hitList;
    }

    @Override
    public String DetectorName() {
        return "FTOF";
    }

    @Override
    public List<BaseHit> MatchHits(ArrayList<BaseHit> ADCandTDCLists, double timeJitter, IndexedTable tdcConv, IndexedTable ADCandTDCOffsets) {
        ArrayList<BaseHit> matchLists = new ArrayList<BaseHit>();

        if (ADCandTDCLists != null) {
            Collections.sort(ADCandTDCLists);
//            System.out.println("Sorted");
//            for (BaseHit h : ADCandTDCLists) {
//                System.out.println(" : " + h.get_Sector() + " " + h.get_Layer() + " " + h.get_Component() + " "
//                        + h.ADC1 + " " + h.ADC2 + " " + h.TDC1 + " " + h.TDC2 + " i: " + h.ADCbankHitIdx1 + " " + h.ADCbankHitIdx2
//                        + " " + h.TDCbankHitIdx1 + " " + h.TDCbankHitIdx2);
//            }
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
                    int adc_idx1 = -1;
                    int adc_idx2 = -1;
                    int tdc_idx1 = -1;
                    int tdc_idx2 = -1;

                    for (BaseHit h : hitlists.get(i)) {
                        if (h.get_ADC1() > 0) {
                            adc_1 = h.get_ADC1();
                            adc_idx1 = h.ADCbankHitIdx1;
                            if (h.get_ADCTime1() > 0) {
                                t_1 = h.get_ADCTime1();
                            }
                            if (h.get_ADCpedestal1() > 0) {
                                ped_1 = h.get_ADCpedestal1();
                            }
                        }
                        if (h.get_ADC2() > 0) {
                            adc_2 = h.get_ADC2();
                            adc_idx2 = h.ADCbankHitIdx2;
                            if (h.get_ADCTime2() > 0) {
                                t_2 = h.get_ADCTime2();
                            }
                            if (h.get_ADCpedestal2() > 0) {
                                ped_2 = h.get_ADCpedestal2();
                            }
                        }
                        if (h.get_TDC1() > 0) {
                            tdc_1 = h.get_TDC1();
                            tdc_idx1 = h.TDCbankHitIdx1;
                        }
                        if (h.get_TDC2() > 0) {
                            tdc_2 = h.get_TDC2();
                            tdc_idx2 = h.TDCbankHitIdx2;
                        }
                    }
                    hit.ADC1 = adc_1;
                    hit.ADC2 = adc_2;
                    hit.TDC1 = tdc_1;
                    hit.TDC2 = tdc_2;
                    hit.ADCpedestal1 = ped_1;
                    hit.ADCpedestal2 = ped_2;
                    hit.ADCTime1 = t_1;
                    hit.ADCTime2 = t_2;
                    hit.ADCbankHitIdx1 = adc_idx1;
                    hit.ADCbankHitIdx2 = adc_idx2;
                    hit.TDCbankHitIdx1 = tdc_idx1;
                    hit.TDCbankHitIdx2 = tdc_idx2;

                    matchLists.add(hit);
//                    System.out.println(i+")  s "+hit.get_Sector()+" l "+hit.get_Layer()+" c "+hit.get_Component()+" adcL "+hit.get_ADC1()+" adcR "+hit.get_ADC2()+" tdcL "+
//                                                 hit.get_TDC1()+" tdcR "+hit.get_TDC2());

                }
            }

        }

        return matchLists;
    }
    
    private double getTriggerPhase(long timestamp, IndexedTable table) {
    // calculate the trigger time jitter correction
        double period = table.getDoubleValue("period", 0,0,0);
        int    phase  = table.getIntValue("phase", 0,0,0);
        int    cycles = table.getIntValue("cycles", 0,0,0);
        double triggerphase=0;
        if(cycles > 0) triggerphase=period*((timestamp+phase)%cycles);
//        System.out.println(period + " " + phase + " " + cycles + " " + timestamp + " " + triggerphase);
        return triggerphase;
    }
    
    public static void main(String arg[]) {
        System.out.println(" TRYING TO MATCH HITS");
        HitReader hr = new HitReader();
        ArrayList<BaseHit> ADCandTDCLists = new ArrayList<BaseHit>();
        BaseHit hit1 = new BaseHit(1,1,1);
        BaseHit hit2 = new BaseHit(1,1,1);
        BaseHit hit3 = new BaseHit(1,1,1);
        BaseHit hit4 = new BaseHit(1,1,1);
        BaseHit hit5 = new BaseHit(1,1,1);
        ADCandTDCLists.add(hit1);
        ADCandTDCLists.add(hit2);
        ADCandTDCLists.add(hit3);
        ADCandTDCLists.add(hit4);
        ADCandTDCLists.add(hit5);
        hit1.TDC1=1000;
        hit2.TDC2=1200;
        hit3.TDC2=500;
        hit4.ADC1=400;
        hit5.ADC2=460;
       
//       List<BaseHit> result = hr.MatchHits(ADCandTDCLists);
    }

}
