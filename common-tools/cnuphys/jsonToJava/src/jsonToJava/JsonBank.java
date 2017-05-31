package jsonToJava;

import javax.json.JsonArray;
import javax.json.JsonObject;

public class JsonBank {

	public String bank;
	public int group;
	public String info;
	public JsonColumn columns[];

	public JsonBank(String bank, int group, String info, JsonColumn[] columns) {
		super();
		this.bank = bank;
		this.group = group;
		this.info = info;
		
		if (columns != null) {
			for (JsonColumn column : columns) {
				column.bank = this;
			}
		}
		
		this.columns = columns;
	}
	
	@Override
	public String toString() {
		return bank;
	}
	
	public static JsonBank fromJsonObject(JsonObject jobj) {
		String bank = jobj.getString("bank");
		int group = jobj.getInt("group");
		String info = jobj.getString("info");
		JsonArray array = jobj.getJsonArray("items");
		
		JsonColumn columns[] = null;
		if (!array.isEmpty()) {
			columns = new JsonColumn[array.size()];
			for (int i = 0; i < array.size(); i++) {
				columns[i] = JsonColumn.fromJsonObject(array.getJsonObject(i));
			}
		}
		
		return new JsonBank(bank, group, info, columns);
	}
	
	public String secondName() {
		int index = bank.indexOf("::");
		return bank.substring(index+2);
	}
}
