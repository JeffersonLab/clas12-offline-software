package org.jlab.clas.detector;

import java.util.ArrayList;
import java.util.List;
import org.jlab.detector.base.DetectorType;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;


/**
 *
 * @author mcontalb
 */
public class RingCherenkovResponse extends DetectorResponse {

    private int cluster=0;
    private int xtalk=0;
    
    public RingCherenkovResponse(){
        super();
    }
    
    public RingCherenkovResponse(RingCherenkovResponse r) {
        super();
        this.copy(r);
    }

    public RingCherenkovResponse(int sector, int layer, int pmt){
        this.getDescriptor().setSectorLayerComponent(sector, layer, pmt);
    }

    public void copy(RingCherenkovResponse r) {
        super.copy(r);
        cluster = r.cluster;
        xtalk = r.xtalk;
    }

    public int getCluster(){ return this.cluster;}
    public int getXtalk(){ return this.xtalk;}

    public void   setCluster(int clu){ this.cluster = clu;}
    public void   setXtalk(int xt){ this.xtalk = xt;}
    
    // ----------------
    public static ArrayList<DetectorResponse>  readHipoEvent(DataEvent event, 
            String bankName, DetectorType type){        
    // ----------------

        int debugMode = 0;
        ArrayList<DetectorResponse> responseList = new ArrayList<DetectorResponse>();

        if(debugMode==1)System.out.print(" reading bank "+bankName);
        if(event.hasBank(bankName)==true){
            DataBank bank = event.getBank(bankName);
            int nrows = bank.rows();
            for(int row = 0; row < nrows; row++){

                int good = 0;
                double energy = 0.0;
                double time = 0.0;
                double x = 0.0;
                double y = 0.0;
                double z = 0.0;
                if(bankName.equals("RICH::clusters")){
                    good=1;
                    if(debugMode>=1)System.out.print(" ---> use clusters and charge \n");
                    energy = bank.getFloat("charge", row);
                    time = bank.getFloat("time", row);
                    x = bank.getFloat("x", row);
                    y = bank.getFloat("y", row);
                    z = bank.getFloat("z", row);
                }

                if(bankName.equals("RICH::hits")){
                    int id   = bank.getShort("id", row); 
                    int cluster = bank.getShort("cluster", row); 
                    int xtalk = bank.getShort("xtalk", row);
                    if(cluster==0 && xtalk==0)good=1;
                    energy = (double) bank.getShort("duration", row);
                    time = (double) bank.getFloat("time", row);
                    x = bank.getFloat("x", row);
                    y = bank.getFloat("y", row);
                    z = bank.getFloat("z", row);
                    if(debugMode>=1)System.out.format(" ---> use hits and duration %4d %4d %4d %4d %4d %8.2f %8.2f \n",row,id,cluster,xtalk,good,energy,time);
                }

                if(good==1){
                    int sector = bank.getShort("sector", row);
                    int layer= 1;  // only one layer used in matching
                    int pmt = bank.getShort("pmt", row);
                    RingCherenkovResponse  response = new RingCherenkovResponse(sector,layer,pmt);
                    //response.setHitIndex((int) bank.getShort("id", row));
                    response.setHitIndex(row);
                    response.getDescriptor().setType(type);
                    response.setPosition(x, y, z);
                    response.setTime(time);
                    response.setEnergy(energy);
                    response.setStatus(1);

                    responseList.add((DetectorResponse) response);
                }
            }
            return responseList;
        }
        return null;
    }
}
