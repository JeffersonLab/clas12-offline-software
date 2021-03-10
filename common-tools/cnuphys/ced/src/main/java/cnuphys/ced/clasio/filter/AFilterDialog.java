package cnuphys.ced.clasio.filter;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import cnuphys.bCNU.dialog.SimpleDialog;
import cnuphys.bCNU.graphics.component.CommonBorder;
import cnuphys.bCNU.util.Fonts;

public class AFilterDialog extends SimpleDialog {

	// the filter being edited
	private AEventFilter _filter;

	// comment text area
	private JTextArea _commentArea;

	// for saving to user pref
	private JButton _saveButton;

	public AFilterDialog(String title, AEventFilter filter) {
		super(title, true, "Close");
		_filter = filter;
	}

	/**
	 * Override to create the component that goes in the center. Usually this is the
	 * "main" component.
	 * 
	 * @return the component that is placed in the center
	 */
	protected Component createCenterComponent() {
		JPanel cp = new JPanel();
		cp.setLayout(new BorderLayout(4, 6));

		JComponent mainComponent = createMainComponent();
		if (mainComponent != null) {
			cp.add(mainComponent, BorderLayout.CENTER);
		}

		cp.add(createCommentArea(), BorderLayout.SOUTH);
		return cp;
	}

	/**
	 * Override to create the component that goes in the north.
	 * 
	 * @return the component that is placed in the center
	 */
	protected Component createNorthComponent() {
		JPanel cp = new JPanel();
		cp.setLayout(new BorderLayout(4, 6));

		cp.add(createButtonPanel(), BorderLayout.SOUTH);
		return cp;
	}

	/**
	 * Create the main component
	 * 
	 * @return the main component of the editor
	 */
	public JComponent createMainComponent() {
		return null;
	}

	/**
	 * Create a button panel.
	 * @return
	 */
	public JPanel createButtonPanel() {
		JPanel bp = new JPanel();

		bp.setLayout(new FlowLayout(FlowLayout.CENTER, 6, 2));

		ActionListener al = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				savePreferences();
			}

		};

		_saveButton = new JButton("Save Settings");
		_saveButton.addActionListener(al);
		bp.add(_saveButton);

		return bp;
	}

	/**
	 * Save the preferences to user pref
	 */
	private void savePreferences() {
		_filter.savePreferences();
	}
	
	/**
	 * A closeout button was hit. The default behavior is to shutdown the dialog.
	 * 
	 * @param command the label on the button that was hit.
	 */
	@Override
	protected void handleCommand(String command) {
		setVisible(false);
	}

	/**
	 * Create the text area that will display comments about the filtering
	 *
	 * @return the scroll pane holding the text area.
	 */
	private JScrollPane createCommentArea() {

		_commentArea = new JTextArea(10, 40) {
			@Override
			public Dimension getMinimumSize() {
				return new Dimension(300, 200);
			}
		};
		_commentArea.setFont(Fonts.defaultMono);
		_commentArea.setEditable(false);

		JScrollPane scrollPane = new JScrollPane() {
			@Override
			public Dimension getMinimumSize() {
				return new Dimension(300, 200);
			}
		};
		scrollPane.getViewport().setView(_commentArea);
		scrollPane.setPreferredSize(new Dimension(400, 200));

		scrollPane.setBorder(new CommonBorder("comments"));
		return scrollPane;
	}

	/**
	 * Set the comment text area text
	 * 
	 * @param text the new text
	 */
	public void setCommentText(String text) {
		_commentArea.setText((text == null) ? "" : text);
	}

}
