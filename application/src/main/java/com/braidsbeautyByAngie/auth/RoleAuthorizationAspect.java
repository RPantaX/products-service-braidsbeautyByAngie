package com.braidsbeautyByAngie.auth;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;

@Aspect
@Component
public class RoleAuthorizationAspect {

    @Around("@annotation(requireRole)")
    public Object checkRoles(ProceedingJoinPoint joinPoint, RequireRole requireRole) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        String userRoles = request.getHeader("X-User-Roles");
        if (userRoles == null || userRoles.isEmpty()) {
            throw new SecurityException("No roles found in request");
        }

        List<String> userRolesList = Arrays.asList(userRoles.split(","));
        String[] requiredRoles = requireRole.value();

        if (requiredRoles.length == 0) {
            // Si no se especifican roles, solo necesita estar autenticado
            return joinPoint.proceed();
        }

        boolean hasAccess;
        if (requireRole.requireAll()) {
            // Requiere TODOS los roles especificados
            hasAccess = userRolesList.containsAll(Arrays.asList(requiredRoles));
        } else {
            // Requiere AL MENOS UNO de los roles especificados
            hasAccess = Arrays.stream(requiredRoles)
                    .anyMatch(userRolesList::contains);
        }

        if (!hasAccess) {
            throw new SecurityException("Insufficient privileges. Required roles: " + Arrays.toString(requiredRoles));
        }

        return joinPoint.proceed();
    }
}