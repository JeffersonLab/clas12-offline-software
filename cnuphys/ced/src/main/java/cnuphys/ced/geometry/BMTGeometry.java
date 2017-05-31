package cnuphys.ced.geometry;

import cnuphys.ced.micromegas.Constants;
import cnuphys.ced.micromegas.Geometry;

public class BMTGeometry {

    //temp object using temp geom until available from Gagik Geom mgr
    private static Geometry geo;
    
    /**
     * Initialize the BST Geometry
     */
    public static void initialize() {

	System.out.println("\n=====================================");
	System.out.println("===  BMT Geometry Initialization  ===");
	System.out.println("=====================================");
	
	Constants.Load();

	geo = new Geometry();

    }

}
