package cnuphys.bCNU.event;

import org.jlab.coda.jevio.BaseStructureHeader;
import org.jlab.coda.jevio.DataType;
import org.jlab.coda.jevio.IEvioStructure;

public class StructureStore {

	// stores the converted array (not converted until needed)
	private Object _convertedArray;

	// the structure in question
	private IEvioStructure _structure;

	// the structure header
	private BaseStructureHeader _header;

	// the evio data type
	private DataType _dataType;

	/**
	 * Create the store.
	 * 
	 * @param structure
	 *            the structure to be stored.
	 */
	public StructureStore(IEvioStructure structure) {
		reset(structure);
	}

	/**
	 * Reset or resuse the store. This is so we don't keep creating objects.
	 * 
	 * @param structure
	 *            the new structure.
	 */
	public void reset(IEvioStructure structure) {
		_structure = structure;
		_convertedArray = null;
		_header = _structure.getHeader();
		_dataType = _header.getDataType(); // jevio4
	}

	/**
	 * Get the raw byte data
	 * 
	 * @return the raw byte data.
	 */
	public byte[] getByteArray() {
		return _structure.getByteData();
	}

	/**
	 * Get the short array (assuming the type is SHORT16)
	 * 
	 * @return the short array (assuming the type is SHORT16)
	 */
	public short[] getShortArray() {
		if (_dataType == DataType.SHORT16) {
			if (_convertedArray == null) {
				_convertedArray = _structure.getShortData();
			}
			return (short[]) _convertedArray;
		}
		return null;
	}

	/**
	 * Get the integer array (assuming the type is INT32)
	 * 
	 * @return the integer array (assuming the type is INT32)
	 */
	public int[] getIntArray() {
		if (_dataType == DataType.INT32) {
			if (_convertedArray == null) {
				_convertedArray = _structure.getIntData();
			}
			return (int[]) _convertedArray;
		}
		return null;
	}

	/**
	 * Get the long array (assuming the type is LONG64)
	 * 
	 * @return the long array (assuming the type is LONG64)
	 */
	public long[] getLongArray() {
		if (_dataType == DataType.LONG64) {
			if (_convertedArray == null) {
				_convertedArray = _structure.getLongData();
			}
			return (long[]) _convertedArray;
		}
		return null;
	}

	/**
	 * Get the float array (assuming the type is FLOAT32)
	 * 
	 * @return the float array (assuming the type is FLOAT32)
	 */
	public float[] getFloatArray() {
		if (_dataType == DataType.FLOAT32) {
			if (_convertedArray == null) {
				_convertedArray = _structure.getFloatData();
			}
			return (float[]) _convertedArray;
		}
		return null;
	}

	/**
	 * Get the double array (assuming the type is DOUBLE64)
	 * 
	 * @return the float array (assuming the type is DOUBLE64)
	 */
	public double[] getDoubleArray() {
		if (_dataType == DataType.DOUBLE64) {
			if (_convertedArray == null) {
				_convertedArray = _structure.getDoubleData();
			}
			return (double[]) _convertedArray;
		}
		return null;
	}

	/**
	 * @return the structure.
	 */
	public IEvioStructure getStructure() {
		return _structure;
	}

	/**
	 * @return the header.
	 */
	public BaseStructureHeader getHeader() {
		return _header;
	}

}
