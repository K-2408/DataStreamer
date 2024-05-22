package com.testingone.example.testingone;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoChangeStreamCursor;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.changestream.ChangeStreamDocument;
import com.mongodb.client.model.changestream.FullDocument;
import jakarta.annotation.PreDestroy;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static com.mongodb.client.model.Filters.gt;

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

    private volatile boolean running = true;

    @PreDestroy
    public void shutdown() {
        running = false;
    }


    @PostMapping("/start-stream-twitter")
    public String startStream(@RequestHeader("Authorization") String authToken) {
        // verifying a client
        String clientId = isVerified(authToken);
        if(clientId.equals("Unauthorized")) {
            return "Unauthorized";
        }

        String collectionName="collectionTestOne";

        // Log client activity (you can enhance this as needed)
        System.out.println("Client " + clientId + " started streaming");

        //Creating topic for the first time if it does not exist. Can dynamically change the topic name afterwards if required.
        String topicName = "topic";
        createTopicIfNotExists(topicName, defaultPartitions);

        // Fetch and process documents in batches
        String lastProcessedId = getLastProcessedDocumentId(clientId, collectionName);
        processDocumentsInBatches(clientId, topicName,collectionName,lastProcessedId);
        startChangeStream(clientId,topicName,collectionName);

        return "Streaming Completed";
    }

    @PostMapping("/start-stream-reddit")
    public String startStreamReddit(@RequestHeader("Authorization") String authToken) {
        // verifying a client
        String clientId = isVerified(authToken);
        if(clientId.equals("Unauthorized")) {
            return "Unauthorized";
        }

        String collectionName="collectionRedditData";

        // Log client activity (you can enhance this as needed)
        System.out.println("Client " + clientId + " started streaming");

        //Creating topic for the first time if it does not exist. Can dynamically change the topic name afterwards if required.
        String topicName = "topic";
        createTopicIfNotExists(topicName, defaultPartitions);

        // Fetch and process documents in batches
        String lastProcessedId = getLastProcessedDocumentId(clientId, collectionName);
//        System.out.println(lastProcessedId);
        processDocumentsInBatches(clientId, topicName,collectionName,lastProcessedId);
        startChangeStream(clientId,topicName,collectionName);

        return "Streaming Completed";
    }

    @PostMapping("/reset-offset")
    public String resetOffset(@RequestHeader("Authorization") String authToken, @RequestParam String collectionName) {
        String clientId = isVerified(authToken);
        if (clientId.equals("Unauthorized")) {
            return "Unauthorized";
        }

        resetClientOffset(clientId, collectionName);
        return "Offset reset successfully for client: " + clientId + " and collection: " + collectionName;
    }

    public String isVerified(String authToken){
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

    @Async("taskExecutor")
    private void processDocumentsInBatches(String clientId, String topicName,String collectionName,String lastProcessedId) {
        int batchSize = 100; // Adjust the batch size as needed
        Bson filter = new Document();
        if (lastProcessedId != null) {
            try {
                ObjectId lastProcessedObjectId = new ObjectId(lastProcessedId);
                filter = gt("_id", lastProcessedObjectId);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                System.out.println("Invalid lastProcessedId format. Starting from the beginning.");
            }
        }
        MongoCursor<Document> cursor = mongoTemplate.getCollection(collectionName).find(filter).batchSize(batchSize).iterator();

        List<Document> batch = new ArrayList<>();
        System.out.println("cursor.hasNext() " +cursor.hasNext());
        while (cursor.hasNext()) {
            batch.add(cursor.next());
            if (batch.size() == batchSize || !cursor.hasNext()) {
                List<Document> currentBatch = new ArrayList<>(batch);
                System.out.println("current batch is " + currentBatch);
                processBatch(currentBatch, clientId, topicName,collectionName);
                batch.clear();
            }
        }
    }

    @Async("taskExecutor")
    private void processBatch(List<Document> batch, String clientId, String topicName,String collectionName) {
        List<String> jsonDocuments = new ArrayList<>();
        Document lastDocument = null;
        for (Document document : batch) {
            try {
                String jsonDocument = objectMapper.writeValueAsString(document);
                jsonDocuments.add(jsonDocument);
                lastDocument = document;
                if (jsonDocuments.size() == 5) {
                    sendBatch(jsonDocuments, clientId, topicName,document.getObjectId("_id").toHexString(),collectionName);
                    jsonDocuments.clear();
                }
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        // Send any remaining documents if the count is less than 10
        if (!jsonDocuments.isEmpty()) {
            sendBatch(jsonDocuments, clientId, topicName,lastDocument.getObjectId("_id").toHexString(),collectionName);
        }
    }

    @Async("taskExecutor")
    private void sendBatch(List<String> jsonDocuments, String clientId, String topicName,String lastProcessesId,String collectionName) {
        try {
            String jsonArray = objectMapper.writeValueAsString(jsonDocuments);
            kafkaProducerService.sendMessage(topicName, jsonArray);
            updateLastProcessedDocumentId(clientId,collectionName,lastProcessesId);
            System.out.println(clientId + " Batch: " + jsonArray);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    @Async("taskExecutor")
    private void startChangeStream(String clientId, String topicName, String collectionName) {
        while (running) {
            try (MongoChangeStreamCursor<ChangeStreamDocument<Document>> cursor = (MongoChangeStreamCursor<ChangeStreamDocument<Document>>) mongoTemplate
                    .getCollection(collectionName)
                    .watch()
                    .fullDocument(FullDocument.UPDATE_LOOKUP)
                    .iterator()) {
                long startTime = System.currentTimeMillis();
                boolean dataReceived = false;

                while (cursor.hasNext() && running) {
                    ChangeStreamDocument<Document> change = cursor.next();
                    Document document = change.getFullDocument();
                    if (document != null) {
                        processDocument(document, clientId, topicName,collectionName);
                        dataReceived = true;
                        startTime = System.currentTimeMillis();
                    }
                }

                // Check if data was received within 10 seconds
                if (!dataReceived && (System.currentTimeMillis() - startTime) >= 10000) {
                    System.out.println("No data received for 10 seconds. Exiting...");
                    return;
                }

            } catch (Exception e) {
//                e.printStackTrace();
                System.out.println("Destroying Threads");
                System.out.println("Change stream cursor disconnected.");
                return;
            }
        }
    }

    @Async("taskExecutor")
    private void processDocument(Document document, String clientId, String topicName,String collectionName) {
        try {
            String jsonDocument = objectMapper.writeValueAsString(document);
            kafkaProducerService.sendMessage(topicName, jsonDocument);
            updateLastProcessedDocumentId(clientId,collectionName,document.getObjectId("_id").toHexString());
            System.out.println(clientId + " Document: " + jsonDocument);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }


    private String getLastProcessedDocumentId(String clientId, String collectionName) {
        Document offsetDoc = mongoTemplate.getCollection("clientOffsets")
                .find(new Document("clientId", clientId).append("collectionName", collectionName))
                .limit(1)
                .first();
        return offsetDoc != null ? offsetDoc.getString("lastProcessedId") : null;
    }

    private void updateLastProcessedDocumentId(String clientId, String collectionName, String lastProcessedId) {
        mongoTemplate.getCollection("clientOffsets").updateOne(
                new Document("clientId", clientId).append("collectionName", collectionName),
                new Document("$set", new Document("lastProcessedId", lastProcessedId)),
                new com.mongodb.client.model.UpdateOptions().upsert(true)
        );
    }

    private void resetClientOffset(String clientId, String collectionName) {
        mongoTemplate.getCollection("clientOffsets").deleteOne(
                new Document("clientId", clientId).append("collectionName", collectionName)
        );
    }

}