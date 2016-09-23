/**
 * 
 */
package org.jlab.rec.tof.hit.ctof;

import java.io.FileNotFoundException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.jlab.geom.component.ScintillatorPaddle;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Path3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.rec.ctof.CTOFGeometry;
import org.jlab.rec.ctof.CalibrationConstantsLoader;
import org.jlab.rec.ctof.Constants;
import org.jlab.rec.tof.banks.ctof.HitReader;
import org.jlab.rec.tof.hit.AHit;
import org.jlab.rec.tof.hit.IGetCalibrationParams;
import org.jlab.service.ctof.CTOFEngine;

/**
 * @author ziegler
 *
 */
public class Hit extends AHit implements IGetCalibrationParams {

	
	public Hit(int id, int panel, int sector, int paddle, int aDCU, int tDCU, int aDCD, int tDCD)  {
		super(id, panel, sector, paddle, aDCU, tDCU, aDCD, tDCD);
	}
	
	private Line3D 	_paddleLine;		// paddle line 
	
	public Line3D get_paddleLine() {
		return _paddleLine;
	}
	
	public void set_paddleLine(Line3D _paddleLine) {
		this._paddleLine = _paddleLine;
	}
	
	public void set_HitParameters(int superlayer) {
		
		double pl = this.get_paddleLine().length();
		
		// Get all the constants used in the hit parameters calculation
		double TW0L = this.TW01();
		double TW0R = this.TW02();
		double TW1L = this.TW11();
		double TW1R = this.TW12();
		double lambdaL = this.lambda1();
		this.set_lambda1(lambdaL);
		this.set_lambda1Unc(this.lambda1Unc());
		double lambdaR = this.lambda1();
		this.set_lambda2(lambdaR);
		this.set_lambda2Unc(this.lambda2Unc());
		double yOffset = this.yOffset();		
		double vL = this.v1();
		double vR = this.v2();
		double vLUnc = this.v1Unc();
		double vRUnc = this.v2Unc();
		double PEDL = this.PED1();
		double PEDR = this.PED2();
		double PEDLUnc = this.PED1Unc();
		double PEDRUnc = this.PED2Unc();
		double paddle2paddle = this.PaddleToPaddle();
		double timeOffset = this.TimeOffset();
		double LSBConv = this.LSBConversion();
		double LSBConvErr = this.LSBConversionUnc();
		double ADCUErr = this.ADC1Unc();
		double ADCDErr = this.ADC2Unc();
		double TDCUErr = this.TDC1Unc();
		double TDCDErr = this.TDC2Unc();
		double ADC_MIP = this.ADC_MIP();
		double ADC_MIPErr = this.ADC_MIPUnc();
		double DEDX_MIP = this.DEDX_MIP();
		double ScinBarThickn = this.ScinBarThickn();
		
		this.set_HitParams(superlayer, TW0L, TW0R, TW1L, TW1R, lambdaL, lambdaR, yOffset, vL, vR, vLUnc, vRUnc, PEDL, PEDR, PEDLUnc, PEDRUnc, paddle2paddle, timeOffset, LSBConv, LSBConvErr, ADCUErr, ADCDErr, TDCUErr, TDCDErr, ADC_MIP, ADC_MIPErr, DEDX_MIP, ScinBarThickn, pl);
		// Set the hit position in the local coordinate of the bar
		this.set_Position(this.calc_hitPosition());

	}
	

	private Point3D calc_hitPosition() {
		Point3D hitPosition = new Point3D();
        Vector3D dir = new Vector3D(
                this.get_paddleLine().end().x() - this.get_paddleLine().origin().x(),
                this.get_paddleLine().end().y() - this.get_paddleLine().origin().y(),
                this.get_paddleLine().end().z() - this.get_paddleLine().origin().z()
        );
        dir.unit();  
        Point3D startpoint = this.get_paddleLine().origin();
        double L_2 = this.get_paddleLine().length()/2;
        hitPosition.setX(startpoint.x() + (L_2+this.get_y())*dir.x());
        hitPosition.setY(startpoint.y() + (L_2+this.get_y())*dir.y());
        hitPosition.setZ(startpoint.z() + (L_2+this.get_y())*dir.z());
        
        return hitPosition;
	}
	 
	public void printInfo() {
		DecimalFormat form = new DecimalFormat("#.##");
		String s = " CTOF Hit in  Paddle "+this.get_Paddle()+" with Status "+this.get_StatusWord()+" in Cluster "+this.get_AssociatedClusterID()+" : \n"+
				"  ADCU =  "+this.get_ADC1()+ 
				"  ADCD =  "+this.get_ADC2()+ 
				"  TDCU =  "+this.get_TDC1()+ 
				"  TDCD =  "+this.get_TDC2()+ 
				"\n  tU =  "+form.format(this.get_t1())+ 
				"  tD =  "+form.format(this.get_t2())+ 
				"  t =  "+form.format(this.get_t())+ 
				"  timeWalkU =  "+form.format(this.get_timeWalk1())+ 
				"  timeWalkD =  "+form.format(this.get_timeWalk2())+ 
				"  lambdaU =  "+form.format(this.get_lambda1())+ 
				"  lambdaD =  "+form.format(this.get_lambda2())+ 
				"  Energy =  "+form.format(this.get_Energy())+ 
				"  EnergyU =  "+form.format(this.get_Energy1())+ 
				"  EnergyD =  "+form.format(this.get_Energy2())+ 
				"  y =  "+form.format(this.get_y())+"\n ";
		if(this.get_Position()!=null)  {
			s+=	"  xPos =  "+form.format(this.get_Position().x())+
				"  yPos =  "+form.format(this.get_Position().y())+
				"  zPos =  "+form.format(this.get_Position().z())+
				"\n ";
		}
		System.out.println(s);
	}


	@Override
	public int compareTo(AHit arg) {
		// Sort by sector, panel, paddle
		int return_val = 0 ;
		int CompSec = this.get_Sector() < arg.get_Sector()  ? -1 : this.get_Sector()  == arg.get_Sector()  ? 0 : 1;
		int CompPan = this.get_Panel()  < arg.get_Panel()   ? -1 : this.get_Panel()   == arg.get_Panel()   ? 0 : 1;
		int CompPad = this.get_Paddle() < arg.get_Paddle()  ? -1 : this.get_Paddle()  == arg.get_Paddle()  ? 0 : 1;
		
		int return_val1 = ((CompPan ==0) ? CompPad : CompPan); 
		return_val = ((CompSec ==0) ? return_val1 : CompSec);
		
		return return_val;
	}
	

	@Override
	public double TW01() {
		double TW0U = CalibrationConstantsLoader.TW0U[this.get_Sector()-1][this.get_Panel()-1][this.get_Paddle()-1];
		
		return TW0U;
	}


	@Override
	public double TW02() {
		double TW0D = CalibrationConstantsLoader.TW0D[this.get_Sector()-1][this.get_Panel()-1][this.get_Paddle()-1];
		
		return TW0D;
	}


	@Override
	public double TW11() {
		double TW1U = CalibrationConstantsLoader.TW1U[this.get_Sector()-1][this.get_Panel()-1][this.get_Paddle()-1];
		
		return TW1U;
	}


	@Override
	public double TW12() {
		double TW1D = CalibrationConstantsLoader.TW1D[this.get_Sector()-1][this.get_Panel()-1][this.get_Paddle()-1];
		
		return TW1D;
	}


	@Override
	public double lambda1() {
		return CalibrationConstantsLoader.LAMBDAU[this.get_Sector()-1][this.get_Panel()-1][this.get_Paddle()-1];
	}


	@Override
	public double lambda2() {
		return CalibrationConstantsLoader.LAMBDAD[this.get_Sector()-1][this.get_Panel()-1][this.get_Paddle()-1];
	}


	@Override
	public double lambda1Unc() {
		return CalibrationConstantsLoader.LAMBDAUU[this.get_Sector()-1][this.get_Panel()-1][this.get_Paddle()-1];
	}


	@Override
	public double lambda2Unc() {
		return CalibrationConstantsLoader.LAMBDADU[this.get_Sector()-1][this.get_Panel()-1][this.get_Paddle()-1];
	}


	@Override
	public double yOffset() {
		return CalibrationConstantsLoader.YOFF[this.get_Sector()-1][this.get_Panel()-1][this.get_Paddle()-1];
	}


	@Override
	public double v1() {
		return CalibrationConstantsLoader.EFFVELU[this.get_Sector()-1][this.get_Panel()-1][this.get_Paddle()-1];
	}


	@Override
	public double v2() {
		return CalibrationConstantsLoader.EFFVELD[this.get_Sector()-1][this.get_Panel()-1][this.get_Paddle()-1];
	}


	@Override
	public double v1Unc() {
		return CalibrationConstantsLoader.EFFVELUU[this.get_Sector()-1][this.get_Panel()-1][this.get_Paddle()-1];
	}


	@Override
	public double v2Unc() {
		return CalibrationConstantsLoader.EFFVELDU[this.get_Sector()-1][this.get_Panel()-1][this.get_Paddle()-1];
	}


	@Override
	public double PED1() {
		return Constants.PEDU[this.get_Panel()-1];
	}

	@Override
	public double PED2() {
		return Constants.PEDD[this.get_Panel()-1];
	}
	
	@Override
	public double PED1Unc() {
		return Constants.PEDUUNC[this.get_Panel()-1];
	}

	@Override
	public double PED2Unc() {
		return Constants.PEDDUNC[this.get_Panel()-1];
	}

	@Override
	public double ADC1Unc() {
		return Constants.ADCJITTERU;
	}
	
	@Override
	public double TDC2Unc() {
		return Constants.TDCJITTERD;
	}
	
	@Override
	public double TDC1Unc() {
		return Constants.TDCJITTERU;
	}
	
	@Override
	public double ADC2Unc() {
		return Constants.ADCJITTERD;
	}
	@Override
	public double PaddleToPaddle(){
		return CalibrationConstantsLoader.PADDLE2PADDLE[this.get_Sector()-1][this.get_Panel()-1][this.get_Paddle()-1];	
	}
	
	@Override
	public double TimeOffset() {
		return CalibrationConstantsLoader.UD[this.get_Sector()-1][this.get_Panel()-1][this.get_Paddle()-1];
	}
	
	@Override
	public double LSBConversion() {
		return Constants.LSBCONVFAC;
	}
	
	@Override
	public double LSBConversionUnc() {
		return Constants.LSBCONVFACERROR;
	}
	
	@Override
	public double ADC_MIP() {
		return Constants.ADC_MIP[this.get_Panel()-1];
	}
	
	@Override
	public double ADC_MIPUnc() {
		return Constants.ADC_MIP_UNC[this.get_Panel()-1];
	}
	
	@Override
	public double DEDX_MIP() {
		return Constants.DEDX_MIP;
	}
	
	@Override
	public double ScinBarThickn() {
		return Constants.SCBARTHICKN[this.get_Panel()-1];
	}
	
	/**

	 * @param geometry TO DO GET THE CTOF GEOMETRY
	 * @return a line representing the direction and end points of the paddle bar
	 */
	public Line3D calc_PaddleLine( CTOFGeometry ctofDetector) {
		ScintillatorPaddle geomPaddle = (ScintillatorPaddle) ctofDetector.getScintillatorPaddle(this.get_Paddle());
		Line3D lineX = geomPaddle.getLine(); // Line representing the paddle 

        return lineX;
	}
	
	
	public static void main (String arg[]) throws FileNotFoundException{

		CTOFEngine rec = new CTOFEngine() ;		
		rec.init();
		HitReader hrd = new HitReader();
		
		// get the status
		int id = 1;
		int sector = 1;
		int paddle = 21;
		// set the superlayer to get the paddle position from the geometry package
		int superlayer = 1;
		List<ArrayList<Path3D>> trks = null;
		List<double[]> paths = null;
		CTOFGeometry geometry = new CTOFGeometry();
		//Detector geometry = rec.getGeometry("CTOF");		
		int statusL = CalibrationConstantsLoader.STATUSU[sector-1][0][paddle-1];
		int statusR = CalibrationConstantsLoader.STATUSD[sector-1][0][paddle-1];
		// create the hit object
		Hit hit = new Hit(id, 1, sector, paddle, 900, 900, 800, 1000) ;
		String statusWord = hrd.set_StatusWord(statusL, statusR, hit.get_ADC1(), hit.get_TDC1(), hit.get_ADC2(), hit.get_TDC2());
		// get the line in the middle of the paddle
		hit.set_paddleLine(hit.calc_PaddleLine(geometry)); 
		hit.set_StatusWord(statusWord);
		hit.set_HitParameters(superlayer);
		// read the hit object
		System.out.println(" hit "); hit.printInfo();
		
	}


}
