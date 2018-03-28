package cnuphys.ced.trigger;

public class Trigger {
	
	//the trigger bits
	private int _trigger;
	
	//the trigger name;
	private String _name;
	
	/**
	 * Create a trigger pattern
	 * @param name the namne of the pattern
	 * @param trigger the trigger bits
	 */
	public Trigger(String name, int trigger) {
		setName(name);
		setTrigger(trigger);
	}
	
	/**
	 * Set the trigger bits
	 * @param trigger the trigger bits
	 */
	public void setTrigger(int trigger) {
		_trigger = trigger;
	}
	
	/**
	 * Get the trigger bits
	 * @return the trigger bits
	 */
	public int getTrigger() {
		return _trigger;
	}
	
	/**
	 * Set the name of the trigger
	 * @param name the name of the trigger
	 */
	public void setName(String name) {
		_name = (name != null) ? name : "???";
	}
	
	/**
	 * Get the name of the trigger
	 * @return the name of the trigger
	 */
	public String getName() {
		return _name;
	}

}
