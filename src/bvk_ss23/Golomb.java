
package bvk_ss23;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Golomb
{

    public static void encodeImage(RasterImage image, DataOutputStream out)
        throws IOException
    {
        BitOutputStream stream = new BitOutputStream(out);

        stream.write(image.width, 16);
        stream.write(image.height, 16);

        stream.write(image.getMode(), 8);
        stream.write((int) image.M, 8);

    }

    public static RasterImage decodeImage(DataInputStream in)
        throws IOException
    {
        BitInputStream stream = new BitInputStream(in);

        int width = stream.read(16);
        int height = stream.read(16);
        int mode = stream.read(8);
        int M = stream.read(8);
        
        // Reconstruct RasterImage
        RasterImage image = new RasterImage(width, height);
        image.setMode(mode);

        // Calculate m-parameter for Golomb decoding
        int b = (int) Math.ceil(Math.log(M) / Math.log(2)); // b = ceil(log2(M))
        int cutoff = (1 << b) - M;  // n = 2^b - M

        for (int i = 0; i < width * height; i++) 
        {
            // Decode Golomb code
            int quotient = 0;
            while (stream.read(1) == 1) 
            { 
            	// Read unary part
                quotient++;
            }
            // Read truncated binary part
            int remainder = stream.read(b - 1); 

            if (remainder >= cutoff) 
            {
                remainder = (remainder << 1) | stream.read(1);
                remainder -= cutoff;
            }
            int value = quotient * M + remainder;
            
            int gray = 0;
            if(mode == 0)
            	gray = value & 0xff;
            else if (mode == 2) 
            	gray = 128 + value & 0xff;
            
            image.argb[i] = 0xFF << 24 | (gray << 16) | (gray << 8) | gray;
        }
        stream.close();
        
        return image;
    }
}
