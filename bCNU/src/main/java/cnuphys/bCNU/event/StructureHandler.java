package cnuphys.bCNU.event;

import java.util.Hashtable;

import org.jlab.coda.jevio.BaseStructureHeader;

import org.jlab.coda.jevio.IEvioStructure;

/**
 * There should be one of these for each distinct tag. Then for each num there
 * will be a data object. These will be stored in a hash table
 * 
 * @author heddle
 * 
 */
public class StructureHandler {

	// stores a "StructureStore" for all structures.
	private Hashtable<String, StructureStore> _hashtable;

	/**
	 * Create a structure handler
	 * 
	 * @param htsize
	 *            the hashtable size.
	 */
	public StructureHandler(int htsize) {
		_hashtable = new Hashtable<String, StructureStore>(htsize);
	}

	/**
	 * Add a structure for this handler. Each handler corresponds to a tag, so
	 * we are adding a structure that has the same tag (hopefully) but a new
	 * num.
	 * 
	 * @param structure
	 *            the structure to store.
	 */
	public void addStructure(IEvioStructure structure) {
		// throw an exception for a tag mismatch
		BaseStructureHeader header = structure.getHeader();

		// create or reuse a store
		String key = getHashKey(header.getTag(), header.getNumber());
		StructureStore ss = _hashtable.remove(key);
		if (ss != null) {
			ss.reset(structure);
		} else {
			ss = new StructureStore(structure);
		}
		_hashtable.put(key, ss);
	}

	/**
	 * Clear, as if no data.
	 */
	public void clear() {
		_hashtable.clear();
	}

	// get the hash key based on the tag and num;
	private String getHashKey(int tag, int num) {
		return "$" + tag + "$" + num;
	}

	/**
	 * Get the raw byte data
	 * 
	 * @param tag
	 *            the tag field from the evio header.
	 * @param num
	 *            the num field from the evio header.
	 * @return the raw byte data.
	 */
	public byte[] getByteArray(int tag, int num) {
		StructureStore ss = _hashtable.get(getHashKey(tag, num));
		if (ss != null) {
			return ss.getByteArray();
		}
		return null;
	}

	/**
	 * Get the short array (assuming the type is SHORT16)
	 * 
	 * @param tag
	 *            the tag field from the evio header.
	 * @param num
	 *            the num field from the evio header.
	 * @return the short array (assuming the type is SHORT16)
	 */
	public short[] getShortArray(int tag, int num) {
		StructureStore ss = _hashtable.get(getHashKey(tag, num));
		if (ss != null) {
			return ss.getShortArray();
		}
		return null;
	}

	/**
	 * Get the integer array (assuming the type is INT32)
	 * 
	 * @param tag
	 *            the tag field from the evio header.
	 * @param num
	 *            the num field from the evio header.
	 * @return the integer array (assuming the type is INT32)
	 */
	public int[] getIntArray(int tag, int num) {
		StructureStore ss = _hashtable.get(getHashKey(tag, num));
		if (ss != null) {
			return ss.getIntArray();
		}
		return null;
	}

	/**
	 * Get the long array (assuming the type is LONG64)
	 * 
	 * @param tag
	 *            the tag field from the evio header.
	 * @param num
	 *            the num field from the evio header.
	 * @return the long array (assuming the type is LONG64)
	 */
	public long[] getLongArray(int tag, int num) {
		StructureStore ss = _hashtable.get(getHashKey(tag, num));
		if (ss != null) {
			return ss.getLongArray();
		}
		return null;
	}

	/**
	 * Get the float array (assuming the type is FLOAT32)
	 * 
	 * @param tag
	 *            the tag field from the evio header.
	 * @param num
	 *            the num field from the evio header.
	 * @return the float array (assuming the type is FLOAT32)
	 */
	public float[] getFloatArray(int tag, int num) {
		StructureStore ss = _hashtable.get(getHashKey(tag, num));
		if (ss != null) {
			return ss.getFloatArray();
		}
		return null;
	}

	/**
	 * Get the double array (assuming the type is DOUBLE64)
	 * 
	 * @param tag
	 *            the tag field from the evio header.
	 * @param num
	 *            the num field from the evio header.
	 * @return the float array (assuming the type is DOUBLE64)
	 */
	public double[] getDoubleArray(int tag, int num) {
		StructureStore ss = _hashtable.get(getHashKey(tag, num));
		if (ss != null) {
			return ss.getDoubleArray();
		}
		return null;
	}

}
