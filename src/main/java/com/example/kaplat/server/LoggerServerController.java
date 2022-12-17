package com.example.kaplat.server;

import com.example.kaplat.server.enums.HttpResponseCode;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Duration;
import java.time.Instant;

@RestController
@RequestMapping("/logs")
public class LoggerServerController {
    @Autowired
    private RequestCounterController requestCounterController;

    @Autowired
    private ServerController serverController;

    private Instant startRequest, endRequest;

    @GetMapping("/level")
    public ResponseEntity<String> getLoggerLevel(@RequestParam(name = "logger-name") String loggerName) {
        try {
            startRequest = Instant.now();
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            requestCounterController.increaseCounter();
            Logger selectedLogger = LogManager.getLogger(loggerName);
            endRequest = Instant.now();
            serverController.printLogForRequest(request, Duration.between(startRequest, endRequest).toMillis());
            return ResponseEntity.status(HttpResponseCode.OK_RESPONSE.getResponseCode()).body(selectedLogger.getLevel().name());
        } catch (Exception e) {
            String errorMessage = "an error occurred while trying to get logger level";
            return ResponseEntity.status(HttpResponseCode.CONFLICT_RESPONSE.getResponseCode()).body(errorMessage);
        }
    }

    @PutMapping("/level")
    public ResponseEntity<String> setLoggerLevel(@RequestParam(name = "logger-name") String loggerName,
                                                 @RequestParam(name = "logger-level") String loggerLevel) {
        try {
            startRequest = Instant.now();
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            requestCounterController.increaseCounter();
            Logger selectedLogger = LogManager.getLogger(loggerName);
            Configurator.setLevel(selectedLogger, Level.getLevel(loggerLevel));
            endRequest = Instant.now();
            serverController.printLogForRequest(request, Duration.between(startRequest, endRequest).toMillis());
            return ResponseEntity.status(HttpResponseCode.OK_RESPONSE.getResponseCode()).body(selectedLogger.getLevel().name());
        } catch (Exception e) {
            String errorMessage = "an error occurred while trying to get logger level";
            return ResponseEntity.status(HttpResponseCode.CONFLICT_RESPONSE.getResponseCode()).body(errorMessage);
        }
    }
}
