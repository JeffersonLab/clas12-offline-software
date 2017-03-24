package org.jlab.detector.geant4.v2.SVT;

import org.jlab.detector.units.Measurement;
import org.jlab.detector.units.SystemOfUnits.Angle;
import org.jlab.detector.volume.Geant4Basic;

/**
 * @author pdavies
 */
public class G4Pgon extends Geant4Basic {

	public G4Pgon( String name, int nsides, double phi0, double dphi ) {
		super( new Pgon( nsides,  phi0,  dphi ));
		setName( name );
		setType("Polyhedra");
		setDimensions( new Measurement(nsides,""), Angle.value(phi0), Angle.value(dphi) );
	}

}
