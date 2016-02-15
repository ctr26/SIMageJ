
import ij.*;
import ij.process.*;
import ij.gui.*;

import java.awt.*;

import ij.plugin.ImageCalculator;
import ij.plugin.filter.*;

/*********************************
 * 
 * URGENT NEEDS TO INCORPROATE tukeyProcess INTO RUN
 * 
 * @author ctr26@cam.ac.uk
 *
 */

public class Tukey_2D implements PlugInFilter{
	
	int height;
	int width;
	float input[][] = new float[width][height];
	ImagePlus TukeyMaskImg;
	
	ImagePlus img;	
	ImagePlus processed;
	public void run(ImageProcessor ip,double r) {

			
		ip.insert(processed.getProcessor(),0,0);
		
	}


	public int setup(String arg0, ImagePlus img) {
		
        int[] wList = WindowManager.getIDList();
        if (wList == null && !arg0.equals("no image")) {

        	return DOES_ALL;
        }        else{	
		this.img = img;
		
		this.height = img.getHeight();
		this.width = img.getWidth();
		this.input = img.getProcessor().getFloatArray();		
        }
        
        this.processed = tukeyProcess(img,0.1);
		return DOES_ALL;
	}
	
	ImagePlus tukeyProcess(ImagePlus img, double r)
	{

		float[] vertFloat = tukey(img.getHeight(),r);
		float[] horiFloat = tukey(img.getWidth(),r);
		float[][] input = img.getProcessor().getFloatArray();
		
		float output[][] = new float[img.getWidth()][img.getHeight()];
		
		for (int x = 0; x <img.getWidth(); x++)
		{
			for (int y = 0; y <img.getHeight(); y++)
			{
				output[x][y]= vertFloat[x] * horiFloat[y] * input[x][y];
			}
		}

		return new ImagePlus(null,new FloatProcessor(output));

	}
	
	
		public ImagePlus tukeyMask( int width, int height, double r){
			
			ImagePlus vert = null;
			float[] vertFloat = tukey(height,r);
			ImagePlus hori = null;
			float[] horiFloat = tukey(width,r);
			
			float [][] output = new float[width][height];
			
			for (int x = 0; x <width; x++)
			{
				for (int y = 0; y <height; y++)
				{
					output[x][y]= vertFloat[x] * horiFloat[y];
				}
			}
				
	        ImagePlus tukeyMask = IJ.createImage("TukeyMask", "8-bit black", width, height, 1);
			FloatProcessor tukeymaskprocessor = new FloatProcessor(output);		
			tukeyMask.setProcessor(tukeymaskprocessor);	
			
			this.TukeyMaskImg = new ImagePlus("TukeyMask",tukeymaskprocessor);
			return TukeyMaskImg;			
		}
	
	
		static float[] tukey(int points){
			
			return tukey(points, 0.25);			
		}
		
		
			
		static float[] tukey(int points, double r){			
			double period = r/2;
		double t[] = linspace(0,1,points);		
		int topleft = (int) (Math.floor(period*(points-1)));
		int topright = points-topleft-1;
		float w[] = new float[points];
		int i = 0;
			for (i = 0; i < topleft; i++)
			{
	
				w[i] = (float) (1 + Math.cos( (Math.PI/period) * (t[i] - period)  ))/2;
				
			}
			
			for (i = topleft; i < topright; i++)
			{
				w[i] = 1;		
				
			}
		
			for (i = topright; i < points; i++)
			{
				w[i] = (float) (1 + Math.cos( (Math.PI/period) * (t[i]  - 1 + period)  ))/2;
				
			}			
		
		return w;
	}
	
	
	public static double[] linspace(double min, double max, int points) {  
	    double[] d = new double[points];  
	    for (int i = 0; i < points; i++){  
	        d[i] = min + i * (max - min) / (points-1);  
	    }  
	    return d;  
	}


	public void run(ImageProcessor ip) {

		double r = 0.5000;
		
		ip.insert(processed.getProcessor(),0,0);
		
	}





}
