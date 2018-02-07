package org.jlab.rec.rich;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import org.jlab.clas.detector.DetectorData;
import org.jlab.clas.detector.DetectorEvent;

import org.jlab.clas.physics.GenericKinematicFitter;
import org.jlab.clas.physics.PhysicsEvent;
import org.jlab.clas.reco.ReconstructionEngine;
import org.jlab.geom.prim.Vector3D;
import org.jlab.groot.data.H1F;
import org.jlab.groot.data.H2F;
import org.jlab.groot.graphics.EmbeddedCanvas;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.evio.EvioDataEvent;
import org.jlab.io.hipo.HipoDataSource;

public class RICHEBEngine extends ReconstructionEngine {

	public RICHEBEngine() {
		super("RICHEB", "mcontalb-kenjo", "3.0");
	}

	int Run = -1;
	
	@Override
	public boolean init() {
		return true;
	}

	@Override
	public boolean processDataEvent(DataEvent event) {
            return true;
	}
	
        
    public static void main (String arg[]) {
	}
}
