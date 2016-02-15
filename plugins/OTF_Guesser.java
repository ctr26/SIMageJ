import ij.*;
import ij.process.*;
import ij.gui.*;

import java.awt.*;

import edu.emory.mathcs.parallelfftj.FloatTransformer;
import edu.emory.mathcs.parallelfftj.FourierDomainOriginType;
import edu.emory.mathcs.parallelfftj.SpectrumType;
import ij.plugin.ContrastEnhancer;
import ij.plugin.SubstackMaker;
import ij.plugin.filter.*;

public class OTF_Guesser implements PlugInFilter {
	ImagePlus imp;

	public int setup(String arg, ImagePlus imp) {
		this.imp = imp;
		return DOES_ALL;
	}

	public void run(ImageProcessor ip) {
		
		calcFringeParams(null,imp,3,3,false);
		
		
	}
	
	
	public int calcFringeParams(String arg, ImagePlus imp,int Orientations, int Phases, boolean threaded)  {
		ComplexImagePlus k0_complex[] = new ComplexImagePlus[Orientations];
		ComplexImagePlus k0_sum = null;
		int n = 0;
		for (int i = 0; i < Orientations ; i++)
		{
			
			int from = i*Phases+1;
			int to = i*Phases+(Phases);			
			ImagePlus input_sub = new SubstackMaker().makeSubstack(imp, from + "-" + to);
			
			ImageStack tD_FFTStack = new ImageStack(imp.getWidth(),imp.getHeight());		
			
			ImagePlus[] inputArray =  new ImagePlus[Phases];
			
			for (int Phase = 0; Phase < Phases; Phase++)
			{		
				n++;
				inputArray[Phase] = new ImagePlus(null,imp.getStack().getProcessor(n));				
				//System.out.println(n);
				IJ.showStatus("Tukey window to remove FFT artefacts");
				inputArray[Phase] = new Tukey_2D().tukeyProcess(inputArray[Phase],0.1);
				tD_FFTStack.addSlice(inputArray[Phase].getProcessor());												
			}
			
			FloatTransformer transformer = new FloatTransformer(tD_FFTStack);
			transformer.fft();
			
			
			k0_complex[i] = new ComplexImagePlus(
					new ImagePlus(null,((transformer.toImagePlus(SpectrumType.REAL_PART, FourierDomainOriginType.AT_CENTER)).getStack()).getProcessor(2))
				   ,new ImagePlus(null,((transformer.toImagePlus(SpectrumType.IMAG_PART, FourierDomainOriginType.AT_CENTER)).getStack()).getProcessor(2)));	
			
			//k0_complex[i].mod().show(); 
					
			if (i == 0)
				k0_sum = new ComplexImagePlus(k0_complex[i]);
			else
				k0_sum = k0_sum.plus(k0_complex[i]);

		}				
		

		ImagePlus k0 = k0_sum.mod();
		k0.show();
		//ContrastEnhancer ce = new ContrastEnhancer();
			//ce.setNormalize(true);
		 	
		 	IJ.run(k0, "16-bit", "");
		 	IJ.run(k0, "Enhance Contrast...", "saturated=0 normalize equalize");
		 	//ce.equalize(k0.getProcessor());
		 	//ce.stretchHistogram(k0.getProcessor(), 0);

		 	Biscuit_Cutting(k0).show();;
		return 0;
	}
	
	ImagePlus Biscuit_Cutting(ImagePlus image){
		double X,Y;
		double r;
		double line[] = new double[image.getWidth()/2];
		double[] n = new double[image.getWidth()/2];
		for (int x = 0; x < image.getWidth() ; x ++)
		{	
			
			//X = -2; 
			//Y = -2;
			
			for (int y = 0; y < image.getHeight() ; y ++)
			{
				
				X = x - image.getWidth()/2;
				Y = y - image.getHeight()/2;				
				r = Math.hypot(X,Y);
				
				if ((int) Math.round(r) < image.getWidth()/2){
					//System.out.println(r);
					n[(int) Math.round(r)]++;
					line[(int) Math.round(r)] = line[(int) Math.round(r)]  + image.getProcessor().getPixel((int) Math.round(x),(int) Math.round(y));
					}
			}
		}
		double[] against = new double[(image.getWidth()/2)];
		for (int i=0; i < (image.getWidth()/2);i++)
		{
			line[i] = line[i] / n[i]; //averaging.			
			against[i] = i;
		}
		
		Plot plot = new Plot("Radial Profile Plot", "Radius [pixels]", "Normalized Integrated Intensity",  against, line);
		plot.show();
		
		float[][] ImageArray = new float[image.getWidth()][image.getHeight()];
		
		for (int x = 0; x < image.getWidth() ; x ++)
		{	
			
			for (int y = 0; y < image.getHeight() ; y ++)
			{
				
				X = x - image.getWidth()/2;
				Y = y - image.getHeight()/2;				
				r = Math.hypot(X,Y);
				
				//line[(int) Math.round(r)];
				
				if ((int) Math.round(r) < image.getWidth()/2)
					ImageArray[x][y] = (float) line[(int) Math.round(r)];
				else
					ImageArray[x][y] = (float)  0 ;
				 
				 
				/*if ((int) Math.round(r) < image.getWidth()/2){
					//System.out.println(r);
					n[(int) Math.round(r)]++;
					line[(int) Math.round(r)] = line[(int) Math.round(r)]  + image.getProcessor().putPixel((int) Math.round(x),(int) Math.round(y), x);
					}*/
			}
		}
		

		return new ImagePlus("Smooooooooth", new FloatProcessor(ImageArray));		
		
	}

}
