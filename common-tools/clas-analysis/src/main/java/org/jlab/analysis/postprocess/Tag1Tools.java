package org.jlab.analysis.postprocess;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.jlab.clas.reco.ReconstructionEngine;
import org.jlab.detector.calib.utils.ConstantsManager;
import org.jlab.detector.calib.utils.RCDBConstants;
import org.jlab.detector.helicity.HelicityBit;
import org.jlab.detector.scalers.DaqScalers;
import org.jlab.detector.helicity.HelicitySequenceManager;
import org.jlab.detector.scalers.DaqScalersSequence;
import org.jlab.jnp.hipo4.data.Bank;
import org.jlab.jnp.hipo4.data.Event;
import org.jlab.jnp.hipo4.io.HipoReader;
import org.jlab.jnp.hipo4.io.HipoWriterSorted;
import org.jlab.utils.groups.IndexedTable;
import org.jlab.utils.options.OptionParser;
import org.jlab.utils.system.ClasUtilsFile;

/**
 * 
 * @author baltzell
 * @author wphelps
 */
public class Tag1Tools {

    static final String CCDB_FCUP_TABLE="/runcontrol/fcup";
    static final String CCDB_SLM_TABLE="/runcontrol/slm";
    static final String CCDB_HWP_TABLE="/runcontrol/hwp";

    ConstantsManager conman = new ConstantsManager();

    private final boolean flipHelicity = false;
    
    private Tag1Tools() {
        conman.init(Arrays.asList(new String[]{CCDB_FCUP_TABLE,CCDB_SLM_TABLE,CCDB_HWP_TABLE}));
    }

    /**
     * @param directory
     * @param extension
     * @return 
     */
    private static String getTemporaryFileName(String directory, String extension) throws IOException {
        File f = File.createTempFile("tmp", extension, new File(directory));
        String ret = f.getAbsolutePath();
        f.delete();
        return ret;
    }

    /**
     * Reassign helicity based on raw helicity and HWP.
     * @param inputFile
     * @param outputFile 
     */
    private void calibrateHelicity(String inputFile, String outputFile) {

        HipoWriterSorted writer = new HipoWriterSorted();
        writer.getSchemaFactory().initFromDirectory(ClasUtilsFile.getResourceDir("COATJAVA", "etc/bankdefs/hipo4"));
        writer.setCompressionType(1);
        writer.open(outputFile);

        HipoReader reader = new HipoReader();
        reader.open(inputFile);

        Event event = new Event();
        Bank runConfigBank = new Bank(writer.getSchemaFactory().getSchema("RUN::config"));

        List<Bank> helBanks = new ArrayList<>();
        helBanks.add(new Bank(writer.getSchemaFactory().getSchema("HEL::flip")));
        helBanks.add(new Bank(writer.getSchemaFactory().getSchema("HEL::online")));
        helBanks.add(new Bank(writer.getSchemaFactory().getSchema("REC::Event")));
        helBanks.add(new Bank(writer.getSchemaFactory().getSchema("RECHB::Event")));
        helBanks.add(new Bank(writer.getSchemaFactory().getSchema("RECFT::Event")));

        while (reader.hasNext()) {

            reader.nextEvent(event);
            event.read(runConfigBank);

            if (!event.hasBank(runConfigBank.getSchema())) continue;
            if (runConfigBank.getInt("run",0) < 100) continue;

            IndexedTable ccdb_hwp = conman.getConstants(runConfigBank.getInt("run",0),CCDB_HWP_TABLE);
            final int hwp = ccdb_hwp.getIntValue("hwp",0,0,0);

            // reassign helicity based on helicityRaw and HWP:
            for (Bank bank : helBanks) {
                if (event.hasBank(bank.getSchema())) {
                    event.read(bank);
                    event.remove(bank.getSchema());
                    byte helicity = (byte)(hwp * bank.getByte("helicityRaw",0));
                    if (flipHelicity) {
                        helicity *= -1;
                    }
                    bank.setByte("helicity",0,helicity);
                    event.write(bank);
                }
            }
            writer.addEvent(event, event.getEventTag());
        }
        reader.close();
        writer.close();
    }
   
    /**
     * Rebuild calibrated scaler banks from raw scaler banks. 
     * @param inputFiles
     * @param outputFile 
     */
    private void rebuildScalers(String inputFile, String outputFile) {
        List<String> inputFiles = new ArrayList<>();
        inputFiles.add(inputFile);
        rebuildScalers(inputFiles,outputFile);
    }

    /**
     * Rebuild calibrated scaler banks from raw scaler banks. 
     * @param inputFiles
     * @param outputFile 
     */
    private void rebuildScalers(List<String> inputFiles, String outputFile) {

        HelicitySequenceManager helSeqMgr = new HelicitySequenceManager(8, inputFiles);

        HipoWriterSorted writer = new HipoWriterSorted();
        writer.getSchemaFactory().initFromDirectory(ClasUtilsFile.getResourceDir("COATJAVA", "etc/bankdefs/hipo4"));
        writer.setCompressionType(1);
        writer.open(outputFile);
			
        Event event = new Event();
        Bank rawScalerBank = new Bank(writer.getSchemaFactory().getSchema("RAW::scaler"));
        Bank runScalerBank = new Bank(writer.getSchemaFactory().getSchema("RUN::scaler"));
        Bank helScalerBank = new Bank(writer.getSchemaFactory().getSchema("HEL::scaler"));
        Bank runConfigBank = new Bank(writer.getSchemaFactory().getSchema("RUN::config"));
            
        RCDBConstants rcdb = null;
        IndexedTable ccdb_fcup = null;
        IndexedTable ccdb_slm = null;
        
        for (String inputFile : inputFiles) {
            
            HipoReader reader = new HipoReader();
            reader.open(inputFile);

            while (reader.hasNext()) {

                // read the event and necessary banks:
                reader.nextEvent(event);
                event.read(runConfigBank);
                event.read(runScalerBank);
                event.read(helScalerBank);
                event.read(rawScalerBank);

                // these are the banks we're here to rebuild:
                event.remove(runScalerBank.getSchema());
                event.remove(helScalerBank.getSchema());

                // get CCDB/RCDB constants:
                if (runConfigBank.getInt("run",0) >= 100) {
                    ccdb_fcup = conman.getConstants(runConfigBank.getInt("run",0),CCDB_FCUP_TABLE);
                    ccdb_slm = conman.getConstants(runConfigBank.getInt("run",0),CCDB_SLM_TABLE);
                    rcdb = conman.getRcdbConstants(runConfigBank.getInt("run",0));
                }
            
                // now rebuild the RUN::scaler bank: 
                if (rcdb!=null && ccdb_fcup!=null) {
                
                    // Inputs for calculation run duration in seconds, since for
                    // some run periods the DSC2 clock rolls over during a run.
                    Time rst = rcdb.getTime("run_start_time");
                    Date uet = new Date(runConfigBank.getInt("unixtime",0)*1000L);
                    
                    DaqScalers ds = DaqScalers.create(rawScalerBank, ccdb_fcup, ccdb_slm, rst, uet);
                    runScalerBank = ds.createRunBank(writer.getSchemaFactory());
                    helScalerBank = ds.createHelicityBank(writer.getSchemaFactory());
                    
                    // the scaler banks always are slightly after the helicity changes, so
                    // assign the previous (delay-corrected) helicity state to this scaler reading:
                    helScalerBank.putByte("helicity",0,helSeqMgr.search(event,-1).value());
                    if (helSeqMgr.getHalfWavePlate(event))
                        helScalerBank.putByte("helicityRaw",0,(byte)(-1*helSeqMgr.search(event,-1).value()));
                    else
                        helScalerBank.putByte("helicityRaw",0,helSeqMgr.search(event,-1).value());
                    
                    // put modified HEL/RUN::scaler back in the event:
                    event.write(runScalerBank);
                    event.write(helScalerBank);
                }
                
                writer.addEvent(event, event.getEventTag());
            }
            reader.close();
        }
        writer.close();
    }

    private void copyTag1ToEvent(List<String> inputFiles, String outputFile) {
    
        HelicitySequenceManager helSeqMgr = new HelicitySequenceManager(8, inputFiles);
        DaqScalersSequence chargeSeq = DaqScalersSequence.readSequence(inputFiles);

        final String[] CREATE_TAG1_EVENTS = {ReconstructionEngine.CONFIG_BANK_NAME};

        HipoWriterSorted writer = new HipoWriterSorted();
        writer.getSchemaFactory().initFromDirectory(ClasUtilsFile.getResourceDir("COATJAVA", "etc/bankdefs/hipo4"));
        writer.setCompressionType(1);
        writer.open(outputFile);
			
        Event event = new Event();

        Event configEvent = new Event();

        List<Bank> evntBanks = new ArrayList<>();
        evntBanks.add(new Bank(writer.getSchemaFactory().getSchema("REC::Event")));
        evntBanks.add(new Bank(writer.getSchemaFactory().getSchema("RECHB::Event")));
        evntBanks.add(new Bank(writer.getSchemaFactory().getSchema("RECFT::Event")));

        // we're going to copy these banks to new tag-1 events:
        List<Bank> configBanks = new ArrayList<>();
        for (String bankName : CREATE_TAG1_EVENTS) {
            configBanks.add(new Bank(writer.getSchemaFactory().getSchema(bankName)));
        }

        long badCharge = 0;
        long goodCharge = 0;
        long badHelicity = 0;
        long goodHelicity = 0;

        for (String filename : inputFiles) {

            HipoReader reader = new HipoReader();
            reader.open(filename);

            while (reader.hasNext()) {

                reader.nextEvent(event);
                
                // do the lookups:
                HelicityBit hb = helSeqMgr.search(event);
                DaqScalers ds = chargeSeq.get(event);
                
                // count good/bad helicity and charge;
                if (ds==null) badCharge++;
                else          goodCharge++;
                if (Math.abs(hb.value())==1) goodHelicity++;
                else badHelicity++;

                // copy charge/helicity to event banks:
                for (Bank bank : evntBanks) {
                    event.read(bank);
                    event.remove(bank.getSchema());
                    if (bank.getRows()>0) {
                        bank.putByte("helicity",0,hb.value());
                        if (ds!=null) {
                            bank.putFloat("beamCharge",0, (float) ds.dsc2.getBeamChargeGated());
                            bank.putDouble("liveTime",0,ds.dsc2.getLivetime());
                        }
                        event.write(bank);
                    }
                }

                // update the output file:
                writer.addEvent(event, event.getEventTag());

                // copy config banks to new tag-1 events:
                configEvent.reset();
                for (Bank bank : configBanks) {
                    event.read(bank);
                    if (bank.getRows()>0) {
                        configEvent.write(bank);
                    }
                }
                if (!configEvent.isEmpty()) {
                    writer.addEvent(configEvent, 1);
                }

            }
            reader.close();
        }
        writer.close();

        System.out.println(String.format("Tag1ToEvent:  Good Helicity Fraction: %.2f%%",100*(float)goodHelicity/(goodHelicity+badHelicity)));
        System.out.println(String.format("Tag1ToEvent:  Good Charge   Fraction: %.2f%%",100*(float)goodCharge/(goodCharge+badCharge)));

    }
    
    public static void main(String[] args) throws IOException {

        OptionParser parser = new OptionParser("rebuildscaler");
        parser.addRequired("-o","output.hipo");
        parser.addOption("-f","0","do global offline helicity flip (0/1=false/true)");
        parser.parse(args);
        List<String> inputFiles = parser.getInputList();
        if(inputFiles.isEmpty()==true){
            parser.printUsage();
            System.err.println("\n >>>> error : no input file is specified....\n");
            System.exit(1);
        }

        Tag1Tools tt = new Tag1Tools();

        final int nFiles = inputFiles.size();
        List<String> tmpFiles = new ArrayList<>();

        for (int ii=0; ii<nFiles; ii++) {
            String inputFile = inputFiles.remove(0);
            String tmpFile1 = getTemporaryFileName(".", ".hipo");
            tt.calibrateHelicity(inputFile, tmpFile1);
            //Files.delete(Paths.get(inputFile));
            String tmpFile2 = getTemporaryFileName(".", ".hipo");
            tt.rebuildScalers(tmpFile1, tmpFile2);
            Files.delete(Paths.get(tmpFile1));
            inputFiles.add(tmpFile2);
            tmpFiles.add(tmpFile2);
        }
        
        tt.copyTag1ToEvent(inputFiles, parser.getOption("-o").stringValue());

        for (String tmpFile : tmpFiles) {
            Files.delete(Paths.get(tmpFile));
        }

    }
}
