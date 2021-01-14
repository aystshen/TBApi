package com.topband.tbapi.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

public class EthernetHelper {
    private static final String TAG = "EthernetHelper";

    private Object mEthManagerObj;

    @SuppressLint("PrivateApi")
    public EthernetHelper(Context context) {
        try {
            String service = (String) Context.class.getField("ETHERNET_SERVICE").get(null);
            mEthManagerObj = context.getSystemService(service);
        } catch (Exception e) {
            Log.e(TAG, "EthernetHelper, " + e.getMessage());
        }
    }

    /**
     * 获取IP
     *
     * @return IP
     */
    public String getIp() {
        if (mEthManagerObj != null) {
            try {
                Method method = mEthManagerObj.getClass().getDeclaredMethod("getIpAddress");
                method.setAccessible(true);
                return (String) method.invoke(mEthManagerObj);
            } catch (Exception e) {
                Log.e(TAG, "getIp, " + e.getMessage());
            }
        }

        return "";
    }

    /**
     * 获取子网掩码
     *
     * @return 子网掩码
     */
    public String getNetmask() {
        if (mEthManagerObj != null) {
            try {
                Method method = mEthManagerObj.getClass().getDeclaredMethod("getNetmask");
                method.setAccessible(true);
                return (String) method.invoke(mEthManagerObj);
            } catch (Exception e) {
                Log.e(TAG, "getNetmask, " + e.getMessage());
            }
        }

        return "";
    }

    /**
     * 获取网关
     *
     * @return 网关
     */
    public String getGateway() {
        if (mEthManagerObj != null) {
            try {
                Method method = mEthManagerObj.getClass().getDeclaredMethod("getGateway");
                method.setAccessible(true);
                return (String) method.invoke(mEthManagerObj);
            } catch (Exception e) {
                Log.e(TAG, "getGateway, " + e.getMessage());
            }
        }

        return "";
    }

    /**
     * 获取DNS1
     *
     * @return DNS1
     */
    public String getDns1() {
        if (mEthManagerObj != null) {
            try {
                Method method = mEthManagerObj.getClass().getDeclaredMethod("getDns");
                method.setAccessible(true);
                String dns = (String) method.invoke(mEthManagerObj);
                String data[] = dns.split(",");
                return data[0];
            } catch (Exception e) {
                Log.e(TAG, "getDns1, " + e.getMessage());
            }
        }

        return "";
    }

    /**
     * 获取DNS2
     *
     * @return DNS2
     */
    public String getDns2() {
        if (mEthManagerObj != null) {
            try {
                Method method = mEthManagerObj.getClass().getDeclaredMethod("getDns");
                method.setAccessible(true);
                String dns = (String) method.invoke(mEthManagerObj);
                String data[] = dns.split(",");
                return data[1];
            } catch (Exception e) {
                Log.e(TAG, "getDns2, " + e.getMessage());
            }
        }

        return "";
    }

    /**
     * 获取IP分配方式
     *
     * @return DHCP：动态IP， STATIC：静态IP
     */
    public String getIpAssignment() {
        if (mEthManagerObj != null) {
            try {
                Method method = mEthManagerObj.getClass().getDeclaredMethod("getConfiguration");
                method.setAccessible(true);
                Object configuration = method.invoke(mEthManagerObj);
                Field ipAssignment = configuration.getClass().getDeclaredField("ipAssignment");
                ipAssignment.setAccessible(true);
                return ipAssignment.get(configuration).toString();
            } catch (Exception e) {
                Log.e(TAG, "getIpAssignment, " + e.getMessage());
            }
        }

        return "";
    }

    /**
     * 设置IP
     *
     * @param ipStr      IP
     * @param netmaskStr 子网掩码
     * @param gatewayStr 网关
     * @param dnsStr1    DNS1
     * @param dnsStr2    DNS2
     * @param mode       连接模式，STATIC|DHCP
     * @return true：成功，false：失败
     */
    public boolean setIp(String ipStr,
                         String netmaskStr,
                         String gatewayStr,
                         String dnsStr1,
                         String dnsStr2,
                         @NonNull String mode) {

        if (!TextUtils.equals(mode, "STATIC")
            && !TextUtils.equals(mode, "DHCP")) {
            Log.e(TAG, "setIp, mode not supported");
            return false;
        }

        try {
            Object sicInstance = null;
            if (TextUtils.equals(mode, "STATIC")) {
                // 模式为STATIC（静态IP）才配置StaticIpConfiguration
                Inet4Address inet = getIPv4Address(ipStr);
                int prefixLength = maskStr2InetMask(netmaskStr);
                InetAddress gateway = getIPv4Address(gatewayStr);
                InetAddress dns = getIPv4Address(dnsStr1);

                if (null == inet || null == gateway || null == dns
                        || inet.getAddress().length <= 0
                        || prefixLength == 0
                        || gateway.toString().isEmpty()
                        || dns.toString().isEmpty()) {
                    Log.e(TAG, "setIp, ip, mask or dns is wrong");
                    return false;
                }

                // new StaticIpConfiguration();
                Class<?> sicClazz = Class.forName("android.net.StaticIpConfiguration");
                //Constructor<?> sicConstructor = sicClazz.getDeclaredConstructor(sicClazz);
                sicInstance = sicClazz.newInstance();

                // new LinkAddress(inet, prefixLength);
                Class<?> linkAddressClazz = Class.forName("android.net.LinkAddress");
                Constructor<?> linkAddressConstructor = linkAddressClazz.getDeclaredConstructor(InetAddress.class, int.class);
                Object linkAddressInstance = linkAddressConstructor.newInstance(inet, prefixLength);

                // IP
                Field ipAddressField = sicInstance.getClass().getDeclaredField("ipAddress");
                ipAddressField.setAccessible(true);
                ipAddressField.set(sicInstance, linkAddressInstance);

                // Gateway
                Field gatewayField = sicInstance.getClass().getDeclaredField("gateway");
                gatewayField.setAccessible(true);
                gatewayField.set(sicInstance, gateway);

                // DNS
                ArrayList<InetAddress> inetAddresses = new ArrayList<InetAddress>();
                inetAddresses.add(dns);
                if (!dnsStr2.isEmpty()) {
                    inetAddresses.add(getIPv4Address(dnsStr2));
                }
                Field dnsServersField = sicInstance.getClass().getDeclaredField("dnsServers");
                dnsServersField.setAccessible(true);
                dnsServersField.set(sicInstance, inetAddresses);
            }

            // IpConfiguration
            Class<?> ipcClazz = Class.forName("android.net.IpConfiguration");

            // 获取IpAssignment和ProxySettings枚举类参数的集合
            HashMap ipAssignmentMap = new HashMap();
            HashMap proxySettingsMap = new HashMap();
            Class<?>[] innerClasses = ipcClazz.getDeclaredClasses();
            for (Class innerClass : innerClasses) {
                // 获取枚举数组
                Object[] enumConstants = innerClass.getEnumConstants();
                if (innerClass.getSimpleName().equals("ProxySettings")) {
                    for (Object enu : enumConstants) {
                        // 设置代理设置集合 STATIC DHCP UNASSIGNED PAC
                        proxySettingsMap.put(enu.toString(), enu);
                    }
                } else if (innerClass.getSimpleName().equals("IpAssignment")) {
                    for (Object enu : enumConstants) {
                        // 设置以太网连接模式集合 STATIC DHCP UNASSIGNED
                        ipAssignmentMap.put(enu.toString(), enu);
                    }
                }
            }

            for (Constructor constructor : ipcClazz.getConstructors()) {
                // 获取IpConfiguration类4个参数的构造方法
                if (constructor.getParameterTypes().length == 4) {
                    // new IpConfiguration(IpAssignment.STATIC, ProxySettings.NONE, staticIpConfiguration, null);
                    Object ipcInstance = constructor.newInstance(ipAssignmentMap.get(mode),
                            proxySettingsMap.get("NONE"), sicInstance, null);

                    // mEthManager.setConfiguration(mIpConfiguration);
                    Method method = mEthManagerObj.getClass().getDeclaredMethod("setConfiguration", ipcClazz);
                    method.setAccessible(true);
                    method.invoke(mEthManagerObj, ipcInstance);
                    break;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "setIp, " + e.getMessage());
            return false;
        }

        return true;
    }

    private Inet4Address getIPv4Address(String text) {
        try {
            // NetworkUtils.numericToInetAddress(text);
            Class<?> networkUtilsClazz = Class.forName("android.net.NetworkUtils");
            Method method = networkUtilsClazz.getMethod("numericToInetAddress", String.class);
            return (Inet4Address) method.invoke(null, text);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Convert netmask
     *
     * @param prefixLength
     * @return
     */
    private String interMask2String(int prefixLength) {
        String netMask = null;
        int inetMask = prefixLength;

        int part = inetMask / 8;
        int remainder = inetMask % 8;
        int sum = 0;

        for (int i = 8; i > 8 - remainder; i--) {
            sum = sum + (int) Math.pow(2, i - 1);
        }

        if (part == 0) {
            netMask = sum + ".0.0.0";
        } else if (part == 1) {
            netMask = "255." + sum + ".0.0";
        } else if (part == 2) {
            netMask = "255.255." + sum + ".0";
        } else if (part == 3) {
            netMask = "255.255.255." + sum;
        } else if (part == 4) {
            netMask = "255.255.255.255";
        }

        return netMask;
    }

    /*
     * convert subMask string to prefix length
     */
    private int maskStr2InetMask(String maskStr) {
        StringBuffer sb;
        String str;
        int inetmask = 0;
        int count = 0;
        /*
         * check the subMask format
         */
        Pattern pattern = Pattern.compile("(^((\\d|[01]?\\d\\d|2[0-4]\\d|25[0-5])\\.){3}(\\d|[01]?\\d\\d|2[0-4]\\d|25[0-5])$)|^(\\d|[1-2]\\d|3[0-2])$");
        if (!pattern.matcher(maskStr).matches()) {
            Log.e(TAG, "subMask is error");
            return 0;
        }

        String[] ipSegment = maskStr.split("\\.");
        for (int n = 0; n < ipSegment.length; n++) {
            sb = new StringBuffer(Integer.toBinaryString(Integer.parseInt(ipSegment[n])));
            str = sb.reverse().toString();
            count = 0;
            for (int i = 0; i < str.length(); i++) {
                i = str.indexOf("1", i);
                if (i == -1)
                    break;
                count++;
            }
            inetmask += count;
        }
        return inetmask;
    }
}
