package jsonToJava;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
import javax.json.stream.JsonParsingException;

public class JsonParser {

	/**
	 * Return a Json object corresponding to a Json file
	 * 
	 * @param path
	 *            the path the file
	 * @return the Json object
	 */
	public static JsonArray parseJsonFile(String path) {
		URL url = null;

		try {
			url = new File(path).toURI().toURL();
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		}

		JsonArray jsonArray = null;
		try (InputStream is = url.openStream(); JsonReader rdr = Json.createReader(is)) {
			jsonArray = rdr.readArray();
		} catch (FileNotFoundException e) {
			e.printStackTrace();		
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JsonParsingException e) {
			e.printStackTrace();
	}

		return jsonArray;
	}
	
	/**
	 * Return a Json object corresponding to a Json file
	 * 
	 * @param is
	 *            the input stream
	 * @return the Json object
	 */
	public static JsonObject parseJsonFile(InputStream is) {

		JsonObject jsonObj = null;

		if (is != null) {
			try {
				JsonReader rdr = Json.createReader(is);
				jsonObj = rdr.readObject();
				is.close();
			} catch (JsonParsingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return jsonObj;
	}

	/**
	 * Extract an child json object from a json object
	 * @param key the key
	 * @param obj the parent object
	 * @return the object, or null on error
	 */
	public static JsonObject getObject(String key, JsonObject obj) {
		
		JsonObject val = null;
		
		if ((obj != null) && (key == null) && obj.containsKey(key)) {
			JsonValue jv = obj.get(key);
			if (jv != null) {
				try {
					val = (JsonObject)jv;
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		return val;
	}
	
	
	/**
	 * Extract an child json array from a json object
	 * 
	 * @param key
	 *            the key
	 * @param obj
	 *            the parent object
	 * @return the json array, or null on error
	 */
	public static JsonArray getJsonArray(String key, JsonObject obj) {

		JsonArray val = null;

		if ((obj != null) && (key != null) && obj.containsKey(key)) {
			try {
				val = obj.getJsonArray(key);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return val;
	}
	
	/**
	 * Extract an int from a json object
	 * @param key the key
	 * @param obj the object
	 * @return the value, or Integer.MIN_VALUE on error
	 */
	public static int getInt(String key, JsonObject obj) {
		
		int val = Integer.MIN_VALUE;
		
		if ((obj != null) && (key != null) && obj.containsKey(key)) {
			JsonValue jv = obj.get(key);
			if (jv != null) {
				try {
					val = Integer.parseInt(jv.toString());
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		return val;
	}
	
	/**
	 * Extract a long from a json object
	 * @param key the key
	 * @param obj the object
	 * @return the vale, or Long.MIN_VALUE on error
	 */
	public static long getLong(String key, JsonObject obj) {
		
		long val = Long.MIN_VALUE;
		
		if ((obj != null) && (key != null) && obj.containsKey(key)) {
			JsonValue jv = obj.get(key);
			if (jv != null) {
				try {
					val = Long.parseLong(jv.toString());
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		return val;
	}
	
	/**
	 * Extract a string from a json object
	 * 
	 * @param key
	 *            the key
	 * @param obj
	 *            the object
	 * @return the string, or null on error
	 */
	public static String getString(String key, JsonObject obj) {

		String val = null;
		if ((obj != null) && (key != null) && obj.containsKey(key)) {
			try {
				val = obj.getString(key);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return val;
	}

	/**
	 * Extract a double from a json object
	 * 
	 * @param key
	 *            the key
	 * @param obj
	 *            the object
	 * @return the value, or NaN on error
	 */
	public static double getDouble(String key, JsonObject obj) {
		
		double val = Double.NaN;
		
		if ((obj != null) && (key != null) && obj.containsKey(key)) {
			JsonValue jv = obj.get(key);
			if (jv != null) {
				try {
					val = Double.parseDouble(jv.toString());
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		
		return val;
	}
	
	/**
	 * Very basic formatter for a json string
	 * @param jsonStr the json string
	 * @return a reasonably formatted version
	 */
    public static String jsonFormatter(JsonObject jobj) {
		if (jobj == null) {
			return "";
		}
    	return jsonFormatter(jobj.toString());
    }
    
	/**
	 * Very basic formatter for a json string
	 * @param jsonStr the json string
	 * @return a reasonably formatted version
	 */
    public static String jsonFormatter(String jsonStr) {
		if ((jsonStr == null) || (jsonStr.length() < 1)) {
			return "";
		}

		int len = jsonStr.length();
		StringBuilder sb = new StringBuilder(len + len / 4);
		
		String whiteSpace = "";

		int quoteLevel = 0;
		
		for (char c : jsonStr.toCharArray()) {
			switch (c) {
			
			case '"':
				quoteLevel = (quoteLevel+1) % 2;
				sb.append(c);
				break;

			case '\n':
				break;

			case '[':
				if (quoteLevel == 0) {
					sb.append(" ");
					sb.append(c);
					sb.append('\n');
					whiteSpace += "  ";
					sb.append(whiteSpace);
				}				
				else {
					sb.append(c);
				}
				break;

			case ']':
				if (quoteLevel == 0) {
					sb.append('\n');
					whiteSpace = whiteSpace.replaceFirst("  ", "");
					sb.append(whiteSpace);
					sb.append(c);
				} else {
					sb.append(c);
				}
				break;

			case '{':
				if (quoteLevel == 0) {
					sb.append(" ");
					sb.append(c);
					sb.append('\n');
					whiteSpace += "  ";
					sb.append(whiteSpace);
				} else {
					sb.append(c);
				}
				break;

			case '}':
				if (quoteLevel == 0) {
					sb.append('\n');
					whiteSpace = whiteSpace.replaceFirst("  ", "");
					sb.append(whiteSpace);
					sb.append(c);
				} else {
					sb.append(c);
				}
				break;

			case ',':
				if (quoteLevel == 0) {
					sb.append(c);
					sb.append('\n');
					sb.append(whiteSpace);
				} else {
					sb.append(c);
				}
				break;

			default:
				sb.append(c);
			} //switch
		} //for

		return sb.toString();
	}
}