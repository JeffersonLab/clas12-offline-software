package cnuphys.bCNU.attributes;

import java.awt.Color;
import java.awt.Font;
import java.io.File;

import cnuphys.bCNU.util.Environment;
import cnuphys.bCNU.util.Fonts;

/**
 * A convenient set of common attributes. You don't have to use one of these as
 * the key--you can use any object.
 * 
 * @author heddle
 * 
 */

public enum AttributeType {
    AZIMUTH, BACKGROUND, BACKGROUNDIMAGE, BOTTOMMARGIN, CENTER, CLASSIFICATION, CLOSABLE, CONFIGFILE, CONTAINER, DATADIR, DRAGGABLE, FILLCOLOR, FONT, FOREGROUND, FRACTION, HEADSUP, HEIGHT, ICONIFIABLE, LABELS, LEFT, LEFTMARGIN, LINECOLOR, LINESTYLE, LINEWIDTH, MAXIMIZE, MAXIMIZABLE, NAME, OPTIONMENUDEFAULTS, RESIZABLE, RIGHTMARGIN, ROTATABLE, SPLITWESTCOMPONENT, STANDARDVIEWDECORATIONS, SYMBOLTYPE, SYMBOLSIZE, TEXT, TEXTCOLOR, TITLE, TOOLBAR, TOOLBARBITS, TOP, TOPMARGIN, USERBITS, UUID, VIEWTYPE, VISIBLE, WINDOWMENU, WORLDSYSTEM, WIDTH, XLABEL, YLABEL;

    private static final Color defaultTransparent = new Color(128, 128, 192,
	    128);

    /**
     * Convenience routine for filling an Attributes object with default values
     * for the "standard" attributes. There is not requirement to use the
     * standard attributes, or just the standard attributes, to the defaults for
     * the standard attributes. If an attribute is not given a default its
     * "default default" is <code>null</code>, so always code for that
     * possibility.
     * 
     * @param attributes
     *            the object to fill with defaults for the standard attributes.
     */
    public static void setDefaults(Attributes attributes) {
	if (attributes == null) {
	    return;
	}
	attributes.add(BOTTOMMARGIN, 0);
	attributes.add(CLASSIFICATION, "UNCLASSIFIED");
	attributes.add(CONFIGFILE, "");
	attributes.add(DATADIR, Environment.getInstance()
		.getCurrentWorkingDirectory() + File.separator + "data");
	attributes.add(FILLCOLOR, defaultTransparent);
	attributes.add(FONT, Fonts.commonFont(Font.PLAIN, 36));
	attributes.add(FRACTION, 0.0);
	attributes.add(HEIGHT, 0);
	attributes.add(LABELS, true);
	attributes.add(LEFT, 0);
	attributes.add(LEFTMARGIN, 0);
	attributes.add(LINECOLOR, Color.black);
	attributes.add(NAME, "???");
	attributes.add(OPTIONMENUDEFAULTS, true);
	attributes.add(RIGHTMARGIN, 0);
	attributes.add(TEXTCOLOR, Color.black);
	attributes.add(TEXT, "");
	attributes.add(TITLE, "???");
	attributes.add(TOP, 0);
	attributes.add(TOPMARGIN, 0);
	attributes.add(VIEWTYPE, -1);
	attributes.add(VISIBLE, true);
	attributes.add(WINDOWMENU, true);
	attributes.add(WIDTH, 0);
    }

}
