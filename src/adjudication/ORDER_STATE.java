package adjudication;

public enum ORDER_STATE {

    UNRESOLVED((byte)0),
    GUESSING((byte)1),
    RESOLVED((byte)2);

    ORDER_STATE(byte state) {
    }

}