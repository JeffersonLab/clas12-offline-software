package cnuphys.cnf.grid;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.jlab.io.base.DataEvent;

import cnuphys.cnf.event.EventManager;
import cnuphys.cnf.event.IEventListener;
import cnuphys.cnf.stream.StreamManager;

public class GridManager implements IEventListener {

	// grid related menu
	private JMenu _gridMenu;
	
	//gridify the data
	private JMenuItem _gridifyItem;

	//the singleton
	private static GridManager _instance;
	
	//private constructor
	private GridManager() {
		EventManager.getInstance().addEventListener(this, 2);
	}

	/**
	 * public access to the singleton
	 * 
	 * @return the GridManager
	 */
	public static GridManager getInstance() {
		if (_instance == null) {
			_instance = new GridManager();
		}

		return _instance;
	}
	
	//fix the menu state
	private void fixState() {
		
	}

	/**
	 * Get the grid related menu
	 * @return the grid menu
	 */
	public JMenu getGridMenu() {
		if (_gridMenu == null) {
			_gridMenu = new JMenu("Grid");
			_gridifyItem = new JMenuItem("Gridify current data");
			_gridifyItem.addActionListener(event->gridify());
			_gridMenu.add(_gridifyItem);
		}
		
		fixState();
		return _gridMenu;
	}
	
	//gridify any data in memory
	private void gridify() {
	}
	
	/**
	 * Check whether the data in memory is gridable.
	 * @return true if the data in memory is gridable.
	 */
	public boolean isGridable() {
		
		ArrayList<float[]> data = StreamManager.getInstance().getData();
		
		if ((data == null) || data.isEmpty()) {
			return false;
		}
		
		ArrayList<Float> uniqueX = new ArrayList<>();
		ArrayList<Float> uniqueY = new ArrayList<>();
		ArrayList<Float> uniqueZ = new ArrayList<>();

		for (float[] row : data) {
			
			float x = row[StreamManager.X];
			float y = row[StreamManager.Y];
			float z = row[StreamManager.Z];
			
			uniqueX.remove(x);
			uniqueX.add(x);
			
			uniqueY.remove(y);
			uniqueY.add(y);

			uniqueZ.remove(z);
			uniqueZ.add(z);

		}
		
		Collections.sort(uniqueX);
		Collections.sort(uniqueY);
		Collections.sort(uniqueZ);
		
		System.err.println("\nNumber of unique X: " + uniqueX.size());
		for (float x : uniqueX) {
			System.err.println(String.format("%7.4f", x));
		}
		
		System.err.println("\nNumber of unique Y: " + uniqueY.size());
		for (float y : uniqueY) {
			System.err.println(String.format("%7.4f", y));
		}

		System.err.println("\nNumber of unique Z: " + uniqueZ.size());
		for (float z : uniqueZ) {
			System.err.println(String.format("%7.4f", z));
		}

		
		return true;
	}

	@Override
	public void newEvent(DataEvent event, boolean isStreaming) {
		// TODO Auto-generated method stub

	}

	@Override
	public void openedNewEventFile(File file) {
		// TODO Auto-generated method stub

	}

	@Override
	public void rewoundFile(File file) {
		// TODO Auto-generated method stub

	}

	@Override
	public void streamingStarted(File file, int numToStream) {
		// TODO Auto-generated method stub

	}

	@Override
	public void streamingEnded(File file, int reason) {
		// TODO Auto-generated method stub

	}
}
