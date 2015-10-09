package cnuphys.bCNU.view;

import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import cnuphys.bCNU.attributes.AttributeType;
import cnuphys.bCNU.shell.IProcessListener;
import cnuphys.bCNU.shell.Shell;
import cnuphys.bCNU.util.Fonts;

/**
 * This is a predefined view used to mimic a shell.
 * 
 * @author heddle
 * 
 */
@SuppressWarnings("serial")
public class MiniShellView extends BaseView implements KeyListener {

	// for entering commands
	private JTextField commandTextField;

	// the underlying faux shell
	private Shell shell;

	// reserved view type for mini shell view
	public static final int MINISHELLVIEWTYPE = -7746231;

	// singleton
	private static MiniShellView instance;

	private MiniShellView() {
		super(AttributeType.TITLE, "Mini Shell", AttributeType.ICONIFIABLE,
				true, AttributeType.MAXIMIZABLE, true, AttributeType.CLOSABLE,
				true, AttributeType.RESIZABLE, true, AttributeType.WIDTH, 700,
				AttributeType.HEIGHT, 450, AttributeType.VISIBLE, false,
				AttributeType.VIEWTYPE, MINISHELLVIEWTYPE);
		add(mainPanel());
		setVisible(false);
	}

	/**
	 * Get the singleton mini shell view
	 * 
	 * @return the singleton mini shell view
	 */
	public static MiniShellView getInstance() {
		if (instance == null) {
			instance = new MiniShellView();
		}
		return instance;
	}

	// create the main panel
	private JPanel mainPanel() {
		JPanel cp = new JPanel();
		cp.setLayout(new BorderLayout());
		shell = new Shell(WIDTH, HEIGHT);

		cp.add(shell, BorderLayout.CENTER);

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(4, 0));
		JLabel label = new JLabel(" command: ");
		label.setFont(Fonts.smallFont);
		panel.add(label, BorderLayout.WEST);

		commandTextField = new JTextField();
		commandTextField.addKeyListener(this);

		panel.add(commandTextField, BorderLayout.CENTER);
		cp.add(panel, BorderLayout.SOUTH);

		cp.add(Box.createHorizontalStrut(4), BorderLayout.WEST);

		return cp;
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

	/**
	 * Get the underlying shell object
	 * 
	 * @return the underlying shell object
	 */
	public Shell getShell() {
		return shell;
	}
}
