package com.ecotrack.repository;


import com.ecotrack.entity.CarbonFootprint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CarbonFootprintRepository extends JpaRepository<CarbonFootprint, UUID> {

    @Query("SELECT SUM(c.totalCo2Tons) FROM CarbonFootprint c WHERE c.user.id = :userId")
    BigDecimal getTotalImpactByUserId(UUID userId);

    Optional<CarbonFootprint> findFirstByUserIdOrderByCalculationDateDesc(UUID userId);

    List<CarbonFootprint> findByUserIdOrderByCalculationDateAsc(UUID userId);

}
