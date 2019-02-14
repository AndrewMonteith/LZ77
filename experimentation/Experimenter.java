package experimentation;

import coders.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

class Experimenter {
    static double calculateCompressionRatio(int originalSize, int codedSize) {
        return 100.0 * ((double) originalSize / codedSize);
    }

    static byte[] generateRandomSymbols(int size) {
        byte[] result = new byte[size];
        Random r = new Random();

        for (int i = 0; i < size; ++i) {
            result[i] = (byte) r.nextInt(128);
        }

        return result;
    }

    private static byte[] loadTestFileContents(String fileName) throws IOException {
        return Files.readAllBytes(Paths.get("./test_data/" + fileName));
    }

    private static byte[] loadTestFileContents(File f) throws IOException {
        return Files.readAllBytes(f.toPath());
    }

    static Map<String, byte[]> loadTestFiles(String... fileNames) throws IOException {
        Map<String, byte[]> result = new HashMap<>();

        for (String fileName : fileNames) {
            byte[] contents = loadTestFileContents(fileName);
            result.put(fileName, contents);
        }

        return result;
    }

    static Map<String, byte[]> loadAllTestFiles() throws IOException {
        Map<String, byte[]> result = new HashMap<>();

        File testFolder = new File("./test_data");

        for (File testFile : testFolder.listFiles()) {
            byte[] contents = loadTestFileContents(testFile);
            result.put(testFile.getName(), contents);
        }

        return result;
    }

    private static void generateDataForVariousFormats(String encodeId, Coder encoder) throws IOException {
        Map<String, byte[]> testFiles = loadTestFiles("ptt5", "bible.txt", "fields.c", "tree.jpg", "video.mp4", "audio.mp3");

        System.out.println("Testing:" + encodeId);
        for (Map.Entry<String, byte[]> testFile : testFiles.entrySet()) {
            byte[] bytes = testFile.getValue();

            var encodedResult = TimedResult.time(() -> encoder.encode(bytes));

            System.out.println("For file:" + testFile.getKey() + " " + bytes.length);
            System.out.printf("Encoded in: %.6f\n", encodedResult.getDuration());
            System.out.println("Compression Ratio:" +
                    calculateCompressionRatio(bytes.length, encodedResult.getResult().getSize()));
        }
    }

    private static <T extends EncodedMessage> void generateDataForDecoding(String coderId, Coder<T> coder) throws IOException {
        Map<String, byte[]> testFiles = loadAllTestFiles();

        System.out.println("Decoding Data for " + coderId);
        for (Map.Entry<String, byte[]> testFile : testFiles.entrySet()) {
            byte[] structuredBytes = testFile.getValue();
            byte[] unstructuredBytes = generateRandomSymbols(structuredBytes.length);

            T encodedStructured = coder.encode(structuredBytes);
            T encodedUnstructured = coder.encode(unstructuredBytes);

            var timedStructuredDecode = TimedResult.time(() -> coder.decode(encodedStructured));
//            var timedUnstructuredDecode = TimedResult.time(() -> coder.decode(encodedUnstructured));
            System.out.printf("%d => %d  :  %.7f\n", structuredBytes.length, encodedStructured.getSize(), timedStructuredDecode.getDuration());

//            System.out.printf("Structured     %d  :  %.7f\n", encodedStructured.getSize(), timedStructuredDecode.getDuration());
//            System.out.printf("Unstructured   %d  :  %.7f\n", encodedUnstructured.getSize(), timedUnstructuredDecode.getDuration());
        }
    }

    public static void main(String[] args) throws IOException {
        generateDataForDecoding("------------ Huffman", new Huffman());
//        generateDataForDecoding("------------ LZ77", new LZ77());
    }
}