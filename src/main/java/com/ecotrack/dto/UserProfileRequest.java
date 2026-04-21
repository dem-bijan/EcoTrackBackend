package com.ecotrack.dto;

import lombok.Data;
import java.util.Map;

@Data
public class UserProfileRequest {
    private Map<String,Object> Location;
    private String housingType;
    private Integer householdSize;
    private String dietType;
    private String vehicleType;
    private Integer annualMileage;
    private String energySource;
    private Map<String, Object> preferences;
}
