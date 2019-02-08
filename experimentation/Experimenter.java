package experimentation;

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
}