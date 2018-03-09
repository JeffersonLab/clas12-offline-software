package cnuphys.ced.trigger;

import org.jlab.io.base.DataEvent;

import cnuphys.ced.alldata.ColumnData;
import cnuphys.ced.alldata.DataManager;
import cnuphys.ced.clasio.AEventFilter;

public class TriggerFilter extends AEventFilter {

	public enum TRIG_FILT_TYPE {EXACT, OR, AND};

	
	//the bank name
	private static String _columnName = "RUN::trigger.trigger";
	
	//the actual bits
	private int _bits;
	
	//the type
	private TRIG_FILT_TYPE _type = TRIG_FILT_TYPE.OR;
	
	public TriggerFilter() {
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

		int tbits = triggerData[0];

		switch (_type) {

		case EXACT:
			return (_bits == tbits);
			
		case OR:
			return (_bits | tbits) != 0;

		case AND:
			return ((_bits & tbits) == _bits);
		}

		return true;
	}

	public void setBits(int bits) {
		_bits = bits;
	}

	public int getBits() {
		return _bits;
	}
	
	public void setType(TRIG_FILT_TYPE type) {
		_type = type;
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
    	
    	public Builder setType(TRIG_FILT_TYPE type) {
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
