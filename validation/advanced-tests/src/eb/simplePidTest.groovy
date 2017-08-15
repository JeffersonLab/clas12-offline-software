import java.util.ArrayList;
import org.jlab.groot.data.H1F;
import org.jlab.groot.data.H2F;
import org.jlab.groot.math.F1D;
import org.jlab.groot.ui.TCanvas;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.hipo.HipoDataSource;
import org.jlab.clas.physics.LorentzVector;
import org.jlab.groot.ui.TCanvas;
import org.jlab.groot.data.GraphErrors;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.clas.detector.DetectorParticle;


HipoDataSource reader = new HipoDataSource();
reader.open(args[0]);


int dc_electron_total = 0;
int eb_electron_total = 0;

int dc_hadron_total = 0;
int eb_hadron_total = 0;


thelist = fillInvariantMassData(reader);
if(thelist.size()==4) {
dc_electron_total = dc_electron_total + thelist.get(0);
eb_electron_total = eb_electron_total + thelist.get(1);
dc_hadron_total = dc_hadron_total + thelist.get(2);
eb_hadron_total = eb_hadron_total + thelist.get(3);
}



System.out.println("Generated electrons " + dc_electron_total);
System.out.println("Reconstructed electrons " + eb_electron_total);			     
System.out.println("Generated hadrons " + dc_hadron_total);
System.out.println("Reconstructed hadrons " + eb_hadron_total);


List<Integer> fillInvariantMassData(HipoDataSource read) {

List<Integer> list = new ArrayList<>();
int generated_electron = 0;
int reconstructed_electron = 0;
int generated_hadron = 0;
int reconstructed_hadron = 0;


while(read.hasEvent())
{
	
        DataEvent event = read.getNextEvent();

	ArrayList<LorentzVector> eb_electrons = new ArrayList<>();
        ArrayList<LorentzVector> eb_hadrons = new ArrayList<>();

        ArrayList<LorentzVector> electrons = new ArrayList<>();
        ArrayList<LorentzVector> hadrons = new ArrayList<>();



        if(event.hasBank("REC::Particle"))
        {
            DataBank bank = event.getBank("REC::Particle");
            for(int k = 0; k < bank.rows(); k++)
                {
			int charge = bank.getInt("charge", k);
                        int pid = bank.getInt("pid", k);
                        float px = bank.getFloat("px", k);
                        float py = bank.getFloat("py", k);
                        float pz = bank.getFloat("pz", k);
			float beta = bank.getFloat("beta", k );
	                Vector3D vec = new Vector3D(px,py,pz);
			double theta = vec.theta()*57.2958;
			double phi = vec.phi()*57.2958;
                        double energy;

	    if(pid==11) {
	    energy = Math.sqrt(px*px + py*py + pz*pz + 0.0005*0.0005);
	    eb_electrons.add(new LorentzVector(px, py, pz, energy));
		 }


	    if(pid==2212) {
	    energy = Math.sqrt(px*px + py*py + pz*pz + 0.938*0.938);
	    eb_hadrons.add(new LorentzVector(px, py, pz, energy));
	         }

	    if(charge<0) {
	    energy = Math.sqrt(px*px + py*py + pz*pz + 0.0005*0.0005);
	    electrons.add(new LorentzVector(px, py, pz, energy));
		 }


	    if(charge>0) {
	    energy = Math.sqrt(px*px + py*py + pz*pz + 0.938*0.938);
	    hadrons.add(new LorentzVector(px, py, pz, energy));
	         }

 
           
                }//end of for loop 


	 
      }//end of if (REC::Particle) loop




//++++++++++++++++++++++Efficiency Analysis++++++++++++++++++++++++++++++++++++++++++



if(electrons.size()>0){
    generated_electron = generated_electron + 1;
   }


if(eb_electrons.size()>0) {
    reconstructed_electron = reconstructed_electron + 1;
}

if(hadrons.size()>0){
    generated_hadron = generated_hadron + 1;
   }


if(eb_hadrons.size()>0) {
    reconstructed_hadron = reconstructed_hadron + 1;
}


}



//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++


if(generated_electron>0 && reconstructed_electron>0) {
list.add(generated_electron);
list.add(reconstructed_electron);
list.add(generated_hadron);
list.add(reconstructed_hadron);
}
return list;
}

