
package bvk_ss23;

import java.io.IOException;
import java.io.OutputStream;

public class BitOutputStream implements AutoCloseable
{

    private OutputStream output;
    private int currentByte;
    private int numBitsInCurrentByte;

    public BitOutputStream(OutputStream output)
    {
        this.output = output;
        this.currentByte = 0;
        this.numBitsInCurrentByte = 0;
    }

    public void write(int value, int bitNumber)
        throws IOException
    {
        if (bitNumber < 0 || bitNumber > 32)
            throw new IllegalArgumentException("bitNumber out of range");

        for (int i = 0; i < bitNumber; i++)
        {
            int bit = value >> bitNumber - i - 1 & 1;
            this.currentByte = this.currentByte << 1 | bit;
            this.numBitsInCurrentByte++;
            if (this.numBitsInCurrentByte == 8)
                flush();
        }
    }

    private void flush()
        throws IOException
    {
        if (this.numBitsInCurrentByte > 0)
        {
            this.currentByte = this.currentByte << 8 - this.numBitsInCurrentByte;
            this.output.write(this.currentByte);
            this.numBitsInCurrentByte = 0;
            this.currentByte = 0;
        }
    }

    @Override
    public void close()
        throws IOException
    {
        flush();
        this.output.close();
    }
}
