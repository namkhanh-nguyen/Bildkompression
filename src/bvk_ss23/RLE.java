// BVK Ue1 SS2023 Vorgabe
//
// Copyright (C) 2023 by Klaus Jung
// All rights reserved.
// Date: 2023-03-27

package bvk_ss23;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;

public class RLE
{

    public static void encodeImage(RasterImage image, DataOutputStream out)
        throws IOException
    {

        out.writeInt(image.width);
        out.writeInt(image.height);

        // Create a color map to store colors
        LinkedHashMap<Integer, Integer> colorMap = new LinkedHashMap<>();
        int colorIndex = 0;

        // fill the color map, make color palette
        for (int pixel : image.argb)
            if (!colorMap.containsKey(Integer.valueOf(pixel)))
            {
                colorIndex++;
                colorMap.put(Integer.valueOf(pixel), Integer.valueOf(colorIndex));
            }
        // number of colors and color palette
        out.writeInt(colorMap.size());
        for (int pixel : colorMap.keySet())
            out.writeInt(pixel);

        Integer currentColor = Integer.valueOf(image.argb[0]);
        int runLength = 1;

        for (int i = 1; i < image.argb.length; i++)
        {
            Integer nextColor = Integer.valueOf(image.argb[i]);

            if (currentColor == nextColor && runLength < 255)
                runLength++;
            else
            {
                out.writeByte(colorMap.get(currentColor).intValue());
                out.writeByte(runLength - 1);
                // Update new color
                currentColor = nextColor;
                runLength = 1;
            }
        }

        out.writeByte(colorMap.get(currentColor).intValue());
        out.writeByte(runLength - 1);

    }

    public static RasterImage decodeImage(DataInputStream in)
        throws IOException
    {
        int width = in.readInt();
        int height = in.readInt();

        // Read palette size and colors
        int paletteSize = in.readInt();
        int[] colors = new int[paletteSize];
        for (int i = 0; i < paletteSize; i++)
            colors[i] = in.readInt();

        // Reconstruct RasterImage
        RasterImage image = new RasterImage(width, height);
        int pixelIndex = 0;
        while (pixelIndex < width * height)
        {
            int colorIndex = in.readByte() & 0xFF; // Convert byte to int
            int color = colors[colorIndex];
            int runLength = in.readByte() & 0xFF; // Convert byte to int
            for (int i = 0; i <= runLength && pixelIndex < width * height; i++)
                image.argb[pixelIndex++] = color;
        }
        return image;
    }

}
