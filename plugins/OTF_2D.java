

import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.process.FloatProcessor;

public class OTF_2D implements PlugIn{
	

	public int width = 1024;	
	public float r0 = (float) 300/width;
	public float[][] OTF2D;
	public float[][] MAG = new float[width][width];
	
	public OTF_2D(int width, float r0) {
		// TODO Auto-generated constructor stub
		
		this.width = width;
		this.r0 = r0;
	}
	
	public OTF_2D() {
		// TODO Auto-generated constructor stub
		
		//this.width = width;
		//this.r0 = r0;
	}

	public void run(String arg,String arg1)
	{
		if (arg != null)
		{
			this.width = Integer.parseInt(arg);
			//this.r0 = Double.parseDouble(arg1);
			
		}
		
		
			
		//this.OTF2D = OTF2D(width,r0);
		ImagePlus img  = new ImagePlus("OTF2D",new FloatProcessor(OTF2D(width,r0)));
		img.show();
	}
	
	public void run(String arg)
	{
		GenericDialog gd = new GenericDialog("New Image");
	
		double dx = 65d;
		double lambda = 488d;
		double NA =  1.4d;
		double r;
		int width = 512;
		
		  gd.addNumericField("width /px: ", width, 0);
	      gd.addNumericField("dx nm/px: ", dx, 1);
	      gd.addNumericField("lambda /nm: ",lambda, 0);
	      gd.addNumericField("NA: ",NA, 3);
	      gd.showDialog();
	      if (gd.wasCanceled()) return;
	      
	      width = (int) gd.getNextNumber();
	      dx = gd.getNextNumber()*0.001d;
	      lambda = gd.getNextNumber()*0.001d;
	      NA = gd.getNextNumber();
				
			r = (2d * NA )/(lambda*dx);	
			System.out.println(r);
			
			float r0 = (float) (2d*r)/(width);
	      
		IJ.showStatus("Running");
		
		
		
		//OTF2D(width,r0);
		ImagePlus img  = new ImagePlus("OTF2D",new FloatProcessor(OTF2D(width,r0)));
		img.show();
		
		
		//new ImagePlus("OTF2D",new FloatProcessor(MAG)).show();
		IJ.showStatus("Complete");
	}
	
	
	float[][] OTF2D(int width, float r0a){
		float OTF2D[][] = new float[width][width];

		int x;
		int y;
		double r;
		double temp;
		

		//float step = (float) 0.007827788;
		double step = 4d/((double) width-1d);
		double  X = 0 ;
		double  Y  =0;
		
				
		//r0 = 0.29296875;
		//r0 = (float) 150/width;
		double r0 = (double) r0a;
		//System.out.println(r0);
		double  X0 = -2;
		double Y0 = -2; 
		
		for (x = 0; x < width ; x ++)
		{	
			
			//X = -2; 
			//Y = -2;
			
			for (y = 0; y < width ; y ++)
			{
				
				//Use floats rather than doubles, ImageJ or java struggles with doubles.
				
				
				X = X0 + ((double) x)*step ;
				Y = Y0 + ((double) y)*step ;
					
				//System.out.println(X);
				
				r = Math.hypot(X,Y);
				
				//System.out.println(r);
				
				temp = ((2/Math.PI)*(Math.acos(r/(2*r0)) - (r/(2d*r0))  *  Math.sqrt(1-(   (r/(2d*r0)) * (r/(2d*r0) ) ) )));
				//temp = ((2d/Math.PI)*(Math.acos(r/(2d*((double) r0))) - (r/(2d*((double) r0)))  *  Math.sqrt(1-(   (r/(2d*((double) r0))) * (r/(2d*((double) r0)) ) ) )));
				//temp = (2d/Math.PI)*Math.acos(r/(2d*(r0))) - r/(2d*(r0)) * Math.sqrt(1-((r/(2d*(r0))) * (r/(2d*(r0)) ) ) ) ;
				
				
				//System.out.println(temp);
				
				if (Double.isNaN(temp))
					temp = 0;
				
				//IJ.showStatus(String.valueOf(temp));
				//IJ.showMessage(String.valueOf(temp));
				
				//if (r > 2*r0)
				//{
				//	temp = 0;
				//}
				
				OTF2D[x][y] = (float) temp;
				
			}
		}

		return OTF2D;

	}

	public float[][] get() {
		
		
		return OTF2D(width,r0);
	}
	
	
}
