package cnuphys.magfield;

import java.util.EnumMap;

public enum TorusMap {
	SYMMETRIC, FULL_025, FULL_050, FULL_075, FULL_100, FULL_125, FULL_150, FULL_200;

	// map to file names

	/**
	 * A map for the base names of the torus map files
	 */
	public static EnumMap<TorusMap, String> fileNames = new EnumMap<TorusMap, String>(TorusMap.class);

	/**
	 * A map for the directories of the torus map files
	 */
	public static EnumMap<TorusMap, String> dirNames = new EnumMap<TorusMap, String>(TorusMap.class);

	/**
	 * A map for the nice names of the torus maps
	 */
	public static EnumMap<TorusMap, String> names = new EnumMap<TorusMap, String>(TorusMap.class);

	/**
	 * A map for whether the maps are full fields
	 */
	public static EnumMap<TorusMap, Boolean> fullField = new EnumMap<TorusMap, Boolean>(TorusMap.class);

	/**
	 * A map for whether the maps are found
	 */
	public static EnumMap<TorusMap, Boolean> foundField = new EnumMap<TorusMap, Boolean>(TorusMap.class);

	static {
		fileNames.put(SYMMETRIC, "clas12-fieldmap-torus.dat");
		fileNames.put(FULL_025, "clas12TorusFull_0.25.dat");
		fileNames.put(FULL_050, "clas12TorusFull_0.50.dat");
		fileNames.put(FULL_075, "clas12TorusFull_0.75.dat");
		fileNames.put(FULL_100, "clas12TorusFull_1.00.dat");
		fileNames.put(FULL_125, "clas12TorusFull_1.25.dat");
		fileNames.put(FULL_150, "clas12TorusFull_1.50.dat");
		fileNames.put(FULL_200, "clas12TorusFull_2.00.dat");

		dirNames.put(SYMMETRIC, "");
		dirNames.put(FULL_025, "");
		dirNames.put(FULL_050, "");
		dirNames.put(FULL_075, "");
		dirNames.put(FULL_100, "");
		dirNames.put(FULL_125, "");
		dirNames.put(FULL_150, "");
		dirNames.put(FULL_200, "");

		names.put(SYMMETRIC, "Symmetric map, 0.25 degree spacing");
		names.put(FULL_025, "Full map, 0.25 degree spacing");
		names.put(FULL_050, "Full map, 0.50 degree spacing");
		names.put(FULL_075, "Full map, 0.75 degree spacing");
		names.put(FULL_100, "Full map, 1.00 degree spacing");
		names.put(FULL_125, "Full map, 1.25 degree spacing");
		names.put(FULL_150, "Full map, 1.50 degree spacing");
		names.put(FULL_200, "Full map, 2.00 degree spacing");

		fullField.put(SYMMETRIC, false);
		fullField.put(FULL_025, true);
		fullField.put(FULL_050, true);
		fullField.put(FULL_075, true);
		fullField.put(FULL_100, true);
		fullField.put(FULL_125, true);
		fullField.put(FULL_150, true);
		fullField.put(FULL_200, true);

		foundField.put(SYMMETRIC, false);
		foundField.put(FULL_025, false);
		foundField.put(FULL_050, false);
		foundField.put(FULL_075, false);
		foundField.put(FULL_100, false);
		foundField.put(FULL_125, false);
		foundField.put(FULL_150, false);
		foundField.put(FULL_200, false);

	}

	/**
	 * Get the nice name of the map.
	 * 
	 * @return the nice name, for combo boxes, menus, etc.
	 */
	public String getName() {
		return names.get(this);
	}

	/**
	 * Get the filename name of the map.
	 * 
	 * @return the filename.
	 */
	public String getFileName() {
		return fileNames.get(this);
	}

	/**
	 * Get the dirname name of the map.
	 * 
	 * @return the directory name.
	 */
	public String getDirName() {
		return dirNames.get(this);
	}

	/**
	 * Set the directory name
	 * 
	 * @param dname the directory name where the map was found
	 */
	public void setDirName(String dname) {
		dirNames.put(this, dname == null ? "" : dname);
	}

	/**
	 * Is this a full field (as opposed to symmetric?)
	 * 
	 * @return <code>true</code> if the field is full.
	 */
	public boolean fullField() {
		return fullField.get(this);
	}

	/**
	 * Was this field found?
	 * 
	 * @return <code>true</code> if the field was found.
	 */
	public boolean foundField() {
		return foundField.get(this);
	}

	/**
	 * Set whether this field was found
	 * 
	 * @param found the value of found
	 */
	public void setFound(boolean found) {
		foundField.put(this, found);
	}

}
