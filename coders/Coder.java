package coders;

/**
 * A coder is any class with the ability to transform an array of bytes
 * into a encoded message type, and back to an equivalent array of bytes.
 * @param <T> Message type for coder
 */
public interface Coder<T extends EncodedMessage>  {
    T encode(byte[] input);
    byte[] decode(T input);
}
