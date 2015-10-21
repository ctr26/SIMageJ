/*
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2015 Tobias Pietzsch, Stephan Preibisch, Barry DeZonia,
 * Stephan Saalfeld, Curtis Rueden, Albert Cardona, Christian Dietz, Jean-Yves
 * Tinevez, Johannes Schindelin, Jonathan Hale, Lee Kamentsky, Larry Lindsey, Mark
 * Hiner, Michael Zinsmaier, Martin Horn, Grant Harris, Aivar Grislis, John
 * Bogovic, Steffen Jaensch, Stefan Helfrich, Jan Funke, Nick Perry, Mark Longair,
 * Melissa Linkert and Dimiter Prodanov.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
import java.io.InputStream;

import ij.ImageJ;
import ij.ImagePlus;
import ij.io.Opener;
import io.scif.img.ImgIOException;
import net.imglib2.exception.IncompatibleTypeException;

/**
 * Perform a gaussian convolution using fourier convolution
 */
public class _GUI
{

	public _GUI() throws ImgIOException, IncompatibleTypeException
	{
		
		//final Img< FloatType > image = ImagePlusAdapter.wrap((new Opener().openImage( (new File( "Segment.tif" )).getAbsolutePath() )));
		//final Img< FloatType > kernel = ImagePlusAdapter.wrap((new Opener().openImage( (new File( "kernelRing.tif" )).getAbsolutePath() )));
		
       // InputStream is = getClass().getResourceAsStream("Segment.tif");
		
        //Opener opener = new Opener();
        //input = ;
        //ImagePlus input = opener.openTiff(is, "Input");
		
		//final Img< FloatType > image2 = ImagePlusAdapter.wrap(input);
		
		InputStream is = getClass().getResourceAsStream("/Users/Craggles/Desktop/Test.tif");
		
        Opener opener = new Opener();
        ImagePlus input = opener.openTiff(is, "Input");
        input.show();

		//ImageJFunctions.show(image2);
		//ImageJFunctions.show(kernel);


		/*long startTime = System.nanoTime();

		//new InverseFFT(image, new FFTImg());
		//new FFTConvolution<FloatType>( image, kernel ).convolve();
		//ImageJFunctions.show( image )
			//.setTitle( "convolution" );
		long endTime = System.nanoTime();
		long duration = ((endTime - startTime)/1000000);  //divide by 1000000 to get milliseconds.
		*/
		//System.out.println(duration);
		//output = new Img<ComplexType>
		
		//new ImgFactory
		
		//kernel.

		//ImgFactory<ComplexFloatType> factory = 
		//img = new ImagePlusImgFactory<FloatType>.create(new long[]{100, 200}, new FloatType());
		//ImagePlusImgs
		//new ImagePlusImg(1, 255, 1, 1024, 2014, new Fraction());
		 //new ImagePlus
		
		
		/*ImagePlusImg< UnsignedByteType, ?> image2
        = new ImagePlusImgFactory< UnsignedByteType >().create(
            new long[] { 1024, 1024, 3 }, new UnsignedByteType() );
		ImageJFunctions.show(image2);
		
		new FFT();//<FloatType,ComplexType>;
		Img<ComplexFloatType> fft = FFT.realToComplex(image,image2.factory().imgFactory(new ComplexFloatType()));
		
		//fft = kernel.factory();
		
		ImageJFunctions.show(fft);*/

		
	}

	/**
	 * Computes the sum of all pixels in an iterable using RealSum
	 *
	 * @param iterable - the image data
	 * @return - the sum of values
	 */
	/*public static < T extends RealType< T > > double sumImage( final Iterable< T > iterable )
	{
		final RealSum sum = new RealSum();

		for ( final T type : iterable )
			sum.add( type.getRealDouble() );

		return sum.getSum();
	}*/

	/**
	 * Norms all image values so that their sum is 1
	 *
	 * @param iterable - the image data
	 */
	/*public static void norm( final Iterable< FloatType > iterable )
	{
		final double sum = sumImage( iterable );

		for ( final FloatType type : iterable )
			type.setReal( type.get() / sum );
	}*/

	public static void main( final String[] args ) throws ImgIOException, IncompatibleTypeException
	{
		// open an ImageJ window
		new ImageJ();

		// run the example
		new _GUI();
	}
}