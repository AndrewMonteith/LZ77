package coders;

import java.util.ArrayDeque;
import java.util.Queue;

public class HuffmanEncodedMessage implements EncodedMessage {
    private final HuffmanNode tree;
    private final String message;

    private int getSizeOfTree(HuffmanNode root) {
        int total = 0;

        Queue<HuffmanNode> nodes = new ArrayDeque<>();
        nodes.add(root);

        while (nodes.size() > 0) {
            HuffmanNode node = nodes.remove();

            total += 1;

            HuffmanNode leftChild = node.getLeft(), rightChild = node.getRight();
            if (leftChild != null) {
                nodes.add(leftChild);
            }
            if (rightChild != null) {
                nodes.add(rightChild);
            }
        }
        
        return total;
    }

    public int getSize() {
        int numberOfNodes = getSizeOfTree(tree);

        return (int) Math.ceil(message.length() / 8.0) + numberOfNodes;
    }

    public String getString() {
        return message;
    }

    public HuffmanNode getTree() {
        return tree;
    }

    HuffmanEncodedMessage(final HuffmanNode tree, final String message) {
        this.tree = tree;
        this.message = message;
    }
}
