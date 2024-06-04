
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
        int M = (int) image.M;

        stream.write(mode, 8);
        stream.write(M, 8);

        for (int i = 0; i < image.argb.length; i++)
        {
            int value = image.argb[i] & 0xff;
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

        // Reconstruct RasterImage
        RasterImage image = new RasterImage(width, height);
        image.setMode(mode);

        // Calculate M-parameter for Golomb decoding
        int b = (int) Math.ceil(Math.log(M) / Math.log(2)); // b = ceil(log2(M))
        int cutoff = (1 << b) - M; // n = 2^b - M

        for (int i = 0; i < width * height; i++)
        {
            int quotient = 0;
            while (stream.read(1) == 1)
                quotient++; // Read unary part

            // Read truncated binary part
            int remainder = stream.read(b - 1);

            if (remainder >= cutoff)
            {
                remainder = remainder << 1 | stream.read(1);
                remainder -= cutoff;
            }
            int value = quotient * M + remainder;

            int gray = 0;

            if (mode == 2 && i > 0)
            {
                // DPCM horizontal mode
                gray = 128 + value & 0xff;
                image.argb[i - 1] = 0xFF << 24 | gray << 16 | gray << 8 | gray;
            }
            else
            {
                // Copy mode
                gray = value & 0xff;
                image.argb[i] = 0xFF << 24 | gray << 16 | gray << 8 | gray;
            }
        }
        stream.close();

        return image;
    }
}
