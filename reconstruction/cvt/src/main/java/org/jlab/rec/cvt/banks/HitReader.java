package org.jlab.rec.cvt.banks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jlab.clas.swimtools.Swim;
import org.jlab.detector.base.DetectorDescriptor;
import org.jlab.detector.base.DetectorType;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.cvt.Constants;
import org.jlab.rec.cvt.Geometry;
import org.jlab.rec.cvt.bmt.BMTGeometry;
import org.jlab.rec.cvt.bmt.BMTType;
import org.jlab.rec.cvt.hit.ADCConvertor;
import org.jlab.rec.cvt.hit.Hit;
import org.jlab.rec.cvt.hit.Strip;
import org.jlab.rec.cvt.svt.SVTGeometry;
import org.jlab.utils.groups.IndexedTable;

/**
 * A class to fill in lists of hits corresponding to reconstructed hits
 * characterized by the strip, its location in the detector (layer, sector), its
 * reconstructed time.
 *
 * @author ziegler
 *
 */
public class HitReader {

    public HitReader() {

    }

    // the list of BMT hits
    private List<Hit> _BMTHits;

    /**
     *
     * @return a list of BMT hits
     */
    public List<Hit> getBMTHits() {
        return _BMTHits;
    }

    /**
     * sets the list of BMT hits
     *
     * @param _BMTHits list of BMT hits
     */
    public void setBMTHits(List<Hit> _BMTHits) {
        this._BMTHits = _BMTHits;
    }
    // the list of SVT hits
    private List<Hit> _SVTHits;

    /**
     *
     * @return a list of SVT hits
     */
    public List<Hit> getSVTHits() {
        return _SVTHits;
    }

    /**
     * sets the list of SVT hits
     *
     * @param _SVTHits list of SVT hits
     */
    public void setSVTHits(List<Hit> _SVTHits) {
        this._SVTHits = _SVTHits;
    }

    /**
     * Gets the BMT hits from the BMT dgtz bank
     *
     * @param event the data event
     * @param swim
     * @param status
     * @param timeCuts
     */
    public void fetch_BMTHits(DataEvent event, Swim swim, IndexedTable status, IndexedTable timeCuts) {

        // return if there is no BMT bank
        if (event.hasBank("BMT::adc") == false) {
            //System.err.println("there is no BMT bank ");
            _BMTHits = new ArrayList<>();

            return;
        }

        // instanciates the list of hits
        List<Hit> hits = new ArrayList<>();
        // gets the BMT dgtz bank
        DataBank bankDGTZ = event.getBank("BMT::adc");
        // fills the arrays corresponding to the hit variables
        int rows = bankDGTZ.rows();

        if (event.hasBank("BMT::adc") == true) {
            
            double tmin = timeCuts.getDoubleValue("hit_min", 0,0,0);
            double tmax = timeCuts.getDoubleValue("hit_max", 0,0,0);
            
            for (int i = 0; i < rows; i++) {

                //if (bankDGTZ.getInt("ADC", i) < 1) {
                    //continue; // gemc assigns strip value -1 for inefficiencies, we only consider strips with values between 1 to the maximum strip number for a given detector
                //}                
                int sector  = bankDGTZ.getByte("sector", i);
                int layer   = bankDGTZ.getByte("layer", i);
                int strip   = bankDGTZ.getShort("component", i);
                double ADCtoEdep = bankDGTZ.getInt("ADC", i);
                double time      = bankDGTZ.getFloat("time", i);
               
                //fix for now... no adc in GEMC
                if (ADCtoEdep < 1) {
                    continue;
                }

                // create the strip object for the BMT
                Strip BmtStrip = new Strip(strip, ADCtoEdep, time);
                BmtStrip.setStatus(status.getIntValue("status", sector, layer, strip));
                if(Constants.getInstance().timeCuts) {
                    if(time!=0 && (time<tmin || time>tmax))
                        BmtStrip.setStatus(2);// calculate the strip parameters for the BMT hit
                }
                BmtStrip.calcBMTStripParams(sector, layer, swim); // for Z detectors the Lorentz angle shifts the strip measurement; calc_Strip corrects for this effect
                // create the hit object for detector type BMT
                
                Hit hit = new Hit(DetectorType.BMT, BMTGeometry.getDetectorType(layer), sector, layer, BmtStrip);                
                hit.setId(i+1);
                // add this hit
                if(hit.getLayer()+3!=Constants.getInstance().getRmReg())
                    hits.add(hit);
            }
            // fills the list of BMT hits
            Collections.sort(hits);
            this.setBMTHits(hits);
        }
    }

    /**
     * Gets the SVT hits from the BMT dgtz bank
     *
     * @param event the data event
     * @param omitLayer
     * @param omitHemisphere
     * @param status
     */
    public void fetch_SVTHits(DataEvent event, int omitLayer, int omitHemisphere, IndexedTable status) {

        if (event.hasBank("BST::adc") == false) {
            //System.err.println("there is no BST bank ");
            _SVTHits = new ArrayList<>();

            return;
        }

        List<Hit> hits = new ArrayList<>();

        DataBank bankDGTZ = event.getBank("BST::adc");
        int rows = bankDGTZ.rows();
        
        if (event.hasBank("BST::adc") == true) {
            //bankDGTZ.show();
            // first get tdcs
            Map<Integer, Double> tdcs = new HashMap<>();
            for (int i = 0; i < rows; i++) {                
                if(bankDGTZ.getInt("ADC", i) < 0) {
                    byte sector = bankDGTZ.getByte("sector", i);
                    byte layer  = bankDGTZ.getByte("layer", i);
                    short strip = bankDGTZ.getShort("component", i);
                    double time = bankDGTZ.getFloat("time", i);
                    int key = DetectorDescriptor.generateHashCode(sector, layer, strip);
                    if(tdcs.containsKey(key)) {
                        if(time<tdcs.get(key))
                            tdcs.replace(key, time);
                    }
                    else 
                        tdcs.put(key, time);
                }
            }
                
            // then get real hits
            for (int i = 0; i < rows; i++) {
                if (bankDGTZ.getInt("ADC", i) < 0) {
                    continue; // ignore hits TDC hits with ADC==-1 
                }
                
                int id      = i + 1;
                byte sector = bankDGTZ.getByte("sector", i);
                byte layer  = bankDGTZ.getByte("layer", i);
                short strip = bankDGTZ.getShort("component", i);
                int ADC     = bankDGTZ.getInt("ADC", i);
                double time = 0;//bankDGTZ.getFloat("time", i);
                int tdcstrip = 1;
                if(strip>128) tdcstrip = 129;
                int key = DetectorDescriptor.generateHashCode(sector, layer, tdcstrip);
                if(tdcs.containsKey(key)) {
                    time = tdcs.get(key);
                }
//                else {
//                    System.out.println("missing time for " + sector + " " + layer + " " + strip);
//                    for(int ii : tdcs.keySet()) {
//                        int s = (ii&0xFF000000)>>24;
//                        int l = (ii&0x00FF0000)>>16;
//                        int c = (ii&0x0000FFFF);
//                        System.out.println("\t"+s+"/"+l+"/"+c);
//                    }
//                    bankDGTZ.show();
//                }
                
                double angle = SVTGeometry.getSectorPhi(layer, sector);
                int hemisphere = (int) Math.signum(Math.sin(angle));
                if (sector == 7 && layer > 6) {
                    hemisphere = 1;
                }
                if (sector == 19 && layer > 6) {
                    hemisphere = -1;
                }
                if (omitHemisphere == -2) {
                    if(layer == omitLayer) {
                        continue;
                    }
                } else {
                    if (hemisphere == omitHemisphere && layer == omitLayer) {
                        continue;
                    }

                }
                // if the strip is out of range skip
                if (strip < 1) {
                    continue;
                }
                if (layer > 6) {
                    continue;
                }
                
                //if(adcConv.SVTADCtoDAQ(ADC[i], event)<50)
                //    continue;
                // create the strip object with the adc value converted to daq value used for cluster-centroid estimate
                Strip SvtStrip = new Strip(strip, ADCConvertor.SVTADCtoDAQ(ADC), time); 
                SvtStrip.setPitch(SVTGeometry.getPitch());
                // get the strip line
                SvtStrip.setLine(Geometry.getInstance().getSVT().getStrip(layer, sector, strip));
                SvtStrip.setModule(Geometry.getInstance().getSVT().getModule(layer, sector));
                SvtStrip.setNormal(Geometry.getInstance().getSVT().getNormal(layer, sector)); 
                // if the hit is useable in the analysis its status is =0
                if (SvtStrip.getEdep() == 0) {
                    SvtStrip.setStatus(1);
                }
//                if (Constants.getInstance().timeCuts) {
//                    if(time > 0 && (time < 150 || time > 350)) {
//                        SvtStrip.setStatus(2);// calculate the strip parameters for the BMT hit
//                    }
//                }
//                SvtStrip.setStatus(status.getIntValue("status", sector, layer, strip));
                
                // BMTGeometry implementation using the geometry package:  Charles Platt
//                Line3d shiftedStrip   = geo.getStrip(layer[i]-1, sector[i]-1, strip[i]-1);
//
 //               Vector3d o1            = shiftedStrip.origin();
 //               Vector3d e1            = shiftedStrip.end();

//                Point3D  MP  = new  Point3D(( o1.x + e1.x ) /2.,
 //                                           ( o1.y + e1.y ) /2.,
 //                                           ( o1.z + e1.z ) /2. );
 //               Vector3D Dir = new Vector3D((-o1.x + e1.x ),
 //                                           (-o1.y + e1.y ),
 //                                           (-o1.z + e1.z )     );

//                Point3D passVals = new Point3D(o1.x, o1.y, o1.z); //switch from Vector3d to Point3D
//                SvtStrip.setImplantPoint(passVals);


                // create the hit object
                Hit hit = new Hit(DetectorType.BST, BMTType.UNDEFINED, sector, layer, SvtStrip);
                hit.setId(id);
                // add this hit
                if(hit.getRegion()!=Constants.getInstance().getRmReg())      
                    hits.add(hit);
            }
        }
        // fill the list of SVT hits
        Collections.sort(hits);
        this.setSVTHits(hits);

    }

}
