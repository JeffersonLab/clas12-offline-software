package cnuphys.bCNU.shell;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import cnuphys.bCNU.dialog.SimpleDialog;
import cnuphys.bCNU.util.Environment;
import cnuphys.bCNU.util.Fonts;

@SuppressWarnings("serial")
public class ShellDialog extends SimpleDialog implements KeyListener {

	// dialog size
	private static final int WIDTH = 700;
	private static final int HEIGHT = 450;

	// singleton
	private static ShellDialog instance;

	// closeout buttons
	private static final String CLOSE = "Close";

	// for entering commands
	private JTextField commandTextField;

	// the main panel
	private Shell shell;

	// private constructor for singleton
	private ShellDialog() {
		super("Micro Shell", false, CLOSE);
		setSize(WIDTH, HEIGHT);
		appendInfo(Environment.getInstance().getUserName()
				+ " micro shell started.\n");
	}

	/**
	 * Get the singleton dialog
	 * 
	 * @return the singleton dialog
	 */
	public static ShellDialog getInstance() {
		if (instance == null) {
			instance = new ShellDialog();
		}
		return instance;
	}

	/**
	 * Set the size of the dialog.
	 */
	@Override
	protected void sizeDialog() {
		setSize(WIDTH, HEIGHT);
	}

	// create the center component
	@Override
	protected Component createCenterComponent() {
		JPanel cp = new JPanel();
		cp.setLayout(new BorderLayout());
		shell = new Shell(WIDTH, HEIGHT);

		cp.add(shell, BorderLayout.CENTER);

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(4, 0));
		JLabel label = new JLabel("command: ");
		label.setFont(Fonts.smallFont);
		panel.add(label, BorderLayout.WEST);

		commandTextField = new JTextField();
		commandTextField.addKeyListener(this);

		panel.add(commandTextField, BorderLayout.CENTER);
		cp.add(panel, BorderLayout.SOUTH);

		return cp;
	}

	@Override
	public Component createWestComponent() {
		return Box.createHorizontalStrut(5);
	}

	@Override
	public Component createEastComponent() {
		return Box.createHorizontalStrut(5);
	}

	@Override
	public void setVisible(boolean vis) {
		if (vis) {
			if (isVisible()) {
				toFront();
			} else {
				super.setVisible(true);
			}
		} else {
			super.setVisible(false);
		}
	}

	public void refresh() {
		shell.refresh();
	}

	/**
	 * Execute an OS command in a separate process
	 * 
	 * @param command
	 *            the command to execute
	 * @param userId
	 *            an optional user id
	 * @param ptype
	 *            an optional process type
	 * @return a process id
	 */
	public int execute(String command, long userId, int ptype) {
		return execute(command, null, userId, ptype);
	}

	/**
	 * Execute a command in its own process
	 * 
	 * @param command
	 *            the command to execute
	 * @param dir
	 *            first cd to this directory
	 * @param userId
	 *            an optional user id
	 * @param ptype
	 *            an optional process type
	 * @return a process id
	 */
	public int execute(final String command, File dir, long userId, int ptype) {
		setVisible(true);
		return shell.execute(command, dir, userId, ptype);
	}

	// append an info line
	public void appendInfo(String line) {
		shell.appendInfo(line);
	}

	// append a result line
	public void appendResult(String line) {
		shell.appendResult(line);
	}

	// append a standard out line
	public void appendStandardOut(String line) {
		shell.appendStandardOut(line);
	}

	// append a standard err line
	public void appendStandardErr(String line) {
		shell.appendStandardErr(line);
	}

	@Override
	public void keyPressed(KeyEvent kev) {
	}

	@Override
	public void keyReleased(KeyEvent kev) {
		if (kev.getKeyCode() == KeyEvent.VK_ENTER) {
			String command = commandTextField.getText();

			if ((command != null) && (command.length() > 0)) {
				execute(command, 0, 0);
			}

			commandTextField.setText("");
		}
	}

	@Override
	public void keyTyped(KeyEvent kev) {
	}

	/**
	 * Add an <code>IProcessListener</code>.
	 * 
	 * @see IProcessListener
	 * @param processListener
	 *            the <code>IProcessListener</code> to add.
	 */
	public void addProcessListener(IProcessListener processListener) {
		shell.addProcessListener(processListener);
	}

	/**
	 * Remove an <code>IProcessListener</code>.
	 * 
	 * @see IProcessListener
	 * @param processListener
	 *            the <code>IProcessListener</code> to remove.
	 */
	public void removeProcessListener(IProcessListener processListener) {
		shell.removeProcessListener(processListener);
	}

	public static void main(String arg[]) {

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				getInstance().setVisible(true);
				// getInstance().execute("ls -al");
				getInstance().execute("printenv | grep PATH", 0, 0);
			}
		});

	}

}
