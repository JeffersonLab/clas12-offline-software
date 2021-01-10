/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rec.fmt.banks;
import java.util.ArrayList;
import java.util.List;
import org.jlab.rec.fmt.hit.Hit;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;

/**
 *
 * @author ziegler
 */
public class HitReader {

    public HitReader() {

    }

    // the list of FMT hits
    private List<Hit> _FMTHits;

    /**
     *
     * @return a list of FMT hits
     */
    public List<Hit> get_FMTHits() {
        return _FMTHits;
    }

    /**
     * sets the list of FMT hits
     *
     * @param _FMTHits list of FMT hits
     */
    public void set_FMTHits(List<Hit> _FMTHits) {
        this._FMTHits = _FMTHits;
    }    
    
    public void fetch_FMTHits(DataEvent event) {
        
        if (event.hasBank("FMT::adc") == false) {
            //System.err.println("there is no BST bank ");
            _FMTHits = new ArrayList<Hit>();

            return;
        }

        List<Hit> hits = new ArrayList<Hit>();

        DataBank bankDGTZ = event.getBank("FMT::adc");

        int rows = bankDGTZ.rows();;

        int[] id = new int[rows];
        int[] sector = new int[rows];
        int[] layer = new int[rows];
        int[] strip = new int[rows];
        int[] ADC = new int[rows];

        if (event.hasBank("FMT::adc") == true) { 
            
            //bankDGTZ.show();
            for (int i = 0; i < rows; i++) {

                id[i] = i + 1;
                sector[i] = bankDGTZ.getByte("sector", i);
                layer[i] = bankDGTZ.getByte("layer", i);
                strip[i] = bankDGTZ.getShort("component", i);
                ADC[i] = bankDGTZ.getInt("ADC", i);

                if(strip[i]==-1 || ADC[i]==0)
                    continue;

                // create the hit object
                Hit hit = new Hit(sector[i], layer[i], strip[i], (double) ADC[i]);
                // if the hit is useable in the analysis its status is 1
               
                hit.set_Id(i+1);

                // add this hit
                hits.add(hit);
            }
        }
        // fill the list of SVT hits
        this.set_FMTHits(hits);

    }

}
