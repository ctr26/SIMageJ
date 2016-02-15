

import java.util.concurrent.RecursiveAction;

import org.apache.commons.math3.complex.Complex;


import edu.emory.mathcs.parallelfftj.FloatTransformer;
import edu.emory.mathcs.parallelfftj.FourierDomainOriginType;
import edu.emory.mathcs.parallelfftj.SpectrumType;
import edu.emory.mathcs.parallelfftj.Transformer;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;

public class simProcess{
	
	static ImagePlus input;	
	static int inWidth;
	static int inHeight;

	float[][] k_sum_real;
	float[][] k_sum_imag;

	
	double fringeFrequency[] = {136.852546526160,135.741042289866,135.439705087354};
	double fringeAngle[] = {-0.513593210927861,0.526789085380402,-1.55759150441705};

	
	double dx = 0.04; // pixel size of the image [um] (field of view [um]/number of pixels in image)
	double n_immersion = 1.515; // refractive index of immersion oil
	double lambda = 0.520; // centre wavelength for fluorescence emission
	double NA = 1.3; // numerical aperture of lens
	
	//cutoff Frequency of coherent system f = w / lambda zi.  Pupil size 2w and zi = distance from exit pupil to imageplane
	
	//f = 2NA / LAMBDA
	
	//psf = 0.61 lambda / na
	
	double r;

	int Orientations = 3;
	int Phases = 3;
	
	static float r0 ; // pixel width of OTF divided by image size in pixels
	
	double Progress = 0;
	
	Transformer transformer;

	

	ComplexImagePlus k0_complex[];

	static float[][] trianglemask ;
	static Weightings weights ;
	
	int Real =0;
	int Imag =1;
	
	float[][][][] k_sim;
	ComplexImagePlus[] kp1_complex;
	
	ImagePlus inputArray[][] = new ImagePlus[Orientations][Phases];			
	ImageStack tD_FFTStack[] = new ImageStack[Orientations];
	

	int n = 0;

	Complex ck[] = {new Complex(2.239,-0.4049),new Complex(-1.3669,-1.3063),new Complex(-3.8236,0.5917)}; // Known Complex weightings ps_speck "Stack.tif"

	private float[][][] k0;
	private int ThreadNumber;


	
	simProcess(ImagePlus input){	
		this.input = input;

		this.inWidth = input.getWidth();
		this.inHeight = input.getHeight();


		this.k_sum_real  = new float[inWidth][inHeight];
		this.k_sum_imag  = new float[inWidth][inHeight];

		this. r0 = (float) 300/(input.getWidth()*2); // pixel width of OTF divided by image size in pixels
		this.r = (2d * NA)/(lambda*dx);

		this.k0_complex = new ComplexImagePlus[Orientations];

		k0 = new float[2][1024][1024];
		this.trianglemask = TriangleMask(inWidth*2,(float) r0*inWidth,0,0);
		this.weights = new Weightings(new ComplexImagePlus(new ImagePlus(null,new FloatProcessor(new OTF_2D(inWidth*2,r0).get()))),inWidth,inHeight);

		this.k_sim = new float[Orientations][2][inWidth*2][inHeight*2];

		this.kp1_complex = new ComplexImagePlus[Orientations];


		}

	
simProcess(ImagePlus input,int Loop){	
this.input = input;

this.inWidth = input.getWidth();
this.inHeight = input.getHeight();


this.k_sum_real  = new float[inWidth][inHeight];
this.k_sum_imag  = new float[inWidth][inHeight];

this. r0 = (float) 300/(input.getWidth()*2); // pixel width of OTF divided by image size in pixels
this.r = (2d * NA)/(lambda*dx);

this.k0_complex = new ComplexImagePlus[Orientations];

k0 = new float[2][1024][1024];
this.trianglemask = TriangleMask(inWidth*2,(float) r0*inWidth,0,0);
this.weights = new Weightings(new ComplexImagePlus(new ImagePlus(null,new FloatProcessor(new OTF_2D(inWidth*2,r0).get()))),inWidth,inHeight);

this.k_sim = new float[Orientations][2][inWidth*2][inHeight*2];

this.kp1_complex = new ComplexImagePlus[Orientations];

getksimOrder(Loop);

}

	float[][][] getK0()
	{
		k0[Real]=k0_complex[0].getReal().getProcessor().getFloatArray();
		k0[Imag]=k0_complex[0].getImag().getProcessor().getFloatArray();
		
		return k0;
	}
	
	static ComplexImagePlus getOTFMask()
	{

		return weights.getOTFMask();
	}
	
	static float[][] getTriangleMask(){
		return trianglemask;
	}
	
	


	float[][][] getksimOrder(int Orientation){
		

			
		long startTime = System.currentTimeMillis();

			ComplexImagePlus shift_cosine = (new CosineShift(fringeAngle[Orientation],fringeFrequency[Orientation],inWidth*2)).get();

			tD_FFTStack[Orientation] = new ImageStack(input.getWidth(),input.getHeight());		
		
			for (int Phase = 0; Phase < Phases; Phase++)
			{
				int count = Orientations*Orientation + Phase + 1;
				//System.out.println(n);
				inputArray[Orientation][Phase] = new ImagePlus(null,input.getStack().getProcessor(count));

				IJ.showStatus("Tukey window to remove FFT artefacts");
				inputArray[Orientation][Phase] = new Tukey_2D().tukeyProcess(inputArray[Orientation][Phase],0.1);
				tD_FFTStack[Orientation].addSlice(inputArray[Orientation][Phase].getProcessor());	
												
			}
			
			IJ.showStatus("Start 3D FFT");
			long startTimeFFT3d = System.currentTimeMillis();			
			
			transformer = new FloatTransformer(tD_FFTStack[Orientation]);
			transformer.fft();
			

			k0_complex[Orientation] = new ComplexImagePlus(
					new ImagePlus(null,((transformer.toImagePlus(SpectrumType.REAL_PART, FourierDomainOriginType.AT_CENTER)).getStack()).getProcessor(2))
				   ,new ImagePlus(null,((transformer.toImagePlus(SpectrumType.IMAG_PART, FourierDomainOriginType.AT_CENTER)).getStack()).getProcessor(2)));	
			
			this.kp1_complex[Orientation] = new ComplexImagePlus(
					new ImagePlus(null,((transformer.toImagePlus(SpectrumType.REAL_PART, FourierDomainOriginType.AT_CENTER)).getStack()).getProcessor(3))
				   ,new ImagePlus(null,((transformer.toImagePlus(SpectrumType.IMAG_PART, FourierDomainOriginType.AT_CENTER)).getStack()).getProcessor(3)));
			
		
			long endTimeFFT3d = System.currentTimeMillis();
			IJ.showStatus("Finished 3D FFT:" + String.valueOf((endTimeFFT3d - startTimeFFT3d )) + " ms");
			System.out.println("Finished 3D FFT:" + String.valueOf((endTimeFFT3d - startTimeFFT3d )) + " ms");
			
			this.Progress+=(1d/(2d*(double)Orientations));
			IJ.showProgress(Progress);

			
			IJ.showStatus("Padding in k-Space");
			
			this.k0_complex[Orientation].setReal(pad(
					k0_complex[Orientation].getReal(),
					k0_complex[Orientation].getReal().getWidth()/2,
					k0_complex[Orientation].getReal().getWidth()/2,
					k0_complex[Orientation].getReal().getHeight()/2,
					k0_complex[Orientation].getReal().getHeight()/2
					
					));
			
			this.k0_complex[Orientation].setImag(pad(
					k0_complex[Orientation].getImag(),
					k0_complex[Orientation].getImag().getWidth()/2,
					k0_complex[Orientation].getImag().getWidth()/2,
					k0_complex[Orientation].getImag().getHeight()/2,
					k0_complex[Orientation].getImag().getHeight()/2
					));


			
			this.kp1_complex[Orientation].setReal(pad(
					kp1_complex[Orientation].getReal(),
					kp1_complex[Orientation].getReal().getWidth()/2,
					kp1_complex[Orientation].getReal().getWidth()/2,
					kp1_complex[Orientation].getReal().getHeight()/2,
					kp1_complex[Orientation].getReal().getHeight()/2
					
					));
			
			this.kp1_complex[Orientation].setImag(pad(
					kp1_complex[Orientation].getImag(),
					kp1_complex[Orientation].getImag().getWidth()/2,
					kp1_complex[Orientation].getImag().getWidth()/2,
					kp1_complex[Orientation].getImag().getHeight()/2,
					kp1_complex[Orientation].getImag().getHeight()/2
					
					));
			
			
			this.kp1_complex[Orientation] = kp1_complex[Orientation].ifft(FourierDomainOriginType.AT_ZERO);					
			this.kp1_complex[Orientation] = kp1_complex[Orientation].times(shift_cosine);			
			IJ.showStatus("Real space cosine multiplication");
			this.kp1_complex[Orientation] = kp1_complex[Orientation].fft(FourierDomainOriginType.AT_ZERO);			
			IJ.showStatus("Producing a fourier shift");
			
		    
		    

			
	/*				
				
		
		k_sim = (c + i*k)*( d + h *i) + (c - i*k)*( f + g *i)  + exp(i * atan(c,k))*( d + h *i) + exp(-i * atan(c,k))*( f + g *i)
		
		
//REAL = (1/A + 1) ( c * (d + f) - k * (g + h)))
//IMAG = (1/A + 1) ( k * (d - f) - c * (h - g)))
				
REAL = cd/A + cf/A + gk/A - hk/A + cd + cf + gk - hk;
IMAG = dk/A + fk/A + cg/A  + ch/A + cg + ch + dk - fk;

REAL = (cd/A + cd) + (cf/A + cf) + (gk/A + gk) - (hk/A + hk );
IMAG = (dk/A + dk) - (fk/A + fk) + (cg/A + cg) + (ch/A + ch );

REAL = (1/A + 1) (cd + cf + gk - hk);
IMAG = (1/A + 1) (dk + cg + ch - fk);	

d = real(kp1) = kp1.getReal()
h = im(kp1) = kp1.getImag()

c = real(ck) 
k = im(ck)

f = real(kp1.rotate(180)) = (new ComplexImagePlus(kp1).rotate(180)).getReal()
g = im(kp1.rotate(180))  = (new ComplexImagePlus(kp1).rotate(180)).getImag()

A = mod(ck) = sqrt(c^2 + k^2)



cd = kp1.getReal().times(ck.re());
cf = (new ComplexImagePlus(kp1).rotate(180)).getReal().times2(ck.re());
gk =  (new ComplexImagePlus(kp1).rotate(180)).getImag().times2(ck.im()) ;
hk =  kp1.getImag().times(ck.im());

dk = kp1.getReal().times2(ck.im());
fk = (new ComplexImagePlus(kp1).rotate(180)).getReal().times2(ck.im());
cg = (new ComplexImagePlus(kp1).rotate(180)).getImag().times2(ck.re());
ch = kp1.getImag().times2(ck.re());


*/

			    


		    	
				IJ.showStatus("Reconstructing for Orientation " + String.valueOf(Orientation+1));
				
				long startComplexWeights = System.currentTimeMillis();			
				
			    
		    	 float[][] d;
		    	 float[][] h;
		    	// float[][] f;
		    	// float[][] g;
		    	 double k;
		    	 double c;
			    
				d = kp1_complex[Orientation].getReal().getProcessor().getFloatArray();
				h = kp1_complex[Orientation].getImag().getProcessor().getFloatArray();	
				
				ImageProcessor d_ip = kp1_complex[Orientation].getReal().getProcessor();
				ImageProcessor h_ip = kp1_complex[Orientation].getReal().getProcessor();
				d_ip.rotate(180);
				h_ip.rotate(180);
				
				//f = d_ip.getFloatArray();
				//g = h_ip.getFloatArray();

				ck[Orientation] =  weights.Complex_Weightings(Orientation,k0_complex,kp1_complex);
				c = ck[Orientation].getReal();
				k = ck[Orientation].getImaginary();
				
				double scale = Math.sqrt(ck[Orientation].getReal() * ck[Orientation].getReal() + ck[Orientation].getImaginary()*ck[Orientation].getImaginary());
				
				for (int p = 0; p < inWidth*2 ; p++)
				{
					for (int q = 0 ; q < inHeight*2 ; q ++)
					{

						
						k_sim[Orientation][0][p][q] =  (float) (((1/scale)+1) * ( (c * (d[p][q] + d[inWidth*2-1-p][1024-1-q])) - (k * (h[inWidth*2-1-p][inWidth*2-1-q] + h[p][q] )) ));
						k_sim[Orientation][1][p][q] =  (float) (((1/scale)+1) * ( (k * (d[p][q] - d[inWidth*2-1-p][1024-1-q])) - (c * (h[inWidth*2-1-p][inWidth*2-1-q] - h[p][q] )) ));
						
						
						//REAL[Orientation][p][q] =  (float) (((1/scale)+1) * ( (c * (d[p][q] + f[p][q])) - (k * (g[p][q] + h[p][q] )) ));
						//IMAG[Orientation][p][q] =  (float) (((1/scale)+1) * ( (k * (d[p][q] - f[p][q])) - (c * (g[p][q] - h[p][q] )) ));

					}
				}	
				
				this.Progress+=(1d/(2d*(double)Orientations));
				IJ.showProgress((double) Progress);
				
				long endComplexWeights = System.currentTimeMillis();
				IJ.showStatus("Finished Complex Weighting: " + String.valueOf((endComplexWeights - startComplexWeights )) + " ms");
				System.out.println("Finished Complex Weighting: " + String.valueOf((endComplexWeights - startComplexWeights )) + " ms");
				
				
				return k_sim[Orientation];
		}
	
	public ImagePlus pad(ImagePlus img,int padLeft,int padRight,int padTop,int padBottom)
	{
		
		
		ImageProcessor ip = img.getProcessor();
		
	int oldw = ip.getWidth();
	int oldh = ip.getHeight();
	int neww = oldw+padLeft+padRight;
	int newh = oldh+padTop+padBottom;
	

	if (ip instanceof ShortProcessor)
		{ImageProcessor p;
		p = new ShortProcessor (neww, newh);}
	else if (ip instanceof ByteProcessor){
		ImageProcessor p;
		p = new ByteProcessor (neww, newh);
		}
	else if (ip instanceof ColorProcessor)
		
	{
		ImageProcessor p;
		p = new ColorProcessor (neww, newh);
	for (int j=0; j < oldh; j++)
		{
		int jj = j+padTop;
		if (jj >= 0 && jj < newh)
			{
			for (int i=0; i < oldw; i++)
				{
				int ii = i+padLeft;
				if (ii >= 0 && ii <= neww)
					p.putPixel (ii,jj,ip.getPixel(i,j));
				}
			}
		}
	
	return new ImagePlus (img.getTitle(), p);
	}
	else{
				
			float[][] padded = new float[neww][newh];
			float[][] orig = ip.getFloatArray();
			
			
			for (int j=0; j < oldh; j++)
			{
			int jj = j+padTop;
			if (jj >= 0 && jj < newh)
				{
				for (int i=0; i < oldw; i++)
					{
					int ii = i+padLeft;
					if (ii >= 0 && ii <= neww)
						padded[ii][jj] = orig[i][j];
					}
				}
			}
		FloatProcessor p = new FloatProcessor (padded);	
			
	return new ImagePlus (img.getTitle(), p);
			
	}
	return null;
	
	
	}
	
	
	public static float[][] TriangleMask(int n,float r0,int x0,int y0)
	{
		float r[][]= new float[n][n];
		double jy = 0;
		double ix = 0;
		
		double half_n = n/2;
		double r_temp;
		
		
		for (int  x= 0; x < n ; x++)
		{
			ix = x - half_n;
			
			for (int y = 0; y < n ; y++)
			{
				jy = y - half_n;
				
				r_temp = Math.hypot(ix, jy);			

				r[x][y] = (float) (r_temp*(-1/r0) + 1);
						
				if (r[x][y] <= 0)
				{
					r[x][y] = 0;
				}
			}
		}
		
		return r;
	}

	public int getOrientations() {
		// TODO Auto-generated method stub
		return Orientations;
	}


	float[][][] getK_sim(int n){
		return k_sim[n];
	}

}
