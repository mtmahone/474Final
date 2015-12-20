import com.jhlabs.image.*;

import java.awt.*;
import java.awt.image.*;

import javax.imageio.*;

import java.io.*;
import java.util.*;

public class Blending {
	
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
		BufferedImage src = null, dst = null;
		BufferedImage src1 = null, dst1 = null;
		BufferedImage src2 = null, dst2 = null;
		BufferedImage tmp = null;	// used for swapping src and dst buffer
		int width, height;			// image width, height

		String arg;
		String outputfilename = "output.png";		// default output filename
		
		while (i < args.length && args[i].startsWith("-")) {
			arg = args[i++];

			if (arg.equals("-input1")) {

				String inputfile1 = args[i++];
				try {
					src1 = ImageIO.read(new File(inputfile1));
				} catch (IOException e) {
					System.out.println("no input");
				}
				width = src1.getWidth();
				height = src1.getHeight();
				dst = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
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
			} else if (arg.equals("-output")) {

				outputfilename = args[i++];
				System.out.println("Output file: " + outputfilename);
				continue;
			}
		}
		//Cheat(src1, src2, dst);
		Blackout(src1, src2, 518, 56, dst);
		//TextureBomb(src1, src2, 518, 56, dst);
		//TextureBomb(src1, src2, 34, 45, dst);
		tmp = src1; src1 = dst; dst = tmp;
		if (i != args.length) {
			System.out.println("there are unused arguments");
		}
		// write the output image to disk file
		File outfile = new File(outputfilename);
		try {
			ImageIO.write(src1, "png", outfile);
		} catch(IOException e) {
		}
		
	}
	
	public static void TextureBombSimple(BufferedImage src1, BufferedImage src2, BufferedImage dst){
		int width = src1.getWidth();
		int height = src1.getHeight();
		if(width != src2.getWidth() || height != src2.getHeight()){
			System.out.println("size mismatch");
		}
		int[] pixels1 = new int[width*height];
		int[] pixels2 = new int[width*height];
		int[] pixels = new int[width*height];
		/*
		for(int i = 0; i < height; i++){
			for(int j = 0; j < width; j++){
				pixels1[i*height+j] = src1.getRGB(j,i);
				pixels2[i*height+j] = src2.getRGB(j,i);
			}
		}
		*/
		
		src1.getRGB(0, 0, width, height, pixels1, 0, width);
		src2.getRGB(0, 0, width, height, pixels2, 0, width);
		
		int a,r,g,b,a1,r1,g1,b1,a2,r2,g2,b2;
		for(int i = 0; i < pixels1.length; i++){
			boolean changed = false;
			Color rgb1 = new Color(pixels1[i]);
			Color rgb2 = new Color(pixels2[i]);
			
			a1 = rgb1.getAlpha();
			r1 = rgb1.getRed();
			g1 = rgb1.getGreen();
			b1 = rgb1.getBlue();
			a2 = rgb2.getAlpha();
			r2 = rgb2.getRed();
			g2 = rgb2.getGreen();
			b2 = rgb2.getBlue();
			r1 = PixelUtils.clamp((int)((float)r1));
			g1 = PixelUtils.clamp((int)((float)g1));
			b1 = PixelUtils.clamp((int)((float)b1));
			r2 = PixelUtils.clamp((int)((float)r2));
			g2 = PixelUtils.clamp((int)((float)g2));
			b2 = PixelUtils.clamp((int)((float)b2));
			/*
			if(r1 != r2 || g1 != g2 || b1 != b2){
				pixels[i]= new Color((r1+r2)/2, (g1+g2)/2, (b1+b2)/2, (a1+a2)/2).getRGB();
			}
			*/
			if(Math.abs(r1-r2) > 100){
				r1 = (r1+r2)/2;
			}
			if(Math.abs(g1-r2) > 100){
				g1 = (g1+g2)/2;
			}
			if(Math.abs(b1-b2) > 100){
				b1 = (b1+b2)/2;
			}
			//else{
				pixels[i]= new Color(r1, g1, b1, a1).getRGB();
				//System.out.println(a1);
			//}
			/*if(r1 != r2){
				r1 = (r1+r2)/2;
			}
			if(g1 != g2){
				g1 = (g1+g2)/2;
			}
			if(b1 != b2){
				b1 = (b1+b2)/2;
			}
			pixels[i]= new Color(r1, g1, b1, a1).getRGB();*/
			
		}
		dst.setRGB(0, 0, width, height, pixels, 0, width);
		
	}
	
	public static void TextureBomb(BufferedImage src1, BufferedImage src2, int x, int y, BufferedImage dst){
		
		int width = src1.getWidth();
		int height = src1.getHeight();
		//System.out.println(width+" "+height);
		int[] p1 = new int[width*height];//pixels for src1
		src1.getRGB(0, 0, width, height, p1, 0, width);
		boolean gradient = false;
		
		// x and y derivatives for r,g,b of p1
		int[][] rdx = new int[width][height];
		int[][] rdy = new int[width][height];
		int[][] gdx = new int[width][height];
		int[][] gdy = new int[width][height];
		int[][] bdx = new int[width][height];
		int[][] bdy = new int[width][height];
		
		// Finds gradient of src1
		int a1,r1,g1,b1;
		for(int j = 0; j < height; j++){
			for(int i = 0; i < width; i++){
				Color rgb = new Color(p1[i+(j*width)]);
				
				a1 = rgb.getAlpha();
				r1 = rgb.getRed();
				g1 = rgb.getGreen();
				b1 = rgb.getBlue();
				r1 = PixelUtils.clamp((int)((float)r1));
				g1 = PixelUtils.clamp((int)((float)g1));
				b1 = PixelUtils.clamp((int)((float)b1));
			
				if(i == width-1){
					rdx[i][j] = 0;
					gdx[i][j] = 0;
					bdx[i][j] = 0;
				}
				else{
					rdx[i][j] = (new Color(p1[(i+1)+(j*width)])).getRed()-r1;
					gdx[i][j] = (new Color(p1[(i+1)+(j*width)])).getGreen()-g1;
					bdx[i][j] = (new Color(p1[(i+1)+(j*width)])).getBlue()-b1;
				}
				if(j == height-1){
					rdy[i][j] = 0;
					gdy[i][j] = 0;
					bdy[i][j] = 0;
				}
				else{
					rdy[i][j] = (new Color(p1[i+(j*width+1)])).getRed()-r1;
					gdy[i][j] = (new Color(p1[i+(j*width+1)])).getGreen()-g1;
					bdy[i][j] = (new Color(p1[i+(j*width+1)])).getBlue()-b1;
				}
			}
		}
		
		width = src2.getWidth();
		height = src2.getHeight();
		int[] p2 = new int[width*height];//pixels for src2
		src2.getRGB(0, 0, width, height, p2, 0, width);
		
		// x and y derivatives for r,g,b of p1
		int[][] rdx2 = new int[width][height];
		int[][] rdy2 = new int[width][height];
		int[][] gdx2 = new int[width][height];
		int[][] gdy2 = new int[width][height];
		int[][] bdx2 = new int[width][height];
		int[][] bdy2 = new int[width][height];
		
		// Finds Derivatives of src2
		int a2,r2,g2,b2;
		for(int j = 0; j < height; j++){
			for(int i = 0; i < width; i++){
				Color rgb = new Color(p2[i+(j*width)]);
				
				a2 = rgb.getAlpha();
				r2 = rgb.getRed();
				g2 = rgb.getGreen();
				b2 = rgb.getBlue();
				r2 = PixelUtils.clamp((int)((float)r2));
				g2 = PixelUtils.clamp((int)((float)g2));
				b2 = PixelUtils.clamp((int)((float)b2));
			
				if(i == width-1){
					rdx2[i][j] = 0;
					gdx2[i][j] = 0;
					bdx2[i][j] = 0;
				}
				else{
					rdx2[i][j] = (new Color(p2[(i+1)+(j*width)])).getRed()-r2;
					gdx2[i][j] = (new Color(p2[(i+1)+(j*width)])).getGreen()-g2;
					bdx2[i][j] = (new Color(p2[(i+1)+(j*width)])).getBlue()-b2;
				}
				if(j == height-1){
					rdy2[i][j] = 0;
					gdy2[i][j] = 0;
					bdy2[i][j] = 0;
				}
				else{
					rdy2[i][j] = (new Color(p2[i+(j*width+1)])).getRed()-r2;
					gdy2[i][j] = (new Color(p2[i+(j*width+1)])).getGreen()-g2;
					bdy2[i][j] = (new Color(p2[i+(j*width+1)])).getBlue()-b2;
				}
			}
		}
		
		// Overlays derivatives of src2 onto src1
		for(int i = x; i < x+width; i++){
			for(int j = y; j < y+height; j++){
				rdx[i][j] = rdx2[i-x][j-y];
				rdy[i][j] = rdy2[i-x][j-y];
				gdx[i][j] = gdx2[i-x][j-y];
				gdy[i][j] = gdy2[i-x][j-y];
				bdx[i][j] = bdx2[i-x][j-y];
				bdy[i][j] = bdy2[i-x][j-y];
			}
		}
		
		//set width and height back for src1
		width = src1.getWidth();
		height = src1.getHeight();
		
		if(gradient == true){
			int[] pixels = new int[width*height];
			for(int j = 0; j < height; j++){
				for(int i = 0; i < width; i++){
					//System.out.println(bdx[i][j]);
					pixels[i+(j*width)] = new Color(PixelUtils.clamp(Math.abs(rdy[i][j])*5),PixelUtils.clamp(Math.abs(bdy[i][j])*5),PixelUtils.clamp(Math.abs(gdy[i][j])*5), 255).getRGB();
				}
			}
			dst.setRGB(0, 0, width, height, pixels, 0, width);
			return;
		}
		
		// Finds laplacian
		int rx,ry,r,gx,gy,g,bx,by,b;
		for(int j = 0; j < height; j++){
			for(int i = 0; i < width; i++){
				Color rgb = new Color(p1[i+(j*width)]);
				
				a1 = rgb.getAlpha();
				r1 = rgb.getRed();
				g1 = rgb.getGreen();
				b1 = rgb.getBlue();
				r1 = PixelUtils.clamp((int)((float)r1));
				g1 = PixelUtils.clamp((int)((float)g1));
				b1 = PixelUtils.clamp((int)((float)b1));
			
				if(i == 0){
					rx = rdx[i][j]-rdx[i+1][j];
					gx = gdx[i][j]-gdx[i+1][j];
					bx = bdx[i][j]-bdx[i+1][j];
				}
				else if(i == width-1){
					rx = rdx[i][j]-rdx[i-1][j];
					gx = gdx[i][j]-gdx[i-1][j];
					bx = bdx[i][j]-bdx[i-1][j];
				}
				else{
					rx = rdx[i][j] - (rdx[i-1][j]+rdx[i+1][j])/2;
					gx = gdx[i][j] - (gdx[i-1][j]+gdx[i+1][j])/2;
					bx = bdx[i][j] - (bdx[i-1][j]+bdx[i+1][j])/2;
				}
				if(j == 0){
					ry = rdy[i][j] - rdy[i][j+1];
					gy = gdy[i][j] - gdy[i][j+1];
					by = bdy[i][j] - bdy[i][j+1];
				}
				else if(j == height-1){
					ry = rdy[i][j] - rdy[i][j-1];
					gy = gdy[i][j] - gdy[i][j-1];
					by = bdy[i][j] - bdy[i][j-1];
				}
				else{
					ry = rdy[i][j] - (rdy[i][j-1]+rdy[i][j+1])/2;
					gy = gdy[i][j] - (gdy[i][j-1]+gdy[i][j+1])/2;
					by = bdy[i][j] - (bdy[i][j-1]+bdy[i][j+1])/2;
				}
				r = PixelUtils.clamp(rx+ry);
				//r = 0;
				g = PixelUtils.clamp(gx+gy);
				//g = 0;
				b = PixelUtils.clamp(bx+by);
				//b = 0;
				float grey = .3f*r+.59f*g+.11f*b;
				//p1[i+(j*width)] = new Color(Math.abs(r),Math.abs(g),Math.abs(b),255).getRGB();
				p1[i+(j*width)] = new Color(r,g,b,255).getRGB();
			}
		}
		dst.setRGB(0, 0, width, height, p1, 0, width);
		
		
	}
	
	public static void Cheat(BufferedImage src1, BufferedImage src2, BufferedImage dst){
		
		int width = src1.getWidth();
		int height = src1.getHeight();
		if(width != src2.getWidth() || height != src2.getHeight()){
			System.out.println("size mismatch");
		}
		int[] pixels1 = new int[width*height];
		int[] pixels2 = new int[width*height];
		int[] pixels = new int[width*height];
		/*
		for(int i = 0; i < height; i++){
			for(int j = 0; j < width; j++){
				pixels1[i*height+j] = src1.getRGB(j,i);
				pixels2[i*height+j] = src2.getRGB(j,i);
			}
		}
		*/
		
		src1.getRGB(0, 0, width, height, pixels1, 0, width);
		src2.getRGB(0, 0, width, height, pixels2, 0, width);
		
		int a,r,g,b,a1,r1,g1,b1,a2,r2,g2,b2;
		for(int i = 0; i < pixels1.length; i++){
			boolean changed = false;
			Color rgb1 = new Color(pixels1[i]);
			Color rgb2 = new Color(pixels2[i]);
			
			a1 = rgb1.getAlpha();
			r1 = rgb1.getRed();
			g1 = rgb1.getGreen();
			b1 = rgb1.getBlue();
			a2 = rgb2.getAlpha();
			r2 = rgb2.getRed();
			g2 = rgb2.getGreen();
			b2 = rgb2.getBlue();
			r1 = PixelUtils.clamp((int)((float)r1));
			g1 = PixelUtils.clamp((int)((float)g1));
			b1 = PixelUtils.clamp((int)((float)b1));
			r2 = PixelUtils.clamp((int)((float)r2));
			g2 = PixelUtils.clamp((int)((float)g2));
			b2 = PixelUtils.clamp((int)((float)b2));
			
			if(r2 >= 235 && g2 >= 235 && b2 >= 235){
				pixels[i]= new Color(r1, g1, b1, a1).getRGB();
			}
			else{
				pixels[i]= new Color(r2, g2, b2, a1).getRGB();
			}
		}
		dst.setRGB(0, 0, width, height, pixels, 0, width);
	}
	
	public static void Blackout(BufferedImage src1, BufferedImage src2, int x, int y, BufferedImage dst){
		
		int width1 = src1.getWidth();
		int height1 = src1.getHeight();
		int[] p1 = new int[width1*height1];//pixels for src1
		for(int i = 0; i < width1*height1; i++){
			p1[i] = new Color(0,0,0,255).getRGB();
		}
		
		int width2 = src2.getWidth();
		int height2 = src2.getHeight();
		int[] p2 = new int[width2*height2];
		for(int i = 0; i < width2*height2; i++){
			p2[i] = new Color(255,255,255,255).getRGB();
		}
		
		for(int j = 0; j < height2; j++){
			for(int i = 0; i < width2; i++){
				p1[(x+i)+(j*width1+y)] = p2[i+(j*width2)];
			}
		}
		dst.setRGB(0, 0, width1, height1, p1, 0, width1);
	}
	
}
