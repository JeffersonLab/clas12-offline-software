package cnuphys.fastMCed.streaming;

import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.event.EventListenerList;

import org.jlab.clas.physics.PhysicsEvent;

import cnuphys.bCNU.graphics.ImageManager;
import cnuphys.bCNU.view.ViewManager;
import cnuphys.fastMCed.fastmc.ParticleHits;

public class StreamManager {
	
	private static StreamManager instance;
	
	// list of listeners. 
	private EventListenerList _listeners = new EventListenerList();

	//the stream state
	private StreamReason _streamState = StreamReason.STOPPED;


	//private singleton constructor
	private StreamManager() {}
	
	public static StreamManager getInstance() {
		if (instance == null) {
			instance = new StreamManager();
		}
		return instance;
	}
	
	/**
	 * Remove a IStreamProcessor. IStreamProcessor listeners listen for
	 * new physics events.
	 * 
	 * @param listener
	 *            the IStreamProcessor listener to remove.
	 */
	public void removeStreamListener(IStreamProcessor listener) {

		if (listener == null) {
			return;
		}

		_listeners.remove(IStreamProcessor.class, listener);
	}
	
	/**
	 * Are we streaming
	 * @return <code>true</code> if we are streaming
	 */
	public boolean isStarted() {
		return _streamState == StreamReason.STARTED;
	}
	
	/**
	 * Are we paused
	 * @return <code>true</code> if we are paused
	 */
	public boolean isPaused() {
		return _streamState == StreamReason.PAUSED;
	}

	/**
	 * Are we stopped
	 * @return <code>true</code> if we are stopped
	 */
	public boolean isStopped() {
		return _streamState == StreamReason.STOPPED;
	}
	
	/**
	 * Notify the stream listeners of a physics event
	 * @param event the event
	 */
	public void notifyStreamListeners(PhysicsEvent event, List<ParticleHits> particleHits) {
		
		if (event == null) {
			return;
		}
		
		// Guaranteed to return a non-null array
		Object[] listeners = _listeners.getListenerList();


		// This weird loop is the bullet proof way of notifying all
		// listeners.
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			IStreamProcessor listener = (IStreamProcessor) listeners[i + 1];
			StreamProcessStatus status = listener.streamingPhysicsEvent(event, particleHits);
			
			if (status == StreamProcessStatus.FLAG) {

				StreamManager.getInstance().setStreamState(StreamReason.PAUSED);
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						JOptionPane.showMessageDialog(null, listener.flagExplanation(), 
								"A consumer has flagged this event",
								JOptionPane.INFORMATION_MESSAGE, ImageManager.cnuIcon);
						
						ViewManager.getInstance().refreshAllViews();
					}
				});
				
				return;

			}
		}
	}
	
	/**
	 * Notify the stream listeners of a change
	 * @param reason the reason for the change
	 */
	public void notifyStreamListeners(StreamReason reason) {
		
		// Guaranteed to return a non-null array
		Object[] listeners = _listeners.getListenerList();


		// This weird loop is the bullet proof way of notifying all
		// listeners.
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			IStreamProcessor listener = (IStreamProcessor) listeners[i + 1];
			listener.streamingChange(reason);
		}
	}



	/**
	 * Add a IStreamProcessor. IStreamProcessor listeners listen for new
	 * events.
	 * 
	 * @param listener
	 *            the IStreamProcessor listener to add.
	 * @param index
	 *            Determines gross notification order. Those in index 0 are
	 *            notified first. Then those in index 1. Finally those in index
	 *            2. The Data containers should be in index 0. The trajectory
	 *            and noise in index 1, and the regular views in index 2 (they
	 *            are notified last)
	 */
	public void addStreamListener(IStreamProcessor listener) {

		if (listener == null) {
			return;
		}

		_listeners.add(IStreamProcessor.class, listener);
		System.err.println("num stream listeners " + _listeners.getListenerCount());
	}
	
	/**
	 * Set the stream state
	 * @param state the stream state
	 */
	public void setStreamState(StreamReason state) {
		if (_streamState == state) {
			return;
		}
		_streamState = state;
		notifyStreamListeners(state);
	}
	
	/**
	 * Get the current state of streaming
	 * @return the current state
	 */
	public StreamReason getStreamState() {
		return _streamState;
	}


}
