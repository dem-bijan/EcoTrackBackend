package com.ecotrack.repository;

import com.ecotrack.entity.ProfileImage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProfileImageRepository extends MongoRepository<ProfileImage, String>{
    Optional<ProfileImage> findByUserId(String userId) ;




}
