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
 * The 2 lines marked with "!!!" are the essential delayed-helicity correction
 * and requires input from RUN::config.run and RUN::config.timestamp.
 * 
 * @author baltzell
 */
public class HelicityAnalysisSimple {

    public static void main(String[] args) {
       
        //final String dir="/Users/baltzell/data/CLAS12/rg-a/decoded/6b.2.0/";
        //final String file="clas_005038.evio.00000-00004.hipo";
        final String dir="/Users/baltzell/data/CLAS12/rg-b/decoded/";
        final String file="clas_006432.evio.00041-00042.hipo";

        List<String> filenames=new ArrayList<>();
        if (args.length>0) filenames.addAll(Arrays.asList(args));
        else               filenames.add(dir+file);

        // !!! 111
        // initialize a sequence, with delay=8, from tag=1 events:
        HelicitySequenceManager seq = new HelicitySequenceManager(8,filenames);

        // or initialize with a reader object:
        //HelicitySequenceManager seq = new HelicitySequenceManager(8,HipoReader);

        // increase info printouts:
        //seq.setVerbosity(2);
        
        // print the sequence if any problems:
        //if (!seq.analyze()) seq.show();

        // now read the full events, e.g. during a normal physics analysis: 
        int nevents=0;
        int nflips=0;
        int nbad=0;
        
        for (String filename : filenames) {

            HipoReader reader = new HipoReader();
            reader.setTags(0);
            reader.open(filename);
            
            SchemaFactory schema = reader.getSchemaFactory();
        
            while (reader.hasNext()) {

                nevents++;
               
                Bank flipBank=new Bank(schema.getSchema("HEL::flip"));
                Bank onliBank=new Bank(schema.getSchema("HEL::online"));
               
                Event event=new Event();
                reader.nextEvent(event);
                event.read(flipBank);
                event.read(onliBank);
           
                // Get HEL::online.rawHelicity, the online, delay-corrected
                // helicity for this event (if available):
                //HelicityBit level3 = HelicityBit.UDF;
                //if (onliBank.getRows()>0)
                //    level3 = HelicityBit.create(onliBank.getByte("helicity",0));

                // !!! 222
                // Get the offline, delay-corrected helicity for this event
                // based on the measured sequence.  If the timestamp is outside
                // the measured range, or the measured sequence is corrupted between
                // the timestamp and the delayed timestamp, this will return null.
                final HelicityBit measured = seq.search(event);

                // Same as previous, except assumes the pseudo-random generator's
                // prediction, which allows to access states later than the measured
                // range and cross intermediate sequence corruption.  However, it
                // requires a 4x longer consecutive valid sequence for initialization
                // than the non-generator methods.
                //final HelicityBit lookedup = seq.searchGenerated(event);
                
                // Same as previous, except relies on TI clock synced with helicity clock
                // instead of counting states:
                //final HelicityBit predicted = seq.predictGenerated(event);
               
                if (measured==HelicityBit.UDF) {
                    nbad++;
                }
                else {
                    // proceed with physics analysis
                }
              
                //if (flipBank.getRows()>0) nflips++;
                //if (nflips>240 && nevents%100!=0) {
                //    System.out.println(String.format("%d %5d L3/Measured/LookedG/PredictG = %6s%6s%6s%6s",
                //            timestamp,nflips,level3,measured,lookedup,predicted));
                //}
            }

            System.out.println(String.format("HelicityAnalysisSimple:  EVENTS BAD/TOTAL/FRACTION = %d/%d/%.2f%%",
                    nbad,nevents,100*((float)nbad)/nevents));

            reader.close();

        }
    }
}
