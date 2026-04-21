package com.ecotrack.entity;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.UUID;
@Data
@Document(collection = "profile_images") // This creates a NoSQL collection instead of an SQL table!
public class ProfileImage {
    @Id
    private String id;

    private String userId;

    private String contentType;

    private String imageBase64;
}
