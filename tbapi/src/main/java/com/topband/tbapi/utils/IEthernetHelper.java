package com.topband.tbapi.utils;

import androidx.annotation.NonNull;

public interface IEthernetHelper {
    String getIp();

    String getNetmask();

    String getGateway();

    String getDns1();

    String getDns2();

    String getIpAssignment();

    boolean setIp(String ipStr,
                  String netmaskStr,
                  String gatewayStr,
                  String dnsStr1,
                  String dnsStr2,
                  @NonNull String mode);

    boolean setEthEnabled(boolean enable);

    boolean isEthEnabled();
}
