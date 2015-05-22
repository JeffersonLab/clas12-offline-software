package cnuphys.bCNU.event;

import java.io.IOException;
import java.nio.ByteBuffer;
import javax.swing.event.EventListenerList;

import cnuphys.bCNU.et.ETSupport;
import cnuphys.bCNU.magneticfield.swim.ISwimAll;
import cnuphys.swim.Swimming;

import org.jlab.coda.jevio.BaseStructure;
import org.jlab.coda.jevio.EventParser;
import org.jlab.coda.jevio.EvioEvent;
import org.jlab.coda.jevio.EvioException;
import org.jlab.coda.jevio.EvioReader;
import org.jlab.coda.jevio.IEvioListener;
import org.jlab.coda.jevio.IEvioStructure;

/**
 * This class gets the events from jevio and prepares them for use by the views.
 * It doesn't know about specific event formats. This does nothing--because this
 * is a generic class. For ced, the ced code also has a class that listens for
 * structures (an IEvioListener) which then deals with CLAS specifics.What this
 * class does is handle the accumulation of events, and notifies listeners that
 * new events have arrived or new accumulations are ready.
 * 
 * @author heddle
 * 
 */
public class EventControl implements IEvioListener {

    // sources of events (the type, not the actual source)
    public enum EventSourceType {
	FILE, ET
    }

    // singleton
    private static EventControl instance;

    // flag that set set to <code>true</code> if we are accumulating events
    private boolean _accumulating = false;

    // list of listeners
    private EventListenerList _viewListenerList;

    // someone who can swim all particles
    private ISwimAll _allSwimmer;

    // Another list used for lsteners who MUST be notified BEFORE the views.
    // This might be a noise reduction
    // algorithm, so that the views are sure to have the results available. Or a
    // trajectory swimmer for the same
    // reason. This is a bit of a hack, but at the moment I don't have a better
    // solution. Among "first notifiers"
    private EventListenerList _beforeViewListenersList;

    // static event parser. Used a singleton in evio1
    private static EventParser _evioParser = new EventParser();

    // 4.0 replacement for EvioFile
    private static EvioReader _evioReader;

    // the current event
    public static EvioEvent _currentEvent = null;

    /**
     * Private constructor for singleton
     */
    private EventControl() {
	// listen for events from jevio
	_evioParser.addEvioListener(this);
    }

    /**
     * Get the number of the current event, -1 if there is none
     * 
     * @return the number of the current event.
     */
    public static int getEventNumber() {
	return (_currentEvent == null) ? -1 : _currentEvent.getEventNumber();
    }

    /**
     * Determines whether any next event control should be enabled.
     * 
     * @return <code>true</code> if any next event control should be enabled.
     */
    public static boolean isNextOK() {
	boolean haveReader = (getEvioReader() != null);

	return (isSourceFile() && haveReader)
		|| (isSourceET() && ETSupport.isReady());
    }

    /**
     * Determines whether any prev event control should be enabled.
     * 
     * @return <code>true</code> if any prev event control should be enabled.
     */
    public static boolean isPrevOK() {
	boolean haveReader = (getEvioReader() != null);
	int evNum = getEventNumber();

	return isSourceFile() && haveReader && (evNum > 1);
    }

    /**
     * Check whether current event source type is a file
     * 
     * @return <code>true</code> is source type is a file.
     */
    public static boolean isSourceFile() {
	return getEventSourceType() == EventSourceType.FILE;
    }

    /**
     * Check whether current event source type is ET
     * 
     * @return <code>true</code> is source type is ET.
     */
    public static boolean isSourceET() {
	return getEventSourceType() == EventSourceType.ET;
    }

    /**
     * Get the object that can swim all MonteCarlo particles
     * 
     * @return the object that can swim all MonteCarlo particles
     */
    public ISwimAll getAllSwimmer() {
	return _allSwimmer;
    }

    /**
     * Set the object that can swim all MonteCarlo particles
     * 
     * @param allSwimmer
     *            the object that can swim all MonteCarlo particles
     */
    public void setAllSwimmer(ISwimAll allSwimmer) {
	_allSwimmer = allSwimmer;
    }

    /**
     * Public access to the event control singleton.
     * 
     * @return the event control singleton.
     */
    public static EventControl getInstance() {
	if (instance == null) {
	    instance = new EventControl();
	}
	return instance;
    }

    /**
     * Get the current event
     * 
     * @return the current event
     */
    public static EvioEvent getCurrentEvent() {
	return _currentEvent;
    }

    /**
     * An event has been parsed. This will notify listeners (e.g., views) that a
     * new event has arrived and they should update their displays. Probably
     * they don't care about the event itself--they will have some event manager
     * that parses the event into format specific structures.
     * 
     * @param baseStructure
     *            the base structure being passed.
     */
    @Override
    public void endEventParse(BaseStructure baseStructure) {

	if (baseStructure instanceof EvioEvent) {
	    _currentEvent = (EvioEvent) baseStructure;
	    notifyPhysicsEvent((EvioEvent) baseStructure);
	}
    }

    /**
     * Got a structure from the event source. This is where we look for
     * structures of interest and put them in conveniently accessible arrays.
     * This does nothing--because this is a generic class that should not know
     * about specific event formats. For ced, the ced code also has a class that
     * listens for structures (an IEvioListener) which then deals with CLAS
     * specifics.
     * 
     * @param baseStructure
     *            the base structure being passed.
     * @param structure
     *            the structure received.
     */
    @Override
    public void gotStructure(BaseStructure baseStructure,
	    IEvioStructure structure) {
    }

    /**
     * A new event is starting to be parsed by jevio.
     * 
     * @param baseStructure
     *            the base structure being passed.
     */
    @Override
    public void startEventParse(BaseStructure baseStructure) {
    }

    /**
     * Notify listeners we have a new event ready for display. All they may want
     * is the notification that a new event has arrived. But the event itself is
     * passed along.
     * 
     * @param evioEvent
     *            the event in question;
     */
    private void notifyPhysicsEvent(EvioEvent evioEvent) {

	Swimming.clearMCTrajectories();
	Swimming.clearReconTrajectories();

	// First listeners first
	if (_beforeViewListenersList != null) {
	    // Guaranteed to return a non-null array
	    Object[] listeners = _beforeViewListenersList.getListenerList();

	    // This weird loop is the bullet proof way of notifying all
	    // listeners.
	    for (int i = listeners.length - 2; i >= 0; i -= 2) {
		if (listeners[i] == IPhysicsEventListener.class) {
		    ((IPhysicsEventListener) listeners[i + 1])
			    .newPhysicsEvent(evioEvent);
		}
	    }
	}

	// now the views
	if (_viewListenerList != null) {
	    // Guaranteed to return a non-null array
	    Object[] listeners = _viewListenerList.getListenerList();

	    // This weird loop is the bullet proof way of notifying all
	    // listeners.
	    for (int i = listeners.length - 2; i >= 0; i -= 2) {
		if (listeners[i] == IPhysicsEventListener.class) {
		    ((IPhysicsEventListener) listeners[i + 1])
			    .newPhysicsEvent(evioEvent);
		}
	    }
	}
    }

    /**
     * Remove a PhysicsEvent listener. PhysicsEvent listeners listen for new
     * events and event arrays.
     * 
     * @param listener
     *            the PhysicsEvent listener to remove.
     */
    public void removePhysicsEventListener(IPhysicsEventListener listener) {

	if ((listener == null) || (_viewListenerList == null)) {
	    return;
	}

	_viewListenerList.remove(IPhysicsEventListener.class, listener);
    }

    /**
     * Add a PhysicsEvent listener. PhysicsEvent listeners listen for new events
     * and event arrays.
     * 
     * @param listener
     *            the PhysicsEvent listener to add.
     */
    public void addPhysicsListener(IPhysicsEventListener listener) {

	if (listener == null) {
	    return;
	}

	if (_viewListenerList == null) {
	    _viewListenerList = new EventListenerList();
	}

	_viewListenerList.add(IPhysicsEventListener.class, listener);
    }

    /**
     * Remove a PhysicsEvent listener. PhysicsEvent listeners listen for new
     * events and event arrays. This method operates on the "first notified"
     * list of listeners that are to be notified before the views.
     * 
     * @param listener
     *            the PhysicsEvent listener to remove.
     */
    public void removeBeforeViewPhysicsEventListener(
	    IPhysicsEventListener listener) {

	if ((listener == null) || (_beforeViewListenersList == null)) {
	    return;
	}

	_beforeViewListenersList.remove(IPhysicsEventListener.class, listener);
    }

    /**
     * Add a PhysicsEvent listener. PhysicsEvent listeners listen for new events
     * and event arrays. This method operates on the "first notified" list of
     * listeners that are to be notified before the views.
     * 
     * @param listener
     *            the PhysicsEvent listener to add.
     */
    public void addBeforeViewPhysicsListener(IPhysicsEventListener listener) {

	if (listener == null) {
	    return;
	}

	if (_beforeViewListenersList == null) {
	    _beforeViewListenersList = new EventListenerList();
	}

	_beforeViewListenersList.add(IPhysicsEventListener.class, listener);
    }

    /**
     * @return the accumulating
     */
    public boolean isAccumulating() {
	return _accumulating;
    }

    /**
     * @param accumulating
     *            the accumulating to set
     */
    public void setAccumulating(boolean accumulating) {
	this._accumulating = accumulating;
    }

    public static EventParser getEvioParser() {
	return _evioParser;
    }

    /**
     * @return the _evioReader
     */
    public static EvioReader getEvioReader() {
	return _evioReader;
    }

    /**
     * Set the evio reader
     * 
     * @param evioReader
     *            the evioReader to set
     */
    public static void setEvioReader(EvioReader evioReader) {
	_evioReader = evioReader;

	if (_evioReader != null) {
	    _evioReader.setParser(_evioParser);
	}
    }

    /**
     * Should be a jevio method. This tries to create an evio event from a byte
     * array bytes, such as might have arrived over a socket or from ET.
     * 
     * @param rawBytes
     *            the raw bytes.
     * @param swap
     *            do I have to swap?
     * @return the reconstructed evio event, or <code>null</code>
     */
    public static EvioEvent eventFromByteBuffer(ByteBuffer buffer) {
	EvioEvent event = null;
	try {

	    EvioReader reader = new EvioReader(buffer);

	    System.err.println("EVCOUNT: " + reader.getEventCount());
	    event = reader.nextEvent();
	    _evioParser.parseEvent(event);
	} catch (EvioException e) {
	    e.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	}

	return event;
    }

    // /**
    // * Should be a jevio method. This tries to create an evio event from a
    // byte array
    // * bytes, such as might have arrived over a socket or from ET.
    // *
    // * @param rawBytes the raw bytes.
    // * @param swap do I have to swap?
    // * @return the reconstructed evio event, or <code>null</code>
    // */
    // public static EvioEvent eventFromBytes(byte rawBytes[], boolean swap) {
    // if (rawBytes == null) {
    // System.err.println("null byte array in eventFromBytes");
    // }
    //
    // int rawByteLen = rawBytes.length;
    // if (rawByteLen < 8) {
    // System.err
    // .println("too few bytes in byte array in eventFromBytes: "
    // + rawByteLen);
    // }
    //
    // // create a backed byte buffer to read from
    // ByteBuffer buffer = ByteBuffer.wrap(rawBytes);
    //
    // int length = buffer.getInt();
    // if (swap) {
    // length = ByteSwap.swapInt(length);
    // }
    // if (length < 1) {
    // System.err.println("negative event length in eventFromBytes");
    // return null;
    // }
    //
    // // the raw byte length should match 4*(event length + 1) where the + 1
    // // is due to the fact that the event length doesn't count the word
    // // that gives the event length!
    // if ((4 * (length + 1)) != rawByteLen) {
    // System.err.println("Unexpected event length in eventFromBytes: "
    // + length);
    // System.err.println("Raw byte length: " + rawByteLen);
    // System.err
    // .println("Expected event length: " + (rawByteLen / 4 - 1));
    // return null;
    // }
    //
    // // read rest of the header
    // byte num = buffer.get();
    // byte dataType = buffer.get();
    //
    // short tag = buffer.getShort();
    // if (swap) {
    // tag = ByteSwap.swapShort(tag);
    // }
    //
    // // the datatype had better be 0xe (14) because an event is a bank of
    // // banks
    // if (dataType != 0xe) {
    // System.err.println("Unexpected dataType in eventFromBytes: "
    // + dataType + "  (expected 14)");
    // return null;
    // }
    //
    // // subtract header to get remaining bytes
    // int payloadBytes = rawByteLen - 8;
    //
    // // that should be the bytes remaining
    // if (payloadBytes != buffer.remaining()) {
    // System.err.println("payload bytes: " + payloadBytes
    // + " does not match remaining bytes: " + buffer.remaining());
    // return null;
    // }
    //
    // // finally feel safe in creating the event
    // EvioEvent event = new EvioEvent();
    // event.setByteOrder(ByteOrder.LITTLE_ENDIAN);
    // event.getHeader().setLength(length);
    // event.getHeader().setNumber(num);
    //
    // DataType dt = DataType.getDataType(dataType); //jevio 4.0
    // event.getHeader().setDataType(dt);
    // event.getHeader().setTag(tag);
    //
    // // System.err.println("Header " + event.getHeader());
    //
    // if (payloadBytes > 0) {
    // byte bytes[] = new byte[buffer.remaining()];
    // buffer.get(bytes);
    //
    // // int len = bytes.length;
    // // System.err.println("rb buffer size: " + len);
    // // System.err.println("first 4 bytes: " + bytes[0] + " " + bytes[1]
    // // + " " + bytes[2] + " " + bytes[3]);
    // // System.err.println("last 4 bytes: " + bytes[len - 4] + " "
    // // + bytes[len - 3] + " " + bytes[len - 2] + " "
    // // + bytes[len - 1]);
    //
    // // buffer.get(bytes);
    // event.setRawBytes(bytes);
    //
    // try {
    // _evioParser.parseEvent(event);
    // } catch (EvioException e) {
    // e.printStackTrace();
    // }
    //
    // }
    //
    // // if (event != null) {
    // // }
    // return event;
    // }
    //

    /**
     * Get the current event source type
     * 
     * @return the current event source type
     */
    public static EventSourceType getEventSourceType() {
	if (EventMenu.getETSourceRadioButton() == null) {
	    return EventSourceType.FILE;
	}

	if (EventMenu.getETSourceRadioButton().isSelected()) {
	    return EventSourceType.ET;
	} else {
	    return EventSourceType.FILE;
	}
    }

}
