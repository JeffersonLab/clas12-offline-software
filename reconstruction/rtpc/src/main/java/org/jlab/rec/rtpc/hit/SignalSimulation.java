// Author: Gabriel Charles
// Language Conversion: David Payette

// This code creates a signal shape for each hit on a single pad, and then sums all signals on that pad and stores them
// into a map called ADCMap which uses the pad (cellID) as the key and returns an array which is organized by time bins.
// All variables used in this code are stored and accessed in HitParameters.java which is instantiated in RTPCEngine.java.
// This code is accessed once for each event, and then the signal shaping map is looped through and filled for all hits in that event.
// The map is refreshed for each event. 

package org.jlab.rec.rtpc.hit;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;



public class SignalSimulation {
    	
    private int SignalStepSize; // step size of the signal before integration (arbitrary value)
    private int BinSize;// electronics integrates the signal over 40 ns
    private int NBinKept;// only 1 bin over 3 is kept by the daq
    private int TrigWindSize;// Trigger window should be 10 micro
    private int NTrigSampl;// number of time samples
    private List<Integer> PadList = new ArrayList<>();
    private int CellID = 0; 
    private double Time;
    private double Edep;   
    private ADCMap ADCMap = new ADCMap();
    

    public SignalSimulation(List<Hit> rawHits, HitParameters params, boolean cosmic){

        SignalStepSize = params.get_SignalStepSize(); // step size of the signal before integration (arbitrary value)
        BinSize = params.get_BinSize(); // electronics integrates the signal over 40 ns
        NBinKept = params.get_NBinKept(); // only 1 bin over 3 is kept by the daq
        TrigWindSize = params.get_TrigWindSize(); // Trigger window should be 10 micro
        NTrigSampl = TrigWindSize/BinSize; // number of time samples

        /*try {

            File out = new File("/Users/davidpayette/Desktop/SignalStudies/");
            if(!out.exists())
            {out.mkdirs();}
            FileWriter write = new FileWriter("/Users/davidpayette/Desktop/SignalStudies/sigafter.txt",true);   
        */
        boolean pass_thresh = false;
        List<Integer> sig_study_list = new ArrayList<>();
        for(Hit hit : rawHits){

            CellID = hit.get_cellID(); //Pad ID number of the hit
            Time = hit.get_Time(); //Time of the hit
            Edep = hit.get_EdepTrue(); //Simulated Energy of the hit
            //System.out.println("Edep " + Edep);
            /*if(Edep > 1e-4){
                //System.out.println("CellID " + CellID);
                if(!sig_study_list.contains(CellID)){
                    sig_study_list.add(CellID);
                    write.write(CellID + "\r\n");
                }
            }*/
            if(!cosmic){
                ADCMap.simulateSignal(CellID,Time,Edep);//Creates a signal based on the Time and the Energy
             
                ADCMap.integrateSignal(CellID); //Integrates the signal into 120 ns bins based on DREAM elec specifications
            }else{
                ADCMap.addSignal(CellID, (int)Time, Edep);
                
            }
            if(!PadList.contains(CellID)){
                PadList.add(CellID);                
            } //Maintains a list of all unique Pad IDs
            
        }
            /*write.write("End\r\n");
            write.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }*/
        params.set_PadList(PadList);
        params.set_ADCMap(ADCMap);
    }
}
