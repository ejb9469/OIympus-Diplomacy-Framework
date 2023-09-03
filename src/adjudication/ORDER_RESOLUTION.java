package adjudication;

public enum ORDER_RESOLUTION {

    FAILS((byte)0),
    SUCCEEDS((byte)1),
    UNRESOLVED((byte)2);

    ORDER_RESOLUTION(byte resolution) {
    }

}