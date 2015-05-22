package cnuphys.bCNU.et;

import java.io.File;
import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;

import org.jlab.coda.et.EtAttachment;
import org.jlab.coda.et.EtConstants;
import org.jlab.coda.et.EtEvent;
import org.jlab.coda.et.EtStation;
import org.jlab.coda.et.EtSystem;
import org.jlab.coda.et.EtSystemOpenConfig;
import org.jlab.coda.et.enums.Mode;
import org.jlab.coda.et.exception.EtClosedException;
import org.jlab.coda.et.exception.EtDeadException;
import org.jlab.coda.et.exception.EtException;
import org.jlab.coda.jevio.EvioEvent;
import org.jlab.coda.jevio.EvioException;
import org.jlab.coda.jevio.EvioReader;

import cnuphys.bCNU.util.Environment;

public class EvioFileProducer {

    // will skip events bigger than this
    public static final int MAXEVENTSIZE = 30000;

    private static long sleepTime = 1000;

    // defaults
    int group = 1;

    int chunk = 5;
    boolean verbose = false;
    boolean remote = true;

    // controls the thread
    private boolean _done;
    private Thread _reader;

    private int _eventIndex = 0;

    // the ET system
    private EtSystem _etSystem;

    // Grand Central
    private EtStation _etGrandCentral;

    // the ET attachment
    private EtAttachment _etAttachment;

    private File _evioFile;

    public EvioFileProducer(final File evioFile, String etName,
	    String hostAddress, int port) {

	_evioFile = evioFile;

	try {
	    // Make a direct connection to ET system's tcp server
	    EtSystemOpenConfig config = new EtSystemOpenConfig(etName,
		    hostAddress, port);
	    config.setConnectRemotely(remote);

	    // create ET system object with verbose debugging output
	    _etSystem = new EtSystem(config);
	    if (verbose)
		_etSystem.setDebug(EtConstants.debugInfo);
	    _etSystem.open();

	    // get GRAND_CENTRAL station object
	    _etGrandCentral = _etSystem.stationNameToObject("GRAND_CENTRAL");

	    // attach to GRAND_CENTRAL
	    _etAttachment = _etSystem.attach(_etGrandCentral);

	    // create control array of correct size
	    final int[] con = new int[EtConstants.stationSelectInts];
	    for (int i = 0; i < EtConstants.stationSelectInts; i++) {
		con[i] = i + 1;
	    }

	    // keep track of time for event rate calculations
	    // t1 = System.currentTimeMillis();

	    // a thread to read the events from the file and put them into ET
	    final Runnable eReader = new Runnable() {

		@Override
		public void run() {
		    EvioReader reader = null;
		    try {
			reader = new EvioReader(_evioFile);
		    } catch (Exception e) {
			System.err.println("Exception in openEventFile "
				+ e.getMessage());
			return;
		    }

		    _done = false;
		    EvioEvent events[] = new EvioEvent[chunk];
		    while (!_done) {
			try {
			    int count = getEventChunk(reader, events);
			    if (count > 0) {

				// now get the et events
				EtEvent[] mevs = _etSystem.newEvents(
					_etAttachment, Mode.SLEEP, false, 0,
					count, MAXEVENTSIZE, group);

				for (int i = 0; i < mevs.length; i++) {

				    if (_done) {
					break;
				    }

				    final EvioEvent event = events[i];

				    if ((event.getEventNumber() % 100) == 0) {
					System.err.println("event number: "
						+ event.getEventNumber()
						+ "  LEN: "
						+ event.getTotalBytes());
				    }

				    ByteBuffer buffer = mevs[i].getDataBuffer();

				    try {
					int numwrit = event.write(buffer);
					mevs[i].setLength(numwrit);

					// set event's control array
					mevs[i].setControl(con);
				    } catch (BufferOverflowException boe) {
					boe.printStackTrace();
				    }

				} // end loop mevs.length

				if (!_done) {
				    _etSystem.putEvents(_etAttachment, mevs);

				    try {
					Thread.sleep(sleepTime);
				    } catch (InterruptedException e) {
				    }
				}
			    } // end count > 0
			    if (count < chunk) {
				_done = true;
			    }

			} catch (Exception e) {
			    e.printStackTrace();
			}

		    } // while !done

		} // end run

	    }; // end eReader def

	    _reader = new Thread(eReader);
	    _reader.start();

	} catch (Exception e) {
	    e.printStackTrace();
	}

    }

    /**
     * Shutdown the producer
     */
    public void shutDown() {
	_done = true;

	if (_etSystem != null) {
	    if (_etAttachment != null) {
		try {
		    _etSystem.detach(_etAttachment);
		} catch (IOException e) {
		    e.printStackTrace();
		} catch (EtDeadException e) {
		    e.printStackTrace();
		} catch (EtClosedException e) {
		    e.printStackTrace();
		} catch (EtException e) {
		    e.printStackTrace();
		}

		_etAttachment = null;
	    }
	    _etSystem.close();
	}
    }

    /**
     * Read a chunk of events
     * 
     * @param reader
     *            the event reader
     * @param events
     *            the array to store the chunk, may be nulls at the end if we
     *            reach EOF
     * @return the number of good events should be length of events array except
     *         at eof
     */
    private int getEventChunk(EvioReader reader, EvioEvent events[]) {
	int len = events.length;
	for (int i = 0; i < len; i++) {
	    events[i] = null;
	}

	int count = 0;
	for (int i = 0; i < len; i++) {
	    try {
		if (_done) {
		    return 0;
		}
		EvioEvent event = reader.parseEvent(++_eventIndex);

		// if event = null, reopen. This is a perpetual producer
		if (event == null) {
		    try {
			reader = new EvioReader(_evioFile);
			_eventIndex = 0;
			event = reader.parseEvent(++_eventIndex);
		    } catch (Exception e) {
			System.err.println("Exception in openEventFile "
				+ e.getMessage());
		    }

		}

		if (event != null) {
		    int size = event.getTotalBytes();
		    if (size < MAXEVENTSIZE) {
			events[i] = event;
			count++;
		    } else {
			System.err.println("Too big event encountered: " + size
				+ " bytes");
			i--;
		    }
		} else {
		    break;
		}
	    } catch (IOException e) {
		e.printStackTrace();
	    } catch (EvioException e) {
		e.printStackTrace();
	    }
	}

	return count;
    }

    // main program for testing
    public static void main(String arg[]) {
	int numEvents = 100;

	System.err.println("cwd: "
		+ Environment.getInstance().getCurrentWorkingDirectory());
	String evioFileName = "../../evioData/etFileSource.ev";
	File evioFile = new File(evioFileName);
	if (!evioFile.exists()) {
	    System.err.println("Evio file does not exist: "
		    + evioFile.getPath());
	    System.exit(1);
	}

	StartET startET = new StartET(MAXEVENTSIZE, numEvents);

	EvioFileProducer evioProducer = new EvioFileProducer(evioFile,
		startET.getETName(), startET.getHostAddress(),
		startET.getServerPort());

	System.err.println("******  Create a consumer");
	ETTestConsumer consumer = new ETTestConsumer(startET.getETName(),
		startET.getHostAddress(), startET.getServerPort());
    }
}