package cnuphys.ced.event.data;

import org.jlab.io.base.DataEvent;

public class Segments extends DetectorData {

	protected String _bankName;
	
	protected SegmentList _segments;
	
	public Segments(String bankName) {
		_bankName = bankName;
		_segments = new SegmentList(_bankName);
	}
	
	
	@Override
	public void newClasIoEvent(DataEvent event) {
		_segments =  new SegmentList(_bankName);
	}
	

	/**
	 * Get the segment list
	 * @return the segment list
	 */
	public SegmentList getSegments() {
		return _segments;
	}

}
