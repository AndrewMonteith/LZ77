package experimentation;

import java.util.Random;

public class Experimenter {
    public static double calculateCompressionRate(int originalSize, int codedSize) {
        return 100.0 * ((double) originalSize / codedSize);
    }

    public static byte[] generateRandomSymbols(int size) {
        byte[] result = new byte[size];
        Random r = new Random();

        for (int i = 0; i < size; ++i) {
            result[i] = (byte) r.nextInt(128);
        }

        return result;
    }

    private static boolean arraysEqual(byte[] bytes1, byte[] bytes2) {
        if (bytes1.length != bytes2.length) {
            return false;
        }

        for (var i = 0; i < bytes1.length; ++i) {
            if (bytes1[i] != bytes2[i]) {
                return false;
            }
        }

        return true;
    }

}