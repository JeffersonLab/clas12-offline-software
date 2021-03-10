package cnuphys.ced.cedview;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;


public class TimedRefreshManager {

	//views that have pending refreshes
	private ArrayList<CedView> _pendingRefresh = new ArrayList<>();
	
	//time that fires the refresh
	private Timer _timer;
	
	//the singleton
	private static TimedRefreshManager _instance;
	
	private TimedRefreshManager() {
		TimerTask task = new TimerTask() {

			
			@Override
			public void run() {
				timerFired();
			}

		};
		_timer = new Timer();
		_timer.scheduleAtFixedRate(task, 3000, 1000);
	}
	
	/**
	 * Public access to the singleton
	 * @return the one and only TimerRefreshManager
	 */
	public static TimedRefreshManager getInstance() {
		
		if (_instance == null) {
			_instance = new TimedRefreshManager();
		}
		return _instance;

	}
	
	/**
	 * Add a view to the list of views waiting to be refreshed.
	 * @param view the view to add
	 */
	public void add(CedView view) {
		if (view != null) {
			if (!_pendingRefresh.contains(view)) {
				_pendingRefresh.add(view);
			}
		}
	}
	
	//the timer has fired
	private void timerFired() {
		
		ArrayList<CedView> tempList = _pendingRefresh;
		_pendingRefresh = new ArrayList<>();
		
		for (CedView view : tempList) {
			view.refresh();
		}
		tempList = null;
	}
}
