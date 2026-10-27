package com.smartshopping.aichat.dto;

import jakarta.validation.constraints.NotBlank;

public record ChatRequest(
        @NotBlank String message,
        Double latitude,
        Double longitude
) {}
