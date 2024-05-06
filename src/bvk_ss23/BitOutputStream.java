package bvk_ss23;

import java.io.IOException;
import java.io.OutputStream;

public class BitOutputStream {
    private OutputStream outputStream;
    private int buffer;
    private int bufferCount;

    public BitOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
        this.buffer = 0;
        this.bufferCount = 0;
    }

    public void write(int value, int bitNumber) throws IOException {
        if (bitNumber < 0 || bitNumber > 32) {
            throw new IllegalArgumentException("Bit number must be between 0 and 32");
        }

        int mask = (1 << bitNumber) - 1;
        value &= mask; // Keep only the specified number of least significant bits

        buffer |= (value << bufferCount);
        bufferCount += bitNumber;

        while (bufferCount >= 8) {
            outputStream.write(buffer & 0xFF); // Write the least significant byte
            buffer >>= 8; // Shift out the written byte
            bufferCount -= 8;
        }
    }

    public void close() throws IOException {
        if (bufferCount > 0) {
            // Fill the buffer with zeros if less than 8 bits are remaining
            buffer <<= (8 - bufferCount);
            outputStream.write(buffer & 0xFF);
        }
        outputStream.close();
    }
}