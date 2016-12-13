/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jlab.physics.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jlab.clas.physics.Particle;
import org.jlab.clas.physics.PhysicsEvent;

/**
 *
 * @author gavalian
 */
public class LundReader {
	private final ArrayList<String> inputFiles = new ArrayList<String>();
	private BufferedReader reader = null;
	private PhysicsEvent physEvent = new PhysicsEvent();

	public LundReader() {

	}

	public LundReader(String file) {
		this.addFile(file);
		this.open();
	}

	public void addFile(String file) {
		inputFiles.add(file);
	}

	public void open() {
		this.openFile(0);
	}

	private Boolean openFile(int counter) {
		try {
			File file = new File(inputFiles.get(counter));
			reader = new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException ex) {
			Logger.getLogger(LundReader.class.getName()).log(Level.SEVERE, null, ex);
		}
		return true;
	}

	public PhysicsEvent getEvent() {
		return physEvent;
	}

	public Boolean next() {
		try {
			physEvent.clear();
			String header = reader.readLine();
			if (header == null)
				return false;
			String[] tokens = header.trim().split("\\s+");
			// System.err.println("TOKENS size = " + tokens.length);
			// for(int loop = 0; loop < tokens.length; loop++){
			// System.err.println(" token " + loop + " = " + tokens[loop]);
			// }
			if (tokens.length != 10)
				return false;
			Integer nrows = Integer.parseInt(tokens[0]);
			if (nrows < 1)
				return false;
			physEvent.addProperty("nPart", nrows);
			physEvent.addProperty("nTarNucl", Double.parseDouble(tokens[1]));
			physEvent.addProperty("nTarProt", Double.parseDouble(tokens[2]));
			physEvent.addProperty("tarPol", Double.parseDouble(tokens[3]));
			physEvent.addProperty("beamPol", Double.parseDouble(tokens[4]));
			physEvent.addProperty("x", Double.parseDouble(tokens[5]));
			physEvent.addProperty("y", Double.parseDouble(tokens[6]));
			physEvent.addProperty("W", Double.parseDouble(tokens[7]));
			physEvent.addProperty("Q2", Double.parseDouble(tokens[8]));
			physEvent.addProperty("nu", Double.parseDouble(tokens[9]));

			for (int loop = 0; loop < nrows; loop++) {
				String particleLine = reader.readLine();
				if (particleLine != null) {
					String[] params = particleLine.trim().split("\\s+");
					// System.err.println("PARAMS LENGTH = " + params.length);
					if (params.length == 14) {

						int pid = Integer.parseInt(params[3]);
						// System.err.println("PID = " + pid);
						int status = Integer.parseInt(params[2]);
						double px = Double.parseDouble(params[6]);
						double py = Double.parseDouble(params[7]);
						double pz = Double.parseDouble(params[8]);
						double vx = Double.parseDouble(params[11]);
						double vy = Double.parseDouble(params[12]);
						double vz = Double.parseDouble(params[13]);
						if (status == 1) {
							physEvent.addParticle(new Particle(pid, px, py, pz, vx, vy, vz));
						}
					}
				}
			}
			return true;
		} catch (IOException ex) {
			Logger.getLogger(LundReader.class.getName()).log(Level.SEVERE, null, ex);
		}
		return false;
	}
}
