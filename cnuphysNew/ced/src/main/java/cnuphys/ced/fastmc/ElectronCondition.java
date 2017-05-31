package cnuphys.ced.fastmc;

/**
 * A simple condition that we have certain number of unique layers hit (in the same sector) for 
 * an electron and a proton
 * @author heddle
 *
 */
public class ElectronCondition extends ACondition {
	
	private int _eLayers;
	
	public ElectronCondition(int eLayers, boolean active) {
		super(active);
		_eLayers = eLayers;
	}

	@Override
	public boolean pass() {
		if (!active) {
			return true;
		}
		return inNUniqueDCLayers(ELECTRON, _eLayers);
	}

	@Override
	public String getDescription() {
		return String.format("e- in %d layers", _eLayers);
	}


}
