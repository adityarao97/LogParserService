package com.example.individualproject.service;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;

@Getter
public abstract class LogParser {
    private LogParser nextParser;

    public void setNextParser(LogParser nextParser) {
        this.nextParser = nextParser;
    }

    public void processLogLine(String logLine) {
        if (canHandleLog(logLine)) {
            parse(logLine);
        } else if (nextParser != null) {
            nextParser.processLogLine(logLine);
        }
    }

    protected abstract boolean canHandleLog(String logLine);

    protected abstract void parse(String logLine);

    // Aggregated results and file name
    public abstract ObjectNode getAggregatedData();
    public abstract String getOutputFileName();
}
