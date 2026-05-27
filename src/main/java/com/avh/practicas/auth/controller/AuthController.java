package com.avh.practicas.auth.controller;

import com.avh.practicas.auth.dto.CambiarPasswordRequest;
import com.avh.practicas.auth.dto.LoginRequest;
import com.avh.practicas.auth.dto.LoginResponse;
import com.avh.practicas.auth.dto.MensajeResponse;
import com.avh.practicas.auth.dto.RecuperarPasswordRequest;
import com.avh.practicas.auth.dto.ResetearPasswordRequest;
import com.avh.practicas.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request.getCorreo(), request.getPassword());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/cambiar-password")
    public ResponseEntity<MensajeResponse> cambiarPassword(@Valid @RequestBody CambiarPasswordRequest request) {
        authService.cambiarPassword(request.getId(), request.getNuevaPassword());
        return ResponseEntity.ok(new MensajeResponse("Contraseña actualizada correctamente"));
    }

    @PostMapping("/recuperar")
    public ResponseEntity<MensajeResponse> recuperar(@Valid @RequestBody RecuperarPasswordRequest request) {
        authService.recuperar(request.getCorreo());
        return ResponseEntity.ok(new MensajeResponse(
                "Si el correo está registrado, recibirá instrucciones para recuperar su contraseña"));
    }

    @PostMapping("/resetear")
    public ResponseEntity<MensajeResponse> resetear(@Valid @RequestBody ResetearPasswordRequest request) {
        authService.resetear(request.getToken(), request.getNuevaPassword());
        return ResponseEntity.ok(new MensajeResponse("Contraseña restablecida correctamente"));
    }
}
