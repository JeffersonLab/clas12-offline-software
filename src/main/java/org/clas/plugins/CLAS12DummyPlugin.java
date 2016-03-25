/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas.plugins;

/**
 *
 * @author gavalian
 */
public class CLAS12DummyPlugin implements ICLASPlugin {

    @Override
    public String getName() {
        return "Dummy Plugin";
    }

    @Override
    public String getAuthor() {
        return "gavalian";
    }

    @Override
    public String getDescription() {
        return "This plugin does absolutely nothing\n" +
                "It is used for testing plugin framework.";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public Class newInstance() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
