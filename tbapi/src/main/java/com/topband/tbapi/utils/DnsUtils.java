package com.topband.tbapi.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class DnsUtils {
    private static final String TAG = "DnsUtils";

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public static boolean setDns(@NonNull Context context, InetAddress dns1, InetAddress dns2) {
        if (dns1 == null && dns2 == null) {
            Log.e(TAG, "setDns, At least one of dns1 and dns2 is not null");
            return false;
        }

        try {
            List<InetAddress> dnsServers = new ArrayList<InetAddress>();
            if (dns1 != null) {
                dnsServers.add(dns1);
            }
            if (dns2 != null) {
                dnsServers.add(dns2);
            }

            LinkProperties properties = new LinkProperties();
            properties.setDnsServers(dnsServers);

            ConnectivityManager connectivityManager = (ConnectivityManager)
                    context.getSystemService(Context.CONNECTIVITY_SERVICE);
            Network network = connectivityManager.getActiveNetwork();
            Log.i(TAG, "network=" + network.toString());
            LinkProperties linkProperties = connectivityManager.getLinkProperties(network);

            Method method = ConnectivityManager.class.getMethod("updateDnses",
                    LinkProperties.class, LinkProperties.class, Network.class);
            method.invoke(connectivityManager, properties, linkProperties, network);

            network = connectivityManager.getActiveNetwork();
            linkProperties = connectivityManager.getLinkProperties(network);

            dnsServers = linkProperties.getDnsServers();
            for (InetAddress dns : dnsServers) {
                Log.i(TAG, "setDns, " + dns.toString());
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
}
