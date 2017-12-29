package cnuphys.ced.alldata;

import org.jlab.io.base.DataEvent;

import cnuphys.bCNU.log.Log;
import cnuphys.ced.clasio.ClasIoEventManager;

public class ColumnData implements Comparable<ColumnData> {

	/** type is unknown*/
	public static final int UNKNOWN = 0;

	/** type is a byte */
	public static final int INT8 = 1;

	/** type is a short */
	public static final int INT16 = 2;

	/** type is an int */
	public static final int INT32 = 3;
	
	/** type is a long int */
	public static final int INT64 = 4;

	/** type is a float */
	public static final int FLOAT32 = 5;

	/** type is a double */
	public static final int FLOAT64 = 6;
	
	/** type names */
	public static String[] typeNames = {"Unknown", "byte", "short", "int", "long", "float", "double"};

	// the bank name
	private String _bankName;

	// the column name
	private String _columnName;

	// the data type
	private int _type;

	// the full name
	private String _fullName;
	
	//used for table rendering
	public int bankIndex;
	
	/**
	 * Holds the data for one column, one event
	 * 
	 * @param bankName the bank name
	 * @param columnName the column name
	 * @param type the data type (one of the class constants)
	 */
	public ColumnData(String bankName, String columnName, int type) {
		_bankName = bankName;
		_columnName = columnName;
		_fullName = bankName + "." + columnName;

		if ((type < 1) || (type > 6) || (type == 24)) {
			Log.getInstance()
					.warning("Bank: [" + _bankName + "] Column: [" + columnName
							+ "] bad data type in ColumnData constructor: ["
							+ type + "]");
			type = 0;
		}
		_type = type;
	}
	
	@Override
	public String toString() {
		return "bank name: [" + _bankName + 
				"] column name: [" + _columnName +
				"] full name: [" + _fullName +
				"] data type: " + typeNames[_type];
	}
	
	/**
	 * Get the name of the data type
	 * @return the name of the data type
	 */
	public String getTypeName() {
		if ((_type < 0) || (_type >= typeNames.length)) {
			return "???";
		}
		else {
			return typeNames[_type];
		}
	}
	
	/**
	 * Get the length of the backing data array
	 * @param event the current event
	 * @return the length of the array
	 */
	public int getLength(DataEvent event) {
		
		if (event != null) {
			try {
				switch (_type) {
				case INT8:
					byte bytes[] = event.getByte(_fullName);
					return (bytes == null) ? 0 : bytes.length;

				case INT16:
					short shorts[] = event.getShort(_fullName);
					return (shorts == null) ? 0 : shorts.length;

				case INT32:
					int ints[] = event.getInt(_fullName);
					return (ints == null) ? 0 : ints.length;
					
				case INT64:
					//TODO MAJOR HACK
					long longs[] = event.getLong(_fullName);
					return (longs == null) ? 0 : longs.length;

				case FLOAT32:
					float floats[] = event.getFloat(_fullName);
					return (floats == null) ? 0 : floats.length;

				case FLOAT64:
					double doubles[] = event.getDouble(_fullName);
					return (doubles == null) ? 0 : doubles.length;
				}
			} catch (Exception e) {
				System.err.println(e.getMessage() + " Exception (ColumnData.getLength) with fullName: " + _fullName);
				e.printStackTrace();
			}
		}
		
		return 0;
	}
	

	/**
	 * Get the data array as an object. It is up to the caller to cast it to the
	 * correct type of array.
	 * 
	 * @return the data array corresponding to the type
	 */
	public Object getDataArray(DataEvent event) {

		Object oa = null;
		
		if (event != null) {
			try {
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
					
				case INT64:
					oa = event.getLong(_fullName);
					break;

				case FLOAT32:
					oa = event.getFloat(_fullName);
					break;

				case FLOAT64:
					oa = event.getDouble(_fullName);
					break;
				}
			} catch (Exception e) {
				System.err.println(e.getMessage() + " Exception with fullName: " + _fullName);
	//			e.printStackTrace();
			}
		}

		return oa;
	}
	
	/**
	 * Get the array with double values regardless of type
	 * @return the data as a double array
	 */
	public double[] getAsDoubleArray(DataEvent event) {
		double da[] = null;
		
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
	public int length(DataEvent event) {
		
		int len = 0;
		Object oa = getDataArray(event);
		
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
				
			case INT64:
				len = ((long[]) oa).length;
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
	
	//a check to avoid null messages and errors
	private static DataEvent hasData(String fullName) {
		DataEvent event = ClasIoEventManager.getInstance().getCurrentEvent();
		if (event == null) {
			return null;
		}
		
		ColumnData cd = DataManager.getInstance().getColumnData(fullName);
		if (cd == null) {
			return null;
		}
		
		if (!event.hasBank(cd.getBankName())) {
			return null;
		}
		
		return event;
	}
	
	/**
	 * Obtain a byte array from the current event for the given full name
	 * @param fullName the full name 
	 * @return the array, or <code>null</code>
	 */
	public static byte[] getByteArray(String fullName) {	
		DataEvent event = hasData(fullName);
		return (event == null) ? null : DataManager.getInstance().getByteArray(event, fullName);
	}

	/**
	 * Obtain a short array from the current event for the given full name
	 * @param fullName the full name 
	 * @return the array, or <code>null</code>
	 */
	public static short[] getShortArray(String fullName) {
		DataEvent event = hasData(fullName);
		return (event == null) ? null : DataManager.getInstance().getShortArray(event, fullName);
	}

	/**
	 * Obtain an int array from the current event for the given full name
	 * @param fullName the full name 
	 * @return the array, or <code>null</code>
	 */
	public static int[] getIntArray(String fullName) {
		DataEvent event = hasData(fullName);
		return (event == null) ? null : DataManager.getInstance().getIntArray(event, fullName);
	}
	
	/**
	 * Obtain a long array from the current event for the given full name
	 * @param fullName the full name 
	 * @return the array, or <code>null</code>
	 */
	public static long[] getLongArray(String fullName) {
		DataEvent event = hasData(fullName);
		return (event == null) ? null : DataManager.getInstance().getLongArray(event, fullName);
	}


	/**
	 * Obtain a float array from the current event for the given full name
	 * @param fullName the full name 
	 * @return the array, or <code>null</code>
	 */
	public static float[] getFloatArray(String fullName) {
		DataEvent event = hasData(fullName);
		return (event == null) ? null : DataManager.getInstance().getFloatArray(event, fullName);
	}

	/**
	 * Obtain double array from the current event for the given full name
	 * @param fullName the full name 
	 * @return the array, or <code>null</code>
	 */
	public static double[] getDoubleArray(String fullName) {
		DataEvent event = hasData(fullName);
		return (event == null) ? null : DataManager.getInstance().getDoubleArray(event, fullName);
	}


	@Override
	public int compareTo(ColumnData o) {
		return _fullName.compareTo(o._fullName);
	}

}
