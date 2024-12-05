package com.example.individualproject.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class LogCheckerServiceImpl implements LogCheckerService {

    private final LogParser logParser;

    public LogCheckerServiceImpl() {
        LogParser apmParser = new APMLogParser();
        LogParser appParser = new AppLogParser();
        LogParser requestParser = new RequestLogParser();

        apmParser.setNextParser(appParser);
        appParser.setNextParser(requestParser);

        this.logParser = apmParser;
    }

    @PostConstruct
    public void init() {
        try {
            // Load sample log file from resources
            String logFilePath = getClass().getClassLoader().getResource("logs/sample_input_logs-1.txt").getPath();
            logFilePath = java.net.URLDecoder.decode(logFilePath, "UTF-8");
            File logFile = new File(logFilePath);
            List<String> logLines = Files.readAllLines(logFile.toPath());

            // Process and write output files
            processLogs(logLines);
            writeOutput();

            System.out.println("Sample logs processed successfully.");
        } catch (Exception e) {
            System.err.println("Error processing sample log file: " + e.getMessage());
        }
    }

    public void processLogs(List<String> logLines) {
        for (String logLine : logLines) {
            logParser.processLogLine(logLine);
        }
    }

    @Override
    public void checkLogs(String path) {
        List<String> lines;
        try {
            lines = Files.readAllLines(Paths.get(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        for (String line : lines) {
            logParser.processLogLine(line);
        }
    }

    @Override
    public void checkLogs(List<String> lines) {
        for (String line : lines) {
            logParser.processLogLine(line);
        }
    }

    @Override
    public void writeOutput() {
        ObjectMapper mapper = new ObjectMapper();
        LogParser currentLogParser = logParser;

        while (currentLogParser != null) {
            ObjectNode aggregatedData = currentLogParser.getAggregatedData();
            String fileName = currentLogParser.getOutputFileName();

            try (FileWriter writer = new FileWriter(fileName)) {
                writer.write(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(aggregatedData));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            currentLogParser = currentLogParser.getNextParser();
        }
    }
}
