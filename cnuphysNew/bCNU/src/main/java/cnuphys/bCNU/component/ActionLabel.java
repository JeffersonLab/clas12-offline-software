package cnuphys.bCNU.component;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JLabel;
import javax.swing.event.EventListenerList;

public class ActionLabel extends JLabel {

	private static int id = 0;

	private static Font enabledFont = new Font("SansSerif", Font.ITALIC, 8);
	private static Font disabledFont = new Font("SansSerif", Font.BOLD, 8);

	private static Color enabledFg = Color.red;
	private static Color disabledFg = Color.gray;

	// list of listeners
	private EventListenerList _actionListenerList;

	public ActionLabel(String label, boolean enabled) {
		super(label);
		setEnabled(enabled);

		MouseAdapter ma = new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				notifyActionListeners();
			}

		};
		addMouseListener(ma);
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		if (enabled) {
			setFont(enabledFont);
			setForeground(enabledFg);
		} else {
			setFont(disabledFont);
			setForeground(disabledFg);
		}
	}

	/**
	 * Notify listeners we have a new event ready for display. All they may want
	 * is the notification that a new event has arrived. But the event itself is
	 * passed along.
	 * 
	 * @param evioEvent
	 *            the event in question;
	 */
	private void notifyActionListeners() {

		if (!isEnabled()) {
			return;
		}

		if (_actionListenerList != null) {
			// Guaranteed to return a non-null array
			Object[] listeners = _actionListenerList.getListenerList();

			ActionEvent ae = new ActionEvent(this, ++id, getText());

			// This weird loop is the bullet proof way of notifying all
			// listeners.
			for (int i = listeners.length - 2; i >= 0; i -= 2) {
				if (listeners[i] == ActionListener.class) {
					((ActionListener) listeners[i + 1]).actionPerformed(ae);
				}
			}
		}
	}

	/**
	 * Remove an ActionListener.
	 * 
	 * @param listener
	 *            the ActionListener to remove.
	 */
	public void removeActionListener(ActionListener listener) {

		if ((listener == null) || (_actionListenerList == null)) {
			return;
		}

		_actionListenerList.remove(ActionListener.class, listener);
	}

	/**
	 * Add an ActionListener.
	 * 
	 * @param listener
	 *            the ActionListener listener to add.
	 */
	public void addActionListener(ActionListener listener) {

		if (listener == null) {
			return;
		}

		if (_actionListenerList == null) {
			_actionListenerList = new EventListenerList();
		}

		_actionListenerList.add(ActionListener.class, listener);
	}

}
