package com.arenahub.presentation.auth;

import com.arenahub.application.auth.port.in.ForgotPasswordUseCase;
import com.arenahub.application.auth.port.in.ResetPasswordUseCase;
import com.arenahub.presentation.auth.dto.ForgotPasswordRequest;
import com.arenahub.presentation.auth.dto.ResetPasswordRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth/password")
public class PasswordController {

    private final ForgotPasswordUseCase forgotPasswordUseCase;
    private final ResetPasswordUseCase resetPasswordUseCase;

    public PasswordController(ForgotPasswordUseCase forgotPasswordUseCase,
                               ResetPasswordUseCase resetPasswordUseCase) {
        this.forgotPasswordUseCase = forgotPasswordUseCase;
        this.resetPasswordUseCase = resetPasswordUseCase;
    }

    @PostMapping("/forgot")
    public ResponseEntity<Void> forgot(@Valid @RequestBody ForgotPasswordRequest req) {
        forgotPasswordUseCase.execute(new ForgotPasswordUseCase.Command(req.email()));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset")
    public ResponseEntity<Void> reset(@Valid @RequestBody ResetPasswordRequest req) {
        resetPasswordUseCase.execute(new ResetPasswordUseCase.Command(req.token(), req.newPassword()));
        return ResponseEntity.ok().build();
    }
}
