package cnuphys.bCNU.shell;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.event.EventListenerList;

import cnuphys.bCNU.graphics.component.TextPaneScrollPane;
import cnuphys.bCNU.util.Environment;

/**
 * A "shell" that creates an external process and captures standard out.
 * 
 * @author heddle
 * 
 */
@SuppressWarnings("serial")
public class Shell extends TextPaneScrollPane {

	// used for process id
	private static int _pid = 1;

	// notification options
	private static final int PROCESS_START = 0;
	private static final int PROCESS_END = 1;
	private static final int PROCESS_FAIL = 2;

	// Listener list for process listeners
	private EventListenerList _listenerList;

	// special command
	private static final String _cwdToFollow = "CWD_TO_FOLLOW";

	// maintain a current working directory
	private String _cwd = Environment.getInstance()
			.getCurrentWorkingDirectory();

	// queued commands
	private Vector<ProcessRecord> commands = new Vector<ProcessRecord>();

	// thread for dequeueing commands
	private Thread dequeueThread;

	// used to find the user path
	private static String _userPath = "";
	private static boolean _triedOnce = false;

	private static final String forbiddenCommands[] = { "vi", "vim", "emacs",
			"more", "less", "top", "edit" };

	/**
	 * Create a Shell object, which behaves like a micro shell and is used for
	 * running OS processes
	 * 
	 * @param w
	 *            width
	 * @param h
	 *            height
	 */
	public Shell(int w, int h) {

		textPane.setBackground(new Color(32, 48, 48));
		textPane.setOpaque(true);

		textPane.setEditable(true);

		setPreferredSize(new Dimension(w, h));

		// the dequeue thread

		Runnable dequeue = new Runnable() {
			@Override
			public void run() {
				while (true) {
					if (commands.isEmpty()) {
						try {
							synchronized (commands) {
								commands.wait();
							}
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					ProcessRecord processRecord = commands.remove(0);
					baseExecute(processRecord);
				}
			}
		};

		dequeueThread = new Thread(dequeue);
		dequeueThread.start();
	}

	/**
	 * Add a <code>IProcessListener</code>.
	 * 
	 * @see IProcessListener
	 * @param processListener
	 *            the <code>IProcessListener</code> to add.
	 */
	public void addProcessListener(IProcessListener processListener) {

		if (processListener == null) {
			return;
		}

		if (_listenerList == null) {
			_listenerList = new EventListenerList();
		}

		_listenerList.add(IProcessListener.class, processListener);
	}

	/**
	 * Remove a <code>IProcessListener</code>.
	 * 
	 * @see IProcessListener
	 * @param processListener
	 *            the <code>IProcessListener</code> to remove.
	 */
	public void removeProcessListener(IProcessListener processListener) {

		if ((processListener == null) || (_listenerList == null)) {
			return;
		}

		_listenerList.remove(IProcessListener.class, processListener);
	}

	/**
	 * Notify process listeners
	 */
	public void notifyProcessListeners(int option, ProcessRecord processRecord) {

		if (_listenerList == null) {
			return;
		}

		// Guaranteed to return a non-null array
		Object[] listeners = _listenerList.getListenerList();

		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == IProcessListener.class) {
				final IProcessListener processListener = (IProcessListener) listeners[i + 1];

				if (option == PROCESS_START) {
					processListener.processStart(processRecord);
				} else if (option == PROCESS_END) {
					processListener.processEnd(processRecord);
				} else if (option == PROCESS_FAIL) {
					processListener.processFail(processRecord);
				}

			}
		}
	}

	/**
	 * Execute a command in its own process
	 * 
	 * @param command
	 *            the array of commands to execute
	 * @param userId
	 *            an optional user id
	 * @param ptype
	 *            an optional process type
	 * @return a process id
	 */
	public int execute(final String command, long userId, int ptype) {
		return execute(command, null, userId, ptype);
	}

	/**
	 * Execute a command in its own process
	 * 
	 * @param command
	 *            the array of commands to execute
	 * @param dir
	 *            first cd to this directory
	 * @param userId
	 *            an optional user id
	 * @param ptype
	 *            an optional process type
	 * @return a process id
	 */
	public int execute(final String command, File dir, long userId, int ptype) {

		// forbidden command?
		for (String fc : forbiddenCommands) {
			if (command.startsWith(fc)) {
				appendInfo("Micro shell cannot execute command: " + fc);
				return -1;
			}
		}

		synchronized (commands) {

			if ((dir != null) && dir.exists() && dir.isDirectory()) {
				_cwd = dir.getPath();
			}

			ProcessRecord processRecord = new ProcessRecord(command,
					dir == null ? null : dir.getPath(), _pid, userId, ptype);
			commands.add(processRecord);
			commands.notify();

		}
		return _pid++;
	}

	// underlying execution of the process
	public void baseExecute(ProcessRecord processRecord) {
		(new Executive(processRecord)).execute();
	}

	// append an info line
	public void appendInfo(String line) {
		append(line + "\n", YELLOW_TERMINAL);
	}

	// append a result line
	public void appendResult(String line) {
		append(line + "\n", CYAN_TERMINAL);
	}

	boolean pending = false;

	// append a standard out line
	public void appendStandardOut(String line) {
		if (line.contains(_cwdToFollow)) {
			pending = true;
			return;
		}
		if (pending) {
			_cwd = line;
			pending = false;
			return;
		}
		append(line + "\n", GREEN_TERMINAL);
	}

	// append a standard err line
	public void appendStandardErr(String line) {
		if (ignore(line)) {
			return;
		}
		append(line + "\n", RED_TERMINAL);
	}

	// used to ignore certain stderr lines
	private boolean ignore(String line) {
		if (line == null) {
			return true;
		}

		if (Environment.getInstance().isWindows()) {
			String ucline = line.toLowerCase().trim();

			if (ucline.startsWith("cygwin")) {
				return true;
			}
			if (ucline.startsWith("ms-dos")) {
				return true;
			}
			if (ucline.startsWith("preferred posix")) {
				return true;
			}
			if (ucline.startsWith("consult the user")) {
				return true;
			}

		}

		return false;
	}

	// build a script file around the command
	private File tempFileScript(String command) {

		File file = null;
		try {
			file = File.createTempFile("bCNU", null);

			// try to get the user path(mostly for eclipse running)
			_userPath = userPath();

			PrintWriter printWriter = new PrintWriter(file);
			printWriter.write("#!/bin/bash\n");
			printWriter.write(_userPath + "\n");

			if (Environment.getInstance().isWindows()) {
				printWriter.write("cd " + _cwd.replace('\\', '/') + "\n");
			} else {
				printWriter.write("cd " + _cwd + "\n");
			}

			printWriter.write(command + "\n");
			printWriter.write("echo " + _cwdToFollow + "\n");
			printWriter.write("pwd" + "\n");
			printWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return file;
	}

	// try to get the user path. Mostly for eclipse running--since at least on
	// the mac I can't
	// get eclipse to use the path from the user's .profile
	private String userPath() {

		if (_triedOnce) {
			return _userPath;
		}
		_triedOnce = true;

		if (Environment.getInstance().isMac()) {
			File pfile = new File(Environment.getInstance().getHomeDirectory(),
					".profile");
			if (pfile.exists()) {
				try {
					final BufferedReader bufferedReader = new BufferedReader(
							new FileReader(pfile));
					boolean reading = true;
					while (reading) {
						String line = bufferedReader.readLine();
						if (line == null) {
							reading = false;
						} else {
							if (line.contains("PATH")) {
								_userPath += line + "\n";
							}
						}
					}
					bufferedReader.close();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		}
		return _userPath;
	}

	public static void main(String arg[]) {
		final JFrame frame = new JFrame("Test Shell");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new BorderLayout());

		Shell shell = new Shell(400, 400);
		frame.add(shell, BorderLayout.CENTER);

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				frame.pack();
				frame.setVisible(true);
				frame.setLocationRelativeTo(null);
			}
		});

		shell.execute("ls -al", 0, 0);
		shell.execute("printenv | grep PATH", 0, 0);
		shell.execute("passwd", 0, 0);
	}

	class Executive {

		private boolean done;
		private Process process;
		private ProcessRecord processRecord;

		public Executive(ProcessRecord pr) {
			processRecord = pr;
			done = false;
		}

		public void execute() {

			appendInfo(processRecord.getCommand());

			final File file = tempFileScript(processRecord.getCommand());
			if (file == null) {
				return;
			}

			try {
				notifyProcessListeners(PROCESS_START, processRecord);
				process = Runtime.getRuntime().exec("bash " + file.getPath());

				if (process == null) {
					notifyProcessListeners(PROCESS_FAIL, processRecord);
					return;
				}

				processRecord.setProcess(process);

				final BufferedReader stdOutReader = new BufferedReader(
						new InputStreamReader(process.getInputStream()));
				final BufferedReader stdErrReader = new BufferedReader(
						new InputStreamReader(process.getErrorStream()));

				// this will just block until process ends
				Runnable runnable = new Runnable() {

					@Override
					public void run() {
						try {
							process.waitFor();
							done = true;
						} catch (InterruptedException e) {
							e.printStackTrace();
						}

					}

				};

				Runnable writer = new Runnable() {

					@Override
					public void run() {
						while (!done) {
							try {
								Thread.sleep(50);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}

					}

				};

				Runnable reader = new Runnable() {

					@Override
					public void run() {
						try {
							while (!done) {
								String line = stdOutReader.readLine();
								if (line != null) {
									// System.out.println("NOT DONE");
									appendStandardOut(line);
								} else {
									try {
										Thread.sleep(50);
									} catch (InterruptedException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
							}
							// flush final lines after process ended
							process.getOutputStream().flush();
							// System.out.println("**DONE");
							boolean reading = true;
							while (reading) {
								String line = stdOutReader.readLine();
								if (line == null) {
									reading = false;
								} else {
									// System.out.println("DONE");

									appendStandardOut(line);
								}
							}
							// really done
							stdOutReader.close();

							reading = true;
							while (reading) {
								String line = stdErrReader.readLine();
								if (line == null) {
									reading = false;
								} else {
									appendStandardErr(line);
								}
							}
							stdErrReader.close();
						} catch (IOException e) {
							e.printStackTrace();
						}

						notifyProcessListeners(PROCESS_END, processRecord);
						file.delete();
					} // end reader run

				};

				(new Thread(writer)).start();
				(new Thread(runnable)).start();
				(new Thread(reader)).start();

			} catch (Error error) {
				notifyProcessListeners(PROCESS_FAIL, processRecord);
				appendStandardErr(error.getMessage());
			} catch (Exception e) {
				notifyProcessListeners(PROCESS_FAIL, processRecord);
				appendStandardErr(e.getMessage());
			}

		}
	}
}
