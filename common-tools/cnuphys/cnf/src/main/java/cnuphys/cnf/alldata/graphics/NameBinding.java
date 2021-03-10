package cnuphys.cnf.alldata.graphics;

/**
 * A simple class that binds a name like "x" or "theta" to abank.column, like
 * DC::dgtz.sector
 * 
 * @author heddle
 *
 */
public class NameBinding implements Comparable<NameBinding> {

	public String varName;
	public String bankColumnName;

	public NameBinding(String vname, String bcname) {
		varName = vname;
		bankColumnName = bcname;
	}

	@Override
	public int compareTo(NameBinding o) {
		String lcv = varName.toLowerCase();
		String lco = o.varName.toLowerCase();
		return lcv.compareTo(lco);
	}


}
