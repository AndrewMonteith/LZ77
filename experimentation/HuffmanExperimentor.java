package experimentation;

import static experimentation.Experimenter.*;
import coders.Huffman;

import java.io.IOException;
import java.util.Map;

public class HuffmanExperimentor {
    private static void printStatsForEncoding(String id, byte[] bytes) {
        var coder = new Huffman();

        var encoded = TimedResult.time(() -> coder.encode(bytes));
        var encodedSize = encoded.getResult().getSize();

        System.out.printf("%s Time %.5f Compression Ratio %.5f \n", id, encoded.getDuration(),
                calculateCompressionRatio(bytes.length, encodedSize));

        byte[] randomBytes = generateRandomSymbols(bytes.length);
        var randomEncoded = TimedResult.time(() -> coder.encode(randomBytes));
        var randomEncodedSize = randomEncoded.getResult().getSize();

        System.out.printf("Unstructured %s Time %.5f Compression Ratio %.5f \n", id, randomEncoded.getDuration(),
                calculateCompressionRatio(bytes.length, randomEncodedSize));
    }

    private static void generateDataForEncoding() throws IOException {
        Map<String, byte[]> testFiles = loadAllTestFiles();

        for (Map.Entry<String, byte[]> testFile : testFiles.entrySet()) {
            byte[] bytes = testFile.getValue();
            printStatsForEncoding(String.valueOf(bytes.length), bytes);
        }
    }

    private static void generateDataForDecoding() throws IOException {
        Map<String, byte[]> testFiles = loadAllTestFiles();
        var coder = new Huffman();

        for (Map.Entry<String, byte[]> testFile : testFiles.entrySet()) {
            byte[] bytes = testFile.getValue();

            var encoded = coder.encode(bytes);

            var timedDecoded = TimedResult.time(() -> Huffman.decode(encoded));
            System.out.printf("Decoded Size %d in %.3f\n", bytes.length, timedDecoded.getDuration());
        }
    }



    public static void main(String[] args) throws IOException {
        generateDataForEncoding();
        generateDataForDecoding();
    }
}