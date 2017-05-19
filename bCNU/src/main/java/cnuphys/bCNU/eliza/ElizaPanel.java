package cnuphys.bCNU.eliza;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JPanel;
import javax.swing.JTextField;

import cnuphys.bCNU.graphics.component.TextPaneScrollPane;
import cnuphys.bCNU.util.Environment;

/**
 * A panel to hold an Eliza dialog
 * 
 * @author heddle
 *
 */
@SuppressWarnings("serial")
public class ElizaPanel extends JPanel {

	private ElizaMain _elizaMain;
	private TextPaneScrollPane _textPane;
	private JTextField _textfield;

	private String _userName;

	public ElizaPanel() {
		_elizaMain = new ElizaMain();
		setLayout(new BorderLayout(4, 4));

		_textPane = new TextPaneScrollPane();
		_textPane.setPreferredSize(new Dimension(500, 500));

		add("Center", _textPane);

		_textfield = new JTextField(15);
		add("South", _textfield);

		String hello = "Hello.";

		patient(hello);
		doctor(_elizaMain.processInput(hello));

		_textfield.requestFocus();

		KeyAdapter keyAdapter = new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent kev) {
				if (kev.getKeyCode() == KeyEvent.VK_ENTER) {
					String input = _textfield.getText();

					// see if we override
					String reply = ElizaOverride.getResponse(input);

					if (reply == null) {
						reply = _elizaMain.processInput(input);
					}
					_textfield.setText("");
					patient(input);
					doctor(reply);
				}
			}
		};
		_textfield.addKeyListener(keyAdapter);

	}

	/**
	 * The doctor is speaking
	 * 
	 * @param s
	 *            what she is saying
	 */
	public void doctor(String s) {
		Environment.getInstance().say(s);
		_textPane.append("Eliza:  ", TextPaneScrollPane.BLUE_SS_12_B);
		_textPane.append(s + "\n", TextPaneScrollPane.BLUE_SS_12_B);
	}

	/**
	 * The patient is speaking
	 * 
	 * @param s
	 */
	public void patient(String s) {
		if (_userName == null) {
			_userName = System.getProperty("user.name");
		}
		_textPane.append(_userName + ":  ", TextPaneScrollPane.GREEN_SS_12_B);
		_textPane.append(s + "\n", TextPaneScrollPane.GREEN_SS_12_B);
	}

	/**
	 * Clear all text
	 */
	public void clear() {
		_textPane.clear();
	}

}
