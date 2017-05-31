package cnuphys.ced.event.data;

import org.jlab.io.base.DataEvent;

public class Crosses  extends DetectorData {

	protected String _bankName;
	
	protected CrossList _crosses;
	
	public Crosses(String bankName) {
		_bankName = bankName;
		_crosses = new CrossList(_bankName);
	}
	
	
	@Override
	public void newClasIoEvent(DataEvent event) {
		_crosses =  new CrossList(_bankName);
	}
	

	/**
	 * Get the cross list
	 * @return the cross list
	 */
	public CrossList getCrosses() {
		return _crosses;
	}

}