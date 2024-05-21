package com.testingone.example.testingone;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MyDocumentRepository extends MongoRepository<MyDocument, String> {
}
