package com.ecotrack.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "Email cannot be empty")
    @Email(regexp = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$", message = "Not a valid email syntax")
    private String email;

    @NotBlank(message = "Full name cannot be empty")
    @Size(min = 2, max = 32, message = "Full name must be between 2 and 100 characters")
    private String fullname;

    @NotBlank(message = "Password cannot be empty")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String passwordhash; //Raw Password from nextjs

}
