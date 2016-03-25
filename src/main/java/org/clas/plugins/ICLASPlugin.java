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
public interface ICLASPlugin {
    String getName();
    String getAuthor();
    String getDescription();
    String getVersion();
    Class  newInstance();
}
