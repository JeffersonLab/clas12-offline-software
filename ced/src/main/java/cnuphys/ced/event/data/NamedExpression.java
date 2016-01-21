package cnuphys.ced.event.data;

public class NamedExpression implements Comparable<NamedExpression>{

	public String expName;
	public String expString;
	
	public NamedExpression(String eName, String eString) {
		expName = eName;
		expString = eString;
	}
	
	@Override
	public int compareTo(NamedExpression o) {
		String lcv = expName.toLowerCase();
		String lco = o.expName.toLowerCase();
		return lcv.compareTo(lco);
	}

}
