package org.jlab.rec.cvt.banks;

import java.util.ArrayList;
import java.util.List;
import org.jlab.clas.swimtools.Swim;
import org.jlab.detector.base.DetectorType;
import org.jlab.geom.prim.Line3D;

import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.cvt.bmt.BMTGeometry;
import org.jlab.rec.cvt.bmt.BMTType;
import org.jlab.rec.cvt.hit.ADCConvertor;
import org.jlab.rec.cvt.hit.Hit;
import org.jlab.rec.cvt.hit.Strip;
import org.jlab.rec.cvt.svt.SVTGeometry;

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
    public void fetch_BMTHits(DataEvent event, ADCConvertor adcConv, BMTGeometry geo, Swim swim) {

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

            for (int i = 0; i < rows; i++) {

                //if (bankDGTZ.getInt("ADC", i) < 1) {
                    //continue; // gemc assigns strip value -1 for inefficiencies, we only consider strips with values between 1 to the maximum strip number for a given detector
                //}
                double ADCtoEdep = bankDGTZ.getInt("ADC", i);
               
                //fix for now... no adc in GEMC
                if (ADCtoEdep < 1) {
                    continue;
                }
                // create the strip object for the BMT
                //Strip BmtStrip = new Strip((int) bankDGTZ.getShort("component", i), ADCtoEdep);
                Strip BmtStrip = new Strip((int) bankDGTZ.getShort("component", i), ADCtoEdep, (double) bankDGTZ.getFloat("time", i));
                // calculate the strip parameters for the BMT hit
                BmtStrip.calc_BMTStripParams(geo,(int) bankDGTZ.getByte("sector", i),(int) bankDGTZ.getByte("layer", i), swim); // for Z detectors the Lorentz angle shifts the strip measurement; calc_Strip corrects for this effect
                // create the hit object for detector type BMT
                
                Hit hit = new Hit(DetectorType.BMT, BMTGeometry.getDetectorType(bankDGTZ.getByte("layer", i)),(int) bankDGTZ.getByte("sector", i),(int) bankDGTZ.getByte("layer", i), BmtStrip);
                // a place holder to set the status of the hit, for simulated data if the strip number is in range and the Edep is above threshold the hit has status 1, useable
                hit.set_Status(1);
                //if(BmtStrip.get_Edep()==0)
                //	hit.set_Status(-1);
                hit.set_Id(i+1);
                // add this hit
                if(hit.get_Layer()+3!=org.jlab.rec.cvt.Constants.getRmReg())
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
    public void fetch_SVTHits(DataEvent event, ADCConvertor adcConv, int omitLayer, int omitHemisphere, SVTGeometry geo) {

        if (event.hasBank("BST::adc") == false) {
            //System.err.println("there is no BST bank ");
            _SVTHits = new ArrayList<Hit>();

            return;
        }

        List<Hit> hits = new ArrayList<Hit>();

        DataBank bankDGTZ = event.getBank("BST::adc");

        int rows = bankDGTZ.rows();;

        int[] id = new int[rows];
        int[] sector = new int[rows];
        int[] layer = new int[rows];
        int[] strip = new int[rows];
        int[] ADC = new int[rows];
        float[] time = new float[rows];
        
        if (event.hasBank("BST::adc") == true) {
            //bankDGTZ.show();
            for (int i = 0; i < rows; i++) {

                if (bankDGTZ.getInt("ADC", i) < 0) {
                    continue; // ignore hits TDC hits with ADC==-1 
                }
                
                id[i] = i + 1;
                sector[i] = bankDGTZ.getByte("sector", i);
                layer[i] = bankDGTZ.getByte("layer", i);
                
                strip[i] = bankDGTZ.getShort("component", i);
                ADC[i] = bankDGTZ.getInt("ADC", i);
                
                double angle = 2. * Math.PI * ((double) (sector[i] - 1) / (double) org.jlab.rec.cvt.svt.Constants.NSECT[layer[i] - 1]) + org.jlab.rec.cvt.svt.Constants.PHI0[layer[i] - 1];
                int hemisphere = (int) Math.signum(Math.sin(angle));
                if (sector[i] == 7 && layer[i] > 6) {
                    hemisphere = 1;
                }
                if (sector[i] == 19 && layer[i] > 6) {
                    hemisphere = -1;
                }
                if (omitHemisphere == -2) {
                    if (layer[i] == omitLayer) {
                        continue;
                    }
                } else {
                    if (hemisphere == omitHemisphere && layer[i] == omitLayer) {
                        continue;
                    }

                }
                // if the strip is out of range skip
                if (strip[i] < 1) {
                    continue;
                }
                if (layer[i] > 6) {
                    continue;
                }
                
                //if(adcConv.SVTADCtoDAQ(ADC[i], event)<50)
                //    continue;
                // create the strip object with the adc value converted to daq value used for cluster-centroid estimate
                Strip SvtStrip = new Strip(strip[i], adcConv.SVTADCtoDAQ(ADC[i], event), (double) time[i]); 
                // get the strip endPoints
                 double[][] X = geo.getStripEndPoints(SvtStrip.get_Strip(), (layer[i] - 1) % 2);
                Point3D EP1 = geo.transformToFrame(sector[i], layer[i], X[0][0], 0, X[0][1], "lab", "");
                Point3D EP2 = geo.transformToFrame(sector[i], layer[i], X[1][0], 0, X[1][1], "lab", "");
                SvtStrip.set_Line(new Line3D(EP1, EP2));
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
                Hit hit = new Hit(DetectorType.BST, BMTType.UNDEFINED, sector[i], layer[i], SvtStrip);
                // if the hit is useable in the analysis its status is 1
                hit.set_Status(1);
                if (SvtStrip.get_Edep() == 0) {
                    hit.set_Status(-1);
                }
                //System.out.println("SVT e "+SvtStrip.get_Edep());
                
                hit.set_Id(id[i]);
                // add this hit
                if(SvtStrip.get_Edep()>0 && hit.get_Region()!=org.jlab.rec.cvt.Constants.getRmReg())      
                    hits.add(hit);
            }
        }
        // fill the list of SVT hits
        this.set_SVTHits(hits);

    }

}
