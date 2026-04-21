package com.ecotrack.entity;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
@Data
@Entity
@Table(name = "user_profiles")
public class UserProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    // The critical link to the Users table!
    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;
    @Column(name = "housing_type")
    private String housingType;
    @Column(name = "household_size")
    private Integer householdSize;
    @Column(name = "diet_type")
    private String dietType;
    @Column(name = "vehicle_type")
    private String vehicleType;
    @Column(name = "annual_mileage")
    private Integer annualMileage;
    @Column(name = "energy_source")
    private String energySource;
    // This perfectly maps your Postgres JSONB column!
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> preferences;
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}