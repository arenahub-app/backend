package com.arenahub.presentation.auth.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record RegisterRequest(
        @NotBlank @Size(min = 2, max = 100) String name,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8, max = 72)
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z\\d]).+$",
                 message = "Senha deve ter maiúscula, minúscula, número e caractere especial")
        String password,
        @NotBlank @Pattern(regexp = "\\d{10,11}", message = "Telefone deve ter 10 ou 11 dígitos")
        String phone,
        @NotNull @Past LocalDate birthDate
) {}
