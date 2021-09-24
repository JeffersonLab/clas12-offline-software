package org.jlab.rec.dc.banks;

/**
 *
 * @author devita
 */
public class Banks {
    
    private final String tdcBank  = "DC::tdc";
    private final String docaBank = "DC::doca";
    private String inHitsBank      = null;
    private String inClustersBank  = null;
    private String inTracksBank    = null;
    private String hitsBank        = null;
    private String clustersBank    = null;
    private String segmentsBank    = null;
    private String crossesBank     = null;
    private String tracksBank      = null;
    private String idsBank         = null;
    private String trajBank        = null;
    private String covmatBank      = null;
    private final String aiBank    = "ai::tracks";
    private String recEventBank    = null;
    private String recPartBank     = null;
    private String recTrackBank    = null;

    public void init(String hits, String clusters, String segments, String crosses, String tracks, String ids, String traj, String covmat) {
        
    }

    public String getTdcBank() {
        return tdcBank;
    }

    public String getDocaBank() {
        return docaBank;
    }

    public String getAiBank() {
        return aiBank;
    }

    public String getHitsInputBank() {
        return inHitsBank;
    }

    public void setHitsInputBank(String inHitsBank) {
        this.inHitsBank = inHitsBank;
    }

    public String getClustersInputBank() {
        return inClustersBank;
    }

    public void setClustersInputBank(String inClustersBank) {
        this.inClustersBank = inClustersBank;
    }

    public String getTracksInputBank() {
        return inTracksBank;
    }

    public void setTracksInputBank(String inTracksBank) {
        this.inTracksBank = inTracksBank;
    }
    
    public String getHitsBank() {
        return hitsBank;
    }

    public void setHitsBank(String hitsBank) {
        this.hitsBank = hitsBank;
    }

    public String getClustersBank() {
        return clustersBank;
    }

    public void setClustersBank(String clustersBank) {
        this.clustersBank = clustersBank;
    }

    public String getSegmentsBank() {
        return segmentsBank;
    }

    public void setSegmentsBank(String segmentsBank) {
        this.segmentsBank = segmentsBank;
    }

    public String getCrossesBank() {
        return crossesBank;
    }

    public void setCrossesBank(String crossesBank) {
        this.crossesBank = crossesBank;
    }

    public String getTracksBank() {
        return tracksBank;
    }

    public void setTracksBank(String tracksBank) {
        this.tracksBank = tracksBank;
    }

    public String getIdsBank() {
        return idsBank;
    }

    public void setIdsBank(String idsBank) {
        this.idsBank = idsBank;
    }

    public String getTrajBank() {
        return trajBank;
    }

    public void setTrajBank(String trajBank) {
        this.trajBank = trajBank;
    }

    public String getCovmatBank() {
        return covmatBank;
    }

    public void setCovmatBank(String covmatBank) {
        this.covmatBank = covmatBank;
    }
    
    public String getRecEventBank() {
        return recEventBank;
    }

    public void setRecEventBank(String recEventBank) {
        this.recEventBank = recEventBank;
    }

    public String getRecPartBank() {
        return recPartBank;
    }

    public void setRecPartBank(String recPartBank) {
        this.recPartBank = recPartBank;
    }

    public String getRecTrackBank() {
        return recTrackBank;
    }

    public void setRecTrackBank(String recTrackBank) {
        this.recTrackBank = recTrackBank;
    }
    
    
}
