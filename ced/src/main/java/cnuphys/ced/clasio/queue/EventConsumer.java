package cnuphys.ced.clasio.queue;

import org.jlab.io.base.DataEvent;

public class EventConsumer {

    public EventConsumer(final ClasIoEventQueue queue,
	    final IEventProcessor processor) {

	Runnable dequerer = new Runnable() {

	    @Override
	    public void run() {
		while (true) {
		    // the dequeue method "waits" so no thread yielding is
		    // necessary
		    DataEvent event = queue.dequeue();
		    processor.processEvent(event);
		}
	    }
	};
	
	Thread t = new Thread(dequerer);
	t.start();
    }
}
