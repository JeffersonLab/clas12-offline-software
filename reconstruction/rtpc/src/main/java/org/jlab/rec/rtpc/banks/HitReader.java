package org.jlab.rec.rtpc.banks;

import java.util.ArrayList;
import java.util.List;

import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.rtpc.hit.Hit;
/**
 * @author payette
 *
 */
public class HitReader {

	private List<Hit> _Hits;
	
	/**
	 *
	 * @return a list of RTPC hits
	 */
	public List<Hit> get_RTPCHits() {
		return _Hits;
	}

	/**
	 *  sets the list of RTPC hits
	 * @param RTPCHits list of RTPC hits
	 */
	public void set_RTPCHits(List<Hit> RTPCHits) {
		this._Hits = RTPCHits;
	}

	

	
	/**
	 * reads the hits using clas-io methods to get the EvioBank for the RTPC and fill the values to instanciate the RTPChit and MChit classes.
	 * This methods fills the RTPChit and MChit list of hits.  If the data is not MC, the MChit list remains empty
	 * @param event DataEvent
	 */
	public void fetch_RTPCHits(DataEvent event) {

 

            List<Hit> hits = new ArrayList<>();

            DataBank bankDGTZ = null;
            if(event.hasBank("RTPC::adc")==true)
                bankDGTZ=event.getBank("RTPC::adc");
            DataBank bankTrue = null;
            if(event.hasBank("RTPC::pos")==true)
                bankTrue=event.getBank("RTPC::pos");
            if(bankDGTZ==null || bankTrue==null)
                return ;
            
            int rows = bankDGTZ.rows();
            if(bankTrue.rows()!=rows)
                return;

            int[] hitnb 	= new int[rows];
            int[] cellID 	= new int[rows];
            double[] Time	= new double[rows];
            int[] step      = new int[rows];
            double[] posX 	= new double[rows];
            double[] posY 	= new double[rows];
            double[] posZ 	= new double[rows];
            double[] Edep = new double[rows];
            double[] TShift = new double[rows];

            for(int i = 0; i<rows; i++){				
                hitnb[i] = i+1;
                cellID[i] = bankDGTZ.getInt("component", i);                    
                Time[i]	= (double) bankTrue.getFloat("time", i);
                step[i]	= bankTrue.getInt("step", i);
                Edep[i] = (double) bankTrue.getFloat("energy", i);
                TShift[i] = (double) bankDGTZ.getFloat("timeshift", i);						    
                posX[i] = (double) bankTrue.getFloat("posx", i);
                posY[i] = (double) bankTrue.getFloat("posy", i);
                posZ[i] = (double) bankTrue.getFloat("posz", i);				
                if(Time[i] <= 0 || Time[i] > 10000) 
                {
                    Time[i] = 0;
                    Edep[i] = 0;
                    posX[i] = 0;
                    posY[i] = 0;
                    posZ[i] = 0;
                }				
                Hit hit = new Hit(1, cellID[i], 1, Time[i]);
                hit.set_EdepTrue(Edep[i]);
                hit.set_PosXTrue(posX[i]);
                hit.set_PosYTrue(posY[i]);
                hit.set_PosZTrue(posZ[i]);
                hit.set_Time(Time[i]);
                hit.set_TShift(TShift[i]);

                hits.add(hit); 
            }
            
            this.set_RTPCHits(hits);

	}
}
