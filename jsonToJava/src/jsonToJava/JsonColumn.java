package jsonToJava;

import javax.json.JsonObject;


public class JsonColumn {

	public JsonBank bank;
	public String name;
	public int id;
	public String type;
	public String info;
	
	public JsonColumn(String name, int id, String type, String info) {
		super();
		this.name = name;
		this.id = id;
		this.type = type;
		this.info = info;
	}

	public String varName() {
		return name + "_" + bank.secondName();
	}
	
	public String fullName() {
		return bank.bank + "." + name;
	}
	
	public static JsonColumn fromJsonObject(JsonObject jobj) {
		String name = jobj.getString("name");
		int id = jobj.getInt("id");
		String type = jobj.getString("type");
		String info = jobj.getString("info");
		return new JsonColumn(name, id, type, info);
	}
	
	public String read() {
		String s = "    " + varName() + " = ColumnData.";
		if (type.equalsIgnoreCase("int8")) {
			s += "getByteArray(\"" + bank + "." + name + "\");\n";
		}
		else if (type.equalsIgnoreCase("int16")) {
			s += "getShortArray(\"" + bank + "." + name + "\");\n";
		}
		else if (type.equalsIgnoreCase("int32")) {
			s += "getIntArray(\"" + bank + "." + name + "\");\n";
		}
		else if (type.equalsIgnoreCase("float")) {
			s += "getFloatArray(\"" + bank + "." + name + "\");\n";
		}
		else {
			System.err.println("UNKNOWN TYPE: [" + type + "] in JsonColumn.declaration." );
			System.exit(1);
		}


		return s;
	}
	
	public String rawGetter() {
		String s = "\n//" + info + "\n";
	    s += "  ";
	    
	    String array = "";
		if (type.equalsIgnoreCase("int8")) {
			s += "public byte[] get_";
			array = "getByteArray(";
		}
		else if (type.equalsIgnoreCase("int16")) {
			s += "public short[] get_";
			array = "getShortArray(";
		}
		else if (type.equalsIgnoreCase("int32")) {
			s += "public int[] get_";
			array = "getIntArray(";
		}
		else if (type.equalsIgnoreCase("float")) {
			s += "public float[] get_";
			array = "getFloatArray(";
		}
		else {
			System.err.println("UNKNOWN TYPE: [" + type + "] in JsonColumn.rawGetter." );
			System.exit(1);
		}
		
		s += (varName() + "() {return ColumnData." + array + "\"" +
		fullName() + "\");}");

		return s;
	}
	
	public String staticRawGetter() {
		String s = "\n//" + info + "\n";
	    s += "  ";
	    
	    String array = "";
		if (type.equalsIgnoreCase("int8")) {
			s += "static public byte[] get_";
			array = "getByteArray(";
		}
		else if (type.equalsIgnoreCase("int16")) {
			s += "static public short[] get_";
			array = "getShortArray(";
		}
		else if (type.equalsIgnoreCase("int32")) {
			s += "static public int[] get_";
			array = "getIntArray(";
		}
		else if (type.equalsIgnoreCase("float")) {
			s += "static public float[] get_";
			array = "getFloatArray(";
		}
		else {
			System.err.println("UNKNOWN TYPE: [" + type + "] in JsonColumn.rawGetter." );
			System.exit(1);
		}
		
		s += (varName() + "() {return ColumnData." + array + "\"" +
		fullName() + "\");}");

		return s;
	}

	
	public String getter() {
		String s = "\n//" + info + "\n";
	    s += "  ";
		if (type.equalsIgnoreCase("int8")) {
			s += "public byte[] get_";
		}
		else if (type.equalsIgnoreCase("int16")) {
			s += "public short[] get_";
		}
		else if (type.equalsIgnoreCase("int32")) {
			s += "public int[] get_";
		}
		else if (type.equalsIgnoreCase("float")) {
			s += "public float[] get_";
		}
		else {
			System.err.println("UNKNOWN TYPE: [" + type + "] in JsonColumn.getter." );
			System.exit(1);
		}
		
		s += (varName() + "() {return " + varName() + ";}\n");

		return s;
	}
	
	public String nullify() {
		return "    " +  varName() + " = null;\n";
	}
	
	public String declaration() {
		String s = "  ";
		if (type.equalsIgnoreCase("int8")) {
			s += "private byte[] ";
		}
		else if (type.equalsIgnoreCase("int16")) {
			s += "private short[] ";
		}
		else if (type.equalsIgnoreCase("int32")) {
			s += "private int[] ";
		}
		else if (type.equalsIgnoreCase("float")) {
			s += "private float[] ";
		}
		else {
			System.err.println("UNKNOWN TYPE: [" + type + "] in JsonColumn.declaration." );
			System.exit(1);
		}
		
		s += ( varName() + ";\n");


		return s;
	}
}
