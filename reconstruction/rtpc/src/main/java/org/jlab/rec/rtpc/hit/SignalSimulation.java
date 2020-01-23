// Author: Gabriel Charles
// Language Conversion: David Payette

// This code creates a signal shape for each hit on a single pad, and then sums all signals on that pad and stores them
// into a map called ADCMap which uses the pad (cellID) as the key and returns an array which is organized by time bins.
// All variables used in this code are stored and accessed in HitParameters.java which is instantiated in RTPCEngine.java.
// This code is accessed once for each event, and then the signal shaping map is looped through and filled for all hits in that event.
// The map is refreshed for each event. 

package org.jlab.rec.rtpc.hit;
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
    

    public SignalSimulation(List<Hit> rawHits, HitParameters params, boolean simulation){

        SignalStepSize = params.get_SignalStepSize(); // step size of the signal before integration (arbitrary value)
        BinSize = params.get_BinSize(); // electronics integrates the signal over 40 ns
        NBinKept = params.get_NBinKept(); // only 1 bin over 3 is kept by the daq
        TrigWindSize = params.get_TrigWindSize(); // Trigger window should be 10 micro
        NTrigSampl = TrigWindSize/BinSize; // number of time samples

        for(Hit hit : rawHits){

            CellID = hit.get_cellID(); //Pad ID number of the hit
            Time = hit.get_Time(); //Time of the hit
            Edep = hit.get_EdepTrue(); //Simulated Energy of the hit

            if(simulation){
                ADCMap.simulateSignal(CellID,Time,Edep);//Creates a signal based on the Time and the Energy             
                ADCMap.integrateSignal(CellID); //Integrates the signal into 120 ns bins based on DREAM elec specifications
            }else{
                ADCMap.addSignal(CellID, (int)Time, Edep);
                
            }
            if(!PadList.contains(CellID)){
                PadList.add(CellID);                
            } //Maintains a list of all unique Pad IDs
            
        }
        
        params.set_PadList(PadList);
        params.set_ADCMap(ADCMap);
    }
}
