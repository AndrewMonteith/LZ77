package coders;

import java.util.*;

/**
 * Huffman Coder
 */
public class Huffman implements Coder<HuffmanEncodedMessage> {

    /**
     * Computes a frequency distribution of a given sequence of symbols
     * @param symbols Sequence of symbols
     * @return Frequency Distribution
     */
    private Map<Byte, Integer> countFrequencies(byte[] symbols) {
        var frequencies = new HashMap<Byte, Integer>();

        for (byte symbol : symbols) {
            frequencies.put(symbol, frequencies.getOrDefault(symbol, 0) + 1);
        }

        return frequencies;
    }

    /**
     * Creates the leaf nodes for a huffman tree from a given sequence of symbols
     * @param symbols Sequence of symbols
     * @return Priority queue of huffman nodes based on frequency of node
     */
    private PriorityQueue<HuffmanNode> buildLeafNodes(byte[] symbols) {
        var frequencies = countFrequencies(symbols);

        var result = new PriorityQueue<HuffmanNode>(frequencies.size());

        for (var symbolFreqEntry : frequencies.entrySet()) {
            result.add(new HuffmanNode(symbolFreqEntry.getKey(), symbolFreqEntry.getValue()));
        }

        return result;
    }

    /**
     * Create a huffman tree from a given sequence of symbols
     * @param symbols Sequence of symbols
     * @return Root node of huffman tree
     */
    private HuffmanNode buildHuffmanTree(byte[] symbols) {
        PriorityQueue<HuffmanNode> nodes = buildLeafNodes(symbols);

        while (nodes.size() > 1) {
            nodes.add(new HuffmanNode(nodes.remove(), nodes.remove()));
        }

        return nodes.remove();
    }

    private void walkTreeToBuildCodewords(HuffmanNode node, Map<Byte, String> codewords, String codeword) {
        if (node.isLeaf()) {
            codewords.put(node.getSymbol(), codeword);
        } else {
            HuffmanNode leftChild = node.getLeft(), rightChild = node.getRight();

            if (leftChild != null) {
                walkTreeToBuildCodewords(leftChild, codewords, codeword + '0');
            }
            if (rightChild != null) {
                walkTreeToBuildCodewords(rightChild, codewords, codeword + '1');
            }
        }

    }

    /**
     * Create the codewords of the code from a huffman tree
     * @param tree Huffman tree
     * @return All codewords in the code
     */
    private Map<Byte, String> buildCodewordsFromTree(HuffmanNode tree) {
        Map<Byte, String> codewords = new HashMap<>();

        walkTreeToBuildCodewords(tree, codewords, "");

        return codewords;
    }

    /**
     * Encodes a sequences of bytes using the codewords of a huffman code into a string of 0 & 1's
     * @param symbols Sequence of bytes to encode
     * @param codewords codewords of the huffman code
     * @return raw string of encoded message
     */
    private String encodeMessageUsingCodewords(byte[] symbols, Map<Byte, String> codewords) {
        StringBuilder builder = new StringBuilder();

        for (byte symbol : symbols) {
            builder.append(codewords.get(symbol));
        }

        return builder.toString();
    }

    /**
     * Encodes a sequence of bytes into a encoded message
     * @param symbols sequence of bytes
     * @return encoded huffman message
     */
    public HuffmanEncodedMessage encode(byte[] symbols) {
        HuffmanNode tree = buildHuffmanTree(symbols);

        Map<Byte, String> codewords = buildCodewordsFromTree(tree);

        String encodedMessage = encodeMessageUsingCodewords(symbols, codewords);

        return new HuffmanEncodedMessage(tree, encodedMessage);
    }

    private byte[] byteListToArray(List<Byte> bytes) {
        byte[] result = new byte[bytes.size()];
        for (var i = 0; i < result.length; ++i) {
            result[i] = bytes.get(i);
        }
        return result;
    }

    /**
     * Decodes an encoded message to it's equivalent sequence of bytes
     * @param encodedMessage Encoded message to decode
     * @return Equivalent sequence of bytes
     */
    public byte[] decode(HuffmanEncodedMessage encodedMessage) {
        List<Byte> decodedMessage = new ArrayList<>();
        String encodedMsg = encodedMessage.getString();

        HuffmanNode node = encodedMessage.getTree();
        for (int i = 0; i < encodedMsg.length(); ++i) {
            if (encodedMsg.charAt(i) == '0') {
                node = node.getLeft();
            } else {
                node = node.getRight();
            }

            if (node.isLeaf()) {
                decodedMessage.add(node.getSymbol());
                node = encodedMessage.getTree();
            }
        }

        return byteListToArray(decodedMessage);
    }

}