package coders;

/**
 * A EncodedMessage will represented a encoded sequence of bytes,
 * but one can query the final binary size of that message
 */
public interface EncodedMessage {
    int getSize();
}