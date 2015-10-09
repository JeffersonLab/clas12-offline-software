package cnuphys.ced.clasio.tojava;

import javax.swing.JTextArea;

public class Holder implements Comparable<Holder> {
	String bankName;
	String sectionName;
	String columnName;
	String colInfo;
	String colType;
	String arrayname;
	String getname;
	String banksectname;
	String javatype = "???[]";
	String getter = "event.get???";

	JTextArea textArea;

	public Holder(final JTextArea ta, String bankName, String sectionName,
			String columnName, String colInfo, final String colType) {
		super();
		textArea = ta;
		this.bankName = bankName; // e.g., BST
		this.sectionName = sectionName; // e.g., true
		this.columnName = columnName; // e.g., pid
		this.colInfo = colInfo; // comment
		this.colType = colType; // type

		System.err.println("   new holder with bank name: " + bankName);

		arrayname = bankName.toLowerCase() + "_" + sectionName.toLowerCase()
				+ "_" + columnName;
		banksectname = bankName + "::" + sectionName;
		getname = banksectname + "." + columnName;

		if (colType.equalsIgnoreCase("float64")) {
			javatype = "double[]";
			getter = "event.getDouble";
		} else if (colType.equalsIgnoreCase("float32")) {
			javatype = "float[]";
			getter = "event.getFloat";
		} else if (colType.equalsIgnoreCase("int32")) {
			javatype = "int[]";
			getter = "event.getInt";
		} else if (colType.equalsIgnoreCase("int16")) {
			javatype = "short[]";
			getter = "event.getShort";
		} else if (colType.equalsIgnoreCase("int8")) {
			javatype = "byte[]";
			getter = "event.getByte";
		}

	}

	public void declaration() {
		if (colInfo != null) {
			textArea.append("\n/** " + colInfo + " */\n");
		}
		textArea.append(" public " + javatype + " " + arrayname + ";\n");
	}

	public void nullify() {
		textArea.append("  " + arrayname + " = null;\n");
	}

	public void getter() {
		// true_pid = event.getInt("DC::true.pid");
		textArea.append("    " + arrayname + " = " + getter + "(\"" + getname
				+ "\");\n");
	}

	@Override
	public int compareTo(Holder o) {
		int val = bankName.compareToIgnoreCase(o.bankName);
		if (val == 0) {
			val = sectionName.compareToIgnoreCase(o.sectionName);
			if (val == 0) {
				val = columnName.compareToIgnoreCase(o.columnName);
			}
		}

		return val;
	}

}
