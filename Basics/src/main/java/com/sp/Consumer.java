package com.sp;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

/**
 * Kafka Consumer Service - Demonstrates how to consume messages from Kafka topics.
 *
 * Key Concepts:
 * - Consumer: Client that reads records from Kafka topics
 * - Consumer Group: A group of consumers that cooperate to consume data from topics
 *   - Each partition is consumed by exactly ONE consumer in the group
 *   - Enables parallel processing and load balancing
 * - Offset: Sequential ID for each record in a partition (like a bookmark)
 * - Deserializer: Converts bytes back to Java objects (opposite of Serializer)
 * - Poll Loop: Continuous loop that fetches records from Kafka
 *
 * Consumer Group Behavior:
 * - If you have more consumers than partitions, some consumers will be idle
 * - If you have more partitions than consumers, some consumers will read from multiple partitions
 * - Adding/removing consumers triggers "rebalancing" (partitions are redistributed)
 */
@Service
public class Consumer {

    // Consumer group ID - all consumers with same group.id share the work
    // If you start multiple instances with same group, they split the partitions
    private static final String group = "myGroup";

    // Topic to subscribe to - the consumer will read all partitions of this topic
    private static final String topic = "first_topic";

    private static final Logger logger = LoggerFactory.getLogger(Consumer.class);

    // Static reference to the consumer - allows access from KafkaApplication for graceful shutdown
    // Using static so wakeup() can be called from shutdown hook
    public static KafkaConsumer<String, String> consumer;

    /**
     * INITIALIZE CONSUMER - Setup and Configuration
     *
     * Creates and configures the KafkaConsumer with necessary properties.
     * Must be called before consume() to establish connection to Kafka.
     *
     * Flow: Create Properties -> Create Consumer -> Subscribe to Topic(s)
     */
    public static void initializeConsumer() {
        Properties props = new Properties();

        // bootstrap.servers: List of Kafka broker addresses (entry point to cluster)
        // Consumer will discover full cluster topology from these initial brokers
        props.put("bootstrap.servers", "127.0.0.1:9092");

        // key.deserializer: How to convert record KEY from bytes back to Java object
        // Must match the serializer used by the producer (StringSerializer -> StringDeserializer)
        props.put("key.deserializer", StringDeserializer.class.getName());

        // value.deserializer: How to convert record VALUE from bytes back to Java object
        props.put("value.deserializer", StringDeserializer.class.getName());

        // group.id: Consumer group this consumer belongs to
        // Kafka tracks offsets PER GROUP - different groups read independently
        props.setProperty("group.id", group);

        // auto.offset.reset: What to do when there's no initial offset or offset is invalid
        // Options:
        //   "none"     - Throw exception if no previous offset found for consumer group
        //                (Use when you MUST have existing offset - fail-fast approach)
        //   "earliest" - Start reading from the beginning of the topic (don't miss any data)
        //   "latest"   - Start reading only NEW messages (skip historical data)
        props.setProperty("auto.offset.reset", "earliest");

        // Create the consumer with our configuration
        // KafkaConsumer is NOT thread-safe - only use from one thread
        consumer = new KafkaConsumer<>(props);

        // subscribe(): Tell consumer which topic(s) to read from
        // Can pass multiple topics: Arrays.asList("topic1", "topic2", "topic3")
        // Consumer will be assigned partitions automatically based on group membership
        consumer.subscribe(Collections.singletonList(topic));
    }

    /**
     * CONSUME - The Poll Loop (Heart of the Consumer)
     *
     * Continuously polls Kafka for new records and processes them.
     * This is a blocking operation that runs until interrupted.
     *
     * Poll Loop Pattern:
     * - poll() fetches records from assigned partitions
     * - Process each record in the batch
     * - Repeat forever (until shutdown signal)
     *
     * Graceful Shutdown:
     * - wakeup() is called from shutdown hook (see KafkaApplication)
     * - This causes poll() to throw WakeupException
     * - We catch it and exit the loop cleanly
     * - finally block ensures consumer.close() is always called
     */
    public static void consume(){
        try {

            try {
                // Infinite poll loop - the standard pattern for Kafka consumers
                // Exits only when wakeup() is called (throws WakeupException)
                while (true) {
                    logger.info("Polling records from Kafka");

                    // poll(): Fetch records from Kafka brokers
                    // Parameter: Maximum time to block waiting for records
                    // Returns: Batch of records (may be empty if no new data)
                    // Also handles: heartbeats, rebalancing, offset commits (if auto-commit enabled)
                    ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));

                    // Process each record in the batch
                    // ConsumerRecord contains: key, value, partition, offset, timestamp, headers
                    for (ConsumerRecord<String, String> record : records) {
                        logger.info("Key : {} Value : {}", record.key(), record.value());
                        // Partition + Offset uniquely identifies a record in a topic
                        logger.info("Partition : {} Offset : {}", record.partition(), record.offset());
                    }
                    // After processing, offsets are committed automatically (default behavior)
                    // To control when offsets are committed, use enable.auto.commit=false
                    // and call consumer.commitSync() or consumer.commitAsync() manually
                }
            }
            catch (WakeupException e) {
                // WakeupException is expected when shutdown is initiated
                // consumer.wakeup() is the only thread-safe method on KafkaConsumer
                // It causes poll() to exit immediately with this exception
                logger.info("Wakeup Exception: Consumer is starting to shut down....");
            }
            catch (Exception e) {
                // Unexpected errors - could be network issues, deserialization errors, etc.
                logger.error("Unexpected Error {}",e.getMessage());
            }
            finally {
                // close(): Clean up resources and leave consumer group
                // IMPORTANT: This commits final offsets and triggers rebalance
                // Other consumers in the group will take over this consumer's partitions
                consumer.close();
                logger.info("Consumer gracefully shutdown"); // this might not run - as Spring boot will kill the logger
                System.out.println("..Consumer gracefully shutdown");  // Backup - logger may be dead
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
