/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.service.dc;

/**
 *
 * @author ziegler
 */
public class DCHBPostClusterAI extends DCHBPostCluster {
    public DCHBPostClusterAI() {
        super("DCHBAI");
        super.aiAssist = true;
    }
}
