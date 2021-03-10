package cnuphys.bCNU.dialog;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.Insets;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import cnuphys.bCNU.component.TextAreaWriter;
import cnuphys.bCNU.util.Fonts;

public class TextDisplayDialog extends SimpleDialog {

	private static final String CLOSE = "Close";

	private static final int WIDTH = 800;
	private static final int HEIGHT = 900;

	private static final Font _font = Fonts.defaultMono;

	// the writer
	private TextAreaWriter _writer;

	// the text area
	private JTextArea _textArea;

	private JPanel _centerPanel;

	/**
	 * Create a simple display dialog
	 */
	public TextDisplayDialog(String title) {
		super(title, false, CLOSE);
		_textArea.setText("");
		_textArea.setEditable(false);
		_textArea.setLineWrap(false);
		_textArea.setFont(_font);

		_writer = new TextAreaWriter(_textArea);

		setSize(WIDTH, HEIGHT);
		DialogUtilities.centerDialog(this);
	}

	/**
	 * Set the text in the text area
	 * 
	 * @param text the text
	 */
	public void setText(String text) {
		_textArea.setText(text);
	}

	/**
	 * Get the Writer object that writes to the text area
	 * 
	 * @return the writer
	 */
	public TextAreaWriter getWriter() {
		return _writer;
	}

	/**
	 * Override to create the component that goes in the center.
	 * 
	 * @return the component that is placed in the north
	 */
	@Override
	protected Component createCenterComponent() {

		_centerPanel = new JPanel() {
			@Override
			public Insets getInsets() {
				Insets def = super.getInsets();
				return new Insets(def.top + 4, def.left + 4, def.bottom + 4, def.right + 4);
			}
		};

		_textArea = new JTextArea(20, 80);

		JScrollPane scrollPane = new JScrollPane(_textArea);

		_centerPanel.setLayout(new BorderLayout(4, 4));
		_centerPanel.add(scrollPane, BorderLayout.CENTER);

		return _centerPanel;
	}

	/**
	 * Get the text area that displays the yaml
	 * 
	 * @return the text area that displays the yaml
	 */
	public JTextArea getTextArea() {
		return _textArea;
	}
}