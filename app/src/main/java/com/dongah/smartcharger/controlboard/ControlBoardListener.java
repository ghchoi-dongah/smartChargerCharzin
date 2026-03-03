package com.dongah.smartcharger.controlboard;

public interface ControlBoardListener {
    void onControlBoardReceive(RxData rxData);

    void onControlBoardSend(TxData txData);
}
