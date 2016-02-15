

import ij.*;
import ij.process.*;
import ij.gui.*;

import java.awt.*;

import ij.plugin.*;
import ij.plugin.filter.Filler;
import ij.plugin.filter.ImageMath;
import ij.plugin.frame.*;

import java.awt.Button;
import java.awt.Component;
import java.awt.Label;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;
import java.util.Stack;
import java.util.logging.Logger;
import java.lang.Math.*;

import javax.swing.SwingUtilities;

import org.apache.commons.math3.complex.Complex;

import sun.rmi.runtime.Log;
import ij.gui.Roi;
import ij.plugin.*;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;
import ij.process.StackProcessor;
import edu.emory.mathcs.parallelfftj.DoubleTransformer;
import edu.emory.mathcs.parallelfftj.FloatTransformer;
import edu.emory.mathcs.parallelfftj.FourierDomainOriginType;
import edu.emory.mathcs.parallelfftj.SpectrumType;
import edu.emory.mathcs.parallelfftj.Transformer;
import edu.emory.mathcs.utils.ConcurrencyUtils;

public class ComplexImagePlus extends Object {

	public ImagePlus real,imag;
    public static Transformer transformer;
    public FourierDomainOriginType domain = FourierDomainOriginType.AT_CENTER;
    public ImagePlus frequency_img;

    public ComplexImagePlus(ImagePlus real,ImagePlus imag) {
        this.real=real;
        this.imag=imag;
        
        this.transformer = new FloatTransformer(real.getStack(),imag.getStack());
    }
    
    public ComplexImagePlus(int width,int height) {
        this.real=new ImagePlus("Real",new FloatProcessor(new float[width][height]));
        this.imag=new ImagePlus("Imag",new FloatProcessor(new float[width][height]));
        
        this.transformer = new FloatTransformer(real.getStack(),imag.getStack());
    }
    
    
    
    public ComplexImagePlus(ImagePlus real) {
        this.real=real;        
        this.imag=real.duplicate();
        
        //new Filler().clear(this.imag.getProcessor());
        this.imag.getProcessor().setColor(0);
        this.imag.getProcessor().fill();
        
        this.transformer = new FloatTransformer(real.getStack(),imag.getStack());
    }
    
    public ComplexImagePlus(ComplexImagePlus cimp) {
        this.real=cimp.getReal();        
        this.imag=cimp.getImag();
        this.transformer = cimp.getTransformer();
    }
    

    public ComplexImagePlus(Complex[][] k_sum) {
		// TODO Auto-generated constructor stub
    	
    	float[][] real_array = new float[k_sum.length][k_sum[0].length];
    	float[][] imag_array = new float[k_sum.length][k_sum[0].length];
    	
    	for (int i = 0; i < k_sum.length ; i ++)
    	{
    		for (int j = 0; j < k_sum[0].length ; j ++)
    		{
    			real_array[i][j] = (float) k_sum[i][j].getReal();
    			imag_array[i][j] = (float) k_sum[i][j].getImaginary();
    		}
    	}
    	
    	this.real = new ImagePlus("Real", new FloatProcessor(real_array));
    	this.imag = new ImagePlus("Imaginary", new FloatProcessor(imag_array));
	}

	private Transformer getTransformer() {

		return transformer;
	}

	public ImagePlus getReal() {
        return real;
    }
    
    public void setReal(ImagePlus real) {
        this.real = real;
    }
    
    public ImagePlus getImag() {
        return imag;
    }
    
    public void setImag(ImagePlus imag) {
        this.imag = imag;
    }
    
    public ComplexImagePlus fft(FourierDomainOriginType domain){
    	this.domain = domain;
    	
    	transformer = new FloatTransformer(real.getStack(),imag.getStack());
    	transformer.fft();	
    	
    	//this.transformer = transformer;
    	frequency_img = new ImagePlus(null,transformer.toImagePlus(SpectrumType.FREQUENCY_SPECTRUM,domain).getProcessor());
		return new ComplexImagePlus(
					transformer.toImagePlus(SpectrumType.REAL_PART,domain)
				   ,transformer.toImagePlus(SpectrumType.IMAG_PART,domain)
				   );
        
    }
    
    public ComplexImagePlus ifft(FourierDomainOriginType domain){
    	this.domain = domain;
    	
    	transformer = new FloatTransformer(real.getStack(),imag.getStack());
    	transformer.ifft(true);	
    	
    	//this.transformer = transformer;
    	frequency_img = new ImagePlus(null,transformer.toImagePlus(SpectrumType.FREQUENCY_SPECTRUM,domain).getProcessor());
		return new ComplexImagePlus(
					transformer.toImagePlus(SpectrumType.REAL_PART,domain)
				   ,transformer.toImagePlus(SpectrumType.IMAG_PART,domain)
				   );
        
    }
    
    public ImagePlus frequency(){
    	
		return frequency_img;
        
    }
    
    public static ImagePlus frequency(FourierDomainOriginType domain){

    	ImagePlus img = transformer.toImagePlus(SpectrumType.FREQUENCY_SPECTRUM,domain);
		return img;
        
    }
    
    
    public ImagePlus abs() {
    	//ImagePlus square_real = new ImagePlus();
    	//ImagePlus square_imag = new ImagePlus();
    		//square_real = new ImageCalculator().run("Multiply create",real,real);
    		//square_imag = new ImageCalculator().run("Multiply create",imag,imag);
    		
    		
        	float square_real_float[][] = real.getProcessor().getFloatArray();
        	float square_imag_float[][] = imag.getProcessor().getFloatArray();
        	
        	
        	float output[][] = new float[real.getWidth()][real.getHeight()];
        	
        	for (int i = 0; i < real.getWidth(); i++)
        	{
            	for (int j = 0; j < real.getWidth(); j++)
            	{
            		output[i][j] = (float) Math.sqrt(square_real_float[i][j]*square_real_float[i][j] - square_imag_float[i][j]*square_imag_float[i][j]);
            	}
        	}
    		
    	return new ImagePlus(null,new FloatProcessor(output));
    	
        //return new ImageCalculator().run("Add create",square_real,square_imag);
    }
  
    
    public ImagePlus mod() {
    	//ImagePlus square_real = new ImagePlus();
    	//ImagePlus square_imag = new ImagePlus();
    		//square_real = new ImageCalculator().run("Multiply create",real,real);
    		//square_imag = new ImageCalculator().run("Multiply create",imag,imag);
    		
    		
        	float square_real_float[][] = real.getProcessor().getFloatArray();
        	float square_imag_float[][] = imag.getProcessor().getFloatArray();
        	
        	
        	float output[][] = new float[real.getWidth()][imag.getHeight()];
        	
        	for (int i = 0; i < real.getWidth(); i++)
        	{
            	for (int j = 0; j < real.getWidth(); j++)
            	{
            		output[i][j] = (float) Math.sqrt(square_real_float[i][j]*square_real_float[i][j] + square_imag_float[i][j]*square_imag_float[i][j]);
            	}
        	}
    		
    	return new ImagePlus(null,new FloatProcessor(output));
    	
        //return new ImageCalculator().run("Add create",square_real,square_imag);
    }


    public ComplexImagePlus rotate(double angle) {
    	
    	ImagePlus rot_real = real.duplicate();
    	ImagePlus rot_imag = imag.duplicate();
    	
  	
    	ImageProcessor rot_real_ip = rot_real.getProcessor();
    	ImageProcessor rot_imag_ip = rot_imag.getProcessor();
    	
    	rot_real_ip.rotate(angle);
    	rot_imag_ip.rotate(angle);
    	
    	return new ComplexImagePlus(new ImagePlus(real.getTitle(),rot_real_ip),new ImagePlus(imag.getTitle(),rot_imag_ip));
    }
    
    
    
    public ComplexImagePlus translate(double x, double y) {
    	
    	ImagePlus trans_real = real.duplicate();
    	ImagePlus trans_imag = imag.duplicate();
    	
  	
    	ImageProcessor trans_real_ip = trans_real.getProcessor();
    	ImageProcessor trans_imag_ip = trans_imag.getProcessor();
    	
    	trans_real_ip.translate(x,y);
    	trans_imag_ip.translate(x,y);
    	
    	return new ComplexImagePlus(new ImagePlus(real.getTitle(),trans_real_ip),new ImagePlus(imag.getTitle(),trans_imag_ip));
    }
    
    public ImagePlus translatereal(double x, double y) {
    	
    	ImagePlus trans_real = real.duplicate();
    	ImagePlus trans_imag = imag.duplicate();
    	
  	
    	ImageProcessor trans_real_ip = trans_real.getProcessor();
    	ImageProcessor trans_imag_ip = trans_imag.getProcessor();
    	
    	trans_real_ip.translate(x,y);
    	trans_imag_ip.translate(x,y);
    	
    	return new ImagePlus(real.getTitle(),trans_real_ip);
    }

    public ComplexImagePlus conj() {
    	ImagePlus conj = imag.duplicate();    	
    	ImageProcessor ip = conj.getProcessor();
    	ip.multiply(-1);
    	//return new ComplexImagePlus(real,new ImagePlus(imag.getTitle(),ip));
    	
    	return new ComplexImagePlus(real,new ImagePlus(imag.getTitle(),ip));
    }
    
    public ComplexImagePlus plus(ComplexImagePlus w) {
        return new ComplexImagePlus(new ImageCalculator().run("Add create", getReal(),w.getReal()),new ImageCalculator().run("Add create", getImag(),w.getImag()));
    }
    
   
    public ComplexImagePlus minus(ComplexImagePlus w) {
        return new ComplexImagePlus(new ImageCalculator().run("Subtract create", getReal(),w.getReal()),new ImageCalculator().run("Add create", getImag(),w.getImag()));
    }
 
	public ComplexImagePlus times(Complex ck) {
		// TODO Auto-generated method stub
		ImageProcessor real_ip = real.getProcessor();
		ImageProcessor imag_ip = imag.getProcessor();
		
		real_ip.multiply(ck.getReal());		
		imag_ip.multiply(ck.getImaginary());
		
		 //new ImageCalculator().run("Subtract create",new ImagePlus(null,real_ip),new ImagePlus(null,imag_ip));
		//real_ip.subtract(imag_ip);
		
		

		
		return new ComplexImagePlus(
				new ImageCalculator().run("Subtract create"	,new ImagePlus(null,real_ip),new ImagePlus(null,imag_ip)),
				new ImageCalculator().run("Add create"		,new ImagePlus(null,real_ip),new ImagePlus(null,imag_ip)));
		
		
	}
	
	public ComplexImagePlus times2(Complex ck) {
		// TODO Auto-generated method stub
		ImageProcessor real_ip = real.getProcessor();
		ImageProcessor imag_ip = imag.getProcessor();
		
		float real_float[][] = real.getProcessor().getFloatArray();		
		float imag_float[][] = imag.getProcessor().getFloatArray();	
		ComplexImagePlus output;
		
		float real_output[][] = new float[real.getWidth()][real.getWidth()];		
		float imag_output[][] = new float[real.getWidth()][real.getWidth()];	
		
		
		for (int i = 0; i < real.getWidth(); i++){
			
			for (int j = 0; j < real.getWidth(); j++){
				
				real_output[i][j] =  (float) (real_float[i][j] * ck.getReal() - imag_float[i][j] * ck.getImaginary());
				
				imag_output[i][j] =  (float) (real_float[i][j] * ck.getImaginary() - real_float[i][j] * ck.getReal());
				
			}
			
			
		}
		
		  return new ComplexImagePlus(new ImagePlus(null,new FloatProcessor(real_output)),new ImagePlus(null,new FloatProcessor(imag_output)));

		
		
		//imag_ip.multiply(ck.im());
		
		 //new ImageCalculator().run("Subtract create",new ImagePlus(null,real_ip),new ImagePlus(null,imag_ip));
		//real_ip.subtract(imag_ip);
		
		

		
		//return new ComplexImagePlus(
				//new ImageCalculator().run("Subtract create"	,new ImagePlus(null,real_ip),new ImagePlus(null,imag_ip)),
				//new ImageCalculator().run("Add create"		,new ImagePlus(null,real_ip),new ImagePlus(null,imag_ip)));
		
		
	}
	
	public ComplexImagePlus times(double a) {
		// TODO Auto-generated method stub
		ImageProcessor real_ip = real.getProcessor();
		ImageProcessor imag_ip = imag.getProcessor();
		
		real_ip.multiply(a);		
		imag_ip.multiply(a);
		
		 //new ImageCalculator().run("Subtract create",new ImagePlus(null,real_ip),new ImagePlus(null,imag_ip));
		//real_ip.subtract(imag_ip);

		
		return new ComplexImagePlus(
				new ImagePlus(null,real_ip),
				new ImagePlus(null,imag_ip));
	}	
    
    
    
    public ComplexImagePlus times(ComplexImagePlus w) {
    	
    	ImagePlus ac = new ImagePlus();
		ImagePlus db = new ImagePlus();
		
	
			ac = new ImageCalculator().run("Multiply create",real,w.getReal());				
			db = new ImageCalculator().run("Multiply create",imag,w.getImag());
		
		ImagePlus iad = new ImagePlus();
		ImagePlus ibc = new ImagePlus();
		
			iad = new ImageCalculator().run("Multiply create",real,w.getImag());
			ibc = new ImageCalculator().run("Multiply create",imag,w.getReal());
			
		ImagePlus real_temp = new ImagePlus();
		
			real_temp = new ImageCalculator().run("Subtract create",ac,db);
			
		ImagePlus imag_temp = new ImagePlus();
		
			imag_temp= new ImageCalculator().run("Add create",iad,ibc);
			
    	return new ComplexImagePlus(real_temp,imag_temp);
    }
    
    public ComplexImagePlus divide(ComplexImagePlus w) {
    	ImagePlus ac = new ImagePlus();
		ImagePlus db = new ImagePlus();
    	ImagePlus cc = new ImagePlus();
		ImagePlus dd = new ImagePlus();
		ImagePlus ccdd = new ImagePlus();
	
			ac = new ImageCalculator().run("Multiply create",real,w.getReal());				
			db = new ImageCalculator().run("Multiply create",imag,w.getImag());
			
			cc = new ImageCalculator().run("Multiply create",w.getReal(),w.getReal());
			dd = new ImageCalculator().run("Multiply create",real,real);
			
			ccdd = new ImageCalculator().run("Add create",cc,dd);
			
			ac = new ImageCalculator().run("Divide create",ac,ccdd);	
			db = new ImageCalculator().run("Divide create",db,ccdd);	
				
			
			
		
		ImagePlus iad = new ImagePlus();
		ImagePlus ibc = new ImagePlus();
		
			iad = new ImageCalculator().run("Multiply create",real,w.getImag());
			ibc = new ImageCalculator().run("Multiply create",imag,w.getReal());
			
			iad = new ImageCalculator().run("Divide create",iad,ccdd);	
			ibc = new ImageCalculator().run("Divide create",ibc,ccdd);	
			
		ImagePlus real_temp = new ImagePlus();
		
			real_temp = new ImageCalculator().run("Add create",ac,db);
			
			
		ImagePlus imag_temp = new ImagePlus();
		
			imag_temp= new ImageCalculator().run("Subtract create",ibc,iad);
			
    	return new ComplexImagePlus(real_temp,imag_temp);
    }




  
}