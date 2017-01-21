package cnuphys.ced.event.data;

import org.jlab.io.base.DataEvent;

public class DCHits  extends DetectorData {

	protected String _bankName;
	
	protected DCHitList _hits;
	
	public DCHits(String bankName) {
		_bankName = bankName;
		_hits = new DCHitList(_bankName);
	}
	
	
	@Override
	public void newClasIoEvent(DataEvent event) {
		_hits =  new DCHitList(_bankName);
	}
	

	/**
	 * Get the cross list
	 * @return the cross list
	 */
	public DCHitList getHits() {
		return _hits;
	}

}