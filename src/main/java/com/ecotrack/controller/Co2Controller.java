package com.ecotrack.controller;


import com.ecotrack.entity.CarbonFootprint;
import com.ecotrack.entity.User;
import com.ecotrack.repository.CarbonFootprintRepository;
import com.ecotrack.repository.UserRepository;
import com.ecotrack.service.CarbonFootprintService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/CarbonFootprints")
@CrossOrigin(origins = "http://localhost:3000")
public class Co2Controller {
    private final CarbonFootprintService carbonFootprintService;
    private final CarbonFootprintRepository footprintRepository;
    private final UserRepository userRepository;

    @GetMapping("/me")
    public ResponseEntity<BigDecimal> getMyCarbonFootprint() {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        BigDecimal carbonFootprint =  carbonFootprintService.getUserCarbonFootprint(email);

        return ResponseEntity.ok(carbonFootprint);
    }

    @GetMapping("/trends")
    public ResponseEntity<List<Map<String,Object>>> getFootprintTrends() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        List<CarbonFootprint> history = footprintRepository.findByUserIdOrderByCalculationDateAsc(user.getId());

        java.util.Map<java.time.LocalDate, List<CarbonFootprint>> groupedByDate = history.stream()
                .collect(java.util.stream.Collectors.groupingBy(CarbonFootprint::getCalculationDate));
        // 3. Sort the Dates chronologically
        List<java.time.LocalDate> sortedDates = new ArrayList<>(groupedByDate.keySet());
        java.util.Collections.sort(sortedDates);
        List<Map<String,Object>> trends = new ArrayList<>();
        // 4. Calculate the AVERAGE for each day
        for (java.time.LocalDate date : sortedDates) {
            List<CarbonFootprint> dailyFootprints = groupedByDate.get(date);

            // Extract all scores for the day and calculate the exact average
            double dailyAverage = dailyFootprints.stream()
                    .mapToDouble(fp -> fp.getTotalCo2Tons().doubleValue())
                    .average()
                    .orElse(0.0);
            // 5. Add to the final chart data
            trends.add(Map.of(
                    "date", date.toString(),
                    "score", dailyAverage
            ));
        }
        return ResponseEntity.ok(trends);

    }
}
