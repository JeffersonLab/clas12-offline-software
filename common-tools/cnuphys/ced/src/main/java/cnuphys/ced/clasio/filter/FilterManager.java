package cnuphys.ced.clasio.filter;

import java.util.ArrayList;

import org.jlab.io.base.DataEvent;

import cnuphys.ced.frame.Ced;

public class FilterManager extends ArrayList<IEventFilter> {
	
	
	// singleton
	private static FilterManager _instance;

	// the bank name
	private static String _bankName = "RUN::trigger";

	// private constructor for singleton
	private FilterManager() {
		//trigger filter added by trigger manager
		//add other standard filters
		add(new BankSizeFilter());
	}

	/**
	 * Public access to the FilterManager
	 * 
	 * @return the FilterManager singleton
	 */
	public static FilterManager getInstance() {
		if (_instance == null) {
			_instance = new FilterManager();
		}
		return _instance;
	}
	
	/**
	 * Check if there are any active filters
	 * 
	 * @return <code>true</code> if there are any active filters
	 */
	public boolean isFilteringOn() {
			for (IEventFilter filter : this) {
				if (filter.isActive()) {
					return true;
				}
		}
		return false;
	}
	

	/**
	 * Do this late in ced initialization
	 */
	public void setUpFilterMenu() {
		if (!isEmpty()) {
			for (IEventFilter filter : this) {
				Ced.getCed().getEventFilterMenu().add(filter.getMenuComponent());
			}
		}
	}

	
	/**
	 * Does the event pass all the active registered filters?
	 * @param event the event to check
	 * @return <code>true</code> if the event passes all the filters
	 */
	public boolean pass(DataEvent event) {
		
		if (!isEmpty()) {
			for(IEventFilter filter : this) {
				if (filter.isActive()) {
					boolean pass = filter.pass(event);
					
					if (!pass) {
						return false;
					}
				}
			}
		}
		return true;
	}


}
