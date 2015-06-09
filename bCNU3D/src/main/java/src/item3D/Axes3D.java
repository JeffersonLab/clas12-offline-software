package item3D;

import item3D.Axis3D.AxisType;

import java.awt.Color;
import java.awt.Font;

import bCNU3D.Panel3D;
import bCNU3D.Support3D;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;

public class Axes3D extends Item3D {

    private Axis3D _xAxisItem;
    private Axis3D _yAxisItem;
    private Axis3D _zAxisItem;
    
    /**
     * A set of Cartesian axes
     * @param panel3D the owner 3D panel
     * @param limits the limits as [xmin, xmax, ymin, ymax, zmin, zmax]
     * @param color the color
     * @param lineWidth the line width
     * @param numMajorTicks the number of major ticks
     * @param textColor color of axis labels
     * @param font the text font
     */
    public Axes3D(Panel3D panel3D, float[] limits, Color color, float lineWidth, int numMajorTicks, Color textColor, Font font) {
	   // public Axis3D(AxisType type, float vmin, float vmax, Color color, float lineWidth) {
        super(panel3D);
	_xAxisItem = new Axis3D(panel3D, Axis3D.AxisType.X_AXIS, limits[0], limits[1], color, lineWidth, numMajorTicks, textColor, font);
	_yAxisItem = new Axis3D(panel3D, Axis3D.AxisType.Y_AXIS, limits[2], limits[3], color, lineWidth, numMajorTicks, textColor, font);
	_zAxisItem = new Axis3D(panel3D, Axis3D.AxisType.Z_AXIS, limits[4], limits[5], color, lineWidth, numMajorTicks, textColor, font);
	
	addChild(_xAxisItem);
	addChild(_yAxisItem);
	addChild(_zAxisItem);
	
	setColor(color);
	setLineWidth(lineWidth);
    }
    
    /**
     * A set of Cartesian axes
     * @param panel3D the owner 3D panel
     * @param xmin the minimum value for x
     * @param xmax the maximum value for x
     * @param ymin the minimum value for y
     * @param ymax the maximum value for y
     * @param zmin the minimum value for z
     * @param zmax the maximum value for z
     * @param color the color
     * @param lineWidth the line width
     * @param numMajorTicks the number of major ticks
     * @param textColor color of axis labels
     * @param font the text font
     */
    public Axes3D(Panel3D panel3D, float xmin, float xmax, float ymin, float ymax, 
	    float zmin, float zmax, Color color, float lineWidth, int numMajorTicks, Color textColor, Font font) {
	this(panel3D, Support3D.toArray(xmin, xmax, ymin, ymax, zmin, zmax), color, lineWidth, numMajorTicks, textColor, font);
    }
    
    @Override
    public void draw(GLAutoDrawable drawable) {
    }
    

}
