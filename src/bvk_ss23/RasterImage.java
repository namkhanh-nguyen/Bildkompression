// BVK Ue1 SS2023 Vorgabe
//
// Copyright (C) 2023 by Klaus Jung
// All rights reserved.
// Date: 2023-03-27

package bvk_ss23;

import java.io.File;
import java.util.Arrays;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

public class RasterImage {
	
	private static final int gray  = 0xffa0a0a0;

	public int[] argb;	// pixels represented as ARGB values in scanline order
	public int width;	// image width in pixels
	public int height;	// image height in pixels
	
	public RasterImage(int width, int height) {
		// creates an empty RasterImage of given size
		this.width = width;
		this.height = height;
		argb = new int[width * height];
		Arrays.fill(argb, gray);
	}
	
	public RasterImage(File file) {
		// creates an RasterImage by reading the given file
		Image image = null;
		if(file != null && file.exists()) {
			image = new Image(file.toURI().toString());
		}
		if(image != null && image.getPixelReader() != null) {
			width = (int)image.getWidth();
			height = (int)image.getHeight();
			argb = new int[width * height];
			image.getPixelReader().getPixels(0, 0, width, height, PixelFormat.getIntArgbInstance(), argb, 0, width);
		} else {
			// file reading failed: create an empty RasterImage
			this.width = 256;
			this.height = 256;
			argb = new int[width * height];
			Arrays.fill(argb, gray);
		}
	}
	
	public RasterImage(ImageView imageView) {
		// creates a RasterImage from that what is shown in the given ImageView
		Image image = imageView.getImage();
		width = (int)image.getWidth();
		height = (int)image.getHeight();
		argb = new int[width * height];
		image.getPixelReader().getPixels(0, 0, width, height, PixelFormat.getIntArgbInstance(), argb, 0, width);
	}
	
	public void setToView(ImageView imageView) {
		// sets the current argb pixels to be shown in the given ImageView
		if(argb != null) {
			WritableImage wr = new WritableImage(width, height);
			PixelWriter pw = wr.getPixelWriter();
			pw.setPixels(0, 0, width, height, PixelFormat.getIntArgbInstance(), argb, 0, width);
			imageView.setImage(wr);
		}
	}
		
	// image operations
	public static int[] convertToGrayscale(int[] argb) 
	{
        int[] grayscale = new int[argb.length];

        for (int i = 0; i < argb.length; i++) {
            int alpha = (argb[i] >> 24) & 0xFF;
            
            // Calculate grayscale value using RGB
            int red = (argb[i] >> 16) & 0xFF;
            int green = (argb[i] >> 8) & 0xFF;
            int blue = argb[i] & 0xFF;
            int gray = (red + green + blue)/3;

            // Create new color with grayscale
            grayscale[i] = (alpha << 24) | (gray << 16) | (gray << 8) | gray;
        }
        return grayscale;
	}
	
	public double getMSEfromComparisonTo(RasterImage image) 
	{
		
		if (image == null || image.argb.length != this.argb.length) 
		{
            return -1.0; // Invalid comparison, return -1.0 as error code
        }

        double sum = 0.0;
        for (int i = 0; i < this.argb.length; i++) 
        {
            // Calculate squared differences for each pixel component (ARGB)
            int diffA = ((this.argb[i] >> 24) & 0xFF) - ((image.argb[i] >> 24) & 0xFF);
            int diffR = ((this.argb[i] >> 16) & 0xFF) - ((image.argb[i] >> 16) & 0xFF);
            int diffG = ((this.argb[i] >> 8) & 0xFF) - ((image.argb[i] >> 8) & 0xFF);
            int diffB = (this.argb[i] & 0xFF) - (image.argb[i] & 0xFF);

            // Accumulate squared differences
            sum += diffA * diffA + diffR * diffR + diffG * diffG + diffB * diffB;
        }
		
        double meanSquaredError = sum / this.argb.length;
        return meanSquaredError;
	}
	
	public static RasterImage preprocessImage(RasterImage image)
	{
		return image;
	}
	
	public static RasterImage processGolombImage(RasterImage image)
	{
		return image;
	}
	
}
