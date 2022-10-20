package org.jlab.analysis.postprocess;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.jlab.detector.calib.utils.ConstantsManager;
import org.jlab.detector.calib.utils.RCDBConstants;
import org.jlab.detector.helicity.HelicityBit;
import org.jlab.detector.helicity.HelicitySequenceManager;
import org.jlab.detector.scalers.DaqScalers;
import org.jlab.detector.scalers.DaqScalersSequence;

import org.jlab.jnp.hipo4.data.Bank;
import org.jlab.jnp.hipo4.data.Event;
import org.jlab.jnp.hipo4.data.SchemaFactory;
import org.jlab.jnp.hipo4.io.HipoReader;
import org.jlab.jnp.hipo4.io.HipoWriterSorted;

import org.jlab.logging.DefaultLogger;
import org.jlab.utils.groups.IndexedTable;
import org.jlab.utils.options.OptionParser;
import org.jlab.utils.system.ClasUtilsFile;

/**
 *
 * @author baltzell
 */
public class Postprocess {

    public static void main(String[] args) {

        DefaultLogger.debug();
        
        OptionParser parser = new OptionParser("postprocess");
        parser.addOption("-r","0","rebuild RUN::scaler and HEL::scaler (0/1=false/true)");
        parser.addOption("-c","0","fix clock rollover while rebuilding scalers (0/1=false/true)");
        parser.addOption("-q","0","copy beam charge to REC::Event (0/1=false/true)");
        parser.addOption("-d","0","correct helicity delay in REC::Event (0/1=false/true)");
        parser.addOption("-i","0","invert helicity (0/1=false/true)");
        parser.addRequired("-o","output.hipo");
        parser.parse(args);

        Postprocess pp = new Postprocess();

        pp.rebuildScalers = parser.getOption("-r").intValue() == 1;
        pp.fixClockRollover = parser.getOption("-c").intValue() == 1;
        pp.correctHelicityDelay = parser.getOption("-d").intValue() == 1;
        pp.invertHelicity = parser.getOption("-i").intValue() == 1;

        pp.init(parser.getInputList());
        pp.process(parser.getOption("-o").stringValue());
    }

    static final String CCDB_FCUP_TABLE = "/runcontrol/fcup";
    static final String CCDB_SLM_TABLE = "/runcontrol/slm";
    static final String CCDB_HEL_TABLE = "/runcontrol/helicity";

    static final String SCHEMA_DIR = ClasUtilsFile.getResourceDir("COATJAVA","etc/bankdefs/hipo4");

    protected boolean fixClockRollover = false;
    protected boolean rebuildScalers = false;
    protected boolean invertHelicity = false;
    protected boolean correctHelicityDelay = false;

    List<String> inputFiles;
    HipoWriterSorted writer;
    HelicitySequenceManager helicitySequence;
    DaqScalersSequence scalerSequence;
    ConstantsManager constantsManager;
    SchemaFactory schemaFactory;
    Bank rawScaler;
    Bank runScaler;
    Bank helScaler;
    Bank runConfig;
    Bank helFlip;
    Bank recEvent;
    Bank coatConfig;
    IndexedTable fcupTable;
    IndexedTable slmTable;
    IndexedTable helTable;
    RCDBConstants rcdbConstants;
    int firstRunNumber;

    public Postprocess() {
        this.writer = new HipoWriterSorted();
        writer.getSchemaFactory().initFromDirectory(SCHEMA_DIR);
        writer.setCompressionType(2);
        schemaFactory = writer.getSchemaFactory();
        rawScaler = new Bank(schemaFactory.getSchema("RAW::scaler"));
        runScaler = new Bank(schemaFactory.getSchema("RUN::scaler"));
        helScaler = new Bank(schemaFactory.getSchema("HEL::scaler"));
        runConfig = new Bank(schemaFactory.getSchema("RUN::config"));
        helFlip = new Bank(schemaFactory.getSchema("HEL::flip"));
        recEvent = new Bank(schemaFactory.getSchema("REC::Event"));
        coatConfig = new Bank(schemaFactory.getSchema("COAT::config"));
        constantsManager = new ConstantsManager();
        constantsManager.init(Arrays.asList(new String[]{CCDB_FCUP_TABLE,CCDB_SLM_TABLE,CCDB_HEL_TABLE}));
        inputFiles = new ArrayList<>();
    }

    public void init(String... filenames) {
        init(Arrays.asList(filenames));
    }

    public void init(List<String> filenames) {
        inputFiles.addAll(filenames);
        firstRunNumber = getRunNumber();
        if (firstRunNumber<=0) {
            throw new RuntimeException("Found no valid run number.");
        }
        loadRunConstants(firstRunNumber);
        int delay = helTable.getIntValue("delay",0,0,0);
        helicitySequence = new HelicitySequenceManager(delay, inputFiles);
        if (rebuildScalers) {
            scalerSequence = DaqScalersSequence.readSequenceRaw(constantsManager, filenames);
            if (fixClockRollover) {
                scalerSequence.fixClockRollover(fcupTable, slmTable);
            }
        }
        else {
            scalerSequence = DaqScalersSequence.readSequence(filenames);
        }
    }

    public void loadRunConstants(int run) {
        if (run>0) {
            fcupTable = constantsManager.getConstants(run, CCDB_FCUP_TABLE);
            slmTable = constantsManager.getConstants(run, CCDB_SLM_TABLE);
            helTable = constantsManager.getConstants(run, CCDB_HEL_TABLE);
            rcdbConstants = constantsManager.getRcdbConstants(run);
        }
    }

    public void loadRunConstants(Event event) {
        loadRunConstants(getRunNumber(event));
    }

    public int getRunNumber(Event event) {
        event.read(runConfig);
        if (runConfig.getRows()>0) {
            return runConfig.getInt("run",0);
        }
        return -1;
    }

    public int getRunNumber() {
        Event event = new Event();
        for (int i=0; i<inputFiles.size(); i++) {
            HipoReader reader = new HipoReader();
            reader.setTags(1);
            reader.open(inputFiles.get(i));
            while (reader.hasNext()) {
               reader.nextEvent(event);
               int runNumber = getRunNumber(event);
               if (runNumber > 0) return runNumber;
            }
        }
        return -1;
    }

    public void rebuildScalersFromUnixTime(Event event) {
        Time rst = rcdbConstants.getTime("run_start_time");
        Date uet = new Date(runConfig.getInt("unixtime",0)*1000L);
        DaqScalers ds = DaqScalers.create(rawScaler, fcupTable, slmTable, helTable, rst, uet);
        runScaler = ds.createRunBank(writer.getSchemaFactory());
        helScaler = ds.createHelicityBank(writer.getSchemaFactory());
    }

    public void process(String outputFile) {

        writer.open(outputFile);

        long badCharge = 0;
        long goodCharge = 0;
        long badHelicity = 0;
        long goodHelicity = 0;

        Event event = new Event();
        Event configEvent = new Event();
        
        for (String inputFile : inputFiles) {

            HipoReader reader = new HipoReader();
            reader.open(inputFile);

            while (reader.hasNext()) {

                reader.nextEvent(event);
                event.read(recEvent);
                event.read(rawScaler);
                event.read(helFlip);
                event.read(helScaler);
                event.read(runScaler);

                final int runNumber = getRunNumber(event);
                if (runNumber > 0 && runNumber != firstRunNumber) {
                    throw new RuntimeException("Found multiple run numbers.");
                }

                // do the sequence lookups:
                DaqScalers scalers = scalerSequence.get(event);
                HelicityBit helicity = helicitySequence.search(event);
                HelicityBit helicityRaw = helicitySequence.getHalfWavePlate(event)
                    ? HelicityBit.getFlipped(helicity) : helicity;

                // count good/bad:
                if (Math.abs(helicity.value())==1) goodHelicity++; else badHelicity++;
                if (scalers!=null) goodCharge++; else badCharge++;

                if (rebuildScalers && rawScaler.getRows()>0) {
                    event.remove(runScaler.getSchema());
                    event.remove(helScaler.getSchema());
                    if (fixClockRollover) {
                        helScaler = scalerSequence.get(event).createHelicityBank(schemaFactory);
                        runScaler = scalerSequence.get(event).createRunBank(schemaFactory);
                    }
                    else {
                        rebuildScalersFromUnixTime(event);
                    }
                    RebuildScalers.assignScalerHelicity(event, helScaler, helicitySequence);
                    event.write(runScaler);
                    event.write(helScaler);
                }

                // invert this event's helicity:
                if (invertHelicity) {
                    helicity = HelicityBit.getFlipped(helicity);
                    helicityRaw = HelicityBit.getFlipped(helicityRaw);
                    if (helFlip.getRows()>0) {
                        event.remove(helFlip.getSchema());
                        helFlip.setByte("helicity", 0, (byte)-helFlip.getByte("helicity",0));
                        helFlip.setByte("helicityRaw", 0, (byte)-helFlip.getByte("helicityRaw",0));
                        event.write(helFlip);
                    }
                }

                // write delay-corrected helicty to REC::Event and HEL::scaler:
                if (correctHelicityDelay) {
                    recEvent.putByte("helicity",0,helicity.value());
                    recEvent.putByte("helicityRaw",0,helicityRaw.value());
                    if (helScaler.getRows() > 0) {
                        event.remove(helScaler.getSchema());
                        RebuildScalers.assignScalerHelicity(event, helScaler, helicitySequence);
                        event.write(helScaler);
                    }
                }

                // invert the non-delay-corrected helicity in place in REC::Event:
                else if (invertHelicity) {
                    recEvent.putByte("helicity",0,(byte)-recEvent.getByte("helicity",0));
                    recEvent.putByte("helicityRaw",0,(byte)-recEvent.getByte("helicityRaw",0));
                }

                if (scalers!=null) {
                    recEvent.putFloat("beamCharge",0, (float) scalers.dsc2.getBeamChargeGated());
                    recEvent.putDouble("liveTime",0,scalers.dsc2.getLivetime());
                }

                // copy config banks to new tag-1 events:
                configEvent.reset();
                event.read(coatConfig);
                if (coatConfig.getRows()>0) {
                    configEvent.write(coatConfig);
                    writer.addEvent(configEvent,1);
                }

                // update the output file:
                event.write(recEvent);
                writer.addEvent(event, event.getEventTag());
            }

            reader.close();
            
        }

        writer.close();

        System.out.println(String.format("Tag1ToEvent:  Good Helicity Fraction: %.2f%%",100*(float)goodHelicity/(goodHelicity+badHelicity)));
        System.out.println(String.format("Tag1ToEvent:  Good Charge   Fraction: %.2f%%",100*(float)goodCharge/(goodCharge+badCharge)));
    }
    
}
