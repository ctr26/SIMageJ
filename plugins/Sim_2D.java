import java.io.InputStream;
import java.lang.reflect.Array;
import java.net.URL;
import java.util.concurrent.RecursiveAction;

import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import ij.*;
import ij.io.Opener;
import ij.plugin.*;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;
import edu.emory.mathcs.parallelfftj.FloatTransformer;
import edu.emory.mathcs.parallelfftj.FourierDomainOriginType;
import edu.emory.mathcs.parallelfftj.SpectrumType;
import edu.emory.mathcs.parallelfftj.Transformer;
import edu.emory.mathcs.utils.ConcurrencyUtils;
import no.uib.cipr.matrix.DenseMatrix;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.linear.Array2DRowFieldMatrix;
import org.apache.commons.math3.linear.FieldMatrix;
import org.ejml.simple.SimpleMatrix;

import cern.colt.function.tfloat.FloatFunction;
import cern.colt.matrix.tdcomplex.DComplexFactory2D;
import cern.colt.matrix.tdcomplex.DComplexMatrix2D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tfloat.FloatFactory2D;
import cern.colt.matrix.tfloat.FloatMatrix2D;


public class Sim_2D extends RecursiveAction implements PlugIn  {

	ImagePlus input;
	
	int inWidth;
	int inHeight;

	int Orientations;
	int Phases ;
	
	private float[][] k_sum_real;
	private float[][] k_sum_imag;

	double fringeFrequency[];
	double fringeAngle[];

	
	double dx; // pixel size of the image [um] (field of view [um]/number of pixels in image)
	double n_immersion; // refractive index of immersion oil
	double lambda; // centre wavelength for fluorescence emission
	double NA; // numerical aperture of lens
	Weightings weights;
	float[][] trianglemask;
	//cutoff Frequency of coherent system f = w / lambda zi.  Pupil size 2w and zi = distance from exit pupil to imageplane
	
	//f = 2NA / LAMBDA
	
	//psf = 0.61 lambda / na
	
	double r;


	float r0; // pixel width of OTF divided by image size in pixels
	
	double Progress = 0;

	private JProgressBar progressBar = null;

	private Complex[][][] k_sim;

	private Complex[][] k_sum;
	
	public Sim_2D(){
		
		this.fringeFrequency = new double[]{136.852546526160,135.741042289866,135.439705087354};
		this.fringeAngle = new double[]{-0.513593210927861,0.526789085380402,-1.55759150441705};

		
		this.dx = 0.04d; // pixel size of the image [um] (field of view [um]/number of pixels in image)
		this.n_immersion = 1.515d; // refractive index of immersion oil
		this.lambda = 0.520d; // centre wavelength for fluorescence emission
		this.NA = 1.3d; // numerical aperture of lens
		
		//double r = (2d * NA)/(lambda*dx);		//in pixels
		//System.out.println(r);
		InputStream is = getClass().getResourceAsStream("Test.tif");
		
        Opener opener = new Opener();
        input = opener.openTiff(is, "Input");
        System.out.println("Constructing");
        
        
		this.r = (2d * NA)/(lambda*dx);
		this.r0 = (float) (2d*r)/(input.getWidth()*2f); // pixel width of OTF divided by image size in pixels	
		
		System.out.println("Theoretical OTF Radius\t" + r0);       
        
		//ImagePlus input = new ImagePlus("C\\Stack.tif");
		
		//ImagePlus input = IJ.openImage("C:\\Stack.tif");		
		

		
		this.inWidth = input.getWidth();
		this.inHeight = input.getHeight();


		
		this.Orientations = 3;
		this.Phases = 3;

		trianglemask = TriangleMask(inWidth*2,(float) r0*inWidth,0,0);
		weights = new Weightings(new ComplexImagePlus(new ImagePlus(null,new FloatProcessor(new OTF_2D(inWidth*2,r0).get()))),inWidth,inHeight);		
	}
	
	public void setup(JProgressBar progressBar,ImagePlus ext_input,float[][] OTF_2D,
					  double[]fringeFrequency,
					  double[]fringeAngle,
					  //double dx,
					  //double n_immersion,
					  //double lambda,
					  //double NA,
					  int Orientations,
					  int Phases,
					  float[][] trianglemask){
		this.progressBar  = progressBar;
		progressBar.setStringPainted(true);
		progressBar.setString("SIM Reconstruction - Setup");
		this.fringeFrequency = fringeFrequency;
		this.fringeAngle = fringeAngle;

		
		//this.dx = dx; // pixel size of the image [um] (field of view [um]/number of pixels in image)
		//this.n_immersion = n_immersion; // refractive index of immersion oil
		//this.lambda = lambda; // centre wavelength for fluorescence emission
		//this.NA = NA; // numerical aperture of lens
		
		//double r = (2d * NA)/(lambda*dx);		//in pixels
		//System.out.println(r);
		//InputStream is = getClass().getResourceAsStream("Test.tif");
		
        //Opener opener = new Opener();
       // ImagePlus input = opener.openTiff(is, "Input");
        
        input = ext_input;

		this.r = (2d * NA * n_immersion)/(lambda*dx);		
		r0 = (float) (2d*r)/(input.getWidth()*2f);		// pixel width of OTF divided by image size in pixels	
		
		//System.out.println("r0  " + r0);        
        
		//ImagePlus input = new ImagePlus("C\\Stack.tif");		
		//ImagePlus input = IJ.openImage("C:\\Stack.tif");		

		
		this.inWidth = input.getWidth();
		this.inHeight = input.getHeight();
		
		this.Orientations = Orientations;
		this.Phases = Phases;

		this.trianglemask = trianglemask;
		weights = new Weightings(new ComplexImagePlus(new ImagePlus(null,new FloatProcessor(OTF_2D))),inWidth,inHeight);		
	}
	
	public void run(String arg) {
		/*if (arg == "no_setup")
		{
			
		}
		else
		{
			setup();
		}*/
		//double fringeFrequency[] = {136.852546526160,135.741042289866,135.439705087354};
		//double fringeAngle[] = {-0.513593210927861,0.526789085380402,-1.55759150441705};

		
		//double dx = 0.04; // pixel size of the image [um] (field of view [um]/number of pixels in image)
		//double n_immersion = 1.515; // refractive index of immersion oil
		////double lambda = 0.520; // centre wavelength for fluorescence emission
		//double NA = 1.3; // numerical aperture of lens
		
		//cutoff Frequency of coherent system f = w / lambda zi.  Pupil size 2w and zi = distance from exit pupil to imageplane
		
		//f = 2NA / LAMBDA
		
		//psf = 0.61 lambda / na
		
		//double r = (2d * NA)/(lambda*dx);
		
		
		this.Progress = 0;
		progressBar.setString("SIM Reconstruction - Running");
		this.k_sum_real  = new float[inWidth][inHeight];
		this.k_sum_imag  = new float[inWidth][inHeight];
		
		Transformer transformer;
		ConcurrencyUtils.setNumberOfThreads(ConcurrencyUtils.getNumberOfProcessors());
		
		
		ComplexImagePlus kp1_complex[] = new ComplexImagePlus[Orientations];
		ComplexImagePlus k0_complex[] = new ComplexImagePlus[Orientations];

		float[][][] k_sim_real = new float[Orientations][inWidth*2][inHeight*2];
		float[][][] k_sim_imag = new float[Orientations][inWidth*2][inHeight*2];
		
		Complex [][][] k_sim = new Complex[Orientations][inWidth*2][inHeight*2];
		
		//// Create stretched image for comparison with input. Padded in kspace

		ImagePlus input_widefield = kspace_pad(input,input.getWidth()*2,input.getHeight()*2);
		input_widefield.setTitle("Input_Widefield");
		input_widefield.show();

		
		long startTime = System.currentTimeMillis();
		

		
		ImagePlus inputArray[][] = new ImagePlus[Orientations][Phases];			
		ImageStack tD_FFTStack[] = new ImageStack[Orientations];
		

		int n = 1;

		Complex ck[] = {new Complex(2.239,-0.4049),new Complex(-1.3669,-1.3063),new Complex(-3.8236,0.5917)}; // Known Complex weightings ps_speck "Stack.tif"


		
		for (int Orientation = 0;  Orientation < Orientations; Orientation++)
		{
			System.out.println("Starting Orientation "+ (Orientation+1));
			


			ComplexImagePlus shift_cosine = (new CosineShift(fringeAngle[Orientation],fringeFrequency[Orientation],inWidth*2)).get();

			tD_FFTStack[Orientation] = new ImageStack(input.getWidth(),input.getHeight());		
		
			for (int Phase = 0; Phase < Phases; Phase++)
			{

				inputArray[Orientation][Phase] = new ImagePlus(null,input.getStack().getProcessor(n));
				n++;
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
			
			kp1_complex[Orientation] = new ComplexImagePlus(
					new ImagePlus(null,((transformer.toImagePlus(SpectrumType.REAL_PART, FourierDomainOriginType.AT_CENTER)).getStack()).getProcessor(3))
				   ,new ImagePlus(null,((transformer.toImagePlus(SpectrumType.IMAG_PART, FourierDomainOriginType.AT_CENTER)).getStack()).getProcessor(3)));
			
		
			long endTimeFFT3d = System.currentTimeMillis();
			IJ.showStatus("Finished 3D FFT:" + String.valueOf((endTimeFFT3d - startTimeFFT3d )) + " ms");
			System.out.println("Finished 3D FFT:" + String.valueOf((endTimeFFT3d - startTimeFFT3d )) + " ms");
			
			this.Progress+=(1d/(2d*(double)Orientations));
			progressBar.setValue((int) Math.floor(Progress*100d)); 
			IJ.showProgress(Progress);
			
			
			IJ.showStatus("Padding in k-Space");
			
			k0_complex[Orientation].setReal(pad(
					k0_complex[Orientation].getReal(),
					k0_complex[Orientation].getReal().getWidth()/2,
					k0_complex[Orientation].getReal().getWidth()/2,
					k0_complex[Orientation].getReal().getHeight()/2,
					k0_complex[Orientation].getReal().getHeight()/2
					
					));
			
			k0_complex[Orientation].setImag(pad(
					k0_complex[Orientation].getImag(),
					k0_complex[Orientation].getImag().getWidth()/2,
					k0_complex[Orientation].getImag().getWidth()/2,
					k0_complex[Orientation].getImag().getHeight()/2,
					k0_complex[Orientation].getImag().getHeight()/2
					));


			
			kp1_complex[Orientation].setReal(pad(
					kp1_complex[Orientation].getReal(),
					kp1_complex[Orientation].getReal().getWidth()/2,
					kp1_complex[Orientation].getReal().getWidth()/2,
					kp1_complex[Orientation].getReal().getHeight()/2,
					kp1_complex[Orientation].getReal().getHeight()/2
					
					));
			
			kp1_complex[Orientation].setImag(pad(
					kp1_complex[Orientation].getImag(),
					kp1_complex[Orientation].getImag().getWidth()/2,
					kp1_complex[Orientation].getImag().getWidth()/2,
					kp1_complex[Orientation].getImag().getHeight()/2,
					kp1_complex[Orientation].getImag().getHeight()/2
					
					));
			
			//kp1_complex[Orientation].mod().show();
			kp1_complex[Orientation] = kp1_complex[Orientation].ifft(FourierDomainOriginType.AT_ZERO);					
			kp1_complex[Orientation] = kp1_complex[Orientation].times(shift_cosine);			
			IJ.showStatus("Real space cosine multiplication");
			kp1_complex[Orientation] = kp1_complex[Orientation].fft(FourierDomainOriginType.AT_ZERO);			
			IJ.showStatus("Producing a fourier shift");
			
			//kp1_complex[Orientation].mod().show();
			
		    
		    

			
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
		    	 float[][] f;
		    	// float[][] g;
		    	 double k;
		    	 double c;
			    
				d = kp1_complex[Orientation].getReal().getProcessor().getFloatArray();
				h = kp1_complex[Orientation].getImag().getProcessor().getFloatArray();	
				
				//ImageProcessor d_ip = kp1_complex[Orientation].getReal().getProcessor();
				//ImageProcessor h_ip = kp1_complex[Orientation].getReal().getProcessor();
				//d_ip.rotate(180);
				//h_ip.rotate(180);
				
				//f = d_ip.getFloatArray();
				//g = h_ip.getFloatArray();

				ck[Orientation] =  weights.Complex_Weightings(Orientation,k0_complex,kp1_complex);
				c = ck[Orientation].getReal();
				k = ck[Orientation].getImaginary();
				
				double scale = Math.sqrt(ck[Orientation].getReal() * ck[Orientation].getReal() + ck[Orientation].getImaginary()*ck[Orientation].getImaginary());
				

				
				/*for (int p = 0; p < inWidth*2 ; p++)
				{
					for (int q = 0 ; q < inHeight*2 ; q ++)
					{

						
						k_sim_real[Orientation][p][q] =  (float) (((1/scale)+1) * ( (c * (d[p][q] + d[inWidth*2-1-p][1024-1-q])) - (k * (h[inWidth*2-1-p][inWidth*2-1-q] + h[p][q] )) ));
						k_sim_imag[Orientation][p][q] =  (float) (((1/scale)+1) * ( (k * (d[p][q] - d[inWidth*2-1-p][1024-1-q])) - (c * (h[inWidth*2-1-p][inWidth*2-1-q] - h[p][q] )) ));
						
					}
				}	*/
				
				k_sim[Orientation] = ExpensiveCalc(ck[Orientation],d,h); //not as expensive as it first seemed
				
				
				
				
				this.Progress+=(1d/(2d*(double)Orientations));
				IJ.showProgress((double) Progress);				
				progressBar.setValue((int) Math.floor(Progress*100d)); 
				
				long endComplexWeights = System.currentTimeMillis();
				IJ.showStatus("Finished Complex Weighting: " + String.valueOf((endComplexWeights - startComplexWeights )) + " ms");
				System.out.println("Finished Complex Weighting: " + String.valueOf((endComplexWeights - startComplexWeights )) + " ms");
				
				System.out.println("----------------------------------------");
		} 
		
		long startReconstruction = System.currentTimeMillis();	
		
		k_sum_real = k0_complex[0].getReal().getProcessor().getFloatArray();
		k_sum_imag = k0_complex[0].getImag().getProcessor().getFloatArray();
						
		k_sum = ComplexArray(k0_complex[0]);
		
	for (int Orientation = 0; Orientation < 3 ; Orientation++)
	{
		for (int p = 0; p < inWidth*2 ; p++)
		{
			for (int q = 0 ; q < inHeight*2 ; q ++)
			{

				//k_sum_real[p][q] = k_sum_real[p][q] + k_sim[0][Orientation][p][q];
				//k_sum_imag[p][q] = k_sum_imag[p][q] + k_sim[1][Orientation][p][q];
				
				k_sum[p][q] = k_sum[p][q].add(k_sim[Orientation][p][q]);
			}
		}	
	}
	
	

	
	long endReconstruction = System.currentTimeMillis();
	IJ.showStatus("Finished Reconstruction: " + String.valueOf((endReconstruction - startReconstruction )) + " ms");
	System.out.println("Finished Reconstruction: " + String.valueOf((endReconstruction - startReconstruction )) + " ms");
	
	this.Progress=0.99d;
	progressBar.setValue((int) Math.floor(Progress*100d)); 
	IJ.showProgress((double) Progress);
	
	ComplexImagePlus OTFMask = weights.getOTFMask();
	
	//OTFMask.abs().show();
	//ComplexImagePlus simage = new ComplexImagePlus(new ImagePlus(null,new FloatProcessor(k_sum_real)), new ImagePlus(null,new FloatProcessor(k_sum_imag)));
	ComplexImagePlus simage = new ComplexImagePlus(k_sum);
	ComplexImagePlus TriangleMask = new ComplexImagePlus(new ImagePlus(null,new FloatProcessor(trianglemask)));
	
	simage = simage.times(OTFMask);
	
	ComplexImagePlus simage_filtered = simage.times(TriangleMask);
	
	//simage.abs().show();
		
		
		simage = simage.ifft(FourierDomainOriginType.AT_ZERO);		
		long endTime = (System.currentTimeMillis());

		ImagePlus Recon = simage.mod();
		Recon.setTitle("IJ Reconstruction");
		Recon.show();
		
		
		float duration = (endTime - startTime) / 1000f;
		System.out.println("Reconstruction took " + duration+"s");
		IJ.showStatus(String.valueOf("Reconstruction took " + duration +"s"));
		
		
		this.Progress=1.00d;
		progressBar.setValue((int) Math.floor(Progress*100d)); 
		IJ.showProgress((double) Progress);
		progressBar.setString("SIM Reconstruction - Fin");
//Fin
	}	
	

	private Complex[][] ExpensiveCalc(Complex complex, float[][] d, float[][] h) {
		long startExpensiveCalc = System.currentTimeMillis();
		double scale = Math.sqrt(complex.getReal() * complex.getReal() + complex.getImaginary()*complex.getImaginary());
		
		Complex[][] k_sim= new Complex[inWidth*2][inHeight*2];

		
		double c = complex.getReal();
		double k = complex.getImaginary();
		
		for (int p = 0; p < inWidth*2 ; p++)
		{
			for (int q = 0 ; q < inHeight*2 ; q ++)
			{				

				k_sim[p][q] = new Complex(
						(((1/scale)+1) * ( (c * (d[p][q] + d[inWidth*2-1-p][1024-1-q])) - (k * (h[inWidth*2-1-p][inWidth*2-1-q] + h[p][q] )) )),
						(((1/scale)+1) * ( (k * (d[p][q] - d[inWidth*2-1-p][1024-1-q])) - (c * (h[inWidth*2-1-p][inWidth*2-1-q] - h[p][q] )) ))
						)	;	
				
			}
		}
		long endxpensiveCalc = System.currentTimeMillis();
		//System.out.println("Expensive time "+ (endxpensiveCalc-startExpensiveCalc) + "ms");
		return k_sim;
		
		// TODO Auto-generated method stub
		
	}


	class Weightings {
		int width;
		int height;
		
		Complex ck;
		
		ComplexImagePlus OTFp[] = new ComplexImagePlus[Orientations];
		ComplexImagePlus OTFn[] = new ComplexImagePlus[Orientations];
		
		
		Complex[][] OTFs = new Complex[1024][1024];
		
		Complex[][] OTFp_Complex = new Complex[1024][1024];
		Complex[][] OTFn_Complex =  new Complex[1024][1024];
		Complex[][] OTF0_Complex =  new Complex[1024][1024];

		Complex[][] OTF = new Complex[1024][1024];

		ComplexImagePlus OTF0;
		Weightings(ComplexImagePlus OTF0,int Width,int Height){
			
			this.OTF0 = OTF0;
			this.width = Width;
			this.height = Height;
			
			this.OTF0_Complex = ComplexArray(new ComplexImagePlus(OTF0));
			
			for (int i = 0; i < Orientations; i++)
			{
			
			double otf_x_shift  = -Math.round(fringeFrequency[i]*Math.cos(fringeAngle[i]));				
			double otf_y_shift  =  -Math.round(fringeFrequency[i]*Math.sin(fringeAngle[i]));			
			
			OTFp[i] = OTF0.translate(otf_x_shift, otf_y_shift);
			
			OTFn[i] = OTFp[i].rotate(180);
			
			//OTFP_float[i] = OTFP[i].getReal().getProcessor().getFloatArray();
			
			
			
			}
		}
		
		Complex Complex_Weightings(int orientation, ComplexImagePlus[] k0_complex, ComplexImagePlus[] kp1_complex) {
			
			
			//OTF0.getReal().show();

			
			
			//float[][][] OTFP_float = new float[3][1024][1024];
			//float[][] OTF0_float = OTF0.getReal().getProcessor().getFloatArray();
			
			/*if (orientation == 0 )
					this.OTF0_Complex = ComplexArray(new ComplexImagePlus(OTF0)); //replace with constructor

			for (int i = 0; i < Orientations; i++)
				{
				
				double otf_x_shift  = -Math.round(fringeFrequency[i]*Math.cos(fringeAngle[i]));				
				double otf_y_shift  =  -Math.round(fringeFrequency[i]*Math.sin(fringeAngle[i]));			
				
				OTFp[i] = OTF0.translate(otf_x_shift, otf_y_shift);
				
				OTFn[i] = OTFp[i].rotate(180);
				
				//OTFP_float[i] = OTFP[i].getReal().getProcessor().getFloatArray();
				
				}*/

			//Complex[][] OTFP_Complex = new Complex[1024][1024];
			//Complex OTF0_Complex[][] = new Complex[1024][1024];
			
			this.OTFp_Complex = ComplexArray(OTFp[orientation]);
			this.OTFn_Complex = ComplexArray(OTFn[orientation]);
			

			
			
			//Complex a[][] = new Complex[1024][1024];
			//Complex[][] b = new Complex[1024][1024];
			
			//Complex k0[][] = new Complex[1024][1024];
			//Complex kp1[][] = new Complex[1024][1024];
			
			Complex[][] k0 = ComplexArray(k0_complex[0]);
			
			
			
			//MatFileReader a = new MatFileReader("C:\\User\\Craggles\\Desktop\\re_save_k0");
			//Map<String, MLArray> b = a.getContent(); 
			Complex[][] kp1 = ComplexArray(kp1_complex[orientation]);
			
			Complex sum1 = new Complex(0);
			Complex sum2 = new Complex(0);
			
			Complex a ;
			Complex b;
			Complex dum1_temp;
			Complex dum2_temp;

				long startComplex_Weightings = System.currentTimeMillis();
			for (int x = 0; x < 1024; x++)
			{
				for (int y = 0; y < 1024; y++)
				{
					
					if (orientation == 0)
					{
						OTFs[x][y] = new Complex(0,0);
					}
					//OTFP_Complex[x][y] = new Complex(OTFP_float[1][x][y],0); 
					//OTF0_Complex[x][y] = new Complex(OTF0_float[x][y],0);
					
					//k0[x][y] = new Complex(mat_k0_re[x][y],mat_k0_im[x][y]);
													
					//kp1[x][y] = new Complex(mat_kp1_re[x][y],mat_kp1_im[x][y]);
					//a[x][y] = k0[x][y].multiply(OTF0_Complex[x][y]);
					//b[x][y] = kp1[x][y].multiply(OTFP_Complex[x][y]); 
					
				
					a = k0[x][y].multiply(OTFp_Complex[x][y]);
					

					//out[x][y] = (double) a.getReal();
						//System.out.println(String.valueOf(a));


					b = kp1[x][y].multiply(OTF0_Complex[x][y]); 
					
					dum1_temp = (a.conjugate()).multiply(b);
					dum2_temp = new Complex( ( a.getReal() * a.getReal() ) +   ( a.getImaginary()* a.getImaginary() ) );
					
					
					
					sum1 = sum1.add(dum1_temp);
					sum2 = sum2.add(dum2_temp);

					OTFs[x][y] = OTFs[x][y].add(OTFp_Complex[x][y].add(OTFn_Complex[x][y].conjugate()));

				}
			}
			long endComplex_Weightings = System.currentTimeMillis();
			
			//1. First create example arrays
			//  double[][] src = out;
			//// MLDouble mlDouble = new MLDouble( "double_arr", src);

			// ArrayList list = new ArrayList();
			// list.add( mlDouble );
			
			//MatFileWriter mata = new MatFileWriter("C:\\Users\\Craggles\\Desktop\\matlaba.mat", list );
			
			//IJ.showMessage(String.valueOf(sum1)); 
			//IJ.showMessage(String.valueOf(sum2)); 
			
			//System.out.println(String.valueOf(sum1));
			//System.out.println(String.valueOf(sum2));
			
			
			
			this.ck = new Complex(0.5).divide(sum1.divide(sum2));
			System.out.println("Complex weighting value\t" + String.valueOf(ck));
			//return null;

			//FieldMatrix<Complex> OTFP_Matrix = new Array2DRowFieldMatrix<Complex>(OTFP_Complex); 
			//FieldMatrix<Complex> OTF0_Matrix = new Array2DRowFieldMatrix<Complex>(OTF0_Complex);

			
			System.out.println("Complex loop time "+ (endComplex_Weightings-startComplex_Weightings) + "ms");
			
			return ck;

		}
		
		public Complex getCK(){
			return ck;
		}
		
		public ComplexImagePlus getOTFMask(){
			
			float OTF_real[][] = new float[1024][1024];
			float OTF_imag[][] = new float[1024][1024];
			
			
			for(int n = 0; n  < 1024; n++)
			{
				for (int m = 0; m < 1024 ; m++)
				{
					OTF[n][m] = OTF0_Complex[n][m].add(OTFs[n][m].multiply(0.5));
					
					
					//OTF_real[n][m] = (float) OTF[n][m].getReal();
					//OTF_imag[n][m] = (float) OTF[n][m].getImaginary();
					
					if(OTF[n][m].getReal() > 0)
						OTF_real[n][m] =1;
					else
						OTF_real[n][m] =0;
					
					if(OTF[n][m].getImaginary() > 0)
						OTF_imag[n][m] =1;
					else
						OTF_imag[n][m] =0;

					
					//OTF[n][m] = OTF0_Complex[n][m].add(OTFs[n][m].multiply(0.5));
				}
			}
			//OTF[n][m] = OTF0_Complex[n][m].plus(OTFs[n][m].multiply(0.5));
			
			return new ComplexImagePlus(new ImagePlus(null,new FloatProcessor(OTF_real)),new ImagePlus(null,new FloatProcessor(OTF_imag)));
		}
		
		public ComplexImagePlus getOTF(){
			
			float OTF_real[][] = new float[1024][1024];
			float OTF_imag[][] = new float[1024][1024];
			
			
			for(int n = 0; n  < 1024; n++)
			{
				for (int m = 0; m < 1024 ; m++)
				{
					OTF[n][m] = OTF0_Complex[n][m].add(OTFs[n][m].multiply(0.5));
					
					
					OTF_real[n][m] = (float) OTF[n][m].getReal();
					OTF_imag[n][m] = (float) OTF[n][m].getImaginary();


					
					//OTF[n][m] = OTF0_Complex[n][m].add(OTFs[n][m].multiply(0.5));
				}
			}
			//OTF[n][m] = OTF0_Complex[n][m].plus(OTFs[n][m].multiply(0.5));
			
			return new ComplexImagePlus(new ImagePlus(null,new FloatProcessor(OTF_real)),new ImagePlus(null,new FloatProcessor(OTF_imag)));
		}
		

	}
	
	public float[][] TriangleMask(int n,float r0,int x0,int y0)
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
	
	

	public ImagePlus kspace_pad(ImagePlus input, int out_Width, int out_Height){
		
		ZProjector projector = new ZProjector(input);
		projector.setMethod(projector.SUM_METHOD);
		projector.doProjection();
		ImagePlus sum = projector.getProjection();

		ConcurrencyUtils.setNumberOfThreads(16);
		
			Transformer transformer = new FloatTransformer(sum.getStack());
			transformer.fft();
			
			//ImagePlus temp = transformer.toImagePlus(SpectrumType.FREQUENCY_SPECTRUM, FourierDomainOriginType.AT_CENTER);	
			ImagePlus real = transformer.toImagePlus(SpectrumType.REAL_PART, FourierDomainOriginType.AT_CENTER);
			ImagePlus imag = transformer.toImagePlus(SpectrumType.IMAG_PART, FourierDomainOriginType.AT_CENTER);
			
			//temp.show();
			ImagePlus outputRl = IJ.createImage("WideField", "32-bit black", out_Width, out_Height, 1);	
			ImagePlus outputIm = IJ.createImage("WideField", "32-bit black", out_Width, out_Height, 1);
			
			outputRl.setTitle("Real");
			outputIm.setTitle("Imag");
			//outputRl.show();
			//outputIm.show();
			
			(outputRl.getProcessor()).insert(real.getProcessor(), out_Width/2, out_Height/2);
			(outputIm.getProcessor()).insert(imag.getProcessor(), out_Width/2, out_Height/2);
			
			//output.show();
			transformer = new FloatTransformer(outputRl.getStack(),outputIm.getStack());			
			transformer.ifft(true);
			
			
			return transformer.toImagePlus(SpectrumType.FREQUENCY_SPECTRUM, FourierDomainOriginType.AT_ZERO);
}
	

	
	public static ImagePlus pad(ImagePlus img,int padLeft,int padRight,int padTop,int padBottom)
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

	
    public Complex[][] ComplexArray(ComplexImagePlus img) {
    	
    	int width = img.getReal().getWidth();
    	
    	Complex[][] output = new Complex[width][width];
    	
    	float[][] real = img.getReal().getProcessor().getFloatArray();
    	float[][] imag = img.getImag().getProcessor().getFloatArray();
    	
 
    	for(int i = 0; i < width ; i++ )
    	{
    		for(int j = 0; j < width ; j++ )
    		{
    			output[i][j] = new Complex(real[i][j],imag[i][j]) ;
    		}
    	}

    	return output;
    }
    
    public Complex[][] ComplexArray(ImagePlus real_in,ImagePlus imag_in) {
    	
    	int width = real_in.getWidth();
    	int height = real_in.getHeight();
    	
    	Complex[][] output = new Complex[width][width];
    	
    	float[][] real = real_in.getProcessor().getFloatArray();
    	float[][] imag = imag_in.getProcessor().getFloatArray();
    	
 
    	for(int i = 0; i < width ; i++ )
    	{
    		for(int j = 0; j < height ; j++ )
    		{
    			output[i][j] = new Complex(real[i][j],imag[i][j]) ;
    		}
    	}

    	return output;
    	
    }
    
    

	@Override
	protected void compute() {
		// TODO Auto-generated method stub
		
	}


}

