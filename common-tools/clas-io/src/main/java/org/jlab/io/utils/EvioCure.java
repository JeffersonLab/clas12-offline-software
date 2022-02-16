package org.jlab.io.utils;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jlab.coda.jevio.EventWriter;
import org.jlab.coda.jevio.EvioCompactStructureHandler;
import org.jlab.coda.jevio.EvioEvent;
import org.jlab.coda.jevio.EvioException;
import org.jlab.coda.jevio.EvioReader;
import org.jlab.io.evio.EvioDataSync;
import org.jlab.logging.DefaultLogger;

/**
 *
 * @author gavalian
 */
public class EvioCure {

    private static Logger LOGGER = Logger.getLogger(EvioCure.class.getName());

    public static void main(String[] args) {
        DefaultLogger.debug();

        String inputFile = args[0];
        String outputFile = args[1];

        // String inputFile =
        // "/Users/gavalian/Work/Software/project-3a.0.0/Distribution/clas-dis-rad.e10.600.emn0.75tmn0.05.0091.dat.evio";
        // String outputFile =
        // "/Users/gavalian/Work/Software/project-3a.0.0/Distribution/test-dis.hipo";
        int icounter = 0;
        EventWriter evioWriter = null;
        try {
            EvioReader reader = new EvioReader(inputFile, false, false);
            LOGGER.log(Level.INFO, " READER OPENED " + reader.getEventCount());
            String dictionary = "<xmlDict>\n" +
            // EvioDictionaryGenerator.createDAQDictionary(CLASDetectors)
                    "</xmlDict>\n";
            LOGGER.log(Level.INFO, " ENDIANNESS : " + reader.getByteOrder());
            evioWriter = new EventWriter(outputFile, false, reader.getByteOrder());
            // EvioWriter writer = new EvioDataSync();
            boolean isActive = true;
            reader.rewind();
            for (int i = 1; i < reader.getEventCount(); i++) {
                // EvioEvent event = reader.parseEvent(i);
                // EvioEvent event = reader.nextEvent();
                ByteBuffer buffer = reader.getEventBuffer(i);
                buffer.order(reader.getByteOrder());
                // ByteBuffer evioBuffer = ByteBuffer.wrap(event.getRawBytes());
                // evioBuffer.order(event.getByteOrder());
                evioWriter.writeEvent(buffer);
                icounter++;
            }
            /*
             * while(isActive==true){ EvioEvent event = reader.parseNextEvent();
             * if(event==null){ System.out.println("EVENT IS NULL"); break;
             * 
             * } evioWriter.writeEvent(event);
             * 
             * }
             */
            LOGGER.log(Level.INFO, " RECOVERED EVENT " + icounter);
            evioWriter.close();
        } catch (EvioException ex) {
            LOGGER.log(Level.WARNING, " RECOVERED EVENT (EVIO exception) " + icounter, ex);
            ex.printStackTrace();
            evioWriter.close();
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, " RECOVERED EVENT (IO Exception) " + icounter, ex);
            evioWriter.close();
        }
    }
}
