/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas.utils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author gavalian
 */
public class CoatUtilsJson {
    
    public static String getJsonList(List list){
        StringBuilder str = new StringBuilder();
        if(list.get(0) instanceof Integer){
            str.append("[");
            int counter = 0;
            for(Integer item : ( List<Integer> ) list){
                if(counter!=0) str.append(",");
                str.append(item);
                counter++;
            }
            str.append("]");
        }
    
        if(list.get(0) instanceof Double){
            str.append("[");
            int counter = 0;
            for(Double item : ( List<Double> ) list){
                if(counter!=0) str.append(",");
                str.append(item);
                counter++;
            }
            str.append("]");
        }
        
        if(list.get(0) instanceof String){
            str.append("[");
            int counter = 0;
            for(String item : ( List<String> ) list){
                if(counter!=0) str.append(",");
                str.append("\"");
                str.append(item);
                str.append("\"");                
                counter++;
            }
            str.append("]");
        }
        
        return str.toString();
    }
    
    
    public static String getJsonMap(Map<String,Object> map){
        StringBuilder str = new StringBuilder();
        int counter = 0;
        str.append("{");
        for(Map.Entry<String,Object> object : map.entrySet()){
            if(counter!=0) str.append(", ");
            if(object.getValue() instanceof List){
                str.append("\"").append(object.getKey()).append("\":");
                str.append(CoatUtilsJson.getJsonList((List)object.getValue()));
            }
            if(object.getValue() instanceof Integer || object.getValue()
                    instanceof String || object.getValue() instanceof Double){
                str.append(CoatUtilsJson.getJsonPair(object.getKey(), object.getValue()));
            }
            if(object.getValue() instanceof Map){
                str.append("\"").append(object.getKey()).append("\":");
                str.append(CoatUtilsJson.getJsonMap((Map<String,Object>) object.getValue()));
            }
            //str.append(CoatUtilsJson.getJsonPair(object.getKey(), object));
            counter++;
        }
        str.append("}");
        return str.toString();
    }
    
    public static String getJsonPair(String key, Object obj){
        StringBuilder str = new StringBuilder();
        //System.out.println("adding : " + key + " : " + obj);
        str.append("\"").append(key).append("\"");
        if(obj instanceof List){
            String jsonList = CoatUtilsJson.getJsonList((List) obj);
            str.append(":").append(jsonList);
        }
        if(obj instanceof Integer){
            str.append(":").append(obj);
        }
        if(obj instanceof Double){
            str.append(":").append(obj);
        }
        if(obj instanceof String){
            str.append(":").append("\"").append(obj).append("\"");
        }
        return str.toString();
    }
    
    public static Map   parseMap(String strMap){
        
        Map<String,Object> objectMap = new LinkedHashMap<String,Object>();
        if(strMap.startsWith("{")==true && strMap.endsWith("}")==true){
            String[] tokens = strMap.substring(1, strMap.length()-1).split(",");
            
            for(String token : tokens){
                System.out.println("token = [" + token + "]");
                String[] key = token.trim().split(":");
                if(key.length!=2){
                    System.out.println("error");
                } else {
                    
                }
            }
        }
        return objectMap;
    }
    
    public static List  parseList(String strList){
        List<Object>  parsedList = new ArrayList<Object>();
        if(strList.startsWith("[")==true&&strList.endsWith("]")){
            
        }
        return parsedList;
    }
    
    public static String  getMapString(String json, String mapName){
        String name = String.format("\"%s\"", mapName);
        if(json.contains(name)==true){
            int index = json.indexOf(name);
            int separator    = json.indexOf(":", index);
            int bracketEnd   = json.indexOf("}", separator);
            int bracketStart = json.indexOf("{", separator);
            if(bracketEnd<json.length()&&bracketEnd>=0&&
                    bracketStart<json.length()&&bracketStart>=0&&
                    bracketStart<bracketEnd
                    ){
                return json.substring(bracketStart, bracketEnd+1);
            }
        }
        return null;
    }
    
    public static List<String>  getListMaps(String json){
        List<String>  maps = new ArrayList<String>();
        int indexS = json.indexOf("{");
        int indexE = json.indexOf("}",indexS);
        while(indexS>=0&&indexE>=0&&indexS<json.length()){
            String mapString = json.substring(indexS, indexE+1);
            maps.add(mapString);
            indexS = json.indexOf("{", indexE);
            indexE = json.indexOf("}", indexS);
        }
        return maps;
    }
    /**
     * Find enclosing bracket for give opening bracket.
     * @param json json string
     * @param start starting position of opening bracket
     * @param brs opening bracket string
     * @param bre closing bracket string
     * @return 
     */
    public static int getNextIndex(String json, int start, char brs, char bre){
        int sbcount = 0;
        for(int loop = start + 1; loop < json.length(); loop++){
            if(json.charAt(loop)==brs) sbcount++;
            if(json.charAt(loop)==bre){
                if(sbcount==0){
                    return loop;
                } else {
                    sbcount--;
                }
            }
        }
        return -1;
    }
    
    public static String getValueString(String json, String key){
        String name = String.format("\"%s\"", key);
        //System.out.println("loopking for " + key);
        if(json.contains(name)==true){
            int index = json.indexOf(name);          
            int separator    = json.indexOf(":", index);
            int end          = json.indexOf(",", separator);
            int endBr          = json.indexOf("}", separator);

            //System.out.println("found  " + key + " at " + index 
             //       + " separator " + separator + "  end " + end);
            String stringValue = null;
            
            if(separator>=0&&end>=0){
                stringValue = json.substring(separator+1, end).trim();
            } else if (separator>=0&&endBr>=0) {
                stringValue = json.substring(separator+1,endBr).trim();
            }
            
            if(stringValue.startsWith("\"")&&stringValue.endsWith("\"")){
                return stringValue.substring(1,stringValue.length()-1);
            } else {
                return stringValue;
            }
        }
        return null;
    }
    
    public static List<Integer> getListAsInteger(String json){
        List<Integer> iList = new ArrayList<Integer>();
        
        return iList;
    }
    
    public static List<String> getListAsString(String json){
        List<String> sList = new ArrayList<String>();
        String[] values = json.substring(1, json.length()-1).split(",");
        //System.out.println("parsing list: " + json + " " );
        for(String item : values){
            //System.out.println("ITEM = " + item);
            if(item.startsWith("\"")&&item.endsWith("\"")){
                sList.add(item.substring(1, item.length()-1));
            } else {
                sList.add(item);
            }
        }
        return sList;
    }
    
    public static String getListString(String json, String listName){
        String name = String.format("\"%s\"", listName);
        if(json.contains(name)==true){
            int index = json.indexOf(name);
            int separator    = json.indexOf(":", index);
            int bracketEnd   = json.indexOf("]", separator);
            int bracketStart = json.indexOf("[", separator);
            if(bracketEnd<json.length()&&bracketEnd>=0&&
                    bracketStart<json.length()&&bracketStart>=0&&
                    bracketStart<bracketEnd
                    ){
                return json.substring(bracketStart, bracketEnd+1);
            }
        }
        return null;
    }
    
    public static void main(String[] args){
        Map<String,Object> map = new LinkedHashMap<String,Object>();

        List<String> ab = new ArrayList<String>();
        ab.add("cow");
        ab.add("dog");
        ab.add("cat");
        map.put("index", 1);
        map.put("value", 2.4);
        map.put("options",ab);

        Map<String,Object> mapL = new LinkedHashMap<String,Object>();
        mapL.put("map", map);
        mapL.put("type", "descriptor");
        mapL.put("name", "DCHB");
        System.out.println(CoatUtilsJson.getJsonMap(mapL));
        System.out.println("MAP ----> " + mapL);
        CoatUtilsJson.parseMap("{\"bank\":\"DCHB\", \"tag\":120 }");
        String json = CoatUtilsJson.getJsonMap(mapL);
        System.out.println(CoatUtilsJson.getListString(json, "options"));
        System.out.println(CoatUtilsJson.getMapString(json, "map"));
        String  value = CoatUtilsJson.getValueString(json, "value");
        System.out.println(value);
        
        
        String jsonBank = "{\n" +
                "\"bank\"  :\"DCHB\",\n" +
                "\"tag\"   :122,\n" +
                "\"parent\":120,\n" +
                "\"rows\":[\n" +
                "        {\"name\":\"px\",   \"type\": \"int32\", \"order\":1, \"desc\":\"particle momentum X component\"},\n" +
                "	{\"name\":\"py\",   \"type\":\"double\", \"order\":2, \"desc\":\"particle momentum Y component\"}\n" +
                "	{\"name\":\"pz\",   \"type\":\"double\", \"order\":3, \"desc\":\"particle momentum Z component\"}\n" +
                "        {\"name\":\"beta\", \"type\":\"double\", \"order\":4, \"desc\":\"beta of the particle\"}\n" +
                "       ]\n" +
                "}";
        
        String  rows = CoatUtilsJson.getListString(jsonBank, "rows");
        System.out.println(rows);
        List<String>  entries = CoatUtilsJson.getListMaps(rows);
        System.out.println(entries);
    }
}
