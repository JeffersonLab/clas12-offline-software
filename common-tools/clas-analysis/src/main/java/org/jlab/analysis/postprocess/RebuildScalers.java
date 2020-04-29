package org.jlab.analysis.postprocess;

import java.sql.Time;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.jlab.detector.calib.utils.ConstantsManager;
import org.jlab.detector.calib.utils.RCDBConstants;
import org.jlab.detector.decode.DaqScalers;
import org.jlab.jnp.hipo4.data.Bank;
import org.jlab.jnp.hipo4.data.Event;
import org.jlab.jnp.hipo4.io.HipoReader;
import org.jlab.jnp.hipo4.io.HipoWriterSorted;
import org.jlab.utils.groups.IndexedTable;
import org.jlab.utils.options.OptionParser;
import org.jlab.utils.system.ClasUtilsFile;

/**
 * Rebuild RUN::scaler from RAW::scaler
 * 
 * @author baltzell
 */
public class RebuildScalers {

    static final String CCDB_FCUP_TABLE="/runcontrol/fcup";
    
    public static void main(String[] args) {
        
        OptionParser parser = new OptionParser("rebuildscaler");
        parser.addRequired("-o","output.hipo");
        parser.parse(args);
        List<String> inputList = parser.getInputList();
        if(inputList.isEmpty()==true){
            parser.printUsage();
            System.err.println("\n >>>> error : no input file is specified....\n");
            System.exit(1);
        }
        
        HipoWriterSorted writer = new HipoWriterSorted();
        writer.getSchemaFactory().initFromDirectory(ClasUtilsFile.getResourceDir("COATJAVA", "etc/bankdefs/hipo4"));
        writer.setCompressionType(1);
        writer.open(parser.getOption("-o").stringValue());
			
        Event event = new Event();
        Bank rawScalerBank = new Bank(writer.getSchemaFactory().getSchema("RAW::scaler"));
        Bank runScalerBank = new Bank(writer.getSchemaFactory().getSchema("RUN::scaler"));
        Bank runConfigBank = new Bank(writer.getSchemaFactory().getSchema("RUN::config"));
            
        ConstantsManager conman = new ConstantsManager();
        conman.init(Arrays.asList(new String[]{CCDB_FCUP_TABLE}));
        
        for (String filename : inputList) {

            HipoReader reader = new HipoReader();
            reader.open(filename);

            RCDBConstants rcdb = null;
            IndexedTable ccdb = null;

            while (reader.hasNext()) {

                // read the event and necessary banks:
                reader.nextEvent(event);
                event.read(runConfigBank);
                event.read(runScalerBank);
                event.read(rawScalerBank);

                // this is the bank we're here to rebuild:
                event.remove(runScalerBank.getSchema());

                // get CCDB/RCDB constants:
                if (runConfigBank.getInt("run",0) >= 100) {
                    ccdb = conman.getConstants(runConfigBank.getInt("run",0),CCDB_FCUP_TABLE);
                    rcdb = conman.getRcdbConstants(runConfigBank.getInt("run",0));
                }

                // now rebuild the RUN::scaler bank: 
                if (rcdb!=null && ccdb !=null && rawScalerBank.getRows()>0) {
                    
                    // Run duration in seconds.  Nasty but works, until RCDB (uses java.sql.Time)
                    // is changed to support full date and not just HH:MM:SS.  Meanwhile just
                    // requires that runs last less than 24 hours.
                    Date uet = new Date(runConfigBank.getInt("unixtime",0)*1000L);
                    Time rst = rcdb.getTime("run_start_time");
                    final double s1 = rst.getSeconds()+60*rst.getMinutes()+60*60*rst.getHours();
                    final double s2 = uet.getSeconds()+60*uet.getMinutes()+60*60*uet.getHours();
                    final double seconds = s2<s1 ? s2+60*60*24-s1 : s2-s1;

                    // modify RUN::scaler and put it back in the event:
                    DaqScalers ds = DaqScalers.create(rawScalerBank, ccdb, seconds);
                    runScalerBank.putFloat("fcupgated",0,ds.getBeamChargeGated());
                    runScalerBank.putFloat("fcup",0,ds.getBeamCharge());
                    runScalerBank.putFloat("livetime",0,ds.getLivetime());
                    event.write(runScalerBank);
                }

                writer.addEvent(event, event.getEventTag());
            }
            reader.close();
        }
        writer.close();
    }
}
