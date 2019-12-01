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
 * FIXME:  Use standard command-line interpreter, with decent usage help.
 * 
 * @author wphelps
 * @author baltzell
 */

public class Tag1ToEvent {

	public static void main(String[] args) {

        // first argument is output filename:
		String fileout = args[0];

        // all other arguments are input filenames:
		List<String> filenames = new ArrayList<>();
		for (int i = 1; i < args.length; i++)
			filenames.add(args[i]);

        HelicitySequenceManager helSeq = new HelicitySequenceManager(8,filenames);
		DaqScalersSequence chargeSeq = DaqScalersSequence.readSequence(filenames);

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

		for (String filename : filenames) {

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
                recEventBank.putByte("helicity",0,hb.value());
               
                // write beam charge to REC::Event:
                if (ds==null) badCharge++;
                else {
                    goodCharge++;
                    recEventBank.putFloat("beamCharge",0,ds.getBeamCharge());
                    recEventBank.putDouble("liveTime",0,ds.getLivetime());
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
