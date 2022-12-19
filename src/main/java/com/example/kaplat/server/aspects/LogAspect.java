package com.example.kaplat.server.aspects;

import com.example.kaplat.server.RequestCounterController;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Duration;
import java.time.Instant;

@Aspect
@Component
public class LogAspect {
    @Autowired
    private RequestCounterController requestCounterController;

    private final Logger logger = LogManager.getLogger("request-logger");

    @Around("execution(* com.example.kaplat.server.ApiControllers.*.*(..))")
    public Object logAroundRequest(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        Instant startRequest = Instant.now();
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        requestCounterController.increaseCounter();
        logger.info("Incoming request | #{} | resource: {} | HTTP Verb {}",
                requestCounterController.getRequestCounter(), request.getRequestURI(), request.getMethod());
        Object process = proceedingJoinPoint.proceed();
        Instant endRequest = Instant.now();
        logger.debug("request #{} duration: {}",
                requestCounterController.getRequestCounter(), Duration.between(startRequest, endRequest).toMillis());
        return process;
    }
}
