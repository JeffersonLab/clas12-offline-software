package cnuphys.ced.clasio;

public abstract class AEventFilter implements IEventFilter {
	
	//a name for the filter
	private String _name = "???";
	
	//the active flag
	private boolean _isActive;


	@Override
	public void setActive(boolean active) {
		_isActive = active;

	}

	@Override
	public boolean isActive() {
		return _isActive;
	}

	@Override
	public void setName(String name) {
		_name = name;
	}

	@Override
	public String getName() {
		return _name;
	}

}
