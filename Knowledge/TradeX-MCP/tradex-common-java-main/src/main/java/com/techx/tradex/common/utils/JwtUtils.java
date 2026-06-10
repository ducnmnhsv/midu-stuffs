package com.techx.tradex.common.utils;

import com.techx.tradex.common.constants.DomainConstant;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class JwtUtils {
    private final static Logger log = LoggerFactory.getLogger(JwtUtils.class);

    public void process(JwtConf jwtItemConf, String domain) {
        if (StringUtils.isEmpty(domain) || DomainConstant.TRADEX.equals(domain)) {
            process(jwtItemConf);
            jwtItemConf.getDomains().forEach((key, value) -> {
                process(value);
            });
        } else {
            process(jwtItemConf.getDomains().get(domain));
        }
    }

    public void process(JwtItemConf jwtItemConf) {
        if (!StringUtils.isEmpty(jwtItemConf.privateKeyFile)) {
            try {
                jwtItemConf.privateKey = FileUtils.readFileToString(
                        new File(jwtItemConf.privateKeyFile), StandardCharsets.UTF_8);
            } catch (Exception e) {
                log.error("Fail to load key from file {}", jwtItemConf.privateKeyFile, e);
            }
        }
        if (!StringUtils.isEmpty(jwtItemConf.publicKeyFile)) {
            try {
                jwtItemConf.publicKey = FileUtils.readFileToString(
                        new File(jwtItemConf.publicKeyFile), StandardCharsets.UTF_8);
            } catch (Exception e) {
                log.error("Fail to load key from file {}", jwtItemConf.publicKeyFile, e);
            }
        }
    }

    public static class JwtConf extends JwtItemConf {
        private Map<String, JwtItemConf> domains;

        public Map<String, JwtItemConf> getDomains() {
            return domains;
        }

        public void setDomains(Map<String, JwtItemConf> domains) {
            this.domains = domains;
        }

        public JwtItemConf get(String domain) {
            if (StringUtils.isEmpty(domain) || DomainConstant.TRADEX.name().equals(domain)) {
                return this;
            } else {
                return this.getDomains().get(domain);
            }
        }
    }

    public static class JwtItemConf {
        private String publicKey;
        private String publicKeyFile;
        private String privateKey;
        private String privateKeyFile;

        public String getPublicKey() {
            return publicKey;
        }

        public void setPublicKey(String publicKey) {
            this.publicKey = publicKey;
        }

        public String getPublicKeyFile() {
            return publicKeyFile;
        }

        public void setPublicKeyFile(String publicKeyFile) {
            this.publicKeyFile = publicKeyFile;
        }

        public String getPrivateKey() {
            return privateKey;
        }

        public void setPrivateKey(String privateKey) {
            this.privateKey = privateKey;
        }

        public String getPrivateKeyFile() {
            return privateKeyFile;
        }

        public void setPrivateKeyFile(String privateKeyFile) {
            this.privateKeyFile = privateKeyFile;
        }
    }
}
