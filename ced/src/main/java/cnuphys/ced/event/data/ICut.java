package cnuphys.ced.event.data;

public interface ICut {

	public boolean pass(double val);
	
	public boolean pass(int index);
	
	public void setActive(boolean active);
	
	public boolean isActive();
	
	public String getName();

	public String getDefinition();
	
	public String getCutType();
}
