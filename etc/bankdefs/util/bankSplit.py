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
def createdirandlinks(dirname, banklist):
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

# create dst, calibration and monitoring directories
dst = ["BAND::laser","RUN::config","RAW::epics","RAW::scaler","REC::Event","REC::Particle","REC::Calorimeter","REC::Cherenkov","REC::CovMat","REC::ForwardTagger","REC::Scintillator","REC::ScintExtras","REC::Track","REC::Traj","RECFT::Event","RECFT::Particle","RICH::tdc","RICH::ringCher","RICH::hadCher","RTPC::tracks","RUN::scaler","HEL::flip","HEL::online","MC::Event","MC::Header","MC::Lund","MC::Particle","MC::True"]

calibration = ["BAND::adc","BAND::tdc","BAND::hits","BAND::rawhits","BAND::laser","CND::adc","CND::hits","CND::tdc","CTOF::adc","CTOF::hits","CTOF::tdc","CVTRec::Tracks","ECAL::adc","ECAL::calib","ECAL::clusters","ECAL::peaks","ECAL::tdc","FT::particles","FTCAL::adc","FTCAL::clusters","FTCAL::hits","FTHODO::adc","FTHODO::clusters","FTHODO::hits","FTOF::adc","FTOF::hits","FTOF::tdc","HEL::flip","HEL::online","HTCC::adc","HTCC::rec","LTCC::adc","LTCC::clusters","MC::Event","MC::Header","MC::Lund","MC::Particle","MC::True","RAW::epics","RAW::scaler","REC::Calorimeter","REC::Cherenkov","REC::CovMat","REC::Event","REC::ForwardTagger","REC::Particle","REC::Scintillator","REC::ScintExtras","REC::Track","REC::Traj","RECFT::Event","RECFT::Particle","RF::adc","RF::tdc","RICH::tdc","RICH::hits","RICH::hadCher","RICH::hadrons","RICH::photons","RICH::ringCher","RTPC::hits","RTPC::tracks","RUN::config","RUN::rf","RUN::scaler","RUN::trigger","TimeBasedTrkg::TBCrosses","TimeBasedTrkg::TBHits","TimeBasedTrkg::TBSegments","TimeBasedTrkg::TBSegmentTrajectory","TimeBasedTrkg::TBTracks","TimeBasedTrkg::Trajectory"]

monitoring = ["BAND::adc","BAND::tdc","BAND::rawhits","BAND::laser","BAND::hits","BMT::adc","BMTRec::Clusters","BMTRec::Crosses","BMTRec::Hits","BMTRec::LayerEffs","BST::adc","BSTRec::Clusters","BSTRec::Crosses","BSTRec::Hits","BSTRec::LayerEffs","CND::adc","CND::clusters","CND::hits","CND::tdc","CTOF::adc","CTOF::hits","CTOF::tdc","CVTRec::Tracks","CVTRec::Trajectory","ECAL::adc","ECAL::calib","ECAL::clusters","ECAL::hits","ECAL::peaks","ECAL::tdc","FT::particles","FTCAL::adc","FTCAL::clusters","FTCAL::hits","FTHODO::adc","FTHODO::clusters","FTHODO::hits","FTOF::adc","FTOF::hits","FTOF::tdc","HEL::adc","HEL::flip","HEL::online","HitBasedTrkg::HBTracks","HTCC::adc","HTCC::rec","LTCC::adc","LTCC::clusters","MC::Event","MC::Header","MC::Lund","MC::Particle","MC::True","RAW::epics","RAW::scaler","RAW::vtp","REC::Calorimeter","REC::Cherenkov","REC::CovMat","REC::Event","REC::ForwardTagger","REC::Particle","REC::Scintillator","REC::ScintExtras","REC::Track","REC::Traj","RECFT::Event","RECFT::Particle","RECHB::Calorimeter","RECHB::Cherenkov","RECHB::Event","RECHB::ForwardTagger","RECHB::Particle","RECHB::Scintillator","RECHB::Track","RF::adc","RF::tdc","RICH::tdc","RICH::hits","RICH::hadCher","RICH::hadrons","RICH::photons","RICH::ringCher","RTPC::hits","RTPC::tracks","RUN::config","RUN::rf","RUN::scaler","RUN::trigger","TimeBasedTrkg::TBCrosses","TimeBasedTrkg::TBHits","TimeBasedTrkg::TBSegments","TimeBasedTrkg::TBSegmentTrajectory","TimeBasedTrkg::TBTracks","TimeBasedTrkg::Trajectory"]

ebrerun=list(dst)
ebrerun.extend(["FTOF::hits","TimeBasedTrkg::TBTracks","TimeBasedTrkg::Trajectory","TimeBasedTrkg::TBCovMat","HitBasedTrkg::HBTracks","FTOF::hbhits","ECAL::clusters","CTOF::hits","CND::clusters","HTCC::rec","LTCC::clusters","ECAL::moments","CVTRec::Tracks","CVTRec::Trajectory","FT::particles","FTCAL::clusters","FTHODO::clusters","RUN:rf"])

dsthb=list(dst)
dsthb.extend(["RECHB::Event","RECHB::Particle","RECHB::Calorimeter","RECHB::Cherenkov","RECHB::CovMat","RECHB::ForwardTagger","RECHB::Scintillator","RECHB::ScintExtras","RECHB::Track"])

createdirandlinks("dst/", dst)
createdirandlinks("dsthb/", dsthb)
createdirandlinks("calibration/", calibration)
createdirandlinks("monitoring/",  monitoring)
#createdirandlinks("ebrerun/",  ebrerun)


