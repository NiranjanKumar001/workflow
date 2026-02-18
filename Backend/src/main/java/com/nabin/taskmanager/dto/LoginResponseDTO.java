package com.nabin.taskmanager.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponseDTO
{
    private String token;  // JWT token
    private String type = "Bearer";  // Token type
    private UserResponseDTO user;  // User information
}