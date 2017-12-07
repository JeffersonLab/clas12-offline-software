/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jlab.io.evio;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jlab.coda.jevio.DataType;
import org.jlab.coda.jevio.EventBuilder;
import org.jlab.coda.jevio.EventWriter;
import org.jlab.coda.jevio.EvioBank;
//import org.jlab.coda.jevio.EvioCompactEventWriter;
import org.jlab.coda.jevio.EvioEvent;
import org.jlab.coda.jevio.EvioException;
import org.jlab.io.base.DataEvent;
import org.jlab.io.base.DataSync;

/**
 *
 * @author gavalian
 */
public class EvioDataSync implements DataSync {

	private String evioOutputDirectory = null;
	private String evioOutputFile = null;
	private Integer evioCurrentFileNumber = 0;
	// private Long maximumBytesToWrite = (long) 1*1024*1024*1024;
	private Long maximumBytesToWrite = (long) 1932735283;
	private Long maximumRecordsToWrite = (long) 2000000;
	private Long currentBytesWritten = (long) 0;
	private Long currentRecordsWritten = (long) 0;
	private Boolean splitFiles = true;
	private ByteOrder writerByteOrder = ByteOrder.LITTLE_ENDIAN;
	private EventWriter evioWriter = null;
	private String[] CLASDetectors = new String[] { "EC", "PCAL", "FTOF1A", "FTOF1B", "FTOF2", "BST", "CND", "HTCC", "FTCAL", "CTOF" };

	public EvioDataSync() {

	}

	public EvioDataSync(String filename) {
		this.open(filename);
	}

	public void setSplit(boolean flag) {
		this.splitFiles = flag;
	}

	public void open(String filename) {
		// this.openFileForWriting(filename);
		this.initFileNames(filename);
		this.openFileForWriting();
	}

	public void initFileNames(String filename) {
		Path filepath = Paths.get(filename);
		this.evioOutputFile = filepath.getFileName().toString();
		if (filepath.getParent() == null) {
			this.evioOutputDirectory = "";
		} else {
			this.evioOutputDirectory = filepath.getParent().toString();
		}
		/*
		 * int extensionIndex = this.evioOutputFile.lastIndexOf("."); if(extensionIndex>=0&&extensionIndex<this.evioOutputFile.length()){ this.evioOutputFile =
		 * this.evioOutputFile.substring(0, extensionIndex); }
		 */
		System.out.println("[EvioDataSync] ---> " + this.evioOutputDirectory);
		System.out.println("[EvioDataSync] ---> " + this.evioOutputFile);

	}

	private void openFileForWriting() {
		StringBuilder str = new StringBuilder();
		if (this.evioOutputDirectory.length() > 2) {
			str.append(this.evioOutputDirectory);
			str.append("/");
		}
		str.append(this.evioOutputFile);
		// str.append(".");
		// str.append(this.evioCurrentFileNumber);
		// str.append(".evio");
		String filename = str.toString();
		String dictionary = "<xmlDict>\n" +
		// EvioDictionaryGenerator.createDAQDictionary(CLASDetectors)
		        "</xmlDict>\n";
		System.out.println(dictionary);
		this.currentBytesWritten = (long) 0;
		this.currentRecordsWritten = (long) 0;
		File file = new File(filename);
		try {
			evioWriter =
			        new EventWriter(new File(filename), dictionary, true);
			// writerByteOrder, null, true);
			// new EventWriter(file, 1000000, 2,
			// ByteOrder.BIG_ENDIAN, null, null);
		} catch (EvioException ex) {
			Logger.getLogger(EvioDataSync.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public void writeEvent(DataEvent event) {

		if (this.currentBytesWritten > this.maximumBytesToWrite || this.currentRecordsWritten > this.maximumRecordsToWrite) {
			this.evioWriter.close();
			this.evioCurrentFileNumber++;
			this.openFileForWriting();
			System.out.println("open file # " + this.evioCurrentFileNumber);
		}

		try {
			// System.err.println("[sync] ---> buffer size = " + event.getEventBuffer().limit());
			ByteBuffer original = event.getEventBuffer();
			Long bufferSize = (long) original.capacity();
			this.currentBytesWritten += bufferSize;
			this.currentRecordsWritten++;
			ByteBuffer clone = ByteBuffer.allocate(original.capacity());
			clone.order(original.order());
			original.rewind();
			clone.put(original);
			original.rewind();
			clone.flip();
			evioWriter.writeEvent(clone);
			// event.getEventBuffer().flip();
		
                } catch (Exception e){
                    System.out.println("Something went wrong with writing");   
                }
	}

	public void openWithDictionary(String filename, String dictionary) {
		File file = new File(filename);
		try {
			evioWriter =
                                new EventWriter(new File(filename),  null, true);
			// new EventWriter(file, 1000000, 2,
			// ByteOrder.BIG_ENDIAN, null, null);
		} catch (EvioException ex) {
			Logger.getLogger(EvioDataSync.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public void close() {

		evioWriter.close();
	}

	public static void writeFileWithDictionary(String inputfile, String outputfile) {
		EvioSource reader = new EvioSource();
		reader.open(inputfile);
		EvioDataSync writer = new EvioDataSync();
		EvioDataDictionary dict = new EvioDataDictionary("CLAS12DIR", "lib/bankdefs/clas12new/");
		String dictString = dict.getXML();

	}

	public EvioDataEvent createEvent(EvioDataDictionary dict) {

		try {
			// EvioEvent baseBank = new EvioEvent(1, DataType.BANK, 0);
			EventBuilder builder = new EventBuilder(1, DataType.BANK, 0);
			EvioEvent event = builder.getEvent();
			EvioBank baseBank = new EvioBank(10, DataType.ALSOBANK, 0);

			builder.addChild(event, baseBank);

			ByteOrder byteOrder = writerByteOrder;

			int byteSize = event.getTotalBytes();
			// System.out.println("base bank size = " + byteSize);
			ByteBuffer bb = ByteBuffer.allocate(byteSize);
			bb.order(byteOrder);
			event.write(bb);
			bb.flip();

			return new EvioDataEvent(bb, dict);
		} catch (EvioException ex) {
			Logger.getLogger(EvioDataSync.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}

	public DataEvent createEvent() {

		try {
			// EvioEvent baseBank = new EvioEvent(1, DataType.BANK, 0);
			EventBuilder builder = new EventBuilder(1, DataType.BANK, 0);
			EvioEvent event = builder.getEvent();
			EvioBank baseBank = new EvioBank(10, DataType.ALSOBANK, 0);

			builder.addChild(event, baseBank);

			ByteOrder byteOrder = writerByteOrder;

			int byteSize = event.getTotalBytes();
			// System.out.println("base bank size = " + byteSize);
			ByteBuffer bb = ByteBuffer.allocate(byteSize);
			bb.order(byteOrder);
			event.write(bb);
			bb.flip();

			// return new EvioDataEvent(bb);
			return new EvioDataEvent(bb, EvioFactory.getDictionary());
		} catch (EvioException ex) {
			Logger.getLogger(EvioDataSync.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}

	public static void main(String[] args) {
		String inputfile = args[0];

	}
}
