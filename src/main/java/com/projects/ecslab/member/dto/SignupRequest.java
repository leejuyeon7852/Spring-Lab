package com.projects.ecslab.member.dto;

import jakarta.validation.constraints.NotBlank;

public record SignupRequest(
    @NotBlank String username,
    @NotBlank String password
){

}
