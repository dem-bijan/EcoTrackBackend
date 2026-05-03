package com.ecotrack.entity;


import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name="recommendations")
public class Recommendation {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "action_type", nullable = false, length = 50)
    private String actionType; // energy, transport, food, shopping

    @Column(name = "estimated_impact", precision = 10, scale = 2)
    private BigDecimal estimatedImpact; // tons saved per year

    @Column(name = "estimated_cost", precision = 10, scale = 2)
    private BigDecimal estimatedCost;

    @Column(name = "difficulty_level", length = 20)
    private String difficultyLevel; // easy, medium, hard

    @Column(name = "priority_score")
    private Integer priorityScore;

    @Column(length = 20)
    private String status = "pending"; // pending, started, completed, dismissed

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

}
