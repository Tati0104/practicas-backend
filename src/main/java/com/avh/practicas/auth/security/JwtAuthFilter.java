package com.avh.practicas.auth.security;

import com.avh.practicas.shared.enums.Rol;
import com.avh.practicas.shared.enums.Scope;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String token = extraerToken(request);

        if (token != null && jwtTokenProvider.validarToken(token)) {
            String correo = jwtTokenProvider.obtenerCorreo(token);
            Rol rol = jwtTokenProvider.obtenerRol(token);
            Scope scope = jwtTokenProvider.obtenerScope(token);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(correo, null, construirAuthorities(rol, scope));

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    private String extraerToken(HttpServletRequest request) {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            return null;
        }
        return authorization.substring(BEARER_PREFIX.length()).trim();
    }

    private List<SimpleGrantedAuthority> construirAuthorities(Rol rol, Scope scope) {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();

        if (rol != null) {
            authorities.add(new SimpleGrantedAuthority(rol.name()));
            authorities.add(new SimpleGrantedAuthority("ROLE_" + rol.name()));
        }

        if (scope != null) {
            authorities.add(new SimpleGrantedAuthority(scope.name()));
            authorities.add(new SimpleGrantedAuthority("SCOPE_" + scope.name()));
        }

        if (rol == Rol.EMPRESA || rol == Rol.COORD_PRACTICA) {
            authorities.add(new SimpleGrantedAuthority("EMPRESA_LISTAR"));
        }

        return authorities;
    }
}
