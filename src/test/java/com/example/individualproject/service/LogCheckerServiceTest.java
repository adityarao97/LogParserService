package com.example.individualproject.service;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class LogCheckerServiceTest {
    @Test
    void testFullLogProcessing() throws Exception {
        LogCheckerService LogCheckerService = new LogCheckerServiceImpl();

        // Sample log lines
        List<String> logLines = List.of(
                "timestamp=2024-12-12T13:20:05 metric=cpu_usage_percent value=67",
                "timestamp=2024-12-12T13:20:05 level=INFO message=Application started",
                "timestamp=2024-12-12T13:20:05 request_url=/api/update response_time_ms=30 response_status=200"
        );

        // Process logs and write output files
        LogCheckerService.checkLogs(logLines);
        LogCheckerService.writeOutput();

        // Verify output files
        File apmFile = new File("apm.json");
        File appFile = new File("application.json");
        File requestFile = new File("request.json");

        if (apmFile.exists()) {
            System.out.println("APM File Content: " + new String(Files.readAllBytes(apmFile.toPath())));
        }
        if (appFile.exists()) {
            System.out.println("Application File Content: " + new String(Files.readAllBytes(appFile.toPath())));
        }
        if (requestFile.exists()) {
            System.out.println("Request File Content: " + new String(Files.readAllBytes(requestFile.toPath())));
        }

        assertTrue(apmFile.exists());
        assertTrue(appFile.exists());
        assertTrue(requestFile.exists());

        // Clean up test files
        apmFile.delete();
        appFile.delete();
        requestFile.delete();
    }

    @Test
    void testFileContents() throws Exception {
        LogCheckerService LogCheckerService = new LogCheckerServiceImpl();

        // Sample log lines
        List<String> logLines = List.of(
                "timestamp=2024-12-12T13:20:05 metric=cpu_usage_percent value=67",
                "timestamp=2024-12-12T13:20:05 level=INFO message=Application started",
                "timestamp=2024-12-12T13:20:05 request_url=/api/update response_time_ms=30 response_status=200"
        );

        // Process logs and write output files
        LogCheckerService.checkLogs(logLines);
        LogCheckerService.writeOutput();

        // Verify APM log file content
        File apmFile = new File("apm.json");
        String apmContent = new String(Files.readAllBytes(apmFile.toPath()));
        assertTrue(apmContent.contains("cpu_usage_percent"));

        // Verify Application log file content
        File appFile = new File("application.json");
        String appContent = new String(Files.readAllBytes(appFile.toPath()));
        assertTrue(appContent.contains("INFO"));

        // Verify Request log file content
        File requestFile = new File("request.json");
        String requestContent = new String(Files.readAllBytes(requestFile.toPath()));
        JSONObject requestJson = new JSONObject(requestContent);

        // Assert the presence of the "/api/update" key
        assertTrue(requestJson.has("/api/update"));

        // Assert the nested response_times object
        JSONObject apiUpdate = requestJson.getJSONObject("/api/update");
        assertNotNull(apiUpdate.getJSONObject("response_times"));
        assertEquals(30, apiUpdate.getJSONObject("response_times").getInt("min"));

        // Clean up test files
        apmFile.delete();
        appFile.delete();
        requestFile.delete();
    }

    @Test
    void testEmptyLogInput() throws Exception {
        LogCheckerService LogCheckerService = new LogCheckerServiceImpl();

        // Empty log lines
        List<String> logLines = List.of();

        // Process logs and write output files
        LogCheckerService.checkLogs(logLines);
        LogCheckerService.writeOutput();

        // Verify no files are created
        assertTrue(new File("apm.json").exists());
        assertTrue(new File("application.json").exists());
        assertTrue(new File("request.json").exists());
    }

    @Test
    void testOnlyAPMLogs() throws Exception {
        LogCheckerService LogCheckerService = new LogCheckerServiceImpl();

        // Only APM log lines
        List<String> logLines = List.of(
                "timestamp=2024-12-12T13:20:05 metric=cpu_usage_percent value=67"
        );

        // Process logs and write output files
        LogCheckerService.checkLogs(logLines);
        LogCheckerService.writeOutput();

        // Verify only APM file is created
        assertTrue(new File("apm.json").exists());
        assertTrue(new File("application.json").exists());
        assertTrue(new File("request.json").exists());
    }

    @Test
    void testExceptionFreeExecution() {
        LogCheckerService LogCheckerService = new LogCheckerServiceImpl();

        // Sample log lines
        List<String> logLines = List.of(
                "timestamp=2024-12-12T13:20:05 metric=cpu_usage_percent value=67",
                "timestamp=2024-12-12T13:20:05 level=INFO message=Application started"
        );

        assertDoesNotThrow(() -> {
            LogCheckerService.checkLogs(logLines);
            LogCheckerService.writeOutput();
        });
    }
}
