# Kafka

# Introduction

Apache Kafka is an open-source, enterprise-scale data streaming technology. It helps move data in real time between different systems, reducing the complexity of data integration. Kafka acts as a middle layer where source systems produce data to Kafka, and target systems consume data from Kafka. This decoupling makes data transfer more scalable and efficient. Kafka is known for its high performance, fault tolerance, and wide adoption by companies like LinkedIn, Netflix, and Uber.

## Scenario

Imagine you work for a company that has multiple systems generating data, such as:

Source Systems: Website events, pricing data, financial transactions, and user interactions.
Target Systems: Databases, analytics systems, email systems, and audit systems.

### Problem

Initially, each source system needs to send data directly to each target system. This creates a complex web of integrations, where each source system must connect to each target system. For example, if you have 4 source systems and 6 target systems, you would need 24 integrations. Each integration has its own challenges, such as different data formats (e.g., JSON, CSV, Binary) and protocols (e.g., HTTP, FTP, JDBC).

### Solution with Kafka

To simplify this, you introduce Apache Kafka as a middle layer:

Source Systems Produce Data: Each source system sends (produces) its data to Kafka. This data is organized into topics within Kafka.
Kafka Stores Data: Kafka acts as a central hub, storing streams of data from all source systems.
Target Systems Consume Data: Each target system retrieves (consumes) the data it needs from Kafka.

### Example

Let's say your company has a website that logs user interactions, such as clicks and page views. You want to send this data to both an analytics system for real-time analysis and a database for storage.

- Website (Source System): The website produces a stream of user interaction data to a Kafka topic called user-interactions.
- Kafka: Kafka stores the user-interactions data in real-time.
- Analytics System (Target System): The analytics system consumes the user-interactions data from Kafka to provide real-time insights.
- Database (Target System): The database consumes the same user-interactions data from Kafka for long-term storage.

### Benefits

- Decoupling: Source systems are decoupled from target systems, reducing the complexity of integrations.
- Scalability: Kafka can handle high throughput, allowing you to scale by adding more brokers to the Kafka cluster.
- Fault Tolerance: Kafka is fault-tolerant, ensuring data is not lost even if some components fail.
- Real-Time Processing: Kafka enables real-time data processing with low latency.
- Apache Kafka addresses the critical challenge of managing complex data integrations between multiple source and target systems. Without Kafka, each source system would need to connect directly to each target system, resulting in a complex web of integrations. Kafka simplifies this by acting as a middle layer where source systems produce data to Kafka, and target systems consume data from Kafka. This decoupling reduces complexity, improves scalability, and enhances fault tolerance.

# Kafka theory

## Topics

### Definition:

A topic in Kafka is a stream of data, similar to a table in a database but without constraints. You can send any type of data to a Kafka topic, such as JSON, Avro, text files, or binary data.

### Naming:

Topics are identified by their names, like logs, purchases, tweets, or trucks_gps.

### Purpose:

Topics help organize data streams within a Kafka cluster, allowing producers to send data and consumers to read data from these streams.

## Partitions

### Definition:

A topic is divided into partitions, which are ordered, immutable sequences of records. Each partition is a log where records are appended.

### Ordering:

Messages within a partition are ordered by their offsets. For example, in partition 0, messages will have offsets 0, 1, 2, and so on.

### Scalability:

Partitions allow Kafka to scale horizontally. More partitions mean more parallelism and higher throughput.

### Example:

If you have a topic named trucks_gps with 10 partitions, each partition will store a portion of the data, and messages within each partition will be ordered.

## Offsets

### Definition:

An offset is a unique identifier for each message within a partition. It represents the position of the message in the partition.

### Immutability:

Once a message is written to a partition, it cannot be changed or deleted. This ensures data integrity.

### Usage:

Offsets help consumers keep track of their position in the partition. For example, if a consumer reads up to offset 5, it knows to start from offset 6 next time.

### Example:

In partition 0 of the trucks_gps topic, the first message will have offset 0, the second message will have offset 1, and so on.

#### Key Points

- Immutability: Data in Kafka is immutable, meaning once written, it cannot be changed. This applies to both topics and partitions.
- Limited Retention: By default, data in Kafka is retained for one week, but this is configurable.
- Partitioning Strategy: Messages are assigned to partitions randomly unless a key is provided, which helps in maintaining order within partitions.

## Producers

### Definition:

Producers are applications or systems that send data to Kafka topics. They are responsible for writing data to the topic partitions.

### Function:

Producers decide which partition to write the data to in advance. They send data to the Kafka brokers, which are the servers that store the data.

### Load Balancing:

Producers can distribute data across multiple partitions, which helps in load balancing and scalability.

### Recovery:

In case of a broker failure, producers know how to automatically recover and continue sending data to the correct partitions.

## Message Keys

### Definition:

A message key is an optional part of a Kafka message that can be used to determine the partition to which the message will be sent.

### Function:

If a key is provided, all messages with the same key will be sent to the same partition. This ensures that messages with the same key are ordered within that partition.

### Types:

The key can be of any data type, such as a string, number, or binary.

### Serialization:

Producers use serializers to convert the key and value of a message into a binary format before sending it to Kafka. Common serializers include string, integer, and JSON serializers.

### Example:

For a fleet of trucks, you might use the truck ID as the key. This ensures that all messages related to a specific truck are sent to the same partition, maintaining the order of messages for that truck.

#### Key Points

- Message Structure: A Kafka message consists of a key, value, compression type, headers, partition, offset, and timestamp.
- Hashing: The default Kafka partitioner uses a hashing algorithm (murmur2) to determine the partition for a message based on the key.
- Ordering: Using keys helps maintain the order of messages within a partition, which is crucial for certain use cases like tracking the position of trucks.

## Kafka Message Anatomy

A Kafka message consists of several components that work together to ensure efficient data streaming and processing. Here's a detailed breakdown:

### Key:

An optional part of the message that can be used to determine the partition to which the message will be sent. The key helps maintain the order of messages within a partition.

### Value:

The actual content of the message. This is the data that you want to transmit. It can be any type of data, such as a string, number, or binary.

### Headers:

Optional metadata for the message, represented as a list of key-value pairs. Headers can be used to add additional context or information to the message.

### Partition:

The specific partition within the topic to which the message is sent. The partition helps in distributing the load and ensuring scalability.

### Offset:

A unique identifier for each message within a partition. It represents the position of the message in the partition and helps consumers keep track of their position.

### Timestamp:

The time when the message was created. This can be set by the system or by the user.

### Compression:

The mechanism used to compress the message, such as gzip, snappy, lz4, or zstd. Compression helps reduce the size of the message and improve performance.

## Serialization

### Definition:

Serialization is the process of converting the key and value of a message into a binary format before sending it to Kafka. Kafka only accepts a series of bytes as input from producers and sends bytes as output to consumers.

### Common Serializers:

Kafka comes with common serializers such as string, integer, JSON, Avro, and Protobuf serializers.

### Example:

If you have a key object (e.g., truck ID 123) and a value object (e.g., string)

## Message Key Hashing in Kafka

### Purpose:

Hashing of message keys in Kafka is used to determine the partition to which a message will be sent. This ensures that all messages with the same key are consistently sent to the same partition, maintaining their order.

### Process:

When a producer sends a message with a key, Kafka uses a partitioner to determine the partition. The default partitioner in Kafka uses the murmur2 hashing algorithm.

### Hashing Algorithm:

The murmur2 algorithm takes the key's bytes and produces a hash value. This hash value is then used to determine the partition by taking the modulus of the hash value with the number of partitions. The formula is:

        partition = hash(key) % number_of_partitions

This ensures a uniform distribution of messages across partitions.

### Example:

If you have a key truckID123 and a topic with 10 partitions, the murmur2 algorithm will hash truckID123 to a specific value. If the hash value is 25, the partition will be 25 % 10 = 5. Thus, the message will be sent to partition 5.

### Benefits:

This hashing mechanism ensures that messages with the same key are always sent to the same partition, which is crucial for maintaining the order of messages within that partition.

## Consumers

### Definition:

Consumers are applications or systems that read data from Kafka topics. They are responsible for fetching data from the topic partitions.

### Pull Model:

Consumers implement a pull model, meaning they request data from Kafka brokers (servers) and receive a response back. Kafka does not push data to consumers.

### Partition Assignment:

Consumers can read from one or more partitions. They automatically know which broker to read from and can recover in case of broker failure.

### Ordering:

Consumers read data in order within each partition, from the lowest offset to the highest. However, there is no ordering guarantee across different partitions.

### Consumer Groups:

Multiple consumers can form a consumer group to share the work of consuming data from a topic. Each partition is assigned to only one consumer within a group, ensuring that each message is processed only once by the group.

## Deserialization

### Definition:

Deserialization is the process of converting the binary data received from Kafka into objects or data types that the application can use.

### Key and Value:

Each Kafka message consists of a key and a value, both in binary format. Deserializers transform these bytes into the expected data types.

### Deserializers:

Kafka provides built-in deserializers for common data types such as strings, integers, JSON, Avro, and Protobuf. Custom deserializers can also be implemented.

### Example:

If a consumer expects the key to be an integer and the value to be a string, it will use an integer deserializer for the key and a string deserializer for the value. For instance, a key of bytes 123 is deserialized to the integer 123, and a value of bytes hello world is deserialized to the string hello world.

### Consistency:

The consumer must know the format of the messages in advance. If the format changes, it can break the deserialization process. To change the data format, a new topic should be created, and consumers should be updated to read from the new topic.

## Consumer Groups

### Definition:

A consumer group is a group of consumers that work together to consume data from Kafka topics. Each consumer in the group reads data from different partitions of the topic, ensuring that the workload is distributed.

### Function:

When a consumer group is created, Kafka assigns each partition of the topic to a single consumer within the group. This ensures that each message is processed only once by the group.

### Scalability:

Consumer groups allow for horizontal scaling. By adding more consumers to the group, you can increase the rate at which data is consumed from the topic.

### Example:

If you have a topic with 5 partitions and a consumer group with 3 consumers, each consumer will read from one or more partitions. If you add a fourth consumer, it will be assigned a partition, and the workload will be redistributed.

## Consumer Offsets

### Definition:

An offset is a unique identifier for each message within a partition. Consumer offsets track the position of the last message read by a consumer in a partition.

### Function:

Offsets help consumers keep track of their position in the partition. When a consumer reads a message, it commits the offset, indicating that it has successfully processed the message.

### Storage:

Kafka stores consumer offsets in a special topic called \_\_consumer_offsets. This allows consumers to resume reading from the last committed offset in case of a failure or restart.

### Example:

If a consumer reads up to offset 100 in partition 0, it will commit this offset. If the consumer restarts, it will start reading from offset 101.

### Commit Strategies:

There are different strategies for committing offsets:

- Automatic Commit: Offsets are committed automatically at regular intervals. This is the default behavior in Kafka.
- Manual Commit: The application explicitly commits offsets after processing the messages. This provides more control but requires additional implementation.

#### Key Points

- At Least Once: By default, Kafka ensures that each message is processed at least once. This means that in case of a failure, a message might be processed more than once.
- At Most Once: If offsets are committed before processing the message, it ensures that each message is processed at most once. However, some messages might be lost in case of a failure.
- Exactly Once: Kafka provides mechanisms to achieve exactly-once semantics, ensuring that each message is processed exactly once. This is crucial for applications where data consistency is critical.

## Brokers

### Definition:

A Kafka broker is a server that receives and sends data. It is part of a Kafka cluster, which is an ensemble of multiple brokers.

### Identification:

Each broker is identified by an ID, which is an integer (e.g., broker 101, broker 102).

### Function:

Brokers store data in topic partitions and manage the data flow between producers and consumers. They also handle replication of data for fault tolerance.

### Bootstrap Broker:

When a client (producer or consumer) connects to any Kafka broker (bootstrap broker), it automatically learns about the entire Kafka cluster, including all the brokers and their partitions. This means you only need to know one broker to connect to the entire cluster.

### Scalability:

Kafka clusters can scale horizontally by adding more brokers. A typical starting point is three brokers, but large clusters can have over 100 brokers.

## Topics

### Definition:

A topic in Kafka is a category or feed name to which records are sent by producers. It is similar to a table in a database but without constraints.

### Partitions:

Each topic is divided into partitions, which are ordered, immutable sequences of records. Partitions allow Kafka to scale horizontally and handle large volumes of data.

### Data Distribution:

Data in a topic is distributed across multiple brokers. For example, a topic with three partitions might have partition 0 on broker 101, partition 1 on broker 103, and partition 2 on broker 102.

### Replication:

Partitions can be replicated across multiple brokers to ensure fault tolerance. If one broker fails, the data is still available on other brokers.

### Horizontal Scaling:

The more partitions a topic has, the more scalable it is. Adding more brokers and partitions allows Kafka to handle more data and higher throughput.

### Example:

If you have a topic A with three partitions and three brokers (101, 102, 103), the partitions will be distributed across the brokers. For instance, partition 0 might be on broker 101, partition 1 on broker 103, and partition 2 on broker 102.

## Topic-Broker Relation

### Definition:

In Kafka, a topic is divided into partitions, and each partition is stored on a broker (server).

### Data Distribution:

Data for a topic is distributed across multiple brokers to ensure scalability and fault tolerance.

### Example:

If you have a topic user-logs with 3 partitions and 3 brokers (Broker 101, 102, 103), the partitions might be distributed as follows:

Partition 0 on Broker 101
Partition 1 on Broker 102
Partition 2 on Broker 103

This setup ensures that the data is spread out and can be processed in parallel.

## Kafka Broker Discovery

### Bootstrap Broker:

When a client (producer or consumer) connects to any Kafka broker, known as a bootstrap broker, it automatically learns about the entire Kafka cluster.

### Metadata Request:

The client initiates a connection to a bootstrap broker and sends a metadata request.

### Cluster Information:

The bootstrap broker responds with a list of all brokers in the cluster, as well as information about which broker has which partition.

### Automatic Connection:

Using this information, the client can then connect to the appropriate broker for producing or consuming data.

### Example:

If you have five brokers in your Kafka cluster, you only need to connect to one bootstrap broker. The client will receive the list of all brokers and their partitions, allowing it to interact with the entire cluster efficiently.

## Replication Factor in Kafka

### Definition:

The replication factor in Kafka determines the number of copies of a topic's data that are maintained across different brokers in the Kafka cluster.

### Purpose:

It ensures fault tolerance and high availability. If one broker fails, the data is still available on other brokers.

### Common Values:

In production environments, a replication factor of 3 is common. This means each piece of data is stored on three different brokers.

### Example:

If you have a topic with a replication factor of 2, each partition of the topic will have two copies on different brokers. If one broker goes down, the data is still accessible from the other broker.

## Leader of a Partition in Kafka

### Definition:

In Kafka, each partition has one broker that acts as the leader for that partition. The leader is responsible for all read and write operations for the partition.

### Role:

The leader broker handles all the data requests (both producing and consuming) for its partition. It ensures that data is written to the partition and read from it correctly.

### Replication:

Other brokers that store copies of the partition are called followers. They replicate the data from the leader to ensure fault tolerance.

### Failover:

If the leader broker fails, one of the follower brokers is automatically promoted to be the new leader. This ensures that the partition remains available and that data can still be read and written.

### Example:

If you have a partition 0 of topic A with a replication factor of 3, broker 101 might be the leader, and brokers 102 and 103 might be followers. All read and write operations for partition 0 will go through broker 101. If broker 101 fails, broker 102 or 103 will become the new leader.

## Producer Acknowledgments in Kafka

Producer acknowledgments (acks) in Kafka determine how many acknowledgments the producer requires the leader to have received before considering a request complete. This setting impacts the durability and reliability of the messages sent by the producer.

### acks=0:

The producer does not wait for any acknowledgment from the broker. This means the producer will not know if the message was successfully received, leading to possible data loss if the broker fails.

### acks=1:

The producer waits for an acknowledgment from the leader broker only. This provides limited data loss protection, as the message is considered successfully written once the leader broker acknowledges it, but before the replicas confirm it.

### acks=all:

The producer waits for acknowledgments from the leader and all in-sync replicas (ISRs). This provides the highest level of data durability, ensuring no data loss as long as at least one in-sync replica is alive.

### Example:

If you have a Kafka topic with a replication factor of 3 and set acks=all, the producer will wait for acknowledgments from the leader and the two replicas before considering the message successfully written. This ensures that the message is safely stored across multiple brokers, providing high fault tolerance.

## Role of Zookeeper in Kafka

### Broker Management:

Zookeeper keeps a list of Kafka brokers and helps manage them. It tracks which brokers are alive and available.

### Leader Election:

When a broker goes down, Zookeeper helps perform a leader election to choose a new leader for the partitions that were managed by the failed broker.

### Notifications:

Zookeeper sends notifications to Kafka brokers in case of changes, such as the creation of new topics, broker failures, or topic deletions.

### Metadata Storage:

Zookeeper stores a lot of Kafka metadata, such as broker information and topic configurations.

### Transition to KRaft:

Kafka is transitioning to work without Zookeeper using the Kafka Raft (KRaft) mechanism, but Zookeeper is still necessary for managing Kafka brokers in the current versions.

## Kafka KRaft Mode

### Definition:

Kafka KRaft (Kafka Raft) mode is a new architecture in Kafka that removes the dependency on Zookeeper. It simplifies the architecture by integrating the consensus protocol directly into Kafka.

### Purpose:

KRaft mode addresses scaling issues and improves stability, making it easier to maintain and set up Kafka clusters. It also provides a single security model and faster controller shutdown and recovery times.

### Implementation:

KRaft mode has been production-ready since Kafka version 3.3.1. Kafka 4.0 will only support KRaft mode, with no Zookeeper support.

### Benefits:

KRaft mode allows Kafka to scale to millions of partitions, improves monitoring, support, and administration, and provides performance improvements.
