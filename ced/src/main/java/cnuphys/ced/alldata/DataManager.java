package cnuphys.ced.alldata;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import org.jlab.clas.detector.DetectorResponse;
import org.jlab.detector.base.DetectorType;
import org.jlab.io.base.DataDescriptor;
import org.jlab.io.base.DataDictionary;
import org.jlab.io.base.DataEvent;
import org.jlab.io.hipo.HipoDataDictionary;

import cnuphys.bCNU.log.Log;

public class DataManager {
	
	//the data dictionary
	private DataDictionary _dictionary;
	
	//all the known banks
	private  String[] _knownBanks; 

	//the full set of column data. ALL columns for a bank name key
	//this maps bank name to a ColumnData object
	private Hashtable<String, ColumnData> _columnData;

	//singleton
	private static DataManager _instance;
	
	/**
	 * Create a DataManager
	 * @param dictionary
	 */
	private DataManager() {
		
		_dictionary = new HipoDataDictionary();
		_knownBanks = _dictionary.getDescriptorList();
		Arrays.sort(_knownBanks);
		
		initializeColumnData();
	}
	
	/**
	 * public access to singleton
	 * @return data manager singleton
	 */
	public static DataManager getInstance() {
		if (_instance == null) {
			_instance = new DataManager();
		}
		return _instance;
	}
	
	//initialize the column data
	private void initializeColumnData() {
		_columnData = new Hashtable<String, ColumnData>();
		
		
		Log.getInstance().info(
				"Initializing column data from discovered bank definitions.");

			if (_knownBanks != null) {
				
				for (String bankName : _knownBanks) {
					Log.getInstance().info(bankName);

					DataDescriptor dd = _dictionary.getDescriptor(bankName);

					String entries[] = dd.getEntryList();
					for (String columnName : entries) {
						int type = dd.getProperty("type", columnName);

						if ((type < 1) || (type > 6) || (type == 24)) {
							Log.getInstance()
									.warning("Bank: [" + bankName
											+ "] Column: [" + columnName
											+ "] bad data type in ColumnData initialization: ["
											+ type + "]");
						}
						else {
							ColumnData cd = new ColumnData(bankName, columnName,
									type);
							System.out.println(cd.toString());
							_columnData.put(cd.getFullName(), cd);
						}
					}
				}
			} // known banks not null
		Log.getInstance().info("Number of column definitions: " + _columnData.size());
	}
	
	/**
	 * Get the dictionary
	 * @return the dictionary
	 */
	public DataDictionary getDictionary() {
		return _dictionary;
	}
	
	/**
	 * Get the known banks
	 * @return the (sorted) known bank names
	 */
	public String[] getKnownBanks() {
		return _knownBanks;
	}
	
	/**
	 * Get a ColumnData
	 * @param bankName the bank name
	 * @param columnName the column data
	 * @return the ColumnData 
	 */
	public ColumnData getColumnData(String bankName, String columnName) {
		return _columnData.get(bankName + "." + columnName);
	}
	
	/**
	 * Get a ColumnData
	 * @param fullName the full name
	 * @return the ColumnData 
	 */
	public ColumnData getColumnData(String fullName) {
		return _columnData.get(fullName);
	}

	
	/**
	 * Obtain an byte array from the given event for the given full name
	 * @param event the given event
	 * @param fullName the full name 
	 * @return the array, or <code>null</code>
	 */
	public byte[] getByteArray(DataEvent event, String fullName) {
		byte[] array = null;
		ColumnData cd = getColumnData(fullName);
		if (cd == null) {
			//Log.getInstance().warning("In ColumnData.getByteArray, requested array for non-existent column: [" + fullName + "]");
		}
		else {
			Object o = cd.getDataArray(event);
			if (o != null) {
				if (o instanceof byte[]) {
					array = (byte[])o;
				}
				else {
					//Log.getInstance().warning("In ColumnData.getByteArray, requested array for non-matching column: [" + fullName + "]");
				}
			}
		}
		return array;
	}

	/**
	 * Obtain a short array from the given event for the given full name
	 * @param event the given event
	 * @param fullName the full name 
	 * @return the array, or <code>null</code>
	 */
	public short[] getShortArray(DataEvent event, String fullName) {
		short[] array = null;
		ColumnData cd = getColumnData(fullName);
		if (cd == null) {
			//Log.getInstance().warning("In ColumnData.getShortArray, requested array for non-existent column: [" + fullName + "]");
		}
		else {
			Object o = cd.getDataArray(event);
			if (o != null) {
				if (o instanceof short[]) {
					array = (short[])o;
				}
				else {
					//Log.getInstance().warning("In ColumnData.getShortArray, requested array for non-matching column: [" + fullName + "]");
				}
			}
		}
		return array;
	}

	/**
	 * Obtain an int array from the current event for the given full name
	 * @param event the given event
	 * @param fullName the full name 
	 * @return the array, or <code>null</code>
	 */
	public int[] getIntArray(DataEvent event, String fullName) {
		int[] array = null;
		ColumnData cd = getColumnData(fullName);
		if (cd == null) {
			//Log.getInstance().warning("In ColumnData.getIntArray, requested array for non-existent column: [" + fullName + "]");
		}
		else {
			Object o = cd.getDataArray(event);
			if (o != null) {
				if (o instanceof int[]) {
					array = (int[])o;
				}
				else {
					//Log.getInstance().warning("In ColumnData.getIntArray, requested array for non-matching column: [" + fullName + "]");
				}
			}
		}
		return array;
	}
	

	/**
	 * Obtain a float array from the current event for the given full name
	 * @param event the given event
	 * @param fullName the full name 
	 * @return the array, or <code>null</code>
	 */
	public float[] getFloatArray(DataEvent event, String fullName) {
		float[] array = null;
		ColumnData cd = getColumnData(fullName);
		if (cd == null) {
			//Log.getInstance().warning("In ColumnData.getFloatArray, requested array for non-existent column: [" + fullName + "]");
		}
		else {
			Object o = cd.getDataArray(event);
			if (o != null) {
				if (o instanceof float[]) {
					array = (float[])o;
				}
				else {
					//Log.getInstance().warning("In ColumnData.getFloatArray, requested array for non-matching column: [" + fullName + "]");
				}
			}
		}
		return array;
	}


	/**
	 * Obtain a double array from the current event for the given full name
	 * @param event the given event
	 * @param fullName the full name 
	 * @return the array, or <code>null</code>
	 */
	public double[] getDoubleArray(DataEvent event, String fullName) {
		double[] array = null;
		ColumnData cd = getColumnData(fullName);
		if (cd == null) {
			//Log.getInstance().warning("In ColumnData.getDoubleArray, requested array for non-existent column: [" + fullName + "]");
		}
		else {
			Object o = cd.getDataArray(event);
			if (o != null) {
				if (o instanceof double[]) {
					array = (double[])o;
				}
				else {
					//Log.getInstance().warning("In ColumnData.getDoubleArray, requested array for non-matching column: [" + fullName + "]");
				}
			}
		}
		return array;
	}
	
	/**
	 * (Approximate) test whether this is a valid column name. Doesn't test whether
	 * the column exists.
	 * @param name the name to test
	 * @return <code>true</code> if name is structured as a valid column name.
	 */
	public boolean validColumnName(String name) {
		return ((name != null) && (name.length() > 4) && name.contains(":") && name.contains("."));
	}
	
	/**
	 * Get a list of detector responses
	 * @param event the event
	 * @param bankName the bank name
	 * @param type the detector type
	 * @return a list of detector responses
	 */
	public List<DetectorResponse> getDetectorResponse(DataEvent event, String bankName, DetectorType type) {
		List<DetectorResponse> responses = DetectorResponse.readHipoEvent(event, bankName, type);
		return responses;
	}
}
