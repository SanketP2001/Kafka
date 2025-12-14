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

    public void sendMessageWithCallback(String message) {
        try {
            Properties props = new Properties();
            // create properties
            props.put("bootstrap.servers", "127.0.0.1:9092");
            props.put("key.serializer", StringSerializer.class.getName());
            props.put("value.serializer", StringSerializer.class.getName());
            // create producer
            KafkaProducer<String, String> producer = new KafkaProducer<>(props);


            ProducerRecord<String, String> record = new ProducerRecord<>("first_topic", message);
            producer.send(record, new Callback() {
                @Override
                public void onCompletion(RecordMetadata recordMetadata, Exception e) {
                    // executes every time a record successfully sent or throw an exception
                    if (e == null) {
                        logger.info("Received new metadata \n" +
                                "Topic : {} \n" +
                                "Partition : {} \n" +
                                "Offset : {} \n" +
                                "Timestamp : {} \n", recordMetadata.topic(), recordMetadata.partition(), recordMetadata.offset(), recordMetadata.timestamp());
                    } else {
                        logger.error("Error while sending message, {}", e.getMessage(), e);
                    }
                }
            });
            logger.info("Send message to Kafka Async");
            logger.info(message);
            producer.flush();
            producer.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // here if we are producing 10 records still this record will go to same partition , as default partition will use
    public void sendMessageWithCallbackMultiple(String message, boolean batch) {
        try {
            Properties props = new Properties();
            // create properties
            props.put("bootstrap.servers", "127.0.0.1:9092");
            props.put("key.serializer", StringSerializer.class.getName());
            props.put("value.serializer", StringSerializer.class.getName());
            if (batch) {
                props.setProperty("batch.size", "400");
                // not recommended
//                props.setProperty("partitioner.class", RoundRobinPartitioner.class.getName());
            }
            // create producer
            KafkaProducer<String, String> producer = new KafkaProducer<>(props);
            if (!batch) produceBatch(producer, message);
            else {
                // here we are sending message as batch with some time interval
                for (int j = 0; j < 10; j++) {
                    produceBatch(producer, message);
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            }
            producer.flush();
            producer.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void produceBatch(KafkaProducer<String, String> producer, String message) {
        for (int i = 0; i < 30; i++) {
            ProducerRecord<String, String> record = new ProducerRecord<>("first_topic", message + " : " + i);
            producer.send(record, new Callback() {
                @Override
                public void onCompletion(RecordMetadata recordMetadata, Exception e) {
                    // executes every time a record successfully sent or throw an exception
                    if (e == null) {
                        logger.info("Received new metadata \n" +
                                "Topic : {} \n" +
                                "Partition : {} \n" +
                                "Offset : {} \n" +
                                "Timestamp : {} \n", recordMetadata.topic(), recordMetadata.partition(), recordMetadata.offset(), recordMetadata.timestamp());
                    } else {
                        logger.error("Error while sending message, {}", e.getMessage(), e);
                    }
                }
            });
        }
    }

    public void sendMessageWithKey(String key, String message) {
        try {
            String topic = "first_topic";
            Properties props = new Properties();
            // create properties
            props.put("bootstrap.servers", "127.0.0.1:9092");
            props.put("key.serializer", StringSerializer.class.getName());
            props.put("value.serializer", StringSerializer.class.getName());
            // create producer
            KafkaProducer<String, String> producer = new KafkaProducer<>(props);

            for (int j = 0; j < 3; j++) {
                for (int i = 0; i < 10; i++) {
                    String key_ = key + i;
                    String value_ = message + i;
                    ProducerRecord<String, String> record = new ProducerRecord<>(topic, key_, value_);
                    producer.send(record, new Callback() {
                        @Override
                        public void onCompletion(RecordMetadata recordMetadata, Exception e) {
                            // executes every time a record successfully sent or throw an exception
                            if (e == null) {
                                logger.info("Key : {} \n" +
                                        "Partition : {} \n", key_, recordMetadata.partition());
                            } else {
                                logger.error("Error while sending message, {}", e.getMessage(), e);
                            }
                        }
                    });
                }
            }
        } catch (Exception e) {
            logger.error("Error while sending message, {}", e.getMessage(), e);
        }
    }
}
