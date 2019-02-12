package coders;

public interface Encoder {
    EncodedMessage encode(byte[] input);
}
