package cnuphys.bCNU.et;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.jlab.coda.et.EtAttachment;
import org.jlab.coda.et.EtConstants;
import org.jlab.coda.et.EtEvent;
import org.jlab.coda.et.EtStation;
import org.jlab.coda.et.EtStationConfig;
import org.jlab.coda.et.EtSystem;
import org.jlab.coda.et.EtSystemOpenConfig;
import org.jlab.coda.et.enums.Mode;
import org.jlab.coda.et.exception.EtBusyException;
import org.jlab.coda.et.exception.EtClosedException;
import org.jlab.coda.et.exception.EtDeadException;
import org.jlab.coda.et.exception.EtEmptyException;
import org.jlab.coda.et.exception.EtException;
import org.jlab.coda.et.exception.EtExistsException;
import org.jlab.coda.et.exception.EtTimeoutException;
import org.jlab.coda.et.exception.EtTooManyException;
import org.jlab.coda.et.exception.EtWakeUpException;
import org.jlab.coda.jevio.EvioEvent;

import cnuphys.bCNU.log.Log;

public class EvioETConsumer {

	// the station name
	private static final String _statName = "CED";

	// defaults
	private int position = 10;
	private int pposition = 0;
	private int qSize = 0;
	private int chunk = 1; // one event at a time
	boolean blocking = false;
	int flowMode = EtConstants.stationSerial;

	// the connected ET system
	private EtSystem _etSystem;

	// the ET attachment
	private EtAttachment _etAttachment;

	/**
	 * Create a ced consumer
	 * 
	 * @param etName
	 *            the name of the
	 * @param hostAddress
	 * @param port
	 */
	public EvioETConsumer(String etName, String hostAddress, int port) {

		// make a direct connection to ET system's tcp server
		EtSystemOpenConfig config;
		try {
			config = new EtSystemOpenConfig(etName, hostAddress, port);
			config.setConnectRemotely(true);

			// create ET system object
			_etSystem = new EtSystem(config, EtConstants.debugInfo);
			_etSystem.open();

			// configuration of a new station
			EtStationConfig statConfig = new EtStationConfig();
			statConfig.setFlowMode(flowMode);
			if (!blocking) {
				statConfig.setBlockMode(EtConstants.stationNonBlocking);
				if (qSize > 0) {
					statConfig.setCue(qSize);
				}
			}

			// create station
			EtStation stat = _etSystem.createStation(statConfig, _statName,
					position, pposition);

			// attach to new station
			_etAttachment = _etSystem.attach(stat);

		} catch (EtException e) {
			Log.getInstance().warning(
					"CedETConsumer constructor: " + e.getMessage());
			_etSystem = null;
			_etAttachment = null;
		} catch (IOException e) {
			Log.getInstance().warning(
					"CedETConsumer constructor: " + e.getMessage());
			_etSystem = null;
			_etAttachment = null;
		} catch (EtDeadException e) {
			Log.getInstance().warning(
					"CedETConsumer constructor: " + e.getMessage());
			_etSystem = null;
			_etAttachment = null;
		} catch (EtClosedException e) {
			Log.getInstance().warning(
					"CedETConsumer constructor: " + e.getMessage());
			_etSystem = null;
			_etAttachment = null;
		} catch (EtExistsException e) {
			Log.getInstance().warning(
					"CedETConsumer constructor: " + e.getMessage());
			_etSystem = null;
			_etAttachment = null;
		} catch (EtTooManyException e) {
			Log.getInstance().warning(
					"CedETConsumer constructor: " + e.getMessage());
			_etSystem = null;
			_etAttachment = null;
		}

	}

	/**
	 * Obtain next event from ET
	 * 
	 * @return the next event from ET
	 */
	public EvioEvent nextEvent() {

		if (_etSystem == null) {
			Log.getInstance().warning(
					"Called nextEvent in CedETConsumer with null ET system");
			return null;
		}
		if (_etAttachment == null) {
			Log.getInstance()
					.warning(
							"Called nextEvent in CedETConsumer with null ET attachment");
			return null;
		}

		// array of events
		EtEvent[] mevs = null;
		EvioEvent event = null;

		// get event from ET system
		try {
			mevs = _etSystem.getEvents(_etAttachment, Mode.SLEEP, null, 0,
					chunk);
			if ((mevs != null) && (mevs.length > 0)) {
				ByteBuffer buf = mevs[0].getDataBuffer();

				byte byteArray[] = new byte[buf.limit()];
				buf.get(byteArray);
				// event = EventControl.eventFromBytes(byteArray, false);
			}
		} catch (EtException e) {
			e.printStackTrace();
		} catch (EtDeadException e) {
			e.printStackTrace();
		} catch (EtClosedException e) {
			e.printStackTrace();
		} catch (EtEmptyException e) {
			e.printStackTrace();
		} catch (EtBusyException e) {
			e.printStackTrace();
		} catch (EtTimeoutException e) {
			e.printStackTrace();
		} catch (EtWakeUpException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return event;
	} // nextEvent

	public void detach() {

		if (_etSystem == null) {
			Log.getInstance().warning(
					"Called detach in CedETConsumer with null ET system");
			return;
		}
		if (_etAttachment == null) {
			Log.getInstance().warning(
					"Called detach in CedETConsumer with null ET attachment");
			return;
		}

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
	}

}
