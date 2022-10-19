package org.jlab.detector.scalers;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import org.jlab.detector.calib.utils.ConstantsManager;

import org.jlab.logging.DefaultLogger;
import org.jlab.jnp.hipo4.io.HipoReader;
import org.jlab.jnp.hipo4.data.Event;
import org.jlab.jnp.hipo4.data.Bank;
import org.jlab.jnp.hipo4.data.SchemaFactory;
import org.jlab.utils.groups.IndexedTable;

/**
 * For easy access to most recent scaler readout for any given event.
 *
 * See the main() method for example use case, where only the 2 lines
 * marked with "!!!" are specific to accessing scalers.
 * 
 * @author baltzell
 */
public class DaqScalersSequence implements Comparator<DaqScalers> {
  
    public static final double TI_CLOCK_FREQ = 250e6; // Hz
    
    protected final List<DaqScalers> scalers=new ArrayList<>();
    
    private Bank rcfgBank=null;

    public List<DaqScalers> getList() { return scalers; }

    public class Interval {
        private DaqScalers previous = null;
        private DaqScalers next = null;
        protected Interval(DaqScalersSequence seq, long t1, long t2) {
            final int idx1 = seq.findIndex(t1);
            final int idx2 = seq.findIndex(t2);
            if (idx1>=0 && idx2<scalers.size()-1) {
                this.previous = scalers.get(idx1);
                this.next = scalers.get(idx2+1);
            }
        }
        public double getBeamChargeGated() {
            if (previous!=null && next!=null) {
                return this.next.dsc2.getBeamChargeGated()
                      -this.previous.dsc2.getBeamChargeGated();
            }
            return 0;
        }
        public double getBeamCharge() {
            if (previous!=null && next!=null) {
                return this.next.dsc2.getBeamCharge()
                      -this.previous.dsc2.getBeamCharge();
            }
            return 0;
        }
        public double getBeamCurrent() {
            if (previous!=null && next!=null) {
                final double dt = (next.getTimestamp()-previous.getTimestamp())/TI_CLOCK_FREQ;
                if (dt>0) {
                    return this.getBeamCharge()/dt;
                }
            }
            return 0;
        }
    }
    
    @Override
    public int compare(DaqScalers o1, DaqScalers o2) {
        if (o1.getTimestamp() < o2.getTimestamp()) return -1;
        if (o1.getTimestamp() > o2.getTimestamp()) return +1;
        return 0;
    }
  
    public void show() {
        for (int ii=0; ii<scalers.size(); ++ii) {
            Dsc2Scaler d = scalers.get(ii).dsc2;
            String s = String.format("%d",scalers.get(ii).getTimestamp());
            s += String.format(" %d %d",d.getClock(),d.getGatedClock());
            s += String.format(" %d %d",d.getFcup(),d.getGatedFcup());
            s += String.format(" %d %d",d.getSlm(),d.getGatedSlm());
            s += String.format(" %f %f",d.getBeamCharge(),d.getBeamChargeGated());
            System.out.println(s);
        }
    }

    protected int findIndex(long timestamp) {
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
     * @param event 
     * @return the most recent DaqScalers for the given event
     */
    public DaqScalers get(Event event) {
        event.read(this.rcfgBank);
        return this.get(this.rcfgBank.getLong("timestamp", 0));
    }

    /**
     * @param timestamp TI timestamp (i.e. RUN::config.timestamp)
     * @return smallest interval of scaler readings around that timestamp
     */
    public Interval getInterval(long timestamp) {
        return this.getInterval(timestamp,timestamp);
    }
    
    /**
     * @param event
     * @return smallest interval of scaler readings around that event
     */
    public Interval getInterval(Event event) {
        event.read(this.rcfgBank);
        return this.getInterval(this.rcfgBank.getLong("timestamp", 0));
    }

    /**
     * @param t1 first TI timestamp (i.e. RUN::config.timestamp)
     * @param t2 second TI timestamp
     * @return smallest interval of scaler readings around those timestamps
     */
    public Interval getInterval(long t1,long t2) {
        return new Interval(this,t1,t2);
    }
    
    /**
     * @param event1 first event
     * @param event2 second event
     * @return smallest interval of scaler readings around those events
     */
    public Interval getInterval(Event event1, Event event2) {
        event1.read(this.rcfgBank);
        final long t1 = this.rcfgBank.getLong("timestamp",0);
        event2.read(this.rcfgBank);
        final long t2 = this.rcfgBank.getLong("timestamp",0);
        return this.getInterval(t1,t2);
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

            if (seq.rcfgBank==null) {
                seq.rcfgBank = new Bank(reader.getSchemaFactory().getSchema("RUN::config"));
            }
        
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
  
    public static DaqScalersSequence readSequenceRaw(String... filenames) {
        return readSequenceRaw(Arrays.asList(filenames));
    }

    /**
     * This reads tag=1 events for RAW::scaler banks, and initializes and returns
     * a {@link DaqScalersSequence} that can be used to access the most recent scaler
     * readout for any given event.
     * 
     * @param filenames list of names of HIPO files to read
     * @return  sequence
     */
    public static DaqScalersSequence readSequenceRaw(List<String> filenames) {

        final String CCDB_FCUP_TABLE="/runcontrol/fcup";
        final String CCDB_SLM_TABLE="/runcontrol/slm";
        final String CCDB_HEL_TABLE="/runcontrol/helicity";
        ConstantsManager conman = new ConstantsManager();
        conman.init(Arrays.asList(new String[]{CCDB_FCUP_TABLE,CCDB_SLM_TABLE,CCDB_HEL_TABLE}));

        DaqScalersSequence seq=new DaqScalersSequence();

        for (String filename : filenames) {

            HipoReader reader = new HipoReader();
            reader.setTags(1);
            reader.open(filename);

            if (seq.rcfgBank==null) {
                seq.rcfgBank = new Bank(reader.getSchemaFactory().getSchema("RUN::config"));
            }

            SchemaFactory schema = reader.getSchemaFactory();
            Event event=new Event();
            Bank scalerBank=new Bank(schema.getSchema("RAW::scaler"));
            Bank configBank=new Bank(schema.getSchema("RUN::config"));

            while (reader.hasNext()) {
            
                reader.nextEvent(event);
                event.read(scalerBank);
                event.read(configBank);
                    
                IndexedTable ccdb_fcup = conman.getConstants(configBank.getInt("run",0),CCDB_FCUP_TABLE);
                IndexedTable ccdb_slm = conman.getConstants(configBank.getInt("run",0),CCDB_SLM_TABLE);
                IndexedTable ccdb_hel = conman.getConstants(configBank.getInt("run",0),CCDB_HEL_TABLE);

                if (scalerBank.getRows()<1) continue;
                if (configBank.getRows()<1) continue;
        
                DaqScalers ds = DaqScalers.create(scalerBank, ccdb_fcup, ccdb_slm, ccdb_hel);
                ds.setTimestamp(configBank.getLong("timestamp",0));
                seq.add(ds);
            }

            reader.close();
        }
        
        return seq;
    }

    /**
     * Fix DSC2 clock rollover and recalibrate now using the the DSC2 clock
     * instead of an external one.  Note, this requires data starting from early
     * enough in a run, and no large gaps later, in order not to miss any
     * rollovers.
     * @param fcupTable
     * @param slmTable 
     */
    public void fixClockRollover(IndexedTable fcupTable, IndexedTable slmTable) {
        // maximum unsiged 32-bit, used to store the DSC2 clock:
        final long max=4294967295L;
        long offset=0;
        for (int ii=1; ii<this.scalers.size(); ++ii) {
            if (this.scalers.get(ii).dsc2.clock < this.scalers.get(ii).dsc2.clock-offset) {
                offset += max;
            }
            this.scalers.get(ii).dsc2.clock += offset;
            this.scalers.get(ii).dsc2.gatedClock += offset;
            this.scalers.get(ii).dsc2.calibrate(fcupTable, slmTable);
        }
    }

    public static void main(String[] args) {
      
        DefaultLogger.debug();

        String filename="/Users/baltzell/data/jpsitcs_005340.hipo";

        ConstantsManager conman = new ConstantsManager();
        conman.init(Arrays.asList(new String[]{"/runcontrol/fcup","/runcontrol/slm"}));
                
        IndexedTable fcup = conman.getConstants(5340,"/runcontrol/fcup");
        IndexedTable slm = conman.getConstants(5340,"/runcontrol/slm");

        DaqScalersSequence seq = DaqScalersSequence.readSequenceRaw(filename);
        
        seq.show();

        seq.fixClockRollover(fcup,slm);

        seq.show();
    }

    public static void main2(String[] args) {
        
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
                    System.out.println(timestamp+" "+ds.dsc2.getBeamCharge()+" "+ds.dsc2.getBeamChargeGated());
                }
            }

            System.out.println("DaqScalersSequence:  bad/good/badPercent: "
                    +bad+" "+good+" "+100*((float)bad)/(bad+good)+"%");

            reader.close();

        }
    }
}