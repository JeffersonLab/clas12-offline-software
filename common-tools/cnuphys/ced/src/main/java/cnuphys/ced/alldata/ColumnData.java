package cnuphys.ced.alldata;

import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;

import cnuphys.bCNU.log.Log;
import cnuphys.ced.clasio.ClasIoEventManager;

public class ColumnData implements Comparable<ColumnData> {
	
	/** type is unknown */
	public static final int UNKNOWN = 0;

	/** type is a byte */
	public static final int INT8 = 1;

	/** type is a short */
	public static final int INT16 = 2;

	/** type is an int */
	public static final int INT32 = 3;

	/** type is a float */
	public static final int FLOAT32 = 4;

	/** type is a double */
	public static final int FLOAT64 = 5;

	/** type is a string */
	public static final int STRING = 6;

	/** type is a group */
	public static final int GROUP = 7;

	/** type is a long int */
	public static final int INT64 = 8;

	/** type is a vector3f */
	public static final int VECTOR3F = 9;

	/** type is a composite */
	public static final int COMPOSITE = 10;

	/** type is a table */
	public static final int TABLE = 11;

	/** type is a branch */
	public static final int BRANCH = 12;

	/** type names */
	public static String[] typeNames = { "Unknown", "byte", "short", "int", "float", "double", "string", "group", "long", "vector3f", "composite", "table", "branch"};


//	/** type is unknown */
//	public static final int UNKNOWN = 0;
//
//	/** type is a byte */
//	public static final int INT8 = 1;
//
//	/** type is a short */
//	public static final int INT16 = 2;
//
//	/** type is an int */
//	public static final int INT32 = 3;
//
//	/** type is a long int */
//	public static final int INT64 = 4;
//
//	/** type is a float */
//	public static final int FLOAT32 = 5;
//
//	/** type is a double */
//	public static final int FLOAT64 = 6;
//
//	/** type names */
//	public static String[] typeNames = { "Unknown", "byte", "short", "int", "long", "float", "double" };

	// the bank name
	private String _bankName;

	// the column name
	private String _columnName;

	// the data type
	private int _type;

	// the full name
	private String _fullName;

	// used for table rendering
	public int bankIndex;

	/**
	 * Holds the data for one column, one event
	 * 
	 * @param bankName   the bank name
	 * @param columnName the column name
	 * @param type       the data type (one of the class constants)
	 */
	public ColumnData(String bankName, String columnName, int type) {
		_bankName = bankName;
		_columnName = columnName;
		_fullName = bankName + "." + columnName;

		if ((type < 1) || (type > 6) || (type == 24)) {
			Log.getInstance().warning("Bank: [" + _bankName + "] Column: [" + columnName
					+ "] bad data type in ColumnData constructor: [" + type + "]");
			type = 0;
		}
		_type = type;
	}

	@Override
	public String toString() {
		return "bank name: [" + _bankName + "] column name: [" + _columnName + "] full name: [" + _fullName
				+ "] data type: " + typeNames[_type];
	}

	/**
	 * Get the name of the data type
	 * 
	 * @return the name of the data type
	 */
	public String getTypeName() {
		if ((_type < 0) || (_type >= typeNames.length)) {
			return "???";
		} else {
			return typeNames[_type];
		}
	}

	/**
	 * Get the length of the backing data array
	 * 
	 * @param event the current event
	 * @return the length of the array
	 */
	public int getLength(DataEvent event) {

		if (event != null) {
			try {
				switch (_type) {
				case INT8:
					byte bytes[] = getByteArray(event);
					return (bytes == null) ? 0 : bytes.length;

				case INT16:
					short shorts[] = getShortArray(event);
					return (shorts == null) ? 0 : shorts.length;

				case INT32:
					int ints[] = getIntArray(event);
					return (ints == null) ? 0 : ints.length;

				case INT64:
					long longs[] = getLongArray(event);
					return (longs == null) ? 0 : longs.length;

				case FLOAT32:
					float floats[] = getFloatArray(event);
					return (floats == null) ? 0 : floats.length;

				case FLOAT64:
					double doubles[] = getDoubleArray(event);
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
	 * Get a byte array for the bank and column names in the given event
	 * @param event the given event
	 * @return a byte array
	 */
	public byte[] getByteArray(DataEvent event) {
		DataBank bank = event.getBank(_bankName);
		byte[] array = bank.getByte(_columnName);
		return array;
	}
	
	/**
	 * Get a short array for the bank and column names in the given event
	 * @param event the given event
	 * @return a shortarray
	 */
	public short[] getShortArray(DataEvent event) {
		DataBank bank = event.getBank(_bankName);
		short[] array = bank.getShort(_columnName);
		return array;
	}

	/**
	 * Get an int array for the bank and column names in the given event
	 * @param event the given event
	 * @return an int array
	 */
	public int[] getIntArray(DataEvent event) {
		DataBank bank = event.getBank(_bankName);
		int[] array = bank.getInt(_columnName);
		return array;
	}
	
	/**
	 * Get a long array for the bank and column names in the given event
	 * @param event the given event
	 * @return a long array
	 */
	public long[] getLongArray(DataEvent event) {
		DataBank bank = event.getBank(_bankName);
		long[] array = bank.getLong(_columnName);
		return array;
	}
	/**
	 * Get a float array for the bank and column names in the given event
	 * @param event the given event
	 * @return a float array
	 */
	public float[] getFloatArray(DataEvent event) {
		DataBank bank = event.getBank(_bankName);
		float[] array = bank.getFloat(_columnName);
		return array;
	}
	
	/**
	 * Get a double array for the bank and column names in the given event
	 * @param event the given event
	 * @return a double array
	 */
	public double[] getDoubleArray(DataEvent event) {
		DataBank bank = event.getBank(_bankName);
		double[] array = bank.getDouble(_columnName);
		return array;
	}
	
//	/**
//	 * Get the data array as an object. It is up to the caller to cast it to the
//	 * correct type of array.
//	 * 
//	 * @return the data array corresponding to the type
//	 */
//	public Object getDataArray(DataEvent event) {
//
//		Object oa = null;
//
//		if (event != null) {
//			try {
//				switch (_type) {
//				case INT8:
//					oa = event.getByte(_fullName);
//					break;
//
//				case INT16:
//					oa = event.getShort(_fullName);
//					break;
//
//				case INT32:
//					oa = event.getInt(_fullName);
//					break;
//
//				case INT64:
//					oa = event.getLong(_fullName);
//					break;
//
//				case FLOAT32:
//					oa = event.getFloat(_fullName);
//					break;
//
//				case FLOAT64:
//					oa = event.getDouble(_fullName);
//					break;
//				}
//			} catch (Exception e) {
//				System.err.println(e.getMessage() + " Exception with fullName: " + _fullName);
//				// e.printStackTrace();
//			}
//		}
//
//		return oa;
//	}

	/**
	 * Get the array with double values regardless of type
	 * 
	 * @return the data as a double array
	 */
	public double[] getAsDoubleArray(DataEvent event) {
		double da[] = null;

		if (event != null) {
			switch (_type) {
			case INT8:
				byte b[] = getByteArray(event);
				int len = (b == null) ? 0 : b.length;
				if (len > 0) {
					da = new double[len];
					for (int j = 0; j < len; j++) {
						da[j] = b[j];
					}
				}
				break;

			case INT16:
				short s[] = getShortArray(event);
				len = (s == null) ? 0 : s.length;
				if (len > 0) {
					da = new double[len];
					for (int j = 0; j < len; j++) {
						da[j] = s[j];
					}
				}
				break;

			case INT32:
				int i[] = getIntArray(event);
				len = (i == null) ? 0 : i.length;
				if (len > 0) {
					da = new double[len];
					for (int j = 0; j < len; j++) {
						da[j] = i[j];
					}
				}
				break;

			case FLOAT32:
				float f[] = getFloatArray(event);
				len = (f == null) ? 0 : f.length;
				if (len > 0) {
					da = new double[len];
					for (int j = 0; j < len; j++) {
						da[j] = f[j];
					}
				}
				break;

			case FLOAT64:
				da = getDoubleArray(event);
				break;
			}
		}

		return da;
	}

	/**
	 * Get the length of the underlying data array
	 * 
	 * @return the length of the underlying data array
	 */
	public int length(DataEvent event) {

		int len = 0;

			switch (_type) {
			case INT8:
				byte ba[] = getByteArray(event);
				len = (ba != null) ? ba.length : 0;
				break;

			case INT16:
				short sa[] = getShortArray(event);
				len = (sa != null) ? sa.length : 0;
				break;

			case INT32:
				int ia[] = getIntArray(event);
				len = (ia != null) ? ia.length : 0;
				break;

			case INT64:
				long la[] = getLongArray(event);
				len = (la != null) ? la.length : 0;
				break;

			case FLOAT32:
				float fa[] = getFloatArray(event);
				len = (fa != null) ? fa.length : 0;
				break;

			case FLOAT64:
				double da[] = getDoubleArray(event);
				len = (da != null) ? da.length : 0;
				break;
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

	// a check to avoid null messages and errors
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
	 * 
	 * @param fullName the full name
	 * @return the array, or <code>null</code>
	 */
	public static byte[] getByteArray(String fullName) {
		DataEvent event = hasData(fullName);
		return (event == null) ? null : DataManager.getInstance().getByteArray(event, fullName);
	}

	/**
	 * Obtain a short array from the current event for the given full name
	 * 
	 * @param fullName the full name
	 * @return the array, or <code>null</code>
	 */
	public static short[] getShortArray(String fullName) {
		DataEvent event = hasData(fullName);
		return (event == null) ? null : DataManager.getInstance().getShortArray(event, fullName);
	}

	/**
	 * Obtain an int array from the current event for the given full name
	 * 
	 * @param fullName the full name
	 * @return the array, or <code>null</code>
	 */
	public static int[] getIntArray(String fullName) {
		DataEvent event = hasData(fullName);
		return (event == null) ? null : DataManager.getInstance().getIntArray(event, fullName);
	}

	/**
	 * Obtain a long array from the current event for the given full name
	 * 
	 * @param fullName the full name
	 * @return the array, or <code>null</code>
	 */
	public static long[] getLongArray(String fullName) {
		DataEvent event = hasData(fullName);
		return (event == null) ? null : DataManager.getInstance().getLongArray(event, fullName);
	}

	/**
	 * Obtain a float array from the current event for the given full name
	 * 
	 * @param fullName the full name
	 * @return the array, or <code>null</code>
	 */
	public static float[] getFloatArray(String fullName) {
		DataEvent event = hasData(fullName);
		return (event == null) ? null : DataManager.getInstance().getFloatArray(event, fullName);
	}

	/**
	 * Obtain double array from the current event for the given full name
	 * 
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
