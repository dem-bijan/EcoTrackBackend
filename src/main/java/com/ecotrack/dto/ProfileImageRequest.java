package com.ecotrack.dto;

import lombok.Data;

@Data
public class ProfileImageRequest {

    private String base64Image;
    private String contentType;


}
