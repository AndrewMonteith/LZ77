package coders;

import java.util.List;
import java.util.ArrayList;

class FatIndex {
    public final int index;
    public final int len;

    public FatIndex(int index, int len) {
        this.index = index;
        this.len = len;
    }
}

public class LZ77 {
    private final int window, lookahead;

    private static final byte NO_SYMBOL = -1;

    public static final class Triple {
        public final int d;
        public final char l; // char are unsigned byte.
        public final byte next;

        public Triple(int d, char l, byte next) {
            this.d = d;
            this.l = l;
            this.next = next;
        }

        public String toString() {
            return "(" + d + "," + (int) l + "," + next + ")";
        }
    }

    /**
     * Decodes an LZ77 encoded message.
     * 
     * @param triples List to decode
     * @return Decoded message
     */
    public static byte[] decode(List<Triple> triples) {
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
    public List<Triple> encode(byte[] symbols) {
        var result = new ArrayList<Triple>();

        for (int symbolI = 0; symbolI < symbols.length; ++symbolI) {
            var prefix = findLongestPrefixInWindow(symbols, symbolI);

            if (prefix.index == symbolI) {
                result.add(new Triple(0, (char) 0, symbols[symbolI]));
            } else {
                var howFarBack = symbolI - prefix.index;
                var nextSymbolI = symbolI + prefix.len;
                var nextSymbol = (nextSymbolI < symbols.length) ? symbols[nextSymbolI] : -1;

                result.add(new Triple(howFarBack, (char) prefix.len, nextSymbol));
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
    public List<Triple> encode(String s) {
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
}
