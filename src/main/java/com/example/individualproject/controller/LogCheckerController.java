package com.example.individualproject.controller;

import com.example.individualproject.service.LogCheckerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/log-checker")
public class LogCheckerController {
    @Autowired
    private LogCheckerService logCheckerService;

    @PostMapping("/check")
    public ResponseEntity<Map<String, String>> checkLogs(@RequestParam String path) {
        if (path == null || path.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "path cannot be empty"));
        }
        try {
            logCheckerService.checkLogs(path);
            logCheckerService.writeOutput();
            return ResponseEntity.ok(Map.of("message", "Success"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "something went wrong, error : " + e.getMessage()));
        }
    }
}
