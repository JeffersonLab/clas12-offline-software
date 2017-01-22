package cnuphys.ced.event.data;

import org.jlab.io.base.DataEvent;

public class TbHbHits  extends DetectorData {

	protected String _bankName;
	
	protected TbHbHitList _hits;
	
	public TbHbHits(String bankName) {
		_bankName = bankName;
		_hits = new TbHbHitList(_bankName);
	}
	
	
	@Override
	public void newClasIoEvent(DataEvent event) {
		_hits =  new TbHbHitList(_bankName);
	}
	

	/**
	 * Get the cross list
	 * @return the cross list
	 */
	public TbHbHitList getHits() {
		return _hits;
	}

}