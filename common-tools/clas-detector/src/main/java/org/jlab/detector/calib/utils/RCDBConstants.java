package org.jlab.detector.calib.utils;
import java.util.HashMap;
import java.sql.Time;

/**
 *
 * @author baltzell
 */
public class RCDBConstants {

    public static class RCDBConstant<T> {
        T obj;
        RCDBConstant(T obj) { this.obj=obj; }
        public T getValue() { return this.obj; }
    }

    private final HashMap<String,RCDBConstant> data=new HashMap<>();

    public RCDBConstants(){};

    public void add(String key, RCDBConstant value) {
        data.put(key,value);
    }
    public void add(String key,Double value) {
        data.put(key,new RCDBConstant(value));
    }
    public void add(String key,Long value) {
        data.put(key,new RCDBConstant(value));
    }
    public void add(String key,String value) {
        data.put(key,new RCDBConstant(value));
    }
    public void add(String key,Time value) {
        data.put(key,new RCDBConstant(value));
    }

    public void show() {
        data.keySet().forEach((name) -> {
            System.out.println(String.format("   %-30s",name)+" "+data.get(name).getValue());
        });
    }

    public RCDBConstant get(String key) {
        if (data.containsKey(key)) return data.get(key);
        return null;
    }
    public Double getDouble(String key) {
        if (data.containsKey(key)) {
            if (data.get(key).getValue() instanceof Double) {
                return (Double) data.get(key).getValue();
            }
        }
        return null;
    }
    public String getString(String key) {
        if (data.containsKey(key)) {
            if (data.get(key).getValue() instanceof String) {
                return (String) data.get(key).getValue();
            }
        }
        return null;
    }
    public Long getLong(String key) {
        if (data.containsKey(key)) {
            if (data.get(key).getValue() instanceof Long) {
                return (Long) data.get(key).getValue();
            }
        }
        return null;
    }
    public Time getTime(String key) {
        if (data.containsKey(key)) {
            if (data.get(key).getValue() instanceof Double) {
                return (Time) data.get(key).getValue();
            }
        }
        return null;
    }

}
