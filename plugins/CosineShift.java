

import ij.ImagePlus;
import ij.process.FloatProcessor;


public class CosineShift {
	float[][][] shift_cosine;
	double fringe_angle;
	double fringe_r;
	float[][] plane_equation;
	int width;
	
	public CosineShift(double fringeAngle, double fringeFrequency,int width)
	{
		this.fringe_angle = fringeAngle;
		this.fringe_r = fringeFrequency;
		this.width = width;
		int x = 0;
		int y = 0;
		//fringeAngle = -0.1557;
				
		this.shift_cosine= new float[2][width][width];
		this.plane_equation = new float[width][width];
		for(int X = 0; X < width ; X++)
		{
			for(int Y = 0; Y < width ; Y++)
			{
				x  = X + 1;
				y = Y + 1;
				
				plane_equation[X][Y] = (float) ((x*Math.cos(fringe_angle)) + (y*Math.sin(fringe_angle)));	
				
				//shift_cosine[0][x][y] = 1;
				//shift_cosine[1][x][y] = 1;
				
				
				//shift_cosine[0][X][Y] = (float) Math.cos( 2 * Math.PI * fringe_r * plane_equation[X][Y] / (width));
				
				shift_cosine[0][X][Y] = (float) Math.cos((2*Math.PI*fringe_r *plane_equation[X][Y])/(width));  ///THIS LINE IS DANGEROUS
				
				shift_cosine[1][X][Y] = (float) ((-1)* Math.sin((2*Math.PI*fringe_r * plane_equation[X][Y] )/(width)));	///THIS LINE IS DANGEROUS
				
				//plane_equation =1;	

			}
		}	
		

}
	
	public ComplexImagePlus get(){
		return new ComplexImagePlus(new ImagePlus(null,new FloatProcessor(shift_cosine[0])),new ImagePlus(null,new FloatProcessor(shift_cosine[1])));
	}

	
	public ImagePlus getplane(){
		return new ImagePlus(null,new FloatProcessor(plane_equation));
	}
}