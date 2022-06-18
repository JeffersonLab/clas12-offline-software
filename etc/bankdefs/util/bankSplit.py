#!/usr/bin/env python

import json
import sys
import os

# print usage
if len(sys.argv)<2:
   print("usage: bankSplit.py coatjavahipobankfolder (e.g. coatjava/etc/bankdefs/hipo4/)")
   sys.exit()

# hipo schema directory
hipodirectory = sys.argv[1] +  "/"
os.chdir(hipodirectory)

# single jsons directory
workdirectory   = "singles/"
singledirectory = "full/"
os.mkdir(workdirectory)
os.mkdir(workdirectory + singledirectory)

# create schema directory
def create(dirname, banklist):
    os.mkdir(workdirectory + dirname)
    os.chdir(workdirectory + dirname)
    for bank in banklist:
        os.symlink("../" + singledirectory + bank + ".json", bank + ".json")
    os.chdir("../../")
    print("Json file links created in " + workdirectory + dirname)

# for each json file in hipo schema folder
for filename in os.listdir("./"):
    if filename.endswith(".json"):

        #Read JSON data into the datastore variable
        f = open(filename)
        try:
            datastore = json.load(f)
        except:
            print('Invalid JSON:  '+filename)
            sys.exit(1)
        # loop over banks in the json file
        for bank in datastore:
            bankname = bank['name']
            file = open(workdirectory + singledirectory + bankname + ".json", 'w')
            file.write(json.dumps([bank], sort_keys=True, indent=4))
            file.close
print("Single json files saved in " + workdirectory + singledirectory)

# these should *always* be kept:
mc = ["MC::Event", "MC::Header", "MC::Lund", "MC::Particle", "MC::True"]
tag1 = ["RUN::config", "RAW::epics", "RAW::scaler", "RUN::scaler", "COAT::config", "HEL::flip", "HEL::online"]

# these are the output of the event builder:
rectb   = ["REC::Event","REC::Particle","REC::Calorimeter","REC::Cherenkov","REC::CovMat","REC::ForwardTagger","REC::Scintillator","REC::ScintExtras","REC::Track","REC::Traj","RECFT::Event","RECFT::Particle"]
rechb   = ["RECHB::Event","RECHB::Particle","RECHB::Calorimeter","RECHB::Cherenkov","RECHB::ForwardTagger","RECHB::Scintillator","RECHB::ScintExtras","RECHB::Track"]
rectbai = ["RECAI::Event","RECAI::Particle","RECAI::Calorimeter","RECAI::Cherenkov","RECAI::CovMat","RECAI::ForwardTagger","RECAI::Scintillator","RECAI::ScintExtras","RECAI::Track","RECAI::Traj","RECAIFT::Event","RECAIFT::Particle"]
rechbai = ["RECHBAI::Event","RECHBAI::Particle","RECHBAI::Calorimeter","RECHBAI::Cherenkov","RECHBAI::ForwardTagger","RECHBAI::Scintillator","RECHBAI::ScintExtras","RECHBAI::Track"]

# special, detector-specific raw banks that are kept in DSTs (for now):
band = ["BAND::laser"]
rich = ["RICH::tdc","RICH::ringCher","RICH::hadCher"]
rtpc = ["RTPC::hits","RTPC::tracks"]
dets = band + rich + rtpc

# additions for the calibration schema: 
calib = ["BAND::adc","BAND::laser","BAND::tdc","BAND::hits","BAND::rawhits","CND::adc","CND::hits","CND::tdc","CTOF::adc","CTOF::hits","CTOF::tdc","CVTRec::Tracks","ECAL::adc","ECAL::calib","ECAL::clusters","ECAL::peaks","ECAL::tdc","FMT::Hits","FMT::Clusters","FMT::Tracks","FMT::Trajectory","FT::particles","FTCAL::adc","FTCAL::clusters","FTCAL::hits","FTHODO::adc","FTHODO::clusters","FTHODO::hits","FTTRK::adc","FTTRK::clusters","FTTRK::hits","FTTRK::crosses","FTOF::adc","FTOF::hits","FTOF::tdc","HTCC::adc","HTCC::rec","LTCC::adc","LTCC::clusters","RF::adc","RF::tdc","RICH::tdc","RICH::hits","RICH::hadCher","RICH::hadrons","RICH::photons","RICH::ringCher","RTPC::adc","RTPC::hits","RTPC::tracks","RUN::rf","RUN::trigger","TimeBasedTrkg::TBCrosses","TimeBasedTrkg::TBHits","TimeBasedTrkg::TBSegments","TimeBasedTrkg::TBSegmentTrajectory","TimeBasedTrkg::TBTracks","TimeBasedTrkg::Trajectory"]

# additions for the monitoring schema:
mon = ["BMT::adc","BMTRec::Clusters","BMTRec::Crosses","BMTRec::Hits","BMTRec::LayerEffs","BST::adc","BSTRec::Clusters","BSTRec::Crosses","BSTRec::Hits","BSTRec::LayerEffs","CND::clusters","CVTRec::Trajectory","ECAL::hits","FMT::adc","HEL::adc","HitBasedTrkg::HBTracks","RAW::vtp"]

#ebrerun = list(dst)
#ebrerun.extend(["FTOF::hits","TimeBasedTrkg::TBTracks","TimeBasedTrkg::Trajectory","TimeBasedTrkg::TBCovMat","HitBasedTrkg::HBTracks","FTOF::hbhits","ECAL::clusters","CTOF::hits","CND::clusters","HTCC::rec","LTCC::clusters","ECAL::moments","CVTRec::Tracks","CVTRec::Trajectory","FT::particles","FTCAL::clusters","FTHODO::clusters","RUN:rf"])

dst = rectbai + rectb + mc + tag1 + dets
dsthb = dst + rechbai + rechb

calib.extend(dsthb)
mon.extend(calib)

create("dst/", set(dst))
create("dsthb/", set(dsthb))
create("calib/", set(calib))
create("mon/",  set(mon))
#create("ebrerun/", setebrerun))

