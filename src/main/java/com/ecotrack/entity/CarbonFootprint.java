package com.ecotrack.entity;


import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@Entity
@Table(name="carbon_footprints")
public class CarbonFootprint {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id",nullable = false)
    private User user;

    @Column(name = "total_co2_tons",precision = 10 ,scale = 2, nullable = false)
    private BigDecimal totalCo2Tons;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name="breakdown", columnDefinition = "jsonb")
    private Map<String, Object> breakdown;

    @Column(name="calculation_date")
    private LocalDate calculationDate;

    @Column(name = "is_baseline")
    private Boolean isBaseline;

    @Column(name = "created_at")
    private Timestamp createdAt;



}
