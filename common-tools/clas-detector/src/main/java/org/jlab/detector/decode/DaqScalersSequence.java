package org.jlab.detector.decode;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import org.jlab.jnp.hipo4.io.HipoReader;
import org.jlab.jnp.hipo4.data.Event;
import org.jlab.jnp.hipo4.data.Bank;
import org.jlab.jnp.hipo4.data.SchemaFactory;

/**
 * For easy access to most recent scaler readout for any given event.
 *
 * See the main() method for example use case, where only the 2 lines
 * marked with "!!!" are specific to accessing scalers.
 * 
 * @author baltzell
 */
public class DaqScalersSequence implements Comparator<DaqScalers> {
   
    private final List<DaqScalers> scalers=new ArrayList<>();
   
    @Override
    public int compare(DaqScalers o1, DaqScalers o2) {
        if (o1.getTimestamp() < o2.getTimestamp()) return -1;
        if (o1.getTimestamp() > o2.getTimestamp()) return +1;
        return 0;
    }
   
    private int findIndex(long timestamp) {
        if (this.scalers.isEmpty()) return -1;
        if (timestamp < this.scalers.get(0).getTimestamp()) return -1;
        // assume late timestamps are ok and go with last readout, so comment this out:
        //if (timestamp > this.scalers.get(this.scalers.size()-1).getTimestamp()) return -1;
        // make a fake state for timestamp search:
        DaqScalers ds=new DaqScalers();
        ds.setTimestamp(timestamp);
        final int index=Collections.binarySearch(this.scalers,ds,new DaqScalersSequence());
        final int n = index<0 ? -index-2 : index;
        return n;
    }
   
    protected boolean add(DaqScalers ds) {
        if (this.scalers.isEmpty()) {
            this.scalers.add(ds);
            return true;
        }
        else {
            final int index=Collections.binarySearch(this.scalers,ds,new DaqScalersSequence());
            if (index==this.scalers.size()) {
                // its timestamp is later than the existing sequence:
                this.scalers.add(ds);
                return true;
            }
            else if (index<0) {
                // it's a unique timestamp, insert it:
                this.scalers.add(-index-1,ds);
                return true;
            }
            else {
                // it's a duplicate timestamp, ignore it:
                return false;
            }
        }
    }
    
    /**
     * @param timestamp TI timestamp (i.e. RUN::config.timestamp)
     * @return the most recent DaqScalers for the given timestamp
     */
    public DaqScalers get(long timestamp) {
        final int n=this.findIndex(timestamp);
        if (n>=0) return this.scalers.get(n);
        return null;
    }
   
    /**
     * This reads tag=1 events for RUN::scaler banks, and initializes and returns
     * a {@link DaqScalersSequence} that can be used to access the most recent scaler
     * readout for any given event.
     * 
     * @param filenames list of names of HIPO files to read
     * @return  sequence
     */
    public static DaqScalersSequence readSequence(List<String> filenames) {
       
        DaqScalersSequence seq=new DaqScalersSequence();

        for (String filename : filenames) {

            HipoReader reader = new HipoReader();
            reader.setTags(1);
            reader.open(filename);
        
            SchemaFactory schema = reader.getSchemaFactory();
        
            while (reader.hasNext()) {
            
                Event event=new Event();
                Bank scalerBank=new Bank(schema.getSchema("RUN::scaler"));
                Bank configBank=new Bank(schema.getSchema("RUN::config"));
            
                reader.nextEvent(event);
                event.read(scalerBank);
                event.read(configBank);
         
                long timestamp=0;
                
                if (scalerBank.getRows()<1) continue;
                if (configBank.getRows()>0) {
                    timestamp=configBank.getLong("timestamp",0);
                }
        
                DaqScalers ds=DaqScalers.create(scalerBank);
                ds.setTimestamp(timestamp);
                seq.add(ds);
            }

            reader.close();
        }
        
        return seq;
    }
    
    public static void main(String[] args) {
        
        final String dir="/Users/baltzell/data/CLAS12/rg-a/decoded/6b.2.0/";
        final String file="clas_005038.evio.00000-00004.hipo";
        //final String dir="/Users/baltzell/data/CLAS12/rg-b/decoded/";
        //final String file="clas_006432.evio.00041-00042.hipo";

        List<String> filenames=new ArrayList<>();
        if (args.length>0) filenames.addAll(Arrays.asList(args));
        else               filenames.add(dir+file);

        // 1!!!1 initialize a sequence from tag=1 events: 
        DaqScalersSequence seq = DaqScalersSequence.readSequence(filenames);

        long good=0;
        long bad=0;
        
        for (String filename : filenames) {

            HipoReader reader = new HipoReader();
            reader.setTags(0);
            reader.open(filename);
            
            SchemaFactory schema = reader.getSchemaFactory();
        
            while (reader.hasNext()) {

                Bank rcfgBank=new Bank(schema.getSchema("RUN::config"));
               
                Event event=new Event();
                reader.nextEvent(event);
              
                event.read(rcfgBank);
            
                long timestamp = -1;
                if (rcfgBank.getRows()>0) 
                    timestamp = rcfgBank.getLong("timestamp",0);

                // 2!!!2 use the timestamp to get the most recent scaler data:
                DaqScalers ds=seq.get(timestamp);

                if (ds==null) {
                    bad++;
                }
                else {
                    good++;
                    // do something useful with beam charge here:
                    System.out.println(timestamp+" "+ds.getBeamCharge()+" "+ds.getBeamChargeGated());
                }
            }

            System.out.println("DaqScalersSequence:  bad/good/badPercent: "
                    +bad+" "+good+" "+100*((float)bad)/(bad+good)+"%");

            reader.close();

        }
    }
}
