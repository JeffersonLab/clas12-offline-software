package cnuphys.bCNU.showhide;

public class ShowHideString implements IShowHide {

	private boolean _visible = true;

	private String _string;

	public ShowHideString(String s) {
		_string = s;
	}

	@Override
	public void setVisible(boolean vis) {
		_visible = vis;
	}

	@Override
	public boolean isVisible() {
		return _visible;
	}

	@Override
	public String getName() {
		return _string;
	}

}
