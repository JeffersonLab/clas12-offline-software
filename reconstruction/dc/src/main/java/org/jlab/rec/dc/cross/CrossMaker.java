package org.jlab.rec.dc.cross;

import java.util.ArrayList;
import java.util.List;
import org.jlab.detector.geant4.v2.DCGeant4Factory;

import org.jlab.geom.prim.Point3D;
import org.jlab.rec.dc.Constants;
import org.jlab.rec.dc.segment.Segment;

/**
 * Driver class to make DC crosses
 *
 * @author ziegler
 *
 */
public class CrossMaker {

    public CrossMaker() {

    }

    /**
     *
     * @param allSegments the list of segments in the event
     * @param DcDetector
     * @return an list of crosses obtained from the input segments
     */
    public List<Cross> find_Crosses(List<Segment> allSegments, DCGeant4Factory DcDetector) {
        List<Cross> crosses = new ArrayList<>();
        
        int rid = 0;  // rsegment id

        for (int s = 0; s < Constants.NSECT; s++) // loop over sectors
        {
            for (int r = 0; r < Constants.NREG; r++) // loop over regions
            {
                // looping over all segments
                for (Segment seg1 : allSegments) { // first segment
                    //if(seg1.isOnTrack==true)
                    //    continue;
                    if (seg1.get_Sector() == s + 1 && seg1.get_RegionSlayer() == 1 && seg1.get_Region() == r + 1) { 
                        for (Segment seg2 : allSegments) { //second segment
                            if (seg2.equals(seg1)) {
                                continue;
                            } 
                            
                            if(seg1.isOnTrack==true && seg2.isOnTrack==true && seg1.associatedCrossId==seg2.associatedCrossId)
                                continue;
                            
                            //if(seg1.associatedCrossId!=-1 && seg1.associatedCrossId==seg2.associatedCrossId) {
                            //    continue;
                            //}
                            if (seg2.get_Sector() == s + 1 && seg2.get_RegionSlayer() == 2 && seg2.get_Region() == r + 1) {   //wire proximity                              
                                if (seg1.isCloseTo(seg2) && seg2.hasConsistentSlope(seg1)) {
                                    Cross cross = new Cross(s + 1, r + 1, rid++);
                                    cross.set_Id(seg1.get_Id()*1000+seg2.get_Id());
                                    cross.add(seg1);
                                    cross.add(seg2);
                                    cross.set_Segment1(seg1);
                                    cross.set_Segment2(seg2);
                                    cross.set_CrossParams(DcDetector);

                                    Point3D CS = cross.getCoordsInSector(cross.get_Point().x(), cross.get_Point().y(), cross.get_Point().z());

                                    if (CS.x() > 0) {
                                        double jitter = 2;
                                        if (cross.isPseudoCross) { 
                                            jitter = 10;  
                                        }
                                        if (Math.abs(Math.toDegrees(Math.atan2(CS.y(), CS.x()))) < 30. + jitter) { //2 degrees jitter

                                            //cross.set_Id(crosses.size() + 1);
                                            if (cross.isPseudoCross) {
                                                cross.set_Id(-1);
                                            }
                                            cross.set_CrossDirIntersSegWires();
                                            seg1.associatedCrossId = cross.get_Id();
                                            seg2.associatedCrossId = cross.get_Id();
                                            crosses.add(cross);  //insures the cross is correctly reconstructed in the sector
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        //this.setAssociatedIDs(crosses);
        return crosses;
    }

}
