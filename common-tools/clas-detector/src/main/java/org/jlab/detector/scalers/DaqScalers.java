package org.jlab.detector.scalers;

import java.util.Date;
import org.jlab.jnp.hipo4.data.Bank;
import org.jlab.jnp.hipo4.data.SchemaFactory;
import org.jlab.utils.groups.IndexedTable;

/**
 *
 * Read the occasional scaler bank, extract beam charge, livetime, etc.
 *
 * We have at least two relevant scaler hardware boards, STRUCK and DSC2, both
 * readout on helicity flips and with DAQ-busy gating, both decoded into RAW::scaler.
 * This class reads RAW::scaler and converts to more user-friendly information.
 *
 * STRUCK.  Latching on helicity states, zeroed upon readout, with both helicity
 * settle (normally 500 us) and non-settle counts, useful for "instantaneous"
 * livetime, beam charge asymmetry, beam trip studies, ...
 *
 * DSC2.  Integrating since beginning of run, useful for beam charge normalization.
 *
 * @see <a href="https://logbooks.jlab.org/comment/14616">logbook entry</a>
 * and common-tools/clas-detector/doc
 *
 * The EPICS equation for converting Faraday Cup raw scaler S to beam current I:
 *   I [nA] = (S [Hz] - offset ) / slope * attenuation;
 *
 * offset/slope/attenuation are read from CCDB
 *
 * Accounting for the offset in accumulated beam charge requires knowledge of
 * time duration.  Currently, the (32 bit) DSC2 clock is zeroed at run start
 * but at 1 Mhz rolls over every 35 seconds, and the (48 bit) 250 MHz TI timestamp
 * can also rollover within a run since only zeroed upon reboot.  Instead we allow
 * run duration to be passed in, e.g. using run start time from RCDB and event
 * unix time from RUN::config.
 *
 * FIXME:  Use CCDB for GATEINVERTED, CLOCK_FREQ, CRATE/SLOT/CHAN
 *
 * @author baltzell
 */
public class DaqScalers {

    public Dsc2Scaler dsc2=null;
    public StruckScaler struck=null;

    private long timestamp=0;
    public void setTimestamp(long timestamp) { this.timestamp=timestamp; }
    public long getTimestamp(){ return this.timestamp; }
    
    /**
     * Get seconds between two dates assuming the differ by not more than 24 hours.
     *
     * The 24 hour requirement is because the java RCDB library currently provides
     * times as java.sql.Time, which only supports HH:MM:SS and not full date.
     * 
     * Necessitated because run-integrating DSC2's clock frequency in some run
     * periods was too large and rolls over during run.  And that was the only
     * clock that is reset at beginning of the run.
     * 
     * Since DAQ runs are never 24 hours, this works.
     * 
     * @param rst run start time
     * @param uet unix event time
     * @return 
     */
    public static double getSeconds(Date rst,Date uet) {
        // seconds since 00:00:00, on their given day:
        final double s1 = rst.getSeconds()+60*rst.getMinutes()+60*60*rst.getHours();
        final double s2 = uet.getSeconds()+60*uet.getMinutes()+60*60*uet.getHours();
        return s2<s1 ? s2+60*60*24-s1 : s2-s1;
    }

    /**
     * @param runScalerBank HIPO RUN::scaler bank
     * @return 
     */
    public static DaqScalers create(Bank runScalerBank) {
        DaqScalers ds=new DaqScalers();
        ds.dsc2=new Dsc2Scaler();
        for (int ii=0; ii<runScalerBank.getRows(); ii++) {
            ds.dsc2.setLivetime(runScalerBank.getFloat("livetime", ii));
            ds.dsc2.setBeamCharge(runScalerBank.getFloat("fcup",ii));
            ds.dsc2.setBeamChargeGated(runScalerBank.getFloat("fcupgated",ii));
            break; 
        }
        return ds;
    }

    /**
     * @param rawScalerBank HIPO RAW::scaler bank
     * @param fcupTable /runcontrol/fcup from CCDB
     * @param slmTable /runcontrol/slm from CCDB
     * @param seconds duration between run start and current event
     * @return 
     */
    public static DaqScalers create(Bank rawScalerBank,IndexedTable fcupTable,IndexedTable slmTable,double seconds) {
        StruckScaler struck = new StruckScaler(rawScalerBank,fcupTable,slmTable);
        Dsc2Scaler dsc2 = new Dsc2Scaler(rawScalerBank,fcupTable,slmTable,seconds);
        if (dsc2.getClock()>0 || struck.getClock()>0) {
            DaqScalers ds=new DaqScalers();
            ds.dsc2=dsc2;
            ds.struck=struck;
            return ds;
        }
        return null;
    }
    
    /**
     * @param rawScalerBank HIPO RAW::scaler bank
     * @param fcupTable /runcontrol/fcup from CCDB
     * @param slmTable /runcontrol/slm from CCDB
     * @param rst run start time
     * @param uet unix event time
     * @return 
     */
    public static DaqScalers create(Bank rawScalerBank,IndexedTable fcupTable,IndexedTable slmTable,Date rst, Date uet) {
        return DaqScalers.create(rawScalerBank,fcupTable,slmTable,DaqScalers.getSeconds(rst, uet));
    }
    
    /**
     * Same as create(Bank,IndexedTable,double), except relies on DSC2's clock.
     *
     * @param rawScalerBank HIPO RAW::scaler bank
     * @param fcupTable /runcontrol/fcup from CCDB
     * @param slmTable /runcontrol/slm from CCDB
     * @return  
     */
    public static DaqScalers create(Bank rawScalerBank,IndexedTable fcupTable,IndexedTable slmTable) {
        Dsc2Scaler dsc2 = new Dsc2Scaler(rawScalerBank,fcupTable,slmTable);
        return DaqScalers.create(rawScalerBank,fcupTable,slmTable,dsc2.getGatedClockSeconds());
    }

    /**
     * @param schema bank schema
     * @return RUN::scaler banks
     */
    public Bank createRunBank(SchemaFactory schema) {
        Bank bank = new Bank(schema.getSchema("RUN::scaler"),1);
        bank.putFloat("fcup",0,(float)this.dsc2.getBeamCharge());
        bank.putFloat("fcupgated",0,(float)this.dsc2.getBeamChargeGated());
        bank.putFloat("livetime",0,(float)this.struck.getLivetimeClock());
        return bank;
    }

    /**
     * @param schema bank schema
     * @return HEL::scaler banks
     */
    public Bank createHelicityBank(SchemaFactory schema) {
        Bank bank = new Bank(schema.getSchema("HEL::scaler"),1);
        bank.putFloat("fcup",0,(float)this.struck.getBeamCharge());
        bank.putFloat("fcupgated",0,(float)this.struck.getBeamChargeGated());
        bank.putFloat("slm",0,(float)this.struck.getBeamChargeSLM());
        bank.putFloat("slmgated",0,(float)this.struck.getBeamChargeGatedSLM());
        bank.putFloat("clock",0,(float)this.struck.getClock());
        bank.putFloat("clockgated",0,(float)this.struck.getGatedClock());
        return bank;
    }
        
    /**
     * @param rawScalerBank RAW::scaler bank
     * @param schema bank schema
     * @param fcupTable /runcontrol/fcup CCDB table
     * @param slmTable /runcontrol/slm CCDB table
     * @return [RUN::scaler,HEL::scaler] banks
     */
    public static Bank[] createBanks(SchemaFactory schema,Bank rawScalerBank,IndexedTable fcupTable,IndexedTable slmTable) {
        DaqScalers ds = DaqScalers.create(rawScalerBank,fcupTable,slmTable);
        if (ds==null) return null;
        Bank ret[] = {ds.createRunBank(schema),ds.createHelicityBank(schema)};
        return ret;
    }
    
    /**
     * @param rawScalerBank RAW::scaler bank
     * @param schema bank schema
     * @param fcupTable /runcontrol/fcup CCDB table
     * @param slmTable /runcontrol/slm CCDB table
     * @param seconds duration between run start and current event
     * @return [RUN::scaler,HEL::scaler] banks
     */
    public static Bank[] createBanks(SchemaFactory schema,Bank rawScalerBank,IndexedTable fcupTable,IndexedTable slmTable,double seconds) {
        DaqScalers ds = DaqScalers.create(rawScalerBank,fcupTable,slmTable,seconds);
        if (ds==null) return null;
        Bank ret[] = {ds.createRunBank(schema),ds.createHelicityBank(schema)};
        return ret;
    }

    public static Bank[] createBanks(SchemaFactory schema,Bank rawScalerBank,IndexedTable fcupTable,IndexedTable slmTable,Date rst,Date uet) {
        return DaqScalers.createBanks(schema,rawScalerBank,fcupTable,slmTable,DaqScalers.getSeconds(rst,uet));
    }

}

