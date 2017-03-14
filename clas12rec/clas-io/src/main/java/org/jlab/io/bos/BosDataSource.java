/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jlab.io.bos;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jlab.io.base.DataEvent;
import org.jlab.io.base.DataEventList;
import org.jlab.io.base.DataSource;
import org.jlab.io.base.DataSourceType;

/**
 *
 * @author gavalian
 */
public class BosDataSource implements DataSource {

	private String bosFileName = "undef";
	private static final int MAXIMUM_BYTE_READ = 7000;
	private int currentBufferPosition = -1;
	private int currentEventInBuffer = 0;
	private int currentBufferEventPosition = -1;
	private int nextBufferEventPosition = -1;
	private ByteBuffer ioFileBuffer;
	private BufferedInputStream buffInputStream = null;
	private BosDataDictionary dictionary = new BosDataDictionary();
	private ArrayList<Integer> eventIndex = new ArrayList<Integer>();
	private Boolean isLastBufferRead = true;

	public BosDataSource() {
		dictionary.init("*");
		dictionary.getDescriptor("EVNT").show();
		ioFileBuffer = ByteBuffer.allocate(0);
	}

	public void open(File file) {

	}

	public void open(String filename) {
		try {
			bosFileName = filename;
			File inFile = new File(bosFileName);
			buffInputStream = new BufferedInputStream(new FileInputStream(inFile));
			currentEventInBuffer = -1;
			isLastBufferRead = false;
		} catch (FileNotFoundException ex) {
			Logger.getLogger(BosDataSource.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public void open(ByteBuffer buff) {
		throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools | Templates.
	}

	public void close() {
		// throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	public int getSize() {
		throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools | Templates.
	}

	public DataEventList getEventList(int start, int stop) {
		throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools | Templates.
	}

	public DataEventList getEventList(int nrecords) {
		throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools | Templates.
	}

	public void dumpBufferToFile(String filename) {
		System.out.println("DUMPING EVENT # " + this.currentEventInBuffer + "  BUFF " + this.currentBufferPosition);
		try {
			boolean append = false;
			FileChannel wChannel = new FileOutputStream(new File(filename), append).getChannel();
			wChannel.write(this.ioFileBuffer);
			wChannel.close();
		} catch (FileNotFoundException ex) {
			Logger.getLogger(BosDataSource.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IOException ex) {
			Logger.getLogger(BosDataSource.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	private int findStructure(String struct, int startpos) {
		byte[] bufferBytes = ioFileBuffer.array();
		byte[] nameBytes = struct.getBytes();
		for (int loop = startpos; loop < bufferBytes.length; loop++) {
			if (bufferBytes[loop] == nameBytes[0] && loop < bufferBytes.length - 12) {
				String header = new String(bufferBytes, loop, struct.length());
				// System.out.println("[StrcutureFinder] ----> Found R at pos = "
				// + loop + " header = " + header);
				if (header.compareTo(struct) == 0) {
					// System.out.println("[StrcutureFinder] ----> Found events at pos = "
					// + loop);
					return loop;
				}
			}
		}
		return -1;
	}

	private void printIndexArray() {
		System.err.print("--> EVENT INDEX : ");
		for (Integer i : eventIndex) {
			System.out.print(" " + i);
		}
		System.err.println();
		System.err.print("--> EVENT SIZE : ");
		for (int loop = 0; loop < eventIndex.size() - 1; loop++) {
			System.out.print(" " + (eventIndex.get(loop + 1) - eventIndex.get(loop)));
		}
		System.err.println();
	}

	private void readAppendEvent(int last_event_start) {

		// System.out.println(" REDING EVENT BUFFER ----> CURRENT EVENT " + this.currentEventInBuffer);
		byte[] a = Arrays.copyOfRange(ioFileBuffer.array(), last_event_start, ioFileBuffer.array().length);
		byte[] b = new byte[MAXIMUM_BYTE_READ];
		try {
			int bytesRead = buffInputStream.read(b);
			if (bytesRead < MAXIMUM_BYTE_READ) {
				System.err.println("********** BOSIO FILE LAST BUFFER READ ********");
				isLastBufferRead = true;
			}
			byte[] c = new byte[a.length + b.length];
			System.arraycopy(a, 0, c, 0, a.length);
			System.arraycopy(b, 0, c, a.length, b.length);
			/*
			 * System.out.println("-----> array copy " + " a = " + a.length + " b = " + b.length + " c = " + c.length);
			 */
			ioFileBuffer = ByteBuffer.wrap(c);
			// ioFileBuffer = ByteBuffer.wrap(b);
			// System.out.println("----> result buffer = " + ioFileBuffer.array().length);
			this.updateEventIndex();
			currentEventInBuffer = 0;
		} catch (IOException ex) {
			Logger.getLogger(BosDataSource.class.getName()).log(Level.SEVERE, null, ex);
		}

	}

	private int findNextPosition(int start) {
		int start_position = start;
		int position = this.findStructure("RUNEVENT", start_position);
		int head_position = this.findStructure("HEAD", start_position + 8);
		int epic_position = this.findStructure("EPIC", start_position + 8);
		if (epic_position >= 0) {
			if (head_position >= 0) {
				if (epic_position < head_position) {
					return (epic_position - position);
				}
			}
		}
		// System.out.println("POSITION DIFFERENCE = " + position + " " + head_position);
		return (head_position - position);
	}

	public void showIndex() {
		System.out.println(" INDEX ARRAY SIZE = " + this.eventIndex.size());
		int counter = 1;
		for (Integer index : this.eventIndex) {
			System.out.print(String.format("%8d", index));
			if (counter % 5 == 0)
				System.out.println();
			counter++;
		}
	}

	private void updateEventIndex() {
		ArrayList<Integer> crudeIndex = new ArrayList<Integer>();
		eventIndex.clear();

		this.findNextPosition(0);
		int start_position = 0;
		int nextPosition = this.findStructure("RUNEVENT", start_position);
		// System.out.println("------> first position = " + nextPosition);
		// eventIndex.add(nextPosition);
		crudeIndex.add(nextPosition);
		while (nextPosition >= 0) {
			start_position = nextPosition + 8;
			nextPosition = this.findStructure("RUNEVENT", start_position);
			// System.out.println("------> adding position " + nextPosition);
			if (nextPosition > 0) {
				crudeIndex.add(nextPosition);
				// eventIndex.add(nextPosition);
			}
		}

		for (Integer index : crudeIndex) {
			if (this.findNextPosition(index) < 40) {
				eventIndex.add(index);
			}
		}
		// this.showIndex();
		// System.err.println("[BosDataSource]-----> read buffer contains " +
		// eventIndex.size() + " events");
		// this.printIndexArray();
	}

	public DataEvent getNextEvent() {

		if (this.hasEvent() == false) {
			byte[] emptybuff = new byte[4];
			ByteBuffer iobcs = ByteBuffer.wrap(emptybuff);
			iobcs.order(ByteOrder.LITTLE_ENDIAN);
			return new BosDataEvent(iobcs, dictionary);
		}

		if (currentEventInBuffer < 0) {
			try {
				byte[] result = new byte[MAXIMUM_BYTE_READ];
				int bytesRead = buffInputStream.read(result);
				System.err.println("[BosDataSource]----> Read buffer bytes read = " + bytesRead);
				ioFileBuffer = ByteBuffer.wrap(result);
				ioFileBuffer.order(ByteOrder.LITTLE_ENDIAN);
				currentEventInBuffer = 0;
				this.updateEventIndex();
			} catch (IOException ex) {
				System.err.println("[BosDataSource]----> ERROR while reading the file...");
			}
		}
		/*
		 * THIS PART WAS MODIFIED ON MAY/13/2015 before it was: currentEventInBuffer==eventIndex.size()-1 Will check later if this makes sense.
		 */
		if (currentEventInBuffer == eventIndex.size() - 2) {
			/*
			 * System.out.println("[BosDataSource]----> current size " + ioFileBuffer.array().length + "  " + eventIndex.get(eventIndex.size()-2));
			 * System.out.println("[BosDataSource]----> READING new BUFFER " + " remaining size = " + (ioFileBuffer.array().length-
			 * eventIndex.get(eventIndex.size()-2)));
			 */
			this.readAppendEvent(eventIndex.get(eventIndex.size() - 2));
		}

		// System.err.println(" current event = " + currentEventInBuffer + " index size = "
		// + eventIndex.size());
		// THIS WAS CHANGED from if(currentEventInBuffer>=0&&currentEventInBuffer<eventIndex.size()-2){
		if (currentEventInBuffer >= 0 && currentEventInBuffer < eventIndex.size() - 2) {
			// System.err.println("[BosDataSource]----> creating an event start pos = "
			// + eventIndex.get(currentEventInBuffer) + " end pos = "
			// + eventIndex.get(currentEventInBuffer+1));
			byte[] evt = ioFileBuffer.array();
			byte[] bcs = Arrays.copyOfRange(evt, eventIndex.get(currentEventInBuffer), eventIndex.get(currentEventInBuffer + 1) + 8);

			ByteBuffer bcsBOS = ByteBuffer.wrap(bcs);
			bcsBOS.order(ByteOrder.LITTLE_ENDIAN);

			currentEventInBuffer++;
			return new BosDataEvent(bcsBOS, dictionary);
		}
		/*
		 * try { byte[] result = new byte[MAXIMUM_BYTE_READ]; int bytesRead = buffInputStream.read(result);
		 * 
		 * ByteBuffer bosBytes = ByteBuffer.wrap(result); bosBytes.order(ByteOrder.LITTLE_ENDIAN); return new BosDataEvent(bosBytes,dictionary); } catch (IOException
		 * ex) { Logger.getLogger(BosDataSource.class.getName()).log(Level.SEVERE, null, ex); }
		 */
		return null;
	}

	public void reset() {
		throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools | Templates.
	}

	public int getCurrentIndex() {
		throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools | Templates.
	}

	public boolean hasEvent() {
		if (currentEventInBuffer >= eventIndex.size() - 2 && isLastBufferRead == true) {
			return false;
		}
		return true;
	}

	public DataEvent getPreviousEvent() {
		throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools | Templates.
	}

	public DataEvent gotoEvent(int index) {
		throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools | Templates.
	}

    @Override
    public DataSourceType getType() {
        return DataSourceType.FILE;
    }

    @Override
    public void waitForEvents() {
        
    }

}
