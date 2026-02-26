package com.masterannonce.infrastructure.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Aspect AOP pour le logging transversal des services.
 * Log automatiquement : entrée (arguments sans secrets), durée d'exécution, sortie, et exceptions.
 */
@Aspect
@Component
public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    /**
     * Intercepte toutes les méthodes publiques des services.
     */
    @Around("execution(* com.masterannonce.application.service.*.*(..))")
    @SuppressWarnings("java:S2139") // Intentionnel : l'aspect AOP doit logger ET renvoyer l'exception
    public Object logServiceMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        String args = sanitizeArgs(joinPoint.getArgs());

        log.info("→ {}.{}({})", className, methodName, args);
        long startTime = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;

            log.info("← {}.{} [{}ms] → OK", className, methodName, duration);
            return result;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("✖ {}.{} [{}ms] → {} : {}", className, methodName, duration,
                e.getClass().getSimpleName(), e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Masque les arguments sensibles (password, token, secret).
     */
    private String sanitizeArgs(Object[] args) {
        if (args == null || args.length == 0) return "";
        return Arrays.stream(args)
            .map(arg -> {
                if (arg == null) return "null";
                String str = arg.toString();
                // Masquer les champs sensibles
                if (str.toLowerCase().contains("password") || str.toLowerCase().contains("token")
                    || str.toLowerCase().contains("secret")) {
                    return "[REDACTED]";
                }
                // Tronquer les arguments trop longs
                if (str.length() > 100) {
                    return str.substring(0, 100) + "...";
                }
                return str;
            })
            .collect(Collectors.joining(", "));
    }
}
