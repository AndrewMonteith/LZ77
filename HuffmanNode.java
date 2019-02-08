package coders;

public class HuffmanNode implements Comparable<HuffmanNode> {
    private byte symbol;
    private HuffmanNode left, right;
    private int frequency;

    public byte getSymbol() {
        return symbol;
    }

    public boolean isLeaf() {
        return left == null && right == null;
    }

    public HuffmanNode getLeft() {
        return left;
    }

    public HuffmanNode getRight() {
        return right;
    }

    public HuffmanNode(byte symbol, int frequency) {
        this.symbol = symbol;
        this.frequency = frequency;
    }

    public HuffmanNode(HuffmanNode left, HuffmanNode right) {
        this.left = left;
        this.right = right;
    }

    public int compareTo(HuffmanNode node) {
        return this.frequency - node.frequency;
    }
}