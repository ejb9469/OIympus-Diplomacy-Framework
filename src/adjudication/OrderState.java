package adjudication;

public enum OrderState {

    UNRESOLVED((byte)0),
    GUESSING((byte)1),
    RESOLVED((byte)2);

    OrderState(byte state) {
    }

}