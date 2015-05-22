package cnuphys.bCNU.et;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.util.List;

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
import org.jlab.coda.jevio.DataType;
import org.jlab.coda.jevio.EvioCompactStructureHandler;
import org.jlab.coda.jevio.EvioException;
import org.jlab.coda.jevio.EvioNode;

public class ETTestConsumer {

    // defaults
    int position = 1, pposition = 0, qSize = 0, chunk = 1;
    boolean blocking = true, verbose = false;
    String statName = null;
    int flowMode = EtConstants.stationSerial;

    public ETTestConsumer(String etName, String hostAddress, int port) {

	// make a direct connection to ET system's tcp server
	EtSystemOpenConfig config;
	try {
	    config = new EtSystemOpenConfig(etName, hostAddress, port);
	    config.setConnectRemotely(true);

	    // create ET system object with verbose debugging output
	    EtSystem sys = new EtSystem(config, EtConstants.debugInfo);
	    sys.open();

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
	    EtStation stat = sys.createStation(statConfig, "CED", position,
		    pposition);

	    // attach to new station
	    EtAttachment att = sys.attach(stat);

	    // array of events
	    EtEvent[] mevs;

	    while (true) {

		// get events from ET system
		mevs = sys.getEvents(att, Mode.SLEEP, null, 0, chunk);

		for (EtEvent mev : mevs) {
		    // Get event's data buffer
		    // buf.limit() = length of the actual data (not buffer
		    // capacity)
		    ByteBuffer buf = mev.getDataBuffer();

		    EvioCompactStructureHandler comHandler;
		    try {

			comHandler = new EvioCompactStructureHandler(buf,
				DataType.BANK);
			System.out
				.println(" --- Compact Structure Nodes --------");
			try {
			    List<EvioNode> nodes = comHandler.getNodes();
			    for (EvioNode node : nodes) {
				writeNode(System.out, node);
			    }
			} catch (EvioException e) {
			    e.printStackTrace();
			}
			System.out.println(" --------------------------------");

		    } catch (EvioException e) {
			e.printStackTrace();
		    }

		}

	    }

	} catch (EtException e) {
	    e.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	} catch (EtDeadException e) {
	    e.printStackTrace();
	} catch (EtClosedException e) {
	    e.printStackTrace();
	} catch (EtExistsException e) {
	    e.printStackTrace();
	} catch (EtTooManyException e) {
	    e.printStackTrace();
	} catch (EtEmptyException e) {
	    e.printStackTrace();
	} catch (EtBusyException e) {
	    e.printStackTrace();
	} catch (EtTimeoutException e) {
	    e.printStackTrace();
	} catch (EtWakeUpException e) {
	    e.printStackTrace();
	}

    }

    /**
     * Writes a node in a more useful form than its toString merthod
     * 
     * @param stream
     *            any print stream, such as System.out
     * @param node
     */
    public static void writeNode(PrintStream stream, EvioNode node) {
	if (node == null) {
	    stream.println("null");
	} else {
	    StringBuilder builder = new StringBuilder(100);
	    builder.append("tag = ");
	    builder.append(node.getTag());
	    builder.append(", num = ");
	    builder.append(node.getNum());
	    builder.append(", structure = ");
	    builder.append(DataType.getName(node.getType()));
	    builder.append(", dataType = ");
	    builder.append(node.getDataTypeObj());
	    builder.append(", pos = ");
	    builder.append(node.getPosition());
	    builder.append(", dataPos = ");
	    builder.append(node.getDataPosition());
	    builder.append(", totlen = ");
	    builder.append(4 * node.getLength());

	    int lastByte = node.getPosition() + (4 * node.getLength()) - 1;
	    builder.append(", lastByte: " + lastByte);

	    boolean bofb = bankOfBanks(node);
	    builder.append(", bankOfBanks: " + bofb);

	    stream.println(builder.toString());
	}
    }

    // is this node a bank of banks?
    private static boolean bankOfBanks(EvioNode node) {
	if (node == null) {
	    return false;
	}
	DataType structureType = DataType.getDataType(node.getType());
	DataType dataType = node.getDataTypeObj();

	boolean sBank = (structureType == DataType.BANK)
		|| (structureType == DataType.ALSOBANK);
	boolean dBank = (dataType == DataType.BANK)
		|| (dataType == DataType.ALSOBANK);
	return sBank && dBank;
    }

}