/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jlab.io.bos;

/**
 *
 * @author gavalian
 */
public class BosBankHeader {
    
    int NAME_START_INDEX;
    int NROWS_WORD_INDEX;
    int NCOLS_WORD_INDEX;
    int NDATA_START_INDEX;
    int NWORDS_WORD_INDEX;
        
    public BosBankHeader(int nameIndex){
        NAME_START_INDEX  = nameIndex;
        NDATA_START_INDEX = NAME_START_INDEX - 4;
        NCOLS_WORD_INDEX  = NAME_START_INDEX + 12;
        NROWS_WORD_INDEX  = NAME_START_INDEX + 16;
        NWORDS_WORD_INDEX = NAME_START_INDEX + 28;
    }
}
