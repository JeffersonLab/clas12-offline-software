package org.jlab.rec.cvt.alignment;

import java.util.ArrayList;
import java.util.List;

import org.jlab.detector.base.DetectorType;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.cvt.banks.RecoBankReader;
import org.jlab.rec.cvt.bmt.BMTGeometry;
import org.jlab.rec.cvt.cluster.Cluster;
import org.jlab.rec.cvt.cross.Cross;
import org.jlab.rec.cvt.hit.Hit;
import org.jlab.rec.cvt.hit.Strip;
import org.jlab.rec.cvt.track.StraightTrack;
import org.jlab.rec.cvt.track.Track;
import org.jlab.rec.cvt.trajectory.Helix;

/**
 *
 * @author spaul
 *
 */
public class AlignmentBankReader {

    private List<Cross>      _SVTcrosses;
    private List<Cluster>   _SVTclusters;
    private List<Cross>      _BMTcrosses;
    private List<Cluster>   _BMTclusters;

    public List<StraightTrack> getCosmics(DataEvent event) {

        
        _SVTclusters = RecoBankReader.readBSTClusterBank(event, "BSTRec::Clusters");
        _BMTclusters = RecoBankReader.readBMTClusterBank(event, "BMTRec::Clusters");
        
        
        _SVTcrosses = RecoBankReader.readBSTCrossBank(event, "BSTRec::Crosses");
        _BMTcrosses = RecoBankReader.readBMTCrossBank(event, "BMTRec::Crosses");
        if(_SVTcrosses!=null) {
            for(Cross cross : _SVTcrosses) {
                cross.setCluster1(_SVTclusters.get(cross.getCluster1().getId()-1));
                cross.setCluster2(_SVTclusters.get(cross.getCluster2().getId()-1)); 
            }
        }
        if(_BMTcrosses!=null) {
            for(Cross cross : _BMTcrosses) {
                cross.setCluster1(_BMTclusters.get(cross.getCluster1().getId()-1));
            }
        }
                       
        List<StraightTrack> tracks = RecoBankReader.readCVTCosmicsBank(event, "CVTRec::Cosmics");
        if(tracks == null) 
            return null;
        
        for(StraightTrack track : tracks) {
            
            List<Cross> crosses = new ArrayList<>();
            for(Cross c : track) {
                if(_SVTcrosses!=null && c.getDetector()==DetectorType.BST) {
                    for(Cross cross : _SVTcrosses) {
                        if(c.getId() == cross.getId())
                            crosses.add(cross);
                    }
                }
                if(_BMTcrosses!=null && c.getDetector()==DetectorType.BMT) {
                    for(Cross cross : _BMTcrosses) {
                        if(c.getId() == cross.getId())
                            crosses.add(cross);
                    }
                }
            }
            track.clear();
            track.addAll(crosses);
        }
        
        return tracks;
    }

    public List<Track> getTracks(DataEvent event) {

        
        _SVTclusters = RecoBankReader.readBSTClusterBank(event, "BSTRec::Clusters");
        _BMTclusters = RecoBankReader.readBMTClusterBank(event, "BMT::Clusters");
        
        
        _SVTcrosses = RecoBankReader.readBSTCrossBank(event, "BSTRec::Crosses");
        _BMTcrosses = RecoBankReader.readBMTCrossBank(event, "BMTRec::Crosses");
        if(_SVTcrosses!=null) {
            for(Cross cross : _SVTcrosses) {
                cross.setCluster1(_SVTclusters.get(cross.getCluster1().getId()-1));
                cross.setCluster2(_SVTclusters.get(cross.getCluster2().getId()-1)); 
            }
        }
        if(_BMTcrosses!=null) {
            for(Cross cross : _BMTcrosses) {
                cross.setCluster1(_BMTclusters.get(cross.getCluster1().getId()-1));
            }
        }
                       
        List<Track> tracks = RecoBankReader.readCVTTracksBank(event, "CVTRec::Tracks");
        if(tracks == null) 
            return null;
        
        for(Track track : tracks) {
            
            List<Cross> crosses = new ArrayList<>();
            for(Cross c : track) {
                if(_SVTcrosses!=null && c.getDetector()==DetectorType.BST) {
                    for(Cross cross : _SVTcrosses) {
                        if(c.getId() == cross.getId())
                            crosses.add(cross);
                    }
                }
                if(_BMTcrosses!=null && c.getDetector()==DetectorType.BMT) {
                    for(Cross cross : _BMTcrosses) {
                        if(c.getId() == cross.getId())
                            crosses.add(cross);
                    }
                }
            }
            track.clear();
            track.addAll(crosses);
           
        }
       
        return tracks;
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
