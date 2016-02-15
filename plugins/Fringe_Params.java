
import ij.*;
import ij.measure.Measurements;
import ij.measure.ResultsTable;
import ij.process.*;
import ij.gui.*;

import java.awt.*;
import java.text.DecimalFormat;

import edu.emory.mathcs.parallelfftj.FloatTransformer;
import edu.emory.mathcs.parallelfftj.FourierDomainOriginType;
import edu.emory.mathcs.parallelfftj.SpectrumType;
import edu.emory.mathcs.utils.ConcurrencyUtils;
import ij.plugin.ImageCalculator;
import ij.plugin.PlugIn;
import ij.plugin.SubstackMaker;
import ij.plugin.filter.*;

public class Fringe_Params implements PlugInFilter { //(A - mean(A(:))) in matlab

	ImagePlus input = null;
	int Orientations = 3;
	int Phases = 3;
	
	double[] fringeFrequency = new double[]{136.852546526160,135.741042289866,135.439705087354};
	double[] fringeAngle = new double[]{-0.513593210927861,0.526789085380402,-1.55759150441705};
	
	/*Fringe_Params(ImagePlus input)
	{
		//TODO Guess orientations and phases
		this.input = input;
	}
	
	Fringe_Params(ImagePlus input,int Orientations, int Phases)
	{
		this.input = input;
		this.Orientations = Orientations;
		this.Phases = Phases;
		//TODO Guess orientations and phases
	}*/
	

	
	ImagePlus ImageSubMean(ImagePlus A){
		Analyzer a = new Analyzer(A);
		//a.setResultsTable(new ResultsTable()); //fails for some reason
		Analyzer.setMeasurement(Measurements.MEAN, true);
		a.measure(); 
		ResultsTable rt = Analyzer.getResultsTable();
		//a.displayResults();
		int column = rt.getColumnIndex("Mean");
		double aMean = rt.getValueAsDouble(rt.getColumnIndex("Mean"), rt.getCounter()-1);	//most result result
		//System.out.println("Row\t " + rt.getCounter()+"\tColumn\t" + column + "\tMean\t" + aMean);
		FloatProcessor A_sub_mean = ((ImageProcessor) A.getProcessor().clone()).convertToFloatProcessor(); //Duplicate processor, operate on it, then pass it to the return image.
		A_sub_mean.subtract(aMean);		
		//A.getProcessor().subtract(aMean);
		ImagePlus ASubMean = new ImagePlus("ASum",A_sub_mean);
		rt.deleteRow(0); 
		return ASubMean;   //This function attempts to manipulate ImageJ's inbuilt functions as much as possible, in a bid to gain speed over linear looped operations inbuilt to java.
							// Assuming imagej doesn't do that currently.
	}
	
	double correlationCoeff(ImagePlus A,ImagePlus B){
		double coeff = 0;
		
		ImagePlus AsubMeanA = ImageSubMean(A);
		ImagePlus BsubMeanB = ImageSubMean(B);
		//AsubMeanA.show();
		
		//FloatProcessor d1 = ((ImageProcessor) AsubMeanA.getProcessor().clone()).convertToFloatProcessor();
		//d1.abs();
		
		//FloatProcessor d2 = ((ImageProcessor) BsubMeanB.getProcessor().clone()).convertToFloatProcessor();
		//d2.abs();
		
		
		ImagePlus d1 = new ImageCalculator().run("Multiply create",AsubMeanA,AsubMeanA);
		ImagePlus d2 = new ImageCalculator().run("Multiply create",BsubMeanB,BsubMeanB);
		
		ImagePlus n = new ImageCalculator().run("Multiply create",AsubMeanA,BsubMeanB);
		
		//n.show();
		
		Analyzer d1_int = new Analyzer(d1);
		Analyzer d2_int = new Analyzer(d2);
		Analyzer n_int = new Analyzer(n);
		
		Analyzer.setMeasurement(Measurements.INTEGRATED_DENSITY, true);
		ResultsTable rt = Analyzer.getResultsTable();
		
		d1_int.measure(); 		
		double d1_sum = rt.getValueAsDouble(rt.getColumnIndex("IntDen"), rt.getCounter()-1);
		//System.out.println(" d1_sum " + d1_sum);
		d2_int.measure(); 		
		double d2_sum = rt.getValueAsDouble(rt.getColumnIndex("IntDen"), rt.getCounter()-1);
		//System.out.println(" d2_sum " + d2_sum);
		n_int.measure(); 		
		double n_sum = rt.getValueAsDouble(rt.getColumnIndex("IntDen"), rt.getCounter()-1);
		//System.out.println(" n_sum " + n_sum);
		
		
		coeff = n_sum / Math.sqrt(d1_sum*d2_sum);
		
		//System.out.println(coeff);
		
		//new ImagePlus("null", A_clone).show();
		
		
		//ImagePlus d1 = new ImageCalculator().run("Multiply create",AsubMeanA,AsubMeanA);
		//ImagePlus d2 = new ImageCalculator().run("Multiply create",BsubMeanB,BsubMeanB);
		
		
		
		
		return coeff;
		
	}


	public double[] process(ImagePlus imp) {
		if (imp==null){
			imp = IJ.openImage("C:\\Users\\Craggles\\Desktop\\junk\\substack.tif");
			imp.show();
		}
		ImageStack tD_FFTStack = new ImageStack(imp.getWidth(),imp.getHeight());		
		int n = 1;
		ImagePlus[] inputArray =  new ImagePlus[Phases];
		for (int Phase = 0; Phase < Phases; Phase++)
		{			
			inputArray[Phase] = new ImagePlus(null,imp.getStack().getProcessor(n));
			n++;
			IJ.showStatus("Tukey window to remove FFT artefacts");
			inputArray[Phase] = new Tukey_2D().tukeyProcess(inputArray[Phase],0.1);
			tD_FFTStack.addSlice(inputArray[Phase].getProcessor());												
		}
		
		FloatTransformer transformer = new FloatTransformer(tD_FFTStack);
		transformer.fft();
		
		
		ComplexImagePlus k0_complex = new ComplexImagePlus(
				new ImagePlus(null,((transformer.toImagePlus(SpectrumType.REAL_PART, FourierDomainOriginType.AT_CENTER)).getStack()).getProcessor(2))
			   ,new ImagePlus(null,((transformer.toImagePlus(SpectrumType.IMAG_PART, FourierDomainOriginType.AT_CENTER)).getStack()).getProcessor(2)));	
		
		ComplexImagePlus kp1_complex = new ComplexImagePlus(
				new ImagePlus(null,((transformer.toImagePlus(SpectrumType.REAL_PART, FourierDomainOriginType.AT_CENTER)).getStack()).getProcessor(3))
			   ,new ImagePlus(null,((transformer.toImagePlus(SpectrumType.IMAG_PART, FourierDomainOriginType.AT_CENTER)).getStack()).getProcessor(3)));

		
		
		ImagePlus kp1_complex_mod = new ImagePlus(null,((transformer.toImagePlus(SpectrumType.FREQUENCY_SPECTRUM, FourierDomainOriginType.AT_CENTER))
				.getStack()).getProcessor(3));
		
		ImagePlus k0_complex_log = new ImagePlus(null,((transformer.toImagePlus(SpectrumType.FREQUENCY_SPECTRUM_LOG, FourierDomainOriginType.AT_CENTER))
				.getStack()).getProcessor(2));
		
		IJ.setAutoThreshold(k0_complex_log, "Minimum dark");   //Threshold image
		IJ.run(k0_complex_log, "Make Binary", "");				//Threshold binary
		IJ.run(k0_complex_log, "Invert", "");					//invert for mutliply avoids infins and nans
		//k0_complex_log.show();
		//input.show();
		//correlationCoeff(input,input);
		//kp1_complex_mod.show();//new ImageCalculator().run("Subtract create",kp1_complex_mod,k0_complex.abs())
		ImagePlus masked = new ImageCalculator().run("Multiply create", kp1_complex_mod,k0_complex_log);
		//masked.show();
		double maxXY[] = maximaSearch(masked); //TODO Subtract K0 to mask 
		//double angle = null;
		double fringeFrequency = Math.hypot(imp.getWidth()/2 - maxXY[0],imp.getHeight()/2 - maxXY[1]);
		//System.out.println("fringeFrequency" + fringeFrequency);
		
		//double fringeAngle = Math.atan2( maxXY[0] -input.getWidth()/2,-(input.getHeight()/2 - maxXY[1]));
		double fringeAngle = -Math.atan2(imp.getHeight()/2-maxXY[1],maxXY[0] -imp.getWidth()/2 ); //Need to check this is producing correct sign.
		//System.out.println("fringeAngle" + fringeAngle);
		
		
		
	/*	//ComplexImagePlus compInput = new ComplexImagePlus(input,input);
		//kp1_complex.abs().show();
		ComplexImagePlus shift_cosine = (new CosineShift(fringeAngle,fringeFrequency,input.getWidth())).get();
		kp1_complex = kp1_complex.ifft(FourierDomainOriginType.AT_ZERO);
		//shift_cosine.abs().show();
		kp1_complex = kp1_complex.times(shift_cosine);
		kp1_complex = kp1_complex.fft(FourierDomainOriginType.AT_ZERO);
		//kp1_complex.mod().show();*/
		
		return robust_slow_search(fringeAngle,fringeFrequency,kp1_complex,k0_complex);
	}
	
	double[] robust_slow_search(double fringeAngle,double fringeFrequency,ComplexImagePlus kp1_complex,ComplexImagePlus k0_complex){
		int n = 10;
		int iMax = 0;
		int jMax = 0;
		double cc;
		double currentMax = 0;
		double angle = fringeAngle;
		double frequency = fringeFrequency;
		DecimalFormat df = new DecimalFormat("#0.00000");
		System.out.println("Registration\t" + "Guess" + "\tFringe Angle\t" + df.format(fringeAngle) + "\tFringe Freq \t" + df.format(fringeFrequency));
		ComplexImagePlus kp1_shift = new ComplexImagePlus(kp1_complex); //dupe
		
		ConcurrencyUtils.setNumberOfThreads(ConcurrencyUtils.getNumberOfProcessors()-2);
		
		for ( int nReg = 0 ;nReg < 6; nReg++)
		{
			System.out.println("Registration\t" + nReg + "\tFringe Angle\t" + df.format(fringeAngle) + "\tFringe Freq \t" + df.format(fringeFrequency));
			IJ.showStatus("Registration  " + nReg + " Fringe Angle " + df.format(fringeAngle) + " Fringe Freq " + df.format(fringeFrequency));
			//nReg = 2;
			//double power = 2^nReg;
			
			//double power = Math.pow(2, nReg);
			//System.out.println(power);
			double an = 0.04/( Math.pow(2, nReg));			
			double rn = 4/( Math.pow(2, nReg));
			
			for (int i =0 ; i < n ; i++)
			{
				for (int j =0 ;j < n ; j++)
				{
					
					angle = fringeAngle - an/2 + (((double) i+1)*an)/n ;				
					frequency = fringeFrequency - rn/2 + (((double) j+1)*rn)/n;
					
					
					ComplexImagePlus shift_cosine = (new CosineShift(angle,frequency,input.getWidth())).get();
					kp1_shift = kp1_complex.ifft(FourierDomainOriginType.AT_ZERO);
					//shift_cosine.abs().show();
					kp1_shift = kp1_shift.times(shift_cosine);
					kp1_shift = kp1_shift.fft(FourierDomainOriginType.AT_ZERO);
					
				//	kp1_shift.abs()
					
					cc = correlationCoeff(kp1_shift.mod(),k0_complex.mod());
					
					//System.out.println("CC i " + i +" j " +j + " arr: " + ccArray[i][j]);			
					
					if (cc > currentMax)
					{
						currentMax = cc;
						iMax = i;
						jMax = j;
					}
					
					
				}
				
				
			}
			//System.out.println("angle " + angle + " frequency " + frequency + "   nReg " + nReg);
			fringeAngle = fringeAngle - an/2 + (((double) iMax+1)*an)/n;			
			fringeFrequency = fringeFrequency - rn/2 + (((double) jMax+1)*rn)/n;
			//System.out.println("Registration  " + nReg + " Fringe Angle " + fringeAngle + " Fringe Freq " + fringeFrequency);
			currentMax = 0;
			iMax = 0;
			jMax = 0;
			
		}
		
		return new double[]{fringeAngle,fringeFrequency};
			
	}
	
	double[] maximaSearch(ImagePlus in){
		
		MaximumFinder a = new MaximumFinder();	
		ImageStatistics ab = in.getStatistics();		
		ByteProcessor MaximumProcessor = new MaximumFinder().findMaxima(in.getProcessor(), in.getStatistics().max, ImageProcessor.NO_THRESHOLD ,MaximumFinder.SINGLE_POINTS,false,false);	
		//System.out.println("max x" + MaximumProcessor.getStatistics().xCenterOfMass);
		//System.out.println("max y" + MaximumProcessor.getStatistics().yCenterOfMass);
		//new ImagePlus("Max",MaximumProcessor).show();
		return new double[] { MaximumProcessor.getStatistics().xCenterOfMass,MaximumProcessor.getStatistics().yCenterOfMass};
		//return double[]{xCenterOfMass,xCenterOfMass};
		
	}
	
	public double[] fringeFrequency() {
		// TODO Auto-generated method stub
		return fringeFrequency;
	}

	public double[] fringeAngle() {
		// TODO Auto-generated method stub
		return fringeAngle;
	}



	@Override
	public void run(ImageProcessor arg0) {
		double[] outputArray = process(input);
		
		
	}

	public int setup(String arg, ImagePlus imp) {
		this.input = imp;
		return DOES_ALL;
	}
	
	public int calcFringeParams(String arg, ImagePlus imp,int Orientations, int Phases, boolean threaded) throws InterruptedException {
		this.input = imp;
		this.Orientations = Orientations;
		this.Phases = Phases;
		Thread threads[] = new Thread[Orientations];

		
		for (int i = 0; i < Orientations ; i++)
		{
			final int num = i;
			final int ThreadPhases = Phases;
			threads[i] = new Thread(new Runnable() {
				public void run() {
			
			int from = num*ThreadPhases+1;
			int to = num*ThreadPhases+(ThreadPhases);			
			ImagePlus input_sub = new SubstackMaker().makeSubstack(input, from + "-" + to);	
			//input_sub.show();
			double[] temp = process(input_sub);
			fringeFrequency[num] = temp[1];
			fringeAngle[num] = temp[0];
				}
			});
			
			threads[i].start();
			if (!threaded)
				{threads[i].join();}
		}
		
		for (int i = 0; i < Orientations ; i++)
		{		
			threads[i].join();
		}
		
		return DOES_ALL;
	}



}






