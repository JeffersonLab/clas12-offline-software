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

    private int cluster = 0;
    private int xtalk   = 0;
    
    //public RingCherenkovResponse(){
    //    super();
    //}
    
    public RingCherenkovResponse(int sector, int layer, int component){
        this.getDescriptor().setSectorLayerComponent(sector, layer, component);
    }

    public RingCherenkovResponse(RingCherenkovResponse r){
        this.getDescriptor().setSectorLayerComponent(r.getDescriptor().getSector(), r.getDescriptor().getLayer(), r.getDescriptor().getComponent());
    }

    public int get_cluster(){ return this.cluster;}
    public int get_xtalk(){ return this.xtalk;}

    public void   set_cluster(int cluster){ this.cluster = cluster;}
    public void   set_xtalk(int xtalk){ this.xtalk = xtalk;}
    
    // ----------------
    public static ArrayList<DetectorResponse>  readHipoEvent(DataEvent event, 
            String bankName, DetectorType type, int signal_type){        
    // ----------------

        int debugMode = 0;
        ArrayList<DetectorResponse> responseList = new ArrayList<DetectorResponse>();

        if(debugMode==1){
            if(signal_type==0)System.out.format(" reading bank %s for single hits \n", bankName);
            if(signal_type==1)System.out.format(" reading bank %s for clusters \n", bankName);
        } 

        if(event.hasBank(bankName)==true){
            DataBank bank = event.getBank(bankName);
            int nrows = bank.rows();
            for(int row = 0; row < nrows; row++){

                int id        = 0;
                int anode     = 0;
                int good      = 0;
                int status    = 0;
                double energy = 0.0;
                double time   = 0.0;
                double x      = 0.0;
                double y      = 0.0;
                double z      = 0.0;

                if(bankName.equals("RICH::Cluster")){
                    good=1;
                    id          = bank.getShort("id", row); 
                    energy      = bank.getFloat("charge", row);
                    time        = bank.getFloat("time", row);
                    x           = bank.getFloat("x", row);
                    y           = bank.getFloat("y", row);
                    z           = bank.getFloat("z", row);
                    if(debugMode>=1)System.out.format(" ---> read cluster %4d %4d  %8.2f %8.2f ",row,id,energy,time);
                }

                if(bankName.equals("RICH::Hit")){
                    id          = bank.getShort("id", row); 
                    int cluster = bank.getShort("cluster", row); 
                    int xtalk   = bank.getShort("xtalk", row);
                    status      = bank.getShort("status", row);
                    anode       = bank.getShort("anode", row);
                    if((status==0 || status==5) && cluster==0 && xtalk==0)good=1;
                    energy      = (double) bank.getShort("duration", row);
                    time        = bank.getFloat("time", row);
                    x           = bank.getFloat("x", row);
                    y           = bank.getFloat("y", row);
                    z           = bank.getFloat("z", row);
                    if(debugMode>=1)System.out.format(" ---> read hit %4d %4d (%3d %3d %5d --> %3d) %8.2f %8.2f ",
                        row,id,status,cluster,xtalk,good,energy,time);
                }
                if(bankName.equals("RICH::Signal")){
                    id         = bank.getShort("id", row); 
                    int hindex = bank.getShort("hindex", row); 
                    int size   = bank.getShort("size", row); 
                    status     = bank.getShort("status", row);
                    if(signal_type == 0 && size==1 ) good=1;
                    if(signal_type == 1 && size>1 ) good=1;
                    anode      = bank.getShort("anode", row);
                    energy     = bank.getFloat("charge", row);
                    time       = bank.getFloat("time", row);
                    x = bank.getFloat("x", row);
                    y = bank.getFloat("y", row);
                    z = bank.getFloat("z", row);
                    if(debugMode>=1)System.out.format(" ---> read signal %4d %4d (%3d %5d --> %3d) %8.2f %8.2f ", row,id,hindex,size,good,energy,time);
                }

                if(good==1){

                    int sector = bank.getShort("sector", row);
                    int pmt = bank.getShort("pmt", row);
                    RingCherenkovResponse  response = new RingCherenkovResponse(sector,pmt,anode);
                    response.setHitIndex(row);
                    response.getDescriptor().setType(type);
                    response.setPosition(x, y, z);
                    response.setTime(time);
                    response.setEnergy(energy);
                    response.setStatus(status);

                    responseList.add((DetectorResponse) response);
                    if(debugMode>=1)System.out.format(" save with id %3d \n",response.getHitIndex());
                }else{
                    if(debugMode>=1)System.out.format(" ---> rejected \n");
                }
            }
            return responseList;
        }
        return null;
    }
}
