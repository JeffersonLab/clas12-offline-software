/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.physics.base;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author gavalian
 */
public class ParticleStringParser {
    
    public static final String  SQUARE_BRACKET_OPEN = "[";
    public static final String SQUARE_BRACKET_CLOSE = "]";
    public static final String   CURLY_BRACKET_OPEN = "{";
    public static final String  CURLY_BRACKET_CLOSE = "}";
    public static final String   ROUND_BRACKET_OPEN = "(";
    public static final String  ROUND_BRACKET_CLOSE = ")";
    
    public static final int    BRACKET_TYPE_SQUARE  = 1;    
    public static final int     BRACKET_TYPE_ROUND  = 2;
    public static final int     BRACKET_TYPE_CURLY  = 3;

    public static final int   BRACKET_TYPE_UNKNOWN  = -1;
    
    public ParticleStringParser(){
        
    }
    
    private int minIndex(int[] index){
        List<Integer> idx = new ArrayList<Integer>();
        for(int i = 0; i < index.length; i++){
            if(index[i]>=0) idx.add(index[i]);
        }
        
        int idxSize = idx.size();
        
        switch (idxSize){
            case 0: return -1;
            case 1: return idx.get(0);
            case 2: return Math.min(idx.get(0), idx.get(1));
            case 3: return Math.min(idx.get(2),Math.min(idx.get(0), idx.get(1)));
            default: return -1;
        }        
    }
    
    private int getPositionOpen(String str, int start_index){
        int[] bracket_index = new int[3];
        bracket_index[0] = str.indexOf(ParticleStringParser.CURLY_BRACKET_OPEN, start_index);
        bracket_index[1] = str.indexOf(ParticleStringParser.SQUARE_BRACKET_OPEN, start_index);
        bracket_index[2] = str.indexOf(ParticleStringParser.ROUND_BRACKET_OPEN, start_index);
        return this.minIndex(bracket_index);
    }
    
    private int getPositionClose(String str, int start_index){
        int[] bracket_index = new int[3];
        bracket_index[0] = str.indexOf(ParticleStringParser.CURLY_BRACKET_CLOSE, start_index);
        bracket_index[1] = str.indexOf(ParticleStringParser.SQUARE_BRACKET_CLOSE, start_index);
        bracket_index[2] = str.indexOf(ParticleStringParser.ROUND_BRACKET_CLOSE, start_index);
        return this.minIndex(bracket_index);
    }
    
    private int getBracketType(String str, int pstart, int pend){
        char bstart = str.charAt(pstart);
        char bend   = str.charAt(pend);
        if(bstart=='{'&&bend=='}'){
            return ParticleStringParser.BRACKET_TYPE_CURLY;
        }
        if(bstart=='['&&bend==']'){
            return ParticleStringParser.BRACKET_TYPE_SQUARE;
        }
        if(bstart=='('&&bend==')'){
            return ParticleStringParser.BRACKET_TYPE_ROUND;
        }
        return ParticleStringParser.BRACKET_TYPE_UNKNOWN;
    }
    
    public List<String> parse(String strOperation){
        
        int start_position = 0;
        String operation = strOperation.replaceAll("\\s+", "");
        List<String>  operands = new ArrayList<String>();
        
        while(start_position>=0&&start_position<operation.length()){
            int bracket_start = getPositionOpen(operation,start_position);
            if(bracket_start<0) break;
            int bracket_end = getPositionClose(operation,bracket_start);
            int type = getBracketType(operation,bracket_start,bracket_end);
            if(type<0) break;
            
            String  operator = operation.substring(bracket_start, bracket_end+1);
            if(bracket_start==0){
                operands.add("+"+operator);
            } else {
                if(operation.charAt(bracket_start-1)=='-'||operation.charAt(bracket_start-1)=='+'){
                    operands.add( operation.charAt(bracket_start-1) + operator);
                }
            }
            //System.out.println("bracket starts at : " + bracket_start + " end = " + bracket_end + "  TYPE = " + type);
            
            start_position = bracket_start+1;
        }
        
        return operands;
    }
    
    public static void main(String[] args){
        ParticleStringParser parser = new ParticleStringParser();
        List<String> operands = parser.parse("[11]+(321)+{2212,1}+[2212]");
        for(String item : operands){
            System.out.println("---> : " + item);
        }
    }
}
