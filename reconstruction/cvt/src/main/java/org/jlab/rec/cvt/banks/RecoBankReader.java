package org.jlab.rec.cvt.banks;

import java.util.ArrayList;
import java.util.List;

import org.jlab.clas.swimtools.Swim;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.cvt.bmt.BMTGeometry;
import org.jlab.rec.cvt.bmt.BMTType;
import org.jlab.rec.cvt.cluster.Cluster;
import org.jlab.rec.cvt.cross.Cross;
import org.jlab.rec.cvt.hit.FittedHit;
import org.jlab.rec.cvt.hit.Hit;
import org.jlab.rec.cvt.hit.Strip;
import org.jlab.rec.cvt.track.StraightTrack;
import org.jlab.rec.cvt.track.Track;
import org.jlab.rec.cvt.trajectory.Helix;
import org.jlab.rec.cvt.trajectory.Ray;

public class RecoBankReader {
	private List<StraightTrack> _cosmics;
	private List<Cross> _SVTcrosses;
	private List<Cluster> _SVTclusters;
	private List<FittedHit> _SVTHits;
	private List<Cross> _BMTcrosses;
	private List<Cluster> _BMTclusters;
	private List<FittedHit> _BMThits;



	



	public void fetch_SVTCrosses(DataEvent event, double zShift) {
		if(_SVTclusters == null)
			fetch_SVTClusters(event);

		if (event.hasBank("BSTRec::Crosses") == false) {
			//System.err.println("there is no BST bank ");
			_SVTcrosses = new ArrayList<Cross>();

			return;
		}
		_SVTcrosses = new ArrayList<Cross>();
		DataBank bank = event.getBank("BSTRec::Crosses");

		for (int j = 0; j < bank.rows(); j++) {
			int region = bank.getByte("region", j);
			int sector = bank.getByte("sector", j);
			int id = bank.getShort("ID",j);
			Cross cross = new Cross("SVT", BMTType.UNDEFINED, sector, region, j);
			cross.set_Id(id);
			cross.set_Point(new Point3D(10.*bank.getFloat("x", j), 10.*bank.getFloat("y", j),10.*(bank.getFloat("z", j)-zShift)));
			cross.set_PointErr(new Point3D(10.*bank.getFloat("err_x", j), 10.*bank.getFloat("err_y", j),10.*bank.getFloat("err_z", j)));
			cross.set_AssociatedTrackID(bank.getShort("trkID",j));
			cross.set_Dir(new Vector3D(bank.getFloat("ux", j),bank.getFloat("uy", j),bank.getFloat("uz", j)));

			int cluster1id = bank.getShort("Cluster1_ID", j);
			for (Cluster cluster: _SVTclusters)
				if (cluster.get_Id() == cluster1id)
					cross.set_Cluster1(cluster);
			int cluster2id = bank.getShort("Cluster2_ID", j);
			for (Cluster cluster: _SVTclusters)
				if (cluster.get_Id() == cluster2id)
					cross.set_Cluster2(cluster);

			_SVTcrosses.add(cross);
		}



	}
	
	public void fetch_BMTCrosses(DataEvent event, double zShift) {
		if(_BMTclusters == null)
			fetch_BMTClusters(event);

		if (event.hasBank("BMTRec::Crosses") == false) {
			//System.err.println("there is no BST bank ");
			_BMTcrosses = new ArrayList<Cross>();

			return;
		}
		_BMTcrosses = new ArrayList<Cross>();
		DataBank bank = event.getBank("BMTRec::Crosses");

		for (int j = 0; j < bank.rows(); j++) {
			int region = bank.getByte("region", j);
			int sector = bank.getByte("sector", j);
			int id = bank.getShort("ID",j);
			Cross cross = new Cross("BMT", BMTType.UNDEFINED, sector, region, id);
			cross.set_Point(new Point3D(10.*bank.getFloat("x", j), 10.*bank.getFloat("y", j),10.*(bank.getFloat("z", j)-zShift)));
			cross.set_PointErr(new Point3D(10.*bank.getFloat("err_x", j), 10.*bank.getFloat("err_y", j),10.*bank.getFloat("err_z", j)));
			cross.set_AssociatedTrackID(bank.getShort("trkID",j));
			cross.set_Dir(new Vector3D(bank.getFloat("ux", j),bank.getFloat("uy", j),bank.getFloat("uz", j)));

			int cluster1id = bank.getShort("Cluster1_ID", j);
			for (Cluster cluster: _BMTclusters) {
				if (cluster.get_Id() == cluster1id) {
					cross.set_Cluster1(cluster);
					cross.set_DetectorType(BMTGeometry.getDetectorType(cluster.get_Layer()));
				}
			}
			//int cluster2id = bank.getShort("Cluster2_ID", j);
			//for (Cluster cluster: _clusters)
			//	if (cluster.get_Id() == cluster2id)
			//		cross.set_Cluster2(cluster);

			_BMTcrosses.add(cross);
		}

	}

	public void fetch_BMTClusters(DataEvent event) {
		_BMTclusters = new ArrayList<Cluster>();
		if(_BMThits == null)
			this.fetch_BMTHits(event);
		DataBank bank = event.getBank("BMTRec::Clusters");

		for (int i = 0; i < bank.rows(); i++) {


			int id = bank.getShort("ID", i);
			int layer = bank.getByte("layer", i);
			int sector = bank.getByte("sector", i);
			Cluster cluster = new Cluster(0, 0, sector, layer, id);

			int size = bank.getInt("size", i);
			cluster.set_TotalEnergy(bank.getFloat("ETot", i));
			cluster.set_SeedStrip(bank.getInt("seedStrip", i));
			cluster.set_Centroid(bank.getFloat("centroid",i));
			cluster.set_SeedEnergy(bank.getFloat("seedE",i));
			cluster.set_SeedEnergy(bank.getFloat("seedE",i));
			cluster.set_CentroidResidual(bank.getFloat("centroidResidual",i));
			cluster.set_SeedResidual(bank.getFloat("seedResidual",i));
			cluster.set_AssociatedTrackID(bank.getShort("trkID",i));
			//Since only up to 5 hits per track are written...
			for (int j = 0; j < 5; j++) {
				String hitStrg = "Hit";
				hitStrg += (j + 1);
				hitStrg += "_ID";
				if(!hasColumn(bank,hitStrg))
					continue;
				int hitId = bank.getShort(hitStrg, i);
				for(FittedHit hit : _BMThits) {
					if (hit.get_Id() == hitId) {
						cluster.add(hit);
					}
				}
			}
			_BMTclusters.add(cluster);

		}
	}

	public void fetch_Cosmics(DataEvent event, double zShift) {

		if(_SVTcrosses == null)
			fetch_SVTCrosses(event, zShift);
		if(_BMTcrosses == null)
			fetch_BMTCrosses(event, zShift);
		if (event.hasBank("CVTRec::Cosmics") == false) {
			//System.err.println("there is no BST bank ");
			_cosmics = new ArrayList<StraightTrack>();

			return;
		}

		List<Hit> hits = new ArrayList<Hit>();

		DataBank bank = event.getBank("CVTRec::Cosmics");

		int rows = bank.rows();;

		short ids[] = bank.getShort("ID");
		float chi2s[] = bank.getFloat("chi2");
		short ndfs[] = bank.getShort("ndf");
		float yx_slopes[] = bank.getFloat("trkline_yx_slope");
		float yx_intercs[] = bank.getFloat("trkline_yx_interc");
		float yz_slopes[] = bank.getFloat("trkline_yz_slope");
		float yz_intercs[] = bank.getFloat("trkline_yz_interc");


		_cosmics = new ArrayList<StraightTrack>();


		for(int i = 0; i<rows; i++) {
			// get the cosmics ray unit direction vector
			Vector3D u = new Vector3D(yx_slopes[i], 1, yz_slopes[i]).asUnit();
			Point3D point = new Point3D(10.*yx_intercs[i], 0, 10.*(yz_intercs[i]-zShift));
			Ray ray = new Ray(point, u);
			StraightTrack track = new StraightTrack(ray);
			track.set_Id(ids[i]);
			track.set_chi2(chi2s[i]);
			track.set_ndf(ndfs[i]);


			loopCrossId: for (int j = 0; j < 18; j++) { 

				String hitStrg = "Cross";
				hitStrg += (j + 1);
				hitStrg += "_ID";
				if(!hasColumn(bank,hitStrg))
					continue;
				int crossid = bank.getShort(hitStrg, i);
				for(Cross cross : _SVTcrosses) {
					if(cross.get_Id() == crossid) {
						track.add(cross);
						continue loopCrossId;
					}
					
				}
				for(Cross cross : _BMTcrosses) {
					if(cross.get_Id() == crossid) {
						track.add(cross);
						continue loopCrossId;
					}
				}
			}
			_cosmics.add(track);

		}
	}
	
	ArrayList<Track> _tracks;
	
	public void fetch_Tracks(DataEvent event, org.jlab.rec.cvt.svt.Geometry geo, double zShift) {

		if(_SVTcrosses == null)
			fetch_SVTCrosses(event, zShift);
		if(_BMTcrosses == null)
			fetch_BMTCrosses(event, zShift);
		if (event.hasBank("CVTRec::Tracks") == false) {
			//System.err.println("there is no BST bank ");
			_tracks = new ArrayList<Track>();

			return;
		}

		List<Hit> hits = new ArrayList<Hit>();

		DataBank bank = event.getBank("CVTRec::Tracks");

		int rows = bank.rows();;

		short ids[] = bank.getShort("ID");
		float chi2s[] = bank.getFloat("chi2");
		short ndfs[] = bank.getShort("ndf");
		byte qs[] = bank.getByte("q");
		float ps[] = bank.getFloat("p");
		float pts[] = bank.getFloat("pt");
		float tandips[] = bank.getFloat("tandip");
		float phi0s[] = bank.getFloat("phi0");
		float z0s[] = bank.getFloat("z0");
		float d0s[] = bank.getFloat("d0");
		float xbs[] = bank.getFloat("xb");
		float ybs[] = bank.getFloat("yb");
		
		float curvatures[];
		try {
			curvatures = bank.getFloat("curvature");
		}catch(Exception e){
			curvatures =  new float[ids.length];
		}
		
		 /*bank.setFloat("phi0", i, (float) helix.get_phi_at_dca());
         bank.setFloat("tandip", i, (float) helix.get_tandip());
         bank.setFloat("z0", i, (float) (helix.get_Z0()/10.+zShift));
         bank.setFloat("d0", i, (float) (helix.get_dca()/10.));
         bank.setFloat("xb", i, (float) (org.jlab.rec.cvt.Constants.getXb()/10.0));
         bank.setFloat("yb", i, (float) (org.jlab.rec.cvt.Constants.getYb()/10.0));*/

		_tracks = new ArrayList<Track>();
		
		for(int i = 0; i<rows; i++) {
			// get the cosmics ray unit direction vector
			Track track = new Track(new Helix(d0s[i]*10.f, phi0s[i], curvatures[i]/10.f, (z0s[i]-zShift)*10.f, tandips[i], null));
			track.set_Id(ids[i]);
			track.setChi2(chi2s[i]);
			track.setNDF(ndfs[i]);
			//there are other entries that should be read.  add these later.  


			loopCrossId: for (int j = 0; j < 18; j++) { 

				String hitStrg = "Cross";
				hitStrg += (j + 1);
				hitStrg += "_ID";
				if(!hasColumn(bank,hitStrg))
					continue;
				int crossid = bank.getShort(hitStrg, i);
				for(Cross cross : _SVTcrosses) {
					if(cross.get_Id() == crossid)
						track.add(cross);
				}
				for(Cross cross : _BMTcrosses) {
					if(cross.get_Id() == crossid) {
						track.add(cross);
						continue loopCrossId;
					}
				}
			}
			_tracks.add(track);

		}
	}
	
	
	

	public boolean hasColumn(DataBank bank, String name) {
		for(String n : bank.getColumnList()) {
			if (name.equalsIgnoreCase(n))
				return true;
		}
		return false;
	}

	public void fetch_SVTClusters(DataEvent event) {
		_SVTclusters = new ArrayList<Cluster>();
		if(_SVTHits == null)
			this.fetch_SVTHits(event);
		DataBank bank = event.getBank("BSTRec::Clusters");

		for (int i = 0; i < bank.rows(); i++) {


			int id = bank.getShort("ID", i);
			int layer = bank.getByte("layer", i);
			int sector = bank.getByte("sector", i);
			Cluster cluster = new Cluster(0, 0, sector, layer, id);

			int size = bank.getInt("size", i);
			cluster.set_TotalEnergy(bank.getFloat("ETot", i));
			cluster.set_SeedStrip(bank.getInt("seedStrip", i));
			cluster.set_Centroid(bank.getFloat("centroid",i));
			cluster.set_SeedEnergy(bank.getFloat("seedE",i));
			cluster.set_SeedEnergy(bank.getFloat("seedE",i));
			cluster.set_CentroidResidual(bank.getFloat("centroidResidual",i));
			cluster.set_SeedResidual(bank.getFloat("seedResidual",i));
			cluster.set_AssociatedTrackID(bank.getShort("trkID",i));

			//Since only up to 5 hits per track are written...
			for (int j = 0; j < 5; j++) {
				String hitStrg = "Hit";
				hitStrg += (j + 1);
				hitStrg += "_ID";
				if(!hasColumn(bank,hitStrg))
					continue;
				int hitId = bank.getShort(hitStrg, i);
				for(FittedHit hit : _SVTHits) {
					if (hit.get_Id() == hitId) {
						cluster.add(hit);
					}
				}
			}
			_SVTclusters.add(cluster);

		}

	}


	private void fetch_SVTHits(DataEvent event) {
		DataBank bank = event.getBank("BSTRec::Hits");

		_SVTHits = new ArrayList<FittedHit>();
		for (int i = 0; i < bank.rows(); i++) {
			int layer = bank.getByte("layer", i);
			int sector = bank.getByte("sector", i);
			int strip = bank.getInt("strip", i);
			int id = bank.getShort("ID", i);
			FittedHit hit = new FittedHit(0, 0, sector, layer, new Strip(strip, 0,0));

			hit.set_Id(id);
			hit.set_docaToTrk(bank.getFloat("fitResidual", i));
			hit.set_TrkgStatus(bank.getInt("trkingStat", i));

			hit.set_AssociatedClusterID(bank.getShort("clusterID", i));
			hit.set_AssociatedTrackID(bank.getShort("trkID", i));
			_SVTHits.add(hit);
		}
		//bank.show();
	}

	public void fetch_BMTHits(DataEvent event) {
		DataBank bank = event.getBank("BSTRec::Hits");

		_BMThits = new ArrayList<FittedHit>();
		for (int i = 0; i < bank.rows(); i++) {
			int layer = bank.getByte("layer", i);
			int sector = bank.getByte("sector", i);
			int strip = bank.getInt("strip", i);
			int id = bank.getShort("ID", i);
			FittedHit hit = new FittedHit(0, 0, sector, layer, new Strip(strip, 0,0));

			hit.set_Id(id);
			hit.set_docaToTrk(bank.getFloat("fitResidual", i));
			hit.set_TrkgStatus(bank.getInt("trkingStat", i));

			hit.set_AssociatedClusterID(bank.getShort("clusterID", i));
			hit.set_AssociatedTrackID(bank.getShort("trkID", i));
			_BMThits.add(hit);
		}
		//bank.show();
	}

	public List<StraightTrack> get_Cosmics() {
		return _cosmics;
	}

	public List<Track> get_Tracks() {
		return _tracks;
	}

	public List<Cluster> get_ClustersSVT() {
		return _SVTclusters;
	}
	
	public List<Cluster> get_ClustersBMT() {
		return _BMTclusters;
	}

	public List<Cross> get_CrossesSVT() {
		return _SVTcrosses;
	}
	
	public List<Cross> get_CrossesBMT() {
		return _BMTcrosses;
	}

}	