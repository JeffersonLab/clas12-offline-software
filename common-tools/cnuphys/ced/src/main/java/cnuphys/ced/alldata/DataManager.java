package cnuphys.ced.alldata;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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

	// EXCLUSION LIST
	private String _exclusions[] = null;

	// the data dictionary
	private DataDictionary _dictionary;

	// all the known banks
	private String[] _knownBanks;

	// the full set of column data. ALL columns for a full bank name key
	// key is something like DET::NAME.COLUMN e.g.
	// DC::dgtz.doca,
	// or
	// HitBasedTrkg::HBTracks.Cross1_ID
	// this maps bank name to a ColumnData object
	private Hashtable<String, ColumnData> _columnData;

	// singleton
	private static DataManager _instance;

	/**
	 * Create a DataManager
	 * 
	 * @param dictionary
	 */
	private DataManager() {

		System.out.println("DICTIONARY");
		_dictionary = new HipoDataDictionary();

		// HACK filter out dgtz banks
		String allBanks[] = _dictionary.getDescriptorList();
		ArrayList<String> okbanks = new ArrayList<String>();
		for (String s : allBanks) {
			// check for exclusions
			if (include(s)) {
				okbanks.add(s);
			}
		}
		_knownBanks = new String[okbanks.size()];
		for (int i = 0; i < okbanks.size(); i++) {
			_knownBanks[i] = okbanks.get(i);
		}

		// _knownBanks = _dictionary.getDescriptorList();
		Arrays.sort(_knownBanks);

		initializeColumnData();
	}

	// check exclusions
	private boolean include(String bankName) {
		if ((_exclusions != null) && (_exclusions.length > 0)) {
			for (String es : _exclusions) {
				if (bankName.contains(es)) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Get the collection of recognized columns
	 * 
	 * @return the collection of recognized columns
	 */
	public ArrayList<ColumnData> getColumnData() {
		if (_columnData == null) {
			return null;
		}
		if (_columnData.size() < 1) {
			return null;
		}
		ArrayList<ColumnData> columns = new ArrayList<ColumnData>();
		for (ColumnData cd : _columnData.values()) {
			columns.add(cd);
		}
		Collections.sort(columns);
		return columns;
	}

	/**
	 * public access to singleton
	 * 
	 * @return data manager singleton
	 */
	public static DataManager getInstance() {
		if (_instance == null) {
			_instance = new DataManager();
		}
		return _instance;
	}

	/**
	 * Get a list of all column data objects that have data in the given event for a
	 * specific bank
	 * 
	 * @param event    the event in question
	 * @param bankName the bank
	 * @return a list of all columns in the given bank with data
	 */

	public ArrayList<ColumnData> hasData(DataEvent event, String bankName) {
		ArrayList<ColumnData> list = new ArrayList<ColumnData>();

		String columns[] = event.getColumnList(bankName);
		if (columns != null) {
			for (String columnName : columns) {
				list.add(getColumnData(bankName, columnName));
			}
		}

		return list;

	}

	/**
	 * Get a list of all column data objects that have data in the given event
	 * 
	 * @param event the event in question
	 * @return a list of all columns in all banks with data
	 */
	public ArrayList<ColumnData> hasData(DataEvent event) {
		ArrayList<ColumnData> list = new ArrayList<ColumnData>();

		String banks[] = event.getBankList();
		if (banks != null) {
			for (String bankName : banks) {
				String columns[] = event.getColumnList(bankName);
				if (columns != null) {
					for (String columnName : columns) {
						ColumnData cd = getColumnData(bankName, columnName);
						if (cd == null) {
							Log.getInstance().warning("Dictionary does not seem to know about bank named [" + bankName
									+ "." + columnName + "] May be a disconnect with json files");
						} else {
							list.add(cd);
						}
					}
				}
			}
		}

		Collections.sort(list);

		String bankName = "";
		int bankIndex = 1;
		for (ColumnData cd : list) {
			if (!cd.getBankName().equals(bankName)) {
				bankIndex++;
				bankName = cd.getBankName();
			}
			cd.bankIndex = bankIndex;
		}
		return list;
	}

	/**
	 * Get the number of rows (length) of a given bank
	 * @param event the event
	 * @param bankName the bank name
	 * @return the number of rows
	 */
	public int getRowCount(DataEvent event, String bankName) {
		if (bankName == null) {
			return 0;
		}
		String colNames[] = getColumnNames(bankName);
		
		if ((colNames == null) || (colNames.length < 1)) {
			return 0;
		}
		
		//uses the 1st column, assumes all columns have the same length
		ColumnData cd = getColumnData(bankName, colNames[0]);
		
		if (cd == null) {
			return 0;
		}
		
		return cd.getLength(event);
	}
	
	/**
	 * Get the list of column names for a bank name
	 * 
	 * @param bankName the bank name
	 * @return the list of column names
	 */
	public String[] getColumnNames(String bankName) {
		DataDescriptor dd = _dictionary.getDescriptor(bankName);
		return dd.getEntryList();
	}

	// initialize the column data
	private void initializeColumnData() {
		_columnData = new Hashtable<String, ColumnData>();

		Log.getInstance().info("Initializing column data from discovered bank definitions.");

		if (_knownBanks != null) {

			for (String bankName : _knownBanks) {
				Log.getInstance().info(bankName);

				DataDescriptor dd = _dictionary.getDescriptor(bankName);
				
				String entries[] = dd.getEntryList();
				for (String columnName : entries) {
					int type = dd.getProperty("type", columnName);


					if ((type < 1) || (type > 6) || (type == 24)) {
//							Log.getInstance()
//									.warning("Bank: [" + bankName
//											+ "] Column: [" + columnName
//											+ "] bad data type in ColumnData initialization: ["
//											+ type + "]");
					} else {
						ColumnData cd = new ColumnData(bankName, columnName, type);
						_columnData.put(cd.getFullName(), cd);
					}
				}
			} // bank names
		} // known banks not null
	}

	/**
	 * Get the dictionary
	 * 
	 * @return the dictionary
	 */
	public DataDictionary getDictionary() {
		return _dictionary;
	}

	/**
	 * Get the known banks
	 * 
	 * @return the (sorted) known bank names
	 */
	public String[] getKnownBanks() {
		return _knownBanks;
	}

	/**
	 * Get a ColumnData
	 * 
	 * @param bankName   the bank name
	 * @param columnName the column data
	 * @return the ColumnData
	 */
	public ColumnData getColumnData(String bankName, String columnName) {
		return _columnData.get(bankName + "." + columnName);
	}

	/**
	 * Get a ColumnData
	 * 
	 * @param fullName the full name
	 * @return the ColumnData
	 */
	public ColumnData getColumnData(String fullName) {
		return _columnData.get(fullName);
	}

	/**
	 * Obtain an byte array from the given event for the given full name
	 * 
	 * @param event    the given event
	 * @param fullName the full name
	 * @return the array, or <code>null</code>
	 */
	public byte[] getByteArray(DataEvent event, String fullName) {
		ColumnData cd = getColumnData(fullName);
		return (cd == null) ? null : cd.getByteArray(event);
	}

	/**
	 * Obtain a short array from the given event for the given full name
	 * 
	 * @param event    the given event
	 * @param fullName the full name
	 * @return the array, or <code>null</code>
	 */
	public short[] getShortArray(DataEvent event, String fullName) {
		ColumnData cd = getColumnData(fullName);
		return (cd == null) ? null : cd.getShortArray(event);
	}

	/**
	 * Obtain an int array from the current event for the given full name
	 * 
	 * @param event    the given event
	 * @param fullName the full name
	 * @return the array, or <code>null</code>
	 */
	public int[] getIntArray(DataEvent event, String fullName) {
		ColumnData cd = getColumnData(fullName);
		return (cd == null) ? null : cd.getIntArray(event);
	}

	/**
	 * Obtain a long array from the current event for the given full name
	 * 
	 * @param event    the given event
	 * @param fullName the full name
	 * @return the array, or <code>null</code>
	 */
	public long[] getLongArray(DataEvent event, String fullName) {
		ColumnData cd = getColumnData(fullName);
		return (cd == null) ? null : cd.getLongArray(event);
	}

	/**
	 * Obtain a float array from the current event for the given full name
	 * 
	 * @param event    the given event
	 * @param fullName the full name
	 * @return the array, or <code>null</code>
	 */
	public float[] getFloatArray(DataEvent event, String fullName) {
		ColumnData cd = getColumnData(fullName);
		return (cd == null) ? null : cd.getFloatArray(event);
	}

	/**
	 * Obtain a double array from the current event for the given full name
	 * 
	 * @param event    the given event
	 * @param fullName the full name
	 * @return the array, or <code>null</code>
	 */
	public double[] getDoubleArray(DataEvent event, String fullName) {
		ColumnData cd = getColumnData(fullName);
		return (cd == null) ? null : cd.getDoubleArray(event);
	}

	/**
	 * (Approximate) test whether this is a valid column name. Doesn't test whether
	 * the column exists.
	 * 
	 * @param name the name to test
	 * @return <code>true</code> if name is structured as a valid column name.
	 */
	public boolean validColumnName(String name) {
		return ((name != null) && (name.length() > 4) && name.contains(":") && name.contains("."));
	}

	/**
	 * Get a list of detector responses
	 * 
	 * @param event    the event
	 * @param bankName the bank name
	 * @param type     the detector type
	 * @return a list of detector responses
	 */
	public List<DetectorResponse> getDetectorResponse(DataEvent event, String bankName, DetectorType type) {

		// TODO remove aftergagik fixes
		// if (true) return null;

		if (event == null) {
			return null;
		}

		List<DetectorResponse> responses = null;
		if (event.hasBank(bankName)) {
			System.err.println("Event is null: " + (event == null));
			System.err.println("bankName: [" + bankName + "]");
			System.err.println("event has bank: " + event.hasBank(bankName));
			System.err.println("detector type: [" + type + "]");
			responses = DetectorResponse.readHipoEvent(event, bankName, type);
		}
		return responses;
	}
}
