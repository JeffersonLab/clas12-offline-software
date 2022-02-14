package org.jlab.analysis.postprocess;

import java.util.ArrayList;
import java.util.List;
import org.jlab.clas.reco.ReconstructionEngine;

import org.jlab.jnp.hipo4.data.Bank;
import org.jlab.jnp.hipo4.data.Event;

import org.jlab.jnp.hipo4.io.HipoReader;
import org.jlab.jnp.hipo4.io.HipoWriterSorted;

import org.jlab.utils.system.ClasUtilsFile;

import org.jlab.detector.scalers.DaqScalers;
import org.jlab.detector.scalers.DaqScalersSequence;

import org.jlab.detector.helicity.HelicityBit;
import org.jlab.detector.helicity.HelicitySequenceManager;

import org.jlab.utils.options.OptionParser;

/**
 * Calls routines to do analysis and per-event lookup of delayed helicity
 * and beam charge from tag-1 events, and outputs a file with modified
 * REC::Event.helicity/beamCharge/liveTime.
 * 
 * Also, adds tag-1 events for configuration banks (e.g. SOFT::config)
 * 
 * Usage: Tag1ToEvent outputFile inputFile1 [inputFile2 [inputFile3 [...]]]
 * 
 * FIXME:  DaqScalersSequence doesn't manage run numbers.  Until then, we
 * cannot mix run numbers here.
 * 
 * FIXME:  delay=8 is hardcoded below, should come from CCDB.  
 *
 * @author wphelps
 * @author baltzell
 */

public class Tag1ToEvent {

    public static final String[] CREATE_TAG1_EVENTS = {
        ReconstructionEngine.CONFIG_BANK_NAME
    };
    
    public static void main(String[] args) {

        OptionParser parser = new OptionParser("postprocess");
        parser.addOption("-q","0","do beam charge and livetime (0/1=false/true)");
        parser.addOption("-d","0","do delayed helicity (0/1=false/true)");
        parser.addOption("-f","0","do global offline helicity flip (0/1=false/true)");
        parser.addRequired("-o","output.hipo");
        parser.parse(args);

        // input files:
        List<String> inputList = parser.getInputList();
        if(inputList.isEmpty()==true){
            parser.printUsage();
            System.err.println("\n >>>> error : no input file is specified....\n");
            System.exit(1);
        }
        
        // output file:
        String fileout = parser.getOption("-o").stringValue();

        // helicity / beamcharge options:
        final boolean doHelicityDelay = parser.getOption("-d").intValue() != 0;
        final boolean doBeamCharge = parser.getOption("-q").intValue() != 0;
        final boolean doHelicityFlip = parser.getOption("-f").intValue() != 0;
        if (!doHelicityDelay && !doBeamCharge && !doHelicityFlip) {
            parser.printUsage();
            System.err.println("\n >>>>> error : at least one of -q/-d/-f must be specified\n");
            System.exit(1);
        }

        HelicitySequenceManager helSeq = new HelicitySequenceManager(8,inputList,doHelicityFlip);
        DaqScalersSequence chargeSeq = DaqScalersSequence.readSequence(inputList);

        HipoWriterSorted writer = new HipoWriterSorted();
        writer.getSchemaFactory().initFromDirectory(ClasUtilsFile.getResourceDir("COATJAVA", "etc/bankdefs/hipo4"));
        writer.setCompressionType(2);
        writer.open(fileout);
			
        Event event = new Event();

        Event configEvent = new Event();
        
        // we're going to modify this bank:
        Bank recEventBank = new Bank(writer.getSchemaFactory().getSchema("REC::Event"));
        
        // we're going to modify this bank if doHelicityFlip is set:
        Bank helFlipBank = new Bank(writer.getSchemaFactory().getSchema("HEL::flip"));

        // we're going to copy these banks to new tag-1 events:
        List<Bank> configBanks = new ArrayList<>();
        for (String bankName : CREATE_TAG1_EVENTS) {
            configBanks.add(new Bank(writer.getSchemaFactory().getSchema(bankName)));
        }
        
        long badCharge = 0;
        long goodCharge = 0;
        long badHelicity = 0;
        long goodHelicity = 0;

        for (String filename : inputList) {

            HipoReader reader = new HipoReader();
            reader.open(filename);

            while (reader.hasNext()) {

                reader.nextEvent(event);
                event.read(recEventBank);
                event.read(helFlipBank);

                event.remove(recEventBank.getSchema());

                if (doHelicityFlip && helFlipBank.getRows()>0) {
                    event.remove(helFlipBank.getSchema());
                    helFlipBank.setByte("helicity", 0, (byte)-helFlipBank.getByte("helicity",0));
                    helFlipBank.setByte("helicityRaw", 0, (byte)-helFlipBank.getByte("helicityRaw",0));
                    event.write(helFlipBank);
                }

                // do the lookups:
                HelicityBit hb = helSeq.search(event);
                DaqScalers ds = chargeSeq.get(event);

                // count helicity good/bad;
                if (Math.abs(hb.value())==1) goodHelicity++;
                else badHelicity++;

                // write delay-corrected helicty to REC::Event:
                if (doHelicityDelay) {
                    recEventBank.putByte("helicity",0,hb.value());
                }
                // flip the non-delay-corrected helicity in place in REC::Event:
                else if (doHelicityFlip) {
                    recEventBank.putByte("helicity",0,(byte)-recEventBank.getByte("helicity",0));
                    recEventBank.putByte("helicityRaw",0,(byte)-recEventBank.getByte("helicityRaw",0));
                }

                // write beam charge to REC::Event:
                if (ds==null) badCharge++;
                else {
                    goodCharge++;
                    if (doBeamCharge) {
                        recEventBank.putFloat("beamCharge",0, (float) ds.dsc2.getBeamChargeGated());
                        recEventBank.putDouble("liveTime",0,ds.dsc2.getLivetime());
                    }
                }

                // copy config banks to new tag-1 events:
                configEvent.reset();
                for (Bank bank : configBanks) {
                    event.read(bank);
                    if (bank.getRows()>0) {
                        configEvent.write(bank);
                    }
                }
                if (!configEvent.isEmpty()) {
                    writer.addEvent(configEvent,1);
                }

                // update the output file:
                event.write(recEventBank);
                writer.addEvent(event, event.getEventTag());
            }
            reader.close();
        }
        writer.close();

        System.out.println(String.format("Tag1ToEvent:  Good Helicity Fraction: %.2f%%",100*(float)goodHelicity/(goodHelicity+badHelicity)));
        System.out.println(String.format("Tag1ToEvent:  Good Charge   Fraction: %.2f%%",100*(float)goodCharge/(goodCharge+badCharge)));
    }
}
