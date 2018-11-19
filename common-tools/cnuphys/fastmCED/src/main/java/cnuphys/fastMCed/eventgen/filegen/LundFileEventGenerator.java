package cnuphys.fastMCed.eventgen.filegen;

import java.awt.Toolkit;
import java.io.File;

import org.jlab.clas.physics.PhysicsEvent;
import org.jlab.physics.io.LundReader;

import cnuphys.fastMCed.eventgen.AEventGenerator;
import cnuphys.lund.LundFileSupport;

/**
 * A generator that uses a lund file as its source
 * @author heddle
 *
 */
public class LundFileEventGenerator extends AEventGenerator {
	
	//the lund file
	private File _file;
	
	//for parsing lund files
	private LundReader _lundReader;
	
	// the event number
	private int _eventNum;

	// event count
	private int _eventCount;

	//the most recently read or created event
	private PhysicsEvent _currentEvent;
		
	/**
	 * A generator based on a lund file
	 * @param file the lund file
	 */
	public LundFileEventGenerator(File file) {
		_file = file;
		_lundReader = new LundReader();

		_eventCount = LundFileSupport.getInstance().countEvents(file);
		System.err.println("Event count: " + _eventCount);

		_lundReader.addFile(_file.getPath());
		_lundReader.open();
	}

	@Override
	public String generatorDescription() {
		if (_file == null) {
			return "lund file: none";
		}
		else {
			return "lund file: " + _file.getName() + " #events: " + _eventCount;
		}
	}

	@Override
	public PhysicsEvent nextEvent() {
		boolean gotOne = _lundReader.next();

		if (gotOne) {
			_currentEvent = _lundReader.getEvent();
			_eventNum++;
		} else {
			_currentEvent = null;
		}
		return _currentEvent;
	}
	

	@Override
	public int eventNumber() {
		return _eventNum;
	}

	@Override
	public int eventCount() {
		return _eventCount;
	}

	@Override
	public PhysicsEvent getCurrentEvent() {
		return _currentEvent;
	}
	
	/**
	 * Get the underlying file
	 * @return the lund file
	 */
	public File getFile() {
		return _file;
	}
	
	

}
