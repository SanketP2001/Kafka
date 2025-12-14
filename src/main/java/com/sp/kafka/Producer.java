package com.sp.kafka;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Service
public class Producer {

    private final Logger logger = LoggerFactory.getLogger(Producer.class);

    public void sendMessage(String message) {
        try {
            Properties props = new Properties();
            // create properties
            props.put("bootstrap.servers", "127.0.0.1:9092");
            props.put("key.serializer", StringSerializer.class.getName());
            props.put("value.serializer", StringSerializer.class.getName());
            // create producer
            KafkaProducer<String, String> producer = new KafkaProducer<>(props);

            ProducerRecord<String, String> record = new ProducerRecord<>("first_topic", message);
            producer.send(record);
            logger.info("Send message to Kafka");
            logger.info(message);
            producer.flush();
            producer.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
