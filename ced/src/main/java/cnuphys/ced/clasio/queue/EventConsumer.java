package cnuphys.ced.clasio.queue;

import org.jlab.evio.clas12.EvioDataEvent;

public class EventConsumer {

    public EventConsumer(final ClasIoEventQueue queue,
	    final IEventProcessor processor) {

	Runnable dequerer = new Runnable() {

	    @Override
	    public void run() {
		while (true) {
		    // the dequeue method "waits" so no thread yielding is
		    // necessary
		    EvioDataEvent event = queue.dequeue();
		    processor.processEvent(event);
		}
	    }
	};
	
	Thread t = new Thread(dequerer);
	t.start();
    }
}
