package org.jlab.rec.cvt.banks;

import java.util.ArrayList;
import java.util.List; 

import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.cvt.Constants;
import org.jlab.rec.cvt.bmt.Geometry;
import org.jlab.rec.cvt.hit.ADCConvertor;
import org.jlab.rec.cvt.hit.Hit;
import org.jlab.rec.cvt.hit.Strip;

/**
 * A class to fill in lists of hits  corresponding to reconstructed hits characterized by the strip, its location in the detector 
 * (layer, sector), its reconstructed time.  
 * @author ziegler
 *
 */
public class HitReader {
	
	public HitReader(){
		
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
	 *  sets the list of BMT hits
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
	 *  sets the list of SVT hits
	 * @param _SVTHits list of SVT hits
	 */
	public void set_SVTHits(List<Hit> _SVTHits) {
		this._SVTHits = _SVTHits;
	}

	/**
	 * Gets the BMT hits from the BMT dgtz bank
	 * @param event the data event
	 * @param adcConv converter from adc to values used in the analysis (i.e. Edep for gemc, adc for cosmics)
	 * @param geo the BMT geometry
	 */
	public void fetch_BMTHits(DataEvent event, ADCConvertor adcConv, Geometry geo) {
		// return if there is no BMT bank
		if(event.hasBank("BMT::dgtz")==false ) {
			//System.err.println("there is no BMT bank ");
			_BMTHits= new ArrayList<Hit>();
			
			return;
		}
		// instanciates the list of hits
		List<Hit> hits = new ArrayList<Hit>();
		// gets the BMT dgtz bank
		DataBank bankDGTZ = event.getBank("BMT::adc");
        // fills the arrays corresponding to the hit variables
		int rows = bankDGTZ.rows();
		int[] id = new int[rows];
        int[] sector = new int[rows];
		int[] layer = new int[rows];
		int[] strip = new int[rows];
		int[] ADC = new int[rows];
		
		// exit if the array is empty
		int size = rows;
		if(size==0)
			return;
		
		
		for(int i = 0; i<size; i++){
			strip[i] = bankDGTZ.getInt("strip",i);
			if(strip[i]<1)
				continue; // gemc assigns strip value -1 for inefficiencies, we only consider strips with values between 1 to the maximum strip number for a given detector
			double ADCtoEdep = ADC[i];
			
			// if it is not simulation then use the adc value
			if(Constants.isSimulation==false)
				ADCtoEdep = adcConv.BMTADCtoDAQ(ADC[i]);
			if(Constants.isSimulation==true)
				ADCtoEdep = ADC[i];
			
			// create the strip object for the BMT
			Strip BmtStrip = new Strip(strip[i], ADCtoEdep);
			// calculate the strip parameters for the BMT hit
			BmtStrip.calc_BMTStripParams(geo, sector[i], layer[i]); // for Z detectors the Lorentz angle shifts the strip measurement; calc_Strip corrects for this effect
			// create the hit object for detector type BMT
			String detectortype = new String();
			if(layer[i]%2==1) // Z detector
				detectortype = "Z";
			if(layer[i]%2==0) // C detector
				detectortype = "C";
			Hit hit = new Hit("BMT", detectortype, sector[i], layer[i], BmtStrip);
			// a place holder to set the status of the hit, for simulated data if the strip number is in range and the Edep is above threshold the hit has status 1, useable
			hit.set_Status(1);
			if(BmtStrip.get_Edep()==0)
				hit.set_Status(-1);
			hit.set_Id(id[i]);
    	    // add this hit
            hits.add(hit); 
	   
		}
		// fills the list of BMT hits
	    this.set_BMTHits(hits);

	}
	
	/**
	 * Gets the SVT hits from the BMT dgtz bank
	 * @param event the data event
	 * @param adcConv converter from adc to daq values
	 * @param geo the SVT geometry
	 */
	public void fetch_SVTHits(DataEvent event, ADCConvertor adcConv, int omitLayer, int omitHemisphere) {
		
		if(event.hasBank("SVT::adc")==false) {
			//System.err.println("there is no BST bank ");
			_SVTHits= new ArrayList<Hit>();
			
			return;
		}

		List<Hit> hits = new ArrayList<Hit>();
		
		DataBank bankDGTZ = event.getBank("SVT::adc");
		
		int rows = bankDGTZ.rows();;
		
		int[] id = new int[rows];
        int[] sector = new int[rows];
		int[] layer = new int[rows];
		int[] strip = new int[rows];
		int[] ADC = new int[rows];

		
		if(event.hasBank("SVT::adc")==true) {
			//bankDGTZ.show();
			for(int i = 0; i<rows; i++){
				
				id[i] = i+1;
		        sector[i] = bankDGTZ.getInt("sector", i);
				layer[i] = bankDGTZ.getInt("layer", i);
				strip[i] = bankDGTZ.getInt("component", i);
				ADC[i] = bankDGTZ.getInt("ADC", i);
				
				
				double angle = 2.*Math.PI*((double)(sector[i]-1)/(double)org.jlab.rec.cvt.svt.Constants.NSECT[layer[i]-1]) + org.jlab.rec.cvt.svt.Constants.PHI0[layer[i]-1];
			    int  hemisphere = (int) Math.signum(Math.sin(angle) );
			    if(sector[i]==7 && layer[i]>6)
			    	hemisphere=1;
			    if(sector[i]==19 && layer[i]>6)
			    	hemisphere=-1;
			    if(omitHemisphere==-2) {
			    	if(layer[i]==omitLayer)
			    		continue;
			    } else {
			    	if(hemisphere==omitHemisphere && layer[i]==omitLayer) 
			    		continue;
			    	
			    }
			    // if the strip is out of range skip
				if(strip[i]<1)
					continue;
				// create the strip object with the adc value converted to daq value used for cluster-centroid estimate
				Strip SvtStrip = new Strip(strip[i], adcConv.SVTADCtoDAQ(ADC[i]));
				
				// create the hit object
				Hit hit = new Hit("SVT", "", sector[i], layer[i], SvtStrip);
				// if the hit is useable in the analysis its status is 1
				hit.set_Status(1);
				if(SvtStrip.get_Edep()==0)
					hit.set_Status(-1);
				hit.set_Id(id[i]);
	    	    // add this hit
	            hits.add(hit); 
			}
		}
		// fill the list of SVT hits
		this.set_SVTHits(hits);
	
	}

	 
}	
	
	

