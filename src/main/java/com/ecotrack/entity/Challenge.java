package com.ecotrack.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Entity
@Table(name = "Challenges")
public class Challenge {

    @Id
    @jakarta.persistence.GeneratedValue(strategy = jakarta.persistence.GenerationType.UUID)
    private UUID id;

    @Column(nullable = false,unique = true)
    private String code;

    private String title;
    private String description;

    @Column(nullable = false)
    private String type; // ACTIVITY_COUNT, CATEGORY_SUM, SCORE_GOAL

    private String category; // transportation, food, etc. (Optional)

    @Column(name = "target_value", precision = 10, scale = 2)
    private BigDecimal targetValue; // e.g., 5 (for logs) or 10.0 (for kg saved)

    private String difficulty; // easy, medium, hard

    private Integer rewardPoints;
}
