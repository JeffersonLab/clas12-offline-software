package cnuphys.bCNU.component;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.event.EventListenerList;

import cnuphys.bCNU.graphics.component.CommonBorder;

/**
 * A control made of roll over labels
 * @author heddle
 *
 */
public class RollOverPanel extends JPanel {
	
	protected EventListenerList _listenerList;

	
	/**
	 * 
	 * @param title
	 * @param numCols
	 * @param font
	 * @param fg
	 * @param bg
	 * @param labels
	 */
	public RollOverPanel(String title, int numCols, 
			Font font, Color fg, Color bg, String... labels) {
		
		int numLabels = labels.length;
		int numRows = 1 + (numLabels-1)/numCols;
		setLayout(new GridLayout(numRows, numCols, 4, 4));
		for (String label : labels) {
			addLabel(label, font, fg, bg);
		}
		
		if (title != null) {
			new CommonBorder(title);
		}
		
	}
	
	//add a roll over listener
	private void addLabel(String label, Font font, Color fg, Color bg) {
		JLabel jlab = new JLabel(label);
		jlab.setOpaque(true);
		jlab.setFont(font);
		jlab.setForeground(fg);
		jlab.setBackground(bg);
		
		jlab.setHorizontalAlignment(SwingConstants.CENTER);
		
		MouseAdapter ml = new MouseAdapter() {


			@Override
			public void mouseEntered(MouseEvent e) {
				notifyListeners(jlab, 0, e);	
			}

			@Override
			public void mouseExited(MouseEvent e) {
				notifyListeners(jlab, 1, e);	
			}

			
		};

		jlab.addMouseListener(ml);
		add(jlab);
	}

	/**
	 * Notify listeners of a roll over event
	 */
	protected void notifyListeners(JLabel label, int option, MouseEvent e) {
		if (_listenerList == null) {
			return;
		}

		// Guaranteed to return a non-null array
		Object[] listeners = _listenerList.getListenerList();

		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == IRollOverListener.class) {
				if (option == 0) {
					((IRollOverListener) listeners[i + 1]).RollOverMouseEnter(label, e);
				}
				else if (option == 1) {
					((IRollOverListener) listeners[i + 1]).RollOverMouseExit(label, e);
				}

			}
		}

	}

	/**
	 * Remove a roll over listener
	 * 
	 * @param listener the roll over listener remove.
	 */
	public void removeRollOverListener(IRollOverListener listener) {

		if ((listener == null) || (_listenerList == null)) {
			return;
		}

		_listenerList.remove(IRollOverListener.class, listener);
	}

	/**
	 * Add a roll over listener
	 * 
	 * @param listener the roll over listener to add.
	 */
	public void addRollOverListener(IRollOverListener listener) {

		if (listener == null) {
			return;
		}

		if (_listenerList == null) {
			_listenerList = new EventListenerList();
		}

		_listenerList.add(IRollOverListener.class, listener);
	}

}
