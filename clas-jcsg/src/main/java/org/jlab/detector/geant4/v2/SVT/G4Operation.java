package org.jlab.detector.geant4.v2.SVT;

import org.jlab.detector.volume.Geant4Basic;

/**
 * @author pdavies
 */
public class G4Operation extends Geant4Basic {

	public G4Operation( String name, String operation, String... operands ) {
		super( new Operation( operation, operands ) );
		setName( name );
		
		String expr = "none";
		
		switch( operation.toLowerCase() )
		{
		case "subtract":
			expr = operands[0]+" - "+operands[1];
		}
		
		setType("Operation: "+expr);
	}
	
}
