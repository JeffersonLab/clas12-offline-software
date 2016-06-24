/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jlab.process.base;

/**
 *
 * @author gavalian
 */
public interface CalibrationAlgorithm {
    void begin(String options);
    void processEvent();
    void end(String options);
}
