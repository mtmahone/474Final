import com.jhlabs.image.*;

import java.awt.*;
import java.awt.image.*;

import javax.imageio.*;

import java.io.*;
import java.util.*;

public class Blending3 {

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

	private static class Point
	{
		public int x;
		public int y;

		public Point(int x, int y)
		{
			this.x = x;
			this.y = y;
		}

		public void print()
		{
			System.out.println("x: " + this.x + " y: " + this.y);
		}

	}


	private static ArrayList<FColor[]> laplacians1 = new ArrayList<FColor[]>();
	private static ArrayList<FColor[]> laplacians2 = new ArrayList<FColor[]>();
	private static ArrayList<FColor[]> gaussians1 = new ArrayList<FColor[]>();
	private static ArrayList<FColor[]> gaussians2 = new ArrayList<FColor[]>();
	private static ArrayList<FColor[]> gaussians3 = new ArrayList<FColor[]>();
	private static ArrayList<FColor[]> combined = new ArrayList<FColor[]>();
	private static ArrayList<Point> sizes = new ArrayList<Point>();

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
		combine(src1, src2, src3, outputfilename);
	}

	public static void combine(BufferedImage src1, BufferedImage src2, BufferedImage src3, String outputfilename){
		//images must me same size
		int width = src1.getWidth();
		int height = src1.getHeight();
		int[] p1 = new int[width*height];//pixels for src1
		src1.getRGB(0, 0, width, height, p1, 0, width);

		int[] p2 = new int[width*height];//pixels for src2
		src2.getRGB(0, 0, width, height, p2, 0, width);

		int[] p3 = new int[width*height];//pixels for src3
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
		//for(int j = 0; j < iter; j++)
		int k = 0;
		gaussians1.add(p1c);
		gaussians2.add(p2c);
		gaussians3.add(p3c);
		sizes.add(new Point(width,height));
		while(width>1&&height>1)
		{
			k++;
			for(int i = 0; i < /*p1.length*/ width*height; i++)
			{
				pOutput[i] = getGaussian(p1c,i,width);
				pOutput2[i] = getGaussian(p2c,i,width);
				pOutput3[i] = getGaussian(p3c,i,width);
			}
			pOutput = shrink(pOutput,width,height);
			pOutput2 = shrink(pOutput2,width,height);
			pOutput3 = shrink(pOutput3,width,height);
			width = (width+1)/2;
			height = (height+1)/2;
			sizes.add(new Point(width,height));
			
			gaussians1.add(pOutput.clone());
			gaussians2.add(pOutput2.clone());
			gaussians3.add(pOutput3.clone());
			p1c = (FColor[])pOutput.clone();
			p2c = (FColor[])pOutput2.clone();
			p3c = (FColor[])pOutput3.clone();
			//convert back to ints
			p1 = new int[p1c.length];
			p2 = new int[p2c.length];
			p3 = new int[p3c.length];
		}
		for(int i = 0; i < gaussians1.size(); i++)
		{
			FColor[] p1g = gaussians1.get(i);
			FColor[] p2g = gaussians2.get(i);
			FColor[] p3g = gaussians3.get(i);
			for(int j = i; j > 0; j--)
			{
				p1g = grow(p1g,sizes.get(j).x,sizes.get(j).y,sizes.get(j-1).x,sizes.get(j-1).y);
				p2g = grow(p2g,sizes.get(j).x,sizes.get(j).y,sizes.get(j-1).x,sizes.get(j-1).y);
				p3g = grow(p3g,sizes.get(j).x,sizes.get(j).y,sizes.get(j-1).x,sizes.get(j-1).y);
			}

			//set to grown ones
			gaussians1.set(i, p1g);
			gaussians2.set(i, p2g);
			gaussians3.set(i, p3g);
		}
		
		//print out resized lowres images
//		for(int i = 0; i < gaussians1.size(); i++)
//		{
//			p1 = new int[gaussians1.get(i).length];
//			for(int j = 0; j < gaussians1.get(i).length; j++)
//			{
//				p1[j] = gaussians1.get(i)[j].toInt();
//			}
//			BufferedImage d = new BufferedImage(sizes.get(0).x,sizes.get(0).y,BufferedImage.TYPE_INT_ARGB);
//			d.setRGB(0, 0, sizes.get(0).x, sizes.get(0).y, p1, 0, sizes.get(0).x);
//			File outfile = new File(outputfilename.substring(0, outputfilename.length()-4) + "_g" + (i+1) + outputfilename.substring(outputfilename.length()-4,outputfilename.length()));
//			try 
//			{
//				//ImageIO.write(dst, "png", outfile);
//				ImageIO.write(d, "png", outfile);
//			} 
//			catch(IOException e) 
//			{
//			}
//		}

		
		for(int i = 0; i < gaussians1.size()-1; i++)
		{
			FColor[] pL = subtract(gaussians1.get(i),gaussians1.get(i+1));
			FColor[] pL2 = subtract(gaussians2.get(i),gaussians2.get(i+1));
			laplacians1.add(pL);
			laplacians2.add(pL2);
		}
		
		//print out difference images (laplacians
//		for(int i = 0; i < laplacians1.size(); i++)
//		{
//			p1 = new int[laplacians1.get(i).length];
//			for(int j = 0; j < laplacians1.get(i).length; j++)
//			{
//				p1[j] = laplacians1.get(i)[j].toInt();
//			}
//			BufferedImage d = new BufferedImage(sizes.get(0).x,sizes.get(0).y,BufferedImage.TYPE_INT_ARGB);
//			d.setRGB(0, 0, sizes.get(0).x, sizes.get(0).y, p1, 0, sizes.get(0).x);
//			File outfile = new File(outputfilename.substring(0, outputfilename.length()-4) + "_g" + (i+1) + outputfilename.substring(outputfilename.length()-4,outputfilename.length()));
//			try 
//			{
//				//ImageIO.write(dst, "png", outfile);
//				ImageIO.write(d, "png", outfile);
//			} 
//			catch(IOException e) 
//			{
//			}
//		}

		width = sizes.get(0).x;
		height = sizes.get(0).y;
		
		FColor[] total = new FColor[width*height];
		for(int i = 0; i < total.length; i++)
		{
			total[i] = new FColor();
		}
		int[] totali = new int[width*height];

		putTogether();

		for(FColor[] i : combined)
		{
			//System.out.println(combined.size());
			total = sum(total,i);
		}
		for(int i = 0; i < total.length; i++)
		{
			totali[i] = total[i].toInt();
		}
		BufferedImage d = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
		d.setRGB(0, 0, width, height, totali, 0, width);

		File outfile = new File(outputfilename.substring(0, outputfilename.length()-4) + "total" + outputfilename.substring(outputfilename.length()-4,outputfilename.length()));
		try 
		{
			ImageIO.write(d, "png", outfile);
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

	private static FColor[] sum(FColor[] first, FColor[] second)
	{
		FColor[] out = new FColor[first.length];
		for(int i = 0; i < first.length; i++)
		{
			float[] argb = new float[4];

			argb[1] = getValue(first,i).getRed() + getValue(second,i).getRed();

			argb[2] = getValue(first,i).getGreen() + getValue(second,i).getGreen();

			argb[3] = getValue(first,i).getBlue() + getValue(second,i).getBlue();

			out[i] = new FColor(argb[1],argb[2],argb[3]);
		}
		return out;
	}

	private static FColor[] shrink(FColor[] array, int width, int height)
	{
		int w = (width+1)/2;
		int h = (height+1)/2;
		FColor[] out = new FColor[w * h];

		for(int j = 0; j < height; j++)
		{
			for(int i = 0; i < width; i++)
			{
				if(j%2 == 1 || i%2 == 1)
					continue;
				else
					out[twodtooned(i/2,j/2,w)] = array[twodtooned(i,j,width)];
			}
		}


		return out;	
	}

	private static FColor[] grow(FColor[] array, int width, int height, int nw, int nh)//nw,nh should be 2n or 2n-1 from width, height
	{
		FColor[] out = new FColor[nw*nh];
		for(int j = 0; j < nh; j++)
		{
			for(int i = 0; i < nw; i++)
			{
				out[twodtooned(i,j,nw)] = array[twodtooned(i/2,j/2,width)];
			}
		}

		return out;
	}

	private static int twodtooned(int index, int index2, int width)//simple function for making a 2D array input into a 1D array input
	{
		return(index + index2*width);
	}

	private static void putTogether()
	{
		//final float diff = 1.0f;
		final float totalPer = 0.1f;
		final float fc = totalPer;//*(1+diff);
		final float fd = totalPer;//*(1-diff);
		for(int i = 0; i < laplacians1.size(); i++)
		{
			FColor[] c = new FColor[laplacians1.get(i).length];
			for(int j = 0; j < laplacians1.get(i).length; j++)
			{
				float red =  gaussians3.get(i)[j].getRed()/255.0f * laplacians1.get(i)[j].getRed() *fc + (1 - laplacians2.get(i)[j].getRed()/255.0f) * gaussians2.get(i)[j].getRed() *fd;
				float green =  gaussians3.get(i)[j].getRed()/255.0f * laplacians1.get(i)[j].getGreen() *fc + (1 - laplacians2.get(i)[j].getRed()/255.0f) * gaussians2.get(i)[j].getGreen() *fd;
				float blue =  gaussians3.get(i)[j].getRed()/255.0f * laplacians1.get(i)[j].getBlue() *fc + (1 - laplacians2.get(i)[j].getRed()/255.0f) * gaussians2.get(i)[j].getBlue() *fd;
				c[j] = new FColor(red,green,blue);
			}
			combined.add(c);
		}
	}
	
}
