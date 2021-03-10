package cnuphys.cnf.stream;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.jlab.io.base.DataEvent;

import cnuphys.cnf.alldata.ColumnData;
import cnuphys.cnf.alldata.DataManager;
import cnuphys.cnf.event.EventManager;
import cnuphys.cnf.event.IEventListener;
import cnuphys.cnf.grid.GridManager;


public class StreamManager implements IEventListener {
	
	//small diff tester
	private static final float TINY = (float) 1.0e-5;

	
	// indices
	public static final int X = 0;
	public static final int Y = 1;
	public static final int Z = 2;
	public static final int BX = 3;
	public static final int BY = 4;
	public static final int BZ = 5;
	public static final int DEL = 6;
	public static final int R = 7;
	public static final int BMAG = 8;

	
	//the singleton
	private static StreamManager _instance;
	
	// just counts "events"
	private int _totalCount;
	private DataRanges _dataRanges = new DataRanges();

	// data related
	
	ColumnData cd_x;
	ColumnData cd_y;
	ColumnData cd_z;
	ColumnData cd_Bx;
	ColumnData cd_By;
	ColumnData cd_Bz;
	ColumnData cd_Del;
	
	//the actual data in memory
	private ArrayList<float[]> _data;
	
	//private constructor
	private StreamManager() {
		EventManager.getInstance().addEventListener(this, 2);
		_data = new ArrayList<>();
		initializeColumnData();
	}

	/**
	 * public access to the singleton
	 * 
	 * @return the StreamManager
	 */
	public static StreamManager getInstance() {
		if (_instance == null) {
			_instance = new StreamManager();
		}

		return _instance;
	}
	
	
	/**
	 * Get the ranges for the in-memory data
	 * @return the ranges for the in memory data
	 */
	public DataRanges getDataRanges() {
		return _dataRanges;
	}
	
	
	/**
	 * Clear all in memory data
	 */
	public void clear() {
		_data.clear();
		_dataRanges = new DataRanges();
	}


	// initialize the column data
	private void initializeColumnData() {
		
		cd_x = DataManager.getInstance().getColumnData("CNF::nucleon_map.x");
		cd_y = DataManager.getInstance().getColumnData("CNF::nucleon_map.y");
		cd_z = DataManager.getInstance().getColumnData("CNF::nucleon_map.z");
		cd_Bx = DataManager.getInstance().getColumnData("CNF::nucleon_map.Bx");
		cd_By = DataManager.getInstance().getColumnData("CNF::nucleon_map.By");
		cd_Bz = DataManager.getInstance().getColumnData("CNF::nucleon_map.Bz");
		cd_Del = DataManager.getInstance().getColumnData("CNF::nucleon_map.Del");
	}

	
	/**
	 * Get some data in memory, Returned as a hash table where
	 * the key is a column name
	 * @return in memory data for a given column
	 */
	public ArrayList<float[]> getData() {
		return _data;
	}
	
	/**
	 * Get the number of data rows
	 * @return the number of data rows
	 */
	public int count() {
		return _data.size();
	}

	/**
	 * Get a row of data
	 * @param index the row index
	 * @param array will be filled with the data
	 */
	public float[] getDataRow(int index) {
		return _data.get(index);
	}
	
	@Override
	public void newEvent(DataEvent event, boolean isStreaming) {

		_totalCount++;

		if ((_totalCount % 1000) == 0) {
			System.err.println("Data read count: " + _totalCount);
		}
		
		double x[] = cd_x.getAsDoubleArray(event);
		if ((x == null) || (x.length < 1)) {
			System.err.println("No data in the event");
			return;
		}
		
		double y[] = cd_y.getAsDoubleArray(event);
		double z[] = cd_z.getAsDoubleArray(event);		
		double bx[] = cd_Bx.getAsDoubleArray(event);
		double by[] = cd_By.getAsDoubleArray(event);
		double bz[] = cd_Bz.getAsDoubleArray(event);
		double del[] = cd_Del.getAsDoubleArray(event);
		
		for (int i = 0; i < x.length; i++) {
			double r = Math.sqrt(x[i] * x[i] + y[i] * y[i] + z[i] * z[i]);
			double b = Math.sqrt(bx[i] * bx[i] + by[i] * by[i] + bz[i] * bz[i]);
			float[] newRow = { (float) x[i], (float) y[i], (float) z[i], 
					(float) bx[i], (float) by[i], (float) bz[i], 
					(float)del[i], (float) r, (float) b };
			_data.add(newRow);
		}

	}

	@Override
	public void openedNewEventFile(File file) {
		System.err.println("Opened new event file [" + file.getPath() + "]");
		_totalCount = 0;
	}

	@Override
	public void rewoundFile(File file) {
		System.err.println("Rewound [" + file.getPath() + "]");
		_totalCount = 0;
	}

	@Override
	public void streamingStarted(File file, int numToStream) {
		System.err.println("Streaming Started [" + file.getPath() + "] num: " + numToStream);
	}

	// are two values essentially the same
	private boolean sameValue(float v1, float v2) {
		return Math.abs(v1 - v2) < TINY;
	}

	@Override
	public void streamingEnded(File file, int reason) {
		System.err.println(
				"Streaming Ended [" + file.getPath() + "] reason: " + ((reason == 0) ? "completed" : "interrupted"));

		System.err.println(String.format("table has %d values", _data.size()));
		
		//sort for potential gridding
		Comparator<float[]> comp = new Comparator<float[]>() {

			@Override
			public int compare(float[] o1, float[] o2) {
				
				
				if (sameValue(o1[0], o2[0])) {
					if (sameValue(o1[1], o2[1])) {
						if (sameValue(o1[2], o2[2])) {
							return 0;
						}
						if (o1[2] < o2[2]) {
							return -1;
						}
						else {
							return 1;
						}
					
					}
					if (o1[1] < o2[1]) {
						return -1;
					}
					else {
						return 1;
					}
					
				}
				if (o1[0] < o2[0]) {
					return -1;
				}
				else {
					return 1;
				}
			}
		};

		Collections.sort(_data, comp);
		System.err.println("data sorted in griddable manner.");
		//update the data range
		for (float[] array : _data) {
			_dataRanges.newValue(array);
		}

		System.err.println(_dataRanges.toString());
		
		//is it gridable?
		
		System.err.println("Data is gridable: " + GridManager.getInstance().isGridable());
	}

}
