package dip;

public enum OrderResolution {

    FAILS((byte)0),
    SUCCEEDS((byte)1),
    UNRESOLVED((byte)2);

    OrderResolution(byte resolution) {
    }

}