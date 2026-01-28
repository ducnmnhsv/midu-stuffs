package com.techx.tradex.common.utils;

import lombok.Data;
import org.apache.commons.io.IOUtils;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class HttpUtil {
    public static InputStream openUrl(String httpUrl) throws IOException {
        return new URL(httpUrl).openStream();
    }

    public static InputStreamReader downloadFile(String httpUrl) throws IOException {
        if (httpUrl.startsWith("https")) {
            return downloadHttpsFile(httpUrl);
        }
        return downloadHttpFile(httpUrl);
    }

    public static InputStreamReader downloadHttpFile(String httpUrl) throws IOException {
        URL url = new URL(httpUrl);
        HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
        int status = con.getResponseCode();
        System.out.println(status);
        if (status > 299) {
            throw new DownloadException(status, new InputStreamReader(con.getErrorStream(), StandardCharsets.UTF_8));
        } else {
            System.out.println("????");
            return new InputStreamReader(con.getInputStream());
        }
    }

    public static InputStreamReader downloadHttpsFile(String httpUrl) throws IOException {
        URL url = new URL(httpUrl);
        HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
        int status = con.getResponseCode();
        System.out.println(status);
        if (status > 299) {
            throw new DownloadException(status, new InputStreamReader(con.getErrorStream(), StandardCharsets.UTF_8));
        } else {
            System.out.println("????");
            return new InputStreamReader(con.getInputStream());
        }
    }

    public static class ParameterStringBuilder {
        public static String getParamsString(Map<String, String> params)
                throws UnsupportedEncodingException {
            StringBuilder result = new StringBuilder();

            for (Map.Entry<String, String> entry : params.entrySet()) {
                result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                result.append("=");
                result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
                result.append("&");
            }

            String resultString = result.toString();
            return resultString.length() > 0
                    ? resultString.substring(0, resultString.length() - 1)
                    : resultString;
        }
    }

    @Data
    public static class DownloadException extends RuntimeException {
        private int code;

        public DownloadException(int code, Reader reader) throws IOException {
            super(code + ":" + IOUtils.toString(reader));
            this.code = code;
        }
    }

    public static void main(String[] args) throws IOException {
        System.out.println(IOUtils.toString(openUrl("https://s3.ap-southeast-1.amazonaws.com/tradex-vn/lang_resource/common/tuxedo/vi.json")));
    }
}
