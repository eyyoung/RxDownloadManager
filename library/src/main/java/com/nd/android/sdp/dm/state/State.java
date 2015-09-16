package com.nd.android.sdp.dm.state;

/**
 * The enum State.
 *
 * @author Young
 */
public enum State {
    DOWNLOADING(0),
    PAUSING(1),
    CANCEL(-1),
    FINISHED(2),
    ERROR(-999);

    public int getValue() {
        return mValue;
    }

    private final int mValue;

    State(int value) {
        mValue = value;
    }

    public static State fromInt(int pValue) {
        switch (pValue) {
            case 0:
                return DOWNLOADING;
            case 1:
                return PAUSING;
            case 2:
                return FINISHED;
            case -999:
                return ERROR;
        }
        return CANCEL;
    }
}
