package cnuphys.ced.clasio.tojava;

import javax.swing.JTextArea;

/**
 * Used to hold information during the converstion xml to java process
 * 
 * @author heddle
 *
 */
public class Holder {
    String bankName;
    String sectionName;
    String columnName;
    String colInfo;
    String colType;
    String arrayname;
    String getname;
    String javatype = "???[]";
    String getter = "event.get???";

    JTextArea textArea;

    public Holder(final JTextArea ta, String bankName, String sectionName,
	    String columnName, String colInfo, final String colType) {
	super();
	textArea = ta;
	this.bankName = bankName;
	this.sectionName = sectionName;
	this.columnName = columnName;
	this.colInfo = colInfo;
	this.colType = colType;

	arrayname = sectionName.toLowerCase() + "_" + columnName;
	getname = bankName + "::" + sectionName + "." + columnName;

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
	// textArea.append("    if(event.hasBank(\"" + getname +"\")) " +
	// arrayname + " = " + getter + "(\"" + getname + "\");\n");
	textArea.append("    " + arrayname + " = " + getter + "(\"" + getname
		+ "\");\n");
    }

}
