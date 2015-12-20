import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

public class Poisson
{
	
	public static void pasteOntoAtPoint(BufferedImage pastee, BufferedImage paster, int x, int y)
	{
		for(int j = 0; j < paster.getHeight(); j++)
		{
			for(int i = 0; i < paster.getWidth(); i++)
			{
				try
				{
					pastee.setRGB(x+i, y+j, paster.getRGB(i, j));
				}
				catch(Exception e)
				{
					System.err.println("bad point " + e);
				}
			}
		}
	}
	
	public static void pasteModified(BufferedImage pastee, BufferedImage paster, int x, int y)
	{
		Graphics2D g2d = (Graphics2D) pastee.getGraphics();
		double angle = Math.random()*Math.PI*2;
		double scale = (Math.random()+1)*0.1;
		AffineTransform af2 = AffineTransform.getRotateInstance(angle,paster.getWidth()/2.0,paster.getHeight()/2.0);
		double[] matrix = new double[6];
		af2.getMatrix(matrix);
		for(int i = 0; i < 6; i++)
			matrix[i]*=scale;
		
		matrix[4] += x;
		matrix[5] += y;
		af2 = new AffineTransform(matrix);
		g2d.drawImage(paster, af2, null);
		
		matrix[4] -= pastee.getWidth();
		matrix[5] -= 0;
		af2 = new AffineTransform(matrix);
		g2d.drawImage(paster, af2, null);
		
		matrix[4] -= 0;
		matrix[5] -= pastee.getHeight();
		af2 = new AffineTransform(matrix);
		g2d.drawImage(paster, af2, null);
		
		matrix[4] += pastee.getWidth();
		matrix[5] -= 0;
		af2 = new AffineTransform(matrix);
		g2d.drawImage(paster, af2, null);
	}
	
	private static class Point
	{
		static float minDist = 0.12f;
		float x;
		float y;
		
		public Point(float x, float y)
		{
			this.x = x;
			this.y = y;
		}
		
		public static boolean isTooClose(Point a, Point b)
		{
			return ((a.x - b.x)*(a.x - b.x) + (a.y - b.y)*(a.y - b.y) < minDist*minDist);
		}
		
		public void print()
		{
			System.out.println("x: " + this.x + " y: " + this.y);
		}
		
	}
	
	public static void main(String[] args) throws IOException
	{
		BufferedImage im1 = ImageIO.read(new File(args[0]));//gets pasted onto
		BufferedImage im2 = ImageIO.read(new File(args[1]));//paste with this
		BufferedImage im3 = ImageIO.read(new File(args[2]));//gets pasted onto (normal)
		BufferedImage im4 = null;
		if(args.length >= 4)
			im4 = ImageIO.read(new File(args[3]));//paste with this (normal)
		File outputfile1 = new File(args[0].substring(0, args[0].length()-4) + "_m" + args[0].substring(args[0].length()-4,args[0].length()));
		File outputfile2 = new File(args[2].substring(0, args[2].length()-4) + "_m" + args[2].substring(args[2].length()-4,args[2].length()));
		
		
		 ArrayList<Point> points = new ArrayList<Point>();
		 int failures = 0;
		 int maxFailures = 1000;
		 while(failures < maxFailures)
		 {
			 boolean breakOut = false;
			 float x = (float)Math.random();
			 float y = (float)Math.random();
			 Point p = new Point(x,y);
			 for(Point pj: points)
			 {
				 if(Point.isTooClose(pj,p) || Point.isTooClose(pj,new Point(p.x-1,p.y)) || Point.isTooClose(pj,new Point(p.x,p.y-1)) || Point.isTooClose(pj,new Point(p.x-1,p.y-1)) || Point.isTooClose(pj,new Point(p.x+1,p.y)) || Point.isTooClose(pj,new Point(p.x,p.y+1)) || Point.isTooClose(pj,new Point(p.x+1,p.y+1)))
				 {
					 failures++;
					 breakOut = true;
					 break;
				 }
			 }
			 if(breakOut)
				 continue;
			 failures = 0;
			 points.add(p);
		 }
		 for(Point p: points)
		 {
			 pasteModified(im1,im2,(int)(p.x*im1.getWidth()),(int)(p.y*im1.getHeight()));
			 if(args.length >= 4)
				 pasteModified(im3,im4,(int)(p.x*im3.getWidth()),(int)(p.y*im3.getHeight()));
			 p.print();
		 }
		 ImageIO.write(im1, "png", outputfile1);
		 ImageIO.write(im3, "png", outputfile2);
			 
	}
}
