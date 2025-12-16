package com.sp;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Properties;

/**
 * Kafka Producer Service - Demonstrates different ways to produce messages to Kafka.
 *
 * Key Concepts:
 * - Producer: Client that publishes records to Kafka topics
 * - ProducerRecord: A key-value pair to be sent to Kafka (key is optional)
 * - Serializer: Converts Java objects to bytes for transmission
 * - Partitioner: Determines which partition a record goes to
 */
@Service
public class Producer {

    private final Logger logger = LoggerFactory.getLogger(Producer.class);

    /**
     * BASIC PRODUCER - Fire and Forget
     *
     * Simplest way to send a message. We send the record but don't wait for
     * acknowledgment. This is fast but we don't know if the message was delivered.
     *
     * Flow: Create Producer -> Create Record -> Send -> Flush -> Close
     */
    public void sendMessage(String message) {
        try {
            Properties props = new Properties();

            // bootstrap.servers: List of Kafka broker addresses
            // This is the entry point to the Kafka cluster. The producer uses this
            // to discover the full cluster and connect to the appropriate brokers.
            props.put("bootstrap.servers", "127.0.0.1:9092");

            // key.serializer: How to convert the record KEY from Java object to bytes
            // StringSerializer converts String -> byte[] for transmission over network
            props.put("key.serializer", StringSerializer.class.getName());

            // value.serializer: How to convert the record VALUE from Java object to bytes
            // Both key and value need serializers (even if key is null)
            props.put("value.serializer", StringSerializer.class.getName());

            // Create the producer with our configuration
            // KafkaProducer is thread-safe and should be reused (creating it here for demo)
            KafkaProducer<String, String> producer = new KafkaProducer<>(props);

            // ProducerRecord: The message to send
            // Parameters: (topic, value) - key is null here, so partition is chosen round-robin
            ProducerRecord<String, String> record = new ProducerRecord<>("first_topic", message);

            // send() is ASYNCHRONOUS - it returns immediately without waiting for broker acknowledgment
            // The record is added to an internal buffer and sent in batches for efficiency
            producer.send(record);
            logger.info("Send message to Kafka");
            logger.info(message);

            // flush(): Forces all buffered records to be sent immediately
            // Blocks until all records in the buffer have been sent to the broker
            // Without flush(), records might still be in buffer when close() is called
            producer.flush();

            // close(): Releases all resources (connections, threads, buffers)
            // Also calls flush() internally, but explicit flush() makes intent clear
            producer.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * PRODUCER WITH CALLBACK - Async with Acknowledgment
     *
     * Same as basic producer but with a callback to know if the send succeeded or failed.
     * The callback executes asynchronously when the broker acknowledges the record.
     *
     * Use Case: When you need to know if message was delivered (logging, retry logic, etc.)
     */
    public void sendMessageWithCallback(String message) {
        try {
            Properties props = new Properties();
            props.put("bootstrap.servers", "127.0.0.1:9092");
            props.put("key.serializer", StringSerializer.class.getName());
            props.put("value.serializer", StringSerializer.class.getName());

            KafkaProducer<String, String> producer = new KafkaProducer<>(props);

            ProducerRecord<String, String> record = new ProducerRecord<>("first_topic", message);

            // send() with Callback - the callback is invoked when broker responds
            // Callback runs in producer's I/O thread, so keep it fast (don't block!)
            producer.send(record, new Callback() {
                @Override
                public void onCompletion(RecordMetadata recordMetadata, Exception e) {
                    // This executes asynchronously when broker acknowledges (or fails)
                    // Either recordMetadata OR exception will be non-null, never both

                    if (e == null) {
                        // SUCCESS - recordMetadata contains delivery details:
                        // - topic: which topic the record was sent to
                        // - partition: which partition within the topic (0, 1, 2, etc.)
                        // - offset: unique sequential ID within that partition
                        // - timestamp: when the broker received it
                        logger.info("Received new metadata \n" +
                                "Topic : {} \n" +
                                "Partition : {} \n" +
                                "Offset : {} \n" +
                                "Timestamp : {} \n", recordMetadata.topic(), recordMetadata.partition(), recordMetadata.offset(), recordMetadata.timestamp());
                    } else {
                        // FAILURE - exception contains the error details
                        // Common errors: NetworkException, TimeoutException, SerializationException
                        logger.error("Error while sending message, {}", e.getMessage(), e);
                    }
                }
            });

            // Note: This log appears BEFORE the callback because send() is async
            logger.info("Send message to Kafka Async");
            logger.info(message);
            producer.flush();
            producer.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * BATCH PRODUCER - Understanding Sticky Partitioner & Batching
     *
     * Demonstrates how Kafka batches messages for efficiency and how the
     * default "sticky partitioner" works.
     *
     * Key Concept - Sticky Partitioner (Kafka 2.4+):
     * When sending messages WITHOUT a key, Kafka uses the "sticky partitioner":
     * - It "sticks" to one partition until the batch is full or linger.ms expires
     * - This improves batching efficiency (more messages per batch = fewer requests)
     * - After batch is sent, it may switch to a different partition
     *
     * batch.size: Maximum bytes per batch (default 16KB)
     * - Smaller batch.size = more frequent sends = messages spread across partitions
     * - Larger batch.size = fewer sends = messages grouped together
     */
    public void sendMessageWithCallbackMultiple(String message, boolean batch) {
        try {
            Properties props = new Properties();
            props.put("bootstrap.servers", "127.0.0.1:9092");
            props.put("key.serializer", StringSerializer.class.getName());
            props.put("value.serializer", StringSerializer.class.getName());

            if (batch) {
                // batch.size = 400 bytes (very small, default is 16384 bytes)
                // This forces more frequent batch sends, which causes the sticky
                // partitioner to switch partitions more often
                // Result: messages spread across multiple partitions
                props.setProperty("batch.size", "400");

                // RoundRobinPartitioner: Sends each message to a different partition
                // NOT recommended because it defeats batching (1 message per batch)
                // Sticky partitioner is better for throughput
                // props.setProperty("partitioner.class", RoundRobinPartitioner.class.getName());
            }

            KafkaProducer<String, String> producer = new KafkaProducer<>(props);

            if (!batch) {
                // Without batch mode: send 30 messages rapidly
                // Sticky partitioner will likely put ALL in same partition (one batch)
                produceBatch(producer, message);
            } else {
                // With batch mode: send 30 messages, wait 5 seconds, repeat 10 times
                // The 5-second delay allows batches to flush between iterations
                // Combined with small batch.size, messages will spread across partitions
                for (int j = 0; j < 10; j++) {
                    produceBatch(producer, message);
                    try {
                        Thread.sleep(5000); // Force batch to send before next iteration
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

    /**
     * Helper method to produce 30 messages in a tight loop.
     * Without delays, these will likely be batched together by the sticky partitioner.
     */
    private void produceBatch(KafkaProducer<String, String> producer, String message) {
        for (int i = 0; i < 30; i++) {
            ProducerRecord<String, String> record = new ProducerRecord<>("first_topic", message + " : " + i);
            producer.send(record, new Callback() {
                @Override
                public void onCompletion(RecordMetadata recordMetadata, Exception e) {
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

    /**
     * PRODUCER WITH KEY - Guaranteed Partition Assignment
     *
     * When you provide a KEY, Kafka guarantees that:
     * - Same key ALWAYS goes to the same partition (deterministic hashing)
     * - This ensures ordering for all messages with the same key
     *
     * Use Case: When you need ordering guarantees
     * Example: All events for user_123 should be processed in order
     *          -> Use "user_123" as the key
     *
     * How it works:
     * 1. Key is hashed using murmur2 algorithm
     * 2. Hash is mapped to partition: hash(key) % num_partitions
     * 3. Same key = same hash = same partition (unless partition count changes!)
     *
     * WARNING: If you add partitions to a topic, key-partition mapping changes!
     */
    public void sendMessageWithKey(String key, String message) {
        try {
            String topic = "first_topic";
            Properties props = new Properties();
            props.put("bootstrap.servers", "127.0.0.1:9092");
            props.put("key.serializer", StringSerializer.class.getName());
            props.put("value.serializer", StringSerializer.class.getName());

            KafkaProducer<String, String> producer = new KafkaProducer<>(props);

            // Send same keys multiple times to prove they always go to same partition
            // Outer loop (j): repeat 3 times to demonstrate consistency
            // Inner loop (i): 10 different keys (key0, key1, ..., key9)
            for (int j = 0; j < 3; j++) {
                for (int i = 0; i < 10; i++) {
                    String key_ = key + i;      // e.g., "myKey0", "myKey1", etc.
                    String value_ = message + i;

                    // ProducerRecord with KEY - partition is determined by key hash
                    // Same key will ALWAYS go to same partition across all 3 iterations
                    ProducerRecord<String, String> record = new ProducerRecord<>(topic, key_, value_);

                    producer.send(record, new Callback() {
                        @Override
                        public void onCompletion(RecordMetadata recordMetadata, Exception e) {
                            if (e == null) {
                                // You'll notice: same key always logs same partition number
                                // e.g., "myKey0" -> Partition 2 (always)
                                //       "myKey1" -> Partition 0 (always)
                                logger.info("Key : {} \n" +
                                        "Partition : {} \n", key_, recordMetadata.partition());
                            } else {
                                logger.error("Error while sending message, {}", e.getMessage(), e);
                            }
                        }
                    });
                }
            }
            // Note: Missing flush() and close() here - should add for proper cleanup!
        } catch (Exception e) {
            logger.error("Error while sending message, {}", e.getMessage(), e);
        }
    }
}
