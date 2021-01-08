package org.jlab.analysis.postprocess;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.jlab.detector.calib.utils.ConstantsManager;
import org.jlab.jnp.hipo4.data.Bank;
import org.jlab.jnp.hipo4.data.Event;
import org.jlab.jnp.hipo4.io.HipoReader;
import org.jlab.jnp.hipo4.io.HipoWriterSorted;
import org.jlab.utils.groups.IndexedTable;
import org.jlab.utils.options.OptionParser;
import org.jlab.utils.system.ClasUtilsFile;

/**
 * Rebuild helicity banks based on their helicityRaw variables and current HWP
 * state from CCDB.
 * 
 * @author baltzell
 */
public class RebuildHelicity {
    
    static final String CCDB_HWP_TABLE="/runcontrol/hwp";
    
    public static void main(String[] args) {
        
        OptionParser parser = new OptionParser("rebuildHelicity");
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
			
        IndexedTable ccdb_hwp = null;
        Event event = new Event();
        Bank runConfigBank = new Bank(writer.getSchemaFactory().getSchema("RUN::config"));
        
        // banks to recreate:
        List<Bank> helBanks = new ArrayList<>();
        helBanks.add(new Bank(writer.getSchemaFactory().getSchema("HEL::flip")));
        helBanks.add(new Bank(writer.getSchemaFactory().getSchema("HEL::online")));
            
        ConstantsManager conman = new ConstantsManager();
        conman.init(Arrays.asList(new String[]{CCDB_HWP_TABLE}));
        
        for (String filename : inputList) {

            HipoReader reader = new HipoReader();
            reader.open(filename);

            while (reader.hasNext()) {

                reader.nextEvent(event);
                event.read(runConfigBank);

                if (!event.hasBank(runConfigBank.getSchema())) continue;
                if (runConfigBank.getInt("run",0) < 100) continue;
                
                ccdb_hwp = conman.getConstants(runConfigBank.getInt("run",0),CCDB_HWP_TABLE);
                final int hwp = ccdb_hwp.getIntValue("hwp",0,0,0);

                for (Bank bank : helBanks) {
                    if (event.hasBank(bank.getSchema())) {
                        event.read(bank);
                        event.remove(bank.getSchema());
                        bank.setByte("helicity",0,(byte)(hwp*bank.getByte("helicityRaw",0)));
                        event.write(bank);
                    }
                }
                writer.addEvent(event, event.getEventTag());
            }
            reader.close();
        }
        writer.close();
    }
}
