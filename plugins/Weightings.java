

import ij.ImagePlus;
import ij.process.FloatProcessor;

import org.apache.commons.math3.complex.Complex;

public class Weightings {

	int width;
	int height;
	int Orientations;
	Complex ck;
	
	double fringeFrequency[] = {136.852546526160,135.741042289866,135.439705087354};
	double fringeAngle[] = {-0.513593210927861,0.526789085380402,-1.55759150441705};
	
	ComplexImagePlus OTFp[] = new ComplexImagePlus[3];
	ComplexImagePlus OTFn[] = new ComplexImagePlus[3];
	
	
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
		this.Orientations = 3;
	}
	
	Complex Complex_Weightings(int orientation, ComplexImagePlus[] k0_complex, ComplexImagePlus[] kp1_complex) {
		

		//OTF0.getReal().show();

		
		
		//float[][][] OTFP_float = new float[3][1024][1024];
		//float[][] OTF0_float = OTF0.getReal().getProcessor().getFloatArray();
		
		if (orientation == 0 )
				this.OTF0_Complex = ComplexArray(new ComplexImagePlus(OTF0));

		for (int i = 0; i < Orientations; i++)
			{
			
			double otf_x_shift  = -Math.round(fringeFrequency[i]*Math.cos(fringeAngle[i]));				
			double otf_y_shift  =  -Math.round(fringeFrequency[i]*Math.sin(fringeAngle[i]));			
			
			OTFp[i] = OTF0.translate(otf_x_shift, otf_y_shift);
			
			OTFn[i] = OTFp[i].rotate(180);
			
			//OTFP_float[i] = OTFP[i].getReal().getProcessor().getFloatArray();
			
			}

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
				
			
				Complex a = k0[x][y].multiply(OTFp_Complex[x][y]);
				

				//out[x][y] = (double) a.getReal();
					//System.out.println(String.valueOf(a));


				Complex b = kp1[x][y].multiply(OTF0_Complex[x][y]); 
				
				Complex dum1_temp = (a.conjugate()).multiply(b);
				Complex dum2_temp = new Complex( ( a.getReal() * a.getReal() ) +   ( a.getImaginary()* a.getImaginary() ) );
				
				sum1 = sum1.add(dum1_temp);
				sum2 = sum2.add(dum2_temp);
				
				OTFs[x][y] = OTFs[x][y].add(OTFp_Complex[x][y].add(OTFn_Complex[x][y].conjugate()));

			}
		}

		
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
		System.out.println(String.valueOf(ck));
		//return null;

		//FieldMatrix<Complex> OTFP_Matrix = new Array2DRowFieldMatrix<Complex>(OTFP_Complex); 
		//FieldMatrix<Complex> OTF0_Matrix = new Array2DRowFieldMatrix<Complex>(OTF0_Complex); 

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
    
}