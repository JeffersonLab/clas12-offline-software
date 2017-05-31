package cnuphys.ced.event.data;

import org.jlab.io.base.DataEvent;

public class Crosses2  extends DetectorData {

	protected String _bankName;
	
	protected CrossList2 _crosses;
	
	public Crosses2(String bankName) {
		_bankName = bankName;
		_crosses = new CrossList2(_bankName);
	}
	
	
	@Override
	public void newClasIoEvent(DataEvent event) {
		_crosses =  new CrossList2(_bankName);
	}
	

	/**
	 * Get the cross list
	 * @return the cross list
	 */
	public CrossList2 getCrosses() {
		return _crosses;
	}

}