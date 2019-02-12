package coders;

public interface Coder<T extends EncodedMessage>  {
    T encode(byte[] input);
    byte[] decode(T input);
}
