package com.dongah.smartcharger.utils;

import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

public class MacAddressFromInterface {

    private String getMacAddressFromInterface() {
        try {
            List<NetworkInterface> interfaces =
                    Collections.list(NetworkInterface.getNetworkInterfaces());

            for (NetworkInterface nif : interfaces) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) return null;

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(String.format("%02X:", b));
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }

                return res1.toString();

            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;

    }
}
