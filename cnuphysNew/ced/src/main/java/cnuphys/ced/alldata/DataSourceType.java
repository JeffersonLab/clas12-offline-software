package cnuphys.ced.alldata;

import java.util.EnumMap;

public enum DataSourceType {
HIPPOFILE, HIPPORING;
	
	/**
	 * A map for the names of the event sources
	 */
	public static EnumMap<DataSourceType, String> names = new EnumMap<DataSourceType, String>(DataSourceType.class);

	static {
		names.put(HIPPOFILE, "Hippo File");
		names.put(HIPPORING, "Hippo Ring");
	}
	
	/**
	 * Get the nice name of the enum.
	 * 
	 * @return the nice name, for combo boxes, menus, etc.
	 */
	public String getName() {
		return names.get(this);
	}
	

	/*
	 * Returns the mapped "nice name"
	 * @see java.lang.Enum#toString()
	 */
	@Override
	public String toString() {
		return this.getName();
	}

	/**
	 * Returns the enum value from the name.
	 * 
	 * @param name
	 *            the name to match.
	 * @return the <code>DataSourceType</code> that corresponds to the name. Returns
	 *         <code>null</code> if no match is found. Note it will check (case
	 *         insensitive) both the map and the <code>name()</code> result.
	 */
	public static DataSourceType getValue(String name) {
		if (name == null) {
			return null;
		}
		
		for (DataSourceType fs : values()) {
			if (name.equalsIgnoreCase(fs.name())) {
				return fs;
			}
			if (name.equalsIgnoreCase(names.get(fs))) {
				return fs;
			}
		}

		return null;
	}

}
