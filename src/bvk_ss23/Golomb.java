
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

        int mode = image.getMode();
        int M = image.M < 1 ? 1 : (int) image.M;

        stream.write(mode, 8);
        stream.write(M, 8);

        int prevPixel = 128;

        for (int i = 0; i < image.argb.length; i++)
        {
            int value;
            if (mode == 2)
            {
                int currentPixel = image.argb[i] & 0xFF;
                int difference = currentPixel - prevPixel;
                value = difference >= 0 ? 2 * difference : -2 * difference - 1;
                prevPixel = currentPixel;
            }
            else
            {
                value = image.argb[i] & 0xFF;
            }

            int quotient = value / M;
            int remainder = value % M;
            for (int j = 0; j < quotient; j++)
                stream.write(1, 1);
            stream.write(0, 1);
            int b = (int) Math.ceil(Math.log(M) / Math.log(2));
            int cutoff = (1 << b) - M;
            if (remainder < cutoff)
                stream.write(remainder, b - 1);
            else
                stream.write(remainder + cutoff, b);
        }
        stream.close();
    }

    public static RasterImage decodeImage(DataInputStream in)
        throws IOException
    {
        BitInputStream stream = new BitInputStream(in);

        int width = stream.read(16);
        int height = stream.read(16);
        int mode = stream.read(8);
        int M = stream.read(8);
        if (M < 1)
            M = 1;

        // Reconstruct RasterImage
        RasterImage image = new RasterImage(width, height);
        image.setMode(mode);

        // Calculate M-parameter for Golomb decoding
        int b = (int) Math.ceil(Math.log(M) / Math.log(2)); // b = ceil(log2(M))
        int cutoff = (1 << b) - M; // n = 2^b - M
        
        int prevPixel = 128;

        for (int i = 0; i < width * height; i++)
        {
            int quotient = 0;
            while (stream.read(1) == 1)
                quotient++; // Read unary part

            // Read truncated binary part
            int remainder;
            if (M == 1)
            {
                remainder = 0;
            }
            else
            {
                int valueOrPrefix = stream.read(b - 1);
                if (valueOrPrefix < cutoff)
                {
                    remainder = valueOrPrefix;
                }
                else
                {
                    remainder = (valueOrPrefix << 1) | stream.read(1);
                    remainder -= cutoff;
                }
            }
            int value = quotient * M + remainder;

            if (mode == 2)
            {
                // DPCM horizontal mode
                int difference = value % 2 == 0 ? value / 2 : -(value / 2 + 1);
                int currentPixel = prevPixel + difference;
                if (currentPixel < 0)
                    currentPixel = 0;
                if (currentPixel > 255)
                    currentPixel = 255;
                prevPixel = currentPixel;
                image.argb[i] = 0xFF << 24 | currentPixel << 16 | currentPixel << 8 | currentPixel;
            }
            else
            {
                // Copy mode
                int colour = value & 0xff;
                image.argb[i] = 0xFF << 24 | colour << 16 | colour << 8 | colour;
            }
        }
        stream.close();

        return image;
    }
}
