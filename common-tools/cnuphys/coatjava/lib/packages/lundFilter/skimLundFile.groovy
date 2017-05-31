//*********************************************
// 
//*********************************************
import org.jlab.physics.io.*;
import org.jlab.clas.physics.*;

int        nEvents = Integer.parseInt(argv[0]);
String   inputFile = argv[1];

LundReader      reader = new LundReader();

reader.addFile(inputFile);
reader.open();

int       evCounter = 0;
int selectedCounter = 0;

while(reader.next()==true){
	PhysicsEvent genEvent = reader.getEvent();
	evCounter++;
	if(evCounter>=nEvents) break;
}

System.out.println("[][][] ====>  processed events # " + evCounter + "  selected " + selectedCounter);
