package experimentation;

import java.util.Map;
import java.io.IOException;

import static experimentation.Experimenter.*;

import coders.LZ77;

public class LZ77Experimentor {
    private static void testByteData(String id, byte[] data) {
        var coder = new LZ77();

        System.out.println("------------ For " + id);
        var encoded = TimedResult.time(() -> coder.encode(data));
        var encodedMessage = encoded.getResult();

        var decoded = TimedResult.time(() -> coder.decode(encodedMessage));

        System.out.printf("Encoded in %.3f seconds\n", encoded.getDuration());
        System.out.printf("Decoded in %.6f seconds\n", decoded.getDuration());

        var originalSize = data.length;
        var codedSize = encodedMessage.getSize();

        System.out.printf("Before Compression was %d bytes, after compression %d bytes. Compression Ration %.3f\n",
                originalSize, codedSize, calculateCompressionRatio(originalSize, codedSize));
    }

    private static void generateDataForTimingsAndCompression() throws IOException {
        Map<String, byte[]> testFiles = loadAllTestFiles();

        for (Map.Entry<String, byte[]> testFile : testFiles.entrySet()) {
            byte[] contents = testFile.getValue();

            testByteData(testFile.getKey(), contents);

            var unstrucutredEquivalent = generateRandomSymbols(contents.length);
            testByteData(contents.length + " - unstructured", unstrucutredEquivalent);
        }
    }

    private static void generateDataForVaryingParamters() throws IOException {
        Map<String, byte[]> testFiles = loadTestFiles("ptt5", "plrabn12.txt", "world192.txt");

        var W = 65535;
        System.out.println("Fixed W:" + W);
        for (Map.Entry<String, byte[]> testFile : testFiles.entrySet()) {
            byte[] bytes = testFile.getValue();

            System.out.println("-------- " + bytes.length);
            for (var L = 3; L <= 11; ++L) {
                var lookahead = (int) Math.pow(2, L);
                var coder = new LZ77(W, lookahead);

                var encoded = TimedResult.time(() -> coder.encode(bytes));

//                System.out.printf("L: %d Time:%.3f\n", lookahead, encoded.getDuration());
                System.out.printf("L: %d Compression:%.3f\n", lookahead, calculateCompressionRatio(bytes.length, encoded.getResult().getSize()));
            }

        }

        var L = 255;
        System.out.println("Fixed L:" + L);
        for (Map.Entry<String, byte[]> testFile : testFiles.entrySet()) {
            byte[] bytes = testFile.getValue();
            System.out.println("-------" + bytes.length);

            for (W = 18; W <= 25; ++W) {
                var window = (int) Math.pow(2, W);
                var coder = new LZ77(window, L);

                var encoded = TimedResult.time(() -> coder.encode(bytes));

                System.out.printf("W: %d Compression:%.3f\n", window,
                        calculateCompressionRatio(bytes.length, encoded.getResult().getSize()));
            }
        }
    }

    public static void main(String[] args) throws IOException {
//        generateDataForTimingsAndCompression();
        generateDataForVaryingParamters();
    }

}