
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

        //		stream.write(0, 8);
        //		stream.write(64, 8);

    }

    public static RasterImage decodeImage(DataInputStream in)
        throws IOException
    {

        BitInputStream stream = new BitInputStream(in);

        /**
         *     Bits (width, height, mode, M):
        	   1000011110001000 1000011110000111 10000110 10000111 1000011110000110
         */

        int width = stream.read(16);
        int height = stream.read(16);
        int mode = stream.read(8);

        int M = stream.read(8);
        int golomb = stream.read(16);

        // Reconstruct RasterImage
        RasterImage image = new RasterImage(width, height);

        image.setMode(mode);

        return image;
    }

}
