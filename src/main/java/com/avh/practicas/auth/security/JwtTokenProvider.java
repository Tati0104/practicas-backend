package com.avh.practicas.auth.security;

import com.avh.practicas.auth.entity.Usuario;
import com.avh.practicas.shared.enums.Rol;
import com.avh.practicas.shared.enums.Scope;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private static final String CLAIM_ROL = "rol";
    private static final String CLAIM_SCOPE = "scope";

    private final SecretKey signingKey;
    private final long expirationMs;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long expirationMs) {
        this.signingKey = buildSigningKey(secret);
        this.expirationMs = expirationMs;
    }

    public String generarToken(Usuario usuario) {
        Date ahora = new Date();
        Date expiracion = new Date(ahora.getTime() + expirationMs);

        return Jwts.builder()
                .setSubject(usuario.getCorreo())
                .claim(CLAIM_ROL, usuario.getRol().name())
                .claim(CLAIM_SCOPE, usuario.getScope().name())
                .setIssuedAt(ahora)
                .setExpiration(expiracion)
                .signWith(signingKey, SignatureAlgorithm.HS512)
                .compact();
    }

    public boolean validarToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    public String obtenerCorreo(String token) {
        return parseClaims(token).getSubject();
    }

    public Rol obtenerRol(String token) {
        String rol = parseClaims(token).get(CLAIM_ROL, String.class);
        return Rol.valueOf(rol);
    }

    public Scope obtenerScope(String token) {
        String scope = parseClaims(token).get(CLAIM_SCOPE, String.class);
        return Scope.valueOf(scope);
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private SecretKey buildSigningKey(String secret) {
        try {
            byte[] keyBytes = MessageDigest.getInstance("SHA-512")
                    .digest(secret.getBytes(StandardCharsets.UTF_8));
            return Keys.hmacShaKeyFor(keyBytes);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("No se pudo inicializar la clave de firma JWT", ex);
        }
    }
}
