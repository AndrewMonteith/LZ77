package coders;

import coders.EncodedMessage;
import coders.LZ77;

import java.util.List;

public class LZ77CodedMessage implements EncodedMessage {
    private List<LZ77Triple> triples;

    public LZ77Triple getTriple(int i) {
        return triples.get(i);
    }

    public int getNumberOfTriples() {
        return triples.size();
    }

    public int getSize() {
        return triples.size() * 6;
    }

    public int getDecodedSize() {
        return triples.stream().mapToInt(s -> s.l + (s.next == -1 ? 0 : 1)).sum();
    }

    public LZ77CodedMessage(List<LZ77Triple> triples) {
        this.triples = triples;
    }
}