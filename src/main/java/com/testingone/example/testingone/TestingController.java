package com.testingone.example.testingone;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCursor;
import jakarta.annotation.PreDestroy;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.web.bind.annotation.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api")
public class TestingController {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private KafkaProducerService kafkaProducerService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private KafkaAdmin kafkaAdmin;

    @Value("${kafka.default.partitions:12}")
    private int defaultPartitions;

    private static final int THREAD_POOL_SIZE = 10;
    private ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);


    @PostMapping("/start-stream-twitter")
    public String startStream(@RequestHeader("Authorization") String authToken) {
        // verifying a client
        String clientId = isVerified(authToken);
        if(clientId == "Unauthorized") {
            return "Unauthorized";
        }

        String collectionName="collectionTestOne";

        // Log client activity (you can enhance this as needed)
        System.out.println("Client " + clientId + " started streaming");

        //Creating topic for the first time if it does not exist. Can dynamically change the topic name afterwards if required.
        String topicName = "topic";
        createTopicIfNotExists(topicName, defaultPartitions);

        // Fetch and process documents in batches
        processDocumentsInBatches(clientId, topicName,collectionName);

        return "Streaming Completed";
    }

    @PostMapping("/start-stream-reddit")
    public String startStreamReddit(@RequestHeader("Authorization") String authToken) {
        // verifying a client
        String clientId = isVerified(authToken);
        if(clientId == "Unauthorized") {
            return "Unauthorized";
        }

        String collectionName="collectionRedditData";

        // Log client activity (you can enhance this as needed)
        System.out.println("Client " + clientId + " started streaming");

        //Creating topic for the first time if it does not exist. Can dynamically change the topic name afterwards if required.
        String topicName = "topic";
        createTopicIfNotExists(topicName, defaultPartitions);

        // Fetch and process documents in batches
        processDocumentsInBatches(clientId, topicName,collectionName);

        return "Streaming Completed";
    }

    private String isVerified(String authToken){
        Optional<Document> clientDocOpt = mongoTemplate.getCollection("collectionClientData")
                .find(new Document("authToken", authToken))
                .limit(1)
                .into(new ArrayList<>())
                .stream()
                .findFirst();

        if(clientDocOpt.isEmpty()){
            return "Unauthorized";
        }
        return clientDocOpt.get().getString("clientId");
    }

    private void createTopicIfNotExists(String topicName, int numPartitions) {
        try (AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
            if (!adminClient.listTopics().names().get().contains(topicName)) {
                NewTopic newTopic = new NewTopic(topicName, numPartitions, (short) 1);
                adminClient.createTopics(Collections.singleton(newTopic)).all().get();
                System.out.println("Created topic: " + topicName);
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    private void processDocumentsInBatches(String clientId, String topicName,String collectionName) {
        int batchSize = 100; // Adjust the batch size as needed
        MongoCursor<Document> cursor = mongoTemplate.getCollection(collectionName).find().batchSize(batchSize).iterator();

        List<Document> batch = new ArrayList<>();
        while (cursor.hasNext()) {
            batch.add(cursor.next());
            if (batch.size() == batchSize || !cursor.hasNext()) {
//                processBatch(batch, clientId, topicName);
//                batch.clear();
                List<Document> currentBatch = new ArrayList<>(batch);
                executorService.submit(() -> processBatch(currentBatch, clientId, topicName));
                batch.clear();
            }
        }
    }

//    private void processBatch(List<Document> batch, String clientId, String topicName) {
//        for (Document document : batch) {
//            processDocument(document,clientId,topicName);
//        }
//    }

    private void processBatch(List<Document> batch, String clientId, String topicName) {
        List<String> jsonDocuments = new ArrayList<>();
        for (Document document : batch) {
            try {
                String jsonDocument = objectMapper.writeValueAsString(document);
                jsonDocuments.add(jsonDocument);
                if (jsonDocuments.size() == 5) {
                    sendBatch(jsonDocuments, clientId, topicName);
                    jsonDocuments.clear();
                }
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        // Send any remaining documents if the count is less than 10
        if (!jsonDocuments.isEmpty()) {
            sendBatch(jsonDocuments, clientId, topicName);
        }
    }

    private void sendBatch(List<String> jsonDocuments, String clientId, String topicName) {
        try {
            String jsonArray = objectMapper.writeValueAsString(jsonDocuments);
            kafkaProducerService.sendMessage(topicName, jsonArray);
            System.out.println(clientId + " Batch: " + jsonArray);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    private void processDocument(Document document, String clientId, String topicName) {
        try {
            String jsonDocument = objectMapper.writeValueAsString(document);
            kafkaProducerService.sendMessage(topicName, jsonDocument);
            System.out.println(clientId + " Document: " + jsonDocument);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    // Remember to shut down the executor service appropriately
    @PreDestroy
    public void shutdown() {
        executorService.shutdown();
    }

}
