/*
 * File: Arithmetic.java
 *
 * Copyright (c) 2004-2021 Diebold Nixdorf Systems GmbH,
 * Heinz-Nixdorf-Ring 1, 33106 Paderborn, Germany
 * All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of Diebold Nixdorf ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered
 * into with Diebold Nixdorf.
 */

package bvk_ss23;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Arithmetic
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

        int prevPixel = 128;

        for (int i = 0; i < width * height; i++)
        {
            int quotient = 0;
            while (stream.read(1) == 1)
                quotient++; // Read unary part

            // Read truncated binary part
            if (b < 1)
                b = 1;
            int remainder = stream.read(b - 1);

            if (remainder >= cutoff)
            {
                remainder = remainder << 1 | stream.read(1);
                remainder -= cutoff;
            }
            int value = quotient * M + remainder;

            if (mode == 2)
            {
                // DPCM horizontal mode
                int difference = value % 2 == 0 ? value / 2 : -(value / 2 + 1);
                int currentPixel = prevPixel + difference;
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
