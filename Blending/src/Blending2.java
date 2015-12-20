import com.jhlabs.image.*;

import java.awt.*;
import java.awt.image.*;

import javax.imageio.*;

import java.io.*;
import java.util.*;

public class Blending2 {
	
	private static class FColor
	{
		float red;
		float green;
		float blue;
		
		public FColor()
		{
			this.red = 0;
			this.green = 0;
			this.blue = 0;
		}
		
		public FColor(float red, float green, float blue)
		{
			this.red = red;
			this.green = green;
			this.blue = blue;
		}
		
		public FColor(int rgb)
		{
			Color c = new Color(rgb);
			red = c.getRed();
			green = c.getGreen();
			blue = c.getBlue();
		}
		public Color toColor()
		{
			return new Color(PixelUtils.clamp((int)(red+1)),PixelUtils.clamp((int)(green+1)),PixelUtils.clamp((int)(blue+1)));
		}
		
		public int toInt()
		{
			return new Color(PixelUtils.clamp((int)(red+1)),PixelUtils.clamp((int)(green+1)),PixelUtils.clamp((int)(blue+1))).getRGB();
		}

		public float getRed()
		{
			return red;
		}
		
		public float getGreen()
		{
			return green;
		}
		
		public float getBlue()
		{
			return blue;
		}
	
	}
	
	private static ArrayList<FColor[]> laplacians = new ArrayList<FColor[]>();
	private static ArrayList<FColor[]> laplacians2 = new ArrayList<FColor[]>();
	private static ArrayList<FColor[]> gaussians = new ArrayList<FColor[]>();
	private static ArrayList<FColor[]> combined = new ArrayList<FColor[]>();
	
	public static void printUsage() {
		System.out.println("Usage: java Blending -input <filename> [options]");
		System.out.println("-output <filename>");
		System.out.println("-brightness <float>");
		System.out.println("-edgedetect");
		System.out.println("-blur <float>");
		System.exit(1);
	}

	public static void main(String[] args){
		
		int i = 0;
		BufferedImage src1 = null, dst1 = null;
		BufferedImage src2 = null, dst2 = null;
		BufferedImage src3 = null, dst3 = null;
		BufferedImage tmp = null;	// used for swapping src and dst buffer
		int width, height;			// image width, height

		String arg;
		String outputfilename = "output.png";		// default output filename

		while (i < args.length && args[i].startsWith("-")) {
			arg = args[i++];

			if (arg.equals("-input1")) {

				String inputfile1 = args[i++];
				outputfilename = inputfile1;
				try {
					src1 = ImageIO.read(new File(inputfile1));
				} catch (IOException e) {
					System.out.println("no input");
				}
				width = src1.getWidth();
				height = src1.getHeight();
				dst1 = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
				continue;

			}else if(arg.equals("-input2")){
				String inputfile2 = args[i++];
				try {
					src2 = ImageIO.read(new File(inputfile2));
				} catch (IOException e) {
					System.out.println("no input2");
				}
				width = src2.getWidth();
				height = src2.getHeight();
				dst2 = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
				continue;
			} else if(arg.equals("-input3")){
				String inputfile3 = args[i++];
				try {
					src3 = ImageIO.read(new File(inputfile3));
				} catch (IOException e) {
					System.out.println("no input3");
				}
				width = src3.getWidth();
				height = src3.getHeight();
				dst3 = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
				continue;
			}
		}
		TextureBomb(src1, src2, src3, 518, 56, dst1, outputfilename);
		//TextureBomb(src1, src2, 34, 45, dst);
		//		tmp = src1; src1 = dst; dst = tmp;
		//		if (i != args.length) {
		//			System.out.println("there are unused arguments");
		//		}
		//		// write the output image to disk file
		//		File outfile = new File(outputfilename);
		//		try {
		//			ImageIO.write(src1, "png", outfile);
		//		} catch(IOException e) {
		//		}

	}

	public static void TextureBomb(BufferedImage src1, BufferedImage src2, BufferedImage src3, int x, int y, BufferedImage dst, String outputfilename){
		//images must me same size
		int width = src1.getWidth();
		int height = src1.getHeight();
		int[] p1 = new int[width*height];//pixels for src1
		src1.getRGB(0, 0, width, height, p1, 0, width);
		
		int[] p2 = new int[width*height];//pixels for src2
		src2.getRGB(0, 0, width, height, p2, 0, width);
		
		int[] p3 = new int[width*height];//pixels for src2
		src3.getRGB(0, 0, width, height, p3, 0, width);
		FColor[] p1c = new FColor[p1.length];
		FColor[] p2c = new FColor[p2.length];
		FColor[] p3c = new FColor[p3.length];
		for(int i = 0; i < p1.length; i++)
		{
			p1c[i] = new FColor(p1[i]);
			p2c[i] = new FColor(p2[i]);
			p3c[i] = new FColor(p3[i]);
		}
		
		FColor[] pOutput = new FColor[width*height];
		FColor[] pOutput2 = new FColor[width*height];
		FColor[] pOutput3 = new FColor[width*height];
		final int iter = 100;
		for(int j = 0; j < iter; j++)
		{
			for(int i = 0; i < p1c.length; i++)
			{
				pOutput[i] = getGaussian(p1c,i,width);
				pOutput2[i] = getGaussian(p2c,i,width);
				pOutput3[i] = getGaussian(p3c,i,width);
			}
			FColor[] pL = subtract(p1c,pOutput);
			FColor[] pL2 = subtract(p2c,pOutput2);
			laplacians.add(pL.clone());
			laplacians2.add(pL2.clone());
			gaussians.add(pOutput3.clone());
			p1c = (FColor[])pOutput.clone();
			p2c = (FColor[])pOutput2.clone();
			p3c = (FColor[])pOutput3.clone();
			//convert back
			for(int i = 0; i < p1.length; i++)
			{
				p1[i] = p1c[i].toInt();
				p2[i] = p2c[i].toInt();
				p3[i] = p3c[i].toInt();
			}
			//
//			dst.setRGB(0, 0, width, height, p3, 0, width);
//			File outfile = new File(outputfilename.substring(0, outputfilename.length()-4) + "_g" + (j+1) + outputfilename.substring(outputfilename.length()-4,outputfilename.length()));
//			try 
//			{
//				ImageIO.write(dst, "png", outfile);
//			} 
//			catch(IOException e) 
//			{
//			}
			//
		}
		FColor[] total = new FColor[width*height];//probably need to initialize everything
		for(int i = 0; i < total.length; i++)
		{
			total[i] = new FColor();
		}
		int[] totali = new int[width*height];
		putTogether();
		for(FColor[] i : combined)
		{
			//System.out.println(combined.size());
			total = add(total,i);
		}
		for(int i = 0; i < total.length; i++)
		{
			totali[i] = total[i].toInt();
		}
		dst.setRGB(0, 0, width, height, totali, 0, width);
		//dst.setRGB(0, 0, width, height, totali, 0, width);
		File outfile = new File(outputfilename.substring(0, outputfilename.length()-4) + "total" + outputfilename.substring(outputfilename.length()-4,outputfilename.length()));
		try 
		{
			ImageIO.write(dst, "png", outfile);
		} 
		catch(IOException e) 
		{
		}
	}
	
	private static FColor getValue(FColor[] array, int index)
	{
		try
		{
			return array[index];
		}
		catch(Exception e)
		{
			return new FColor(0,0,0);
		}
	}

	private static int getValue(int[][] array, int index, int index2)
	{
		try
		{
			return array[index][index2];
		}
		catch(Exception e)
		{
			return 0;
		}
	}

	private static FColor getGaussian(FColor[] array, int index, int width)
	{
		int i = index % width;
		int j = index / width;
		float[] argb = new float [4];
		float[][] gaussian = new float[][]{{1,4,7,4,1},{4,16,26,16,4},{7,26,41,26,7},{4,16,26,16,4},{1,4,7,4,1}};
		for(int x = 0; x < 5; x++)
			for(int y = 0; y < 5; y++)
				gaussian[x][y] /= 273;
		
		for(int x = -2; x <= 2; x++)
		{
			for(int y = -2; y <= 2; y++)
			{
				argb[1] += getValue(array,twodtooned(i + x, j + y, width)).getRed()*gaussian[x+2][y+2];
			}
		}
		
		for(int x = -2; x <= 2; x++)
		{
			for(int y = -2; y <= 2; y++)
			{
				argb[2] += getValue(array,twodtooned(i + x, j + y, width)).getGreen()*gaussian[x+2][y+2];
			}
		}
		
		for(int x = -2; x <= 2; x++)
		{
			for(int y = -2; y <= 2; y++)
			{
				argb[3] += getValue(array,twodtooned(i + x, j + y, width)).getBlue()*gaussian[x+2][y+2];
			}
		}
//		argb[0] = 	(int) (new Color(getValue(array,twodtooned(i - 1, j - 1, width))).getAlpha() / 16.0 + 
//				new Color(getValue(array,twodtooned(i + 1, j + 1, width))).getAlpha() / 16.0 +
//				new Color(getValue(array,twodtooned(i + 1, j - 1, width))).getAlpha() / 16.0 +
//				new Color(getValue(array,twodtooned(i - 1, j + 1, width))).getAlpha() / 16.0 +
//				new Color(getValue(array,twodtooned(i - 1, j, width))).getAlpha() / 8.0 + 
//				new Color(getValue(array,twodtooned(i + 1, j, width))).getAlpha() / 8.0+
//				new Color(getValue(array,twodtooned(i, j - 1, width))).getAlpha() / 8.0+
//				new Color(getValue(array,twodtooned(i, j + 1, width))).getAlpha()  / 8.0+
//				new Color(getValue(array,twodtooned(i, j, width))).getAlpha() / 4.0);
//
//		argb[1] = 	(int) (new Color(getValue(array,twodtooned(i - 1, j - 1, width))).getRed() / 16.0 + 
//				new Color(getValue(array,twodtooned(i + 1, j + 1, width))).getRed() / 16.0 +
//				new Color(getValue(array,twodtooned(i + 1, j - 1, width))).getRed() / 16.0 +
//				new Color(getValue(array,twodtooned(i - 1, j + 1, width))).getRed() / 16.0 +
//				new Color(getValue(array,twodtooned(i - 1, j, width))).getRed() / 8.0+ 
//				new Color(getValue(array,twodtooned(i + 1, j, width))).getRed() / 8.0+
//				new Color(getValue(array,twodtooned(i, j - 1, width))).getRed() / 8.0+
//				new Color(getValue(array,twodtooned(i, j + 1, width))).getRed() / 8.0+
//				new Color(getValue(array,twodtooned(i, j, width))).getRed() / 4.0);
//
//		argb[2] = 	(int) (new Color(getValue(array,twodtooned(i - 1, j - 1, width))).getGreen() / 16.0 + 
//				new Color(getValue(array,twodtooned(i + 1, j + 1, width))).getGreen() / 16.0 +
//				new Color(getValue(array,twodtooned(i + 1, j - 1, width))).getGreen() / 16.0 +
//				new Color(getValue(array,twodtooned(i - 1, j + 1, width))).getGreen() / 16.0 +
//				new Color(getValue(array,twodtooned(i - 1, j, width))).getGreen() / 8.0+ 
//				new Color(getValue(array,twodtooned(i + 1, j, width))).getGreen() / 8.0+
//				new Color(getValue(array,twodtooned(i, j - 1, width))).getGreen() / 8.0+
//				new Color(getValue(array,twodtooned(i, j + 1, width))).getGreen() / 8.0+
//				new Color(getValue(array,twodtooned(i, j, width))).getGreen() / 4.0);
//
//		argb[3] = 	(int) (new Color(getValue(array,twodtooned(i - 1, j - 1, width))).getBlue() / 16.0 + 
//				new Color(getValue(array,twodtooned(i + 1, j + 1, width))).getBlue() / 16.0 +
//				new Color(getValue(array,twodtooned(i + 1, j - 1, width))).getBlue() / 16.0 +
//				new Color(getValue(array,twodtooned(i - 1, j + 1, width))).getBlue() / 16.0 +
//				new Color(getValue(array,twodtooned(i - 1, j, width))).getBlue() / 8.0 + 
//				new Color(getValue(array,twodtooned(i + 1, j, width))).getBlue() / 8.0 +
//				new Color(getValue(array,twodtooned(i, j - 1, width))).getBlue() / 8.0 +
//				new Color(getValue(array,twodtooned(i, j + 1, width))).getBlue() / 8.0 +
//				new Color(getValue(array,twodtooned(i, j, width))).getBlue() / 4.0);

		//		argb[0] = new Color(getValue(array,twodtooned(i, j, width))).getAlpha();
		//		
		//		argb[1] = new Color(getValue(array,twodtooned(i, j, width))).getRed();
		//		
		//		argb[2] = new Color(getValue(array,twodtooned(i, j, width))).getGreen();
		//		
		//		argb[3] = new Color(getValue(array,twodtooned(i, j, width))).getBlue();

		//return (new Color(PixelUtils.clamp((int) argb[1]), PixelUtils.clamp((int) argb[2]), PixelUtils.clamp((int) argb[3])).getRGB());
		return new FColor(argb[1],argb[2],argb[3]);
	}

	private static FColor[] subtract(FColor[] first, FColor[] second)
	{
		FColor[] out = new FColor[first.length];
		for(int i = 0; i < first.length; i++)
		{
			float[] argb = new float[4];

			argb[1] = getValue(first,i).getRed() - getValue(second,i).getRed();

			argb[2] = getValue(first,i).getGreen() - getValue(second,i).getGreen();

			argb[3] = getValue(first,i).getBlue() - getValue(second,i).getBlue();
			
			//out[i] = new Color(PixelUtils.clamp(argb[1]), PixelUtils.clamp(argb[2]), PixelUtils.clamp(argb[3])).getRGB();
			out[i] = new FColor(argb[1],argb[2],argb[3]);
		}
		return out;
	}
	
	private static FColor[] add(FColor[] first, FColor[] second)
	{
		FColor[] out = new FColor[first.length];
		for(int i = 0; i < first.length; i++)
		{
			float[] argb = new float[4];

			argb[1] = getValue(first,i).getRed() + getValue(second,i).getRed();

			argb[2] = getValue(first,i).getGreen() + getValue(second,i).getGreen();

			argb[3] = getValue(first,i).getBlue() + getValue(second,i).getBlue();
			
			//out[i] = new Color(PixelUtils.clamp(argb[1]), PixelUtils.clamp(argb[2]), PixelUtils.clamp(argb[3])).getRGB();
			out[i] = new FColor(argb[1],argb[2],argb[3]);
		}
		return out;
	}

//	private static int intensify(int[] array, int index, int width)
//	{
//		int i = index % width;
//		int j = index / width;
//		int[] argb = new int [4];
//		final int its = 16;
//
//		argb[0] = new Color(getValue(array,twodtooned(i, j, width))).getAlpha();
//
//		argb[1] = new Color(getValue(array,twodtooned(i, j, width))).getRed();
//
//		argb[2] = new Color(getValue(array,twodtooned(i, j, width))).getGreen();
//
//		argb[3] = new Color(getValue(array,twodtooned(i, j, width))).getBlue();
//
//		return (new Color(PixelUtils.clamp(argb[1]*its), PixelUtils.clamp(argb[2]*its), PixelUtils.clamp(argb[3]*its)).getRGB());
//	}

	private static int twodtooned(int index, int index2, int width)
	{
		return(index + index2*width);
	}

	private static void putTogether()
	{
		for(int i = 0; i < gaussians.size(); i++)
		{
			FColor[] c = new FColor[laplacians.get(i).length];
			for(int j = 0; j < laplacians.get(i).length; j++)
			{
//				int red =  (int) (0.5 * new Color(laplacians.get(i)[j]).getRed() + 0.5 * new Color(laplacians2.get(i)[j]).getRed());
//				int green =  (int) (0.5 * new Color(laplacians.get(i)[j]).getGreen() + 0.5 * new Color(laplacians2.get(i)[j]).getGreen());
//				int blue =  (int) (0.5 * new Color(laplacians.get(i)[j]).getBlue() + 0.5 * new Color(laplacians2.get(i)[j]).getBlue());
				//c[j] = (int) (new Color(gaussians.get(i)[j]).getRed()/255.0 * laplacians.get(i)[j] + (1 - new Color(gaussians.get(i)[j]).getRed()/255.0) * laplacians2.get(i)[j]);
				float red =  gaussians.get(i)[j].getRed()/255.0f * laplacians.get(i)[j].getRed() + (1 - gaussians.get(i)[j].getRed()/255.0f) * laplacians2.get(i)[j].getRed();
				float green =  gaussians.get(i)[j].getRed()/255.0f * laplacians.get(i)[j].getGreen() + (1 - gaussians.get(i)[j].getRed()/255.0f) * laplacians2.get(i)[j].getGreen();
				float blue =  gaussians.get(i)[j].getRed()/255.0f * laplacians.get(i)[j].getBlue() + (1 - gaussians.get(i)[j].getRed()/255.0f) * laplacians2.get(i)[j].getBlue();
				//System.out.println((new Color(laplacians.get(i)[j])).getBlue());
				//System.out.println(red + " " + green + " " + blue);
				c[j] = new FColor(red,green,blue);
			}
			combined.add(c);
		}
	}

	private static int[] onlyRed(int[] array)
	{
		int[] array2 = new int[array.length];
		for(int i = 0; i < array.length; i++)
		{
			array2[i] = new Color(new Color(array[i]).getRed(),0,0).getRGB();
		}
		return array2;
	}
}
