package coders;

import coders.EncodedMessage;
import coders.LZ77;

import java.util.List;

public class LZ77CodedMessage implements EncodedMessage {
    private List<LZ77Triple> triples;
    private int sizeOfDecodedMessage;

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
        return sizeOfDecodedMessage;
    }

    public LZ77CodedMessage(List<LZ77Triple> triples, int sizeOfDecodedMessage) {
        this.triples = triples;
        this.sizeOfDecodedMessage = sizeOfDecodedMessage;
    }
}