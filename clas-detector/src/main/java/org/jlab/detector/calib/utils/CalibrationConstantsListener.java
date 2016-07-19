/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.detector.calib.utils;

/**
 *
 * @author gavalian
 */
public interface CalibrationConstantsListener {
    void constantsEvent(CalibrationConstants cc, int col, int row);
}
