package com.avh.practicas.shared.pattern.proxy;

import com.avh.practicas.shared.exception.NegocioException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class ScopeGuardInterceptor implements HandlerInterceptor {

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
                throw new NegocioException("Acceso denegado. Se requiere autenticación para el scope: " + requiredScope);
            }

            boolean isAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> "ADMIN".equalsIgnoreCase(a.getAuthority())
                            || "ROLE_ADMIN".equalsIgnoreCase(a.getAuthority()));

            if (isAdmin) {
                return true;
            }

            boolean hasAuthority = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equalsIgnoreCase(requiredScope));

            if (!hasAuthority) {
                throw new NegocioException("Acceso denegado. No posee el permiso (scope) requerido: " + requiredScope);
            }
        }

        return true;
    }
}
