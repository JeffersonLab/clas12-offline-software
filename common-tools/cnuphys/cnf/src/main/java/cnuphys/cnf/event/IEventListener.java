package cnuphys.cnf.event;

import java.io.File;
import java.util.EventListener;

import org.jlab.io.base.DataEvent;

public interface IEventListener extends EventListener {
	/**
	 * a new event has arrived.
	 * 
	 * @param event the new event
	 * @param isStreaming <code>true</code> if this is during file streaming
	 */
	public void newEvent(final DataEvent event, boolean isStreaming);

	/**
	 * Opened a new event file
	 * 
	 * @param file the new file
	 */
	public void openedNewEventFile(File file);
	
	/**
	 * Rewound the current file
	 * @param file the file
	 */
	public void rewoundFile(File file);
	
	/**
	 * Streaming start message
	 * @param file file being streamed
	 * @param numToStream number that will be streamed
	 */
	public void streamingStarted(File file, int numToStream);
	
	/**
	 * Streaming ended message
	 * @param file the file that was streamed
	 * @param int the reason the streaming ended
	 */
	public void streamingEnded(File file, int reason);


}