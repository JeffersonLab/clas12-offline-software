package org.jlab.detector.scalers;

import java.sql.Time;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
    public StruckScalers struck=null;

    private long timestamp=0;
    public void setTimestamp(long timestamp) { this.timestamp=timestamp; }
    public long getTimestamp(){ return this.timestamp; }

    private static final ZoneId zoneId = ZoneId.of( "America/New_York" );

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
     * Get seconds between two times specified as a local time and a unix time
     * assuming they differ by less than 24 h
     * 
     * @param rst  run start local local time
     * @param uet  unix event time
     * @return 
     */
    public static double getSeconds(Time rst,long uet) {
        // seconds since 00:00:00, on their given day:
        ZonedDateTime et = ZonedDateTime.ofInstant(Instant.ofEpochSecond(uet), zoneId);
        ZonedDateTime st = ZonedDateTime.of(et.toLocalDate(), rst.toLocalTime(), zoneId);
        if(st.isAfter(et)) st.minusDays(1L);
        return (double) (et.toEpochSecond()-st.toEpochSecond());
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
     * @param helTable
     * @param seconds duration between run start and current event
     * @return 
     */
    public static DaqScalers create(Bank rawScalerBank,IndexedTable fcupTable,IndexedTable slmTable,IndexedTable helTable,double seconds) {
        StruckScalers struck = StruckScalers.read(rawScalerBank,fcupTable,slmTable,helTable);
        Dsc2Scaler dsc2 = new Dsc2Scaler(rawScalerBank,fcupTable,slmTable,seconds);
        DaqScalers ds = new DaqScalers();
        ds.dsc2 = dsc2;
        ds.struck = struck;
        return ds;
    }

    /**
     * @param rawScalerBank HIPO RAW::scaler bank
     * @param fcupTable /runcontrol/fcup from CCDB
     * @param slmTable /runcontrol/slm from CCDB
     * @param helTable /runcontrol/helicity from CCDB
     * @param rst run start time
     * @param uet unix event time
     * @return 
     */
    public static DaqScalers create(Bank rawScalerBank,IndexedTable fcupTable,IndexedTable slmTable,IndexedTable helTable,Date rst, Date uet) {
        return DaqScalers.create(rawScalerBank,fcupTable,slmTable,helTable,DaqScalers.getSeconds(rst, uet));
    }

    /**
     * @param rawScalerBank HIPO RAW::scaler bank
     * @param fcupTable /runcontrol/fcup from CCDB
     * @param slmTable /runcontrol/slm from CCDB
     * @param helTable /runcontrol/helicity from CCDB
     * @param zone run start time zone
     * @param rst  run start time
     * @param uet  unix event time
     * @return 
     */
    public static DaqScalers create(Bank rawScalerBank,IndexedTable fcupTable,IndexedTable slmTable,IndexedTable helTable,Time rst,long uet) {
        return DaqScalers.create(rawScalerBank,fcupTable,slmTable,helTable,DaqScalers.getSeconds(rst, uet));
    }

    /**
     * Same as create(Bank,IndexedTable,double), except relies on DSC2's clock.
     *
     * @param rawScalerBank HIPO RAW::scaler bank
     * @param fcupTable /runcontrol/fcup from CCDB
     * @param slmTable /runcontrol/slm from CCDB
     * @param helTable /runcontrol/helicity from CCDB
     * @return  
     */
    public static DaqScalers create(Bank rawScalerBank,IndexedTable fcupTable,IndexedTable slmTable,IndexedTable helTable) {
        Dsc2Scaler dsc2 = new Dsc2Scaler(rawScalerBank,fcupTable,slmTable);
        return DaqScalers.create(rawScalerBank,fcupTable,slmTable,helTable,dsc2.getGatedClockSeconds());
    }

    /**
     * @param schema bank schema
     * @return RUN::scaler banks
     */
    public Bank createRunBank(SchemaFactory schema) {
        Bank bank = new Bank(schema.getSchema("RUN::scaler"),1);
        bank.putFloat("fcup",0,(float)this.dsc2.getBeamCharge());
        bank.putFloat("fcupgated",0,(float)this.dsc2.getBeamChargeGated());
        if (this.struck.size() > 0)
          bank.putFloat("livetime",0,(float)this.struck.get(this.struck.size()-1).getLivetimeClock());
        return bank;
    }

    /**
     * @param schema bank schema
     * @return HEL::scaler banks
     */
    public Bank createHelicityBank(SchemaFactory schema) {
        Bank bank = new Bank(schema.getSchema("HEL::scaler"),this.struck.size());
        for (StruckScaler ss : this.struck) {
            bank.putFloat("fcup",0,(float)ss.getBeamCharge());
            bank.putFloat("fcupgated",0,(float)ss.getBeamChargeGated());
            bank.putFloat("slm",0,(float)ss.getBeamChargeSLM());
            bank.putFloat("slmgated",0,(float)ss.getBeamChargeGatedSLM());
            bank.putFloat("clock",0,(float)ss.getClock());
            bank.putFloat("clockgated",0,(float)ss.getGatedClock());
        }
        return bank;
    }

    /**
     * @param rawScalerBank RAW::scaler bank
     * @param schema bank schema
     * @param fcupTable /runcontrol/fcup CCDB table
     * @param slmTable /runcontrol/slm CCDB table
     * @param helTable /runcontrol/helicity CCDB table
     * @return [RUN::scaler,HEL::scaler] banks
     */
    public static List<Bank> createBanks(SchemaFactory schema,Bank rawScalerBank,IndexedTable fcupTable,IndexedTable slmTable,IndexedTable helTable) {
        DaqScalers ds = DaqScalers.create(rawScalerBank,fcupTable,slmTable,helTable);
        List<Bank> ret = new ArrayList<>();
        // only add the RUN::scaler bank if we actually got a DSC2 readout:
        if (ds.dsc2.getClock()>0 || ds.dsc2.getGatedClock()>0) {
            ret.add(ds.createRunBank(schema));
        }
        // only add the HEL::scaler bank if we actually got a Struck readout:
        if (!ds.struck.isEmpty()) {
            ret.add(ds.createHelicityBank(schema));
        }
        return ret;
    }

    /**
     * @param rawScalerBank RAW::scaler bank
     * @param schema bank schema
     * @param fcupTable /runcontrol/fcup CCDB table
     * @param slmTable /runcontrol/slm CCDB table
     * @param helTable /runcontrol/helicity CCDB table
     * @param seconds duration between run start and current event
     * @return [RUN::scaler,HEL::scaler] banks
     */
    public static List<Bank> createBanks(SchemaFactory schema,Bank rawScalerBank,IndexedTable fcupTable,IndexedTable slmTable,IndexedTable helTable,double seconds) {
        DaqScalers ds = DaqScalers.create(rawScalerBank,fcupTable,slmTable,helTable,seconds);
        List<Bank> ret = new ArrayList<>();
        // only add the RUN::scaler bank if we actually got a DSC2 readout:
        if (ds.dsc2.getClock()>0 || ds.dsc2.getGatedClock()>0) {
            ret.add(ds.createRunBank(schema));
        }
        // only add the HEL::scaler bank if we actually got a Struck readout:
        if (!ds.struck.isEmpty()) {
            ret.add(ds.createHelicityBank(schema));
        }
        return ret;
    }

    /**
     * @param rawScalerBank RAW::scaler bank
     * @param schema bank schema
     * @param fcupTable /runcontrol/fcup CCDB table
     * @param slmTable /runcontrol/slm CCDB table
     * @param helTable /runcontrol/helicity CCDB table
     * @param rst run start time
     * @param uet event time
     * @return [RUN::scaler,HEL::scaler] banks
     */
    public static List<Bank> createBanks(SchemaFactory schema,Bank rawScalerBank,IndexedTable fcupTable,IndexedTable slmTable,IndexedTable helTable,Date rst,Date uet) {
        return DaqScalers.createBanks(schema,rawScalerBank,fcupTable,slmTable,helTable,DaqScalers.getSeconds(rst,uet));
    }

    /**
     * @param rawScalerBank RAW::scaler bank
     * @param schema bank schema
     * @param fcupTable /runcontrol/fcup CCDB table
     * @param slmTable /runcontrol/slm CCDB table
     * @param helTable /runcontrol/helicity CCDB table
     * @param rst run start time
     * @param uet event time
     * @return [RUN::scaler,HEL::scaler] banks
     */
    public static List<Bank> createBanks(SchemaFactory schema,Bank rawScalerBank,IndexedTable fcupTable,IndexedTable slmTable,IndexedTable helTable,Time rst,long uet) {
        return DaqScalers.createBanks(schema,rawScalerBank,fcupTable,slmTable,helTable,DaqScalers.getSeconds(rst,uet));
    }

}

