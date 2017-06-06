package cnuphys.ced.geometry;

import cnuphys.ced.geometry.bmt.Constants;
import cnuphys.ced.geometry.bmt.ConstantsLoader;
import cnuphys.ced.geometry.bmt.Geometry;

public class BMTGeometry {
	
	private static Geometry _geometry;

	/**
	 * Initialize the BMT Geometry
	 */
	public static void initialize() {
		
		if (_geometry != null) {
			return;
		}

		System.out.println("\n=====================================");
		System.out.println("===  BMT Geometry Initialization  ===");
		System.out.println("=====================================");

		ConstantsLoader.Load(10);
		Constants.Load();
		_geometry = new Geometry();

	}
	
	public static Geometry getGeometry() {
		initialize();
		return _geometry;
	}

}
