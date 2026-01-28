package com.techx.tradex.common.model.configuration;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class LocaleResponse extends ArrayList<LocaleResponse.LocaleItem> {

    @Data
    public static class File {
        private String namespace;
        private String url;
        private Map<String, String> content;
    }

    @Data
    public static class LocaleItem {
        private List<File> files;
        private String lang;
        private String latestVersion;
        private String msName;
    }
}
