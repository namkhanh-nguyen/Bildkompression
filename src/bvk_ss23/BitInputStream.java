package bvk_ss23;

import java.io.IOException;
import java.io.InputStream;

public class BitInputStream {
    private InputStream inputStream;
    private int buffer;
    private int bufferCount;

    public BitInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
        this.buffer = 0;
        this.bufferCount = 0;
    }
    
    public int getBufferCount()
    {
    	return this.bufferCount;
    }

    public int read(int bitNumber) throws IOException {
        if (bitNumber < 0 || bitNumber > 32) {
            throw new IllegalArgumentException("Bit number must be between 0 and 32");
        }

        int result = 0;
        while (bitNumber > 0) {
            if (bufferCount == 0) {
                buffer = inputStream.read();
                if (buffer == -1) {
                    throw new IOException("End of stream reached");
                }
                bufferCount = 8;
            }

            int bitsToRead = Math.min(bufferCount, bitNumber);
            int mask = (1 << bitsToRead) - 1;
            int value = buffer & mask;
            result |= value << (bitNumber - bitsToRead);

            buffer >>= bitsToRead;
            bufferCount -= bitsToRead;
            bitNumber -= bitsToRead;
        }

        return result;
    }

    public void close() throws IOException {
        inputStream.close();
    }
}