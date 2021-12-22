package org.jlab.rec.cvt.banks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jlab.clas.swimtools.Swim;
import org.jlab.detector.base.DetectorDescriptor;
import org.jlab.detector.base.DetectorType;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.cvt.Constants;
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
    public List<Hit> get_BMTHits() {
        return _BMTHits;
    }

    /**
     * sets the list of BMT hits
     *
     * @param _BMTHits list of BMT hits
     */
    public void set_BMTHits(List<Hit> _BMTHits) {
        this._BMTHits = _BMTHits;
    }
    // the list of SVT hits
    private List<Hit> _SVTHits;

    /**
     *
     * @return a list of SVT hits
     */
    public List<Hit> get_SVTHits() {
        return _SVTHits;
    }

    /**
     * sets the list of SVT hits
     *
     * @param _SVTHits list of SVT hits
     */
    public void set_SVTHits(List<Hit> _SVTHits) {
        this._SVTHits = _SVTHits;
    }

    /**
     * Gets the BMT hits from the BMT dgtz bank
     *
     * @param event the data event
     * @param adcConv converter from adc to values used in the analysis (i.e.
     * Edep for gemc, adc for cosmics)
     * @param geo the BMT geometry
     */
    public void fetch_BMTHits(DataEvent event, ADCConvertor adcConv, BMTGeometry geo, Swim swim, IndexedTable status, IndexedTable timeCuts) {

        // return if there is no BMT bank
        if (event.hasBank("BMT::adc") == false) {
            //System.err.println("there is no BMT bank ");
            _BMTHits = new ArrayList<Hit>();

            return;
        }

        // instanciates the list of hits
        List<Hit> hits = new ArrayList<Hit>();
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
                if(Constants.TIMECUTS) {
                    if(time!=0 && (time<tmin || time>tmax))
                        BmtStrip.setStatus(2);// calculate the strip parameters for the BMT hit
                }
                BmtStrip.calc_BMTStripParams(geo, sector, layer, swim); // for Z detectors the Lorentz angle shifts the strip measurement; calc_Strip corrects for this effect
                // create the hit object for detector type BMT
                
                Hit hit = new Hit(DetectorType.BMT, BMTGeometry.getDetectorType(layer), sector, layer, BmtStrip);                
                hit.set_Id(i+1);
                // add this hit
                if(hit.get_Layer()+3!=Constants.getRmReg())
                    hits.add(hit);
            }
            // fills the list of BMT hits
            this.set_BMTHits(hits);
        }
    }

    /**
     * Gets the SVT hits from the BMT dgtz bank
     *
     * @param event the data event
     * @param adcConv converter from adc to daq values
     * @param geo the SVT geometry
     */
    public void fetch_SVTHits(DataEvent event, ADCConvertor adcConv, int omitLayer, int omitHemisphere, SVTGeometry geo, IndexedTable status) {

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
                int key = DetectorDescriptor.generateHashCode(sector, layer,(short) (strip/128)+1);
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
                Strip SvtStrip = new Strip(strip, adcConv.SVTADCtoDAQ(ADC), time); 
                SvtStrip.set_Pitch(SVTGeometry.getPitch());
                // get the strip line
                SvtStrip.set_Line(geo.getStrip(layer, sector, strip));
                SvtStrip.set_Module(geo.getModule(layer, sector));
                SvtStrip.set_Normal(geo.getNormal(layer, sector)); 
                if(layer%2==0) {
                    SvtStrip.setToverX0(2*SVTGeometry.getToverX0());
                    SvtStrip.setZoverA(SVTGeometry.getZoverA());
                    SvtStrip.setMatT(SVTGeometry.getMaterialThickness());
                }
                else {
                    SvtStrip.setToverX0(0);
                }
                // if the hit is useable in the analysis its status is =0
                if (SvtStrip.get_Edep() == 0) {
                    SvtStrip.setStatus(1);
                }
//                if (Constants.TIMECUTS) {
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
//                SvtStrip.set_ImplantPoint(passVals);


                // create the hit object
                Hit hit = new Hit(DetectorType.BST, BMTType.UNDEFINED, sector, layer, SvtStrip);
                hit.set_Id(id);
                // add this hit
                if(hit.get_Region()!=Constants.getRmReg())      
                    hits.add(hit);
            }
        }
        // fill the list of SVT hits
        this.set_SVTHits(hits);

    }

}
