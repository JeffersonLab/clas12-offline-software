package cnuphys.ced.event.data;

import java.util.Arrays;
import java.util.Hashtable;

import org.jlab.data.io.DataDescriptor;
import org.jlab.evio.clas12.EvioDataDictionary;
import org.jlab.evio.clas12.EvioDataEvent;
import org.jlab.evio.clas12.EvioFactory;

import cnuphys.bCNU.log.Log;
import cnuphys.ced.clasio.ClasIoEventManager;

public class ColumnData {

	/** type is */
	public static final int UNKNOWN = 0;

	/** type is a byte */
	public static final int INT8 = 1;

	/** type is a short */
	public static final int INT16 = 2;

	/** type is an int */
	public static final int INT32 = 3;

	/** type is a float */
	public static final int FLOAT32 = 5;

	/** type is a double */
	public static final int FLOAT64 = 6;

	// the bank name
	private String _bankName;

	// the column name
	private String _columnName;

	// the data type
	private int _type;

	// the full name
	private String _fullName;
	
	//the full set of column data
	private static Hashtable<String, ColumnData> _columnData;

	public static void intitialize() {
		_columnData = new Hashtable<String, ColumnData>();
		Log.getInstance().info(
				"Initializing column data from discovered bank definitions.");
		EvioDataDictionary dataDict = EvioFactory.getDictionary();
		if (dataDict != null) {
			String knownBanks[] = dataDict.getDescriptorList();

			if (knownBanks != null) {
				
				int count = 0;
				
				Arrays.sort(knownBanks);
				for (String bankName : knownBanks) {
					Log.getInstance().info(bankName);

					DataDescriptor dd = dataDict.getDescriptor(bankName);

					String entries[] = dd.getEntryList();
					for (String columnName : entries) {
						int type = dd.getProperty("type", columnName);

						if ((type < 1) || (type > 6) || (type == 4)) {
							Log.getInstance()
									.warning("Bank: [" + bankName
											+ "] Column: [" + columnName
											+ "] bad data type in ColumnData initialization: ["
											+ type + "]");
						}
						else {
							ColumnData cd = new ColumnData(bankName, columnName,
									type);
							
							count++;
							Log.getInstance().info(count + "  " + cd);
							_columnData.put(cd._fullName, cd);
						}
					}
				}
			} // known banks not null
		}
		Log.getInstance().info("Number of column definitions: " + _columnData.size());
	}
	
	/**
	 * Holds the data for one column, one event
	 * 
	 * @param bankName the bank name
	 * @param columnName the column name
	 * @param type the data type (one of the class constants)
	 */
	private ColumnData(String bankName, String columnName, int type) {
		_bankName = bankName;
		_columnName = columnName;
		_fullName = bankName + "." + columnName;

		if ((type < 1) || (type > 6) || (type == 4)) {
			Log.getInstance()
					.warning("Bank: [" + _bankName + "] Column: [" + columnName
							+ "] bad data type in ColumnData constructor: ["
							+ type + "]");
			type = 0;
		}
		_type = type;
	}
	
	/**
	 * Get a ColumnData
	 * @param bankName the bank name
	 * @param columnName the column data
	 * @return the ColumnData 
	 */
	public static ColumnData getColumnData(String bankName, String columnName) {
		return _columnData.get(bankName + "." + columnName);
	}
	
	/**
	 * Get a ColumnData
	 * @param fullName the full name
	 * @return the ColumnData 
	 */
	public static ColumnData getColumnData(String fullName) {
		return _columnData.get(fullName);
	}


	/**
	 * Get the data array as an object. It is up tp the caller to cast it to the
	 * correct type of array.
	 * 
	 * @return the data array corresponding to the type
	 */
	public Object getDataArray() {

		Object oa = null;
		
		EvioDataEvent event = ClasIoEventManager.getInstance().getCurrentEvent();

		if (event != null) {
			switch (_type) {
			case INT8:
				oa = event.getByte(_fullName);
				break;

			case INT16:
				oa = event.getShort(_fullName);
				break;

			case INT32:
				oa = event.getInt(_fullName);
				break;

			case FLOAT32:
				oa = event.getFloat(_fullName);
				break;

			case FLOAT64:
				oa = event.getDouble(_fullName);
				break;
			}
		}

		return oa;
	}
	
	/**
	 * Get the array with double values regardless of type
	 * @return the data as a double array
	 */
	public double[] getAsDoubleArray() {
		double da[] = null;
		
		EvioDataEvent event = ClasIoEventManager.getInstance().getCurrentEvent();

		if (event != null) {
			switch (_type) {
			case INT8:
				byte b[] = event.getByte(_fullName);
				int len = (b == null) ? 0 : b.length;
				if (len > 0) {
					da = new double[len];
					for (int j = 0; j < len; j++) {
						da[j] = b[j];
					}
				}
				break;

			case INT16:
				short s[] = event.getShort(_fullName);
				len = (s == null) ? 0 : s.length;
				if (len > 0) {
					da = new double[len];
					for (int j = 0; j < len; j++) {
						da[j] = s[j];
					}
				}
				break;

			case INT32:
				int i[] = event.getInt(_fullName);
				len = (i == null) ? 0 : i.length;
				if (len > 0) {
					da = new double[len];
					for (int j = 0; j < len; j++) {
						da[j] = i[j];
					}
				}
				break;

			case FLOAT32:
				float f[] = event.getFloat(_fullName);
				len = (f == null) ? 0 : f.length;
				if (len > 0) {
					da = new double[len];
					for (int j = 0; j < len; j++) {
						da[j] = f[j];
					}
				}
				break;

			case FLOAT64:
				da = event.getDouble(_fullName);
				break;
			}
		}

		return da;
	}
	
	/**
	 * Get the length of the underlying data array
	 * @return the length of the underlying data array
	 */
	public int length() {
		
		int len = 0;
		Object oa = getDataArray();
		
		if (oa != null) {
			switch (_type) {
			case INT8:
				len = ((byte[]) oa).length;
				break;

			case INT16:
				len = ((short[]) oa).length;
				break;

			case INT32:
				len = ((int[]) oa).length;
				break;

			case FLOAT32:
				len = ((float[]) oa).length;
				break;

			case FLOAT64:
				len = ((double[]) oa).length;
				break;
			}
		}
		
		return len;
	}

	/**
	 * Get the bank name
	 * 
	 * @return the bank name
	 */
	public String getBankName() {
		return _bankName;
	}

	/**
	 * Get the column name
	 * 
	 * @return column name
	 */
	public String getColumnName() {
		return _columnName;
	}

	/**
	 * get the full name
	 * 
	 * @return the full name
	 */
	public String getFullName() {
		return _fullName;
	}

	/**
	 * Get the data type
	 * 
	 * @return the data type [0..6] (0 is error)
	 */
	public int getType() {
		return _type;
	}
	
	@Override
	public String toString() {
		return _fullName + "  type: " + _type;
	}
	
	/**
	 * Obtain an byte array from the current event for the given full name
	 * @param fullName the full name 
	 * @return the array, or <code>null</code>
	 */
	public static byte[] getByteArray(String fullName) {
		byte[] array = null;
		ColumnData cd = getColumnData(fullName);
		if (cd == null) {
			Log.getInstance().warning("In ColumnData.getByteArray, requested array for non-existent column: [" + fullName + "]");
		}
		else {
			Object o = cd.getDataArray();
			if (o != null) {
				if (o instanceof byte[]) {
					array = (byte[])o;
				}
				else {
					Log.getInstance().warning("In ColumnData.getByteArray, requested array for non-matching column: [" + fullName + "]");
				}
			}
		}
		return array;
	}

	/**
	 * Obtain a short array from the current event for the given full name
	 * @param fullName the full name 
	 * @return the array, or <code>null</code>
	 */
	public static short[] getShortArray(String fullName) {
		short[] array = null;
		ColumnData cd = getColumnData(fullName);
		if (cd == null) {
			Log.getInstance().warning("In ColumnData.getShortArray, requested array for non-existent column: [" + fullName + "]");
		}
		else {
			Object o = cd.getDataArray();
			if (o != null) {
				if (o instanceof short[]) {
					array = (short[])o;
				}
				else {
					Log.getInstance().warning("In ColumnData.getShortArray, requested array for non-matching column: [" + fullName + "]");
				}
			}
		}
		return array;
	}

	/**
	 * Obtain an int array from the current event for the given full name
	 * @param fullName the full name 
	 * @return the array, or <code>null</code>
	 */
	public static int[] getIntArray(String fullName) {
		int[] array = null;
		ColumnData cd = getColumnData(fullName);
		if (cd == null) {
			Log.getInstance().warning("In ColumnData.getIntArray, requested array for non-existent column: [" + fullName + "]");
		}
		else {
			Object o = cd.getDataArray();
			if (o != null) {
				if (o instanceof int[]) {
					array = (int[])o;
				}
				else {
					Log.getInstance().warning("In ColumnData.getIntArray, requested array for non-matching column: [" + fullName + "]");
				}
			}
		}
		return array;
	}
	

	/**
	 * Obtain a float array from the current event for the given full name
	 * @param fullName the full name 
	 * @return the array, or <code>null</code>
	 */
	public static float[] getFloatArray(String fullName) {
		float[] array = null;
		ColumnData cd = getColumnData(fullName);
		if (cd == null) {
			Log.getInstance().warning("In ColumnData.getFloatArray, requested array for non-existent column: [" + fullName + "]");
		}
		else {
			Object o = cd.getDataArray();
			if (o != null) {
				if (o instanceof float[]) {
					array = (float[])o;
				}
				else {
					Log.getInstance().warning("In ColumnData.getFloatArray, requested array for non-matching column: [" + fullName + "]");
				}
			}
		}
		return array;
	}


	/**
	 * Obtain a double array from the current event for the given full name
	 * @param fullName the full name 
	 * @return the array, or <code>null</code>
	 */
	public static double[] getDoubleArray(String fullName) {
		double[] array = null;
		ColumnData cd = getColumnData(fullName);
		if (cd == null) {
			Log.getInstance().warning("In ColumnData.getDoubleArray, requested array for non-existent column: [" + fullName + "]");
		}
		else {
			Object o = cd.getDataArray();
			if (o != null) {
				if (o instanceof double[]) {
					array = (double[])o;
				}
				else {
					Log.getInstance().warning("In ColumnData.getDoubleArray, requested array for non-matching column: [" + fullName + "]");
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
	public static boolean validColumnName(String name) {
		return ((name != null) && (name.length() > 4) && name.contains(":") && name.contains("."));
	}

}
