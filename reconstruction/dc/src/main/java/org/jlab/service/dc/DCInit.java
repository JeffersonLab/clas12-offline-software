package org.jlab.service.dc;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.dc.Constants;
import org.jlab.rec.dc.timetodistance.TableLoader;

/**
 * @author zigler
 * 
 */
public class DCInit extends DCEngine {

    public DCInit() {
        super("DCI");
    }

    @Override
    public boolean init() {
        // Load cuts
        Constants.Load();
        super.setOptions();
        super.LoadGeometry();
        super.LoadTables();
        return true;
    }

    @Override
    public boolean processDataEvent(DataEvent event) {
        int run = this.getRun(event);
        if(run==0) return true;
       
       TableLoader.FillT0Tables(run, super.variationName);
       TableLoader.Fill(super.getConstantsManager().getConstants(run, Constants.TIME2DIST));

       if (event.hasBank("MC::Particle") && this.getEngineConfigString("wireDistort")==null) {
           Constants.setWIREDIST(0);
       }

        return true;
    }

}
