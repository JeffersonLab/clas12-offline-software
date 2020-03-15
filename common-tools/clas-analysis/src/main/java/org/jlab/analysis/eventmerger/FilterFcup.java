package org.jlab.analysis.eventmerger;
import org.jlab.detector.decode.DaqScalers;
import org.jlab.detector.decode.DaqScalersSequence;
import org.jlab.jnp.hipo4.data.*;
import org.jlab.jnp.hipo4.io.HipoReader;
import org.jlab.jnp.utils.data.*;



public class FilterFcup implements Worker {

    Bank runConfigBank = null;
    DaqScalersSequence chargeSeq = null;
    double charge  = -1;
    double current = -1;
    double tsResolution = 4E-9;
    
    public FilterFcup(double current){
        this.current=current;
        System.out.println("\nInitializing Faraday Cup reduction: threshold current set to " + this.current);
    }

    @Override
    public void init(HipoReader reader) {
       runConfigBank = new Bank(reader.getSchemaFactory().getSchema("RUN::config"));
    }

    public void setScalerSequence(DaqScalersSequence sequence) {
        this.chargeSeq=sequence;
    }
    
    @Override
    public boolean processEvent(Event event) {
        event.read(runConfigBank);
        
        if(runConfigBank.getRows()>0){
            long timeStamp  = runConfigBank.getLong("timestamp",0);
            
            DaqScalers dsCurrent  = chargeSeq.get(timeStamp);
            DaqScalers dsPrevious = chargeSeq.getPrevious(timeStamp);
            
            double value=-1;
            if(dsCurrent!=null && dsPrevious!=null) {
                value = (dsCurrent.getBeamChargeGated()-dsPrevious.getBeamChargeGated())/
                        (dsCurrent.getTimestamp()-dsPrevious.getTimestamp())/tsResolution;                
//                System.out.println(value);           
            }
            if(value>current) return true;
            }
        return false;
    }
    
    // This function has to be implemented, but not used if
    // HipoStream is not trying to classify the events.
    @Override
    public long clasifyEvent(Event event) { return 0L; }

}
