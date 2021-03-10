package cnuphys.bCNU.ping;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;
import javax.swing.event.EventListenerList;


public class Ping {
		
	//the collection of listeners
	private EventListenerList _listeners;
	
	public Ping(int delayInMillis) {
		ActionListener taskPerformer = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				notifyListeners();
			}
		};
		new Timer(delayInMillis, taskPerformer).start();
	}
	
	//ping all the listeners
	private void notifyListeners() {
				
		if (_listeners == null) {
			return;
		}
		
		// Guaranteed to return a non-null array
		Object[] listeners = _listeners.getListenerList();

		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == IPing.class) {
				((IPing) listeners[i + 1]).ping();
			}
		}
	}
	
	/**
	 * Remove a ping listener.
	 * 
	 * @param listener the ping listener to remove.
	 */
	public void removePingListener(IPing listener) {

		if ((listener == null) || (_listeners == null)) {
			return;
		}

		_listeners.remove(IPing.class, listener);
	}

	/**
	 * Add a ping listener.
	 * 
	 * @param listener the ping listener to add.
	 */
	public void addPingListener(IPing listener) {

		if (listener == null) {
			return;
		}

		if (_listeners == null) {
			_listeners = new EventListenerList();
		}

		_listeners.add(IPing.class, listener);
	}


}
