import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

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
    public final int l;
    public final byte next;

    public LZ77Triple(int d, int l, byte next) {
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

    public static byte[] decode(List<LZ77Triple> triples) {
        var bufferSize = triples.stream().mapToInt(s -> s.l + (s.next == -1 ? 0 : 1)).sum();
        var result = new byte[bufferSize];
        var ptr = 0;

        for (var triple : triples) {
            if (triple.l > 0) {
                var end = ptr - triple.d + triple.l;
                for (var i = ptr - triple.d; i < end; ++i, ++ptr) {
                    result[ptr] = result[i];
                }
            }

            if (triple.next != -1) {
                result[ptr] = triple.next;
                ptr += 1;
            }
        }

        return result;
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

    public List<LZ77Triple> encode(byte[] symbols) {
        var result = new ArrayList<LZ77Triple>();

        for (int symbolI = 0; symbolI < symbols.length; ++symbolI) {
            var prefix = findLongestPrefixInWindow(symbols, symbolI);

            if (prefix.index == symbolI) {
                result.add(new LZ77Triple(0, 0, symbols[symbolI]));
            } else {
                var howFarBack = symbolI - prefix.index;

                var nextSymbolI = symbolI + prefix.len;
                var nextSymbol = (nextSymbolI < symbols.length) ? symbols[nextSymbolI] : -1;

                result.add(new LZ77Triple(howFarBack, prefix.len, nextSymbol));
                symbolI += prefix.len;
            }
        }

        return result;
    }

    public List<LZ77Triple> encode(String s) {
        return encode(s.getBytes());
    }

    public List<LZ77Triple> encode(Path path) throws java.io.IOException {
        return encode(Files.readAllBytes(path));
    }

    public LZ77() {
        this(65535, 255);
    }

    public LZ77(int window, int lookahead) {
        this.window = window;
        this.lookahead = lookahead;
    }

    public static boolean checkString(String s) {
        var coder = new LZ77();

        var encoded = coder.encode(s);
        var decoded = LZ77.decode(encoded);

        return (new String(decoded)).equals(s);
    }

    public static void encodeFile(String fileName) throws java.io.IOException {
        var coder = new LZ77();

        var result = coder.encode(Paths.get("./test_data", fileName));

        System.out.println("Encoded with " + result.size() + " triples");
    }

    public static void main(String[] args) throws IOException {

    }
}