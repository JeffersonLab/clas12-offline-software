package org.jlab.rec.tof.banks;

import java.util.ArrayList;
import java.util.List;
import org.jlab.utils.groups.IndexedTable;

public interface IMatchedHit {

    public String DetectorName();

    public List<BaseHit> MatchHits(ArrayList<BaseHit> ADCandTDCLists, double timeJitter, IndexedTable tdcConv, IndexedTable ADCandTDCOffsets);

}
