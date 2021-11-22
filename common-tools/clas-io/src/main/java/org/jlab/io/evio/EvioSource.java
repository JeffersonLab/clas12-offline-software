package org.jlab.io.evio;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jlab.coda.jevio.EvioCompactReader;
import org.jlab.coda.jevio.EvioException;
import org.jlab.io.base.DataEvent;
import org.jlab.io.base.DataEventList;
import org.jlab.io.base.DataSource;
import org.jlab.io.base.DataSourceType;

public class EvioSource implements DataSource {

	Logger LOGGER = Logger.getLogger(EvioSource.class.getName());
	private ByteOrder storeByteOrder = ByteOrder.BIG_ENDIAN;
	private EvioCompactReader evioReader = null;
	private EvioDataEvent evioEvent = null;
	private int currentEvent;
	private int currentFileEntries;
	private EvioDataDictionary dictionary = new EvioDataDictionary();
	private String dictionaryPath = "some";

	public EvioSource() {

		String CLAS12DIR = System.getenv("CLAS12DIR");
		String CLAS12DIRPROP = System.getProperty("CLAS12DIR");

		if (CLAS12DIR == null) {
			LOGGER.log(Level.WARNING,"---> Warning the CLAS12DIR environment is not defined.");
			// return;
		} else {
			dictionaryPath = CLAS12DIR + "/etc/bankdefs/clas12";
		}

		if (CLAS12DIRPROP == null) {
			LOGGER.log(Level.WARNING,"---> Warning the CLAS12DIR property is not defined.");
		} else {
			dictionaryPath = CLAS12DIRPROP + "/etc/bankdefs/clas12";
		}

		if (CLAS12DIRPROP == null && CLAS12DIR == null) {
			return;
		}
		// dictionary.initWithDir(dictionaryPath);
		// System.err.println("[EvioSource] ---> Loaded bank Descriptors from : " +
		// dictionaryPath);
		// System.err.println("[EvioSource] ---> Factory loaded descriptor count : "
		// + dictionary.getDescriptorList().length);

		EvioFactory.loadDictionary(dictionaryPath);
		dictionary = EvioFactory.getDictionary();
		LOGGER.log(Level.INFO,"[EvioSource] ---> Factory loaded descriptor count : " + dictionary.getDescriptorList().length);
		// dictionary.show();
	}

	public EvioSource(String filename) {
		String CLAS12DIR = System.getenv("CLAS12DIR");
		String CLAS12DIRPROP = System.getProperty("CLAS12DIR");

		if (CLAS12DIR == null) {
			LOGGER.log(Level.WARNING,"---> Warning the CLAS12DIR environment is not defined.");
			// return;
		} else {
			dictionaryPath = CLAS12DIR + "/etc/bankdefs/clas12";
		}

		if (CLAS12DIRPROP == null) {
			LOGGER.log(Level.WARNING,"---> Warning the CLAS12DIR property is not defined.");
		} else {
			dictionaryPath = CLAS12DIRPROP + "/etc/bankdefs/clas12";
		}

		if (CLAS12DIRPROP == null && CLAS12DIR == null) {
			return;
		}
		// dictionary.initWithDir(dictionaryPath);
		// System.err.println("[EvioSource] ---> Loaded bank Descriptors from : " +
		// dictionaryPath);
		// System.err.println("[EvioSource] ---> Factory loaded descriptor count : "
		// + dictionary.getDescriptorList().length);

		EvioFactory.loadDictionary(dictionaryPath);
		dictionary = EvioFactory.getDictionary();
		LOGGER.log(Level.INFO,"[EvioSource] ---> Factory loaded descriptor count : " + dictionary.getDescriptorList().length);
		dictionary.show();
		this.open(filename);
	}

	public void open(File file) {
		this.open(file.getAbsolutePath());
	}

	public void open(String filename) {
		try {
			evioReader = new EvioCompactReader(new File(filename));
			currentEvent = 1;
			currentFileEntries = evioReader.getEventCount();
			storeByteOrder = evioReader.getFileByteOrder();
			LOGGER.log(Level.INFO,"****** opened FILE [] ** NEVENTS = " + currentFileEntries + " *******");
			// TODO Auto-generated method stub
		} catch (EvioException ex) {
			Logger.getLogger(EvioSource.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IOException ex) {
			Logger.getLogger(EvioSource.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public void open(ByteBuffer buff) {
		try {
			evioReader = new EvioCompactReader(buff);
			currentEvent = 1;
			currentFileEntries = evioReader.getEventCount()+1;
			storeByteOrder = evioReader.getFileByteOrder();
			// LOGGER.log(Level.INFO,"****** opened BUFFER [] ** NEVENTS = " + currentFileEntries + " *******");
		} catch (EvioException ex) {
			Logger.getLogger(EvioSource.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public void close() {
		// TODO Auto-generated method stub

	}

	public int getSize() {
		// TODO Auto-generated method stub
		return currentFileEntries;
	}

	public DataEventList getEventList(int start, int stop) {
		// TODO Auto-generated method stub
		return null;
	}

	public DataEventList getEventList(int nrecords) {
		// TODO Auto-generated method stub
		return null;
	}

	public void reset() {
		currentEvent = 1;
	}

	public int getCurrentIndex() {
		// TODO Auto-generated method stub
		return currentEvent;
	}

	public DataEvent getPreviousEvent() {
		if (currentEvent > currentFileEntries || currentEvent == 2)
			return null;
		try {
			currentEvent--;
			currentEvent--;
			ByteBuffer evioBuffer = evioReader.getEventBuffer(currentEvent, true);
			EvioDataEvent event = new EvioDataEvent(evioBuffer.array(), storeByteOrder, dictionary);
			currentEvent++;
			return event;
		} catch (EvioException ex) {
			Logger.getLogger(EvioSource.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}

	public DataEvent gotoEvent(int index) {
		if (index <= 1 || index > currentFileEntries)
			return null;
		try {
			ByteBuffer evioBuffer = evioReader.getEventBuffer(index, true);
			EvioDataEvent event = new EvioDataEvent(evioBuffer.array(), storeByteOrder, dictionary);
			currentEvent = index + 1;
			return event;
		} catch (EvioException ex) {
			Logger.getLogger(EvioSource.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}

	public EvioDataEventHandler getNextEventHandler() {
		if (currentEvent > currentFileEntries)
			return null;
		try {
			ByteBuffer evioBuffer = evioReader.getEventBuffer(currentEvent, true);
			EvioDataEventHandler event = new EvioDataEventHandler(evioBuffer.array(), storeByteOrder);
			currentEvent++;
			return event;
		} catch (EvioException ex) {
			Logger.getLogger(EvioSource.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}

	public DataEvent getNextEvent() {
		if (currentEvent > currentFileEntries)
			return null;
		try {
			ByteBuffer evioBuffer = evioReader.getEventBuffer(currentEvent, true);
			EvioDataEvent event = new EvioDataEvent(evioBuffer.array(), storeByteOrder, dictionary);
			currentEvent++;
			return event;
		} catch (EvioException ex) {
			Logger.getLogger(EvioSource.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}
	/*
	 * public Histogram1D scanTree(String path, int maxevents){ currentEvent = 1; int maxestimate = 5000; if(maxestimate>=maxevents){ maxestimate = maxevents-2; }
	 * 
	 * DataRangeEstimator datae = new DataRangeEstimator(path,100,maxestimate);
	 * 
	 * for(int loop = 0; loop < maxevents; loop++){ DataEvent event = this.getNextEvent(); double[] darray = event.getDouble(path); if(darray!=null){ for(int ndata =
	 * 0; ndata < darray.length; ndata++){ datae.fill(darray[ndata]); //System.out.println("--> " + darray[ndata]); } } } //= new Histogram1D();
	 * System.out.println("----> bins  = " + datae.getHistogram().getAxis().getNbins()); return datae.getHistogram(); }
	 */

	public boolean hasEvent() {
		if (currentEvent >= currentFileEntries)
			return false;
		return true;
	}

	public static void main(String[] args) {

	}

    @Override
    public DataSourceType getType() {
        return DataSourceType.FILE;
    }

    @Override
    public void waitForEvents() {
        // This method does nothing for source associated with
        // a file.
    }
}
