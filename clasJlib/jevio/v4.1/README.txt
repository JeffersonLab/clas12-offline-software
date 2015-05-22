The main differences between jevio-4.0 and jevio-4.1 are:
    
    1) 4.1 is able to read and write files bigger than 2.1GB
    
    2) 4.1 has new classes to read buffers and files which
           do not deserialize what it is reading into EvioEvent
           objects (limited to files/buffers < 2.1GB in size).
           This gains about a factor of 10 in speed.
           There is an ability to add banks to the end of an
           existing event. So the new reader can act as a very
           limited writer.

    3) 4.1 has an improved gui to look at evio files that can
           be run with: java org/jlab/coda/jevio/gui/EventTreeFrame
           
    4) There is a change from the previous releases of 4.1.
       The class EvioCompactEventReader has been replace by
       the class EvioCompactStructureHandler which is more general.
       It's constructor and setBuffer methods take an extra argument
       of DataType. In existing code, just add the literal argument of
       "DataType.BANK" or "DataType.ALSOBANK".
