''Note, this page applies to **COATJAVA 6 and later**, after the
transition to HIPO4. For previous releases, i.e. 5.9.0 and earlier with
HIPO3, see the [corresponding documentation
here](CLAS12_DSTs_HIPO3 "wikilink").

The output data structures of the [Event
Builder](CLAS12_EventBuilder "wikilink") are HIPO banks whose names are
prefixed with "REC". These banks comprise what is commonly, historically
called Data Summary Tapes or DSTs and will be available for physics
analyses.

The true, full structure of each bank is always contained in the
[event.json](https://github.com/JeffersonLab/clas12-offline-software/blob/development/etc/bankdefs/hipo4/event.json)
file in the offline software repository, and below is a more explanatory
summary of some of the more important information.

The DSTs also include the
[header.json](https://github.com/JeffersonLab/clas12-offline-software/blob/development/etc/bankdefs/hipo4/header.json)
banks for some global event information unrelated to the detector
reconstruction, and some special banks from
[data.json](https://github.com/JeffersonLab/clas12-offline-software/blob/development/etc/bankdefs/hipo4/data.json).
There are also simulation-specific banks in
[mc.json](https://github.com/JeffersonLab/clas12-offline-software/blob/development/etc/bankdefs/hipo4/mc.json)

# Bank List

-   Event
    -   <tt>RUN::config
    -   REC::Event</tt>
-   Physics
    -   <tt>REC::Particle
    -   REC::*Response*
        -   REC::Calorimeter
        -   REC::Scintillator
        -   REC::Cherenkov
        -   REC::Track
        -   REC::Forward Tagger
    -   REC::Traj
    -   REC::CovMat</tt>
-   Special
    -   `HEL::online`
    -   `HEL::flip` (tag=1)
    -   `RUN::scaler` (tag=1)
    -   `RAW::scaler` (tag=1)
    -   `RAW::epics` (tag=1)

# Inter-Bank Linking

Each "response" bank contains the variable `pindex`, whose value is the
row index of its corresponding particle in `REC::Particle`. See below
for guidelines on reversing this indexing for easier use.

This is the fundamental link between DST banks, and it serves a flexible
*many-to-one* relationship to naturally accommodate the CLAS12 design
without unnecessary data overhead while enforcing a standardized data
format for detectors of the same type. For example, CLAS12 has many
layers of scintillator, calorimeter, and cherenkov detectors, some with
different geometric acceptances and multiple layers, many of which can
be associated with the same particle. Also, "response" banks can then
later be discarded without keeping dangling links nor changing bank
formats.

''Note, array/bank indices in CLAS12 always start from 0, e.g. the 1st
(2nd) element has an index of 0 (1).

## Reverse Indexing

In the absence of a full analysis framework, while you may be accessing
the HIPO banks directly, you should **not** be performing
multiple/nested bank loops just to interpret the inter-bank links.
Rather, a more efficient method is to generate a reverse index map by
looping over each response bank exactly once. Here's a [java
example](CLAS12_DSTs_-_Bank_Links "wikilink"). The same applies for,
e.g., looking up trajectory points: instead of repeatedly looping over
the trajectory bank for every particle, loop over the bank exactly once
and generate a map.

# Detector Identifiers

Each "response" bank also contains the detector identifier variables
`detector` and `layer`, whose values determine exactly which detector
that response came from. The numbering conventions are always contained
in the
[DetectorType](https://github.com/JeffersonLab/clas12-offline-software/blob/development/common-tools/clas-detector/src/main/java/org/jlab/detector/base/DetectorType.java)
and [Detector
Layer](https://github.com/JeffersonLab/clas12-offline-software/blob/development/common-tools/clas-detector/src/main/java/org/jlab/detector/base/DetectorLayer.java)
classes in the offline software repository, and also shown here for
convenience:

*Note, the layers for tracking detectors are 1 to N, where N is 12 for
CVT and 36 for DC. For DC only layer%6==0 is stored, or the last in each
superlayer.*

# Bank Details

The true, full structure of each bank is always contained in the
[event.json](https://github.com/JeffersonLab/clas12-offline-software/blob/development/etc/bankdefs/hipo4/event.json)
and
[header.json](https://github.com/JeffersonLab/clas12-offline-software/blob/development/etc/bankdefs/hipo4/header.json)
files in the offline software repository. Here are some of the more
generally useful variables:

## Event Banks

### RUN::config

-   `run/event`
    -   the run and event numbers
-   `trigger`
    -   the 32 trigger bits
    -   1-6 are the 6 electron trigger bits, one per sector, and 0 is
        their OR
    -   to test whether bit N is set: `if (trigger & (1<<N))`
-   `torus/solenoid`
    -   the fields' scales, between -1.0 and +1.0

### REC::Event

-   `startTime` (ns)
    -   the event start time. If positive, it's based on `RFTime` and
        the particle in the first row in `REC::Particle`
-   `RFTime` (ns)
    -   the RF time for the event, used as a correction for the start
        time
-   `helicity` and `helicityRaw`
    -   the helicity state for the event, with a value of -1 or +1 (else
        invalid/undefined). The "Raw" version is not corrected for the
        half-wave plate position.
        -   *Note, if helicity reporting is in delayed mode, this is
            populated from the [ HEL::flip
            bank](CLAS12_DSTs#HEL::flip "wikilink") with a delay
            correction during post-processing.*
-   `beamCharge` (nC)
    -   beam charge integrated from the beginning of the run to the most
        recent reading of the gated Faraday Cup scaler in `RAW::scaler`,
        with slope/offset conversion to charge from CCDB. *Note, this
        value will be zero in each file until the first scaler reading
        in that file.*
-   `liveTime`
    -   average livetime between the most recent two readings of
        gated/ungated clock from `RAW::scaler`.

## Physics Banks

### REC::Particle

*Note, as of [COATJAVA
6b.3.0](https://github.com/JeffersonLab/clas12-offline-software/releases/tag/6b.3.0),
REC:Particle is sorted: first the trigger particle, then negatives, then
positives, and finally neutrals, and momentum-ordered within each
group.*

-   `vx/vy/vz` (cm) and `px/py/pz` (GeV)
    -   the particle's primary vertex position and corresponding
        3-momentum
    -   *Note, neutrals' momenta are left unassigned (zero) if no pid is
        assigned, or a neutron's beta is unphysical.*
-   `vt` (ns)
    -   this particle's start time, based on the trigger particle's
        timing and path length, RF-corrected using this particle's `vz`
-   `pid`
    -   the particle identification assigned by the Event Builder
        -   ±11 for e<sup>-</sup>/e<sup>+</sup>
        -   ±211/321/2212/45 for charged hadrons π/K/p/d
        -   2112/22 for neutron/photon
        -   0 for undefined
-   `chi2pid`
    -   a signed-*N*<sub>*σ*</sub> estimate of particle identification
        quality, based on
        -   momentum-dependent calorimetry sampling fraction and
            resolution for e<sup>+</sup>/e<sup>-</sup>
        -   vertex time difference and paddle-dependent resolution for
            charged hadrons
            -   *Note, as of
                [6b.4.0](https://github.com/JeffersonLab/clas12-offline-software/releases/tag/6b.4.0),
                hadron pid no longer requires FTOF/CTOF, in which case
                `chi2pid` is left undefined.*
        -   otherwise currently undefined with a value of 99.0
-   `status`
    -   a summary of the particle's detector "topology", **with a
        negative sign to designate the trigger particle**
    -   aside from the possible negative sign, it is defined as the sum
        of these 4 bullets:
        1.  1000×FT + 2000×FD + 4000×CD + 8000\*BAND
            -   where FT/FD/CD are 1 if that detector subsystem
                contributed to the particle, else 0
        2.  100 × Number of Scintillator responses
        3.  10 × Number of Calorimeter responses
        4.  1 × Number of Cherenkov responses
    -   note, the calorimeter digit is used for CND, since there is no
        central calorimeter and to differentiate it from CTOF.

### RECFT::Particle

This bank contains all the same particles, in exactly the same order, as
`REC::Particle`, but based on start time from the Forward Tagger.

Its variables include only the subset of `REC::Particle` that are
affected by start time (`pid, vt, chi2pid, status, beta)`.

### REC::*Response*

Here *Response* can be Calorimeter, Scintillator, Cherenkov, Track,
Traj, CovMat.

*Note, only responses associated with particles in `REC::Particle` are
included in these banks.*

-   `pindex`
    -   the row index (starting from 0) of this response's particle in
        the `REC::Particle` bank
-   `index`
    -   the row index of this response in its detector bank ([see
        below](CLAS12_DSTs#Non-DST_Detector_Banks "wikilink") for those
        banks' names)
    -   in general, this will not be useful in standard DSTs after
        dropping the lower-level detector banks
-   `detector` and `layer`
    -   the same detector identifiers [listed
        above](CLAS12_DSTs#Detector_Identifiers "wikilink")
    -   note, some detectors do not have layers, or cluster across them,
        in which case `layer=0` in this bank
-   `sector`
    -   the sector (1-6) containing the response, currently only
        applicable for DC, ECAL, HTCC, LTCC, FTOF
-   `path` (cm)
    -   path length from the primary vertex to the detector response
-   `time` (ns)
    -   time of the detector response
-   `energy/nphe`
    -   energy of the detector response
    -   MeV for scintillators, GeV for calorimeters, # photoelectrons
        for cherenkovs
-   `x/y/z` (cm)
    -   the position of the response in the global CLAS12 coordinate
        system

### REC::Traj

This bank contains the tracks' states at various points along its
trajectory. These variables are in possible addition to the ones in
REC::Response banks.

-   `x/y/z` (cm) and `cx/cy/cz`
    -   the position and direction cosines of the track's trajectory at
        a given detector

### REC::ScintExtras

This bank is row-wise synchronized with REC::Scintillator

-   `dedx`
    -   energy deposition per distance travelled in scintillator
        (MeV/cm)
-   `size`
    -   number of hits in the cluster
-   `layermult`
    -   cluster layer multiplicity

## Special Banks

These banks do not fall under the other categories. Unless specified
otherwise, these banks are duplicated from the normal tag-0 events into
new tag-1 events for quick, direct access, along with a copy of the
`RUN::config` bank from their original event. To avoid reading both
copies, the tag can be specified before reading the HIPO file.

*In all cases, `helicityRaw` means not corrected for the
half-wave-plate.*

### HEL::flip

The helicity state changes, not delay-corrected, based on offline FADC
readout of helicity signals. This bank exists once for each registered
state change (plus once at the beginning of every file), determined
during decoding with access to ordered events.

-   `helicity/helicityRaw/pattern/pair`
    -   the state of the helicity signals, with values of +1/-1 else
        0=UDF
-   `timestamp`
    -   TI-timestamp of the state change (units = 4 ns), same as in
        `RUN::config`
-   `run/event`
    -   run/event number of the state change

To extract event-based, delay-corrected helicity from `HEL::flip` banks,
[this class's main
method](https://github.com/JeffersonLab/clas12-offline-software/blob/development/common-tools/clas-detector/src/main/java/org/jlab/detector/helicity/HelicityAnalysisSimple.java)
contains a working example (and there's corresponding
[javadocs](https://clasweb.jlab.org/clas12offline/docs/javadoc/common-tools/clas-detector/org/jlab/detector/helicity/package-summary.html))
and can later be used to populate `REC::Event.helicity` during
post-processing.

### RAW::scaler

*Note, in almost all cases you probably want the corresponding
calibrated information in `HEL::scaler` or `RUN::scaler`, see below,
\*not\* this `RAW::scaler` bank.* This bank contains Faraday cup, SLM,
and clock scalers, for both a run-integrating scaler and
helicity-latched scalers, with both DAQ busy-gated and ungated. It is
generally readout on the helicity clock. Its helicity-latched counts
contain both T-stable and T-stettle intervals and includes their
helicity signal bits. Its format is not user-friendly, and it contains
only raw scaler counts before conversion to beam charge. The
documentation on this bank is [available
here](https://github.com/JeffersonLab/clas12-offline-software/raw/development/common-tools/clas-detector/doc/Scaler%20information%20in%20CLAS12%202018%20data.docx).

### RUN::scaler

The result of converting run-integrated Faraday cup scalers from
`RAW::scaler` to beam charge based on the Faraday cup calibration
constants in CCDB's `/runcontrol/fcup`.

-   `fcup/fcupgated` (nC)
    -   the Faraday cup beam charge integrated from the beginning of the
        run, with and without gating on DAQ busy
-   `livetime`
    -   the average DAQ livetime since the previous scaler reading

### HEL::scaler

*New in coatjava 6.5.11.* The result of converting helicity-latched
scalers from `RAW::scaler` to beam charge, based on CCDB's
`/runcontrol/fcup` and `/runcontrol/slm` tables. This bank is useful for
calculating beam charge per helicity state, beam charge asymmetries,
etc, and contains only the T-stable intervals.

-   `fcup/fcupgated` (nC)
-   `slm/slmgated` (nC)
-   `clock/clockgated` (clock counts)
-   `helicity/helicityRaw` the helicity state for this readout

### RAW::epics

Just a single (character) array of bytes of a JSON string, where the
resulting JSON object is key/value pairs of EPICS PV names/values, e.g.
the beam current measured by 2c21 BPM and the solenoid field would
appear as `{ "IPM2C21A" : 45.1, "B_SOL:MPS:I_ZFCT" : 1001.2 }`. Here's a
[ groovy example](CLAS12_DSTs_RAW::epics "wikilink") of reading it into
a JSON object.

### HEL::online

*This bank is readout every event and is not in tag=1 events and does
not exist before 2019*. The helicity, delay-corrected online in the
Level 3 DAQ. This is based on the pseudorandom helicity board sequence,
requiring initializing the sequence with 30 consecutive patterns (\~4
seconds of data at the nominal 30 Hz helicity clock). Missed transitions
(e.g. due to DAQ busy) require reinitialization and result in UDF values
(for \~4 seconds @ 30 Hz).

-   `helicity` and `helicityRaw`
    -   with values of +1/-1 else 0=UDF

### HEL::decoder

*This bank is readout every event and is not in tag=1 events and does
not exist before Summer 2022*. This is the new helicity decoder board,
which records the current reported helicity and the previous 32 states
to allow initializing the pseudorandom sequence and doing a delay
correction without information from any other events. It is still under
testing.

## Simulation Banks

These are contained in the
[mc.json](https://github.com/JeffersonLab/clas12-offline-software/blob/development/etc/bankdefs/hipo4/mc.json)
bank definitions.

### MC::Header

### MC::Event

### MC::Lund

This bank contains all the information from the original LUND file,
which can include non-simulated mother particles.

### MC::Particle

These are the simulated particles at their starting point.

### MC::True

This bank includes truth information, linking reconstructed to generated
particles.

### MC::User

# Status Variables

Different detectors have different conventions on their status variables
in HIPO banks. The only standard is that zero is good. Here we document
the other possible values.

# Non-DST Detector Banks

The `index` variable in REC::"Response" banks refers to more specialized
detector banks that are the output of the detectors' reconstruction
services and input to the Event Builder. *Note, these banks will not be
included in DSTs.* Here's a table of those banks names and their
corresponding `detector` identifier in DST banks.

\|}
