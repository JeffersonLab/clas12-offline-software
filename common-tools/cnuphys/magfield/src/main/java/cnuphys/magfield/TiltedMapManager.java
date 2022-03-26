package cnuphys.magfield;

import cnuphys.magfield.MagneticFields.FieldType;

/**
 * Manages the use of maps rotated for the tilted system.
 * @author heddle
 *
 */
public class TiltedMapManager implements MagneticFieldChangeListener {
	
	//the flag that determines whether we are to use rotated maps.
	private boolean _useTiltedMaps;
	
	//singleton
	private static TiltedMapManager _instance;
	
	//private constructor for manager
	private TiltedMapManager() {
		System.err.println("[TiltedMapManager] constructor ");
		MagneticFields.getInstance().addMagneticFieldChangeListener(this);
		checkCachedTiltedMaps();
	}
	
	/**
	 * Public access to the singleton
	 *   TiltedMapManager singleton
	 */
	public static TiltedMapManager getInstance() {
		if (_instance == null) {
			_instance = new TiltedMapManager();
		}
		return _instance;
	}
	
	/**
	 * Set whether tilted maps should be used
	 * @param useTiltedMaps the value of the flag.
	 */
	public void setUseTiltedMaps(boolean useTiltedMaps) {
		if (_useTiltedMaps == useTiltedMaps) {
			return;
		}
		
		_useTiltedMaps = useTiltedMaps;
		
		checkCachedTiltedMaps();
	}
	
	//remove any cached maps
	private void removeCachedTiltedMaps() {
		System.err.println("[TiltedMapManager] remove cached maps");
	}
	
	//check whether our cached maps are consistent
	private void checkCachedTiltedMaps() {
		IMagField currentField = MagneticFields.getInstance().getActiveField();
		
		if (currentField == null) {
			System.err.println("[TiltedMapManager] null field in checkCachedTiltedMaps");
			removeCachedTiltedMaps();
			return;
		}
		
		FieldType fieldType = MagneticFields.getInstance().getActiveFieldType();
		
		
		
		System.err.println("[TiltedMapManager] current field name [" + currentField.getName() + "]");
		System.err.println("[TiltedMapManager] current field type [" + fieldType + "]");
		
		if(_useTiltedMaps) {
			//here is where we might call the conversion
		}
		else {
			removeCachedTiltedMaps();
		}
	}
	
	

	@Override
	public void magneticFieldChanged() {
		System.err.println("[TiltedMapManager] magfield change event ");
		checkCachedTiltedMaps();
	}

}
