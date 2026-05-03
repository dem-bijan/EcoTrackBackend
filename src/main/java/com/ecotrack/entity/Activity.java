package com.ecotrack.entity;


import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;

import java.math.BigDecimal;
import java.util.Map;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name="activities")
public class Activity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "activity_type", nullable = false, length = 50)
    private String activityType;

    @Column(name = "activity_category", nullable = false, length = 50)
    private String activityCategory;

    @Column(columnDefinition = "TEXT")
    private String description;
    @Column(name = "co2_impact", nullable = false, precision = 10, scale = 3)
    private BigDecimal co2Impact;

    @Type(io.hypersistence.utils.hibernate.type.json.JsonType.class)
    @Column(name = "activity_data",columnDefinition = "jsonb")
    private Map<String, Object> activityData;

    @Column(name = "activity_date",nullable = false)
    private LocalDate activityDate;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;


}
