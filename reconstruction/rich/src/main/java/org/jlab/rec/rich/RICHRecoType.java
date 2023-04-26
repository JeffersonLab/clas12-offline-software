package org.jlab.rec.rich;

/*
 * @author mcontalb
 */
enum RICHRecoType{
        ANALYTIC (0, "analytic"), TRACED(1, "traced"), UNDEFINED (3, "undefined");

        private String stringtype;
        private int id;

        private RICHRecoType(int id, String stringtype){ this.id=id; this.stringtype = stringtype;}

        public String type() {return stringtype;}
        public int    id() {return id;}
    }
