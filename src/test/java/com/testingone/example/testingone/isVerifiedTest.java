package com.testingone.example.testingone;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.List;

@ExtendWith(MockitoExtension.class)
public class isVerifiedTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private TestingController testingController;

    @Mock
    private MongoCollection<Document> mockCollection;

    @Mock
    private FindIterable<Document> mockFindIterable;

    @BeforeEach
    public void setUp() {
    }

    @Test
    public void testIsVerified_validToken() {
        String authToken = "validToken";
        String clientId = "clientId123";
        Document clientDoc = new Document("authToken", authToken).append("clientId", clientId);

        when(mongoTemplate.getCollection("collectionClientData")).thenReturn(mockCollection);
        when(mockCollection.find(any(Document.class))).thenReturn(mockFindIterable);
        when(mockFindIterable.limit(1)).thenReturn(mockFindIterable);
        when(mockFindIterable.into(any(List.class))).thenAnswer(invocation -> {
            List<Document> list = invocation.getArgument(0);
            list.add(clientDoc);
            return list;
        });

        assertEquals(clientId, testingController.isVerified(authToken));
    }

    @Test
    public void testIsVerified_invalidToken() {
        String authToken = "invalidToken";

        when(mongoTemplate.getCollection("collectionClientData")).thenReturn(mockCollection);
        when(mockCollection.find(any(Document.class))).thenReturn(mockFindIterable);
        when(mockFindIterable.limit(1)).thenReturn(mockFindIterable);
        when(mockFindIterable.into(any(List.class))).thenAnswer(invocation -> {
            List<Document> list = invocation.getArgument(0);
            return list;
        });

        assertEquals("Unauthorized", testingController.isVerified(authToken));
    }
}
