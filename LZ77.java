import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

final class FatIndex {
    public final int index;
    public final int len;

    public FatIndex(int index, int len) {
        this.index = index;
        this.len = len;
    }
}

final class LZ77Triple {
    public final int d;
    public final short l;
    public final byte next;

    public LZ77Triple(int d, short l, byte next) {
        this.d = d;
        this.l = l;
        this.next = next;
    }

    public String toString() {
        return "(" + d + "," + l + "," + next + ")";
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
        var bufferSize = triples.stream().mapToInt(s -> s.l + (s.next == -1 ? 0 : 1)).sum();
        var buffer = new byte[bufferSize];
        var ptr = 0;

        for (var triple : triples) {
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
                result.add(new LZ77Triple(0, (byte) 0, symbols[symbolI]));
            } else {
                var howFarBack = symbolI - prefix.index;
                var nextSymbolI = symbolI + prefix.len;
                var nextSymbol = (nextSymbolI < symbols.length) ? symbols[nextSymbolI] : -1;

                result.add(new LZ77Triple(howFarBack, (short) prefix.len, nextSymbol));
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

    static final class TimedResult<T> {
        public final T result;
        public final double duration;

        public TimedResult(T result, double duration) {
            this.result = result;
            this.duration = duration;
        }
    }

    private static <T> TimedResult<T> time(Supplier<T> f) {
        var now = System.nanoTime();
        var result = f.get();
        return new TimedResult<T>(result, (System.nanoTime() - now) / 1_000_000_000.0);
    }

    private static byte[] generateRandomSymbols(long length) {
        byte[] array = new byte[(int) length];
        new java.util.Random().nextBytes(array);

        // make sure there are no special symbols in there
        // which we assume aren't part of our messages alphabet
        for (var i = 0; i < array.length; ++i) {
            if (array[i] == NO_SYMBOL) {
                array[i] = 0;
            }
        }
        return array;
    }

    private static void testByteData(String id, byte[] data) {
        var coder = new LZ77();

        System.out.println("------------ For " + id);
        var encoded = time(() -> coder.encode(data));
        var decoded = time(() -> LZ77.decode(encoded.result));

        System.out.printf("Encoded in %.3f seconds\n", encoded.duration);
        System.out.printf("Decoded in %.3f seconds\n", decoded.duration);

        var originalSize = data.length;
        var codedSize = encoded.result.size() * 6;

        System.out.printf("Before Compression was %d bytes, after compression %d bytes. Compression Ration %.3f\n",
                originalSize, codedSize, 100 * (1 - (double) codedSize / originalSize));
    }

    private static void generateExperimentationDataForStructured() throws IOException {
        File[] structuredFiles = (new File("./test_data/").listFiles());

        for (var file : structuredFiles) {
            var fileContents = Files.readAllBytes(file.toPath());

            testByteData(file.getName(), fileContents);

            var unstrucutredEquivalent = generateRandomSymbols(file.length());
            testByteData(file.length() + " - unstructured", unstrucutredEquivalent);
        }
    }

    public static void main(String[] args) throws IOException {
        generateExperimentationDataForStructured();
    }
}