package org.jlab.io.utils;

import org.jlab.coda.jevio.EventWriter;
import org.jlab.coda.jevio.EvioException;
import org.jlab.coda.jevio.EvioReader;
import org.jlab.utils.options.OptionParser;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author tylern4
 */
public class EvioFileUtils {
    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    public EvioFileUtils(){
    }

    public static void main(String[] args) {
        // Use option parser to get input and output file options
        OptionParser parser = new OptionParser("EvioFileUtils");
        parser.addRequired("-i","input.evio");
        parser.setRequiresInputList(false);
        parser.addRequired("-o","output.evio");
        parser.addOption("-n","100000","number of events per file");
        // Should be able to use the same util to merge or split evio
        parser.addOption("-s","true","Split evio files");
        parser.addOption("-m","false","Merge evio files");

        parser.parse(args);

        // TODO BROKEN:
        // Option parser hasOption method only checks if the option is present in the list not if it actually
        // has any data in it so it's essentially useless to check if the user actually puts a useful value....
        // This also makes it impossible to add a bool option which would be useful here.
        if(parser.hasOption("-s")) {
            /*
            TODO: Make option to use number of files instead of number of events
             and split the events as evenly as possible into the files
             */
            int numEvents = parser.getOption("-n").intValue();
            String inFile = parser.getOption("-i").stringValue();
            /*
            TODO: Would be nice to check output file template name to see if it already ends in evio and remove it
                change outTemplate.evio => outTemplate
                so that real output file outTemplate_0.evio instead of outTemplate.evio_0.evio
                Could also be nice to take outTemplate from input file
                so inputFile.evio => inputFile_0.evio as output file
             */
            String outFile = parser.getOption("-o").stringValue();
            splitFile(inFile,outFile,numEvents);
        } else if (parser.hasOption("-m")){
            LOGGER.severe("Merge Files not implemented yet.");
            // mergeFiles(inFile,outFile);
        } else {
            parser.printUsage();
        }

    }
    
    public static void mergeFiles(ArrayList<String> inputFiles, String outputFile){
        // TODO: Make merge evio files function
        LOGGER.severe("Merge Files not implemented yet.");
    }
    
    public static void splitFile(String inputFile, String outputTemplate, int nevents){
        // Start reading evio file at event 1
        int     eventNum = 1;
        // Start file number base to 0
        int     fileNum  = 0;

        // Create an evio writer and reader
        EventWriter evioWriter = null;
        EvioReader reader = null;
        try {
            // Read the input file gotten from option parser in main
            reader = new EvioReader(inputFile, false,false);
            // Save total number of events and print message showing that the process is starting
            int totalEvents = reader.getEventCount();
            LOGGER.info("==> Opening file with " + totalEvents + " events, splitting into " + (totalEvents/nevents) + " files with " + nevents + " events per file." );
            reader.rewind();

            // Go through file while the event number we are reading is less than the total number of events in the file
            while (eventNum < totalEvents) {
                // Open new evioWriter file outputTemplate_fileNum.evio to write to
                evioWriter = new EventWriter(new File(outputTemplate+"_" + String.format("%03d" , fileNum++) +".evio"), 8 * 1024,
                        1000, reader.getByteOrder(), null, null);

                // loop over the number of events we want per file
                for (int i = 0; i <= nevents; i++) {
                    // Get the event from the reader, starts at 1
                    ByteBuffer buffer = reader.getEventBuffer(eventNum);
                    buffer.order(reader.getByteOrder());
                    // Copy data current writer file
                    evioWriter.writeEvent(buffer);
                    // If we've hit the end of the file break out of for loop
                    if(eventNum++ == totalEvents) break;
                }
                // Close the current file, at the start of the next while loop a new file is opened with new file name
                evioWriter.close();
            }

            // Last check to make sure the reader and writer file are closed
            if(!reader.isClosed()) reader.close();
            if(!evioWriter.isClosed()) evioWriter.close();

            // Print when finished
            LOGGER.info("==> Finished: " + (eventNum-1) +" events in "+ fileNum + " files.");
            LOGGER.info("==> Outputs: " + outputTemplate+"_*.evio");

        } catch (EvioException ex) {
            LOGGER.log(Level.SEVERE, "Error (EVIO exception) at: " + eventNum, ex);
            ex.printStackTrace();
            evioWriter.close();
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Error (IO Exception) at: " + eventNum, ex);
            evioWriter.close();
        }

    }
}
