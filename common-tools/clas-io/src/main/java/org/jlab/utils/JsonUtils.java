package org.jlab.utils;

import java.util.Map;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.jnp.utils.json.JsonArray;
import org.jlab.jnp.utils.json.JsonObject;
import org.jlab.jnp.utils.json.JsonValue;
import org.jlab.jnp.utils.json.PrettyPrint;
import org.json.JSONObject;

/**
 * Stuff to read and manipulate HIPO banks with JSON objects.
 * @author baltzell
 */
public class JsonUtils {

    /**
     * Just print it to the screen, nicely.
     * @param json 
     */
    public static void show(JsonObject json) {
        BufferedWriter buffWriter = new BufferedWriter(new OutputStreamWriter(System.out));
        try {
            json.writeTo(buffWriter,PrettyPrint.indentWithSpaces(4));
            buffWriter.newLine();
            buffWriter.flush();
        }
        catch (IOException e) {}
    }
    
    /**
     *  Just print it to the screen, nicely.
     * @param bank
     * @param varName 
     */
    public static void show(DataBank bank, String varName) {
        show(read(bank,varName));
    }
    
    /**
     * Convenience method to get a JsonObject from a bank.
     * @param bank bank to read
     * @param varName name of byte variable to read
     * @return JSON generated from array of bytes
     */
    public static JsonObject read(DataBank bank, String varName) {
        return JsonObject.readFrom(new String(bank.getByte(varName)));
    }

    /**
     * This won't be useful once DataBank.setByte(String,byte[]) is implemented
     * @param bank bank to modify
     * @param varName name of bank byte variable to modify
     * @param contents contents of the variable, one byte per row
     * @return the input bank after modification 
     */
    public static DataBank fill(DataBank bank, String varName, String contents) {
        byte[] bytes = contents.getBytes();
        for (int row=0; row<bank.rows(); row++) {
            if (row >= bytes.length) {
                break;
            }
            bank.setByte(varName, row, bytes[row]);
        }
        return bank;
    }

    /**
     * Convert a map to JNP's JsonObject
     * WARNING:  presumably not generic to extended JSONs, but sufficient for
     * configuration sections of CLARA YAMLs.
     * @param map
     * @return 
     */
    public static JsonObject Map2Json(Map<String,Object> map) {
        JsonObject ret = new JsonObject();
        for (Map.Entry<String,Object> entry : map.entrySet()) {
            String topKey = entry.getKey();
            if (entry.getValue() instanceof Map) {
                ret.add(topKey,Map2Json((Map)entry.getValue()));
            }
            else {
                ret.add(topKey, entry.getValue().toString());
            }
        }
        return ret;
    }
    
    /**
     * Convert from Map to JNP's JsonObject
     * @param json an org.json.JSONObject
     * @return the corresponding org.jlab.jnp.utils.json.JsonObject
     */
    public static JsonObject JSON2Json(JSONObject json) {
        return Map2Json(json.toMap());
    }

    /**
     * Convenience method to create a bank containing a JsonObject.
     * @param event event used to generate the bank
     * @param bankName name of bank to create
     * @param varName name of variable to put the JSON object in
     * @param json contents for the variable
     * @return new bank
     */
    public static DataBank create(DataEvent event, String bankName, String varName, JsonObject json) {
        DataBank bank = event.createBank(bankName, json.toString().length());
        fill(bank, varName, json.toString());
        return bank;
    }

    /**
     * Convenience method to create a bank containing a JsonObject.
     * @param event event used to generate the bank
     * @param bankName name of bank to create
     * @param varName name of variable to put the JSON object in
     * @param json contents for the variable
     * @return new bank
     */
    public static DataBank create(DataEvent event, String bankName, String varName, JSONObject json) {
        return create(event, bankName, varName, JSON2Json(json));
    }
    
    /**
     * Convenience method to create a bank containing a JsonObject.
     * @param event event used to generate the bank
     * @param bankName name of bank to create
     * @param varName name of variable to put the JSON object in
     * @param json contents for the variable
     * @return new bank
     */
    public static DataBank create(DataEvent event, String bankName, String varName, Map json) {
        return create(event, bankName, varName, Map2Json(json));
    }

    /**
     * JsonObject's merge method overwrites keys of the same name, this extends.
     * WARNING:  presumably not generic to extended JSONs, but sufficient for
     * configuration sections of CLARA YAMLs.
     * @param a
     * @param b
     * @return a+b
     */
    public static JsonObject add(JsonObject a, JsonObject b) {
        JsonObject ret = new JsonObject();
        for (String key : a.names()) {
            if (b.names().contains(key)) {
                JsonValue aval = a.get(key);
                JsonValue bval = b.get(key);
                if (aval instanceof JsonObject) {
                    JsonObject obj = new JsonObject();
                    JsonObject aobj = new JsonObject((JsonObject)aval);
                    JsonObject bobj = new JsonObject((JsonObject)bval);
                    for (String anam : aobj.names()) {
                        obj.set(anam, aobj.get(anam));
                    }
                    for (String bnam : bobj.names()) {
                        obj.set(bnam, bobj.get(bnam));
                    }
                    ret.set(key, obj);
                }
                else {
                    JsonArray arr = new JsonArray();
                    arr.add(a.get(key));
                    arr.add(b.get(key));
                    ret.set(key,arr);
                }
            }
            else {
                ret.set(key, a.get(key));
            }
        }
        for (String key : b.names()) {
            if (!a.names().contains(key)) {
                ret.set(key, b.get(key));
            }
        }
        return ret;
    }
    
    /**
     * Modify event by extending bank by merging new JSON data to existing.
     * If bank doesn't exist, create it.
     * @param event event to get the bank from and put it back into
     * @param bankName name of bank
     * @param varName name of variable within bank
     * @param extension JSON object to extend with
     * @return 
     */
    public static DataBank extend(DataEvent event, String bankName, String varName, JsonObject extension) {
        JsonObject contents;
        if (event.hasBank(bankName)) {
            contents = add(read(event.getBank(bankName), varName),extension);
            event.removeBank(bankName);
        }
        else {
            contents = new JsonObject(extension);
        }
        DataBank bank = create(event, bankName, varName, contents);
        event.appendBank(bank);
        return bank;
    }
    
    /**
     * Modify event by extending bank by merging new JSON data to existing.
     * If bank doesn't exist, create it.
     * @param event event to get the bank from and put it back into
     * @param bankName name of bank
     * @param varName name of variable within bank
     * @param extension JSON object to extend with
     * @return 
     */
    public static DataBank extend(DataEvent event, String bankName, String varName, JSONObject extension) {
        return extend(event, bankName, varName, JSON2Json(extension));
    }
    
    /**
     * Modify event by extending bank by merging new JSON data to existing.
     * If bank doesn't exist, create it.
     * @param event event to get the bank from and put it back into
     * @param bankName name of bank
     * @param varName name of variable within bank
     * @param extension JSON object to extend with
     * @return 
     */
    public static DataBank extend(DataEvent event, String bankName, String varName, Map<String,Object> extension) {
        return extend(event, bankName, varName, Map2Json(extension));
    }

    /**
     * Emulate the way CLARA parses the full YAML and presents it in EngineData.
     * The "global" and "service" subsections in the "configuration" section get
     * squashed into one namespace, and service-specific keys override any
     * globals of the same name.
     *
     * @param claraJson the full CLARA YAML contents
     * @param serviceName the name of the service in CLARA YAML (not class name)
     * @return data in the format the given CLARA service would see
     */
    public static JSONObject filterClaraYaml(JSONObject claraJson, String serviceName) {
        JSONObject ret = new JSONObject();
        if (claraJson.has("configuration")) {
            JSONObject config = claraJson.getJSONObject("configuration");
            if (config.has("global")) {
                JSONObject globals = config.getJSONObject("global");
                for (String key : globals.keySet()) {
                    ret.accumulate(key, globals.getString(key));
                }
            }
            if (config.has("services")) {
                if (config.getJSONObject("services").has(serviceName)) {
                    JSONObject service = config.getJSONObject("services").getJSONObject(serviceName);
                    for (String key : service.keySet()) {
                        ret.put(key, service.getString(key));
                    }
                }
            }
        }
        return ret;
    }
    
}
