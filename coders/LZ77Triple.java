package coders;

public final class LZ77Triple {
    public final int d;
    public final char l; // char are unsigned byte.
    public final byte next;

    public LZ77Triple(int d, char l, byte next) {
        this.d = d;
        this.l = l;
        this.next = next;
    }

    public String toString() {
        return "(" + d + "," + (int) l + "," + next + ")";
    }
}
