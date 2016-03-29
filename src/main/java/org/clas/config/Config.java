/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas.config;

import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

/**
 *
 * @author gavalian
 */
public class Config<T> implements ObservableValue<T> {
    
    public static void main(String[] args){
        
    }

    @Override
    public void addListener(ChangeListener<? super T> listener) {
        
    }

    @Override
    public void removeListener(ChangeListener<? super T> listener) {
        
    }

    @Override
    public T getValue() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addListener(InvalidationListener listener) {
        
    }

    @Override
    public void removeListener(InvalidationListener listener) {
        
    }
}
