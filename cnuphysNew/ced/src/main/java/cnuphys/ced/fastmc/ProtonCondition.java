package cnuphys.ced.fastmc;

public class ProtonCondition extends ACondition {
	
	private int _pLayers;
	
	public ProtonCondition(int eLayers, boolean active) {
		super(active);
		_pLayers = eLayers;
	}

	@Override
	public boolean pass() {
		if (!active) {
			return true;
		}
		return inNUniqueDCLayers(PROTON, _pLayers);
	}

	@Override
	public String getDescription() {
		return String.format("p in %d layers", _pLayers);
	}

}
