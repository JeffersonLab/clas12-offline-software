package cnuphys.bCNU.et;

import java.io.File;

import org.jlab.coda.et.EtConstants;
import org.jlab.coda.et.system.SystemConfig;
import org.jlab.coda.et.system.SystemCreate;

import cnuphys.bCNU.log.Log;
import cnuphys.splot.plot.Environment;

public class StartET {

    // defaults
    private int _serverPort = EtConstants.serverPort;
    private int udpPort = EtConstants.broadcastPort;
    private int multicastPort = EtConstants.multicastPort;
    private int recvBufSize = 0, sendBufSize = 0;
    private int numGroups = 1;
    private boolean debug = false;
    private boolean noDelay = false;

    // the SystemCreate object
    private SystemCreate _system;

    public StartET() {
	this(EvioFileProducer.MAXEVENTSIZE, 100);
    }

    /**
     * Start an ET system. In java, and et system is essentially a server
     * socket. This will use the default server port in EtConstants.serverPort.
     * 
     * @param sizeInBytes
     *            should be big enough for most evio events. Will still work if
     *            we put in an event bigger than this.
     * @param numEvents
     *            the number of ET events
     */
    public StartET(int sizeInBytes, int numEvents) {
	this(ETSupport._defaultName, sizeInBytes, numEvents);
    }

    /**
     * Start an ET system. In java, and et system is essentially a server
     * socket. This will use the default server port in EtConstants.serverPort.
     * 
     * @param etname
     *            the name of the et system. This should be the full path to a
     *            filr that et can create, so /tmp is a good place.
     * @param sizeInBytes
     *            should be big enough for most evio events. Will still work if
     *            we put in an event bigger than this.
     * @param numEvents
     *            the number of ET events
     */
    public StartET(String etName, int sizeInBytes, int numEvents) {

	if (etName == null) {
	    int ranint = (int) (Integer.MAX_VALUE * Math.random());
	    etName = "/tmp/et_sys_" + ranint;
	}

	// check length of name
	if (etName.length() >= EtConstants.fileNameLengthMax) {
	    System.err.println("ET file name is too long");
	    return;
	}

	// delete the file if it exists
	File f = new File(etName);
	f.delete();

	System.out.println("STARTING ET SYSTEM");
	// ET system configuration object
	SystemConfig config = new SystemConfig();

	try {
	    // set tcp server port
	    config.setServerPort(_serverPort);
	    // set port for listening for udp packets
	    config.setUdpPort(udpPort);
	    // set port for listening for multicast udp packets
	    // (on Java this must be different than the udp port)
	    config.setMulticastPort(multicastPort);
	    // set total number of events
	    config.setNumEvents(numEvents);
	    // set size of events in bytes
	    config.setEventSize(sizeInBytes);
	    // set tcp receive buffer size in bytes
	    if (recvBufSize > 0) {
		config.setTcpRecvBufSize(recvBufSize);
	    }
	    // set tcp send buffer size in bytes
	    if (sendBufSize > 0) {
		config.setTcpSendBufSize(sendBufSize);
	    }
	    // set tcp no-delay
	    if (noDelay) {
		config.setNoDelay(noDelay);
	    }
	    // set debug level
	    if (debug) {
		config.setDebug(EtConstants.debugInfo);
	    }

	    // divide events into equal groups and any leftovers into another
	    // group */
	    if (numGroups > 1) {
		int addgroup = 0;

		int n = numEvents / numGroups;
		int r = numEvents % numGroups;
		if (r > 0) {
		    addgroup = 1;
		}

		int[] groups = new int[numGroups + addgroup];

		for (int i = 0; i < numGroups; i++) {
		    groups[i] = n;
		}

		if (addgroup > 0) {
		    groups[numGroups] = r;
		}

		config.setGroups(groups);
	    }

	    // create an active ET system
	    _system = new SystemCreate(etName, config);
	    System.out.println(_system.getName());
	    System.out.println(_system.getNetAddresses()[0].getHostAddress());

	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    /**
     * Shut down the et system
     */
    public void shutDown() {
	if (_system == null) {
	    Log.getInstance()
		    .warning(
			    "Trying to shutdwon ET system but the system object is null.");
	    return;
	}

	Log.getInstance().info("Attempting to shut down  local ET system");
	_system.shutdown();
    }

    /**
     * Get the System create object
     * 
     * @return the system create object
     */
    public SystemCreate getSystem() {
	return _system;
    }

    /**
     * Get the host IP address
     * 
     * @return the host address
     */
    public String getHostAddress() {
	return _system.getNetAddresses()[0].getHostAddress();
    }

    /**
     * Get the host name
     * 
     * @return the host name
     */
    public String getHostName() {
	return _system.getNetAddresses()[0].getHostName();
    }

    /**
     * Obtaine the server port number
     * 
     * @return the server port number
     */
    public int getServerPort() {
	return _serverPort;
    }

    /**
     * Get the ET name, which is also the path to a tmp file
     * 
     * @return the ET name
     */
    public String getETName() {
	return _system.getName();
    }

    /**
     * Used so that we can launch in a separate vm
     * 
     * @return the launch command
     */
    public static String getLaunchCommand() {
	String command = "java -cp ";
	command = command + Environment.getInstance().getClassPath();
	command = command + " cnuphys.bCNU.et.StartET";

	if (Environment.getInstance().isWindows()) {
	    command = command.replace('\\', '/');
	}
	return command;
    }

    /**
     * Used to start in a different jvm
     * 
     * @param arg
     *            command line arguments
     */
    public static void main(String arg[]) {
	new StartET();
    }
}