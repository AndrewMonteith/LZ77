package coders;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.function.Supplier;
import java.nio.file.Files;
import static experimentation.Experimenter.*;
import experimentation.TimedResult;

class FatIndex {
    public final int index;
    public final int len;

    public FatIndex(int index, int len) {
        this.index = index;
        this.len = len;
    }
}

final class LZ77Triple {
    public final int d;
    public final char l; // char are unsigned byte.
    public final byte next;

    public LZ77Triple(int d, char l, byte next) {
        this.d = d;
        this.l = l;
        this.next = next;
    }

    public String toString() {
        return "(" + d + "," + (int) l + "," + next + ")";
    }
}

public class LZ77 {
    private final int window, lookahead;

    private static final byte NO_SYMBOL = -1;

    /**
     * Decodes an LZ77 encoded message.
     * 
     * @param triples List to decode
     * @return Decoded message
     */
    public static byte[] decode(List<LZ77Triple> triples) {
        var bufferSize = triples.stream().mapToInt(s -> s.l + (s.next == -1 ? 0 : 1)).sum(); // O(n)
        var buffer = new byte[bufferSize];
        var ptr = 0;

        for (var triple : triples) { // O(n)
            if (triple.l > 0) {
                var end = ptr - triple.d + triple.l;
                for (var i = ptr - triple.d; i < end; ++i, ++ptr) {
                    buffer[ptr] = buffer[i];
                }
            }

            if (triple.next != NO_SYMBOL) {
                buffer[ptr] = triple.next;
                ptr += 1;
            }
        }

        return buffer;
    }

    private boolean doSequencesMatch(byte[] symbols, int index1, int index2, int length) {
        var termianteIndex = index1 + length;

        for (; index1 < termianteIndex; ++index1, ++index2) {
            if (symbols[index1] != symbols[index2]) {
                return false;
            }
        }

        return true;
    }

    private FatIndex findLongestPrefixInWindow(byte[] symbols, int symbolI) {
        if (symbolI == 0) {
            return new FatIndex(symbolI, 0);
        }

        final int startOfWindow = Math.max(0, symbolI - window);
        final int endOfLookahead = Math.min(symbols.length, symbolI + lookahead + 1);

        int longestPrefixI = symbolI, longestPrefixLen = 1;
        int windowI = symbolI - 1, curPrefixLen = 1;

        while (startOfWindow <= windowI) {
            var lookingOutsideWindow = windowI + curPrefixLen > symbolI;
            if (lookingOutsideWindow) {
                --windowI;
            } else if (doSequencesMatch(symbols, windowI, symbolI, curPrefixLen)) {
                longestPrefixI = windowI;
                longestPrefixLen = curPrefixLen;

                ++curPrefixLen;

                var lookingOutsideLookahead = symbolI + curPrefixLen > endOfLookahead;
                if (lookingOutsideLookahead) {
                    break;
                }
            } else {
                --windowI;
            }
        }

        return new FatIndex(longestPrefixI, longestPrefixLen);
    }

    /**
     * Encodes an array of symbols
     * 
     * @param symbols symbols to encode
     * @return encoded symbols
     */
    public List<LZ77Triple> encode(byte[] symbols) {
        var result = new ArrayList<LZ77Triple>();

        for (int symbolI = 0; symbolI < symbols.length; ++symbolI) {
            var prefix = findLongestPrefixInWindow(symbols, symbolI);

            if (prefix.index == symbolI) {
                result.add(new LZ77Triple(0, (char) 0, symbols[symbolI]));
            } else {
                var howFarBack = symbolI - prefix.index;
                var nextSymbolI = symbolI + prefix.len;
                var nextSymbol = (nextSymbolI < symbols.length) ? symbols[nextSymbolI] : -1;

                result.add(new LZ77Triple(howFarBack, (char) prefix.len, nextSymbol));
                symbolI += prefix.len;
            }
        }

        return result;
    }

    /**
     * Encodes a given string
     * 
     * @param s string to encode
     * @return encoded string
     */
    public List<LZ77Triple> encode(String s) {
        return encode(s.getBytes());
    }

    /**
     * Create LZ77 with window size 65535 and lookahead size 255
     */
    public LZ77() {
        this(65535, 255);
    }

    /**
     * Create new LZ77 coder with specific window and lookahead sizes.
     */
    public LZ77(int window, int lookahead) {
        this.window = window;
        this.lookahead = lookahead;
    }

    // ------------------- Experimentation Logic.

    private static void testByteData(String id, byte[] data) {
        var coder = new LZ77();

        System.out.println("------------ For " + id);
        var encoded = TimedResult.time(() -> coder.encode(data));
        var triples = encoded.getResult();

        var decoded = TimedResult.time(() -> LZ77.decode(triples));

        System.out.printf("Encoded in %.3f seconds\n", encoded.getDuration());
        System.out.printf("Decoded in %.6f seconds\n", decoded.getDuration());

        var originalSize = data.length;
        var codedSize = triples.size() * 6;

        System.out.printf("Before Compression was %d bytes, after compression %d bytes. Compression Ration %.3f\n",
                originalSize, codedSize, calculateCompressionRate(originalSize, codedSize));
    }

    private static void generateDataForTimingsAndCompression() throws IOException {
        File[] structuredFiles = (new File("./test_data/").listFiles());

        for (var file : structuredFiles) {
            var fileContents = Files.readAllBytes(file.toPath());

            testByteData(file.getName(), fileContents);

            var unstrucutredEquivalent = generateRandomSymbols((int) file.length());
            testByteData(file.length() + " - unstructured", unstrucutredEquivalent);
        }
    }

    private static void generateDataForVaryingParamters() throws IOException {
        byte[][] testBytes = { Files.readAllBytes(new File("./test_data/ptt5").toPath()),
                Files.readAllBytes(new File("./test_data/plrabn12.txt").toPath()),
                Files.readAllBytes(new File("./test_data/world192.txt").toPath()) };

        var W = 65535;
        System.out.println("Fixed W:" + W);
        for (var bytes : testBytes) {
            System.out.println("For Length " + bytes.length);
            for (var L = 3; L <= 11; ++L) {
                var lookahead = (int) Math.pow(2, L);
                var coder = new LZ77(W, lookahead);

                var encoded = TimedResult.time(() -> coder.encode(bytes));

                System.out.printf("L: %d Time:%.3f\n", lookahead, encoded.getDuration());
            }
        }

        var L = 255;
        System.out.println("Fixed L:" + L);
        for (var bytes : testBytes) {
            System.out.println("-------" + bytes.length);

            for (W = 18; W <= 25; ++W) {
                var window = (int) Math.pow(2, W);
                var coder = new LZ77(window, L);

                var encoded = TimedResult.time(() -> coder.encode(bytes));

                System.out.printf("W: %d Compression:%.3f\n", window,
                        calculateCompressionRate(bytes.length, encoded.getResult().size() * 6));
            }
        }

    }

    private static void generateDataForDecoder() throws IOException {
        File[] testFiles = (new File("./test_data").listFiles());
        var coder = new LZ77(65535, 255);

        for (var file : testFiles) {
            var bytes = Files.readAllBytes(file.toPath());
            var randomBytes = generateRandomSymbols(bytes.length);

            var encodedStructured = coder.encode(bytes);
            var encodedUnstructured = coder.encode(randomBytes);

            var decoderStructuredTime = TimedResult.time(() -> LZ77.decode(encodedStructured));
            var decoderUnstructuredTime = TimedResult.time(() -> LZ77.decode(encodedUnstructured));

            System.out.println(file.length() + "   " + decoderStructuredTime.getDuration() + "   "
                    + decoderUnstructuredTime.getDuration());
        }
    }

    public static void main(String[] args) throws IOException {
        // generateDataForTimingsAndCompression();
        generateDataForVaryingParamters();
        // generateDataForDecoder();
    }
}
