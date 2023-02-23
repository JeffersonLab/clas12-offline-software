import org.jlab.clasrec.io.*;
import org.jlab.clas.physics.*;
import org.jlab.clas.reactions.*;
import org.jlab.clas.physics.*;
import org.jlab.clas.fastmc.*;
import org.jlab.geom.prim.*;
import org.jlab.geom.*;
import org.jlab.geom.base.Detector;
import org.jlab.clas.fastmc.Clas12FastMC;
import org.jlab.clas.physics.PhysicsEvent;
import org.jlab.detector.base.DetectorType;
import org.jlab.detector.base.GeometryFactory;
import org.jlab.physics.io.LundReader;

import groovy.util.CliBuilder
import org.apache.commons.cli.HelpFormatter

double torusScale = -1.0;
double solenoidScale = 1.0;
boolean isCentral = false;
boolean isForwardTagger = false;
int nEvents = 1000;

// parse command line options:
def cli=new CliBuilder(usage:"gen.groovy -pid <#> [options]", stopAtNonOption:false);//, formatter:hf);
cli.with {
    pid args:1, argName:"#", required:true, "particle id"
    n   args:1, argName:"#", "number of events (default = 1000)"
    t   args:1, argName:"#", "torus scale factor (default = -1.0)"
    s   args:1, argName:"#", "solenoid scale factor (default = +1.0)"
    cd   "Central Detector"
    ft   "Forward Tagger"
    h    "print usage"
}

def options=cli.parse(args);
if (!options) System.exit(1);
if (options.h || !options.pid || options.arguments) {
    cli.usage(); println();
    System.exit(0);
}
final int pid=Integer.parseInt(options.pid);
if (options.n) nEvents=Integer.parseInt(options.n);
if (options.cd) isCentral=true;
if (options.ft) isForwardTagger=true;

// central + forwardtagger not supported yet:
if (isCentral && isForwardTagger)
    throw new RuntimeException("Unknown Combo:  FowardTagger and pid!=11 and pid!=22");

// contstruct filename:
String fileStub="electron";
if (isForwardTagger) fileStub+="FT";
switch (Math.abs(pid)) {
    case 2212:
        fileStub+="proton";
        break;
    case 2112:
        fileStub+="neutron";
        break;
    case 211:
        fileStub+="pion";
        break;
    case 321:
        fileStub+="kaon";
        break;
    case 22:
        fileStub+="gamma";
        break;
    case 45:
        fileStub+="deuteron";
        break;
    default:
        throw new RuntimeException("Invalid PID:  "+pid);
        break;
}
if (isCentral) fileStub+="C";
if (isForwardTagger && pid==22) fileStub="electrongammaFT";


File file = new File(fileStub+".txt");

final String[] detNames = ["DC", "ECAL"]
final Integer[] detNhits = [36 , 9];


// default #1 is electron in FD:
ParticleGenerator pgen1 = new ParticleGenerator(11,
        1.0,  9.0,  // momentum min/max
       15.0, 35.0,  // theta (deg) min/max
      -10.0, 10.0); // phi (deg) min/max

// default #2 is "pid" in FD:
ParticleGenerator pgen2 = new ParticleGenerator(pid<0?-pid:pid,
       1.0,   4.5,
      20.0,  35.0,
     110.0, 130.0);

// override #2 to be in CD:
if (isCentral) {
    pgen2.setRange(
       0.3,   1.1,
      50.0, 100.0,
     110.0, 130.0);
}

// special case for Forward Tagger (e-gamma or gamma-e)
if (isForwardTagger) {
    if (pid==22) {
        // gamma in FT
        pgen2.setRange(
        3.0,  8.0,
        2.5,  4.5,
      -10.0, 10.0);   
    }
    else if (pid==-22 || pid==-211 || pid==-2212 || pid==-321) {
        // electron in FT
        pgen1.setRange(
        3.0,  8.0,
        2.5,  4.5,
      -10.0, 10.0);
    }
    else throw new RuntimeException("Unknown Combo:  FowardTagger and pid!=11 and pid!=22");
}

// setup fiducial cuts:
Clas12FastMC fastMC = new Clas12FastMC(torusScale,solenoidScale);
Detector detDC = GeometryFactory.getDetector(DetectorType.DC);
Detector detEC = GeometryFactory.getDetector(DetectorType.ECAL);
fastMC.addDetector("DC",  detDC);
fastMC.addDetector("ECAL",detEC);
fastMC.addFilter(-1,detNames,detNhits);
fastMC.addFilter( 1,detNames,detNhits);
fastMC.addFilter( 0,detNames,detNhits);
fastMC.show();

int kk=0;
while (kk < nEvents) {

    // generate kinematics:
    Particle p1 = pgen1.getParticle();
    Particle p2 = pgen2.getParticle();

    // apply fiducial cuts:
    if (isForwardTagger) {
        if (pid==22) {
            // electron is in FD:
            if (!fastMC.checkParticle(p1)) continue;
        }
        else {
            // photon is in FD:
            if (!fastMC.checkParticle(p2)) continue;
        }
    }
    else if (isCentral) {
        // electron is in FD:
        if (!fastMC.checkParticle(p1)) continue;
    }
    else {
        // both are in FD:
        if (!fastMC.checkParticle(p1)) continue;
        if (!fastMC.checkParticle(p2)) continue;
    }
   
    // write event:
    PhysicsEvent ev = new PhysicsEvent();
    ev.addParticle(p1);
    ev.addParticle(p2);
    file << ev.toLundString();
    kk++;
}

