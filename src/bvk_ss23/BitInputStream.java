package bvk_ss23;

import java.io.IOException;
import java.io.InputStream;

public class BitInputStream implements AutoCloseable {
    private InputStream input;
    private int currentByte;
    private int numBitsRemaining;

    public BitInputStream(InputStream input) {
        this.input = input;
        currentByte = 0;
        numBitsRemaining = 0;
    }

    public int read(int bitNumber) throws IOException {
        if (bitNumber < 0 || bitNumber > 32) {
            throw new IllegalArgumentException("bitNumber out of range");
        }

        int value = 0;
        for (int i = 0; i < bitNumber; i++) {
            if (numBitsRemaining == 0) {
                currentByte = input.read();
                if (currentByte == -1) {
                    throw new IOException("End of stream reached");
                }
                numBitsRemaining = 8;
            }

            int bit = (currentByte >> (numBitsRemaining - 1)) & 1;
            value = (value << 1) | bit;
            numBitsRemaining--;
        }
        return value;
    }

    @Override
    public void close() throws IOException {
        input.close();
    }
}