package org.jlab.analysis.eventmerger;
import org.jlab.detector.scalers.DaqScalersSequence;
import org.jlab.jnp.hipo4.data.*;
import org.jlab.jnp.hipo4.io.HipoReader;
import org.jlab.jnp.utils.data.*;

/**
 * Hipo Reduce Worker: filter event based on trigger bit
 * 
 * Inputs: selected trigger bit (0-63)
 * Returns "true" is selected bit is set in the trigger bit word and no other bits are set
 * @author devita
 */
public class FilterTrigger implements Worker {

    Bank triggerBank = null;
    DaqScalersSequence chargeSeq = null;
    int bit = -1;
    
    public FilterTrigger(int bit){
        this.bit=bit;
        System.out.println("\nInitializing trigger reduction: bit set to " + this.bit + "\n");
    }

    /**
     * Initialize bank schema
     * 
     * @param reader
     */
    @Override
    public void init(HipoReader reader) {
        triggerBank = new Bank(reader.getSchemaFactory().getSchema("RUN::config"));
    }

    /**
     * Event filter: select events according to trigger bit
     * 
     * @param event
     * @return
     */
    @Override
    public boolean processEvent(Event event) {
        event.read(triggerBank);
        if(triggerBank.getRows()>0){
            long triggerBit = triggerBank.getLong("trigger",0);
            long timeStamp  = triggerBank.getLong("timestamp",0);
            // Value will be >0 if selected bit is 1 in triggerBit
            int  value    = DataByteUtils.readLong(triggerBit, bit, bit);
            // Check that no other bit is set
            long thisBit  = value*((long) Math.pow(2, bit));
            // If returned true, the event will be write to the output
            if(value>0 && thisBit==triggerBit) return true;
            }
        return false;
    }
    
    // This function has to be implemented, but not used if
    // HipoStream is not trying to classify the events.
    @Override
    public long clasifyEvent(Event event) { return 0L; }

}
