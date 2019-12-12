package org.jlab.analysis.postprocess;

import java.util.ArrayList;
import java.util.List;

import org.jlab.jnp.hipo4.data.Bank;
import org.jlab.jnp.hipo4.data.Event;

import org.jlab.jnp.hipo4.io.HipoReader;
import org.jlab.jnp.hipo4.io.HipoWriterSorted;

import org.jlab.utils.system.ClasUtilsFile;

import org.jlab.detector.decode.DaqScalers;
import org.jlab.detector.decode.DaqScalersSequence;

import org.jlab.detector.helicity.HelicityBit;
import org.jlab.detector.helicity.HelicitySequenceManager;

import org.jlab.utils.options.OptionParser;

/**
 * Calls routines to do analysis and per-event lookup of delayed helicity
 * and beam charge from tag-1 events, and outputs a file with modified
 * REC::Event.helicity/beamCharge/liveTime.
 * 
 * Usage: Tag1ToEvent outputFile inputFile1 [inputFile2 [inputFile3 [...]]]
 * 
 * FIXME:  DaqScalersSequence doesn't manage run numbers.  Until then, we
 * cannot mix run numbers here.
 *
 * @author wphelps
 * @author baltzell
 */

public class Tag1ToEvent {

    public static void main(String[] args) {

        OptionParser parser = new OptionParser("postprocess");
        parser.addOption("-q","0","do beam charge and livetime (0/1=false/true)");
        parser.addOption("-d","0","do delayed helicity (0/1=false/true)");
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
        final boolean doHelicity = parser.getOption("-d").intValue() != 0;
        final boolean doBeamCharge = parser.getOption("-q").intValue() != 0;
        if (!doHelicity && !doBeamCharge) {
            parser.printUsage();
            System.err.println("\n >>>>> error : at least one of -q/-d must be specified\n");
            System.exit(1);
        }

        HelicitySequenceManager helSeq = new HelicitySequenceManager(8,inputList);
        DaqScalersSequence chargeSeq = DaqScalersSequence.readSequence(inputList);

        HipoWriterSorted writer = new HipoWriterSorted();
        writer.getSchemaFactory().initFromDirectory(ClasUtilsFile.getResourceDir("COATJAVA", "etc/bankdefs/hipo4"));
        writer.setCompressionType(1);
        writer.open(fileout);
			
        Event event = new Event();

        // we're going to modify this bank:
        Bank recEventBank = new Bank(writer.getSchemaFactory().getSchema("REC::Event"));

        // FIXME: we shouldn't need this bank, but just the event:
        Bank runConfigBank = new Bank(writer.getSchemaFactory().getSchema("RUN::config"));

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
                event.remove(recEventBank.getSchema());

                // FIXME:  we shouldn't need this bank, but just the event:
                event.read(runConfigBank);
                final long timestamp = runConfigBank.getLong("timestamp", 0);

                // do the lookups:
                HelicityBit hb = helSeq.search(event);
                DaqScalers ds = chargeSeq.get(timestamp);

                // write heliicty to REC::Event:
                if (Math.abs(hb.value())==1) goodHelicity++;
                else badHelicity++;
                if (doHelicity) {
                    recEventBank.putByte("helicity",0,hb.value());
                }

                // write beam charge to REC::Event:
                if (ds==null) badCharge++;
                else {
                    goodCharge++;
                    if (doBeamCharge) {
                        recEventBank.putFloat("beamCharge",0,ds.getBeamCharge());
                        recEventBank.putDouble("liveTime",0,ds.getLivetime());
                    }
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
