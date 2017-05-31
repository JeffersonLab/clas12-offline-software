package com.nr.lna;

import static java.lang.Math.*;
import java.io.*;

public class PSpage {

    PrintWriter PLT;
    File file;
    String fontname;
    double fontsize;

    public PSpage(final File filname) throws IOException {
	file = filname;
	PLT = new PrintWriter(new java.io.FileWriter(file));
	PLT.printf("%%!\n/mt{moveto}def /lt{lineto}def /np{newpath}def\n");
	PLT.printf("/st{stroke}def /cp{closepath}def /fi{fill}def\n");
	PLT.printf("/zp {gsave /ZapfDingbats findfont exch ");
	PLT.printf("scalefont setfont moveto show grestore} def\n");
	setfont("Times-Roman", 12.);
	setlinewidth(0.5);
    }

    public PSpage() {
    }

    public void setfont(final String fontnam, final double size) {
	fontname = new String(fontnam);
	fontsize = size;
	PLT.printf("/%s findfont %g scalefont setfont\n", fontnam, size);
    }

    public void setcolor(final int r, final int g, final int b) {
	PLT.printf("%g %g %g setrgbcolor\n", r / 255., g / 255., b / 255.);
    }

    public void setdash(final String patt) {
	setdash(patt, 0);
    }

    public void setdash(final String patt, final int phase) {
	PLT.printf("[%s] %d setdash\n", patt, phase);
    }

    public void setlinewidth(final double w) {
	PLT.printf("%g setlinewidth\n", w);
    }

    public void setgray(final double w) {
	PLT.printf("%g setgray\n", w);
    }

    public void gsave() {
	PLT.printf("gsave\n");
    }

    public void grestore() {
	PLT.printf("grestore\n");
    }

    public void rawps(final String text) {
	PLT.printf("%s\n", text);
    }

    public void addtext(final String text) {
	PLT.printf("(%s) show ", text);
    }

    public void puttext(final String text, final double x, final double y) {
	puttext(text, x, y, 0.0);
    }

    public void puttext(final String text, final double x, final double y,
	    final double rot) {
	PLT.printf("gsave %g %g translate %g rotate 0 0 mt ", x, y, rot);
	addtext(text);
	PLT.printf("grestore \n");
    }

    public void putctext(final String text, final double x, final double y) {
	putctext(text, x, y, 0.0);
    }

    public void putctext(final String text, final double x, final double y,
	    final double rot) {
	PLT.printf("gsave %g %g translate %g rotate 0 0 mt (%s) ", x, y, rot,
		text);
	PLT.printf("dup stringwidth pop 2 div neg 0 rmoveto show grestore\n");
    }

    public void putrtext(final String text, final double x, final double y) {
	putrtext(text, x, y, 0.0);
    }

    public void putrtext(final String text, final double x, final double y,
	    final double rot) {
	PLT.printf("gsave %g %g translate %g rotate 0 0 mt (%s) ", x, y, rot,
		text);
	PLT.printf("dup stringwidth pop neg 0 rmoveto show grestore\n");
    }

    public void close() {
	PLT.printf("showpage\n");
	PLT.close();
	PLT = null;
    }

    public void pointsymbol(final double x, final double y, final int num,
	    final double size) {
	PLT.printf("(\\%03o) %g %g %g zp\n", num, x - 0.394 * size, y - 0.343
		* size, size);
    }

    public void lineseg(final double xs, final double ys, final double xf,
	    final double yf) {
	PLT.printf("np %g %g mt %g %g lt st\n", xs, ys, xf, yf);
    }

    public void polyline(final double[] x, final double[] y) {
	polyline(x, y, false, false, false);
    }

    public void polyline(final double[] x, final double[] y,
	    final boolean close, final boolean fill, final boolean clip) {
	int i, n = min(x.length, y.length);
	PLT.printf("np %g %g mt\n", x[0], y[0]);
	for (i = 1; i < n; i++)
	    PLT.printf("%g %g lt\n", x[i], y[i]);
	if (close || fill || clip)
	    PLT.printf("cp ");
	if (fill)
	    PLT.printf("fi\n");
	else if (clip)
	    PLT.printf("clip\n");
	else
	    PLT.printf("st\n");
    }
}
