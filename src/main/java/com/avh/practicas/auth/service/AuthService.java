package com.avh.practicas.auth.service;

import com.avh.practicas.auth.dto.LoginResponse;

public interface AuthService {

    LoginResponse login(String correo, String password);

    void cambiarPassword(Long id, String nuevaPassword);

    void recuperar(String correo);

    void resetear(String token, String nuevaPassword);
}
