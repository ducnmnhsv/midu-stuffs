package com.difisoft.nhsv.admin.config;

import com.difisoft.file.FileService;
import com.difisoft.file.FileUtils;
import com.difisoft.kafka.producer.KafkaRequestProducer;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Properties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfig {

    @Bean
    public KafkaRequestProducer kafkaRequestProducer(ObjectMapper objectMapper,
            ApplicationProperties applicationProperties) {
        return new KafkaRequestProducer(
                objectMapper,
                applicationProperties.getKafkaUrls(),
                applicationProperties.getClusterId(),
                applicationProperties.getNodeId(),
                true,
                new Properties(),
                true);
    }

    @Bean
    public FileService fileService(ApplicationProperties applicationProperties) {
        return FileUtils.getFileService(applicationProperties.getFileConf());
    }
}
