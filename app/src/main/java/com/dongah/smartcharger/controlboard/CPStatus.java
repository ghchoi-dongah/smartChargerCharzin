package com.dongah.smartcharger.controlboard;

import java.util.HashMap;
import java.util.Map;

public class CPStatus {

    Map<Integer, String> statusMap;


    public CPStatus() {
        statusMap = new HashMap<>();

        statusMap.put(0, "STATE_A 12V");
        statusMap.put(1, "STATE_B 9V");
        statusMap.put(2, "STATE_C 6V");
        statusMap.put(3, "STATE_D 3V");
        statusMap.put(4, "STATE_E 0V");
        statusMap.put(5, "STATE_E -12V");
    }

}
