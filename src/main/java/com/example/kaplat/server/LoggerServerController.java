package com.example.kaplat.server;

import com.example.kaplat.server.enums.HttpResponseCode;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/logs")
public class LoggerServerController {
    private final Logger logger = LogManager.getLogger(LoggerServerController.class);

    @GetMapping("/level")
    public ResponseEntity<String> getLoggerLevel(@RequestParam(name = "logger-name") String loggerName) {
        try {
            Logger selectedLogger = LogManager.getLogger(loggerName);
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
            Logger selectedLogger = LogManager.getLogger(loggerName);
            Configurator.setLevel(selectedLogger, Level.getLevel(loggerLevel));
            return ResponseEntity.status(HttpResponseCode.OK_RESPONSE.getResponseCode()).body(selectedLogger.getLevel().name());
        } catch (Exception e) {
            String errorMessage = "an error occurred while trying to get logger level";
            return ResponseEntity.status(HttpResponseCode.CONFLICT_RESPONSE.getResponseCode()).body(errorMessage);
        }
    }
}
