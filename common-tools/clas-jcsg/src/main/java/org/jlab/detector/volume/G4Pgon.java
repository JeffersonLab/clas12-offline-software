package org.jlab.detector.volume;

import org.jlab.geometry.prim.Pgon;
import org.jlab.detector.units.Measurement;
import org.jlab.detector.units.SystemOfUnits.Angle;
import org.jlab.detector.units.SystemOfUnits.Length;

/**
 * @author pdavies/devita
 */
// FIXME: currently support only polyheadra defintion to gemc geometry
public class G4Pgon extends Geant4Basic {

	public G4Pgon(String name, double phiStart, double phiTotal, int numSides, int numZPlanes,
                        double[] zPlane, double[] rInner, double[] rOuter ) {
            
              super( new Pgon(phiStart, phiTotal, numSides, numZPlanes, zPlane, rInner, rOuter));
              setName( name );
              setType("Pgon");
              
              Measurement[] dimensions = new Measurement[4+3*numZPlanes];
              dimensions[0] = Angle.value(phiStart);
              dimensions[1] = Angle.value(phiTotal);
              dimensions[2] = new Measurement(numSides,"counts");
              dimensions[3] = new Measurement(numZPlanes,"counts");
              for(int i=0; i<numZPlanes; i++) dimensions[4+0*numZPlanes+i] = Length.value(rInner[i]);
              for(int i=0; i<numZPlanes; i++) dimensions[4+1*numZPlanes+i] = Length.value(rOuter[i]);
              for(int i=0; i<numZPlanes; i++) dimensions[4+2*numZPlanes+i] = Length.value(zPlane[i]);
              setDimensions(dimensions);
        	}

}
