package coders;

import java.io.FileNotFoundException;
import java.util.ArrayList;

class FatIndex {
    public final int index;
    public final int len;

    FatIndex(int index, int len) {
        this.index = index;
        this.len = len;
    }
}

/**
 * LZ77 Coder
 */
public class LZ77 implements Coder <LZ77CodedMessage> {
    private final int window, lookahead;

    /**
     * Decodes an LZ77 coded message into it's equivalent sequence of bytes
     * @param encodedMessage instance of the encoded message
     * @return equivalent sequence of bytes
     */
    public byte[] decode(LZ77CodedMessage encodedMessage) {
        var bufferSize = encodedMessage.getDecodedSize();
        var buffer = new byte[bufferSize];
        var ptr = 0;

        for (int n = 0; n < encodedMessage.getNumberOfTriples(); ++n) {
            var triple = encodedMessage.getTriple(n);

            if (triple.l > 0) {
                var end = ptr - triple.d + triple.l;
                for (var i = ptr - triple.d; i < end; ++i, ++ptr) {
                    buffer[ptr] = buffer[i];
                }
            }

            if (ptr != encodedMessage.getDecodedSize()) { // final triple will have no symbol
                buffer[ptr] = triple.next;
                ptr += 1;
            }
        }

        return buffer;
    }

    private boolean doSubsequencesMatch(byte[] symbols, int index1, int index2, int length) {
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
            } else if (doSubsequencesMatch(symbols, windowI, symbolI, curPrefixLen)) {
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
     * Encodes symbols using LZ77 coding
     * @param symbols symbols to encode
     * @return equivalent encoded message
     */
    public LZ77CodedMessage encode(byte[] symbols) {
        var triples = new ArrayList<LZ77Triple>();

        for (int symbolI = 0; symbolI < symbols.length; ++symbolI) {
            var prefix = findLongestPrefixInWindow(symbols, symbolI);

            if (prefix.index == symbolI) {
                triples.add(new LZ77Triple(0, (char) 0, symbols[symbolI]));
            } else {
                var howFarBack = symbolI - prefix.index;
                var nextSymbolI = symbolI + prefix.len;
                var nextSymbol = (nextSymbolI < symbols.length) ? symbols[nextSymbolI] : -1;

                triples.add(new LZ77Triple(howFarBack, (char) prefix.len, nextSymbol));
                symbolI += prefix.len;
            }
        }

        return new LZ77CodedMessage(triples, symbols.length);
    }

    public LZ77() {
        this(65535, 255);
    }

    public LZ77(int window, int lookahead) {
        this.window = window;
        this.lookahead = lookahead;
    }


    public static void main(String[] args) throws FileNotFoundException {
        LZ77ProgramInterface program = new LZ77ProgramInterface(args);

        if (program.wantsToDecode()) {
            program.decode();
        } else if (program.wantsToEncode()) {
            program.encode();
        } else {
            program.printUsage();
        }

    }
}
