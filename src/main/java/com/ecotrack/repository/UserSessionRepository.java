package com.ecotrack.repository;

import com.ecotrack.entity.UserSession;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserSessionRepository extends MongoRepository<UserSession, String> {
    // Spring Data will provide basic CRUD methods.
}
