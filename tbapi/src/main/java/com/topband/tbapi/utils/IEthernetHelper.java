package com.topband.tbapi.utils;

import androidx.annotation.NonNull;

public interface IEthernetHelper {
    String getIp(String iface);

    String getNetmask(String iface);

    String getGateway(String iface);

    String getDns1(String iface);

    String getDns2(String iface);

    String getIpAssignment(String iface);

    boolean setIp(String iface,
                  String ipStr,
                  String netmaskStr,
                  String gatewayStr,
                  String dnsStr1,
                  String dnsStr2,
                  @NonNull String mode);

    boolean setEnabled(String iface, boolean enable);

    boolean isEnabled(String iface);
}
