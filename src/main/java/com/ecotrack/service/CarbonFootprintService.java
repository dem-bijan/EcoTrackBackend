package com.ecotrack.service;


import com.ecotrack.entity.CarbonFootprint;
import com.ecotrack.entity.User;
import com.ecotrack.repository.CarbonFootprintRepository;
import com.ecotrack.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class CarbonFootprintService {
    private final CarbonFootprintRepository carbonFootprintRepository;
    private final UserRepository userRepository;

    public BigDecimal getUserCarbonFootprint(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        BigDecimal total =  carbonFootprintRepository.getTotalImpactByUserId(user.getId());
        return total != null ? total : BigDecimal.ZERO ;
    }



}
