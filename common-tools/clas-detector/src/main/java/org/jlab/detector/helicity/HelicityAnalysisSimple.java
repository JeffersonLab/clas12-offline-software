package org.jlab.detector.helicity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.jlab.jnp.hipo4.data.Bank;
import org.jlab.jnp.hipo4.data.Event;
import org.jlab.jnp.hipo4.data.SchemaFactory;
import org.jlab.jnp.hipo4.io.HipoReader;

/**
 *
 * @author baltzell
 */
public class HelicityAnalysisSimple {

    /**
     * Example of accessing delay-corrected helicity.
     *
     * The 2 lines marked with "!!!" are specific to delay-corrected helicity.
     * 
     * @param args a list of input HIPO4 filenames 
     */
    public static void main(String[] args) {

        final String dir="/Users/baltzell/data/CLAS12/rg-a/decoded/6b.2.0/";
        final String file="clas_005038.evio.00000-00004.hipo";
        //final String dir="/Users/baltzell/data/CLAS12/rg-b/decoded/";
        //final String file="clas_006432.evio.00041-00042.hipo";
        List<String> filenames=new ArrayList<>();

        // override with user-inputs if available:
        if (args.length>0) filenames.addAll(Arrays.asList(args));
        else               filenames.add(dir+file);

        // 1!!!1 initialize the helicity sequence:
        HelicitySequenceDelayed seq = HelicityAnalysis.readSequence(filenames);
       
        seq.setVerbosity(1);

        seq.analyze();
                
        // now read the full events, e.g. during a normal physics analysis: 
      
        int nGoodEvents=0;
        int nBadEvents=0;
        
        // loop over files:
        for (String filename : filenames) {

            // open the file, initialize reader/tags/schema:
            HipoReader reader = new HipoReader();
            reader.setTags(0);
            reader.open(filename);
            SchemaFactory schema = reader.getSchemaFactory();
       
            // loop over events:
            while (reader.hasNext()) {
              
                // read the event:
                Event event=new Event();
                reader.nextEvent(event);
               
                // get the event's timestamp:
                Bank rcfgBank=new Bank(schema.getSchema("RUN::config"));
                event.read(rcfgBank);
                if (rcfgBank.getRows()<=0) continue;
                final int  evno = rcfgBank.getInt("event",0);
                final long timestamp = rcfgBank.getLong("timestamp",0);
               
                // 2!!!2 use the timestamp to get the delay-corrected helicity:
                HelicityBit predicted = seq.findPrediction(timestamp);

                if (predicted==null || predicted==HelicityBit.UDF) {
                    nBadEvents++;
                    System.out.println(String.format("Bad Helicity: event=%d time=%d helicity=%s",evno,timestamp,predicted));
                }
                else {
                    // proceed with physics analysis:
                    nGoodEvents++;
                }
            }
            reader.close();
        }
        System.out.println(String.format("HelicityAnalysisSimple:  BAD/GOOD/FRACTION=%d/%d/%.1f%%",
                nBadEvents,nGoodEvents,((float)nBadEvents)/(nBadEvents+nGoodEvents)));
    }
}
