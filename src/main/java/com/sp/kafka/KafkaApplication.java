package com.sp.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Kafka Application - Spring Boot entry point with Kafka Consumer integration.
 *
 * This application demonstrates how to properly manage a Kafka Consumer lifecycle
 * within a Spring Boot application, including graceful shutdown handling.
 *
 * Application Flow:
 * 1. Spring Boot starts and initializes the application context
 * 2. Kafka Consumer is initialized with configuration
 * 3. Shutdown hook is registered for graceful termination
 * 4. Consumer starts polling (blocking operation on main thread)
 *
 * Graceful Shutdown Pattern:
 * - Problem: Consumer.consume() runs an infinite loop - how do we stop it cleanly?
 * - Solution: Use JVM shutdown hook + consumer.wakeup()
 * - When SIGTERM/SIGINT is received, shutdown hook triggers wakeup()
 * - wakeup() causes poll() to throw WakeupException, breaking the loop
 * - finally block in consume() calls close() to clean up resources
 *
 * Why is graceful shutdown important?
 * - Ensures offsets are committed (no message loss or reprocessing)
 * - Properly leaves consumer group (faster rebalancing for other consumers)
 * - Releases resources (connections, threads, memory)
 */
@SpringBootApplication
public class KafkaApplication {

    private static final Logger logger = LoggerFactory.getLogger(KafkaApplication.class);

    public static void main(String[] args) {

        // Start Spring Boot application
        // This initializes the Spring context, starts embedded server (if web),
        // and enables component scanning for @Service, @Controller, etc.
        SpringApplication.run(KafkaApplication.class, args);

        // Capture reference to the main thread
        // We need this in the shutdown hook to wait for consumer to finish
        final Thread mainThread = Thread.currentThread();

        // Initialize the Kafka Consumer
        // This creates the consumer and subscribes to topics
        // Must be done before starting the consume loop
        Consumer.initializeConsumer();

        // Register JVM Shutdown Hook for graceful shutdown
        // This runs when: Ctrl+C, kill command, System.exit(), or JVM termination
        // IMPORTANT: Shutdown hooks run in a separate thread, not the main thread
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                logger.info("Shutting down...Detected a shutdown, let's exist by calling.wakeup()");

                // wakeup(): The ONLY thread-safe method on KafkaConsumer
                // This causes the next poll() call to throw WakeupException
                // If poll() is currently blocked, it will return immediately with WakeupException
                Consumer.consumer.wakeup();

                try {
                    // Wait for the main thread (running consume loop) to finish
                    // This ensures the consumer has time to:
                    // 1. Catch the WakeupException
                    // 2. Execute the finally block
                    // 3. Call consumer.close() properly
                    // Without this, JVM might exit before cleanup completes
                    mainThread.join();
                }
                catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        });

        // Start consuming messages - this is a BLOCKING call
        // The main thread will stay here until wakeup() is called
        // All message processing happens inside this method
        Consumer.consume();

    }

}
