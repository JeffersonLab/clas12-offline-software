package cnuphys.bCNU.shell;

public class ProcessRecord {

	private String _command;
	private String _dir;
	private int _pid;
	private long _startTime;
	private int _type;
	private Process _process;

	// an optional if provided by the user application
	private long _userId;

	/**
	 * Constructor
	 * 
	 * @param command
	 *            the command that is being invoked
	 * @param dir
	 *            the working directory
	 * @param pid
	 *            a process id
	 * @param userId
	 *            a user id
	 * @param ptype
	 *            a process type
	 */
	public ProcessRecord(String command, String dir, int pid, long userId,
			int ptype) {
		super();
		_command = command;
		_pid = pid;
		_userId = userId;
		_type = ptype;
		_startTime = System.currentTimeMillis();
	}

	/**
	 * Set the OS process
	 * 
	 * @param process
	 *            the OS process
	 */
	public void setProcess(Process process) {
		_process = process;
	}

	/**
	 * Get the OS process
	 * 
	 * @return the OS process
	 */
	public Process getProcess() {
		return _process;
	}

	/**
	 * Attempt to kill the process
	 */
	public void kill() {
		if (_process != null) {
			System.err.println("Destroying a process.");
			_process.destroy();
		}
	}

	/**
	 * Get the process command
	 * 
	 * @return the command
	 */
	public String getCommand() {
		return _command;
	}

	/**
	 * Get the process optional working directory (might be null if not used)
	 * 
	 * @return the the process optional working directory
	 */
	public String getDir() {
		return _dir;
	}

	/**
	 * Get the process Id
	 * 
	 * @return the ptocessId
	 */
	public int getPid() {
		return _pid;
	}

	/**
	 * Get the approximate process start time
	 * 
	 * @return the approximate startTime
	 */
	public long getStartTime() {
		return _startTime;
	}

	/**
	 * @return the userId
	 */
	public long getUserId() {
		return _userId;
	}

	/**
	 * @param userId
	 *            the userId to set
	 */
	public void setUserId(long userId) {
		this._userId = userId;
	}

	/**
	 * @return the type
	 */
	public int getType() {
		return _type;
	}

}
