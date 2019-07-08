package org.jlab.detector.helicity;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import org.jlab.jnp.hipo4.io.HipoReader;
import org.jlab.jnp.hipo4.data.Event;
import org.jlab.jnp.hipo4.data.Bank;
import org.jlab.jnp.hipo4.data.SchemaFactory;

/**
 * An example of reading the helicity flips, analyzing the sequence, and getting
 * the state for any event.
 * 
 * @author baltzell
 */
public class HelicityAnalysis {

    /**
     * This reads tag=1 events for HEL::flip banks, and initializes and returns
     * a {@link HelicitySequenceDelayed} with delay set to zero.  The delay can
     * be changed later before a user tries to access the sequence.
     * 
     * @param filenames list of names of HIPO file to read
     * @return  unanalyzed sequence
     */
    public static HelicitySequenceDelayed readSequence(List<String> filenames) {
       
        HelicitySequenceDelayed seq=new HelicitySequenceDelayed(8);
       
        for (String filename : filenames) {

            HipoReader reader = new HipoReader();
            reader.setTags(1);
            reader.open(filename);
        
            SchemaFactory schema = reader.getSchemaFactory();
        
            while (reader.hasNext()) {
            
                Event event=new Event();
                Bank flipBank=new Bank(schema.getSchema("HEL::flip"));
            
                reader.nextEvent(event);
                event.read(flipBank);
            
                if (flipBank.getRows()<1) continue;
        
                seq.addState(HelicityState.createFromFlipBank(flipBank));
            }

            reader.close();
        }
        
        return seq;
    }
    
    public static void main(String[] args) {
        
        final String dir="/Users/baltzell/data/CLAS12/rg-b/decoded/";
        final String file="clas_006432.evio.00041-00042.hipo";

        List<String> filenames=new ArrayList<>();
        if (args.length>0) filenames.addAll(Arrays.asList(args));
        else               filenames.add(dir+file);
       
        // initialize a sequence from tag=1 events:
        HelicitySequenceDelayed seq = HelicityAnalysis.readSequence(filenames);
        final boolean integrity = seq.analyze();
        if (!integrity) {
            System.err.println("\n\n######### OOPS\n\n");
            // We may want to investigate further, or discard events.
        }

        // print the sequence:
        seq.show();

        // set the appropriate delay for this data:
        seq.setDelay(8);

        // now read the full events, e.g. during a normal physics analysis: 
        int nevents=0;
        int nflips=0;
       
        for (String filename : filenames) {

            HipoReader reader = new HipoReader();
            reader.setTags(0);
            reader.open(filename);
            
            SchemaFactory schema = reader.getSchemaFactory();
        
            while (reader.hasNext()) {

                nevents++;
                
                Bank flipBank=new Bank(schema.getSchema("HEL::flip"));
                Bank rcfgBank=new Bank(schema.getSchema("RUN::config"));
                Bank onliBank=new Bank(schema.getSchema("HEL::online"));
               
                Event event=new Event();
                reader.nextEvent(event);
                event.read(flipBank);
                event.read(rcfgBank);
                event.read(onliBank);
            
                // just to curtail printouts:
                if (flipBank.getRows()>0) nflips++;
                if (nflips<240) continue;
                if (nevents%100!=0) continue;
         
                long timestamp = -1;
                HelicityBit level3 = HelicityBit.UDF;
                HelicityBit predicted = HelicityBit.UDF;
                HelicityBit measured = HelicityBit.UDF;
               
                // Get RUN::config.timestamp for this event:
                if (rcfgBank.getRows()>0) 
                    timestamp = rcfgBank.getLong("timestamp",0);

                // Get HEL::online.rawHelicity, the online, delay-corrected
                // helicity for this event (if available):
                if (onliBank.getRows()>0)
                    level3 = HelicityBit.create(onliBank.getByte("helicity",0));

                // Get the offline, delay-corrected helicity for this event based
                // on the measured sequence.  If this timestamp is outside the
                // measured range, the bit will be null. 
                if (seq.find(timestamp)!=null)
                    measured = seq.find(timestamp);

                // Same as previous, except use the pseudo-random generator's
                // prediction which provides for bits later than the measured range.
                // For example, the last N windows in a given file are measured in
                // the next file (or not at all if it's the last file in a run),
                // so will only be accessible with the generator.  If you try to 
                // use a timestamp before the measured sequence, the generator will
                // return null.
                if (seq.findPrediction(timestamp)!=null)
                    predicted = seq.findPrediction(timestamp);

                System.out.println(String.format("%d %5d L3/Predict/Measured = %6s%6s%6s",
                        timestamp,nflips,level3,predicted,measured));
            }

            reader.close();

        }
    }
}
