package experimentation;

import static experimentation.Experimenter.*;
import coders.Huffman;

import java.io.IOException;
import java.util.Map;

public class HuffmanExperimentor {
    private static void printStatsForData(String id, byte[] bytes) {
        var coder = new Huffman();

        System.out.println("For " + id);
        var encoded = TimedResult.time(() -> coder.encode(bytes));
        var encodedSize = encoded.getResult().getSize();

        System.out.printf("Time %.3f Compression Ratio %.3f \n", encoded.getDuration(),
                calculateCompressionRatio(bytes.length, encodedSize));

        System.out.println("For Unstructured " + id);

        byte[] randomBytes = generateRandomSymbols(bytes.length);
        var randomEncoded = TimedResult.time(() -> coder.encode(randomBytes));
        var randomEncodedSize = randomEncoded.getResult().getSize();

        System.out.printf("Time %.3f Compression Ratio %.3f \n", randomEncoded.getDuration(),
                calculateCompressionRatio(bytes.length, randomEncodedSize));
    }

    private static void generateData() throws IOException {
        Map<String, byte[]> testFiles = loadAllTestFiles();

        for (Map.Entry<String, byte[]> testFile : testFiles.entrySet()) {
            byte[] bytes = testFile.getValue();
            printStatsForData(String.valueOf(bytes.length), bytes);
        }
    }

    public static void main(String[] args) throws IOException {
        generateData();
    }
}