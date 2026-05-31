package com.avh.practicas.shared.pattern.proxy;

import com.avh.practicas.shared.exception.NegocioException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;

@Component
public class ScopeGuardInterceptor implements HandlerInterceptor {

    private static final Set<String> ROLES_DOCUMENTO = Set.of(
            "ADMIN", "DIRECCION", "COORD_ACADEMICA", "COORD_PRACTICA",
            "SECRETARIA", "DOCENTE_ASESOR", "TUTOR_EMPRESARIAL", "ESTUDIANTE"
    );

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        ScopeGuard scopeGuard = handlerMethod.getMethodAnnotation(ScopeGuard.class);

        if (scopeGuard == null) {
            scopeGuard = handlerMethod.getBeanType().getAnnotation(ScopeGuard.class);
        }

        if (scopeGuard != null) {
            String requiredScope = scopeGuard.value();
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();

            if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
                // Si no hay autenticación, para propósitos de prueba locales podemos dejar pasar 
                // o denegar de forma estricta. Lo denegamos si tiene un scope específico requerido.
                throw new NegocioException("Acceso denegado. Se requiere autenticación para el scope: " + requiredScope);
            }

            if (!tienePermiso(auth, requiredScope)) {
                throw new NegocioException("Acceso denegado. No posee el permiso (scope) requerido: " + requiredScope);
            }
        }

        return true;
    }

    private boolean tienePermiso(Authentication auth, String permisoRequerido) {
        if (tieneRol(auth, "ADMIN")) {
            return true;
        }

        boolean tienePermisoExplicito = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> authority.equalsIgnoreCase(permisoRequerido));

        if (tienePermisoExplicito) {
            return true;
        }

        if (permisoRequerido.startsWith("DOCUMENTO_")) {
            return auth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch(ROLES_DOCUMENTO::contains);
        }

        return false;
    }

    private boolean tieneRol(Authentication auth, String rol) {
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority ->
                        authority.equalsIgnoreCase(rol) || authority.equalsIgnoreCase("ROLE_" + rol));
    }
}
