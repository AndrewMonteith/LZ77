package coders;

/*
    A simple implementation of huffman encoding
*/

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class Huffman {
    public class Node implements Comparable<Node> {
        private byte symbol;
        private Node left, right;
        private int frequency;

        public boolean isLeaf() {
            return left == null && right == null;
        }

        public Node(byte symbol, int frequency) {
            this.symbol = symbol;
            this.frequency = frequency;
        }

        public Node(Node left, Node right) {
            this.left = left;
            this.right = right;
        }

        public int compareTo(Node node) {
            return this.frequency - node.frequency;
        }
    }

    public class EncodedMessage {
        public final Node tree;
        public final String message;

        public EncodedMessage(final Node tree, final String message) {
            this.tree = tree;
            this.message = message;
        }
    }

    private Map<Byte, Integer> countFrequencies(byte[] symbols) {
        var frequencies = new HashMap<Byte, Integer>();

        for (byte symbol : symbols) {
            frequencies.put(symbol, frequencies.getOrDefault(symbol, 0) + 1);
        }

        return frequencies;
    }

    private PriorityQueue<Node> buildLeafNodes(byte[] symbols) {
        var frequencies = countFrequencies(symbols);

        var result = new PriorityQueue<Node>();

        for (var symbolFreqEntry : frequencies.entrySet()) {
            result.add(new Node(symbolFreqEntry.getKey(), symbolFreqEntry.getValue()));
        }

        return result;
    }

    private Node buildHuffmanTree(byte[] symbols) {
        PriorityQueue<Node> nodes = buildLeafNodes(symbols);

        while (nodes.size() > 1) {
            nodes.add(new Node(nodes.remove(), nodes.remove()));
        }

        return nodes.remove();
    }

    private void walkTreeToBuildCodewords(Node node, Map<Byte, String> codewords, String codeword) {
        if (node.isLeaf()) {
            codewords.put(node.symbol, codeword);
        } else {
            if (node.left != null) {
                walkTreeToBuildCodewords(node.left, codewords, codeword + '0');
            }
            if (node.right != null) {
                walkTreeToBuildCodewords(node.right, codewords, codeword + '1');
            }
        }

    }

    private Map<Byte, String> buildCodewordsFromTree(Node tree) {
        Map<Byte, String> codewords = new HashMap<>();

        walkTreeToBuildCodewords(tree, codewords, "");

        return codewords;
    }

    private String encodeMessageUsingCodewords(byte[] symbols, Map<Byte, String> codewords) {
        StringBuilder builder = new StringBuilder();

        for (byte symbol : symbols) {
            builder.append(codewords.get(symbol));
        }

        return builder.toString();
    }

    public EncodedMessage encode(byte[] symbols) {
        Node tree = buildHuffmanTree(symbols);

        Map<Byte, String> codewords = buildCodewordsFromTree(tree);

        String encodedMessage = encodeMessageUsingCodewords(symbols, codewords);

        return new EncodedMessage(tree, encodedMessage);
    }

    public static List<Byte> decode(EncodedMessage encodedMessage) {
        List<Byte> decodedMessage = new ArrayList<>();
        String encodedMsg = encodedMessage.message;

        Node node = encodedMessage.tree;
        for (int i = 0; i < encodedMessage.message.length(); ++i) {
            if (encodedMsg.charAt(i) == '0') {
                node = node.left;
            } else {
                node = node.right;
            }

            if (node.isLeaf()) {
                decodedMessage.add(node.symbol);
                node = encodedMessage.tree;
            }
        }

        return decodedMessage;
    }

    public static void main(String[] args) {
        Huffman huff = new Huffman();

        EncodedMessage message = huff.encode(new byte[] { 65, 65, 65, 66, 66, 67 });
    }

}