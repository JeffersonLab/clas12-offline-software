package cnuphys.ced.trigger;

import org.jlab.io.base.DataEvent;

import cnuphys.ced.alldata.ColumnData;
import cnuphys.ced.alldata.DataManager;
import cnuphys.ced.clasio.AEventFilter;

public class TriggerFilter extends AEventFilter {


	
	//the bank name
	private static String _columnName = "RUN::trigger.trigger";
	
	//the actual bits
	private int _bits;
	
	//the type
	private TriggerMatch _type = TriggerMatch.ANY;
		
	public TriggerFilter() {
		super();
		setActive(false);
	}

	@Override
	public boolean pass(DataEvent event) {
		
		if (event == null) {
			return false;
		}
		
		ColumnData cd = DataManager.getInstance().getColumnData(_columnName);
		if (cd == null) {
			return false;
		}

		if (!event.hasBank(cd.getBankName())) {
			return false;
		}

		int triggerData[] = DataManager.getInstance().getIntArray(event, _columnName);

		if ((_columnName != null) && (triggerData.length > 0)) {
			System.err.println("TRIG FILTER CHECKING BITS: " + triggerData[0]);
		}

		int triggerWord = triggerData[0];

		switch (_type) {

		case EXACT:
			return (_bits == triggerWord);
			
		case ANY:
			return (_bits & triggerWord) != 0;

		case ALL:
			return ((_bits & triggerWord) == _bits);
		}

		return true;
	}
	
	@Override
	protected void toggleActiveState() {
		super.toggleActiveState();
		TriggerDialog.getInstance().getTriggerActiveCheckBox().setSelected(isActive());
	}


	public void setBits(int bits) {
//		System.err.println("Trigger Filter Bits Have Changed");
		_bits = bits;
	}

	public int getBits() {
		return _bits;
	}
	
	/**
	 * Set the type of trigger match
	 * @param type the trigger match
	 */
	public void setType(TriggerMatch type) {
		_type = type;
	}
	
	/**
	 * Get the pattern matching type
	 * @return the pattern matching type
	 */
	public TriggerMatch getType() {
		return _type;
	}
	
    /**
     * A builder for a PluginMessage
     */
    public static class Builder {
    	
    	private TriggerFilter _filter;
    	
    	public Builder() {
    		_filter = new TriggerFilter();
    	}
    	
    	public TriggerFilter build() {
    		return _filter;
    	}
    	
    	public Builder setBits(int bits) {
    		_filter.setBits(bits);
    		return this;
    	}
    	
    	public Builder setType(TriggerMatch type) {
    		_filter.setType(type);
    		return this;
    	}
    	
    	public Builder setActive(boolean active) {
    		_filter.setActive(active);
    		return this;
    	}
    	
    	public Builder setName(String name) {
    		_filter.setName(name);
    		return this;
    	}

    	
    }
    
    

}
