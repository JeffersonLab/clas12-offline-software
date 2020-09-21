package org.jlab.detector.scalers;

import org.jlab.jnp.hipo4.data.Bank;
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

    @Deprecated public double getBeamChargeGated() { return this.dsc2.getBeamChargeGated(); }
    @Deprecated public double getBeamCharge() { return this.dsc2.getBeamCharge(); }
    @Deprecated public double getLivetime() { return this.struck.getLivetime(); }
    
    /**
    * @param runScalerBank HIPO RUN::scaler bank
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
    * @param fcupTable /runcontrol/fcup IndexedTable from CCDB
    * @param seconds duration between run start and current event
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
    * Same as create(Bank,IndexedTable,double), except relies on DSC2's clock.
    *
    * @param rawScalerBank HIPO RAW::scaler bank
    * @param fcupTable /runcontrol/fcup IndexedTable from CCDB
    */
    public static DaqScalers create(Bank rawScalerBank,IndexedTable fcupTable,IndexedTable slmTable) {
        Dsc2Scaler dsc2 = new Dsc2Scaler(rawScalerBank,fcupTable,slmTable);
        return create(rawScalerBank,fcupTable,slmTable,dsc2.getGatedClockSeconds());
    }





}

