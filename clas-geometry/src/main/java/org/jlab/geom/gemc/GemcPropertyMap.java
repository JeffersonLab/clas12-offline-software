package org.jlab.geom.gemc;

import java.util.*;
import java.lang.UnsupportedOperationException;

class GemcPropertyMap extends LinkedHashMap<String,String> {

    boolean locked;

    /**
     * defines the keys and order thereof for this map
     **/
    protected GemcPropertyMap() {
        this.locked = false;
    }

    void lock() {
        this.locked = true;
    }

    /**
     * this prevents new keys from being added outside the constructor
     **/
    @Override
    public String put(String K, String V) {
        if (this.locked && !this.containsKey(K)) {
            throw new UnsupportedOperationException("Unknown key: "+K);
        } else {
            return super.put(K,V);
        }
    }

    /**
     * human-readable format
     **/
    @Override
    public String toString() {
        StringBuilder msg = new StringBuilder();
        for (Map.Entry<String,String> e : this.entrySet()) {
            msg.append(e.getKey()+": "+e.getValue()+"\n");
        }
        return msg.toString();
    }

    /**
     * text format expected by GEMC
     **/
    public String toPaddedString(List<Integer> pads, String sep) {
        StringBuilder msg = new StringBuilder();
        int i = 0;
        Iterator<Map.Entry<String, String>> itr = this.entrySet().iterator();
        while (itr.hasNext()) {
            if (msg.length() > 0) {
                msg.append(" "+sep+" ");
            }
            msg.append(String.format("%1$-" + pads.get(i) + "s", itr.next().getValue()));
            i += 1;
        }
        return msg.toString();
    }

    /**
     * returns the length of the strings stored in this map
     **/
    public List<Integer> getWidths(int minimum) {
        List<Integer> w = new ArrayList<Integer>();
        for (Map.Entry<String,String> e : this.entrySet()) {
            int l = e.getValue().length();
            if (l < minimum) {
                l = minimum;
            }
            w.add(l);
        }
        return w;
    }
}
