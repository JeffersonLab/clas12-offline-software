package cnuphys.ced.plugin;

import java.awt.geom.Rectangle2D.Double;

import org.jlab.evio.clas12.EvioDataEvent;

import cnuphys.bCNU.plugin.Plugin;
import cnuphys.bCNU.plugin.shapes.PluginShape;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.clasio.queue.ClasIoEventQueue;
import cnuphys.ced.clasio.queue.EventConsumer;
import cnuphys.ced.clasio.queue.EventProducer;
import cnuphys.ced.clasio.queue.IEventProcessor;
import cnuphys.ced.event.AccumulationManager;
import cnuphys.ced.event.IAccumulationListener;

public abstract class CedPlugin extends Plugin implements IAccumulationListener {

    // the event manager
    private final ClasIoEventManager _eventManager = ClasIoEventManager
	    .getInstance();

    public CedPlugin() {
	//create the event queue
	ClasIoEventQueue queue = new ClasIoEventQueue();
	
	//the producer is just a regular IClasIoEventListener attached to
	//the event manager that simply queues arriving events
	new EventProducer(queue);
	
	//the processor will, in a separate thread, dequeue the events
	IEventProcessor processor = new IEventProcessor() {

	    @Override
	    public void processEvent(EvioDataEvent event) {
		//tell the plugin to process
		processClasIoEvent(event, _eventManager.isAccumulating());
	    }
	    
	};
	
	new EventConsumer(queue, processor);
    }

    /**
     * Process the incoming event. This is already on a separate
     * thread.
     * @param event the event to process.
     * @param isAccumulating 
     */
    public abstract void processClasIoEvent(EvioDataEvent event, boolean isAccumulating);
    
    public static void main(String arg[]) {
	new CedPlugin() {

	    @Override
	    public void processClasIoEvent(EvioDataEvent event, boolean isAccumulating) {
	    }

	    @Override
	    public void initializePluginWorld(Double world) {
	    }

	    @Override
	    public void customizePlugin() {
	    }

	    @Override
	    public String getPluginTitle() {
		return null;
	    }

	    @Override
	    public void addInitialShapes() {
	    }

	    @Override
	    public void mouseOverShape(PluginShape shape) {
	    }

	    @Override
	    public void shapeClick(PluginShape shape, int clickCount) {
	    }

	    @Override
	    public void shapePopupTrigger(PluginShape shape) {
	    }
	    
	};
    }
    
    @Override
    public void accumulationEvent(int reason) {
	switch (reason) {
	case AccumulationManager.ACCUMULATION_STARTED:
	    break;

	case AccumulationManager.ACCUMULATION_CANCELLED:
	    break;

	case AccumulationManager.ACCUMULATION_FINISHED:
	    break;
	}
    }

}
