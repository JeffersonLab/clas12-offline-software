package cnuphys.ced.clasio;

import org.jlab.io.base.DataEvent;
public interface IRunThrough {
	
	public void nextRunthroughEvent(DataEvent event);
	public void runThroughtDone();

}
