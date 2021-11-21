package org.jlab.rec.dc.banks;

/**
 *
 * @author devita
 */
public class Banks {
    
    private final String tdcBank    = "DC::tdc";
    private final String docaBank   = "DC::doca";
    
    private final String aiBank     = "ai::tracks";
    
    private final String inBankType = "HitBasedTrkg";
    private String outBankType      = "HitBasedTrkg";
    private String inPrefix         = "";
    private String outPrefix        = "";

    public Banks() {
    }

    public Banks(String bankType, String inputBankPrefix, String outputBankPrefix) {
        this.init(bankType, inputBankPrefix, outputBankPrefix);
    }
    
    public final void init(String outputBankType, String inputBankPrefix, String outputBankPrefix) {
        this.outBankType = outputBankType;
        this.inPrefix    = inputBankPrefix;
        this.outPrefix   = outputBankPrefix;
    }

    public final void init(String outputBankPrefix) {
        this.outPrefix   = outputBankPrefix;
        if(!outBankType.equals("HitBasedTrkg")) {
            if(outputBankPrefix.equals("TB")) 
                this.inPrefix = "HB";
            else 
                this.inPrefix = outputBankPrefix;
        }
    }

    public String getPrefix() {
        return outPrefix;
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
    
    private String getRecBank(String item) {
        String bank = "RECHB";
        if(!bank.endsWith(inPrefix)) bank = bank + this.inPrefix;
        bank = bank + "::" + item;
        return bank;
    }
    
    private String getInputBank(String item) {
        String bank = this.inBankType + "::" + this.inPrefix + item;
        return bank;
    }
    
    private String getOutputBank(String item) {
        String bank = this.outBankType + "::" + this.outPrefix + item;
        if(outPrefix.equals("TB") && item.equals("Trajectory"))
            bank = this.outBankType + "::" + item;
        return bank;
    }
    
    public String getInputHitsBank() {
        return this.getInputBank("Hits");
    }

    public String getInputClustersBank() {
        return this.getInputBank("Clusters");
    }

    public String getInputTracksBank() {
        return this.getInputBank("Tracks");
    }
    
    public String getInputIdsBank() {
        return this.getInputBank("HitTrkId");
    }
    
    public String getHitsBank() {
        return this.getOutputBank("Hits");
    }

    public String getClustersBank() {
        return this.getOutputBank("Clusters");
    }

    public String getSegmentsBank() {
        return this.getOutputBank("Segments");
    }

    public String getCrossesBank() {
        return this.getOutputBank("Crosses");
    }

    public String getTracksBank() {
        return this.getOutputBank("Tracks");
    }

    public String getIdsBank() {
        return this.getOutputBank("HitTrkId");
    }

    public String getTrajBank() {
        return this.getOutputBank("Trajectory");
    }

    public String getCovmatBank() {
        return this.getOutputBank("CovMat");
    }
    
    public String getRecEventBank() {
        return this.getRecBank("Event");
    }

    public String getRecPartBank() {
        return this.getRecBank("Particle");
    }

    public String getRecTrackBank() {
        return this.getRecBank("Track");
    }

    @Override
    public String toString() {
        return this.inBankType + "::" + this.inPrefix + "/" + this.outBankType + "::" + this.outPrefix;
    }
}
