package com.techx.tradex.notification.configurations;

import freemarker.cache.TemplateLoader;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.TemplateNotFoundException;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.*;
import java.util.Locale;

@Configuration
public class FmConfiguration {

    @Autowired
    private ResourceLoader resourceLoader;
    private freemarker.template.Configuration fmConfiguration;

    //    @Bean("freeMarkerConfiguration")
    public freemarker.template.Configuration configuration() {
        if (fmConfiguration == null) {
            fmConfiguration = new freemarker.template.Configuration(freemarker.template.Configuration.VERSION_2_3_26);
            fmConfiguration.setDefaultEncoding("UTF-8");
            fmConfiguration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
            fmConfiguration.setLogTemplateExceptions(false);
            fmConfiguration.setTemplateLoader(new CustomTemplateLoader(resourceLoader));
        }
        return fmConfiguration;
    }

    public static class CustomTemplateLoader implements TemplateLoader {
        private ResourceLoader resourceLoader;

        public CustomTemplateLoader(ResourceLoader resourceLoader) {
            this.resourceLoader = resourceLoader;
        }

        @Override
        public Object findTemplateSource(String resourceName) throws IOException {
            File f = new File(resourceName);
            if (f.exists()) return f;
            String name = "classpath:" + getResourceName(resourceName, Locale.getDefault());
            Resource resource = this.resourceLoader.getResource(name);
            if (!resource.exists()) {
                name = "classpath:" + resourceName;
                resource = this.resourceLoader.getResource(name);
            }
            if (!resource.exists()) {
                throw new TemplateNotFoundException(name, null, "does not exist");
            }
            FileUtils.copyInputStreamToFile(resource.getInputStream(), f);
            return f;
        }

        @Override
        public long getLastModified(Object templateSource) {
            return Long.valueOf(((File) templateSource).lastModified());
        }

        @Override
        public Reader getReader(Object templateSource, String encoding) throws IOException {
            if (!(templateSource instanceof File)) {
                throw new IllegalArgumentException("templateSource wasn\'t a File, but a: " + templateSource.getClass().getName());
            } else {
                return new InputStreamReader(new FileInputStream((File) templateSource), encoding);
            }
        }

        @Override
        public void closeTemplateSource(Object o) throws IOException {

        }


        private String getResourceName(String resourceName, Locale locale) {
//            String[] parts = resourceName.split("\\.");
//            StringBuilder sb = new StringBuilder();
//            for (int i = 0; i < parts.length - 1; i ++) {
//                sb.append(parts[i]);
//                if (i > 0) {
//                    sb.append(".");
//                }
//            }
//
//            sb.append("_" + locale.getLanguage() + "." + parts[parts.length - 1]);
//            return sb.toString();
            return resourceName;
        }
    }
}
