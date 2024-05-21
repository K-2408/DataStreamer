package com.testingone.example.testingone;

import jakarta.annotation.PostConstruct;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.stereotype.Service;
import org.springframework.kafka.core.KafkaAdmin;


import java.util.HashMap;
import java.util.Map;

@Service
public class KafkaProducerService {

    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private KafkaAdmin kafkaAdmin;

    @PostConstruct
    public void init() {
        Map<String, Object> configProps = new HashMap<>(kafkaAdmin.getConfigurationProperties());
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaAdmin.getConfigurationProperties().get("bootstrap.servers"));
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 102400); // 50KB buffer size
        configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, 81920); // 30KB batch size
        configProps.put(ProducerConfig.LINGER_MS_CONFIG, 10); // 1ms linger time
        configProps.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "gzip");

        ProducerFactory<String, String> producerFactory = new DefaultKafkaProducerFactory<>(configProps);
        this.kafkaTemplate = new KafkaTemplate<>(producerFactory);
    }

    public void sendMessage(String topic, String message) {
        kafkaTemplate.send(topic, message);
    }
}
