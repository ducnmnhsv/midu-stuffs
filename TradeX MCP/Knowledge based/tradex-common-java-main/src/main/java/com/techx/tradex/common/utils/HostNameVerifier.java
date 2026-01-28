package com.techx.tradex.common.utils;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

public class HostNameVerifier {
    public static void disableSSLHostNameVerifier() {
        HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String s, SSLSession sslSession) {
                return true;
            }
        });
    }

    public static void setSSLHostNames(String ...names) {
        HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String s, SSLSession sslSession) {
                for (String trusted: names) {
                    if (trusted.equals(s)) {
                        return true;
                    }
                }
                return false;
            }
        });
    }
}
