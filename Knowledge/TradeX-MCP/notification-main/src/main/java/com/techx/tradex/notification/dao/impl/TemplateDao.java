package com.techx.tradex.notification.dao.impl;


import com.techx.tradex.common.constants.LocaleExt;
import com.techx.tradex.common.exceptions.GeneralException;
import com.techx.tradex.common.utils.StringUtils;
import com.techx.tradex.notification.configurations.AppConf;
import com.techx.tradex.notification.configurations.FmConfiguration;
import freemarker.core.Environment;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Repository;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Repository
public class TemplateDao implements com.techx.tradex.notification.dao.TemplateDao {
    private AppConf appConf;
    private ResourceLoader resourceLoader;
    private Configuration freeMarkerConfiguration;
    private static final Logger log = LoggerFactory.getLogger(TemplateDao.class);

    private final String dir;

    @Autowired
    public TemplateDao(ResourceLoader resourceLoader
            , AppConf appConf
            , FmConfiguration fmConfiguration
    ) {
        this.appConf = appConf;
        this.resourceLoader = resourceLoader;
        this.freeMarkerConfiguration = fmConfiguration.configuration();
        this.dir = appConf.getTemplate().getDir();
    }

    public String getTemplate(String name, String localeString, Object data) {
        Locale locale = LocaleExt.VIETNAM;
        if (StringUtils.isNotEmpty(localeString)) {
            locale = Locale.forLanguageTag(localeString);
        }
        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        Writer bos = new OutputStreamWriter(bs, StandardCharsets.UTF_8);
        if (!name.endsWith(".html") && !name.endsWith(".ftl")) {
            name += ".ftl";
        }
//        freeMarkerConfiguration.setTemplateLoader(new FreeMarkerConfiguration.CustomTemplateLoader(resourceLoader));
        try {
            freeMarkerConfiguration.setDefaultEncoding("UTF-8");
            freeMarkerConfiguration.setEncoding(locale, "UTF-8");
            Template template = freeMarkerConfiguration.getTemplate(dir + name, locale, "UTF-8");
            Environment environment = template.createProcessingEnvironment(data, bos);
            environment.setOutputEncoding("UTF-8");
            environment.process();
        } catch (Exception e) {
            log.error("problem on template {} locale {}", name, locale, e);
            return null;
        }
        log.info("name {}, locale: {}, result: {}", name, localeString, bs);
        return bs.toString();
    }
}
